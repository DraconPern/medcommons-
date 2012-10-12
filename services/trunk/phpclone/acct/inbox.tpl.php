<?
/**
 * Inbox for Home Page
 * *
 * This was pulled hot off CI at 1.11 eastern on 16 setp 2010
 * 
 */
$template->extend("base.tpl.php");
?>

<?section("title")?>
<?= htmlentities($group->name) ?> Inbox for <?= htmlentities($info->email) ?> on <?= $ROOT ?>
<?end_section("title")?>


<?section("stamp")?>
<!--  no login stamp -->
<?end_section("stamp")?>

<?section("head")?>
<style>
#topheader {
    display: none;
}
ul {
	display: inline;
	margin-left:0px;
	margin-top:20px;
	padding-left:0px;
	padding-bottom: 20px;
}

a {
	text-decoration: none;
	color: #777;
}
.inboxentry {
    font-size: 14px;
	text-decoration: none;
	color: #333377;
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
    background-color: #ccc;
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
 font-size: 220%;
 color: black;
}
h2#grouptitle span {
 white-space: nowrap;
}
#grouplinks a {
    font-weight: bold;
}
#patients .spatient {
    margin-left: 1em;
}
li.grouplinks a {
    white-space: nowrap;
}
.addMemberLink {
    font-weight: bold;
    font-size: 140%;
    margin-left: 1em;
}
a.addMemberLink:hover {
text-decoration: none;
}
</style>

<?end_section("head")?>

<?section("content")?>

 <div class='hideerror'></div>    
 <div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">
	<div class="secondary_header">		
		    		<div class="sheader_right">    			
              <p class="text"><img src="../images/more.gif" alt="ACA icon" />Delete</p>
              <p class="link"><a href="/improve.html">sample</a>&nbsp;<a href="/acct/home.php">dashboard</a></p>
            </div>
		        <div class="section_title">
			
				                		<div class="vCenter">
        	    <div class="vCenter_inner">    	
        	    	<div class="vCenter_body">						
        				<h1>Group: <?=$group->name?></h1>	
        				<p>Uploads and other account activity appear below. 
        				Click to view status or health records.</p>
						        			</div> <!-- end vCenter_body -->
        	    </div> <!-- end vCenter_inner -->
        	  </div> <!-- vCenter -->	
        	</div> <!-- page title -->
	</div> <!-- end secondary_header -->

<div id='wrapper' >       
        
<div id='inboxouter'>
    <div id='inboxinner'>
<span class=sectionheader ><span class=h4Sized>Inbox&nbsp;&nbsp;&nbsp;&nbsp;</span></span>

<div class=toppart>
    <?if(count($inbox)):?>
        
        <table id='patients'>
        <tbody>
        <?foreach ($inbox as $patient):?>
            <?
                $when = strftime('%m/%d/%y %H:%M:%S', $patient->CreationDateTime);
                $date = substr($when,0,8);
                $time = substr($when,9,5);
                $hospital = $patient->custom_00;
                $sender = $patient->custom_01;
                $viewlink = gpath('Secure_Url')."/{$patient->PatientIdentifier}?auth={$info->auth}";
                $toconsultant  = $patient->custom_07;
                $patientid = $patient->PatientIdentifier;
            ?>
            <tr class='patientrow'>
            <td>
            <div class=inboxentry>
                <a href='<?="switchpatients.php?n=".urlencode(trim("$patient->PatientGivenName $patient->PatientFamilyName")).
            "&amp;g=" .urlencode($group->name) ."&amp;gid=".$group->groupinstanceid ."&amp;id="        
            .gpath('Secure_Url')."/{$patient->PatientIdentifier}"?>'>
                <span class='stime' ><?=$time?> <?=$date?></span>
                <span class='spatient'><?=htmlentities(trim("$patient->PatientGivenName $patient->PatientFamilyName"))?></span>
                <span class='shospital' ><?=htmlentities($hospital)?></span>
                <span class='sname'  ><?=htmlentities($sender)?></span>
                <span class='slabel'  ><?=htmlentities($patient->custom_06)?></span>
                <span class='slabel'  ><?=htmlentities($toconsultant)?></span>
                </a>
            </div>
            </td>
            <?/*
            <td><a class='addMemberLink' title='Add patient as member' href='add_patient_member.php?g=<?=$group->groupinstanceid?>&accid=<?=$patient->PatientIdentifier?>'>+</a></td>
            */?>
            </tr>
          <?endforeach?>
              </tbody>
            </table>
         
          <?else:?>
                <p>No entries</p>
          <?endif?>
   </div> 
   </div>
   </div>


<?section("membersblock")?>
<div id='membersouter'>
    <div id='membersinner'>
        <span class=sectionheader ><span class=h4Sized>Members</span><a href=''>Add Member</a></span>

        <div class=picpart>
        <?if(count($members)):?>
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
                <a href='<?=gpath('Secure_Url')."/{$patient->PatientIdentifier}"?>'>
                    <div class=photoblock>
                        <img width=100px src='<?=$photourl?>' />
                        <div class='stime' ><?=$time?> <?=$date?></div>
                        <div class='spatient'>
                            <?=htmlentities(trim("$patient->PatientGivenName $patient->PatientFamilyName"))?></div>
                    </div>
                 </a>
            </div>
    
            <?endforeach?>
            <?else:?>
                <p>No entries</p>
            <?endif?>
            
        <?/*Float must be cleared to prevent overflow */?>
        <div style='clear:both;'></div>
        
        </div>
    </div>
</div>
<?end_section("membersblock")?>

<?end_section("content")?>


