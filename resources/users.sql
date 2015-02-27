-- name: db-create-users-table!
drop table if exists users;
create table users
(
  id serial not null,
  username character varying(20) not null,
  password character varying(79) not null,
  created bigint not null,
  role integer not null,
  primary key (id),
  unique(username)
);

-- name: db-select-user
select * from users where username = :username
