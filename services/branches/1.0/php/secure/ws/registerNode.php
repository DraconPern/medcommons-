<?php
require_once "securewslibdb.inc.php";
require_once "utils.inc.php";

// see spec at ../ws/wsspec.html
class registerNodeWs extends securedbrestws {

  
  /**
   * anybody can call this service, so always return 
   * successfully 
   */
  function verify_caller() {
      return;
  }
  
  function xmlbody(){

    $db = DB::get();

    // pick up and clean out inputs from the incoming args
    $type = req('type');
    $hostname = req('hostname');
    $key = req('key');
    $expectedNodeId = req('nodeId');

    // echo inputs
    //
    $this->xm($this->xmnest ("inputs",  $this->xmfield("type",$type)));

    if(preg_match("/^[0-9]*$/",$expectedNodeId)!==1) {
      $this->xm($this->xmfield("status","failed").$this->xmfield("error","bad node id"));
    }

    // get the ipaddress and port of remote caller
    $ip = $_SERVER['REMOTE_ADDR'];

    // mushedip is used because we (mistakenly?) made the ip address
    // column in the db an integer.  This causes mysql to do some
    // strange things to turn ip addresses "127.0.0.1" into integers
    // which makes them no longer unique.  Hence we "mush" the ip.
    // We should probably just make the column some other data type
    // or maybe convert the ip address to it's full decimal form
    $mushedip = $this->muship($ip);

    // add to the node table
    $timenow=time();

    $nodeid = "";
    $nodeFound = false;
    $key = req('key');

    // See if we have encountered this node yet
    $node = $db->first_row("select * from node where client_key = ? or node_id = ?",array($key,$expectedNodeId));
    if($node) {
      $nodeid = $node->node_id;
    }
    else { // not found.  Make a new row
      $result = $db->first_row("select count(*) as cnt from node");
      $count = $result->cnt;
      if($count > 0)
	      $this->xmlend("Failed - unknown node.  Only the first node can be registered automatically.  Please register manually.");
      
      $nodeid = $db->execute("insert into node (node_id, hostname, fixed_ip  , node_type , client_key)
                              values (NULL, ?, $mushedip, 0, ?)",
                              array($hostname,$key));
    }

    // return outputs
    //docid,rightsid,mcid
    $this->xm($this->xmnest("outputs",
    $this->xmfield("ip",$ip).
    $this->xmfield("nodeid",$nodeid).
    $this->xmfield("status","ok")));
  }
}

//main

$x = new registerNodeWs();
$x->handlews("registerNode_Response");



?>
