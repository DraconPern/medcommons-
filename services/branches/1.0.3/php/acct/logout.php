<?
/*
 * This file is a wrapper for the real logout which belongs to the 'account' services
 * and thus *may* reside on a different server.
 *
 * ttw - replaces the identity server's logout... go through a list of cookies to clear,
 *       then redirect to the home page
 */

$_GLOBALS['no_session_check']=true;

include 'settings.php';
include 'urls.inc.php';
include 'login.inc.php';
require_once 'utils.inc.php';
require_once 'DB.inc.php';

// If the user has an authentication token, kill it
if($info = get_account_info()) {
	$db = DB::get();
	$db->execute("update authentication_token set at_expired_date_time = NOW() where at_token = ?",
	              array($info->auth));
}

logout($acHomePage);

?><html>
 <head>
  <meta http-equiv='Location' content='<?= $acHomePage ?>' />
 </head>
 <body>
<a href='<?= $acHomePage ?>'><?= $acHomePage ?></a>
 </body>
</html>
