<?
/**
 * Redirects to a user's custom logo
 */
require_once "DB.inc.php";
require_once "alib.inc.php";

global $acLogo;
    
$user = get_validated_account_info();
if(!$user->practice) {
    header("Location: $acLogo");
    exit;
}

header( "HTTP/1.1 301 Moved Permanently" ); 
header("Location: ".$user->practice->logo_url);
    
?>