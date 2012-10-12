<?php
/**
* find records from the todir moved into /groups on 14 aug 06
*/
require_once "wslibdb.inc.php";
// see spec at ../ws/wsspec.html
class queryToDirWs extends dbrestws {
	function xmlbody(){
	    
	    $db = DB::get();
	    
		$where = '';
		$params = array();
		
		// pick up and clean out inputs from the incoming args
		$xid = req('xid');
		if ($xid) {
			if ($where!='') $where.=' and ';
			$where.= "(td_xid=?)";
			$params[]=$xid;
		}
		$ctx = req('ctx');
		if ($ctx) {
			if ($where!='')
			 $where.=' and ';
			$where.= "(td_owner_accid=?)";
			$params[]=$ctx;
		}
		$alias = req('alias');
		if ($alias) {
			if ($where!='') 
    			$where.=' and ';
			$where.= "(td_alias=?)";
			$params[]=$alias;
		}
		$accid = req('accid'); //accid of administrator making this entry
		if ($accid) {
			if ($where!='') $where.=' and ';
			$where.= "(td_contact_accid=?)";
			$params[]=$accid;
		}
		
		if ($where!='')	$where = "where $where";
		$timenow=time();
			
		$count = 0; $bulk='';
		$query="SELECT t.*, g.accid as groupAcctId  FROM todir t LEFT JOIN groupinstances g on g.accid =t.td_contact_accid $where";
		$result = $db->query($query,$params);
		foreach($result as $l) {
          $bulk.= $this->xmnest("todir_entry",
          $this->xmfield ("ctx", $l->groupAcctId).
          $this->xmfield ("xid",$l->td_xid).
          $this->xmfield ("alias",$l->td_alias).
          $this->xmfield ("accid",$l->td_contact_accid).
          $this->xmfield ("sharedgroup",$l->td_shared_group).
          $this->xmfield ("pinstate",$l->td_pin_state).
          $this->xmfield ("contact",$l->td_contact_list));
          $count++;	
		}
	
		$this->xm($this->xmnest("outputs",$bulk.$this->xmfield("status","ok rows=$count")));
	}

}

//main

$x = new queryToDirWs();
$x->handlews("queryToDir_Response");
?>
