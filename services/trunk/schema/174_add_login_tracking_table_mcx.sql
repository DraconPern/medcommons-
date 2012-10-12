CREATE TABLE IF NOT EXISTS `logintrakker` (
   `lasttime` int(11) NOT NULL,
   `failurecounter` smallint(6) NOT NULL,
   `userinput` varchar(255) NOT NULL,
   UNIQUE KEY `userinput` (`userinput`)
   ) ENGINE=MyISAM DEFAULT CHARSET=latin1 COMMENT='Used only by Login Code';
