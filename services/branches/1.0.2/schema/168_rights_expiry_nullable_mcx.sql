alter table rights modify column expiration_time timestamp NULL default NULL;
create index rights_expiration_time on rights (expiration_time);
update rights set expiration_time = NULL where expiration_time = '0000-00-00 00:00:00';
