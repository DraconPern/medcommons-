<?php
require_once "wslibdb.inc.php";
require_once "utils.inc.php";

class queryRLSWs extends dbrestws {


	function xmlbody(){
    $db = DB::get();

    // these are the basic query parameter arguments that are passed around
    $pfn = req('PatientFamilyName');
    $pgn = req('PatientGivenName');
    $pid= req('PatientIdentifier');
    $pis= req('PatientIdentifierSource');
    $psx = req('PatientSex');
    $pag = req('PatientAge');
    $spid  = req('SenderProviderId');
    $rpid  = req('ReceiverProviderId');
    $dob  = req('DOB');
    $cc = req('ConfirmationCode');

    // these are not query parameters, but are passed around
    $rs = req('RegistrySecret');
    $guid = req('Guid');
    $purp = req('Purpose');
    $cxpserv = req('CXPServerURL');
    $cxpvendor = req('CXPServerVendor');
    $viewerurl = req('ViewerURL');
    $comment = req('Comment');

    // these params control the formatting of output
    $int = req('int'); // if non-zero, ajax'd dynamic updates
    $st = req('st');
    $ti = req('ti');
    $limit = req('limit');
    $logo = req('logo');

    // this multiplexes the group - wld 072506
    $pcid = req('pid');  // note this is the practice id


    // build WHERE clause for select statement based on the arguments
    // added $pcid multiplexing on groupid
    $where = "WHERE practiceid = '$pcid' and ViewStatus='Visible'"; 

    $params = array();
    $clauses = array();

    if ($pfn!='') { $clauses[]="PatientFamilyName LIKE  ?"; $params[]=$pfn ;}
    if ($pgn!='') { $clauses[]="PatientGivenName LIKE ?"; $params[]=$pgn ;}
    if ($pid!='') { $clauses[]="PatientIdentifier = ?"; $params[]=$pid ;}
    if ($pis!='') { $clauses[]="PatientIdentifierSource=?"; $params[]=$pis ;}
    if ($cc!='')  { $clauses[]="ConfirmationCode=?";  $params[]=$cc ;}
    if ($dob!='') { $clauses[]="DOB=?";  $params[]=$dob ;}
    if ($spid!='') { $clauses[]="SenderProviderId=?"; $params[]=$spid ;}
    if ($rpid!='') { $clauses[]="ReceiverProviderId=?";  $params[]=$rpid ;}

    $whereclause = $where ." AND ".implode(" AND ", $clauses);

    $limit = req('limit','20');
    if($limit>20)
        $limit=20;

    $timenow=time();	// unix style integer timestamp
    $this->xm($this->xmfield ("inputs",
       $this->xmfield("gid","$pcid").
       $this->xmfield("PatientFamilyName","$pfn").
       $this->xmfield("PatientGivenName","$pgn").
       $this->xmfield("PatientIdentifier","$pid").
       $this->xmfield("PatientIdentifierSource","$pis").
       $this->xmfield("SenderProviderId","$spid").
       $this->xmfield("ReceiverProviderId","$rpid").
       $this->xmfield("DOB","$dob").
       $this->xmfield("ConfirmationCode","$cc").
       $this ->xmfield ("where","$where").
       $this->xmfield("timenow","$timenow")
    ));


    $select= "SELECT * FROM practiceccrevents $whereclause ORDER BY CreationDateTime DESC";
    if($limit > 0) $select.=" LIMIT $limit";

    $rows = $db->query($select,$params);
    if(count($rows) == 0)
     $this->xm($this->xmnest("outputs",$this->xmfield("status","failed 0 rows returned")));
    else { //rows
        $this->xm("<outputs>".$this->xmfield("status","ok rows=$rows limit=$limit"));
        foreach($rows as $l) {
            $this->xm($this->xmnest("RLSentry",
              $this->xmfield("PatientFamilyName",$l->PatientFamilyName).
              $this->xmfield("PatientGivenName",$l->PatientGivenName).
              $this->xmfield("PatientSex",$l->PatientSex).
              $this->xmfield("PatientIdentifier",$l->PatientIdentifier).
              $this->xmfield("PatientIdentifierSource",$l->PatientIdentifierSource).
              $this->xmfield("DOB",$l->DOB).
              $this->xmfield("PatientAge",$l->PatientAge).
              $this->xmfield("Guid",$l->Guid).
              $this->xmfield("Purpose",$l->Purpose).
              $this->xmfield("CXPServerURL",$l->CXPServerURL).
              $this->xmfield("CXPServerVendor",$l->CXPServerVendor).
              $this->xmfield("ViewerUrl",$l->ViewerURL).
              $this->xmfield("Comment",$l->Comment).
              $this->xmfield("ConfirmationCode",$l->ConfirmationCode).
              $this->xmfield("RegistrySecret",$l->RegistrySecret).
              $this->xmfield("CreationDateTime",$l->CreationDateTime).
              $this->xmfield("SenderProviderId",$l->SenderProviderId).
              $this->xmfield("ReceiverProviderId",$l->ReceiverProviderId).
              $this->xmfield("Status",$l->Status)
         ));
    } ; // end of while
    $this->xm("</outputs>");
    } // end of have rows
  }
}

//main

$x = new queryRLSWs();
$x->handlews("queryRLS_Response");
?>
