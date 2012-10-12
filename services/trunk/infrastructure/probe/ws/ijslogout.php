<?

require_once 'settings.php';
require_once 'urls.inc.php';

require_once "login.inc.php";
require_once "wslibdb.inc.php";
require_once "../../acct/alib.inc.php";
//require_once "../../acct/rls.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";


class LogoutService extends jsonrestws  {

	function verify_caller() {
		// No verification needed
	}

	function jsonbody() {
		global $acCookieDomain;
		$wasloggedin = (isset($_COOKIE['mc'])?1:0); // send back our previous state
			
		// this part ripped directly out of /acct/logout.php
		session_start();

		if (isset($_COOKIE['mc'])) {
			if ($acCookieDomain && $acCookieDomain != 'localhost' )
			setcookie('mc', False, 1, '/', $acCookieDomain);
			else
			setcookie('mc', False, 1, '/');
		}

		setcookie('mode', False, 1, '/');

		if (isset($_COOKIE['mc_anon_auth'])) {
			if ($acCookieDomain && $acCookieDomain != 'localhost' )
			setcookie('mc_anon_auth', False, 1, '/', $acCookieDomain);
			else
			setcookie('mc_anon_auth', False, 1, '/');
		}

		// Destroy the session variables
		session_destroy();


		$info->wasloggedin = $wasloggedin;

		return $info;
	}
}

$ws = new LogoutService();
$ws->handlews("Logout One User");

?>