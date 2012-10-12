DROP TABLE IF EXISTS `emailq`;
CREATE TABLE `emailq` (
  `ind` int(11) NOT NULL auto_increment,
  `from` varchar(255) NOT NULL,
  `to` varchar(255) NOT NULL,
  `replyto` varchar(255) NOT NULL,
  `subject` varchar(255) NOT NULL,
  `textbody` tinytext NOT NULL,
  `htmlbody` mediumtext NOT NULL,
  `response` mediumtext NOT NULL,
  PRIMARY KEY  (`ind`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;

