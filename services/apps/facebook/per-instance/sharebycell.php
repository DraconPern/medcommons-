<?php
// this is required of all facebook apps

require_once 'healthbook.inc.php';
//require_once "topics.inc.php";



function sharebycell($facebook,$u,$t){
$my = "{$t->getFirstName()} {$t->getLastName()}";
	$tmcid = $t->mcid;
	$mcid = $u->mcid;
	$ad = $t->appliance;
	$user = $u->fbid;


	if($tmcid==0)
	{
		$dash = dashboard($mcid);
		$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Share by Cell</fb:title>
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
$dash = hurl_dashboard($user,'share by cell',$u);
$editcurrent = $t->authorize($ad.$tmcid."/edit",$u);
$editcurrent2 = $t->authorize($ad."router/getPHREditSession?useSchema=11&storageId=".$tmcid,$u);

$faxUrl = $t->appliance."acct/cover.php"; // ?createCover=true&accid=".$t->mcid."&no_cover_letter=true";
$shareUrl = $GLOBALS['base_url']."share_ccr_to_phone.php";

$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Health URL</fb:title>
  <style>
    .errors { color: red; padding-left: 6px; }
    .invalidField { border: solid 2px red; }
  </style>
  <fb:js-string var="phoneError">please enter a phone number in the form XXX-XXX-XXXX</fb:js-string>
  <fb:js-string var="nameError">please enter your first and last name in these fields (required)</fb:js-string>
  <fb:js-string var="blank"></fb:js-string>
  <script>
    var track_box = document.getElementById('track_box');
    var track_result = document.getElementById('track_result');
    function el(id) {
      return document.getElementById(id);
    }
    function share_ccr() {
      var errors = false;

      el('phone').setClassName('');
      el('firstName').setClassName('');
      el('lastName').setClassName('');
      el('phoneErrors').setInnerFBML(blank);
      el('nameErrors').setInnerFBML(blank);

      if(!/^\d{3}-?\d{3}-?\d{4}$/.exec(el('phone').getValue())) {
        el('phone').setClassName('invalidField');
        el('phoneErrors').setInnerFBML(phoneError);
        errors = true;
      }

      if(el('firstName').getValue() == '') {
        el('firstName').setClassName('invalidField');
        el('nameErrors').setInnerFBML(nameError);
        errors = true;
      }
      if(el('lastName').getValue() == '') {
        el('lastName').setClassName('invalidField');
        el('nameErrors').setInnerFBML(nameError);
        errors = true;
      }

      if(errors)
        return;

      errors = false;

      var ajax = new Ajax();
      ajax.responseType = Ajax.FBML;
      ajax.ondone = function(data) {
          track_result.setStyle('display','block');
          track_result.setInnerFBML(data);
      }
      ajax.post('$shareUrl',{
                             phoneNumber:el('phone').getValue(), 
                             carrier: el('carrier').getValue(),
                             firstName: el('firstName').getValue(),
                             lastName: el('lastName').getValue()
                            });
      track_box.setStyle('display','none');
    }
    function share_hide() {
      track_result.setStyle('display','none');
      el('phone').setValue('');
      el('firstName').setValue('');
      el('lastName').setValue('');
      track_box.setStyle('display','block');
    } 
  </script>
  $dash
  <div class='explanation_note' style='color: #333;'>
    <h3>Share $my's HealthURL via Cellphone </h3>

      Share access to this HealthURL using SMS text messaging (at <a href='http://www.medcommons.net' target='new'>MedCommons.net</a>)
       
      <div id='track_box' style='background: white; height: 70px; padding: 10px; margin: 2px 2px 15px 2px; '>
          <form>
          <table>
            <tr><th>Phone</th><td><input type='text' id='phone' name='phone' value='' style='font-size: 11px; width: 16em;'/></td>
                <td id='phoneErrors' class='errors'></td></tr>
            <tr><th>Carrier</th><td><select name='carrier' id='carrier'>
                                      <option value='att'>AT&amp;T</option>
                                      <option value='vrzn'>Verizon</option>
                                      <option value='sprintpcs'>Sprint PCS</option>
                                      <option value='tmob'>T-Mobile</option>
                                    </select>
                                </td><td id='carrierErrors' class='errors'></td></tr>
            <tr><th>Name</th><td><input type='text' id='firstName' name='firstName'/> <input type='text' id='lastName' name='lastName'/></td>
                <td id='nameErrors' class='errors'> </td></tr>
            <tr>
              <td>&nbsp;</td>
              <td><input type='button' class='inputbutton' name='send' value='Send' onclick='share_ccr();'/></td>
            </tr>
          </table>
        </form>
      </div>
      <div id='track_result' style='display: none; background-color: #fff9d7; border: solid 1px #e2c822; padding: 5px; margin: 5px 0px;'>
        
      </div>
    </p>

  </div>
</div></div>
</fb:fbml>
XXX;
return $markup;
}


// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start
list($u,$t) = mustloadtarget($facebook,$user);
  $markup = sharebycell($facebook,$u,$t)  ;

echo $markup;
?>
