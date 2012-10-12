alter table emailq add column recipient varchar(255);
alter table emailq add column via varchar(30);
alter table emailq add column attachments text;
create index eq_recipient_idx on emailq(recipient);
