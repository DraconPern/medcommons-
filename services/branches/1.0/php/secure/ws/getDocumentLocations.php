<?php
/**
 *  Returns an array of DocumentLocation objects for the specified guid. If 
 *  the nodeName is null then all locations are returned; otherwise only the 
 *  ones matching the specified nodeName are returned.
 *
 *  @param guid 
 *  @param nodeName 
 */
require_once "../ws/securewslibdb.inc.php";

class getDocumentLocationsWs extends securedbrestws {

	function xmlbody() {

    $db = DB::get();

		// pick up and clean out inputs from the incoming args
		$guid = req('guid');
		$node = req('node','');
		$storageId = req('storageId');
	
		// echo inputs
		$this->xm($this->xmnest("inputs",
      $this->xmfield("node",$node).
      $this->xmfield("guid",$guid)));
				
		$docid = $this->finddocument($storageId,$guid);
    if($docid=="") 
      throw new Exception("can't find $guid ");
		
		$select="SELECT * FROM document_location WHERE (document_id = ?)";
    if($node!='') 
      $select .= " AND (node_id = '$node')";
		
		$result = $db->query($select, array($docid));

		$str = "";
    foreach($result as $dlobj) {
				$str .= "<docloc><guid>".$dlobj->guid."</guid><ekey>".$dlobj->encrypted_key.
                 "</ekey><node>".$dlobj->node_id."</node></docloc>";
		}

		// return outputs
		$status = "ok";
		$this->xm($this->xmnest("outputs",
      $this->xmfield("docid",$docid).
      $this->xmnest("doclocs",$str).
      $this->xmfield("status",$status)));
	}
}

//main

$x = new getDocumentLocationsWs();
$x->handlews("getDocumentLocations_Response");
?>
