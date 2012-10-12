# phpMyAdmin SQL Dump
# version 2.5.7-pl1
# http://www.phpmyadmin.net
#
# Host: localhost
# Generation Time: Oct 21, 2004 at 07:26 PM
# Server version: 4.0.20
# PHP Version: 5.0.1
# 
# Database : `pink7`
# 

# --------------------------------------------------------

#
# Table structure for table `col_linkages`
#

CREATE TABLE `col_linkages` (
  `CL_ID` varchar(16) NOT NULL default '',
  `CL_TABLENAME` varchar(32) NOT NULL default '',
  `CL_COLUMNNAME` varchar(32) NOT NULL default '',
  `CL_LINKEDTABLE` varchar(32) NOT NULL default ''
) TYPE=MyISAM COMMENT='INTER-TABLE HYPERLINKS';

#
# Dumping data for table `col_linkages`
#

INSERT INTO `col_linkages` VALUES ('9999999999999999', 'USERS', 'ROLE', 'USER_ROLES');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'USER_ROLES', 'PRIVS', 'USER_PRIVS');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'USERS', 'USERID', 'HIPAALOG');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'HIPAALOG', 'USERID', 'USERS');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'GATEWAYS', 'VAETITLE', 'VIRTRAD');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'VIRTRAD', 'GATEWAY', 'GATEWAYS');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'XDSREGISTRY', 'VAETITLE', 'VIRTRAD');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'ORDERS', 'VAETITLEORIGIN', 'VIRTRAD');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'ORDERS', 'VAETITLEDEST', 'GATEWAYS');
INSERT INTO `col_linkages` VALUES ('9999999999999999', 'REMOTE_COMMANDS', 'VAETITLE', 'VIRTRAD');

# --------------------------------------------------------

#
# Table structure for table `config`
#

CREATE TABLE `config` (
  `CN_ID` varchar(16) NOT NULL default '',
  `CN_PARTNER` varchar(255) NOT NULL default '',
  `CN_SHORTNAME` varchar(6) NOT NULL default '',
  `CN_STYLESHEET` varchar(255) NOT NULL default 'astyles.css',
  `CN_PURPLESTYLESHEET` varchar(255) NOT NULL default '',
  `CN_ROOTURL` varchar(255) NOT NULL default '',
  `CN_HOMEPAGE` varchar(255) NOT NULL default '',
  `CN_PHPMYADMINURL` varchar(255) NOT NULL default '',
  `CN_TYPE` varchar(6) NOT NULL default 'MASTER'
) TYPE=MyISAM COMMENT='PARTNER CONFIGURATION PARAMETERS';

#
# Dumping data for table `config`
#

INSERT INTO `config` VALUES ('9999999999999999', 'Local Pink6 Hospital', 'BMHP6', 'astyles.css', 'purplestyles.css', '//localhost', 'HTTP://WWW.MICROSOFT.COM', 'http://localhost/phpmyadmin', 'MASTER');
INSERT INTO `config` VALUES ('9999999999999999', 'ONE AND ONE PINK4HOSPITAL', 'ONEOP6', 'astyles.css', 'purplestyles.css', 'http://virtual01.medcommons.net', 'HTTP://WWW.ONEANDONE.COM', 'HTTP://VIRTUAL01.MEDCOMMONS.NET/phpmyadmin', 'BACKUP');

# --------------------------------------------------------

#
# Table structure for table `counters`
#

CREATE TABLE `counters` (
  `CN_COUNTER` int(10) unsigned NOT NULL default '0',
  `CN_KEY` varchar(32) NOT NULL default '',
  `CN_DESC` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='Counters Maintained by the Pink Box';

#
# Dumping data for table `counters`
#


# --------------------------------------------------------

#
# Table structure for table `gateways`
#

CREATE TABLE `gateways` (
  `GW_ID` varchar(16) NOT NULL default '',
  `GW_VR_VAETITLE` varchar(16) NOT NULL default '',
  `GW_URL` varchar(255) NOT NULL default '',
  `GW_DETAILS` varchar(255) NOT NULL default '',
  `GW_LASTHEARD` timestamp(14) NOT NULL
) TYPE=MyISAM COMMENT='EVERY GATEWAY KNOWN TO THIS AFFILIATE';

#
# Dumping data for table `gateways`
#

INSERT INTO `gateways` VALUES ('9999999999999999', 'MGHA', 'virtual03.medcommons.net:9080', '3RD FLOOR DATA CENTER', 20041020163152);
INSERT INTO `gateways` VALUES ('9999999999999999', 'MGHB', 'virtual03.medcommons.net:9080', 'IN THE NEW BUILDING', 20041020163152);
INSERT INTO `gateways` VALUES ('9999999999999999', 'MGHC', 'virtual02.medcommons.net:9080', 'TEST LAB', 20041020163152);
INSERT INTO `gateways` VALUES ('9999999999999999', '3DRA', 'medcommons.net:9080', '2ND FLOOR LABORATORY', 20041020163152);
INSERT INTO `gateways` VALUES ('999999999393', 'MCPURPLE01', 'mcpurple01.homeip.net:9080', 'Adrian\'s Windows Server', 20041020163152);

# --------------------------------------------------------

#
# Table structure for table `hipaalog`
#

CREATE TABLE `hipaalog` (
  `HL_ID` varchar(16) NOT NULL default '',
  `HL_US_USERID` varchar(32) NOT NULL default '',
  `HL_EVENTCODE` varchar(6) NOT NULL default '',
  `HL_EVENTSTRING` varchar(255) NOT NULL default '',
  `HL_TIMESTAMP` timestamp(14) NOT NULL
) TYPE=MyISAM COMMENT='PLAIN OLD HIPAA LOG';

#
# Dumping data for table `hipaalog`
#


# --------------------------------------------------------

#
# Table structure for table `order_data`
#

CREATE TABLE `order_data` (
  `OS_ID` varchar(32) NOT NULL default '',
  `OS_OR_ORDERGUID` varchar(32) NOT NULL default '',
  `OS_DATAGUID` varchar(32) NOT NULL default '',
  `OS_TIMESTAMP` timestamp(14) NOT NULL
) TYPE=MyISAM COMMENT='ALL THE SERIES ASSOCIATED WITH AN ORDER';

#
# Dumping data for table `order_data`
#


# --------------------------------------------------------

#
# Table structure for table `orders`
#

CREATE TABLE `orders` (
  `OR_ID` varchar(16) NOT NULL default '',
  `OR_ORDERGUID` varchar(64) NOT NULL default '',
  `OR_TRACKING` varchar(12) NOT NULL default '',
  `OR_VR_VAETITLEORIGIN` varchar(16) NOT NULL default '',
  `OR_VR_VAETITLEDEST` varchar(16) NOT NULL default '',
  `OR_TIME` timestamp(14) NOT NULL,
  `OR_GLOBALSTATUS` varchar(32) NOT NULL default '',
  `OR_DESCRIPTION` varchar(255) NOT NULL default '',
  `OR_PATIENTNAME` varchar(32) NOT NULL default '',
  `OR_PATIENTID` varchar(32) NOT NULL default '',
  `OR_MODALITY` varchar(64) NOT NULL default '',
  `OR_SERIES` tinyint(4) NOT NULL default '0',
  `OR_IMAGES` tinyint(4) NOT NULL default '0'
) TYPE=MyISAM COMMENT='ALL ORDERS MANAGED BY ONE PINK BOX';

#
# Dumping data for table `orders`
#

INSERT INTO `orders` VALUES ('999999999999996', '07A9410C9871946AEEB5C491C68A16F3ADC752CD', '999123', 'MGHA', 'MGHB', 20041021125826, '', 'HEAD CTA', 'Voxar4008  _CTACOW', '1234', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'B6BBCC1819E86EC654DF88D901317A2504062DCE', '999999', 'MGHA', 'MGHB', 20190810000000, '', '', 'Voxar4007 _MRACOW', '1234', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'XLqLyqtPtmMd3VD_7uOGyFM6iR0=', '999999', 'MGHA', 'MGHB', 00000000000000, '', 'Runoff', 'Voxar4010 _MRArunoff', '1234', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'OeSt5z3gBsHP8M_D9j73jcZoIBs=', '999999', 'MGHA', 'MGHB', 20041021151751, '', 'RIGHT ANKLE', 'ANKLE FRACTURE', '588', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'f3FMAGB-tPsFrRJ7FZRO8BBILA0=', '999999', 'MGHA', 'MGHB', 00000000000000, '', 'ABD/PELVIS PRE OP AAA', 'Anonymous4800', 'anon4800', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'YImkXt1TdwvM8ZMRMgo2C_9JbeM=', '999999', 'MGHA', 'MGHB', 20041021151751, '', 'CT ABD', 'Anonymous3785', 'anon3785', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'yZl4MjppcrPtp0P7HQLMRtUbE_w=', '999999', 'MGHA', 'MGHB', 20041021151751, '', 'cta abdomen', '', 'IOP4123.240.1016740120', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '999999', 'MGHA', 'MGHB', 20041021151751, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', '9mkwgqLFG3tpcQS5vgB0yWayLxc=', '999999', 'MGHA', 'MGHB', 20041021151751, '', 'CT CTA NECK W/WO', 'Anonymous183', 'anon183', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', 'vQvhsc5rD4wU47DtzdxfIbIw32g=', '999999', 'MGHA', 'MGHB', 20041021151751, '', 'MIEMBROS', '+Runoff', '297188', '', 0, 0);
INSERT INTO `orders` VALUES ('999999999999996', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '999999', 'MGHA', 'MGHB', 20031107000000, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '999999', 'MGHA', 'MGHB', 00000000000000, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '999999', 'MGHA', 'MGHB', 00000000000000, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '144392323134', 'MGHA', 'MGHB', 00000000000000, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '144392323134', 'MGHA', 'MGHB', 00000000000000, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('', '8qF4w1O6Cugn0rsblgKw1LyziHE=', '144392323134', 'MGHA', 'MGHB', 00000000000000, '', 'CTA CARDIAC', 'Cardiac^IQ', 'AW470647469.863.1060347998', '', 0, 0);
INSERT INTO `orders` VALUES ('', '32032093209', '348329832', 'MGHC', '3DR', 00000000000000, '', '23093209', 'l', 'v', '', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'MGHA', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'ask01', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'ask01', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'ask01', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'ask01', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'ask01', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);
INSERT INTO `orders` VALUES ('', '33032jfajsfjdsae33', '444555111222', 'ask01', 'sdk01', 00000000000000, '', 'steel belted radials', 'joe', 'ew03ksdosdoi', 'mri', 0, 0);

# --------------------------------------------------------

#
# Table structure for table `params`
#

CREATE TABLE `params` (
  `PA_MAJOR` mediumint(9) NOT NULL default '4',
  `PA_MINOR` mediumint(9) NOT NULL default '1',
  `PA_TRACELEVEL` smallint(6) NOT NULL default '1',
  `PA_COMMENT` varchar(255) NOT NULL default '',
  `PA_CREATETIME` timestamp(14) NOT NULL
) TYPE=MyISAM COMMENT='One Record Only ';

#
# Dumping data for table `params`
#

INSERT INTO `params` VALUES (7, 1, 0, 'beta pink7 new vrcp support', 20040930184848);

# --------------------------------------------------------

#
# Table structure for table `remote_commands`
#

CREATE TABLE `remote_commands` (
  `RC_ID` varchar(16) NOT NULL default '',
  `RC_VR_VAETITLE` varchar(16) NOT NULL default '',
  `RC_PINKBOXTIME` timestamp(14) NOT NULL,
  `RC_PINKBOXSTATUS` varchar(6) NOT NULL default '',
  `RC_REMOTECOMMAND` varchar(255) NOT NULL default '',
  `RC_REMOTESTATUS` varchar(6) NOT NULL default '',
  `RC_REMOTETIME` timestamp(14) NOT NULL default '00000000000000'
) TYPE=MyISAM COMMENT='COMMANDS FOR EXECUTION ON A REMOTE PURPLE BOX';

#
# Dumping data for table `remote_commands`
#


# --------------------------------------------------------

#
# Table structure for table `replay`
#

CREATE TABLE `replay` (
  `RP_EXECUTE` tinyint(1) unsigned NOT NULL default '1',
  `RP_GATEWAY` varchar(16) NOT NULL default '',
  `RP_CLASS` varchar(16) NOT NULL default 'GENERAL',
  `RP_TIMESTAMP` time default NULL,
  `RP_WEBSERVICECOMMAND` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='holds replayable web service call stream';

#
# Dumping data for table `replay`
#

INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=users');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=user_roles');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=user_privs');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=config');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=params');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=hipaalog');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=remote_commands');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=gateways');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=virtrad');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=xdsregistry');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=orders');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=order_data');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=routing_queue');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=trace');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=replay');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsshowxmltable.php?gateway=SIM01&amp;recording=on&amp;table=counters');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsgetgatewayinfo.php?gateway=SIM01&amp;recording=on');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsgetcommandsince.php?gateway=SIM01&amp;recording=on&amp;SinceTime=');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsinsertorder.php?gateway=SIM01&amp;recording=on&amp;orderGuid=123456789&amp;description=a+test+order&amp;vAETitleOrigin=abc01&amp;vAETitleDest=def01&amp;patientName=joe+bloggs&amp;patientId=39230329303209309&amp;status=ok');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsupdateorderstatus.php?gateway=SIM01&amp;recording=on&amp;OrderGuid=123456779&amp;OrderStatus=pend');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsgetorderinfo.php?gateway=SIM01&amp;recording=on&amp;OrderGuid=123456789');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsinsertorderseries.php?gateway=SIM01&amp;recording=on&amp;orderGuid=123456789&amp;dataGuid=aaaaaaaa');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsinsertroutingqueueinfo.php?gateway=SIM01&amp;recording=on&amp;requestID=383929839&amp;orderGuid=123456789&amp;dataGuid=aaaaaaa&amp;vAETitleOrigin=fkdlasdflk&amp;vAETitleDest=adfkadsfl&amp;protocol=mri&amp;globalStatus=ok&amp;itemType=foo');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsupdateroutingqueueinfo.php?gateway=SIM01&amp;recording=on&amp;requestID=23094309324&amp;orderGuid=3209320932&amp;dataGuid=230932032&amp;globalStatus=32320320932&amp;bytesTotal=3209320932&amp;bytesTransferred=32093093&amp;restartCount=329032');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsupdateroutingqueueinfo.php?gateway=SIM01&amp;recording=on&amp;requestID=23094309324&amp;orderGuid=3209320932&amp;dataGuid=230932032&amp;globalStatus=32320320932&amp;bytesTotal=3209320932&amp;bytesTransferred=32093093&amp;restartCount=329032');
INSERT INTO `replay` VALUES (1, 'SIM01', 'GENERAL', '00:00:00', '/pink4/xmlws/wsgetroutingqueueitems.php?gateway=SIM01&amp;recording=on&amp;destvAETitleDest=ffff');
INSERT INTO `replay` VALUES (1, 'SIM02', 'GENERAL', '00:00:00', '/pink5/xmlws/wsgetgatewayinfo.php?gateway=SIM02&amp;recording=on');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsinsertorder.php?gateway=SIM11&amp;recording=on&amp;orderGuid=aaaa&amp;description=all+aze&amp;vAETitleOrigin=ct01&amp;vAETitleDest=ct99&amp;patientName=joe&amp;patientId=patient123&amp;status=pend');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsupdateorderstatus.php?gateway=SIM11&amp;recording=on&amp;OrderGuid=aaaa&amp;OrderStatus=in+progress');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsgetorderinfo.php?gateway=SIM11&amp;recording=on&amp;OrderGuid=aaaa');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsinsertorderseries.php?gateway=SIM11&amp;recording=on&amp;orderGuid=aaaa&amp;dataGuid=dddd');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsgetorderserieslinks.php?gateway=SIM11&amp;recording=on&amp;OrderGuid=aaaa');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsgetorderserieslinks.php?gateway=SIM11&amp;recording=on&amp;OrderGuid=aaaa');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsinsertroutingqueueinfo.php?gateway=SIM11&amp;recording=on&amp;requestID=1234567890&amp;orderGuid=aaaa&amp;dataGuid=dddd&amp;vAETitleOrigin=cr01&amp;vAETitleDest=pacs03&amp;protocol=mri&amp;globalStatus=looks+good&amp;itemType=order');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsgetroutingqueueitems.php?gateway=SIM11&amp;recording=on&amp;destvAETitle=cr01');
INSERT INTO `replay` VALUES (1, 'SIM11', 'GENERAL', '00:00:00', '/pink5/xmlws/wsgetroutingqueueitems.php?gateway=SIM11&amp;recording=on&amp;destvAETitle=pacs03');

# --------------------------------------------------------

#
# Table structure for table `routing_queue`
#

CREATE TABLE `routing_queue` (
  `RQ_ID` varchar(16) NOT NULL default '',
  `RQ_OS_DATAGUID` varchar(32) NOT NULL default '',
  `RQ_OR_ORDERGUID` varchar(32) NOT NULL default '',
  `RQ_VR_DESTINATIONVAETITLE` varchar(32) NOT NULL default '',
  `RQ_VR_ORIGINVAETITLE` varchar(32) NOT NULL default '',
  `RQ_PROTOCOL` varchar(32) NOT NULL default '',
  `RQ_BYTESTOTAL` int(11) NOT NULL default '0',
  `RQ_BYTESTRANSFERRED` int(11) NOT NULL default '0',
  `RQ_GLOBALSTATUS` varchar(6) NOT NULL default '',
  `RQ_RESTARTCOUNT` int(11) NOT NULL default '0',
  `RQ_TIMEENTERED` timestamp(14) NOT NULL,
  `RQ_TIMESTARTED` timestamp(14) NOT NULL default '00000000000000',
  `RQ_TIMECOMPLETED` timestamp(14) NOT NULL default '00000000000000',
  `RQ_ITEMTYPE` varchar(6) NOT NULL default ''
) TYPE=MyISAM COMMENT='QUEUE OF SPECIFIC COMMANDS ABOUT ROUTING ORDERS';

#
# Dumping data for table `routing_queue`
#


# --------------------------------------------------------

#
# Table structure for table `special_links`
#

CREATE TABLE `special_links` (
  `SL_ROLE` varchar(16) NOT NULL default '',
  `SL_URL` varchar(255) NOT NULL default '',
  `SL_TEXT` varchar(32) NOT NULL default ''
) TYPE=MyISAM COMMENT='extra links to display for certain roles';

#
# Dumping data for table `special_links`
#

INSERT INTO `special_links` VALUES ('ADMIN', '../controller/cuinetopspanel.php', 'Network Operations');
INSERT INTO `special_links` VALUES ('ADMIN', 'http://virtual01.medcommons.net/zabbix/', 'Zabbix Network Monitoring');

# --------------------------------------------------------

#
# Table structure for table `trace`
#

CREATE TABLE `trace` (
  `TR_OWNER` varchar(32) NOT NULL default '',
  `TR_TIME` timestamp(14) NOT NULL,
  `TR_CODE` varchar(6) NOT NULL default '',
  `TR_BLOB` mediumblob NOT NULL,
  `TR_COMMENT` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='Tracelog of webservice requests/responses';

#
# Dumping data for table `trace`
#


# --------------------------------------------------------

#
# Table structure for table `user_privs`
#

CREATE TABLE `user_privs` (
  `UP_ID` varchar(16) NOT NULL default '',
  `UP_CODE` char(1) NOT NULL default '',
  `UP_PRIV` varchar(16) NOT NULL default '',
  `UP_DESCRIPTION` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='ELABORATE PRIVILEGE CODES';

#
# Dumping data for table `user_privs`
#

INSERT INTO `user_privs` VALUES ('9999999999999999', 'A', 'DABBLE', 'VIEW OWN CONTENT');
INSERT INTO `user_privs` VALUES ('9999999999999999', 'B', 'WRITE REPORTS', 'ADD REPORTS, BUT NOT STUDIES TO THE SYSTEM');
INSERT INTO `user_privs` VALUES ('9999999999999999', 'C', 'SUPERUSER', 'DO ANYTHING AT ALL WITH THE SYSTEM');
INSERT INTO `user_privs` VALUES ('9999999999999999', 'D', 'DB MAINT', 'MAINTAIN THE DATABASE');

# --------------------------------------------------------

#
# Table structure for table `user_roles`
#

CREATE TABLE `user_roles` (
  `UR_ID` varchar(16) NOT NULL default '',
  `UR_ROLE` varchar(16) NOT NULL default 'TECH',
  `UR_DESCRIPTION` varchar(255) NOT NULL default 'TECHNICAL SPECIALIST',
  `UR_PRIVS` varchar(16) NOT NULL default 'AB'
) TYPE=MyISAM COMMENT='ALL THE ROLES IN THE SYSTEM, WITH ASSOCIATED PRIVS';

#
# Dumping data for table `user_roles`
#

INSERT INTO `user_roles` VALUES ('9999999999999999', 'REFERRING DOC', 'REFERRING PHYSICIAN', 'A');
INSERT INTO `user_roles` VALUES ('9999999999999999', 'RADIOLOGIST', 'DOES INTERPRETATIONS', 'BCD');
INSERT INTO `user_roles` VALUES ('9999999998765432', 'TECH', 'TECHNICAL SPECIALIST wuth \'quotes\'', 'AB');
INSERT INTO `user_roles` VALUES ('9999999999999999', 'ADMIN', 'ADMINISTERS PINK BOX', 'ABCDEFGH');
INSERT INTO `user_roles` VALUES ('9999999999999999', 'TECH', 'TECHNICAL SPECIALIST', 'AB');
INSERT INTO `user_roles` VALUES ('9999999999999999', 'LOW', 'A POOR FELLOW WITH NO PRIVS AT ALL', '');

# --------------------------------------------------------

#
# Table structure for table `users`
#

CREATE TABLE `users` (
  `US_ID` varchar(16) NOT NULL default '',
  `US_USERID` varchar(16) NOT NULL default '',
  `US_NAME` varchar(255) NOT NULL default '',
  `US_TITLE` varchar(32) NOT NULL default '',
  `US_ADDR1` varchar(32) NOT NULL default '',
  `US_ADDR2` varchar(32) NOT NULL default '',
  `US_CITY` varchar(32) NOT NULL default '',
  `US_STATE` varchar(32) NOT NULL default '',
  `US_ZIP` varchar(9) NOT NULL default '',
  `US_CREATETIME` timestamp(14) NOT NULL,
  `US_EMAIL` varchar(255) NOT NULL default '',
  `US_PHONE` varchar(16) NOT NULL default '',
  `US_SEX` tinyint(4) NOT NULL default '0',
  `US_DOB` datetime NOT NULL default '0000-00-00 00:00:00',
  `US_COMPANY` varchar(32) NOT NULL default '',
  `US_HEALTHCARESPECIALIST` tinyint(4) NOT NULL default '0',
  `US_STATELICENSE` tinyint(4) NOT NULL default '0',
  `US_PIN` varchar(16) NOT NULL default '',
  `US_RL_ROLE` varchar(16) NOT NULL default '',
  `US_DEFAULTWORKLIST` varchar(16) NOT NULL default '',
  `US_DEFAULTGATEWAY` varchar(16) NOT NULL default '',
  `US_BACKUPGATEWAYS` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='ONE ROW FOR EACH PATIENT OR PHYSICIAN OR TECH OR WHOMEVER';

#
# Dumping data for table `users`
#

INSERT INTO `users` VALUES ('', 'billd', 'bill donner', '', '', '', '', '', '', 20041003210134, 'adsfafd', '917 848 7175', 0, '0000-00-00 00:00:00', '', 0, 0, '6184', '', '', 'mcpurple01', 'gmc_gateway,mgha,mghb');

# --------------------------------------------------------

#
# Table structure for table `virtrad`
#

CREATE TABLE `virtrad` (
  `VR_ID` varchar(16) NOT NULL default '',
  `VR_VAETITLE` varchar(16) NOT NULL default '',
  `VR_GW_GATEWAY` varchar(16) NOT NULL default '',
  `VR_LOCALAETITLE` varchar(16) NOT NULL default '',
  `VR_DESCRIPTION` varchar(255) NOT NULL default '',
  `VR_STATUS` varchar(6) NOT NULL default '',
  `VR_LASTCOMMAND` varchar(255) NOT NULL default '',
  `VR_LASTHEARD` timestamp(14) NOT NULL
) TYPE=MyISAM COMMENT='MAINTAINS AFFILIATE WIDE NAMESPACE';

#
# Dumping data for table `virtrad`
#


# --------------------------------------------------------

#
# Table structure for table `vrcp`
#

CREATE TABLE `vrcp` (
  `VC_ID` varchar(16) NOT NULL default '',
  `VC_CREATETIME` timestamp(14) NOT NULL,
  `VC_USER` varchar(32) NOT NULL default '',
  `VC_GATEWAY` varchar(32) NOT NULL default '',
  `VC_DICOM` varchar(32) NOT NULL default '',
  `VC_DICOMWADO` tinyint(4) NOT NULL default '0',
  `VC_ACTION` tinyint(4) NOT NULL default '-1',
  `VC_FILTER` varchar(32) NOT NULL default '',
  `VC_MCDEST` varchar(32) NOT NULL default '',
  `VC_MCDESTWADO` tinyint(4) NOT NULL default '0',
  `VC_DESCRIPTION` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='all vrcp config info for all users';

#
# Dumping data for table `vrcp`
#

INSERT INTO `vrcp` VALUES ('4177cf22aa545', 20041021110050, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf21dae03', 20041021110049, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf21349f2', 20041021110049, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf1e5aaf7', 20041021110046, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf1eefd36', 20041021110046, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf1cea3b9', 20041021110044, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf1da8924', 20041021110045, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('0MVv7MDzFXPd', 20041021110032, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf1b179e3', 20041021110043, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cf1bc0513', 20041021110043, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');
INSERT INTO `vrcp` VALUES ('4177cd7fa4d45', 20041021105351, 'billd', 'mcpurple01', '', 0, 5, '', '', 0, '(action generated)');
INSERT INTO `vrcp` VALUES ('4177cf2379c85', 20041021110051, 'billd', 'MGHC', '', 0, -1, '', 'adsadsa', 1, '(mcdest generated)');

# --------------------------------------------------------

#
# Table structure for table `vrdt`
#

CREATE TABLE `vrdt` (
  `VD_ID` varchar(32) NOT NULL default '',
  `VD_CREATETIME` timestamp(14) NOT NULL,
  `VD_GATEWAY` varchar(32) NOT NULL default '',
  `VD_USER` varchar(32) NOT NULL default '',
  `VD_DISPLAYNAME` varchar(32) NOT NULL default '',
  `VD_AETITLE` varchar(32) NOT NULL default '',
  `VD_SHOWMENUS` tinyint(4) NOT NULL default '0',
  `VD_DICOMIPADDR` varchar(16) NOT NULL default '',
  `VD_DICOMPORT` tinyint(4) NOT NULL default '0',
  `VD_COMMENT` varchar(255) NOT NULL default ''
) TYPE=MyISAM COMMENT='vrcp Dicom Tables';

#
# Dumping data for table `vrdt`
#

INSERT INTO `vrdt` VALUES ('', 20041003190025, 'mcpurple01', 'billd', 'qqq', 'qqq', 1, '', 0, '');
INSERT INTO `vrdt` VALUES ('', 20041020164655, 'mcpurple01', 'billd', 'ddd', 'cd01', 0, '', 0, '');

# --------------------------------------------------------

#
# Table structure for table `vrmd`
#

CREATE TABLE `vrmd` (
  `VM_ID` varchar(32) NOT NULL default '',
  `VM_CREATETIME` timestamp(14) NOT NULL,
  `VM_GATEWAY` varchar(32) NOT NULL default '',
  `VM_USER` varchar(32) NOT NULL default '',
  `VM_DISPLAYNAME` varchar(32) NOT NULL default '',
  `VM_DESTINATION` varchar(32) NOT NULL default '',
  `VM_SHOWMENUS` tinyint(4) NOT NULL default '0',
  `VM_COLLEAGUES` tinyint(4) NOT NULL default '0',
  `VM_REGUSERS` tinyint(4) NOT NULL default '0',
  `VM_SENDEMAIL` tinyint(4) NOT NULL default '0',
  `VM_EMAIL` varchar(255) NOT NULL default '',
  `VM_EMAILTYPE` tinyint(4) NOT NULL default '0',
  `VM_WORKLISTONLY` tinyint(4) NOT NULL default '0',
  `VM_TEMPLIST` tinyint(4) NOT NULL default '0'
) TYPE=MyISAM COMMENT='vrcp MedCommons Destinations ';

#
# Dumping data for table `vrmd`
#

INSERT INTO `vrmd` VALUES ('', 20041021103458, 'MCPURPLE01', 'billd', 'xxxx', 'ssss', 0, 0, 0, 0, 'sss', 0, 0, 0);
INSERT INTO `vrmd` VALUES ('', 20041003190303, 'mcpurple01', 'billd', 'asdsa', 'assad', 0, 0, 0, 0, '', 0, 0, 0);
INSERT INTO `vrmd` VALUES ('', 20041021110032, 'MGHC', 'billd', 'adsadsa', 'asddsa', 1, 0, 0, 0, '', 0, 0, 0);

# --------------------------------------------------------

#
# Table structure for table `xdsregistry`
#

CREATE TABLE `xdsregistry` (
  `XD_ID` varchar(16) NOT NULL default '',
  `XD_OS_STUDYGUID` varchar(64) NOT NULL default '',
  `XD_TRACKING` varchar(16) NOT NULL default '',
  `XD_VR_VAETITLE` varchar(255) NOT NULL default '',
  `XD_NIMAGES` int(11) NOT NULL default '0',
  `XD_NSERIES` int(11) NOT NULL default '0',
  `XD_STUDYDESCRIPTION` varchar(200) NOT NULL default '',
  `XD_STUDYTIME` varchar(32) NOT NULL default '',
  `XD_COMMENTS` varchar(255) NOT NULL default '',
  `XD_MODALITY` varchar(32) NOT NULL default '',
  `XD_TIMESTAMP` timestamp(14) NOT NULL,
  `XD_AFFIILIATE` varchar(16) NOT NULL default '',
  `XD_HISTORY` varchar(255) NOT NULL default '',
  `XD_PATIENT` varchar(100) NOT NULL default '',
  `XD_PATIENTADDR` varchar(255) NOT NULL default '',
  `XD_STATUS` varchar(6) NOT NULL default ''
) TYPE=MyISAM COMMENT='NASCENT XDS STUDY REGISTRY';

#
# Dumping data for table `xdsregistry`
#

INSERT INTO `xdsregistry` VALUES ('9999999999999999', 'FC6C93A3FFB29621282077037843672F76F01715', '5555-6666-7777', 'virtual03.medcommons.net:9080', 0, 0, 'CT097/CV CTA LWREXT W&WO', '09/12/2004 12:39 PM', 'pls fix up', '1234', 20041020110639, 'phive', '(unknown)', 'Voxar4009_CTArunoff', '52 Waltham St, Watertown, MA 02132', 'NEW');
INSERT INTO `xdsregistry` VALUES ('9999999999999999', 'B6BBCC1819E86EC654DF88D901317A2504062DCE', '8765-2341-3428', 'mcpurple01.homeip.net:9080', 0, 0, 'HEAD CTA', '09/12/2004 12:39 PM', 'pls fix up', '1234', 20041020110649, 'phive', '(unknown)', 'Voxar4008  _CTACOW', '50 Waltham St, Watertown, MA 02132', 'NEW');
