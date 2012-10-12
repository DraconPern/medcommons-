<?
/**
 * Base class for making access to user information generic across
 * various gadget platforms.  Specific platform implementations 
 * extend this class.   The default implementation returns
 * dummy data useful for testing only.  It should not be 
 * accessed in production.
 */
require_once "DB.inc.php";
require_once "utils.inc.php";

class GadgetContext { 
    
    public $type = "Test";
    
    public $name = "MedCommons Gadget Test Platform";
    
    public $url = "http://www.medcommons.net";
    
    /**
     * Userid as specified by gadget environment
     *
     * @var unknown_type
     */
    public $userid = null;
    
    /**
     * MCID of user, if known.
     */
    public $mcid = null;
    
    /**
     * Auth token for accessing resources, if known
     */
    public $auth = null;
    
    public function user_name($linked=true, $capitalized=true) {
        $name = "Test User";
        if($linked)
            return "<a href='#'>".htmlentities($name)."</a>";
        else
            return htmlentities($name);
    }
    
    
    /**
     * Returns an attritibute for targeting the top level window
     * with a link or form.
     */
    public function top_target() {
        return "target='_top' ";
    }
    
    public function get_layout() {
        return "gadget.tpl.php";
    }
    
    public static function get() {
        global $GADGET_CONTEXT;
        
        if(!$GADGET_CONTEXT) {
            $GADGET_CONTEXT = new GadgetContext();
            $GADGET_CONTEXT->userid = "12345";
        }
            
        if($GADGET_CONTEXT->mcid === null) {
                dbg("Looking up mcid for type = ".$GADGET_CONTEXT->type);
                $db = DB::get();
                $GADGET_CONTEXT->mcid = $db->first_column("select mcid 
                                                           from external_users eu, 
                                                           identity_providers idp
                                                           where eu.provider_id = idp.id
                                                           and   idp.source_id = ? 
                                                           and   eu.username = ?", array($GADGET_CONTEXT->type, $GADGET_CONTEXT->userid));
                
                // TODO: we should really be creating and linking an auth token
                // when the gadget is connected .... but this will do for now
                $GADGET_CONTEXT->auth = $db->first_column("select at_token from authentication_token where at_account_id = ?",
                                                          array($GADGET_CONTEXT->mcid));
        }
        
        return $GADGET_CONTEXT;
    }
}



/**************************************************************************
 * Google Gadget Support
 */
class GoogleContext extends GadgetContext {
    
    public function __construct() {
        $this->type = "GOOGLE";
        $this->name = "iGoogle";
    }
    
    public function user_name($linked=true, $capitalized=true) {
        return 'Google User';
    } 
    
    public function get_layout() {
        return "igoogle_layout.tpl.php";
    }
}
    
// Autodetect context
if(isset($_GET['opensocial_viewer_id'])) {
    
    dbg("opensocial_viewer_id=".$_GET['opensocial_viewer_id']);
    $GADGET_CONTEXT = new GoogleContext();
    $GADGET_CONTEXT->userid = $_GET['opensocial_viewer_id'];
    $GADGET_CONTEXT->type = "GOOGLE";
    $GADGET_CONTEXT->name = "iGoogle";
    dbg("Gadget context = ".$GADGET_CONTEXT->name);
}


if(isset($_GET['mc_gadget_ctx']) && ($_GET['mc_gadget_ctx']=='GOOGLE')) {
    $GADGET_CONTEXT = new GoogleContext();
    $GADGET_CONTEXT->type = "GOOGLE";
    $GADGET_CONTEXT->name = "iGoogle";
}

/**************************************************************************
 * Yahoo Gadget Support
 */
 class YahooContext extends GadgetContext {
    
    public function __construct() {
        dbg("Creating yahoo gadget context");
        $this->type = "YAHOO";
        $this->name = "Yahoo!";
    }
    
    /**
     * Yahoo gadgets automatically target the top window,
     * and are not allowed to specify _top
     */
    public function top_target() {
        return "";
    }
    
    public function user_name($linked=true, $capitalized=true) {
        return '<yml:name uid="viewer" linked="'.($linked?'true':'false').'" capitalize="'.($capitalized?'true':'false').'"/>';
    } 
}
    
// Autodetect context
if(isset($_POST['yap_viewer_guid']) && isset($_POST['yap_consumer_key'])) {
    $GADGET_CONTEXT = new YahooContext();
    $GADGET_CONTEXT->userid = $_POST['yap_viewer_guid'];
    dbg("Detected  gadget context type = ".$GADGET_CONTEXT->type);
}


if(isset($_GET['mc_gadget_ctx']) && ($_GET['mc_gadget_ctx']=='YAHOO')) {
    $GADGET_CONTEXT = new YahooContext();
}

if(isset($_GET['test']) && $_GET['test']=='true') {
    $google = new GoogleContext();
    echo $google->name;
}

?>