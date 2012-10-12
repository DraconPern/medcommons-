create table practice_patient (
  pp_practice_id int(11),
  pp_name varchar(40),
  pp_accid decimal(16,0),
  primary key (pp_practice_id, pp_name, pp_accid)
);

create index practice_name_idx on practice_patient(pp_practice_id, pp_name);

