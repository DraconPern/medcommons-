create table mcfeatures (
  mf_id int(10) unsigned auto_increment,
  mf_name varchar(60) NOT NULL,
  mf_enabled tinyint(1) NOT NULL,
  mf_description varchar(255),
  primary key (mf_id)
);
