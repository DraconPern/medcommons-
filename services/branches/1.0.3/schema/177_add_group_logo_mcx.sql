alter table groupinstances add column logo_url text;
insert into mcfeatures values (NULL, 'group.customLogo', 1,'Allow groups to customize the logo in the page header');
