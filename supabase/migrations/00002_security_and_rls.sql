-- 00002_security_and_rls.sql
-- PRD v3.0 Chapter 10 & 12: Backend security rules, RLS policies, security definer functions

-- 1. Helper functions
create or replace function is_member(g uuid) returns boolean
language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from group_members
        where group_id = g and user_id = auth.uid()
    );
$$;

create or replace function is_admin(g uuid) returns boolean
language sql stable security definer set search_path = public as $$
    select exists (
        select 1 from group_members
        where group_id = g and user_id = auth.uid() and role = 'admin'
    );
$$;

create or replace function cap(g uuid, setting text) returns text
language sql stable security definer set search_path = public as $$
    select case setting
        when 'msg_send' then msg_send
        when 'msg_reply' then msg_reply
        when 'msg_react' then msg_react
        when 'photo_share' then photo_share
        when 'file_upload' then file_upload
        when 'media_download' then media_download
        when 'member_invite' then member_invite
        when 'join_approve' then join_approve
        when 'member_remove' then member_remove
        when 'edit_info' then edit_info
        when 'manage_invitations' then manage_invitations
        when 'manage_permissions' then manage_permissions
        when 'promote_admin' then promote_admin
        when 'delete_group' then delete_group
    end from group_permissions where group_id = g;
$$;

-- 2. Profiles RLS
alter table profiles enable row level security;

create policy profiles_read on profiles for select using (
    id = auth.uid() or exists (
        select 1 from group_members mine
        join group_members theirs on theirs.group_id = mine.group_id
        where mine.user_id = auth.uid() and theirs.user_id = profiles.id
    )
);

create policy profiles_update on profiles for update
using (id = auth.uid())
with check (id = auth.uid());

-- 3. Groups RLS
alter table groups enable row level security;

create policy groups_read on groups for select using (
    deleted_at is null and is_member(id)
);

create policy groups_insert on groups for insert with check (
    owner_id = auth.uid()
);

create policy groups_update on groups for update using (
    is_admin(id) or (cap(id, 'edit_info') = 'all' and is_member(id))
);

-- 4. Group Members RLS
alter table group_members enable row level security;

create policy gm_read on group_members for select using (
    is_member(group_id)
);

create policy gm_insert on group_members for insert with check (
    is_admin(group_id) or (cap(group_id, 'member_invite') = 'all' and is_member(group_id))
);

create policy gm_delete on group_members for delete using (
    is_admin(group_id) or (cap(group_id, 'member_remove') = 'all' and is_member(group_id))
);

-- 5. Messages RLS
alter table messages enable row level security;

create policy msg_read on messages for select using (
    exists (
        select 1 from conversation_members cm
        where cm.conversation_id = messages.conversation_id
        and cm.user_id = auth.uid()
    )
);

create policy msg_insert on messages for insert with check (
    sender_id = auth.uid()
    and exists (
        select 1 from conversation_members cm
        where cm.conversation_id = messages.conversation_id
        and cm.user_id = auth.uid()
    )
    and (
        (select c.group_id from conversations c where c.id = messages.conversation_id) is null
        or cap((select c.group_id from conversations c where c.id = messages.conversation_id), 'msg_send') = 'all'
        or is_admin((select c.group_id from conversations c where c.id = messages.conversation_id))
    )
);

create policy msg_update on messages for update using (sender_id = auth.uid());
revoke delete on messages from authenticated;

-- 6. Storage Policies (group-media bucket)
create policy media_read on storage.objects for select using (
    bucket_id = 'group-media'
    and is_member(((storage.foldername(name))[1])::uuid)
);

create policy media_write on storage.objects for insert with check (
    bucket_id = 'group-media'
    and is_member(((storage.foldername(name))[1])::uuid)
);
