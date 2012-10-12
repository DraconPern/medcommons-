-- storage_account_id index in Rights table
-- ALTER IGNORE TABLE `rights` DROP INDEX `storage_account_id`;
ALTER TABLE `rights` ADD INDEX ( `storage_account_id` );

-- accid index in groupinstances table
-- ALTER TABLE `groupinstances` DROP INDEX `accid`;
ALTER TABLE `groupinstances` ADD INDEX ( `accid` );

-- active_group_accid index in users table
-- ALTER TABLE `users` DROP INDEX `active_group_accid`;
ALTER TABLE `users` ADD INDEX ( `active_group_accid` );

-- proveridgroupid index in practice table
-- ALTER TABLE `practice` DROP INDEX `providergroupid`;
ALTER TABLE `practice` ADD INDEX ( `providergroupid` );
