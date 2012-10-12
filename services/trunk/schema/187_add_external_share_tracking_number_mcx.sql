alter table external_share add column es_tracking_number varchar(64);
create index es_tracking_number_idx on external_share(es_tracking_number);
