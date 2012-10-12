<?
/**
 * JSON service returns all consents for the current, logged in user
 */
require_once "utils.inc.php";
require_once "ws/consent_support.inc.php";
require_once "JSON.php";

$result = new stdClass;
try {
    
    validate_query_string();
    
    $info = get_account_info();
    $result->consents = get_sharing_info($info->accid,false);
    $result->status = "ok";
}
catch(Exception $ex) {
    $result->status = "failed";
    $result->error = $ex->getMessage();
}

$json = new Services_JSON();
echo $json->encode($result);
?>