alter table dicom_order modify  baseline bit;
alter table dicom_order modify due_date_time datetime;
alter table dicom_order modify protocol_id varchar(30);
alter table dicom_order add sender_email varchar(255);
alter table dicom_order add sender_name varchar(255);
alter table dicom_order add facility varchar(255);
