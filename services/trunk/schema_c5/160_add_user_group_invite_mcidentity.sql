create table if not exists users_group_invite (
  ugi_id bigint not null auto_increment, 
  ugi_accid varchar(16),
  ugi_group_accid varchar(16),
  primary key(ugi_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
create index ugi_accid_idx on users_group_invite (ugi_accid);
create index ugi_group_idx on users_group_invite (ugi_group_accid);
