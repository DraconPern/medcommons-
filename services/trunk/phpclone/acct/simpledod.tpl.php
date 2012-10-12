<?
/**
 * An ultra simple form of the MOD upload page.   This version shows no information about 
 * patient selection or table of voucher etc. at the end.  
 */
$template->extend("dod.tpl.php");

?>

<?section("head");?>
    <base href='<?=$baseUrl?>/acct/'/>
    <?include "acctjs.php"?>
    <link rel='stylesheet' type='text/css' href='dod.css'/>
    <style type='text/css'>
        #voucherTable {
            display: none;
        }
        body {
        	font-size: 14px;
        }
        div#topnav {display:none;}
    </style>
<?end_section("head");?>


<?section("selectSource")?>
<?end_section()?>


<?section("selectData");?>
<?end_section("selectData");?>

<?section("voucherTableMessage")?>
<?end_section("voucherTableMessage")?>

<?section("thankyouMessage")?>    
<p>
Your images are now being uploaded per your instructions. Your computer is reading the 
CD and uploading the images.  You may go back to using your 
computer and this process will continue for several minutes in the background.
</p>
<?if(isset($order) && $order->upload_notification_email):?>
<?$emails = explode(",",$order->upload_notification_email);?>
<p>
    An automatic email will be sent to
    <a href='mailto:<?=htmlentities($emails[0], ENT_QUOTES)?>'><?=htmlentities($order->upload_notification_email)?></a>
    for viewing these images.
</p>
<?endif;?>
<p><button id='simpleCloseButton'>OK, GOT IT</button></p>
<?end_section("thankyouMessage")?>    

<?section("extraScript")?>
<script type='text/javascript'>
   uploadOptions.showPrevious = false; 
   if(window.opener && window.opener != window) {
       replaceChildNodes($('simpleCloseButton'),'Close Window');
   }
   connect(ddlEvents, 'uploadStarted', function(evt) {
	show('topnav');
   });
   connect('simpleCloseButton', 'onclick', closeWindow);
</script>
<?end_section("extraScript")?>
