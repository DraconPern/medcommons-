<?php
require_once "wslibdb.inc.php";

/**
 * addDocumentWs 
 *
 * Adds a document to an account's nominated document table
 *
 * Inputs:
 *    accid - account id to add document for
 *    documentType - type of document to add.  There are a number of predefined types
 *    guid - guid of document to be added
 *    unique - set to true if the document should replace any existing document of the same type
 */
class addDocumentWs extends dbrestws {

	function xmlbody() {

    $db = DB::get();
    $docType = req('documentType');
    $docComment = req('comment');
    $guid = req('guid');
		$accid = req('accid');
		$unique = req('unique');

    if($docType == "") {
      $this->xm($this->xmnest("outputs",$this->xmfield("status","failed - documentType not provided")));
    }

    // If document type is unique, indicate that any old versions of the document have been
    // replaced in the CCR log.
    if($unique && ($unique=="true")) {
      $result = $db->execute("update  ccrlog c, document_type d
                                set c.merge_status = 'Replaced'
                                where c.accid = ?
                                and d.dt_account_id = c.accid
                                and d.dt_guid = c.guid
                                and d.dt_guid != ?
                                and d.dt_type = ?
                                and (c.merge_status is NULL or c.merge_status <> 'Replaced')",
                                  array($accid, $guid, $docType));
    }

    $insert = "insert into document_type (dt_id, dt_account_id, dt_type, dt_guid, dt_privacy_level,dt_comment)
               values (NULL, ?,?,?, 'Private',?);";

		$result = $db->execute($insert,array($accid,$docType,$guid,$docComment));

    $this->xm($this->xmnest("outputs",$this->xmfield("status","ok")));
	}
}

//main

$x = new addDocumentWs();
$x->handlews("addDocument_Response");

?>
