create table if not exists audit_logs (
  id bigserial primary key,
  ts timestamptz not null default now(),
  actor_user_id uuid,
  actor_roles text[],
  action varchar(64) not null,        -- 例: ATTENDANCE_CLOCK_IN
  resource_type varchar(64) not null, -- 例: attendance_record
  resource_id uuid,
  http_method varchar(8),
  http_path text,
  http_status int,
  client_ip inet,
  trace_id varchar(64),
  meta jsonb
);

create index if not exists idx_audit_ts on audit_logs(ts);
create index if not exists idx_audit_action on audit_logs(action);
