<?php
require_once "../ws/securewslibdb.inc.php";
// see spec at ../ws/wsspec.html
class registerDocumentWs extends securedbrestws {

	function xmlbody(){
     $db = DB::get();
		
		// pick up and clean out inputs from the incoming args
		$mcid =$this->cleanreq('mcid');
		$guid = $this->cleanreq('guid');
		$ekey = $this->cleanreq('ekey');
		$intstatus = $this->cleanreq('intstatus');

		//process optional host arg if any
		$this->gethostarg();
	    $rights = isset($_REQUEST["right"])? $_REQUEST["right"] : "";

		//
		// add to the document table
		$timenow=time();
		$insert="INSERT INTO document (guid,storage_account_id,creation_time) ".
            "VALUES('$guid','$mcid',NOW())";
	    $docid =	$db->execute($insert);

		//pick up the id we just created

		// put an entry in the document location table
		$locid= $this->adddocumentlocation($docid,$this->getnodeid(),$ekey,$intstatus);

		//
		// echo inputs
		//
		$this->xm($this->xmnest ("inputs",	$this->xmfield("rights",$rights).$this->xmfield("guid",$guid).
		$this->xmfield("locid",$locid).
		$this->xmfield("ekey",$ekey)));

		//
		// add to the rights table
		//
	    $expiry = get_account_expiry($mcid);
		$insert="INSERT INTO rights (account_id,document_id,rights,creation_time, expiration_time) ".
													"VALUES('$mcid','$docid','R',NOW(),?)";
		$rightsid = $db->execute($insert,array($expiry));

	    // Add additional rights to the table
	    if(is_array($rights)) {
	      foreach($rights as $rs) {
	        $r = explode("=",$rs);
	        dbg("Granting right ".$r[1]." to account ".$r[0]." for document $docid");
	        $db->execute("INSERT INTO rights(rights_id, document_id, account_id, rights,expiration_time)
	                       VALUES (NULL, $docid,?,?, ?)", array($r[0],$r[1],$expiry));
	      }
	    }
			    
		// return outputs
		//docid,rightsid,mcid
		$this->xm($this->xmnest("outputs",
		$this->xmfield("docid",$docid).		$this->xmfield("locid",$locid).
		$this->xmfield("rightsid",$rightsid).
		$this->xmfield("mcid",$mcid).

		$this->xmfield("status","ok")));
	}
}

//main

$x = new registerDocumentWs();
$x->handlews("registerDocument_Response");



?>
