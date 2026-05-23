-- V3__add_correction_time_fields.sql : 修正申請に希望時刻カラムを追加
alter table attendance_corrections
  add column if not exists requested_clock_in  timestamptz,
  add column if not exists requested_clock_out timestamptz;
