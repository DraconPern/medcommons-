<?
require_once "utils.inc.php";
require_once "alib.inc.php";
require_once "template.inc.php";
require_once "login.inc.php";

$info = get_validated_account_info();
if(!$info) 
    throw new Exception("You must be logged in to access this page.");
    
if(!$info->practice) 
    throw new Exception("You must be a member of a group to access this page.");

$gwUrl = allocate_gateway($info->accid);

// Create a new long-lived auth token based on the current one
$auth = get_authentication_token($info->accid);

$startURL = "$gwUrl/ddl/pollgroup?auth=".$auth;

$content ="<h2>DDL Poller</h2>
  <p>The DDL poller is a specially configured DDL service which automatically downloads
     new DICOM linked to patient orders as it appears in your patient list.</p>
 <br/>
  <p>
  <a href='$startURL'>Click Here to Start DDL Poller</a>
  </p>
";
  
echo template("base.tpl.php")->set("title","DDL Poller")->set("content",$content)->fetch();

/* FUTURE:  Put DDL configuration / settings here

        var dicomSettings = $('enableDicomSettings');
        connect(dicomSettings,'onclick',function(evt) {
            if(dicomSettings.checked) 
                blindDown('dicomSettings', {duration:0.3});
            else
                blindUp('dicomSettings', {duration:0.3});
        });
  
  
  
      <h4>Workstation / PACS Connection (Optional) <img id='dicomInstructionsButton' src='images/help.png'/></h4>
      <p id='dicomInstructions' class='instr'>
         Imaging studies can be automatically forwarded to your workstation or PACS as they arrive in 
         the Dropbox. Check the setup screen of your workstation or ask the PACS 
         administrator for these three standard identifiers.  <a href='http://dicomondemand.com/instructions/#DICOM_Networking_6789281184393' target='help'>More Instructions</a>.
      </p>
      <hr/>
      <? $enableDicom = isset(Template::$fields->enableDicomSettings)&&Template::$fields->enableDicomSettings;?>
      <div style='margin-left: 2em; margin-bottom: 15px;'>
          <input type='checkbox' id='enableDicomSettings' name='enableDicomSettings' 
              <?if($enableDicom):?>checked='true'<?endif;?> /> 
              Check to configure real time delivery of DICOM to your Workstation or PACS
          <table border='0' id='dicomSettings' <?if(!$enableDicom):?>style='display: none;'<?endif;?> >
            <tr>
                <th><label for='dicomAeTitle'>DICOM AETITLE</label></th>
                <td><input type='text' name='dicomAeTitle' class='infield' value='<?=field('dicomAeTitle')?>'/></td>
                <td class='error'><?=error_msg()?>
            </tr>
            <tr>
                <th><label for='dicomIpAddress'>DICOM IP Address</label></th>
                <td><input type='text' name='dicomIpAddress' class='infield' value='<?=field('dicomIpAddress')?>'/></td>
                <td class='error'><?=error_msg()?>
            </tr>
            <tr>
                <th><label for='dicomPort'>DICOM Port</label></th>
                <td><input type='text' name='dicomPort' class='infield' value='<?=field('dicomPort')?>'/></td>
                <td class='error'><?=error_msg()?>
            </tr>
          </table>
      </div>
 */