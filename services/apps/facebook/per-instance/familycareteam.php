<?php

require 'healthbook.inc.php';




function family_care_team_page($user,$facebook)
{
	$appname = $GLOBALS['healthbook_application_name'];

	$u = mustload($facebook,$user);

	$familyfbid = $u->familyfbid;
	$dash = dashboard($user);
	$title="Family Car Team";
	$markup = siblings($user,$facebook,$familyfbid);

	$markup = <<<XXX
	<fb:fbml version='1.1'><fb:title>$title</fb:title>
	$dash
	$markup

</fb:fbml>
XXX;
	return $markup;
}

// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit();
// *** end of basic start
// jumping off to outer space
echo family_care_team_page($user,$facebook);
exit;



?>
