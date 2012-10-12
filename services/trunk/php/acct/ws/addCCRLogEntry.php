<?php
/**
 * Adds a new entry in the CCR Log
 *
 * ssadedin: modified 8/31/06 to support multiple accounts in one call
 */
require_once "wslibdb.inc.php";
// see spec at ../ws/wsspec.html
class addCCRLogEntryWs extends dbrestws {

  function xmlbody() {
      $db = DB::get();

    // pick up and clean out inputs from the incoming args
    $date = req('date');
    $idp = req('idp');
    $accid = req('accid');
    $guid = req('guid');
    $tracking  = req('tracking');
    $from = req('from');
    $to = req('to');
    $subject = req('subject');
    $status = req('status');
    $timenow=time();                        

    // add to the CCRLogEntry table
    $accids = explode(",",$accid);
    foreach($accids as $a) {
      $insert="INSERT INTO ccrlog(accid, guid,tracking,status, date ,src, dest,subject,idp) ".
            "VALUES(?,?,?,?, NOW(),?,?,?,?)";
      $ob= "UPDATE users SET  ccrlogupdatetime = ? where (mcid = ?)";
      $db->execute($ob,array($timenow,$a));
      $db->execute($insert,array($a,$guid,$tracking,$status,$from,$to,$subject,$idp));
    } 

    $this->xm($this->xmnest("outputs", $this->xmfield("status","ok $ob")));
  }
}

//main

$x = new addCCRLogEntryWs();
$x->handlews("addCCRLogEntry_Response");
?>
