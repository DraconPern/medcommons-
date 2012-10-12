<?php
require_once "../ws/wslib.inc.php";
// see spec at ../ws/wsspec.html
class registerDocumentWs extends dbrestws {

	function xmlbody(){
		
		// pick up and clean out inputs from the incoming args
		$mcid =$this->cleanreq('mcid');
		$guid = $this->cleanreq('guid');
		$rights = $this->cleanreq('rights');
		
		//
		// echo inputs
		//

		$this->xm($this->xmfield ("inputs",	$this->xmfield("rights",$rights).$this->xmfield("guid",$guid)));

		//
		// add to the document table

		$timenow=time();
		$insert="INSERT INTO document (guid,creation_time) ".
					"VALUES('$guid',NOW())";
		$this->dbexec($insert,"can not insert into table document - ");
		//
		//pick up the id we just created
		$docid = mysql_insert_id();
		//
		// add to the rights table
		//
		$insert="INSERT INTO rights (user_medcommons_user_id,document_ID,rights,creation_time) ".
													"VALUES('$mcid','$docid','$rights',NOW())";
		$this->dbexec($insert,"can not insert into table rights - ");
		$rightsid = mysql_insert_id();
		// return outputs
		//docid,rightsid,mcid
		$this->xm($this->xmfield ("outputs",
		$this->xmfield("docid",$docid).
		$this->xmfield("rightsid",$rightsid).
		$this->xmfield("mcid",$mcid).

		$this->xmfield("status","ok")));
	}
}

//main

$x = new registerDocumentWs();
$x->handlews("registerDocument_Response");



?>