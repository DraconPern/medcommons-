<?php
require_once "../ws/securewslibdb.inc.php";

/**
 * getPermissions 
 *
 * returns the string of permissions representing the access available to the
 * given authentication context to the requested account.
 * 
 * @param auth - authentication token identifying authentication context
 * @param toAccount - account to which permissions are being queried
 *
 * @author ssadedin@medcommons.net
 */
class getPermissionsWs extends securedbrestws {
    
    /**
     * This function is called in some situations by /acct code
     * so there is no 'node key' to verify.
     * Since it only returns permissions rather than changing them
     * this is not too critical.
     */
    function verify_caller() {
        try {
            verify_local_call();
        }
        catch(Exception $e) {
            // If we cannot verify that this is a local call,
            // allow it to come from a remote gateway by 
            // delegating to parent to check node key
            parent::verify_caller();
        }
    }

    function xmlbody() {

        // pick up and clean incoming arguments
        $toAccount=$_REQUEST['toAccount'];
        $auth=$_REQUEST['auth'];
        $rights = "";
        $status = "ok";
    
        try {
          $rights = $this->get_authorized_rights($auth, $toAccount);
        }
        catch(Exception $e) {
          error_log("Failed to get rights for auth $auth and account $toAccount : ".$e->getMessage());
          $status = "failed";
          $rights = "";
        }
     
        $this->xm($this->xmnest ("inputs",
                      $this->xmfield("toAccount",$toAccount)).
                  $this->xmnest ("outputs", 
                      $this->xmfield("status",$status).$this->xmfield("rights", $rights)));
    }
}

//main
$x = new getPermissionsWs();
$x->handlews("getPermissions_Response");
?>
