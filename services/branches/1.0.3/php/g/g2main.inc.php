<?php
//?handler=refhandler
//
//function newest_instance()
//{
//	$hospitals =
//	dosql ("select * from aHospitals order by ind desc limit 1");
//	if (!($hospital=mysql_fetch_object($hospitals))) die ("No hosptials!");
//
//	return array($hospital->ind);
//}
//function favorite_instance()
//{
//	$out = '<ul>';
//	$hospitals = dosql ("select * from aHospitals   where ind='4'   ");
//
//	while ($hospital =  mysql_fetch_object($hospitals))
//	{
//		$h = $hospital->ind;
//		$v = $hospital->indprogram;
//		$vn = $hospital->program;
//		$out.="<li>$h - <a href='?home&h=$h' >{$hospital->hospital} - $vn</a></li>";
//	}
//
//	$out .='</ul>';
//	return $out;
//
//
//}
//
//function recent_instances()
//{
//	$out = '<ul>';
//	$hospitals =
//	dosql ("select * from aHospitals order by ind desc limit 10");
//	while ($hospital =  mysql_fetch_object($hospitals))
//	{
//		$h = $hospital->ind;
//		$v = $hospital->indprogram;
//		$vn = $hospital->program;
//		$out.="<li>$h - <a href='?home&h=$h' >{$hospital->hospital} - $vn</a></li>";
//	}
//
//	$out .='</ul>';
//	return $out;
//
//
//}
//function testmenu()
//{
//	/**
//	 * **** put something up on an intro menu
//	 */
//
//	$data = file_get_contents("htm/gtest.htm");
//	if (isset($_REQUEST['menu']))
//	{ $h = $_REQUEST['h']; } else
//	list($h) = newest_instance(); // use the newest
//	
//
//
//
//	list($hn,$hlogo,$progind,$vn,$servicename,$serviceind) = ctx($h);
//	
//	
//	$recents = recent_instances();
//	$favorite = favorite_instance();
//	$groupadmins = listofproviderlinkswithrole('admin',$h);
//	
//	
//	
//	
//	$adminb = trim("/acct/g.php?home&h=$h");
//	$refphysb =trim("/acct/g.php?home&h=$h");
//	$refconb = trim("/acct/g.php?home&h=$h");
//	
//	
//	$adminh = urlencode("/acct/g.php?home&h=$h");
//	$refphysh = urlencode("/acct/g.php?home&h=$h");
//	$refconh = urlencode ("/acct/g.php?home&h=$h");
//	
//	switch ($progind)
//	{
//		case '1' : {
//			$tem=<<<XXX
//			
//	<p>Template 1 - Incoming CD Dashboard</p>
//	<p>Allows the public to upload to a specific Group owned by a Practice</p>
//	<h5>Actors</h5>
//
//
//	<p>When you login thru MedCommons (future), you will be taken automatically to your correct portal page. Until then, pick your poison:</p>
//	<ul>
//	<li>Admin [<a href='$adminb'>bypass</a>]</li>
//	
//	<li>Random User Sees Upload Form <a href='?home&h=$h'/>page</a></li>
//	</ul>
//	</div>			
//			
//			
//			
//XXX;
//break;
//		}
//		
//	case '2' : {
//			$tem=<<<XXX
//			<p>Template 2 - Referring Physician Portal - Open</p>
//
//	<p>Allows any referring physician to send CDs to any consultant listed in the Hospital Directory</p>
//	<h5>Actors</h5>
//
//
//	<ul>
//	<li>Admin [<a href='$adminb'>bypass</a>]</li>
//	<li>Consultant [<a href='$refconb'>bypass</a>]</li>
//	
//	<li>Random Users including Referring Physician  Sees Hospital Directory on <a href='?home&h=$h'/>page</a></li>
//	</ul>
//	</div>	
//XXX;
//break;}
//
//	case '3' : {
//				$tem=<<<XXX
//
//	<div>
//	<p>Template 3 - Referring Physician Portal - Restricted</p>
//
//	<p>Allows a restricted group of referring physicians to send CDs to any consultant listed in the Hospital Directory</p>
//	<h5>Actors</h5>
//
//
//	<ul>
//	<li>Admin [<a href='$adminb'>bypass</a>]</li>
//	<li>Consultant [<a href='$refconb'>bypass</a>]</li>
//	<li>Referring Physician  [<a href='$refphysb'>bypass</a>]</li>
//	<li>Random User Sees Eye Candy <a href='?handler=eyecandypage&h=$h'/>page</a></li>
//	
//	</ul>
//	</div>
//				
//XXX;
//break;}
//
//		case '4' : {
//				$tem=<<<XXX
//
//	<div>
//	<p>Template 4 - Sports Portal</p>
//
//	<p>Allows an open group of Trainers and other Team Professionals to gather Radiology for the Team's Players <a href=?sports>Imaging Facility Directory</a></p>
//	<h5>Actors</h5>
//
//
//	<ul>
//	<li>Admin/ATC [<a href='$adminb'>bypass</a>]</li>
//	<li>Consultant/Specialist [<a href='$refconb'>bypass</a>]</li>
//	<li>Random Uploader Sees Imaging Facilities Directory <a href='?sports'/>page</a></li>
//	
//	</ul>
//	</div>
//				
//XXX;
//break;}
//
//	default : {die ("Bad template in gmain"); }
//	}
//	$menu =<<<XXX
//	<h2>Setup a New Hospital or Practice</h2>
//	<p>Setup a <a href='?handler=prppage&h=$h'/>New Practice </a> - also sets you up as Admin,makes a MedCommons Group, etc</p>
//	<h2>Test Settings</h2>
//	<p>Practice/Hospital: <b>$hn</b><br/>Program <b>$vn</b><br/>[change down below]<br/></p>
//	<p>Group Administrators: $groupadmins</p>
//	<p>
//$tem
//	</p>
//	<h2>Practice/Hospitals in Operation</h2>
//	<p>If you do nothing else you are running as Practice/Hospital: $hn, Program: $vn</p>
//	
//	<p>Your favorite instance is:</p>
//	$favorite
//	
//	<p>You can decide which hospital to show for testing, here are the most recent:</p>
//	$recents
//XXX;
//
//	$menu = str_replace('$$$body$$$',$menu,$data); // replace body of template
//
//	echo $menu;
//}

function testmenu()
{
	echo "deprecated";
}

?>