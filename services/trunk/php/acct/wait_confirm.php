<?
    require_once "template.inc.php";
    require_once "utils.inc.php";
    
    $next = req('next');
    template("base.tpl.php")->extend();
    
    $wait_url ="/acct/wait_confirm.php?accid=".urlencode(req('accid'))."&email=".urlencode(req('email'))."&resend=true";
    
    $resend_url = "/remail.php?email=".urlencode(req('email')).
                  "&next=".urlencode($wait_url);
    
?>
<?block("head")?>
<script type='text/javascript' src='sha1.js'></script>
<script type='text/javascript'>
var mcid = <?=json_encode(req('accid'))?>;
setInterval(function() {
    if(get_mc_attribute('mcid') == mcid) {
        location.href='/acct/';
    }        
}, 1000);
</script>
<?end_block("head")?>

<?block("content")?>
<input type='hidden' id='accid' name='accid' value='<?=htmlentities(req('accid', ENT_QUOTES))?>'>
<input type='hidden' id='email' name='email' value='<?=htmlentities(req('email', ENT_QUOTES))?>'>
<h2>Please Check Your Email</h2>

<div class='dashboardmsg'>
<? include "confirm_email_msg.tpl.php" ?>
</div>

<?if(!req('resend')):?>
<p>If you don't receive your email within a few minutes, please click below to resend it.</p> 

<button id='resend' title='Click here to send email again' class='mainwide'>Resend Email</button>
<?endif?>
 
<?end_block()?>

<?block("endjs")?>
 <script type='text/javascript'>
     Y.all('#resend').on('click', function() {
         window.location.href='<?=$resend_url?>';
     });
 </script>
<?end_block()?>