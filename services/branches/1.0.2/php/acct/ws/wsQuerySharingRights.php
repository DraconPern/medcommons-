<?php
require_once "settings.php";
require_once "consent_support.inc.php";
require_once "utils.inc.php";
require_once "wslibdb.inc.php";
require_once "JSON.php";

class QuerySharingRightsWs extends jsonrestws {
    function jsonbody() {
      $accid = req("accid");
    
      $rights = get_sharing_info($accid,$this->get_node_key());
    
      return $rights;
    }
}

$ws = new QuerySharingRightsWs();
$ws->handlews("response_querySharingRights");
    
?>
