<?
/*
 * Wrappers for db access using PDO
 */
require_once "settings.php";
require_once "utils.inc.php";

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

      dbg("SQL: $sql (".implode(",",$parameters).")");
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
?>
