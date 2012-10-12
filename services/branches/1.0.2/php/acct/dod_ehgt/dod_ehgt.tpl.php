<?php 
/**
 * EHGT Customized Consumer DOD upload page.
 * 
 * Displays a form allowing anonymous user to upload DICOM via DDL.
 */
global $Secure_Url;

// We extend the base DOD template, overriding just a few key sections of it
$template->extend("dod.tpl.php");

$template->set("title","eHealthLoader");

// Custom favicon
$favicon = "/acct/dod_ehgt/images/favicon.ico";
?>

<?section("favicon")?>
    <link href='<?=$favicon?>' rel='icon'/>
    <link href='<?=$favicon?>' rel='shortcut icon'/>
    <link rel="shortcut icon" href="<?=$favicon?>" type="image/x-icon" />
<?end_section()?>

<?section("logo")?>
    <a href='http://www.ehgt.com/' title='eHealth Global Web Site'><img  border="0" src="dod_ehgt/images/toplogo.png" /></a>
<?end_section("logo")?> 

<?section("topheader")?>
    <?echo "<h2><img src='/acct/dod_ehgt/images/logo.png'/></h2>";?>
<?end_section()?>

<?section("toptext")?>
    <p>This page allows you to upload image data from your PACS for transfer to eHGT and to enable viewing on eHealthViewer <sup>TM</sup>.</p>
<?end_section()?>

<?section("voucherTable")?>    
    <p id='voucherTableMsg' style='margin-top: 10px; margin-left: 60px;'>LOADING ...</p>
    <table id='voucherTable'>
        <tr><th>Patient:</th><td id='patientName'>Please Wait</td></tr>
        <tr><th>Progress:</th>
            <td><span id='progress'>Please Wait</span>
            </td>
        </tr>
        <tr><th>&nbsp;</th><td class='buttons'>
            <span id='cancelUpload' class='hidden'><button id='cancelUploadButton'>Cancel</button></span>
            <button id='printButton' class='hidden'>Print</button> 
        </td></tr>
    </table>
<?end_section()?>    
 
<?section("thankyouMessage")?>    
    <div id='thankyouMessage'>
        <p id='statusMsg'>Success! &nbsp;Files have been uploaded to eHealthGlobal.</p>
        <p>Patient:  <span id='bottomPatientName'></span>
        <p>Thank you for using eHealthLoader!</p>
        <p><button id='restartButton' class='hidden'>New Upload</button></p> 
    </div>
<?end_section()?>    
<?section("footer")?>
     <div id="footer" style='width: 100%;'>
        <div id='poweredBy'>
           <a href='http://www.medcommons.net'><img style='float: right;' src='/images/poweredby.gif'/></a>
        </div>
     
		<span id='footerData' style='float: right;' class='hidden'>
                <span id='healthurlWait'>&nbsp;</span><a id='healthurl' class='hidden' target='ccr' title='Your patient data on MedCommons - Click to Review' href=''></a>
                <span id='voucherId'>&nbsp;</span> / <span id='voucherPin'>&nbsp;</span>
        </span>
     
    	<ul class="listinlinetiny">
			<li>&copy; 2009 eHealth Global Technologies, Inc.</li>&nbsp;&nbsp;&nbsp;&nbsp;
            <li><a href="http://www.medcommons.net/termsofuse.php">Terms of Use</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
            <li><a href='http://www.medcommons.net/privacy.php'>Privacy Policy</a></li>
		</ul>
     </div> 
     <script  type="text/javascript">wsinit();</script>
     <script type='text/javascript'>
         connect(events,'uploadFinished',function() {
             log('setting patient name in bottom');
             $('voucherTableMsg').innerHTML = '';
             $('bottomPatientName').innerHTML = $('patientName').innerHTML;
             hide('voucherTable');
             if(importStatus && importStatus.status == 'Cancelled') {
                $('statusMsg').innerHTML = 'Upload was Cancelled.'; 
             }
             show('thankyouMessage','restartButton');
         });
         connect(events,'voucherDetailsAvailable',function() {
             $('bottomPatientName').innerHTML = $('patientName').innerHTML;
         });
         connect(events,'healthURLAvailable',function() {
             removeElementClass($('footerData'),'hidden');
             <?// default code will try to show these, but only by removing 'hidden' class, so we will still win?>
             hide('printButton'); 
             printed = true; <?// pretend we already printed so the user doesn't get warned?>
             $('healthurl').innerHTML='<img src="/acct/images/hurl.png"/>';
         });
     </script>
     <style type='text/css'>
         #footer {
            position: relative;
         }
         #footerData {
             color: #888;
         }
         #thankyouMessage {
             display: none;
             margin-left: 40px;
         }
         #footerData img {
             position: relative;
             top: 1px;
         }
         #voucherDetailsStepHeader {
             display: none;
         }         
         #poweredBy {
            position: absolute;
            bottom: 34px;
            right: 5px;
         }
         #main h2 {
            margin-bottom: 0px;
            padding-bottom: 10px;
         }
     </style>
<?end_section("footer")?>