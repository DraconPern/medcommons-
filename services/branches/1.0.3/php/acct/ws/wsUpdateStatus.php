<?php

require_once "wslibdb.inc.php";
require_once "../alib.inc.php";
require_once "utils.inc.php";

nocache();

/**
 * Modifies status of an existing CCR event
 *
 * @param pid - practice id of group to modify status for
 * @param 
 */
class updateStatusWs extends jsonrestws {
    
    /**
     * This WS can be invoked directly from browser, so we override
     * normal client verification (which looks for node key) with
     * cookie verification.
     */
    function verify_caller() {
        validate_query_string();
        
        $info = get_validated_account_info();
        if(!$info || !$info->practice || !$info->practice == req('pid','invalid'))
	        throw new Exception("Unable to verify practice");
    }
    
    function jsonbody() {
        
        $pid = req('pid');
        $cc = req('cc');
        $status = req('status');
        
        $count = pdo_first_row("select count(*) as c from practiceccrevents ".
                               "where practiceid = ? and ConfirmationCode = ?",
                               array($pid,$cc));
        if($count->c != 1)
            throw new Exception("Incorrect count for tracking number ".$cc.": ".$count->c);
            
		pdo_execute("update practiceccrevents set Status = ?, ViewStatus='Visible' where practiceid = ? and ConfirmationCode = ?",
		             array($status,$pid,$cc));
	    return "ok";
	  }
}

$x = new updateStatusWs();
$x->handlews("response_updateStatus");