<?PHP
require_once "securewslibdb.inc.php";
require_once "utils.inc.php";

/**
 * From a quick review this service seems to be doing almost entirely
 * the wrong thing.  A user's documents are stored with document.storage_id
 * equal to the user's account id.  Here instead it seems to be finding
 * all documents that the user as an invidual has an explicit right
 * to.  However when a user owns a document they have an implicit right to it
 * so it's possible for documents to be owned by a user but not returned here,
 * and it's also possible for this service to return documents that a user
 * doesn't own but has rights to.
 *
 * TODO:  NEEDS REVIEW
 * FIXME
 */
class queryUserDocumentsWs extends securedbrestws {

	function xmlbody() {
	
        $db = DB::get();
		$mcid=req('mcid');
        $query = "SELECT rights.account_id,rights.document_id,rights.rights_id,document.guid,document.storage_account_id
                  from rights INNER JOIN document
                  on (document.id = rights.document_id)
                  WHERE (account_id=?) and active_status = 'Active'";
		$result = $db->query($query,array($mcid));

        foreach($result as  $l) {
			$this->xm($this->xmfield("entry",
    			    $this->xmfield("mcid",$l->account_id),
    			    $this->xmfield("docid",$l->document_id),
    			    $this->xmfield("rightsid",$l->rights_id),
    			    $this->xmfield("guid",$l->guid),
    			    $this->xmfield("storageId",$l->storage_account_id))
		    );
		}

        $this->xmlend("success");
	}
}

//main
$x = new queryUserDocumentsWs();
$x->handlews("response_UserInfo");

?>
