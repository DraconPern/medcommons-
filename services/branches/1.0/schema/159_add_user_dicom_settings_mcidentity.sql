create table if not exists users_dicom_settings (
  uds_id bigint not null auto_increment, 
  uds_accid varchar(16),
  uds_aetitle varchar(255),
  uds_host varchar(255),
  uds_port int(7),
  uds_create_date_time timestamp not null default CURRENT_TIMESTAMP,
  primary key(uds_id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
create index uds_accid_idx on users_dicom_settings (uds_accid)
