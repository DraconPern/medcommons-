create table if not exists group_ipaddress (
  id bigint not null auto_increment,
  version bigint not null,
  accid varchar(16),
  ipaddress varchar(40),
  primary key (id)
);
create index group_ipaddress_idx on group_ipaddress(ipaddress);
