<?
/**
 * Displays the dashboard view of a patient's details by
 * redirecting to an OAuth-signed URL to load them 
 * fromt the patient's gateway.
 */

require_once "mc_oauth_client.php";
require_once "healthbook.inc.php";
require_once "utils.inc.php";

list ($facebook,$fbid) =fbinit(false); 
list($u,$t) = mustloadtarget($facebook, $fbid);

$href = $t->appliance."/".$t->mcid."/status";
$href = $t->getOAuthAPI()->find_storage($t->mcid)."/CurrentCCRWidget.action?combined=true&accid={$t->mcid}";
dbg("patient details url = $href");

$authorized_href = $t->authorize($href);
dbg("auth'd patient details url = $authorized_href");

// Redirect the user there to get authorization 
echo "<fb:fbml version='1.1'><fb:redirect url='$authorized_href' /></fb:fbml>";
?>
