<?php
require_once "wslibdb.inc.php";
require_once "utils.inc.php";

function parse($s,$tag) {

  // returns whatever follows after tag, upto a blank or tab or eos
  $pos = strpos($s,$tag);
  if ($pos===false) return '';
  // alright, found it, look for next blank or tab
  $pos2 = strpos($s," ",$pos); 
  if ($pos2===false) $pos2=strpos($s,"	",$pos);//try again with tabs
  if ($pos2===false) $len = strlen($s) - strlen($tag); else $len = $pos2-$pos+1-strlen($tag);

  // 
  return substr($s,$pos+strlen($tag),$len);
}


class addCCREventWs extends dbrestws {

	  function xmlbody() {
        
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
      $gid = req('pid');

      $timenow=time();	// unix style integer timestamp
      $this->xm($this->xmnest ("inputs",
        $this->xmfield("gid","$gid"). //wld group multiplexing
        $this->xmfield("pfn","$pfn").
        $this->xmfield("pgn","$pgn").
        $this->xmfield("pid","$pid").
        $this->xmfield("pis","$pis").
        $this->xmfield("psx","$psx").
        $this->xmfield("pag","$pag").
        $this->xmfield("guid","$guid").
        $this->xmfield("purp","$purp").
        $this->xmfield("spid","$spid").
        $this->xmfield("rpid","$rpid").
        $this->xmfield("dob","$dob").
        $this->xmfield("cxpserv","$cxpserv").
        $this->xmfield("cxpvendor","$cxpvendor").
        $this->xmfield("viewerurl","$viewerurl").		
        $this->xmfield("comment","$comment").
        $this->xmfield("cc","$cc").
        $this->xmfield("rs","$rs")
      ));


       // parse out the comment field and override
      $x = parse($comment,'pfn:'); if ($x!='') $pfn = $x;
      $x = parse($comment,'pgn:'); if ($x!='') $pgn = $x;
      $x = parse($comment,'pid:'); if ($x!='') $pid = $x;
      $x = parse($comment,'pis:'); if ($x!='') $pis = $x;
      $x = parse($comment,'sprid:'); if ($x!='') $spid = $x;
      $x = parse($comment,'rprid:'); if ($x!='') $rpid = $x;

      // Update 
      if($pid && ($pid!="")) {
        $db->execute(
          "update practiceccrevents set ViewStatus = null
           where PatientIdentifier=? and practiceid=?",
          array($pid,$gid));
      }

      $row = $db->first_row("select value from mcproperties where property = 'acAccountStatus'");
      $statusValues = explode(',',$row ? $row->value : "");
      $defaultStatus = (count($statusValues)>0) ? $statusValues[0] : "";
       
      $insert=
      "INSERT INTO practiceccrevents (practiceid,PatientGivenName, PatientFamilyName, PatientIdentifier, PatientIdentifierSource, Guid, Purpose, 
          SenderProviderId, ReceiverProviderId, DOB, CXPServerURL, CXPServerVendor, ViewerURL,Comment,
          CreationDateTime, ConfirmationCode, RegistrySecret, PatientSex, PatientAge,Status,ViewStatus) 
          VALUES (?,?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?, ?, ?, ?, ?, ?, ?,?,'Visible');";
            
        $db->execute($insert,array($gid,$pgn,$pfn,$pid,$pis,$guid,$purp,$spid,$rpid,$dob,$cxpserv,$cxpvendor,$viewerurl,$comment,$timenow,$cc,$rs,$psx,$pag,$defaultStatus));
        
        $db->execute("insert into practice_patient (pp_practice_id, pp_name, pp_accid) values (?, 'inbox', ?)",
                      array($gid, $pid));
        
        $this->xm($this->xmnest("outputs",$this->xmfield("status","ok")));
    } 
}
//main
$x = new addCCREventWs();
$x->handlews("addCCREvent_Response");
?>
