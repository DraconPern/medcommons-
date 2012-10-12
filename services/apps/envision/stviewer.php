<?php

require_once "simtrak.inc.php";
require_once "stview.inc.php";
function page_foot()
{
	// called from multiple spots
	global $playerind_,$jsonstuff_,$allplayers_,$tv_;

	$foot = <<<XXX
	</div>
	</div>
	</div>
	<script type="text/javascript">
	//		datax= { accounts: $allplayers_
//	};
//
//	mytabView = new YAHOO.widget.TabView('mystuff');

//Y//AHOO.example.ItemSelectHandler = function() {
// Use a LocalDataSource
//var oDS = new YAHOO.util.LocalDataSource(datax.accounts);
//oDS.responseSchema = {fields : ["name", "id"]};

// Instantiate the AutoComplete
//var oAC = new YAHOO.widget.AutoComplete("myInput", "myContainer", oDS);
//oAC.resultTypeList = false;

// Define an event handler to populate a hidden form field
// when an item gets selected
//var myHiddenField = YAHOO.util.Dom.get("myHidden");
//var myHandler = function(sType, aArgs) {
//var myAC = aArgs[0]; // reference back to the AC instance
//var elLI = aArgs[1]; // reference to the selected LI element
//var oData = aArgs[2]; // object literal of selected item's result data

// update hidden form field with the selected item's ID
//myHiddenField.value = oData.id;
//};
//oAC.itemSelectEvent.subscribe(myHandler);

// Rather than submit the form,
// alert the stored ID instead
//var onFormSubmit = function(e, myForm) {
//YAHOO.util.Event.preventDefault(e);
//alert("MedCommons ID: " + myHiddenField.value);
//};
//YAHOO.util.Event.addListener(YAHOO.util.Dom.get("myForm"), "submit", onFormSubmit);

//return {
//oDS: oDS,
//oAC: oAC
//};
//}();
</script>

<script type="text/javascript" src="Envision-DataKit/fieldedit.js"></script>

<!--=============================== -->


<!--MyBlogLog instrumentation-->
<script type="text/javascript" src="http://track2.mybloglog.com/js/jsserv.php?mblID=2007020704011645"></script>

</body>
</html>

<script type="text/javascript" src="http://us.js2.yimg.com/us.js.yimg.com/lib/rt/rto1_78.js"></script><script>var rt_page="792404008:FRTMA"; var rt_ip="72.89.255.17"; if ("function" == typeof(rt_AddVar) ){ rt_AddVar("ys", escape("F14C9345"));}</script><noscript><img src="http://rtb.pclick.yahoo.com/images/nojs.gif?p=792404008:FRTMA"></noscript><script language=javascript>
if(window.yzq_d==null)window.yzq_d=new Object();
window.yzq_d['ztTzZkLEYrM-']='&U=13esmj1e8%2fN%3dztTzZkLEYrM-%2fC%3d289534.9603437.10326224.9298098%2fD%3dFOOT%2fB%3d4123617%2fV%3d1';
</script><noscript><img width=1 height=1 alt="" src="http://us.bc.yahoo.com/b?P=jKiLskWTTNJXr_8pSKtM7wR2SFn_EUkKLNEABoBX&T=142mb4513%2fX%3d1225403601%2fE%3d792404008%2fR%3ddev_net%2fK%3d5%2fV%3d2.1%2fW%3dH%2fY%3dYAHOO%2fF%3d1646153475%2fQ%3d-1%2fS%3d1%2fJ%3dF14C9345&U=13esmj1e8%2fN%3dztTzZkLEYrM-%2fC%3d289534.9603437.10326224.9298098%2fD%3dFOOT%2fB%3d4123617%2fV%3d1"></noscript>
<!-- VER-548 -->
<script language=javascript>
if(window.yzq_p==null)document.write("<scr"+"ipt language=javascript src=http://l.yimg.com/us.js.yimg.com/lib/bc/bc_2.0.4.js></scr"+"ipt>");
</script><script language=javascript>
if(window.yzq_p)yzq_p('P=jKiLskWTTNJXr_8pSKtM7wR2SFn_EUkKLNEABoBX&T=13tqoqrct%2fX%3d1225403601%2fE%3d792404008%2fR%3ddev_net%2fK%3d5%2fV%3d1.1%2fW%3dJ%2fY%3dYAHOO%2fF%3d3173136303%2fS%3d1%2fJ%3dF14C9345');
if(window.yzq_s)yzq_s();
</script><noscript><img width=1 height=1 alt="" src="http://us.bc.yahoo.com/b?P=jKiLskWTTNJXr_8pSKtM7wR2SFn_EUkKLNEABoBX&T=142h28obv%2fX%3d1225403601%2fE%3d792404008%2fR%3ddev_net%2fK%3d5%2fV%3d3.1%2fW%3dJ%2fY%3dYAHOO%2fF%3d1257063833%2fQ%3d-1%2fS%3d1%2fJ%3dF14C9345"></noscript>
<!-- p2.ydn.re1.yahoo.com compressed/chunked Thu Oct 30 14:53:21 PDT 2008 -->

XXX;
	return $foot;
}

function load_table($table)
{
	// this function called by generated code
	global $simtrakid_,$lastidfetched_;
	$vals = array();
	if (isset($_GET['pivot']))
	{
		$pivot = clean($_GET['pivot']);
		$value = clean($_GET['value']);
		$bonus = "$pivot = '$value' AND";
	}
	else $bonus = ''; // basic query only if no pivot

	dbg("Select * from $table where $bonus personid='$simtrakid_' ");
	$q = "Select * from $table where $bonus personid='$simtrakid_' ";
	$result = dosql ($q);
	while ($r=mysql_fetch_array($result))
	{
		$lastidfetched_[$table] = $r['id'];
		$vals []=$r;
	}
	mysql_free_result($result);
	return $vals;
}

function onetab_horizontal ($rows,$viewer,$tabkey, $tablename,$tablabel,$markup, $sortkey,$more,$pivotfield)
{
	global $labels_,$fields_,$viewer_,$simtrakid_,$frontab_,$mcid_;

	// this function called by generated code
	$admin = isset($_REQUEST['admin'])?'&admin':'';
	$jstop =<<<XXX
<script type="text/javascript">
YAHOO.util.Event.addListener(window, "load", function() {
YAHOO.example.EnhanceFromMarkup = function() {
var myColumnDefs = [

XXX;

	$top = <<<XXX
	<div id="tab_$tablename">
	<div id="$markup">
	<table id="table_$tablename">
      <thead>
        <tr>
XXX;
	/* generate headers by reading the metadata table for the basic

	*/
	$first = true;
	foreach ($fields_[$tablename] as $name) if (isset($viewer_[$viewer][$tablename][$name]))
	{
		$top.="<th title='{$labels_[$tablename][$name]}' >{$fields_[$tablename][$name]}</th>";
		if (!$first) $jstop.=','; $first=false;
		$jstop .="{".'key:"'.$fields_[$tablename][$name].'",label:"'.$labels_[$tablename][$name].'", sortable:true}';
	}
	if ($more!='') {

		$top.="<th title='click for more info' >More</th>";
		if (!$first) $jstop.=','; $first=false;
		$jstop .="{".'key:"'.'morelink'.'",label:"'.'More'.'", sortable:false}';
	}
	$mid = <<<XXX
	    </tr>
      </thead>
      <tbody>
XXX;
	/* jsmid is different */
	$jsmid = <<<XXX
	];
	var myDataSource = new YAHOO.util.DataSource(YAHOO.util.Dom.get("table_$tablename"));
        myDataSource.responseType = YAHOO.util.DataSource.TYPE_HTMLTABLE;
        myDataSource.responseSchema = {
        fields: [
XXX;
	$first=true;
	foreach ($fields_[$tablename] as $name) if (isset($viewer_[$viewer][$tablename][$name]))
	{	if (!$first) $jsmid.=',';
	$first=false;
	$jsmid .="{".'key:"'.$fields_[$tablename][$name].'"}';
	}
	/* generate body as rows by reading actual data tables

	*/
	foreach ($rows as $row)
	{  $mid .= '
	<tr>
';	
	foreach ($fields_[$tablename] as $name)
	if (isset($viewer_[$viewer][$tablename][$name])) $mid .= '<td>'.$row[$name].'</td>';
	if ($more!='') {
		if ($pivotfield!='')
		$keyval = "&pivot=$pivotfield&value=".$row[$pivotfield]; else $keyval='';
		$morelink = "?layout=$more&accid=$mcid_".$keyval.$admin;
	 $mid .= '<td>'."<a  href='$morelink' title='click for more detail'>details</a>".'</td>'	;	}
	 $mid .='
	</tr>
	';
	}

	$bottom = <<<XXX
     </tbody>
    </table>
  </div>
</div> 
XXX;
	if ($more!='') {$jsbottommore = ',{key:"morelink"}'; $caption="for more details, click on the link at the end of a row";}
	else $jsbottommore=$caption='';
	$jsbottom = <<<XXX
	$jsbottommore ] };

	var myDataTable = new YAHOO.widget.DataTable("$markup", myColumnDefs, myDataSource,
	{caption:"$caption", sortedBy:{key:"$sortkey"}}
        );
       
        return {oDS: myDataSource, oDT: myDataTable};
    }();
});
</script>
XXX;

	if ("tab_$tablename" == $frontab_) $extra = "class=selected"; else $extra=''; // highlight chosen tab
	$nav=" <li $extra ><a href='#tab_$tablename'><em>$tablabel</em></a></li> ";
	return array($top.$mid.$bottom,$jstop.$jsmid.$jsbottom,$nav);
}


function onetab_vertical ($rows, $ignored, $tabkey, $tablename,$tablabel,$markup, $sortkey,$extra)
{

	// this function called by generated code
	global $labels_,$fields_,$viewer_,$simtrakid_,$playerind_,$frontab_,$panebreak_,$sizes_,$lines_;
	$jstop ='';

	$top = <<<XXX
	<div id="tab_$tabkey">

	<div id="$markup">
  	<div class=tab_pane>

XXX;
	/* generate headers by reading the metadata table for the basic

	*/
	$first = true;
	$top.='';
	$mid = <<<XXX
	
XXX;
	/* jsmid is different */
	$jsmid = '';
	$first=true; $openpaneline = false; $lastline=-1;
	
	foreach ($fields_[$tabkey] as $name)

	{
		if (isset($panebreak_[$tabkey][$name]))
		{
			if (!$first)
			{
				if ($openpaneline)	$mid .='</div>'; // ends the paneline
				$mid.='</div>'; // ends the pane
				$openpaneline = false; $lastline = -1;
			}
				
			$first = false;
			$mid.= "
			<div class=pane id=pane_{$panebreak_[$tabkey][$name]} >
	";
		}
		if ($lines_[$tabkey][$name]!=$lastline) {
			if ($openpaneline)	$mid .='</div>'; // ends the paneline
			$mid .= '<div class=paneline>';//open new line
				$mid .= "<span class=fieldprompt>{$labels_[$tabkey][$name]}</span>";
			$openpaneline = true;
		} else
		$mid .= "<span class=fieldpromptxtra >{$labels_[$tabkey][$name]}</span>";
		//		$mid .= "{$labels_[$tabkey][$name]}";
		//$mid.="<span class=valuef>$padded</span><br/>
		$size = $sizes_[$tabkey][$name];
		$data=$rows[0][$name];
		// HACK:  fix invalid data values
		if(substr($data,0,4) === "/  /")
		$data = "";

		if ($size>0) {
			$size = 5+ (int) ($size/3);
			$mid.="<input id='${tabkey}_${name}' class='valuef invisible' style='width:{$size}em' type='text' disabled='true' value='".htmlentities($data)."'    />";
}
else	$mid.="<textarea id='${tabkey}_${name}' class='valuef invisible' rows='5' cols='50'  wrap='hard' >".htmlentities($data)."</textarea>	";

$lastline=$lines_[$tabkey][$name];
	}
	if (!$first)
	{
		if ($openpaneline)	$mid .='</div>'; // ends the paneline
		$mid.='</div>'; // ends the pane
		$openpaneline = false; $lastline = -1;
	}
	$bottom = <<<XXX
    </div>
  </div>
</div> 
XXX;

	$jsbottom = '';

	if ("tab_$tabkey" == $frontab_) $extra = "class=selected"; // highlight chosen tab
	$nav=" <li $extra ><a href='#tab_$tabkey'><em>$tablabel</em></a></li> ";
	//return array($top.$mid.$bottom,'',$nav);

	return array($top.$mid.$bottom,$jstop.$jsmid.$jsbottom,$nav);
}


function viewer_plugin()
{

	global $allplayers_,$mcid_,$jsonstuff_,$playerind_,$simtrakid_,$frontab_,$lastidfetched_;

	$mcid_=0;
	$standalone=isset($_REQUEST['admin']);

	function playerchoices()
	{  // build the list needed for autocomplete AND a nav widget


		$r = user_record();
		if ($r===false) return false; else
		{
			$GRID = $r->grid;
			switch ($r->role)
			{
				case 'is':
					{$q ="select p.name,p.team,p.mcid from players p where grid='$GRID' and p.mcid!=''";break;}
				case 'league':
					{$q ="select p.name,p.team,p.mcid  from players p,teams t ,leagueteams l where grid='$GRID' and p.teamind = t.teamind and t.teamind=l.teamind and l.leagueind='$r->leagueind' and p.mcid!=''";break;}
				case 'team':
					{$q ="select p.name,p.team,p.mcid from players p where grid='$GRID' and p.teamind='$r->teamind' and p.mcid!=''";break;}
				default :{return false;}
			}
		}
		//	echo "<p>$q</p>";
		$buf=$nav='';
		$result = dosql($q);
		while ($rr = mysql_fetch_object($result))
		{
			if ($buf!='')   $buf.=',';
			else $buf = '[';

			$buf .=<<<XXX
			{name: "$rr->name  ($rr->team)", id: $rr->mcid}
XXX;
		}
		$buf.=']
';
		return $buf;
	}

	function plslogon () {
		header ("Location: /acct/login.php"); // redirect to medcommons screen
		die ("<h2>First, please signon to a Simtrak-enabled MedCommons Account</h2>");
	}

	
	function page_top ($admin,$playerind,$player,$team,$league,$hurlink,$backlink, $layout, $simtrakid_,$imageurl='')
	{
		if ($admin){ // must ask for admin decorations
			$playerchoiceform = playerchoiceform();
			$playerlink="<a target='_navigator' title='open medcommons simtrak navigator on $player' href='' onclick='open_win();return false;' >$player</a>";
			if ($layout=='Demographics') $layout = $layoutxtra =  ''; //Don't show outer labelling
			else 	$layoutxtra = ">&nbsp; $layout";
			if ($imageurl=='') $imageurl = "http://www.medcommons.net/
		unknown-user.png";
			$imageurl = "<img class=ppic src=$imageurl alt=':-(' >";
			$back=($backlink!='')?"<a href='$backlink'> >  SimTrak</a>":"  > SimTrak";
			return standard_top().
<<<XXX

			<a class=floatright href='/acct/home.php'><img alt='' border='0' id='stamp' src='/acct/stamp.php' /></a>
			<h3>$imageurl <a title="Back to welcome page" href=envision.php >$league</a> > $team > $playerlink $back $layoutxtra &nbsp;&nbsp;&nbsp;$hurlink <span class=floatright>$playerchoiceform</span>
</h3>
<!-- =============================== -->
XXX;
		}
		else {global $mcid_;
		if ($layout=='Demographics') $layout = $layoutxtra =  ''; //Don't show outer labelling
		else 	$layoutxtra = ">&nbsp; $layout";
		return standard_top().
<<<XXX
		<h3><a href='?accid=$mcid_' >$league > $team > $player</a> $layoutxtra</h3>
<!-- =============================== -->
XXX;
		}
	}
	//
	// generate full page for viewerplugin;
	//
	$t1 = microtime(true);

	//decode our intentions
	if (!isset($_GET['tab'])) $frontab_='tab_A';else
	$frontab_ = $_GET['tab'];
	$admin = ($standalone)?'?admin&':'?';
	if (isset($_GET['accid'])) $mcid_  = $_GET['accid'];
	if (!isset($_GET['layout'])) {$backlink = $admin; $layout_='Demographics'; }
	else	{$layout_ = $_GET['layout']; $backlink= $admin."accid=$mcid_";}

	$tk = substr($frontab_,4); //strip tab_
	$result = dosql("Select ddtable  from _viewerorder where tabkey='$tk' ");
	$ddtable = mysql_fetch_array($result); // get the table, must be there
	$t = $ddtable[0];


	$result = dosql("Select * from players where mcid='$mcid_' ");
	$r=isdb_fetch_object($result);
	if ($r===false) die ("Cant find player with mcid $mcid_");
	$team = $r->team;
	$simtrakid_ = $r->simtrakid;
	$player = substr($r->name,0,30);
	$player =str_pad($player,30,' '); // make this fixed size, doesnt matter as long as log is html
	$playerind_ = $r->playerind;
	$imageurl = $r->imageurl;
	$teamind = $r->teamind;
	$leagueind = $r->leagueind;
	//


	//if (!$standalone)
	//islog('embed',$mcid_,"$simtrakid_ $player ($team/$league) embed $layout_"); else
	//	islog('view',$mcid_,"$simtrakid_  $player ($team/$league) embed $layout_");
	$hurlink = "<a  class=hurlink href=$r->healthurl title='open health records in this window'>HealthURL<img src='http://www.medcommons.net/images/icon_healthURL.gif' /></a>";
	$pagetop = page_top($standalone,$playerind_, $r->name,$r->team,$r->league,$hurlink,'', $layout_,$simtrakid_,$imageurl);


	// now use the data
	$func = 'make_section_tabs_'.$layout_;
	$main = $func(); // auto generated in stview.inc.php
	$xtrafoot = <<<XXX
	<script type="text/javascript">
	var tabView = new YAHOO.widget.TabView('demo');
	var parseNumberFromCurrency = function(sString) {return parseFloat(sString.substring(1));};
	$jsonstuff_
	var playerId = $playerind_;
	var recordId = $lastidfetched_[$t];
addLoadEvent(simtrakEditor.init);
</script> 
XXX;
	$pagefoot = page_foot().$xtrafoot;
	// cin gere bitg wats
	$body = <<<XXX
	$pagetop
	$main
	$pagefoot
XXX;
	$t2 = microtime(true);


	$delta2 = round($t2-$t1,4);
	echo $body."<p><small>elapsed $delta2 secs</small></p>";
}



viewer_plugin(); // mcid is hidden variable

?>

