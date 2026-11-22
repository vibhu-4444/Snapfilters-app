-- 00003_invitations_and_recovery.sql
-- PRD v3.0 Chapter 10, 11 & 19: Security definer functions for QR Invitations and Recovery

-- 1. Redeem invitation state machine (Chapter 11)
create or replace function redeem_invitation(token_input text)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
    v_token_hash bytea;
    v_invitation record;
    v_group record;
    v_scanner_id uuid := auth.uid();
    v_request_id uuid;
begin
    if v_scanner_id is null then
        return jsonb_build_object('status', 'UNAUTHENTICATED');
    end if;

    -- Hash token using sha256
    v_token_hash := digest(token_input, 'sha256');

    -- Lookup invitation
    select * into v_invitation from group_invitations
    where token_hash = v_token_hash;

    if not found or v_invitation.revoked_at is not null then
        return jsonb_build_object('status', 'INVALID');
    end if;

    if v_invitation.expires_at < now() then
        return jsonb_build_object('status', 'EXPIRED');
    end if;

    if v_invitation.max_uses is not null and v_invitation.used_count >= v_invitation.max_uses then
        return jsonb_build_object('status', 'EXHAUSTED');
    end if;

    -- Lookup group
    select * into v_group from groups
    where id = v_invitation.group_id and deleted_at is null;

    if not found then
        return jsonb_build_object('status', 'GROUP_GONE');
    end if;

    -- Already member check
    if exists (select 1 from group_members where group_id = v_group.id and user_id = v_scanner_id) then
        return jsonb_build_object('status', 'ALREADY_MEMBER', 'group_id', v_group.id);
    end if;

    -- Already pending check
    if exists (select 1 from join_requests where group_id = v_group.id and user_id = v_scanner_id and state = 'pending') then
        return jsonb_build_object('status', 'ALREADY_PENDING', 'group_id', v_group.id);
    end if;

    -- Increment used count atomically
    update group_invitations
    set used_count = used_count + 1
    where id = v_invitation.id;

    -- Policy branching
    if v_group.joining_policy = 'direct' then
        insert into group_members (group_id, user_id, role)
        values (v_group.id, v_scanner_id, 'member');

        return jsonb_build_object('status', 'JOINED', 'group_id', v_group.id, 'group_name', v_group.name);
    else
        insert into join_requests (group_id, user_id, invitation_id, state)
        values (v_group.id, v_scanner_id, v_invitation.id, 'pending')
        returning id into v_request_id;

        return jsonb_build_object('status', 'REQUESTED', 'group_id', v_group.id, 'group_name', v_group.name, 'request_id', v_request_id);
    end if;
end;
$$;

-- 2. Rotate invitation
create or replace function rotate_invitation(p_invitation_id uuid)
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
    v_inv record;
    v_new_raw_token text;
    v_new_hash bytea;
begin
    select * into v_inv from group_invitations where id = p_invitation_id;
    if not found then
        return jsonb_build_object('error', 'Invitation not found');
    end if;

    if not is_admin(v_inv.group_id) then
        return jsonb_build_object('error', 'Unauthorized');
    end if;

    -- Revoke old
    update group_invitations set revoked_at = now() where id = p_invitation_id;

    -- Generate new 32 random bytes token
    v_new_raw_token := encode(gen_random_bytes(32), 'base64');
    v_new_hash := digest(v_new_raw_token, 'sha256');

    insert into group_invitations (group_id, token_hash, created_by, expires_at, max_uses)
    values (v_inv.group_id, v_new_hash, auth.uid(), now() + interval '7 days', 200);

    return jsonb_build_object('token', v_new_raw_token, 'expires_at', now() + interval '7 days');
end;
$$;

-- 3. Approve join request
create or replace function approve_join_request(p_request_id uuid)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
    v_req record;
begin
    select * into v_req from join_requests where id = p_request_id and state = 'pending';
    if not found then
        return false;
    end if;

    if not is_admin(v_req.group_id) then
        return false;
    end if;

    -- Atomically insert membership and mark request approved
    insert into group_members (group_id, user_id, role)
    values (v_req.group_id, v_req.user_id, 'member')
    on conflict (group_id, user_id) do nothing;

    update join_requests
    set state = 'approved', decided_at = now(), decided_by = auth.uid()
    where id = p_request_id;

    return true;
end;
$$;
