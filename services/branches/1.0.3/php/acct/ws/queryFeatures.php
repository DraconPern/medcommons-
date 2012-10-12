<?php
require_once "../alib.inc.php";
require_once "wslibdb.inc.php";
require_once "utils.inc.php";
require_once "login.inc.php";
require_once "mc.inc.php";

nocache();

/**
 * Returns the features enabled on this appliance
 */
class queryFeaturesWs extends jsonrestws {
  function jsonbody() {
    $db = DB::get();
    $results = array();
    
    $features = $db->query("select * from mcfeatures");
    foreach($features as $f) {
        $obj = new stdClass;
        $obj->name = $f->mf_name;
        $obj->description = $f->mf_description;
        $obj->enabled = $f->mf_enabled == 1 ? true : false;
	    $results[]= $obj;
    }
    return $results;
  }
}

$x = new queryFeaturesWs();
$x->handlews("response_queryFeatures");
?>
