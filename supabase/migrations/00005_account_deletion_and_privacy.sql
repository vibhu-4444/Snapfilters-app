-- 00005_account_deletion_and_privacy.sql
-- PRD v3.0 Chapter 22: Account and Data Deletion Cascade

create or replace function delete_account()
returns jsonb
language plpgsql
security definer
set search_path = public
as $$
declare
    v_user_id uuid := auth.uid();
    v_job_id uuid := gen_random_uuid();
    v_deleted_groups uuid[];
    v_empty_group_id uuid;
begin
    if v_user_id is null then
        raise exception '401: Unauthenticated';
    end if;

    -- Step 1: Mark profile as deleted and revoke active sessions
    update profiles
    set display_name = 'Deleted User', avatar_path = null
    where id = v_user_id;

    -- Step 2: Delete storage objects uploaded by this user in group-media bucket
    delete from storage.objects
    where bucket_id = 'group-media' and owner = v_user_id;

    delete from media_assets
    where uploader_id = v_user_id;

    -- Step 3: Delete group memberships
    delete from group_members
    where user_id = v_user_id;

    -- Soft-delete groups that now have zero members
    for v_empty_group_id in
        select g.id from groups g
        left join group_members gm on gm.group_id = g.id
        where gm.user_id is null and g.deleted_at is null
    loop
        update groups set deleted_at = now() where id = v_empty_group_id;
    end loop;

    -- Step 4: Tombstone authored messages (preserve thread references per Chapter 18 & 22)
    update messages
    set body = 'This message was deleted.', kind = 'system', deleted_at = now()
    where sender_id = v_user_id;

    -- Step 5: Delete recovery code secret hash
    delete from recovery_codes
    where user_id = v_user_id;

    -- Step 6: Write audit log record
    insert into group_audit (group_id, actor_id, action, details)
    values (
        '00000000-0000-0000-0000-000000000000'::uuid,
        v_user_id,
        'account_deletion',
        jsonb_build_object('job_id', v_job_id, 'timestamp', now(), 'status', 'completed')
    );

    return jsonb_build_object(
        'status', 'success',
        'job_id', v_job_id,
        'user_id', v_user_id
    );
end;
$$;
