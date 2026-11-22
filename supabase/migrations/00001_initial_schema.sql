-- 00001_initial_schema.sql
-- PRD v3.0 Chapter 9: Data models and database schema

-- identities
create table if not exists profiles (
    id uuid primary key references auth.users on delete cascade,
    display_name text not null check (char_length(display_name) between 1 and 40),
    avatar_path text,
    created_at timestamptz not null default now()
);

-- groups
create table if not exists groups (
    id uuid primary key default gen_random_uuid(),
    name text not null check (char_length(name) between 1 and 60),
    description text check (char_length(description) <= 300),
    image_path text,
    owner_id uuid not null references profiles(id),
    joining_policy text not null default 'approval' check (joining_policy in ('direct','approval')),
    deleted_at timestamptz,
    created_at timestamptz not null default now()
);

-- group members
create table if not exists group_members (
    group_id uuid not null references groups(id) on delete cascade,
    user_id uuid not null references profiles(id) on delete cascade,
    role text not null default 'member' check (role in ('admin','member')),
    joined_at timestamptz not null default now(),
    primary key (group_id, user_id)
);

-- group permissions (14-capability matrix per ADR-002 and Chapter 12)
create table if not exists group_permissions (
    group_id uuid primary key references groups(id) on delete cascade,
    msg_send text not null default 'all' check (msg_send in ('all','admin')),
    msg_reply text not null default 'all' check (msg_reply in ('all','admin')),
    msg_react text not null default 'all' check (msg_react in ('all','admin')),
    photo_share text not null default 'all' check (photo_share in ('all','admin')),
    file_upload text not null default 'all' check (file_upload in ('all','admin')),
    media_download text not null default 'all' check (media_download in ('all','admin')),
    member_invite text not null default 'admin' check (member_invite in ('all','admin')),
    join_approve text not null default 'admin' check (join_approve in ('all','admin')),
    member_remove text not null default 'admin' check (member_remove in ('all','admin')),
    edit_info text not null default 'admin' check (edit_info in ('all','admin')),
    manage_invitations text not null default 'admin' check (manage_invitations in ('all','admin')),
    manage_permissions text not null default 'admin' check (manage_permissions in ('all','admin')),
    promote_admin text not null default 'admin' check (promote_admin in ('all','admin')),
    delete_group text not null default 'admin' check (delete_group in ('all','admin'))
);

-- group invitations (ADR-010: 32 random bytes token, hashed at rest)
create table if not exists group_invitations (
    id uuid primary key default gen_random_uuid(),
    group_id uuid not null references groups(id) on delete cascade,
    token_hash bytea not null unique,
    created_by uuid not null references profiles(id),
    expires_at timestamptz not null,
    max_uses int check (max_uses is null or max_uses > 0),
    used_count int not null default 0,
    revoked_at timestamptz,
    created_at timestamptz not null default now()
);

-- join requests
create table if not exists join_requests (
    id uuid primary key default gen_random_uuid(),
    group_id uuid not null references groups(id) on delete cascade,
    user_id uuid not null references profiles(id) on delete cascade,
    invitation_id uuid references group_invitations(id) on delete set null,
    state text not null default 'pending' check (state in ('pending','approved','rejected','cancelled')),
    created_at timestamptz not null default now(),
    decided_at timestamptz,
    decided_by uuid references profiles(id),
    unique (group_id, user_id, state) deferrable initially deferred
);

-- recovery codes for anonymous identity (ADR-003)
create table if not exists recovery_codes (
    user_id uuid primary key references profiles(id) on delete cascade,
    code_hash bytea not null unique,
    used_at timestamptz,
    created_at timestamptz not null default now()
);

-- conversations and messages
create table if not exists conversations (
    id uuid primary key default gen_random_uuid(),
    kind text not null check (kind in ('direct','group')),
    group_id uuid references groups(id) on delete cascade,
    last_message_at timestamptz not null default now(),
    created_at timestamptz not null default now(),
    check ((kind = 'group') = (group_id is not null))
);

create table if not exists conversation_members (
    conversation_id uuid not null references conversations(id) on delete cascade,
    user_id uuid not null references profiles(id) on delete cascade,
    last_read_at timestamptz not null default 'epoch',
    primary key (conversation_id, user_id)
);

create table if not exists messages (
    id uuid primary key default gen_random_uuid(),
    conversation_id uuid not null references conversations(id) on delete cascade,
    sender_id uuid not null references profiles(id),
    kind text not null default 'text' check (kind in ('text','photo','file','system')),
    body text check (char_length(body) <= 4000),
    reply_to_id uuid references messages(id) on delete set null,
    client_message_id uuid not null,
    created_at timestamptz not null default now(),
    deleted_at timestamptz,
    unique (sender_id, client_message_id)
);

create table if not exists media_assets (
    id uuid primary key default gen_random_uuid(),
    message_id uuid not null references messages(id) on delete cascade,
    uploader_id uuid not null references profiles(id),
    storage_path text not null,
    tier text not null check (tier in ('preview','original')),
    mime text not null,
    byte_size bigint not null,
    width int,
    height int,
    created_at timestamptz not null default now()
);

create table if not exists reactions (
    message_id uuid not null references messages(id) on delete cascade,
    user_id uuid not null references profiles(id) on delete cascade,
    code text not null check (code in ('heart','laugh','wow','sad','like')),
    created_at timestamptz not null default now(),
    primary key (message_id, user_id, code)
);

-- Indexes per PRD v3.0 Chapter 9
create index if not exists idx_messages_pagination on messages (conversation_id, created_at desc, id desc);
create index if not exists idx_messages_gallery on messages (conversation_id, created_at) where deleted_at is null;
create index if not exists idx_group_members_user on group_members (user_id) include (group_id, role);
create index if not exists idx_join_requests_pending on join_requests (group_id, state) where state = 'pending';
create index if not exists idx_group_invitations_token on group_invitations (token_hash);
create index if not exists idx_conversation_members_user on conversation_members (user_id) include (conversation_id, last_read_at);
create index if not exists idx_media_assets_message on media_assets (message_id);

