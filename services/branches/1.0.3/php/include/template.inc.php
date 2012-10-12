<?php
require_once "urls.inc.php";
require_once "settings.php";

/**
 * Convenience function for creating templates
 */
function template($src) {
  return new Template($src);
} 

/**
 * Helps to hold parent child hierarchy when template sections are nested inside
 * each other.
 */
class NestedSections {
    
    /**
     * A list of child sections
     */
    var $sections = array();
    
    function render() {
        echo $this->__toString();
    }
    
    function __toString() {
        return join("",$this->sections);
    }
}

/**
 * A class for helping to create reusable views that can be
 * rendered by different sections of code (ala MVC style design).
 * 
 * Typical usage:
 * 
 *   $t = template("foo.tpl.php")
 *        ->set("bar","here is the content for bar")
 *        ->set("fubar", template("fubar")->set("title","<h1>Fubar!</h1>"));
 *
 *   echo $t->fetch();
 * 
 * Variables that are set in templates may be used directly by the template file:
 * 
 *     if($bar == 'foo') ...
 * 
 * Templates also support inheritance for use with layouts by declaring
 * overidable sections.  Eg: in a template:
 * 
 *    <?section("head")?>
 *    <p>This content can be overridden by a child</p>
 *    <?end_section("head")?>
 * 
 * A child template:
 *    
 *     <?$template->extend("parent.tpl.php");?>
 * 
 *    <?section("head")?>
 *    <p>My content will go in the head instead!</p>
 *    <?end_section("head")?>
 * 
 * Nested sections are supported - a child template may define 
 * new nested sections inside a section declared by
 * a parent template.  The hierarchy is not treated as significant
 * in matching sections together:  a child can declare a section
 * outside of any hierarchy and that will still override the parent's
 * definition of that section even though it may be deeply nested.
 * 
 * Templates are automatically looked up in the default MedCommons
 * templates path as well as in the local directory, so you can reference
 * globally available templates by their simple names.
 */
class Template {

  var $vars; /// Holds all the template variables
  
  /**
   * If set, this template is considered to "extend" the parent,
   * which means sections in the parent may be over-ridden by 
   * the child.
   */
  var $parentTemplate = false;
  
  /**
   * Constructor
   *
   * @param $file string the file name you want to load
   */
  function Template($file = null) {
    $this->file = $file;
    $this->vars = array();
    if(!Template::$errors)
        Template::$errors = new stdClass;
        
    if(!Template::$fields)
        Template::$fields = new stdClass;
        
    return $this;
  }

  /**
   * Set a template variable.
   */
  function set($name, $value) {
    $this->vars[$name] = (is_object($value) && method_exists($value,"fetch")) ? $value->fetch() : $value;
    return $this;
  }

  /**
   * Set a nested template to be evaluated at render time
   */
  function nest($name, &$t) {
    $this->vars[$name] = $t;
    return $this;
  }

  function is_set($name) {
    return isset($this->vars[$name]);
  }

  /**
   * Set and escape a template variable.
   */
  function esc($name, $value) {
    $this->vars[$name] = htmlspecialchars($value);
    return $this;
  }
  
  function extend($parent) {
      $this->parentTemplate = $parent;
  }
  
  var $sections = array();
  
  var $sectionChildren = array();
  
  /**
   * Begins a new section that may be overridden by a child
   */
  function section($name) {
      
      // Start buffering so that we eat the content from here on
      // It *may not* be rendered to the output, although
      // it *will* be evaluated.  Would be nice to make it not
      // even be evaluated if not needed, but not sure how to do that
      ob_start();
      
      // Has a child template already set a value for this section?
      if(isset($this->vars[$name])) { // yes!
          
          // Render child contents
          $this->sections[]=array(true,$name);
      }
      else
          $this->sections[]=array(false,$name);
  }
  
  function close($expectedName = false) {
      
      if(count($this->sections) == 0)
          throw new Exception("Bad nesting of sections: more close() calls than section() calls");
      
      list($overridden,$name) = array_pop($this->sections);
      
      $parentSection = false;
      if(count($this->sections)>0) {
          $parentSection = $this->sections[count($this->sections)-1][1];
      }
      
      if($expectedName && ($name != $expectedName))
          throw new Exception("Bad nesting of sections: expected close of $name but got close of $expectedName");
          
      if($overridden) {
          ob_end_clean();
          
          if($this->vars[$name] instanceof NestedSections) {
              // dbg("Found $name has nested sections");
              $this->vars[$name]->render();
          }
          else
              echo $this->vars[$name];
      }
      else
      if($this->parentTemplate) {
          // Since there is a parent template, do not render, just buffer
          $contents = ob_get_contents();
          ob_end_clean();
          
          // When sections are nested inside each other we split the parent section
          // into pieces, inserting the child inside the parent in the correct
          // position.
          if($parentSection) {
              //dbg("Splitting buffer due to nested tag '$name' in '$parentSection'");
              $parentContents = ob_get_contents();
              ob_clean();
              if(!isset($this->vars[$parentSection])) {
                  $this->vars[$parentSection] = new NestedSections();
              }
              $this->vars[$parentSection]->sections[]=$parentContents;
              $this->vars[$parentSection]->sections[]=$contents;
          }
          else {
              if(isset($this->vars[$name]) && ($this->vars[$name] instanceof NestedSections))
                  $this->vars[$name]->sections[]=$contents;
              else
                  $this->vars[$name] = $contents;
          }
      }
      else { // No parent template - echo to output
          ob_end_flush();
      }
      
  }

  /**
   * Format the difference between the two given times
   * as an age and return the result as a string.  Times 
   * are passed in seconds since 1/1/1970 GMT.
   */
  function formatAge($itemTime, $refTime) {
    $age = $refTime - $itemTime;
    $days = floor($age / 86400);
    $hours = floor(($age - ($days * 86400)) / 3600);
    $mins = floor( ($age % 3600) / 60);

    $dateTime = ($days > 0) ? "$days days, " : "";
    $dateTime .= "$hours hrs $mins mins ago";

    if($days<1) {
      if($hours > 1) {
        $dateTime =  "$hours hours ago";
      }
      else
        $dateTime = $hours > 0 ? "$hours hour ago" : "$mins mins ago";
    }
    else
      if($days < 7) {
        $dateTime = "$days days ago";
      }
      else {
        $dateTime = htmlspecialchars(strftime('%m/%d/%y',$itemTime));
      }
    return $dateTime;
  }

  /**
   * Open, parse, and return the template file.
   *
   * @param $file string the template file name
   */
  function fetch($file = null, $pageHasNoAds = true ) {
    global $acTemplateFolder;

    if(!$file) $file = $this->file;

    $prefix = null;
    if(preg_match("/^[\.\/]+/",$file, $prefix)) {
      $this->set("relPath", $prefix[0]);
      error_log("setting prefix  ".$prefix[0]." for path ". $this->file);
    }
    else {
      $this->set("relPath", "./");
      // error_log("no prefix  for path ".$this->file);
    }

    if(!$pageHasNoAds) {
      $this->set('noAds','false'); 
    }
    else 
      $this->set('noAds','true');

    if(!isset($this->vars["errors"]) && count(get_object_vars(Template::$errors))>0)
        $this->set("errors",Template::$errors);
    
    $this->set("g",$GLOBALS);
    $this->vars["template"]=$this;
    
    if(isset($GLOBALS['use_combined_files']) && ($GLOBALS['use_combined_files']==true)) {
      $this->set("enableCombinedFiles",true);
      if(isset($GLOBALS['Acct_Combined_File_Base'])) {
        $this->set("httpUrl",rtrim($GLOBALS['Acct_Combined_File_Base'],"/"));
      }
      else
        $this->set("httpUrl",rtrim($GLOBALS['Secure_Url'],"/")."/acct");
    }
    else {
      $this->set("enableCombinedFiles",false);
      $this->set("httpUrl",rtrim($this->vars["relPath"],"/"));
    }

    $outputvars = array();
    foreach($this->vars as $key => $value) {
      if(is_object($value) && method_exists($value,"fetch") && ($key != "template")) {
        $outputvars[$key] = $value->fetch();
      }
      else
        $outputvars[$key] = $value;
    }
    
    // Always search mc_templates folder
    $resolvedFile = $file;
    if(file_exists($acTemplateFolder . $file) && !file_exists($resolvedFile)) {
      $resolvedFile = $acTemplateFolder . $file;
    }

    // Reset - otherwise may enter infinite loop when extended
    // We only want to keep going up the hierarchy if this 
    // next rendering itself sets a new parent
    $this->parentTemplate = false;
    
    Template::$current_template = $this;
    
    extract($outputvars);          // Extract the vars to local namespace
    ob_start();                    // Start output buffering
    include($resolvedFile);         // Include the file
    $contents = ob_get_contents();  // Get the contents of the buffer
    ob_end_clean();                 // End buffering and discard
    
    
    // At this point the current template has executed, but it may
    // have set a parent template that it extends.
    // In that case we evaluate the parent now
    if($this->parentTemplate) {
        return $this->fetch($this->parentTemplate);
    }
    
    return $contents;              // Return the contents
  }
  
  static function has_errors() {
      return count(get_object_vars(Template::$errors))>0;
  }
  
  static $current_template = false;
  
  /**
   * Array of invalid fields that may be populated by the 
   * renderer of a template.  Global to all templates.  
   * Used by error_msg() function, populated by verify()
   * function.
   */
  static $errors = false;
  
  static $fields = false;
  
  static $lastFieldName = false;
}

/**
 * Convenience function to begin a new block. 
 */
function section($name) {
    Template::$current_template->section($name);
}

/**
 * Convenience function to end a section. 
 * Passing name is optional, but if passed it
 * will be checked to ensure the section name
 * matches the most recently opened section.
 */
function end_section($name = false) {
    Template::$current_template->close($name);
}


/**
 * Convenience function to begin a new block. An alias
 * for section() to make api match Django's terminology.
 */
function block($name) {
    Template::$current_template->section($name);
}

/**
 * Convenience function to end a block. An alias
 * for end_section() to make api match Django's terminology.
 */
 function end_block($name = false) {
    Template::$current_template->close($name);
}

/**
 * Checks for a POST variable with given name.  If there is,
 * returns it.  Strips slashes if magic quotes enabled.
 * Registers the specified field name with the 
 * found value.
 *
 * @param String $x
 * @return POST variable named $x
 */
function post($x,$default=null) {
   $value = isset($_POST[$x]) ? $_POST[$x] : $default;
   if($value && get_magic_quotes_gpc() && !is_array($value)) {
       $value = stripslashes($value);    
   }
   Template::$fields->$x = $value;
   return $value;
}

/**
 * Returns value of specified field, as registered by prior 'post()'
 * call.
 *
 * @param String $x     field name
 * @return value of field
 */
function field($x) {
    Template::$lastFieldName = $x;
    if(isset(Template::$fields->$x))
        return htmlentities(Template::$fields->$x, ENT_QUOTES);
    return '';
}

/**
 * Checks that the requested field was provided and if so returns it.
 * If not, sets it on the given errors object.  If expression provided
 * then checked to see if matches expression.
 *
 * @param unknown_type $x
 * @param unknown_type $errors
 */
function verify($x, $fieldName = false, $pattern = false) {
   $value = post($x);  
   
   $fieldName = ($fieldName?$fieldName:ucfirst($x));
   if(!$value) {
       Template::$errors->$x = $fieldName." was not provided"; 
   }
   else
   if($pattern) {
       if(preg_match($pattern, $value) !== 1) {
           Template::$errors->$x = $fieldName." was not in correct format"; 
       }
   }
   return $value;
}

function error_msg($fieldName=false) {
   if(!$fieldName)
       $fieldName = Template::$lastFieldName;
   if(!$fieldName)
       throw new Exception("Unknown field name $fieldName specified for error message");
       
   if(isset(Template::$errors->$fieldName))  
       return Template::$errors->$fieldName;
}
/**
 * An extension to Template that provides automatic caching of
 * template contents.
 */
class CachedTemplate extends Template {
  var $cache_id;
  var $expire;
  var $cached;

  /**
     * Constructor.
     *
     * @param $cache_id string unique cache identifier
     * @param $expire int number of seconds the cache will live
     */
  function CachedTemplate($cache_id = null, $expire = 900) {
    $this->Template();
    $this->cache_id = $cache_id ? 'cache/' . md5($cache_id) : $cache_id;
    $this->expire   = $expire;
  }

  /**
     * Test to see whether the currently loaded cache_id has a valid
     * corrosponding cache file.
     */
  function is_cached() {
    if($this->cached) return true;

    // Passed a cache_id?
    if(!$this->cache_id) return false;

    // Cache file exists?
    if(!file_exists($this->cache_id)) return false;

    // Can get the time of the file?
    if(!($mtime = filemtime($this->cache_id))) return false;

    // Cache expired?
    if(($mtime + $this->expire) < time()) {
      @unlink($this->cache_id);
      return false;
    }
    else {
      /**
       * Cache the results of this is_cached() call.  Why?  So
       * we don't have to double the overhead for each template.
       * If we didn't cache, it would be hitting the file system
       * twice as much (file_exists() & filemtime() [twice each]).
       */
      $this->cached = true;
      return true;
    }
  }

  /**
     * This function returns a cached copy of a template (if it exists),
     * otherwise, it parses it as normal and caches the content.
     *
     * @param $file string the template file
     */
  function fetch_cache($file) {
    if($this->is_cached()) {
      $fp = @fopen($this->cache_id, 'r');
      $contents = fread($fp, filesize($this->cache_id));
      fclose($fp);
      return $contents;
    }
    else {
      $contents = $this->fetch($file);

      // Write the cache
      if($fp = @fopen($this->cache_id, 'w')) {
        fwrite($fp, $contents);
        fclose($fp);
      }
      else {
        die('Unable to write cache.');
      }

      return $contents;
    }
  }
}
?>
