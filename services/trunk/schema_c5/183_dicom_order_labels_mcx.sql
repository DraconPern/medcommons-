create table if not exists dicom_order_label
(
  id bigint not null auto_increment, 
  dicom_order_id bigint,
  version bigint not null, 
  label_00 varchar(120),
  label_01 varchar(120),
  label_02 varchar(120),
  label_03 varchar(120),
  label_04 varchar(120),
  label_05 varchar(120),
  label_06 varchar(120),
  label_07 varchar(120),
  label_08 varchar(120),
  label_09 varchar(120),
  primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
create index dol_order_id_idx on dicom_order_label(dicom_order_id);
