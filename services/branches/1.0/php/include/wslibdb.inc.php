<?

/**
 * Do not apply session timeout to service calls
 */
$_GLOBALS['no_session_check']=true;

require_once "dbparamsidentity.inc.php";
require_once "wslib.inc.php";
require_once "settings.php";
require_once "DB.inc.php";
require_once "JSON.php";

/**
 * Abstract class for providing REST style web services.
 * <p>
 * Note: there is nothing very RESTful about these services
 * except they are not using SOAP as a substrate.
 * <p>
 * Note: by DEFAULT services require a MedCommons Key header
 * called 'HTTP_X_MEDCOMMONS_KEY' containing a valid gateway
 * node id.  If you are making a service for public consumption
 * you need to override the verify_caller() method with your
 * own implementation for verifying the caller.  Please note that
 * verifying callers using cookies alone is typically *not* sufficient
 * and will result in CSRF attack points.  A signature on the URL
 * is the only way to properly protect calls.
 */
abstract class dbrestws extends restws {

    /**
     * Checks that the caller is a bona-fide known client with a real node key
     */
    function verify_caller() {
        $db = DB::get();
        $key = $this->get_node_key();
        $result = $db->first_row("select 1 from node where client_key = ?",array($key));
        if(!$result)
          throw new Exception("Failed to validate client key $key");
    }
    
    function get_node_key() {
        
        /*
        foreach($_SERVER as $k => $v) {
            dbg("Server:  $k  ==>  $v");
        }
        */
        
        if(!isset($_SERVER['HTTP_X_MEDCOMMONS_KEY']))
              throw new Exception("No client key set");
              
        $key = $_SERVER['HTTP_X_MEDCOMMONS_KEY'];
          
        return $key;
    }

    function handlews($servicetag)
    {
      $this->set_servicetag($servicetag);
      // do standard processing for all web services
      $this->xmltop();
      
      $status = "success";
      try {
        $this->verify_caller();
  
        // the xmlbody routine is always overriden
        $this->xmlbody();
      }
      catch(Exception $e) {
          error_log("Web service $servicetag failed: ".$e->getMessage());
          $status = "failed - ".$e->getMessage();
      }
      $this->xmlend($status);
    }
}

/**
 * An extension of dbrestws to make it render JSON instead of
 * XML.  
 *
 * In addition to other services provide by dbrestws, child classes
 * can simply throw exceptions to handle errors, or they can just:
 *
 *   return $this->error("some message")
 */
abstract class jsonrestws extends dbrestws {
  /**
   * Convenience method - sets error message and returns failure status.
   */
  function error($msg) {
    $this->message = $msg;
    return false;
  }

  /**
   * Dummy method
   */
  function xmlbody() {
    return $this->jsonbody();
  }

  /**
   * Handler to execute web service
   */
  function handlews($servicetag) {
      
	$starttime = microtime(true);
	
    // Prevent caching - important for direct browser
    // ajax style json calls
    header("Cache-Control: no-store, no-cache, must-revalidate");
    header("Pragma: no-cache");
      
    $this->set_servicetag($servicetag);
    
      try {
        $this->verify_caller();
        $result = $this->jsonbody();
      }
      catch(Exception $e) {
        $this->error($e->getMessage());
        $result = false;
      }

      // Ensure the content type indicates javascript
      header ("Content-type: text/plain");
      $json = new Services_JSON();
      $out = new stdClass;
      if($result !== false) {
        // If user has set the $result variable on the class, use that
        // as the whole response rather than the returned value.
        // this allows child class to override the whole response if 
        // desired.
        if(isset($this->result))
          $out = $this->result;
        else {
          $out->status = "ok";
          $out->result = $result;
        }
      }
      else {
        $out->status = "failed";
        if(isset($this->message)) {
          $out->message = $this->message;
        }
      }
      
      // Set default outputs
      $out->service = $servicetag;
      $out->servertime = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
      $servicetime = 1000*(microtime(true)-$starttime);
      $out->servicetime = number_format($servicetime, 2, '.', '');
      $out->servername = $_SERVER ['SERVER_NAME'];     
      $out->ipaddr = $_SERVER['REMOTE_ADDR'].'<>'.$_SERVER['SERVER_ADDR'];
      
      // Because JSON encode sometimes emits warnings in the middle of output!
      error_reporting(0);
      echo $json->encode($out);
  }
}
?>
