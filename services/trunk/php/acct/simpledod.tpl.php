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
        #waitstartmsg * {
            vertical-align: middle;
        }
    </style>
<?end_section("head");?>


<?section("selectSource")?>
<?end_section()?>


<?section("selectData");?>
<?end_section("selectData");?>

<?section("voucherTableMessage")?>
<?end_section("voucherTableMessage")?>

<?section("thankyouMessage")?>    
<p id='waitstartmsg'>
 <img src='images/bigloading.gif'/> &nbsp; Starting Upload ... 
</p>
<div id='endinfo' class='hidden'>
    <p>
    Your Images are now being uploaded per your instructions. Your computer is reading the 
    CD and uploading the images. You may close this window. You may go back to using your 
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
    
    <p>You can follow the progress of your transfer through the icon in the notification 
        area of your computer, as illustrated below:</p>
    <p style='text-align: center'><img src='images/ddlstatus.png'/></p> 
    
    <p><button id='simpleCloseButton' class='mainwide'>Continue</button></p>
</div>
<?end_section("thankyouMessage")?>    

<?section("extraScript")?>
<script type='text/javascript'>
   var selfUpload = <?=$selfUpload?"true":"false"?>;
   uploadOptions.showPrevious = false; 
   if(window.opener && window.opener != window) {
       replaceChildNodes($('simpleCloseButton'),'Close Window');
   }
   connect('simpleCloseButton', 'onclick', closeWindow);
   connect(ddlEvents, 'uploadStarted', function(evt) {
       // If the user is uploading to their own account, and 
       // 'next' is not set, go straight back to the inbox
       if(selfUpload && !window.nextPage) {
           window.location.href='/acct/';
           var frm;
           Y.one(document.body).append(frm=FORM({action:'/acct/', method: 'POST'}, 
                           INPUT({type:'hidden', name:'upload', value: callersOrderReference})));
           frm.submit();
       } 
       else {
           appearX('endinfo');
           hide('waitstartmsg');
       }
   });
</script>
<?end_section("extraScript")?>
