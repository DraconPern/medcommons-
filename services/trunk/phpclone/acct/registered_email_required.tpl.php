<?

?>
<style type='text/css'>
    #biglinks {
        display: none;
    }
    #regsubmit {
        padding: 3px;
    }
</style>

<p>The content you are accessing is limited to access by <?=htmlentities(req('email','(unknown)'))?>.  Please
log in to this account.</p>

<span class="header2">Not Registered?</span>
<p>If you own the email address <?=htmlentities(req('email'))?> then you may register an account linked 
to it now to access the content.  You will need to verify your account by responding to an email 
we send you before access will be granted.</p>
<form name='registerForm' action='register.php'>
    <input type='hidden' name='next' value='<?=htmlentities(req('next'))?>'>
    <input type='hidden' name='email' value='<?=htmlentities(req('email'))?>'>
    <input id='regsubmit' type='submit' name='submit' value='Register'>
</form>