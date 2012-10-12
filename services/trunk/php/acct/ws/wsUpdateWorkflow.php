<?php
require_once "wslibdb.inc.php";
require_once "mc.inc.php";
require_once "utils.inc.php";

/**
 * updateWorkflow
 *
 * Updates or adds a workflow item
 *
 * Inputs:
 *    key - externally supplied key that may be used to uniquely identify this item.
 *    src_accid - account id modifying or updating the item
 *    target_accid - account id that is the subject of the item
 *    type - type of the workflow item
 *    status - status of the workflow item
 *    auth - auth parameter proving access to target account
 */
class updateWorkflowWs extends dbrestws {
  function xmlbody(){
     $db = DB::get();

    $srcAccid = req('src_accid');
    $targetAccid = req('target_accid');
    $type = req('type');
    $status = req('status');
    $key = req('key');
    $auth = req('auth');

    dbg("src: $srcAccid trg: $targetAccid");

    // Validate
    if(!is_valid_mcid($srcAccid,true) || !is_valid_mcid($targetAccid,true) || !is_safe_string($type,$status,$key)) {
      $this->xmlend("invalid input");
    }
    
    /*
     Fails self test at the momement.
    if(strpos(getPermissions($auth, $targetAccid),"R")===FALSE)
      throw new Exception("specified auth does not have permission to target account $targetAccid");
    */

    $wi = $db->first_row("select wi_id from workflow_item where wi_source_account_id = ? and wi_target_account_id = ? and wi_key = ?",
                         array($srcAccid,$targetAccid,$key));

    if($wi) { // Exists already, do update
       // BUG Mark all workflow items for same patient. Should be marking individual item.
       $db->execute("update workflow_item set wi_status = ? where wi_target_account_id = ?",
                     array($status,$targetAccid));

    }
    else { // Does not exist, insert
       $db->execute("insert into workflow_item                                                                                                                                                           
                     (wi_source_account_id, wi_target_account_id, wi_key, wi_type, wi_status)                                                                                                            
                     values (?,?,?,?,?)",array($srcAccid,$targetAccid, $key, $type, $status));

        // BUG: Then mark all workflow items with same state; otherwise they won't show up in the worklist.
       $db->execute("update workflow_item set wi_status = ? where wi_target_account_id = ?",
                     array($status,$targetAccid));
      
    }
    $this->xm($this->xmnest("outputs",$this->xmfield("status","ok")));
  }
}

global $is_test;
if(!$is_test) {
  //main
  $x = new updateWorkflowWs();
  $x->handlews("updateWorkflow_Response");
}
?>
