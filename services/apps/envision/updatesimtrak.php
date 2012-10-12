<?
/**
 * AJAX service for updating Simtrak with data from web editor.
 *
 * Author:  Simon Sadedin <ssadedin@medcommons.net>, MedCommons Inc.
 */
require_once 'utils.inc.php';
require_once "Envision-DataKit/JSON.php";
require_once "simtrak.inc.php";

nocache();

$json = new Services_JSON();
$res = new stdClass;
try {

  // TODO: authenticate user
  // TODO: implement / verify signature on request

  $player = $_GET['patientid'];
  $recordid = $_GET['recordid'];

  if(!preg_match('/[0-9]{1,12}/',$player)===1) 
    throw new Exception("Bad format for patientid: $player");

  // For now we just print the modified fields to the log
  $update_count = 0;
  foreach($_POST as $key => $value) {

    // The id of the field is joined with the id of the tab 
    $id_parts = explode('_',$key);
    if(count($id_parts) != 2)
      throw new Exception("Bad format for field $key");

    $tab = $id_parts[0];
    $result = dosql("Select ddtable,pivotfield from _viewerorder where tabkey='$tab' ");
    $ddtable = mysql_fetch_array($result);
    $table = $ddtable[0];
    $pivot = $ddtable[1];
    
    $result = dosql("Select simtrakid,mcid from players where playerind='$player' ");
    $simtrakid = mysql_fetch_array($result);
    $personid = $simtrakid[0];
    $mcid = $simtrakid[1];
    
    $field = $id_parts[1];
	$q = ("update $table set $field='$value' where id='$recordid' ");
	dosql($q);
	
	$q = mysql_real_escape_string($q);
    islog("update",$mcid,"$q ".mysql_error());

    $update_count++;
  }
  $res->status = 'ok';
  $res->updateCount = $update_count;
}
catch(Exception $e) {
    islog("update failure","unknown","failed to update player $player: ".$e->getMessage());
    $res->status = 'failed';
    $res->error = $e->getMessage(); 
    header('HTTP/1.1 500 Internal Server Error',true,500);
}
echo $json->encode($res);
