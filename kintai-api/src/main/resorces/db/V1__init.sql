-- V1__init.sql : 初期スキーマ（休憩申請なし・自動算出）

create table if not exists roles (
  id serial primary key,
  code varchar(32) unique not null
);

create table if not exists users (
  id uuid primary key,
  login_id varchar(100) unique not null,
  name varchar(100) not null,
  email varchar(255),
  password_hash varchar(255) not null,
  active boolean not null default true,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table if not exists user_roles (
  user_id uuid not null references users(id) on delete cascade,
  role_id int not null references roles(id),
  primary key (user_id, role_id)
);

create table if not exists attendance_records (
  id uuid primary key,
  user_id uuid not null references users(id),
  work_date date not null,
  clock_in_at timestamptz,
  clock_out_at timestamptz,
  status varchar(20) not null default 'NOT_STARTED',
  source varchar(20) not null default 'WEB',
  notes text,
  gross_work_minutes integer,
  auto_break_minutes integer default 0,
  net_work_minutes integer,
  calc_policy_code varchar(50),
  calc_at timestamptz,
  calc_trace jsonb,
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now(),
  constraint uq_user_date unique (user_id, work_date),
  constraint chk_status check (status in ('NOT_STARTED','WORKING','COMPLETED'))
);

create table if not exists idempotency_keys (
  key varchar(128) primary key,
  user_id uuid not null,
  request_hash varchar(64) not null,
  response_hash varchar(64),
  created_at timestamptz not null default now()
);

create table if not exists attendance_corrections (
  id uuid primary key,
  record_id uuid not null references attendance_records(id) on delete cascade,
  applicant_id uuid not null references users(id),
  approver_id uuid references users(id),
  status varchar(16) not null default 'pending',
  reason text not null,
  reviewer_comment text,
  created_at timestamptz not null default now(),
  decided_at timestamptz,
  constraint chk_corr_status check (status in ('pending','approved','rejected'))
);

create index if not exists idx_attendance_user_date on attendance_records(user_id, work_date);
create index if not exists idx_attendance_user_created on attendance_records(user_id, created_at);
create index if not exists idx_idempotency_created_at on idempotency_keys(created_at);
create index if not exists idx_corr_record on attendance_corrections(record_id);
