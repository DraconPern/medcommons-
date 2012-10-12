<?php
require_once "dbparamsidentity.inc.php";
require_once "utils.inc.php";
require_once "wslibdb.inc.php";
require_once "../alib.inc.php";
require_once "JSON.php";


/**
 * Updates consents for a set of of account ids
 */
class UpdateSharingRightsWs extends jsonrestws {
    
    
    function jsonbody() {
        
        $accid = req("accid");
        
        $storageId = req('accid');
        $json = new Services_JSON();
        $result = new stdClass;
        $auth = req('auth');
        $updateAccts = array();
        $allaccts = array();
        
        // Find accounts to update
        foreach($_REQUEST as $accid => $rights ) {
          if($rights == "None")
            $rights = "";
          if(preg_match("/^[0-9]{16}$/",$accid)) { // If parameter matches account id format
        
            // Only process if we did not already process this account
            if(isset($allaccts[$accid]))
              continue;
        
            $allaccts[$accid]=true;
        
            $updateAccts[]="$accid=".$rights;
        
            // Expand to group members if there are any
            $members = q_group_members($accid);
            foreach($members as $m) {
              // if set explicitly in params, use explicit value, otherwise inherit from group
              $updateAccts[]="$m=".(isset($_REQUEST[$m])?$_REQUEST[$m]:$rights);
              $allaccts[$m]=true;
            }
          }
          else 
          if(preg_match("/^es_.*/",$accid)) { // If parameter matches external share format
            dbg("account: $accid");
            $updateAccts[]=urlencode($accid)."=".$rights;
          }
          else 
          if(preg_match("/^at_.*/",$accid)) { // If parameter matches application token
            dbg("updating rights for application token: $accid");
            $updateAccts[]=urlencode($accid)."=".$rights;
          }
        }
        
        $updateUrl = gpath('Commons_Url')."/ws/updateAccess.php?accid=".$storageId;
        $updateUrl .= "&auth=$auth";
        dbg("Fetching url: $updateUrl");
        
        $updateUrl.="&".join($updateAccts,"&");
        
        $contents = post_url($updateUrl,null,$this->get_node_key()); 
        $updateResult = $json->decode($contents);
        
        dbg("Update result ".$contents);
        if(!$updateResult)
            throw new Exception("Failed to parse output from updateAccess service: ".$contents);
            
        if($updateResult->status != "ok")
            throw new Exception("Update of access rights returned status ".$updateResult->status.": ".(isset($updateResult->message)?$updateResult->message:"no message"));
               
        return "ok";
    }
}

$x = new UpdateSharingRightsWs();
$x->handlews("response_updateSharingRights");
?>
