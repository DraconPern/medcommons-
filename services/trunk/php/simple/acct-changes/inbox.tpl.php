<?
/**
 * Inbox for Home Page
 */
$template->extend("base.tpl.php");

$template->set("title","MedCommons Scans");
?>


<?section("head")?>
<meta name="viewport" content="width=640" />
<link rel="shortcut icon" href="../acct/images/favicon.gif" type="image/gif"/>
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
.slabel {
    margin: 0 1em;
}
</style>

<?end_section("head")?>
<?block("content")?>

<div class="secondary_page1_3 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">
	<div class="secondary_header">		
		    		<div class="sheader_right">    			
              <p class="text"><img src="/images/sys_images/ACAicon_medium.gif" alt="ACA icon" />Power Users:</p>
              <p class="link"><a href="/improve.html">You can switch groups</a></p>
            </div>
		        <div class="section_title">
			
				                		<div class="vCenter">
        	    <div class="vCenter_inner">    	
        	    	<div class="vCenter_body">						
        				<h1>Inbox  for <?=$group->name?></h1>	
        				<p>Uploads and other account activity appear below. Click to view status or health records.</p>
						        			</div> <!-- end vCenter_body -->
        	    </div> <!-- end vCenter_inner -->
        	  </div> <!-- vCenter -->	
        	</div> <!-- page title -->
	</div> <!-- end secondary_header -->

  

<!-- marker:secondary_portal -->


<div id="wrappers">
<div id="wrapper">
<div id="wrapper2">
<div id="wrapper3">
<div id="wrapper4">
<div id="wrapper5">
<div id="navcollb">
<div id="navcolbg_no">
<div id="navcolbg2">
<div id="sidecolbg_no">
<div id="sidecolspacer">  
<div id="sidecolspacer2">
<div id="sidecolspacer3">



                          
<div id="main">                     
<div class="syndicate"> 
<div id="mainbody2">

   <!-- <div class="mainbody_inner">--><!--ctac commented out-->
   
<div class="mainbody_inner">
<div class="mainbody_content">
          
          
          
          <!-- Begin Page Body -->


<span class=sectionheader ><span class=h4Sized>Inbox&nbsp;&nbsp;&nbsp;&nbsp;</span>
<div class='hideerror'></div>
<div class=toppart>
    <?if(count($inbox)):?>
    <table id='patients'>
    <tbody>
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
                
            $viewlink = gpath('Secure_Url')."/{$patient->PatientIdentifier}?auth={$info->auth}";
            $toconsultant  = $patient->custom_07;
            $patientid = $patient->PatientIdentifier;
            
            
            $patientName = htmlentities(trim("$patient->PatientGivenName $patient->PatientFamilyName"));
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
                $fields[]="<a href='".gpath('Orders_Url')."/orderstatus?callers_order_reference=".
                              urlencode($patient->callers_order_reference)."'>".
                              htmlentities($statuses[$patient->ddl_status]).
                          "</a>";
            }
                
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
        <?/*
        <td><a class='addMemberLink' title='Add patient as member' href='add_patient_member.php?g=<?=$group->groupinstanceid?>&accid=<?=$patient->PatientIdentifier?>'>+</a></td>
        */?>
        </tr>
      <?endforeach?>
          </tbody>
        </table>
        </div> 
      <?else:?>
            <p>No entries</p>
      <?endif?>

<br/>

<div id='membersouter'>
    <div id='membersinner'>
        <span class=sectionheader ><span class=h4Sized>Members</span></span>

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
                        <img width=70px src='<?=$photourl?>' />
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


        </div> <!-- end mainbody_content -->
      </div> <!-- end mainbody_inner2 -->
   <!-- </div>--> <!-- end mainbody_inner ctac commented out-->
</div> <!-- end mainbody -->

</div> <!-- end syndicate -->
</div>
<!-- end main -->
<div class="clear">&nbsp;</div>

</div><!-- end sidecolspacer3 -->
</div><!-- end sidecolspacer2 -->
</div> <!-- end sidecolspacer -->
</div><!-- end sidecolbg -->
</div><!-- end navcolbg2 -->
</div><!-- end navcolbg -->
</div><!-- end navcollb -->
</div> <!-- end wrapper5 -->
</div> <!-- end wrapper4 -->


  

<?end_block("content")?>
