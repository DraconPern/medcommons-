<?php
// (c) 2004 MedCommons, Inc.
// wld 9/4/4

require_once ("../lib/htmlsubs.inc");

// Reset the Pink Box, sqlcmd away orders, order_series, hipaa_log, commands, and routing queue


// support


function doit ($table)
{

	db_empty_table($table);
	      

htmlbody(db_affected_rows()." rows from ".$table.eol());	
}

function restore_chastity()
{


 htmltop("Restore Chastity",
				PINKBOX_STYLE);


	

doit('orders');
doit('order_data');
doit('hipaalog');
doit('remote_commands');
doit('routing_queue');
doit('trace');
doit('vrcp');
doit('vrmd');
doit('vrdt');


htmlbody("The chastity of this Pink Box has been restored\n\r");
echo htmlfooter();
}

restore_chastity();

?>