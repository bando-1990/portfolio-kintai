-- V2__insert_roles.sql : ロール初期データ
insert into roles (code) values
  ('ADMIN'),
  ('MANAGER'),
  ('MEMBER')
on conflict (code) do nothing;
