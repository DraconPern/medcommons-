insert into mcfeatures values (NULL, 'services.menu', 1,'Show the top nav menu for accessing Services administration');
update mcfeatures set mf_name = 'dashboard.newPatient' where mf_name = 'dashboard.newAccount';
insert into mcfeatures values (NULL, 'group.uploadPage', 1,'Allow access to group upload page');

