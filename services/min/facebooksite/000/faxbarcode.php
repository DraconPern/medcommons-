<?php
// this is required of all facebook apps

require_once 'healthbook.inc.php';

function faxbarcode($facebook,$u,$t){
$my = "{$t->getFirstName()} {$t->getLastName()}";
	$tmcid = $u->targetmcid;
	$user = $u->fbid;

	if($tmcid==0)
	{
		$dash = dashboard($user);
		$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Generate Fax Bar Code Covers for $my</fb:title>
$dash
  <fb:explanation>
    <fb:message>$my -- has no MedCommons Account</fb:name></fb:message>
</fb:explanation>
</fb:fbml>
XXX;
		return $markup;
}
$hurlimg = "<img src='".$GLOBALS['medcommons_images']."/hurl.png"."' alt='hurlimage' />";



$faxUrl = $t->appliance."acct/cover.php"; // ?createCover=true&accid=".$t->mcid."&no_cover_letter=true";


		$dash = hurl_dashboard($user,'fax');
$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>Health URL</fb:title>

  $dash
  <div class='explanation_note' style='color: #333;'>
    <h3>Fax Directly Into  $my HealthURL</h3>
Print a custom coded cover sheet.
      <div id='fax_box' style='background: white; height: 140px; width: 400px; padding: 10px; margin: 2px 2px 15px 2px; '>
        <form action='$faxUrl' target='_preview' >
          <input type='hidden' name='createCover' value='true'/>
          <input type='hidden' name='accid' value='$tmcid'/>
          <input type='hidden' name='no_cover_letter' value='true'/>
          <p>Title:</p>
          <input type='text' name='title' value='' style='font-size: 11px;'/>
          <p>Note:</p>
          <textarea name='note' cols="60" rows="5"></textarea>
          <br/>
          <input type='submit' class='inputbutton' name='preview' value='Print / Preview'/>
        </form>
      </div>
      
  </div>
</div></div>
</fb:fbml>
XXX;
return $markup;
}
// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start
list ($u,$t ) = mustloadtarget($facebook,$user);
 $markup = faxbarcode($facebook,$u,$t)   
;
echo $markup;
?>
