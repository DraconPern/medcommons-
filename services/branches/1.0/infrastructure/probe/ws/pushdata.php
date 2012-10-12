<?PHP
// report status of medcommons appliance as JSON
require 'settings.php';
require_once 'utils.inc.php';
   require 'email.inc.php';
// Global pdo object
$pdo = null;

function pdo_connect() {
  global $pdo,$IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS;
  if($pdo === null) {
    $pdo = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS);
    $pdo->setAttribute (PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
  }
  if(!$pdo)
    throw new Exception("Failed to connect to database");

  return $pdo; 
}

function pdo_begin_tx() {
  global $pdo;
  try {
    $pdo = pdo_connect();
    $pdo->beginTransaction();
  }
  catch(PDOException $ex) {
    error_log("begin_tx failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
    throw new Exception("Database statement failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
  }
}

function pdo_commit() {
  global $pdo;
  try {
    $pdo = pdo_connect();
    $pdo->commit();
  }
  catch(PDOException $ex) {
    error_log("commit failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
    throw new Exception("Database commit failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
  }
}

function pdo_rollback() {
  global $pdo;
  try {
    $pdo = pdo_connect();
    $pdo->rollback();
  }
  catch(PDOException $ex) {
    error_log("rollback failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
    throw new Exception("Database rollback failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
  }
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
function pdo_execute($sql, $params = array()) {
  global $pdo;
  try {
    $pdo = pdo_connect();

    dbg("SQL: $sql");

    $s = $pdo->prepare($sql);
    if(!$s) {
      throw new Exception("Failed to prepare sql [$sql]");
    }
    
    if(!$s->execute($params)) {
      throw new Exception("Failed to execute sql [$sql] with params (".var_dump($params).")");
    }
    return $pdo->lastInsertId();
  }
  catch(PDOException $ex) {
    error_log("query $sql failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
    throw new Exception("Database statement failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo()."[sql=$sql]");
  }
}

/**
 * Executes SQL with given bind parameters, loads
 * all results and then returns the first result if there
 * is one.  If no rows returned returns null.
 */
function pdo_first_row($sql, $params) {
  $result = pdo_query($sql,$params);
  if(count($result)<1)
    return null;
  else
    return $result[0];
}

/**
 * Executes the given sql, binding the given parameters if passed.
 * Returns an array of PHP Objects containing the data returned.
 *
 * returns false upon failure. (NOTE: Does NOT throw ANY exceptions upon failure)
 */
function pdo_query($sql, $p1=null,$p2=null,$p3=null,$p4=null) {
  global $pdo;

  dbg("SQL: $sql");

  if(!is_array($p1)) {
    $parameters = array($p1);
    if($p2 != null)
      $parameters[]=$p2;
    if($p3 != null)
      $parameters[]=$p3;
    if($p4 != null)
      $parameters[]=$p4;
  }
  else
    $parameters = $p1;

  dbg("SQL: $sql");
  try {
    $pdo = pdo_connect();

    $s = $pdo->prepare($sql);
    if(!$s) {
     error_log("query $sql failed with Error Info: ".$pdo->errorInfo());
     return false;
    }

    $index = 1;
    foreach($parameters as $p) {
      // NOTE: do NOT bind $p, it's bound by reference
      // you will lose several hours of your life figuring out
      // why it doesn't work
      $s->bindParam($index,$parameters[$index-1]);
      $index++;
    }

    $results = array();
    if($s && $s->execute()) {
      while($r = $s->fetch(PDO::FETCH_OBJ)) {
        $results[]=$r;
      }
    }
    else {
     error_log("query $sql failed with Error Info: ".$pdo->errorInfo());
    }
    return $results;
  }
  catch(PDOException $ex) {
    error_log("query $sql failed: ".$ex->getMessage()." Error Info: ".$pdo->errorInfo());
    return false;
  }
  catch(Exception $ex) {
    error_log("query $sql failed: ".$ex->getMessage());
    return false;
  }
}

                 
              

// main starts here   
$op = $_REQUEST['op'];
$data = $_REQUEST['data'];
$reqlen = strlen($data);
$fn = 'pics/iphone.'.$_SERVER['REMOTE_ADDR'].'.'.time().'.data';
file_put_contents($fn,$data);
$data = "see $fn";
pdo_connect();
pdo_execute("insert into pgtable set reqtime = ?,  reqop = ?, reqlen = ?,reqdata = ?, server = ?, remote = ?, servername = ?",
		           array(time(),$op,$reqlen,$data,$_SERVER['SERVER_ADDR'],$_SERVER['REMOTE_ADDR'],$_SERVER ['SERVER_NAME']));
                 //array(time(),$_POST['op'],$_POST['data'],$_SERVER['SERVER_ADDR'],$_SERVER['REMOTE_ADDR'],$_SERVER ['SERVER_NAME']));

		           
		           
		           
/*
  $recipient = "billdonner@gmail.com,agropper@medcommons.net";
  $srvname = $_SERVER['SERVER_NAME'];

  $srva = $_SERVER['SERVER_ADDR'];
  $srvp = $_SERVER['SERVER_PORT'];
  $gmt = gmstrftime("%b %d %Y %H:%M:%S")." GMT";
  $uri = htmlspecialchars($_SERVER ['REQUEST_URI']);
  $remote = $_SERVER['REMOTE_ADDR'];


  $text = <<<EOF
The SMTP server is on $srvname ($srva:$srvp). The request was from $remote;

We will make this fancier, but your should know that a MedCommons Users iPhone delivered:

op: $op data: $data 

Think about where this email should really go

The MedCommons Team

Thank you for using MedCommons.
EOF;

  $html = <<<EOF
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv='Content-Type' content="text/html; charset=iso-8859-1" />
    <title>MedCommons $op Message Sent From $srvname at $gmt</title>
  </head>
  <body>
    <p>
    <img src='$srvname/probe/custom/logo60by60.png' />
    <br />
    <small>The SMTP server is on $srvname ($srva:$srvp).The request was from the iPhone user on $remote.</small>
    </p>
<p>The operation is $op from the iPhone user on $remote</small>
<p>The entire payload follows as three JSON blocks. All of this will be filtered and prettied up.</p>
$data
    <p>The MedCommons Team</p>

    <p>Thank you for using MedCommons. </p>

  </body>
</html>
EOF;

  $stat = send_mc_email($recipient, "$op message from MedCommons iPhone User on $srvname",
			$text, $html,
			array());
     
  if ($stat)
    $stat = "ok";
  else
    $stat =  "send mail failure: $stat";
*/
                 

?>