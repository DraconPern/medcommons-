<?php
require_once "session.inc.php";
require_once "securelib.inc.php";
require_once "DB.inc.php";
require_once "utils.inc.php";

// turns a tracking number into a Guid, and ultimately redirects through tracking.jsp
function tracking_process($tracking, $pin = false)
{	
    
	dbconnect();
	$node = tracking_to_node($tracking);
	if($node) {
	    dbg("got gateway {$node->hostname} and guid {$node->guid} for lookup of tn $tracking");
	    
		// if the tracking number is found, the user is redirected to the correct gateway
		tracking_redirect($node,$tracking,$pin); 
	}
	   
	// Not found
	dbg("no details found for tracking number $tracking");
	   
	return;
}

/**
 * Query to find the node where the specified tracking number resides.  
 * Return the host name of the gateway and the guid of the document referred to.
 * <p>
 * NOTE: be careful calling this, you probably want the storage id as well, 
 * a guid by itself is not very useful and a little dangerous.
 * 
 * @param string $t
 * @param string $guid
 * @return string
 */
function tracking_to_node_guid ($t,&$guid) {
    $n = tracking_to_node($t);
    if(!$n)
	    return FALSE;
	
    $guid = $n->guid;
    return $n->hostname; 
}

/**
 * converts a tracking number into a gateway node to redirect to:
 * returns node, document and location details in an object, or FALSE
 *
 * 5/1/08 - ssadedin: rewrote using pdo
 */
function tracking_to_node($t)
{ 
    $db = DB::get();

    $nodes = $db->query("SELECT n.hostname, d.guid, d.storage_account_id FROM tracking_number t, document d, document_location l, node n
                         WHERE tracking_number= ?
                         AND d.id = t.doc_id
                         AND l.document_id = d.id
                         AND n.node_id = l.node_id", array($t));

    if(count($nodes)==0)
      return FALSE;

    return $nodes[0];
}

function tracking_redirect($node,$tracking,$pin=false) //*****
{	
    $gw = $node->hostname;
    $guid = $node->guid;
    
    if(is_logged_in()) {
	    $info = get_account_info();
	    $accid = $info->accid;
	    $auth = $info->auth;
    }
    else {
		$accid='0000000000000000';
		$auth = false;
    }
    
    // Default URL for non-logged in user
	$url = "$gw/tracking.jsp?tracking=$tracking&auth=$auth";
	if($pin) {
	  $url .= '&p='.sha1($pin);
	}
	
	if($auth) { 
	    // if resolved, user has access without pin - go straight there 
	    $rights = resolve_guid($accid, $guid, $auth);  
	    
	    if($rights) {
		    foreach($rights as $r) {
		        
		        dbg("Rights {$r->rights_id} - ".$r->rights);
		        
		        if($r->rights == "")
			        break;
			        
			    if(strpos($r->rights, "R")!==FALSE) {
				    $url = "$gw/access?g=$guid&t=$tracking&at=$auth";
				    dbg("Found access by logged in auth");
				    break;
			    }
		    }
	    }
	}

//$terryUrl = strong_url($url);
$terryUrl = $url;

$x=<<<XXX
<html><head><title>Redirecting to MedCommons Repository Gateway from track.inc.php via $url</title>
<meta http-equiv="REFRESH" content="0;url='$terryUrl'"></HEAD>
<body >
<p>
Please wait whilst we connect to the MedCommons Repository Gateway...
</p>
</body>
</html>
XXX;
echo $x;
	exit;
}
?>
