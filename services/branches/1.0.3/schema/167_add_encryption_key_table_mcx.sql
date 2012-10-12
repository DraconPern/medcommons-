create table if not exists encryption_key (
  ek_accid    varchar(16) NOT NULL,
  ek_key      varchar(64) NOT NULL,
  ek_node_key varchar(40) NOT NULL,
  ek_create_date_time timestamp not null default CURRENT_TIMESTAMP,
  primary key(ek_accid,ek_key, ek_node_key)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
create index ek_accid_idx on encryption_key (ek_accid);
