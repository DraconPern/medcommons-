<?
/**
 * AJAX / JSON Service to hide patients.  Requires signed request from client.
 */
require_once "utils.inc.php";
require_once "alib.inc.php";
require_once "JSON.php";

validate_query_string();

$info = get_validated_account_info();

if(!$info)
    throw new Exception("Must be logged on to access this function");

if(!$info->practice)
    throw new Exception("User does not have a currently active group / practice");

$patientId = req('patientId');
echo hide_patient($info->practice->practiceid, $patientId);
?>
