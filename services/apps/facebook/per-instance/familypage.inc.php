<?php // vim: ts=4

function family_page($facebook,$user, $familyfbid)
{
	// display the family at $familyfbid

	function  siblings($user,$facebook,$familyfbid)
	{
		function careteam_member ($user,$r)
		{

			$appUrl = $GLOBALS['app_url'];
			$remove1= <<<DIALOG

			<fb:dialog id="removemember_dialog{$r->userfbid}" cancel_button=1>
			<fb:dialog-title>Remove Yourselft from Care Team</fb:dialog-title>
			<fb:dialog-content><form >Do you really want to remove yourself from this Care Team?</form></fb:dialog-content>
			<fb:dialog-button type="button" value="Yes" href="ct.php?o=r&id=$r->familyfbid" />
	</fb:dialog>
DIALOG;

			$remove2= <<<DIALOG
			<a class=tinylink href="#"  clicktoshowdialog="removemember_dialog{$r->userfbid}" ><img src='http://fb01.medcommons.net/facebook/000/images/icon_deletelink.gif' alt=missingimg /></a>
DIALOG;

			$appname = $GLOBALS['healthbook_application_name']; $time = strftime('%D',$r->accepttime);
			$remlink ='';// $power?" $remove2 ":' ';
			$blurb = "
			<div class='smallwallcontainer'>
			<img src='${appUrl}images/speech.png'/>&nbsp;  <fb:name uid={$r->userfbid} capitalize='true' />	joined the team on $time.
			</div>
		";
			if ($r->fbid==$user)
			$rem = "<div class='smallwallcontainer'>
			<img src='${appUrl}images/speech.png'/>&nbsp;  You can <a href=ct.php?op=r&f=$r->userfbid >remove yourself</a> from the team.
			</div>"; else
			if ($r->fbid==$r->familyfbid)
			$rem = "<div class='smallwallcontainer'>
			<img src='${appUrl}images/speech.png'/>&nbsp;  You have the right to <a href=ct.php?op=r&f=$r->userfbid >remove	</a> <fb:name uid={$r->userfbid} capitalize='true' /> from the team.
			</div>";
			else $rem='';
			$wall = '';
			$walls = authorwallbr ($r->userfbid,4)	;
			foreach($walls as $u) {
				$wall .=
			"<div class='smallwallcontainer'>
				<img src='${appUrl}images/speech.png'/>&nbsp; You wrote about $u[1]: ".
			"\"{$u[2]}\" - ".strftime('%D',$u[0]).
			"</div>";
	}
	$outstr = "<div class=caregivee><table><tr><td  >$remove1 <div class=pic ><fb:profile-pic uid={$r->userfbid} /></div>
	</td><td class='wall'><div class='txt' ><fb:name uid={$r->userfbid} capitalize='true'/> </div> $blurb  $rem $wall</td></table></div>";
	return $outstr;
}
$familyname = $GLOBALS['familyname'];//familyname($familyfbid);
;
if ($user==$familyfbid)
$addlink = "<a class='tinylink' href='invite.php' >add friends</a>";
else $addlink='';


$appUrl = $GLOBALS['app_url'];
$outstr ="  
	<div style='border: 1px solid #bbb' ><div style='color:white; background:#6d84b4; font-size:1.1em; padding: .2em 0 .3em .7em; '>Siblings
	<a style='float: right; color:white; font-size:.7em; font-weight: 100;  padding: .3em .7em;' href=notify.php>contact all siblings</a></div>
	"; $counter = 0;
$q = "select * from teams t, users f
where t.teamfbid= '$familyfbid' and t.userfbid = f.fbid   "; //was f.familyfbid

$result = mysql_query($q) or die("cant  $q ".mysql_error());
while($u=mysql_fetch_object($result))
{
	$mod = $counter -  floor($counter/1)*1;
	if ($mod==0 && $counter!=0)$outstr.="<br/>";
	$outstr.= careteam_member($user,$u);
	$counter++;
}
mysql_free_result($result);
if ($user==$familyfbid)
$myblurb = "<div class=caregivee><table><tr><td><div class=pic ><fb:profile-pic uid=$user></fb:profile-pic></div>
</td><td class='wall'>
<div class='txt' ><fb:name useyou=false uid=$user></fb:name> &nbsp;&nbsp;</div> 
<div class='smallwallcontainer'><img src='${appUrl}images/speech.png'/>&nbsp; You are the manager of the $familyname. You can add family members(Elders) to the family. You can invite friends(Siblings) to 
help care for family members.</div>

</td></table></div>";
else
$myblurb = "<div class=caregivee><table><tr><td><div class=pic ><fb:profile-pic uid=$familyfbid></fb:profile-pic></div>
</td><td class='wall'><div class='txt' ><fb:name useyou=false uid=$familyfbid></fb:name> &nbsp;&nbsp;</div> 

<div class='smallwallcontainer'><img src='${appUrl}images/speech.png'/>&nbsp; Created and manages the $familyname.</div>

</td></table></div>";
$outstr .= $myblurb."
	<div style='padding: 0 0 .5em .7em'><a href=invite.php  >invite more siblings</a></div></div";

return $outstr;
}

function  elders_in_our_care($user,$familyfbid)
{

	function elder_blurb ($user, $r,$candelete,$hurlstuff)
	{

		$appname = $GLOBALS['healthbook_application_name'];
		$appUrl = $GLOBALS['app_url'];
		$smp = false;
		$smp= smallwallbr($r->mcid,$r->familyfbid,3);
		$smallwall='';

        if(!$r->photoUrl || ($r->photoUrl == '')) 
          $r->photoUrl = $GLOBALS['app_url'].'/images/unknown-user.png'; 

		//	}
		//	else
		foreach($smp as $u) {
			if ($u[3]==0) $img = "<img src='${appUrl}images/speech.png'/>"; else $img = "<img src='${appUrl}images/redspeech.png'/>";
			$smallwall .=
			"<div class='smallwallcontainer'>
			$img&nbsp;  <fb:name uid={$u[1]} capitalize='true'/> wrote ".
			"\"{$u[2]}\" - ".strftime('%D',$u[0]).
			"</div>";
        }
        $outstr = "<table><tr><td  >
          <div class=pic ><img src=$r->photoUrl alt='missing photo' /></div>
          </td><td class='wall'>
          <div class='txt' >
          <a href='dispatcher.php?xmcid=$r->mcid+$r->familyfbid' >$r->firstname $r->lastname</a>
          </div>
          <div class='hurl' >
          $hurlstuff
          </div>$smallwall</td></table>";


        // Each care recipient gets their own disconnect confirmation dialog
        // I'm sure there is a better way, but this seems to be the default
        // way to do it in fb examples
        $outstr .= "
          <fb:dialog id='remove_{$r->mcid}_dlg' cancel_button=1>
          <fb:dialog-title>Remove Yourself from  <fb:name uid={$r->familyfbid} possessive='true' capitalize='true'/> Care Team</fb:dialog-title>
          <fb:dialog-content><form id='my_form'>Do you really want to remove yourself from this person's Care Team?</form></fb:dialog-content>
          <fb:dialog-button type='button' value='Yes' href='ct.php?o=r&gid={$r->familyfbid}&id={$r->mcid}' />";

	return $outstr;
  }

$familyname = $GLOBALS['familyname'];//familyname($familyfbid);

$outstr =<<<XXX
	<div style='border: 1px solid #bbb' ><div style='color:white; background:#6d84b4; font-size:1.1em; padding: .2em 0 .3em .7em; '>Elders</div>
      <div class=caregivee >
XXX;

$counter = 0;
$q = "select * from  patients m where m.familyfbid = '$familyfbid' "; //ok

$result = mysql_query($q) or die("cant  $q ".mysql_error());
while($r=mysql_fetch_object($result))
{
	$mod = $counter -  floor($counter/1)*1; //change to run across
	if ($mod==0 && $counter!=0)$outstr.="</div>
<div class=caregivee >";

	// IF THIS ELDER IS THE ONE WE ARE VIEWING THEN ADD A PICTURE AND HEALTHURL LINK
	//$hurl = $r->mcid;
	//$xtra = "<a target='_new' title='open healthURL on MedCommons' href='$hurl'>view records<img src=http://www.medcommons.net/images/tx_hurl.gif alt=hurl /></a>";

	list($hurl,$xtra) = patient_hurl($r);
	if ($r->ghUrl!='') $xtra.=" <a target='_gh' href='$r->ghUrl' class=tinylink title='Open this patient on Google Health'>google</a> ";
	
	if ($r->hvUrl!='') $xtra.=" <a target='_gh' href='$r->hvUrl' class=tinylink title='Open this patient on Microsoft Health Vault'>healthvault</a> ";
	$outstr.= elder_blurb($user,$r,($user==$r->familyfbid),$xtra);
	$counter++;
}
mysql_free_result($result);

if ($counter == 0) $outstr .="<h3>The $familyname is not currently caring for any family members.</h3>";

$outstr .= <<<XXX
	</div>
	<div style='padding: 0 0 .5em .7em'><a href=settings.php  >add more elders</a>
XXX;


if ($counter == 0) $outstr .= <<<XXX
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<a  href='dispatcher.php?xmcid=-2'  >add demo elder</a>
XXX;

return '</div>'.$outstr;
}


// start of family page


$u = mustload($facebook,$familyfbid);
//$familyfbid = $u->familyfbid;
$dash = dashboard($familyfbid,false,false,$user);
//$skey_warning = "";
// new 2 column table style
$markupleft = elders_in_our_care($user,$familyfbid);
$markupright = 	 siblings($user,$facebook,$familyfbid);

$markup = <<<XXX
<fb:fbml version='1.1'><fb:title>$u->accountlabel</fb:title>
$dash
<div style='padding-top:1.5em; '>
<table >
<tr  ><td  width='50%' style='vertical-align:top; height: auto;padding-right:3px;' >$markupleft</td><td style='vertical-align:top; height: auto;padding-left:3px;'>$markupright</td></tr>
</table>
</div>
</fb:fbml>
XXX;
return $markup;
}

?>
