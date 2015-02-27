-- name: db-create-trainings-table!
drop table if exists trainings;
create table trainings
(
   id serial not null,
   user_id integer not null,
   "name" character varying (32) not null,
   created bigint not null,
   primary key (id),
   unique (id, "name")
);

-- name: db-create-trainingexercises-table!
drop table if exists trainingexercises;
create table trainingexercises
(
  training integer not null references trainings on delete cascade,
  exercise character varying (36) not null,
  position integer not null
);
