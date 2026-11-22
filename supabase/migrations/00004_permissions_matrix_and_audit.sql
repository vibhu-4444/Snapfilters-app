-- 00004_permissions_matrix_and_audit.sql
-- PRD v3.0 Chapter 10 & 12: 14-Capability Permission Matrix, Audit Logging & Admin Protection

-- 1. Group Audit Table
create table if not exists group_audit (
    id uuid primary key default gen_random_uuid(),
    group_id uuid not null references groups(id) on delete cascade,
    actor_id uuid not null references profiles(id),
    action text not null, -- 'permission_change', 'member_remove', 'member_promote', 'member_leave'
    target_id uuid references profiles(id),
    details jsonb not null,
    created_at timestamptz not null default now()
);

create index if not exists idx_group_audit_group on group_audit (group_id, created_at desc);

-- 2. Update Group Permission (Chapter 12)
create or replace function update_group_permission(
    p_group_id uuid,
    p_capability text,
    p_value text
)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_old_value text;
begin
    -- 1. Auth check
    if v_actor_id is null then
        raise exception '401: Unauthenticated';
    end if;

    -- 2. Admin check
    if not is_admin(p_group_id) then
        raise exception '403: Forbidden - Only group admins may modify permissions';
    end if;

    -- Validate capability and value
    if p_value not in ('all', 'admin') then
        raise exception 'Invalid setting value: must be "all" or "admin"';
    end if;

    -- Get old value
    v_old_value := cap(p_group_id, p_capability);

    -- Execute dynamic update for the capability column
    execute format('update group_permissions set %I = %L where group_id = %L', p_capability, p_value, p_group_id);

    -- Log to audit
    insert into group_audit (group_id, actor_id, action, details)
    values (
        p_group_id,
        v_actor_id,
        'permission_change',
        jsonb_build_object('capability', p_capability, 'old_value', v_old_value, 'new_value', p_value)
    );

    return true;
end;
$$;

-- 3. Remove Member with Last-Admin Guard (Chapter 12)
create or replace function remove_member(
    p_group_id uuid,
    p_target_user_id uuid
)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_target_role text;
    v_admin_count int;
begin
    if v_actor_id is null then
        raise exception '401: Unauthenticated';
    end if;

    -- User cannot remove themselves via member_remove; must call leave_group
    if v_actor_id = p_target_user_id then
        raise exception 'Cannot remove self via remove_member. Use leave_group instead.';
    end if;

    -- Admin or capability check
    if not (is_admin(p_group_id) or (cap(p_group_id, 'member_remove') = 'all' and is_member(p_group_id))) then
        raise exception '403: Forbidden - Not authorized to remove members';
    end if;

    select role into v_target_role from group_members
    where group_id = p_group_id and user_id = p_target_user_id;

    if not found then
        raise exception 'Target is not a member of this group';
    end if;

    -- Guard: The last admin cannot be removed or demoted
    if v_target_role = 'admin' then
        select count(*) into v_admin_count from group_members
        where group_id = p_group_id and role = 'admin';

        if v_admin_count <= 1 then
            raise exception 'Operation failed: The last admin of the group cannot be removed.';
        end if;
    end if;

    -- Remove member
    delete from group_members
    where group_id = p_group_id and user_id = p_target_user_id;

    -- Log audit
    insert into group_audit (group_id, actor_id, action, target_id, details)
    values (p_group_id, v_actor_id, 'member_remove', p_target_user_id, jsonb_build_object('role', v_target_role));

    return true;
end;
$$;

-- 4. Promote Member to Admin (Chapter 12)
create or replace function promote_member(
    p_group_id uuid,
    p_target_user_id uuid
)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
begin
    if v_actor_id is null then
        raise exception '401: Unauthenticated';
    end if;

    -- Only admin can promote
    if not (is_admin(p_group_id) or (cap(p_group_id, 'promote_admin') = 'all' and is_member(p_group_id))) then
        raise exception '403: Forbidden - Not authorized to promote members';
    end if;

    -- Guard: Target must be an existing member
    if not exists (select 1 from group_members where group_id = p_group_id and user_id = p_target_user_id) then
        raise exception 'Operation failed: User is not a member of this group.';
    end if;

    update group_members
    set role = 'admin'
    where group_id = p_group_id and user_id = p_target_user_id;

    insert into group_audit (group_id, actor_id, action, target_id, details)
    values (p_group_id, v_actor_id, 'member_promote', p_target_user_id, jsonb_build_object('new_role', 'admin'));

    return true;
end;
$$;

-- 5. Leave Group
create or replace function leave_group(p_group_id uuid)
returns boolean
language plpgsql
security definer
set search_path = public
as $$
declare
    v_actor_id uuid := auth.uid();
    v_role text;
    v_admin_count int;
begin
    if v_actor_id is null then
        raise exception '401: Unauthenticated';
    end if;

    select role into v_role from group_members
    where group_id = p_group_id and user_id = v_actor_id;

    if not found then
        return false;
    end if;

    -- Last admin cannot leave without transferring ownership
    if v_role = 'admin' then
        select count(*) into v_admin_count from group_members
        where group_id = p_group_id and role = 'admin';

        if v_admin_count <= 1 then
            raise exception 'Operation failed: The last admin cannot leave without promoting another admin.';
        end if;
    end if;

    delete from group_members
    where group_id = p_group_id and user_id = v_actor_id;

    insert into group_audit (group_id, actor_id, action, details)
    values (p_group_id, v_actor_id, 'member_leave', jsonb_build_object('role', v_role));

    return true;
end;
$$;
