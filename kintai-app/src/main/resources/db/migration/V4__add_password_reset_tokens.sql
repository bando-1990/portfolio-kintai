-- V4__add_password_reset_tokens.sql : パスワードリセットトークン管理テーブル
create table if not exists password_reset_tokens (
  id         uuid primary key,
  token      varchar(64) unique not null,
  user_id    uuid not null references users(id) on delete cascade,
  expires_at timestamptz not null,
  used       boolean not null default false,
  created_at timestamptz not null default now()
);

create index if not exists idx_prt_token   on password_reset_tokens(token);
create index if not exists idx_prt_user_id on password_reset_tokens(user_id);
