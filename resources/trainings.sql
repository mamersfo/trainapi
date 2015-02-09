-- name: db-create-trainings-table!
drop table if exists trainings;
create table trainings
(
   id character varying (36) not null,
   "name" character varying (32) not null,
   created bigint not null,
   primary key (id),
   unique ("name")
);

-- name: db-create-trainingexercises-table!
drop table if exists trainingexercises;
create table trainingexercises
(
  training character varying (36) not null references trainings on delete cascade,
  exercise character varying (36) not null,
  position integer not null
);

-- name: db-insert-training!
insert into trainings (id, "name", created) values (:id, :name, :created);

-- name: db-select-training
select * from trainings where id = :id;

-- name: db-select-all-trainings
select * from trainings;

-- name: db-select-training-exercises
select exercise from trainingexercises
where training = :training
order by position asc;

-- name: db-delete-training!
delete from trainings where id = :id;
