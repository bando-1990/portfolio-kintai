create index if not exists idx_attendance_date on attendance_records(work_date);
create index if not exists idx_corr_status_created on attendance_corrections(status, created_at);
