<?
/**
 * Page displayed after the user enters their email address and a verification
 * email is sent to their account.
 */
  $template->extend("base.tpl.php");
?>
<?block("content")?>

<h2>Email Address Change Email Sent</h2>

<p>A verification email has been sent to your <i>new</i> email address.</p>

<p>You must click on the link provided in the verification email to confirm it before
it will be updated in your account.</p>

<p>You also may need to log out and log in again after using the verification link before 
the new email is visible on all pages.</p>

<p><a href='settings.php'>Return to Settings Page</a></p>

<?end_block("content")?>