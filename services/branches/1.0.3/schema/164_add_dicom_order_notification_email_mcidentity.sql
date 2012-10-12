alter table dicom_order add column notification_email text;
alter table dicom_order_history add column notification_sent bit(1) NOT NULL default 0;
update dicom_order_history set notification_sent = b'1';
create index dicom_order_history_notified_idx on dicom_order_history (notification_sent);
