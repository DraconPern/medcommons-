create table if not exists dicom_order_notification
(
  id bigint not null auto_increment, 
  order_id bigint,
  version bigint not null, 
  recipient varchar(255),
  subject varchar(255),
  status varchar(255),
  error  varchar(255),
  sent_date_time datetime,
  primary key (id)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
create index don_order_id_idx on dicom_order_notification(order_id);

