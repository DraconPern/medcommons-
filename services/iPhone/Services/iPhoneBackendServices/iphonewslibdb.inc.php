<?php

require_once "JSON.php";
require_once "dbcreds.inc.php";

/*
 * Wrappers for db access using PDO
 */

class DB {
    
  static $pdo = null;

  public function __construct() {
    $this->connect();
  }

  public function connect() {
    global $CENTRAL_PDO, $CENTRAL_USER, $CENTRAL_PASS, $DB_SETTINGS;
    if(DB::$pdo === null) {
      DB::$pdo = new PDO($CENTRAL_PDO, $CENTRAL_USER, $CENTRAL_PASS, $DB_SETTINGS);
      DB::$pdo->setAttribute (PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
    }
    return DB::$pdo; 
  }
  
   /**
    * Executes the given insert / update, throwing an exception if there is
    * any kind of failure.
    *
    * @param sql - sql string containing question marks (?) for bind parameters
    * @param params - optional array of parameters, one for each ? in the sql
    *
    * @throws Exception - for all database failures
    * @return - the id of inserted row (if any)
    */
  public function execute($sql, $params = array()) {
    try {
      DB::$pdo = $this->connect();

      dbg("SQL: $sql (".implode(",",$params).")");

      $s = DB::$pdo->prepare($sql);
      if(!$s) {
        throw new Exception("Failed to prepare sql [$sql]");
      }
      
      if(!$s->execute($params)) {
        throw new Exception("Failed to execute sql [$sql] with params (".var_dump($params).")");
      }
      return DB::$pdo->lastInsertId();
    }
    catch(PDOException $ex) { // catch necessary because PDOException does not extend Exception
      throw new Exception("Database statement failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo()."[sql=$sql]");
    }
  }

  /**
   * Executes the given sql, binding the given parameters if passed.
   * Returns an array of PHP Objects containing the data returned.
   *
   * @param int $limit    max number of rows to return.  Does *not* add LIMIT
   *                      to SQL so you probably want to add that to the SQL
   *                      passed in if many rows may be returned.
   * 
   * @throws Exception - for all database failures
   * @return array of objects, one for each row
   */
  public function query($sql, $parameters=array(), $limit=-1) {
    try {
      DB::$pdo = $this->connect();
      $s = DB::$pdo->prepare($sql);
      if(!$s)
       throw new Exception("query $sql failed with Error Info: ".DB::$pdo->errorInfo());

     // dbg("SQL: $sql (".implode(",",$parameters).")");
      /*
      $index = 1;
      foreach($parameters as $p) {
        // NOTE: do NOT bind $p, it's bound by reference
        // you will lose several hours of your life figuring out
        // why it doesn't work
        $s->bindParam($index,$parameters[$index-1]);
        $index++;
      } */

      $results = array();
      $count = 0;
      if($s && $s->execute($parameters)) {
        while($r = $s->fetch(PDO::FETCH_OBJ)) {
          $results[]=$r;
          $count++;
          if($limit >=0 && $count>$limit)
              break;
        }
        $s->closeCursor();
      }
      else 
       throw new Exception("query $sql failed with Error Info: ".DB::$pdo->errorInfo());
      
      return $results;
    }
    catch(PDOException $ex) { // catch necessary because PDOException does not extend Exception
      throw new Exception("Database statement failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo()."[sql=$sql]");
    }
  }

  /**
   * Executes the given sql, binding the given parameters if passed.
   * Returns the first row returned in the form of an object or
   * null if no rows returned from query.
   *
   * @throws Exception - for all database failures
   * @return array of objects, one for each row
   */
  public function first_row($sql, $params=array()) {
    $result = $this->query($sql,$params,1);
    if(count($result)<1)
      return null;
    else
      return $result[0];
  }
  
  /**
   * Executes the given sql, binding the given parameters if passed.
   * Returns the first column in the first row returned.
   *
   * @throws Exception - for all database failures
   * @return array of objects, one for each row
   */
  public function first_column($sql, $params=array()) {
    $result = $this->first_row($sql,$params);
    if(!$result)
      return null;
    else {
      $vars = get_object_vars($result);
      if(!$vars)
        return null;
        
      foreach($vars as $key => $value)
          return $result->$key;
    }
      
  }
  
   

  function begin_tx() {
    try {
      DB::$pdo = $this->connect();
      DB::$pdo->beginTransaction();
    }
    catch(PDOException $ex) {
      error_log("begin_tx failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo());
      throw new Exception("Database begin_tx failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo());
    }
  }

  function commit() {
    try {
      DB::$pdo = $this->connect();
      DB::$pdo->commit();
    }
    catch(PDOException $ex) {
      error_log("commit failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo());
      throw new Exception("Database statement failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo());
    }
  }

  function rollback() {
    try {
      DB::$pdo = $this->connect();
      DB::$pdo->rollback();
    }
    catch(PDOException $ex) {
      error_log("rollback failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo());
      throw new Exception("Database statement failed: ".$ex->getMessage()." Error Info: ".DB::$pdo->errorInfo());
    }
  }

  public static function get() {
    return new DB();
  }
}


// rest web service - outerframework

class XMLFragment {
    public $xml = null;
    
    function __construct($xml) {
        $this->xml = $xml;
    }
    
    function __toString() {
        return $this->xml;
    }
}

abstract class restws {

  /* Set to true in unit tests, prevents exit */
  public $test = false;

    private $outbuf;
    private $servicetag;

    function set_servicetag ($s) { $this->servicetag = $s;} // sets outer tag

    function cleanreq($fieldname)
    {
        // take an input field from the command line or POST
        // and clean it up before going any further
        if (!isset($_REQUEST[$fieldname])) return false; //wld 07sep06 - tough checking
        $value = $_REQUEST[$fieldname];
        $value = htmlspecialchars($value);
        return $value;
    }

    abstract function xmlbody ();
    
    function xmlreply ()
    {
        // generate headers
        $mimetype = 'text/xml';
        $charset = 'ISO-8859-1';
        if(!$this->test)
          header("Content-type: $mimetype; charset=$charset");
        echo ('<?xml version="1.0" ?>'."\n");
        echo $this->outbuf; // this is where we can trace
    }

  function xm($s) { 
      if($s instanceof XMLFragment)
          $this->outbuf.= $s->xml;
      else
          $this->outbuf.= $s;
  }

  /**
   * Escape xml entities
   */
  function xmlentities($string) {
     return str_replace ( array ( '&', '"', "'", '<', '>' ), array ( '&amp;' , '&quot;', '&apos;' , '&lt;' , '&gt;' ), $string );
  }
    
  function xmnest($tag,$val) {
    return new XMLFragment("<$tag>$val</$tag>");
  }
  
  /**
   * Convenience method to format XML tag.  This method
   * actually returns an XMLFragment object which can be treated
   * as a string because PHP implicitly will cast it to a string.
   * <p>
   * If the argument is a raw string then it will be escaped.
   * However if it's an XMLFragment it will be left unescaped.
   * To make an xml field with multiple children, separate
   * children with commas, like so:
   * <p>
   *   xmfield("foo", xmfield("bar","tree"), xmfield("cat","dog"))
   * <p>
   * If you want *no* escaping of children, use xmnest() instead.
   * 
   * @param String $tag name of tag
   * @param String  $val contents of tag, will be escaped
   * @return XML fragment for formatted tag
   */
  function xmfield($tag) {
      
    $out = "";
      
    $numargs = func_num_args();
    for($i=1; $i<$numargs; ++$i) {
        
        $val = func_get_arg($i);
        
        // just returns a string, must go thru xm() to be seen
        if($val === null) {
            // output nothing
        }
        else
        if($val instanceof XMLFragment) 
            $out .= $val;
        else 
            $out .= $this->xmlentities($val);
    }
    return new XMLFragment("<$tag>$out</$tag>");
  }

    //
    //outer frame of XML document response is implemented by
    //   calling xmltop {calls to xm}  calling xmlend()
    //
    function xmltop()
    {
        $this->outbuf="";
        $this->xm("<".$this->servicetag.">\n");//outer level
        $srva = $_SERVER['SERVER_ADDR'];
        $srvp = $_SERVER['SERVER_PORT'];
        $gmt = gmstrftime("%b %d %Y %H:%M:%S");
        $uri = htmlspecialchars($_SERVER ['REQUEST_URI']);
        $this->xm("<details>$srva:$srvp $gmt GMT</details>");
    //    $this->xm("<referer>".htmlspecialchars($_SERVER ['HTTP_REFERER'])."</referer>\n");
        $this->xm("<requesturi>\n".$uri."</requesturi>\n");
    }

    function xmlend( $xml_status)
    {
        $this->xm("<summary_status>".$xml_status."</summary_status>\n");
        $this->xm("</".$this->servicetag.">\n");//outer level
        $this->xmlreply(); // show its all good

    if($this->test)
      throw new Exception("exit with status $xml_status");
    else
      exit;
    }

    function handlews($servicetag)
    {

        $this->set_servicetag($servicetag);
        $this->xmltop();
        $this->xmlbody();
        $this->xmlend("success");

    }
}


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
