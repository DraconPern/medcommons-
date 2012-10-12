<?
require_once "DB.inc.php";

/**
 * Support for querying and modifying patient lists for a group
 * <p>
 * Each patient list object is instantiated with the practice 
 * id and the name of a patient list to operate on.
 *
 * <p>Example:</p>
 * <code>
 *   $inbox = new PatientList(20, "inbox");
 *   $inbox->add(1013062431111407);
 *   $inbox->remove(1088448116240388);
 *   $patients = $inbox->patients();
 *   foreach($patients as $p) 
 *    echo $p->PatientIdentifier;
 * </code>
 */
class PatientList {
    
  /**
   * ID of the practice
   */
  public $practice_id;  
  
  /**
   * Name or "tag" of the patient list
   */
  public $name;
    
  function __construct($practiceId, $name) {
      $this->practice_id = $practiceId;
      $this->name = $name;
  }
  
  /**
   * Add the specified user to this patient list.
   * If user already exists in patient list then an exception will NOT be thrown.
   * @param $accid
   */
  function add($accid) {
    $db = DB::get();
    $db->execute("replace into practice_patient (pp_practice_id, pp_name, pp_accid) 
                  values (?, ?, ?)",
                  array($this->practice_id, $this->name, $accid));    
  }
  
  /**
   * Remove specified user from this patient list.
   * If user does not exist in patient list, do nothing.
   * @param $accid
   */
  function remove($accid) {
    $db = DB::get();
    $db->execute("delete from practice_patient 
                  where pp_accid = ?
                    and pp_name = ?
                    and pp_practice_id = ?", array($accid, $this->name, $this->practice_id));
  }
  
  /**
   * Returns true if this list contains the specified member
   * @param $accid
   */
  function contains($accid) {
      $db = DB::get();
      $count = $db->first_column("select count(*) from practice_patient pp
                                  where 
                                    pp.pp_name = ?
                                    and pp.pp_practice_id = ?
                                    and pp.pp_accid = ?", array($this->name, $this->practice_id, $accid));
      
      return ($count >= 1);
  }
  
  /**
   * Return user records for all users in the list
   */
  function patients() {
      $db = DB::get();
      
      return $db->query("select e.*, u.photoUrl, do.*
                         from practiceccrevents e, practice_patient pp, users u
                         LEFT JOIN dicom_order do on do.mcid = u.mcid
                         where
                             pp.pp_name = ?
                             and pp.pp_practice_id = ?
                             and e.practiceid = pp.pp_practice_id
                             and e.PatientIdentifier = pp.pp_accid
                             and e.ViewStatus  = 'Visible'
                             and u.mcid = pp.pp_accid
                         GROUP BY e.PatientGivenName, e.PatientFamilyName, e.Guid
                         ORDER BY e.CreationDateTime DESC
                             ",
                        array($this->name, $this->practice_id));
  }
}

?>
