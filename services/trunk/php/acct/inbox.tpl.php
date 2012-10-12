<?
/**
 * Inbox for Home Page
 */
$template->extend("base.tpl.php");

// We only ever want to display one tip, so track with a flag
$tip = false;
?>

<?section("title")?>
<?if(isset($group)):?>
<?= htmlentities($group->name) ?> Inbox for <?= htmlentities($info->email) ?> on <?= $ROOT ?>
<?else:?>
Welcome to MedCommons
<?endif;?>
<?end_section("title")?>


<?section("stamp")?>
<!--  no login stamp -->
<?end_section("stamp")?>

<?section("head")?>
<meta name="viewport" content="width=640" />
<link rel="shortcut icon" href="../acct/images/favicon.gif" type="image/gif"/>
<link media="all" href="/zip/yui/2.8.0r4/container/assets/skins/sam/container.css,yui/2.8.0r4/container/assets/skins/sam/button.css" type="text/css" rel="stylesheet" />
<style>
#topheader {
    display: none;
}
ul {
	margin-left:0px;
	margin-top:20px;
	padding-left:0px;
	padding-bottom: 0.2em;
}

a {
	text-decoration: none;
	color: #777;
}
.inboxentry {
    font-size: 14px;
	text-decoration: none;
	color: #555;
	width: 10em;
}
td.linkcell {
    text-align: right;
    width: 20px;
    padding-left: 0;
    padding-right: 0;
}

td.linkcell .statusImg {
    position: relative; 
    top: 2px;
}

a:hover {
	text-decoration: underline
}
.photoblock {
	font-size: 10px;
	color:black;
	}
.outerblock {
	display: inline;
	float: left;
	padding: 10px;
	font-size: 14px;
	}
.bottompart {
	clear: both;
	display: block;
	}
.h3Sized {
	font-size:24px;

	color: #333377;

	}
.h4Sized {
	font-size:22px; padding-right: 40px;
	}
.sectionheader {
 margin: 30px 0 10px 0;
}
.sectionheader * {
    vertical-align: middle;
}
.header {
	margin:10px 0 10px 0;
	}
#content {
    margin: 0px 10px;
}
li.grouplinks {
    display:inline;
    list-style:none outside none;
    padding-right:20px;
}
#grouptitle * {
    vertical-align: middle;
}
#membersinner {
    padding: 10px;
    margin: 0;
}
#membersouter {
    padding: 0;
    border: solid 3px #aaa;
    margin: 0 0 1em 0;
    -webkit-border-radius: 5px;
    -moz-border-radius: 5px;
    border-radius: 5px;    
}
h2#grouptitle {
}
h2#grouptitle span {
 white-space: nowrap;
}
#grouplinks a {
    font-weight: bold;
}
li.grouplinks a {
    white-space: nowrap;
}
.addMemberLink {
    font-weight: bold;
    font-size: 140%;
    margin-left: 0.2em;
}
a.addMemberLink:hover {
text-decoration: none;
}
.slabel {
    margin: 0 1em;
}
#patients thead th {
    padding: 0 0.3em;
}
#patients td {
    padding: 0.3em;
    text-align: left;
}
#patients td.linkcell {
    text-align: right;
}
td.middled * {
vertical-align: middle;
margin-left: 1em;
}
#headerlinks {
float: right;
}
#headerlinks img {
    position: relative;
    top: -5px;
}
#patients {
    width: 100%;
}
#ft {
    margin: 0 1em;
}
#ft p {
    display: none;
}

.photoblock.selected {
    border: dotted 1px #666;
}
.removeLink {
    float: right;
}
.removeLink.mainlarge.hidden {
    display: none;
}
#inboxtip .maintipicon {
   position: absolute;
   top: 5px;
   left: 5px;
}
#inboxtip ul {
    margin-left: 2em;
}

#inboxtip p {
    xmargin-bottom: 0;
}

#inboxtip ul {
    margin-top: 0.5em;
}

#content {
    margin-top: 0;
    padding-top: 0;
}
#uploadtip img {
	margin-left: 1em;
}
#uploadtip {
    padding: 1em 1em 0 1em;
	border: solid 2px #aaa;
	border-top: none;
	background-color: #fff0c0;
	background-color: #FFF9E4;
	background-color: #FFF6D8;
	background-color: #f8f8f8;
	
    -webkit-border-bottom-right-radius: 5px;
    -webkit-border-bottom-left-radius: 5px;
    -moz-border-radius-bottomright: 5px;
    -moz-border-radius-bottomleft: 5px;
    border-bottom-right-radius: 5px;    
    border-bottom-left-radius: 5px;    
}
#refreshLink {
    font-size: 10px;
    position: relative; 
    top: 4px;
}
</style>
<script type="text/javascript" src="/zip/yui/2.8.0r4/yahoo-dom-event/yahoo-dom-event.js,yui/2.8.0r4/dragdrop/dragdrop-min.js,yui/2.8.0r4/container/container-min.js,yui/2.8.0r4/element/element-min.js,yui/2.8.0r4/button/button-min.js""></script>
<script type="text/javascript" src='MochiKit.js'></script>
<script type="text/javascript">

function disableTip(tip, enabled, callback) {
    log("disabling tip " + tip + " to state " + enabled);
    Y.io('disable_tip.php?'+authQueryString({enabled: enabled?'true':'false', tip: tip}), { on: { complete: function(id,r) {
        if(callback)
            callback();
    }}});
}

function toggleMember(accid,notip) {

    if(!notip && !(tipState & 1)) {
        var dlg = new YAHOO.widget.SimpleDialog('membertip', { 
            width: '500px',
            fixedcenter:true,
            modal:true,
            visible:true,
            draggable:true,
            buttons: [ {
                text: 'OK', 
                handler: function() { 
                    if($('goaway').checked) {
                        log('disabling tip');
                        disableTip(1, true, partial(toggleMember,accid,true));
                        log("disabled");
                    }
                    else 
                        toggleMember(accid,true);
                    this.destroy();
           }}]
        });
        dlg.setHeader('Adding People');
        dlg.setBody('<div class=tip><img class=icon src="images/biginfo.png" style="float: left;"><div class=tipmsg>'
                    +'<p>You have clicked the button to add a set of image data as a <i>Person</i>.</p>'
                    +"<p>When you set image data as a Person it becomes permanent and won't expire.</p>"
                    +"<div class=goaway><input type=checkbox id=goaway name=goaway checked=true> Don't show this again</div></div></div>");
        
        dlg.render(document.body);    
        return;
    }
    
    Y.io('toggle_patient_member.php?'+authQueryString({accid:accid}), {
        on: {
            complete:  function(id, resp) {            
                var result = Y.JSON.parse(resp.responseText);
                if(result.status == 'ok') {
                    window.location.reload();
                }
                else {
                    alert('A problem occurred while changing the patient to or from a member:\r\n\r\n' + 
                           result.error);
                }
            }
        }
    });
}

var tipState = <?=$info->tip_state?>;

function removeMembers() {
    var accids = [];
    Y.all('.photoblock.selected').each(function(n) {
        accids.push(n.get('id').replace('photo',''));
    });
    Y.io('toggle_patient_member.php?'+authQueryString({accids:accids.join(',')}), { on: { complete: function(id,r) {
        var result = Y.JSON.parse(r.responseText);
        if(result.status == 'ok') {
            window.location.reload();
        }
        else {
            alert('A problem occurred while changing the patient to or from a member:\r\n\r\n' + 
                   result.error);
        }
    }}});
    
}
function hide(id) {
   Y.one($(id)).addClass("hidden"); 
}
</script>

<?end_section("head")?>

<?section("content")?>

<?if(isset($group)):?>


<?if(($tip===false) && isset($upload) && $upload):?>
<? $tip = true;?>
<div id='uploadtip' class=tip>
   <?if($info->tip_state & 2):?>
   <h3 style='display: inline'>Your Images are Uploading</h3> - Check status in your inbox below.
   <br>
   <br>
    <?else:?>
   <img style='float: right' src='images/ddlstatus_small.png'/>
   <h3>Your Images are Uploading</h3>
    <p>
    Your computer is reading the CD and uploading your images in the background. 
    </p>
    <p>You can follow the progress of your transfer through the icon in the notification 
        area of your computer, as illustrated, or by checking the status in your Inbox below.</p>
    <p style='clear: both;'><input type='checkbox' id='dontshowuploadtip'> Don't show this again <a href='' style='float: right;'>Hide</a></p>
    <?endif?>
</div> 
<br style='clear: both;'>
<?endif?>

<?if(($tip === false) && !($info->tip_state & 4)):
$tip = true;
?>
<div id='inboxtip' class='tip headertip roundcorners'>
    <h3 style='margin-top: 0; padding-top: 0;'>Welcome to your Inbox!</h3>
    <p>This is where you can manage the images in your MedCommons Account.</p>
    <ul>
        <li>Click the patient name to see the health record</li>
        <li>Click the envelope to send an email with a link to the health record</li>
        <li>Click the person to move this health record from the Inbox to your Persons list.</li>
    </ul>
    <p>
    <input type=checkbox id=dontshowinboxtip> Don't show this again
    <a href='javascript:hide("inboxtip")' style='float: right;'>Hide</a>
    </p>
</div>
<?endif?>

<?if($group->logo_url):?>
<img onClick="" src='<?= htmlentities($group->logo_url,ENT_QUOTES) ?>' /> 
<?endif?>


<h2 id='grouptitle'>

<div id='headerlinks'>
    <button class='mainshort' onclick='window.location="<?=htmlentities($uploadUrl,ENT_QUOTES)?>"'>Upload</button>
    <?/*
    <a  title='Import Blue Button File' href='<?=$blueUrl?>'><img src='images/bluebuttonup.png'></a>
    */?>
</div>

<span><?= htmlentities($group->name) ?> Inbox</span> <a id='refreshLink' href='/acct/'>Refresh</a> </h2>
<hr>
<table id='patients'>
<thead>
<tr>
<th>&nbsp;</th><th>Patient</th><th><span class='slabel'>Status</span></th><th>&nbsp;</th></tr>
</thead>
<tbody>
    <?if(count($inbox)):?>
    <?foreach ($inbox as $patient):?>
        <?
            $when = strftime('%m/%d/%y %H:%M:%S', $patient->CreationDateTime);
            $date = substr($when,0,8);
            $time = substr($when,9,5);
            $hospital = $patient->facility;
            $sender = $patient->sender_name;
            if($patient->sender_email) {
                if(!$sender)
                    $sender = $patient->sender_email;
                $sender = "<a href='mailto:".htmlentities($patient->sender_email,ENT_QUOTES)."'>$sender</a>";
            }
                
            $viewlink = gpath('Secure_Url')."/{$patient->PatientIdentifier}?auth={$info->auth}&share=true";
            $toconsultant  = $patient->custom_07;
            $patientid = $patient->PatientIdentifier;
            
            
            $patientName = htmlentities(trim("$patient->PatientGivenName $patient->PatientFamilyName"));
            if(strlen($patientName)==0 && ($patient->ddl_status == "DDL_ORDER_XMITING")) {
                $patientName = "<i>Name Pending</i>";
            }
            if($patient->accession_number) 
                $patientName .= " / " . htmlentities($patient->accession_number);
                
            $details = array();
            $fields = array(
                htmlentities($hospital),
                $sender,
                htmlentities($patient->custom_06),
                htmlentities($toconsultant),
            );
            if(isset($statuses[$patient->ddl_status])) {
                $fields[]= htmlentities($statuses[$patient->ddl_status]);
            }
            else
                $fields[]="N/A";
                
            foreach($fields as $i) {
                if($i)
                    $details[]="<span class='slabel'>".$i."</span>";
            }
                
        ?>
        <tr class='patientrow'>
        <td class='inboxentry'>
            <span class='stime' ><?=$time?> <?=$date?></span>
        </td>
        <td>
            <a href='<?=gpath('Secure_Url')."/{$patient->PatientIdentifier}"?>'>
                <span class='spatient'><?=$patientName?></span>
            </a>
        </td>
        <td>
        <?=implode("&bull;", $details);?>
        </td>
        <td class='linkcell'>
            <a class='addMemberLink' title='Send these Images by Email' href='<?=gpath('Secure_Url')."/{$patient->PatientIdentifier}?share=true"?>'>
            <img src='images/mail-forward.png'>
            </a>
        </td>
      <td class='linkcell'>
            <a class='addMemberLink' title='Add patient as member' href='javascript:toggleMember("<?=$patient->PatientIdentifier?>")'>
            <img src='images/adduser.png'>
            </a>
      </td> 
        <td class='linkcell'>
            <?if(isset($statuses[$patient->ddl_status])):?>
            <?="<a href='".gpath('Orders_Url')."/orderstatus?callers_order_reference=".
                              urlencode($patient->callers_order_reference)."'
                              title='Show detailed status and history for order'
                              ><img class='statusImg' src='images/notepad.png'></a>"?>
            <?endif?>
        </td>
      </tr>
      <?endforeach?>
      <?else:?>
            <tr><td><p>No entries</p></td><td></td></tr>
      <?endif?>
          </tbody>
        </table>
<br/>

<?if(count($members)):?>
<div id='membersouter'>
    <div id='membersinner' class='hdcolor'>
        <button class='removeLink mainlarge hidden ' onclick='removeMembers()'>Remove Selected</button>
        <span class=sectionheader ><span class=h4Sized>People</span></span>
        
        <div class=picpart>
        <?foreach ($members as $patient):?>
            <?
                $photourl = $patient ->photoUrl;
                if($photourl=='') 
                    $photourl = gpath("Secure_Url").'/images/unknown-user.png';
                $name = trim("$patient->PatientGivenName $patient->PatientFamilyName");
                $when = strftime('%m/%d/%y %H:%M:%S', $patient->CreationDateTime);
                $date = substr($when,0,8);
                $time = substr($when,9,5);
            ?>
            <div class=outerblock>
                <div class='photoblock clickable' id='photo<?=$patient->PatientIdentifier?>'>
                        <img width=70px src='<?=$photourl?>' />
                        <div class='spatient'>
                        <a href='<?=gpath('Secure_Url')."/{$patient->PatientIdentifier}"?>'>
                            <?=htmlentities(trim("$patient->PatientGivenName $patient->PatientFamilyName"))?>
                        </a>
                        </div>
                </div>
            </div>
    
            <?endforeach?>
            
        <?/*Float must be cleared to prevent overflow */?>
        <div style='clear:both;'></div>
        
        </div>
    </div>
</div>
<?endif?>
<?else:?>
<h2>Welcome to MedCommons</h2>
<p>You don't currently have an active group.   
   Create one or select an existing group in Settings to display it on this page.</p>
<?endif?>

<br/>
<?end_section("content")?>

<?section("footer")?>
<hr/>
<?if(count($groups)):?>
<div id=grouplinks><ul>
<?
$enc = "&enc=".sha1($_COOKIE['mc']);
foreach($groups as $g):?>
    <?
        $switchURL = "set_dashboard_mode.php?accid=".$g->accid."&next=".urlencode("/acct/?g=".$g->groupinstanceid).$enc;
    ?>
    <li class=grouplinks >
     <a href='<?=$switchURL?>' title='Switch to view <?=htmlentities($g->name,ENT_QUOTES)?>'><?=htmlentities($g->name)?></a>
        <?=htmlentities($g->comment)?></li>
<?endforeach?>    
</ul></div>
<?endif?>
Logged in as <?= htmlentities($info->fn) ?> <?= htmlentities($info->ln) ?> <?= htmlentities($info->email) ?> &nbsp; <a href='/acct/settings.php'>settings</a> | <a href='/acct/logout.php'>logout</a><br/>
<?end_section("footer")?>
<?section("endjs")?>

<script type="text/javascript" src='sha1.js'></script>
<script type="text/javascript">

Y.on('domready', function() {
    Y.all('.photoblock').on('click', function(e) {
        e.currentTarget.toggleClass('selected');
        Y.one('.removeLink').removeClass('hidden');
    });
    Y.one(document.body).addClass('yui-skin-sam');

    Y.all('#dontshowuploadtip').on('click', function() {
        var checked = this.get('checked') == 'true';
        disableTip(2, !checked);
    }); 
    Y.all('#dontshowinboxtip').on('click', function() {
        var checked = this.get('checked') == 'true';
        disableTip(4, !checked);
    }); 
});

</script>
<?end_section()?>

