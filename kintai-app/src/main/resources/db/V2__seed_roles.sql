insert into roles (code) values ('ADMIN') on conflict (code) do nothing;
insert into roles (code) values ('MANAGER') on conflict (code) do nothing;
insert into roles (code) values ('MEMBER') on conflict (code) do nothing;
