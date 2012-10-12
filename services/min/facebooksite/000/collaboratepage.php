<?php

function collaboration_page($facebook,$user)
{
	function  care_wall ($user,$facebook,$mcid,$u,$t)
	{ // return fbml wall

		function build_wallpost_entry($user,$authorfbid,$time,$msg,$dbrecid)
		{
			if ($user==$authorfbid)
			{
				$remove= <<<DIALOG
				<fb:dialog id="removewallpost_dialog{$time}{$user}{$authorfbid}" cancel_button=1>
				<fb:dialog-title>Remove Post from Care Wall</fb:dialog-title>
				<fb:dialog-content><form >Do you really want to remove this post from the Care Wall?</form></fb:dialog-content>
				<fb:dialog-button type="button" value="Yes" href="carewall.php?removepost=$time&id=$dbrecid" />
				</fb:dialog>
				<a class=tinylink href='#' clicktoshowdialog="removewallpost_dialog{$time}{$user}{$authorfbid}" >remove this post</a>
DIALOG;
			}
			else $remove='';
			if ($authorfbid!=0&&$authorfbid!=11)
			{
				return <<<XXX
				<fb:wallpost linked=false uid="$authorfbid" t="$time" >
				$msg
				$remove
  		</fb:wallpost>
XXX;
			}
			else // these are system messages from medcommons
			return <<<XXX
			<fb:wallpost linked=false uid="1107682260" t="$time" >

			$msg
</fb:wallpost>
XXX;
		}
		$appname = $GLOBALS['healthbook_application_name'];

		$my = "{$t->getFirstName()} {$t->getLastName()}'s";
		$cursor = 0; // will move thru merge with activitylog
		$outstr="<fb:explanation><fb:message>$my CareWall</fb:message>
		<p>Access to this  wall is restricted to CareTeam members. &nbsp;<a class=tinylink href=carewall.php>write</a> to $my CareWall</p>
		<div style='font-size:10px; '> <fb:wall>";

		try {      $maxsolellolines = 5; // bill - inefficient but effective way of keeping solleo to

		$sess = array();
		/*if(is_object($t->getOAuthAPI())) {
		 $sessions  = $t->getOAuthAPI()->get_activity($t->mcid);
		 foreach($sessions as $s) // turn into a normal array
		 if ($maxsolellolines>0) {$sess[]=$s; $maxsolellolines-=1;}
		 }
		 */
		$logentrycount = count($sess);
		$q = "select * from  carewalls where wallmcid  = '$mcid' and wallfbid='$u->familyfbid' order by time desc limit 5 ";
		$result = mysql_query($q) or die("cant  $q ".mysql_error());
		while($u=mysql_fetch_object($result))
		{
			$up = "<h4>These activities occured in your MedCommons Account</h4>";
			if ($maxsolellolines==0) {
				$up.="<span class=moresolello><a href='healthurl.php?o=a'> view more MedCommons Activities</a>";
			}

			$up.=" <ul>";
			$any=false; $firsttime='****';
			// get any activity log entries that are olderr than this entry
			while (($cursor<$logentrycount)&&
			($sess[$cursor]->beginTimeMs/1000 >=$u->time))
			{
				if (!$any) $firsttime = $sess[$cursor]->beginTimeMs/1000;
				$s = $sess[$cursor++];
				$any=true;
				$time = strftime("%m-%d-%Y %H:%M:%S",$s->beginTimeMs/1000);
				$up.="<li>".
				$time." ".$s->summary->description.'-'.$s->summary->sourceAccount->id.'-'.$s->summary->sourceAccount->idType."</li>
                                        ";
			}
			$up.="
                                </ul>
                                ";
			if (isset($GLOBALS['facebook_userid'])) $appliance_userid = $GLOBALS['facebook_userid']; else $appliance_userid=6471872199;
			if ($any) $outstr.=build_wallpost_entry ($user,0,$firsttime,$up,$u->id);
			$outstr .= build_wallpost_entry($user, $u->authorfbid,$u->time,$u->msg,$u->id);

		}

		// get any aremaining y log entries that are olderr than this entry
		$up = "<ul>";
		$any=false;

		while (($cursor<$logentrycount))
		{
			$s = $sess[$cursor++];
			$any=true;
			$time = $s->beginTimeMs/1000;
			$up.="<li>".
			$time." ".$s->summary->description.'-'.$s->summary->sourceAccount->id.'-'.$s->summary->sourceAccount->idType."</li>
                                ";
		}
		$up.="
                        </ul>
                        ";
		if ($any) $outstr.=build_wallpost_entry ($user,11,$s->beginTimeMs/1000,$up,0);
		}
		catch(Exception $e) {
			error_log("Error rendering care wall for user $user: ",$e->getMessage());
			$outstr.="<p>A problem was experienced loading your recent activity.  You may need
			to <a href='settings.php'>reconnect</a> your $appname account to it's MedCommons Appliance.</p>";
		}

		if(isset($result) && $result)
		mysql_free_result($result);

		$outstr.="</fb:wall></div></fb:explanation>";
		return $outstr;
	}



	$appname = $GLOBALS['healthbook_application_name'];
	list($u,$t) = mustloadtarget($facebook, $user);

	$targetmcid = $u->targetmcid;
	$mymcid = $u->mcid;

	$familyfbid = $u->familyfbid;
	$appurl = $t? $t->appliance : "";
	$dash = hurl_dashboard($user,'collaborate');
	$title="Collaboration";
	$wall =     care_wall($user, $facebook, $u->targetmcid,$u,$t);
	$app = $GLOBALS['facebook_application_url']."elder.php?xmcid=$u->targetmcid+$familyfbid";
	$my = "{$t->getFirstName()} {$t->getLastName()}'s";
	$markup = <<<XXX
	$wall
	<div style='padding:5px;'><fb:board xid="$u->targetmcid"
	canpost="true"          candelete="false"          canmark="false"
	cancreatetopic="true"          numtopics="2"          returnurl="$app">
	<fb:title>$my Discussion Board</fb:title>
</fb:board>
</div>
XXX;

	$markup = <<<XXX
	<fb:fbml version='1.1'><fb:title>$title</fb:title>
	$dash
	$markup

</fb:fbml>
XXX;
	return $markup;
}
