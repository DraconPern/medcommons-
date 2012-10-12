<?php 
/**
 * Consumer DOD upload page.
 * 
 * Displays a form allowing anonymous user to upload DICOM via DDL.
 */
global $Secure_Url;
global $acLogo;

$template->extend("base.tpl.php");

?>

<?section("head");?>
	<base href='<?=$baseUrl?>/acct/'/>
	<?include "acctjs.php"?>
	
	<link rel='stylesheet' type='text/css' href='dod.css'/>
	
<?end_section("head");?>

<?section("logo")?>
  <a href="/" title="Home">
	<?if(isset($groupAccountId) && $group->logo_url):?>
	  <img  border="0" src="<?=$group->logo_url?>"  id="logoImg"/>
	<?
	  // Some wierd logic here: if the normal medcommons logo is in use, use the small one.
	  //   On the other hand, if a custom logo has been set then honour it
	?>
	<?elseif(isset($acLogo) && !preg_match("/mc_logo.png$/", $acLogo)):?>
	  <img  border="0" src="<?=$acLogo?>"  id="logoImg"/>
	<?else:?>
	  <img  border="0" src="/images/logoHeader.gif"  id="logoImg"/>
	<?endif;?>
  </a>
<?end_section("logo");?>

<? section("content"); ?>
<?section("css")?>
<?end_section()?>

<div id='upload'>

<?section("topheader")?>
<?end_section()?>

<?section("toptext")?>
<?end_section()?>

<?section("startDDLHeading")?>
<?end_section("startDDLHeading")?>

<? include "detectDDL.tpl.php";?>

<div id='wholeDataSourceStep'>

  <?section("dicomSourceHeading")?>
  <?end_section("dicomSourceHeading")?>
  
  <div id='fillOutFormStep' class='section <?if(req("step")!="2"):?> hidden<?endif;?>'>
  
    
    <?section("selectSource")?>
    <div id='selectSourceStep'>
        <p id='selectSourceOptions'>
            <button id='cdfolderButton'>I have a CD</button>
            <span id='showPrevious' class='hidden'>
                &nbsp;&nbsp;
                <button id='showPreviousButton' >Show Previous Order</button>
            </span>
        </p>
    </div>
    <?end_section("selectSource")?>
    
    <div id='cdinstructions' class='hidden'>
    
        <h3>CD Instructions</h3>
        <ul id='cdinstructionlist'>
            <li>Please insert the CD</li>
            <li>Ignore or close any autorun or other windows that the CD puts up</li>
            <li>After 10 seconds or so, click Select CD below</li>
            <li>In the next screen, select the CD and click Open or Choose</li>
        </ul>
        <button id='browseFilesButton'>Select CD</button>
        <div id='scanningMsgBlock' class='hidden'>
    	    <p id='scanningMsg'>  
	    	    <img src='images/bigloadingg.gif'> &nbsp; Scanning Folders
    	    </p>
        </div>
    </div><?/*cdinstructions*/?>
      
    <div id='confirmPatients' class='hidden'>
        <h3 class='error'>Patient Name Mismatch</h3>
        <p>The data you selected contained references to multiple patient names.  Please
           review below to confirm that all names match the desired patient:</p>
        <table id='patientNamesTable' class='summary dodx'>
	        <thead>
	           <tr><th colspan='2'>Patient Name</th></tr>
	        </thead>
	        <tbody>
	        </tbody>
	        <tfoot>
	        <td colspan='2'>
		        <button id='ackPatientsButton'>All Patients are Correct</button>
		        <button id='chooseAgain'>Choose Different Data</button>
	        </td>
	        </tfoot>
        </table>
      </div>
    </div> <?/*fillOutFormStep*/?>
</div><?/*wholeDataSourceStep*/?>
 
<?section("selectData");?>
<div id='patientDataBlock' class='hidden section'>

    <form name='dicomUploadForm' id='dicomUploadForm' method='POST'>
       <?if(isset($groupAccountId)):?>
       <input type='hidden' name='groupAccountId' value='<?=$groupAccountId?>'/>
       <?endif;?>
       <?if(isset($oauthConsumerKey)):?>
       <input type='hidden' name='oauthConsumerKey' value='<?=$oauthConsumerKey?>'/>
       <?endif;?>
       <table id='uploadTable'>
       
         <?if($hasCustomDisplay):?>
	         <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
	         <tr class='headingRow'><td colspan='2'><hr></hr><h4>Order Information</h4></td></tr>
         
	         <?for($i=0; $i<10; ++$i):?>
	            <?$label = "label_0".$i?>
	            <?if($order->$label):?>
	                <?$field = "custom_0".$i?>
		            <tr><th><?=htmlentities($order->$label)?></th><td><?=htmlentities($order->$field)?></td></tr>
	            <?endif;?>
	         <?endfor;?>
	         
	         <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
         <?endif;?>
         
         <?if($hasCustomDisplay):?>
	         <tr class='headingRow'><td colspan='2'><hr></hr><h4>Images</h4></td></tr>
         <?endif;?>
         
         <tr><th>Patient</th><td style='font-weight: bold;' id='patientName'></td></tr>
         <tr><th>Send</th><td style='white-space: nowrap'>
                <select id='studies'>
                    <option>All Studies Found</option>
                </select> 
                <a href='javascript:studyHelp()' title='Show help for choosing studies'><img src='images/help.png'/></a>
         </td></tr>
         <tr class='hidden' id='seriesRow'><th>&nbsp;</th>
             <td>
                <select id='series' multiple='true'>
                </select>
             </td>
         </tr>
         <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
         
         <?if(!$loggedIn):?>
         <?/*
         <tr>
            <td>&nbsp;</td>
            <td ><input type='checkbox' id='termsOfUse' name='termsOfUse' value='true'/> 
                 <span style='position: relative; top: -1px;'>I accept the</span> <a id='termsOfUseLink' href="http://www.medcommons.net/termsofuse.php" target='_new'>Terms of Use</a>
            </td>
         </tr>
         */?>
         <?endif;?>
         <tr><td colspan="2"><input id='beginUploadButton' type='button' class='button' value='Begin CD Upload'/></td></tr>
        </table>
    </form>      <p id='transferError' class='error hidden'>&nbsp;</p>
</div>
<?end_section("selectData")?>

<?section("voucherDetailsHeading")?>
<?end_section("voucherDetailsHeading")?>

<div id='voucherDetailsStep' class='hidden section'>

    <?section("voucherTableMessage")?>
	    <p id='voucherTableMessage'>Patient data is stored in a secure HealthURL. &nbsp;Click the patient name to preview.</p>
    <?end_section("voucherTableMessage")?>
    
    <?section("voucherTable")?>
        <table id='voucherTable' class='summary dodx'>
            <thead>
        	    <tr>
            	    <th><div style='position: relative;'>Voucher</div></th>
            	    <th>PIN</th><th>HealthURL Link</th><th>Progress</th>
            	</tr>
    	    </thead>
    	    <tr>
    		    <td id='voucherId'>Please Wait</td>
    		    <td id='voucherPin'>Please Wait</td>
    		    <td id='healthurl'>Please Wait</td>
    		    <td id='progressCell'><span id='progress'>Please Wait</span></td>
            </tr>
    	    <tr class='buttonRow'><td>&nbsp;</td><td class='buttons' colspan='1'>
    	    
                <span id='cancelUpload' class='hidden'><button id='cancelUploadButton'>Cancel</button></span>
    		    <button id='closeButton' title='Close this form' class='hidden'>Done!</button> 
    			<span id='restartButtonWrapper' class='hidden'>
    				<button id='restartButton' class='hidden'>Close</button>
    		    </span>
    	    </td>
    	    <td colspan='2' class='buttons'>
    		    <!--   <button id='nextButton' title='How to use your uploaded data' class='hidden'>What Next?</button> -->
	    	    <p id='inprogressMessage' class='middled hidden' >
	    	    <img src='images/bigloadingg.gif'> <span>&nbsp; Uploading ...</span></p>
    	    </td>
    	    </tr>
        </table>
    <?end_section("voucherTable")?>
     
           
    <p id='transferError2' class='error hidden'>&nbsp;</p>
    
    <?section("thankyouMessage")?>    
    <ul id='endLinks'>
	    <li id='thankyouMessage' class='hidden'>Upload Complete.  Thank you for using MedCommons!</li>
		<li id='printInstructions' class='hidden'>You may 
		  <a class='obviousLink' id='printButton' 
			 title='Print a voucher for your records or to give to a Service Provider to access your images' href='#'>Print</a> 
		  or copy the voucher to use at
	      <a class='obviousLink' href='/mod/voucherclaim.php' target='_new'>Pickup Records</a>
	    </li>
	    <li id='nextLink' class='hidden'>More information - <a href='#' class='obviousLink'>How to use your HealthURL</a></li>
    </ul>
    <?end_section()?>    
</div>

<div style='height: 10px;'>&nbsp;</div>
    
<?if(!$hasCustomDisplay):?>
<hr style='margin-top: 20px;'/>
<?endif;?>

<?include "problemReport.tpl.php"; ?>  

</div>
<div id='applet' style='position: absolute; top: 0px; left: -100px;'></div>
<script type='text/javascript'>
  <?if(isset($next)):?>
  var nextPage = <?=json_encode($next)?>;
  <?endif;?>
  var localGatewayRootURL = '<?=$Secure_Url?>';
  
  commandProxyBaseURL = localGatewayRootURL + "/router/ddl"
  
  <?if(isset($order)):?>
  var order = <?=$orderJSON?>;
  <?endif;?>

  var callersOrderReference = '<?=isset($order)?ent($order->callers_order_reference):''?>';
  var groupAccountId = '<?=isset($groupAccountId)?$groupAccountId:''?>'; 
  
</script>
<script type='text/javascript' src='dod.js'> </script>
<script type='text/javascript'>
  <? include "required_dod_ddl_version.tpl.php";?>
</script>

<?/* Don't auto connect IE because it pops up ugly and confusing security warnings. */?>
<!--[if IE]>
<script  type="text/javascript">
    enableAutoConnect = false;
</script>
<![endif] -->

<?section("extraScript")?>
<?end_section("extraScript")?>

<?end_section("content");?>

<?section("footer")?>
   
    &nbsp;<br/>
     <div id="footer" style='width: 90%;'>
    <ul class="listinlinetiny">
            <li>Copyright &copy; 2010 MedCommons, Inc.</li>&nbsp;&nbsp;&nbsp;&nbsp;
    </ul>

    </div> <!-- footer  end -->
    
    <script  type="text/javascript">
    wsinit();
    nav('Sign In').hidden = true;
    nav('Help').target='_new';
    nav('DDL').target='_new';
    render_top_nav();
    setIdleTimeout(function(){}); // no inactivity timeout
    <?if(req('step')=='2'):?>
	    tick('startDDLHeading');
    <?endif;?>
    </script>
<?end_section("footer")?>
