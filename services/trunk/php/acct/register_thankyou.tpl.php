<?php
$_GLOBALS['no_session_check']=true;

require_once 'template.inc.php';
require_once 'utils.inc.php';

template("base.tpl.php")->extend();
$uploadLink="/acct/dod.php";
$next=req('next');
?>
<?block("head")?>
<style type='text/css'>
    input.mainwide {
        margin-left: 2em;
    }
    h2 {
        margin-bottom: 1em;
    }
    #buttons {
        margin-top: 1.5em;
    }
    p.headertip {
        padding-bottom: 1em;
    }
</style>
<?end_block()?>

<?block("content")?>
<h2>Thank you for registering with MedCommons!</h2>

<?if($next):?>
    <p>We've sent you an email containing useful links for accessing your account in the future.</p>
    <div id='buttons'>
        <input type="button" class='mainwide' value='Continue' onclick='location.href="<?=htmlentities($next,ENT_QUOTES)?>"'>
    </div>
<?else:?>
    <p>
       Your inbox is the central hub from which you interact with your MedCommons account.   It shows you
       all the images you currently have stored in MedCommons and gives you access to tools for uploading 
       new ones.
    </p>
    
    <div id='buttons'>
        <input type="button" class='mainwide' value='Go to Inbox' onclick='location.href="/acct/"'>
    </div>
    
    <p class='headertip roundcorners'>If you would like sample images to try uploading to your new account,
         try our
         <a href='https://docs.google.com/document/d/1AcK2505E5y-4PTlyHW-Zr6Z5_HQyV1natHxvT__oaa8/edit?hl=en' target='sampledata'>Sample Images for Test Upload</a>.</p>
    
<?endif?>
<?end_block("content")?>
