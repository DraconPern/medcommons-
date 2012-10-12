alter table users add column expiration_date datetime;
update users u, modcoupons c set u.expiration_date = c.expirationdate where u.mcid = c.mcid;
