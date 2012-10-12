<?php
// this is required of all facebook apps

require_once 'healthbook.inc.php';
//require_once "topics.inc.php";



function sharebyemail($facebook,$u,$t){
$my = "{$t->getFirstName()} {$t->getLastName()}";
	$tmcid = $t->mcid;
	$mcid = $u->mcid;
	$ad = $t->appliance;
	$user = $u->fbid;


	if($tmcid==0)
	{
		$dash = dashboard($mcid);
		$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Share by Email</fb:title>
$dash
  <fb:explanation>
    $my -- has no MedCommons Account</fb:message>
</fb:explanation>
</fb:fbml>
XXX;
		return $markup;
}
$hurlimg = "<img src='".$GLOBALS['medcommons_images']."/hurl.png"."' alt='hurlimage' />";
dbg("appliance = $ad");
$hurl2 =$t->authorize($ad.$tmcid,$u);
dbg("hurl = $hurl2");
$eccr = $t->authorize($ad.$tmcid."/eccr",$u);
$clip = $t->authorize($ad.$tmcid."/clip",$u);
$dash = hurl_dashboard($user,'share by email',$u);

$editcurrent = $t->authorize($ad.$tmcid."/edit",$u);
$editcurrent2 = $t->authorize($ad."router/getPHREditSession?useSchema=11&storageId=".$tmcid,$u);

$faxUrl = $t->appliance."acct/cover.php"; // ?createCover=true&accid=".$t->mcid."&no_cover_letter=true";
$shareUrl = $GLOBALS['base_url']."share_ccr.php";

$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Health URL</fb:title>
  <script>
    var fax_box = document.getElementById('fax_box');
    function show_fax_box() {
      fax_box.setStyle('display','block');
      return false;
    } 
    var track_box = document.getElementById('track_box');
    var track_result = document.getElementById('track_result');
    function show_track_box() {
      track_box.setStyle('display','block');
      return false;
    } 
    function share_ccr() {
      track_box.setStyle('display','none');
      var ajax = new Ajax();
      ajax.responseType = Ajax.FBML;
      ajax.ondone = function(data) {
          track_result.setStyle('display','block');
          track_result.setInnerFBML(data);
      }
      ajax.post('$shareUrl',{email:document.getElementById('email').getValue()});
    }
    function share_hide() {
      track_result.setStyle('display','none');
      document.getElementById('email').setValue('');
      track_box.setStyle('display','block');
    } 
  </script>
  $dash
  <div class='explanation_note' style='color: #333;'>
  <fb:explanation>
    <fb:message>Send a Direct Link to $my's HealthURL</fb:message>
     <div id='track_box' style='background: white; height: 30px; padding: 10px; margin: 2px 2px 15px 2px; '>
          <form>
          <b>Email</b> &nbsp;  <input type='text' id='email' name='email' value='' style='font-size: 11px; width: 16em;'/>
          &nbsp;
          <input type='button' class='inputbutton' name='send' value='Share!' onclick='share_ccr();'/>
        </form>
      </div>
      <div id='track_result' style='display: none; background-color: #fff9d7; border: solid 1px #e2c822; padding: 5px; margin: 5px 0px;'>
        
      </div>
    </p>
</fb:explanation>
  
  </div>
</div></div>
</fb:fbml>
XXX;
return $markup;
}

//**start here
// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start
list($u,$t) = mustloadtarget($facebook,$user);
  $markup = sharebyemail($facebook,$u,$t)  ;

echo $markup;
?>
