<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertroutinginfo.php
require_once ("../lib/htmlsubs.inc");

function NetOpsPanel ()
{
htmltop("Network Operations Panel",
				PINKBOX_STYLE);

$strsw = "Schema(".$GLOBALS['SchemaMajorVersion'].".".
                 $GLOBALS['SchemaMinorVersion'].") Software(".
                 SWMajorVersion.".".SWMinorVersion.") ".$GLOBALS['SoftwareStatus'];


$xx = $GLOBALS['DB_Connection']." ".$GLOBALS['DB_Database']." ".$GLOBALS['DB_User'].
	" ".$_SERVER['SERVER_NAME']." ".$_SERVER['SERVER_ADDR'];

htmlbody(
			"You are irrevocably hardwired (via ../dbconfig.inc) to: $xx".eol().
			eol().eol().
			alink("../controller/cuisimulategateway.php","Simulate").
			" a MedCommons Gateway (Purple Box)".eol().eol().
			alink("../controller/cuimonitor.php","Control").
			"this MedCommons Registry (Pink Box)".eol().eol().
			alink("http://virtual01.medcommons.net/zabbix/","Monitor").
			"the network via Zabbix".eol().eol().
			alink("../utils/edittablesviaphpadmin.php","Edit").
			"&nbsp;Tables in phpMyAdmin".eol().eol().
			alink("../utils/resetpinkbox.php","Reset").
			"the Pink Box - restore to virgin state".eol().eol().
			alink("../whitebox/index.php","Whitebox").
			" Partner UI Prototype    ".alink("../whitebox/sqltrace.php","params").eol().eol().
			eol()
			);

		
	echo htmlfooter();
}

NetOpsPanel();
?>
