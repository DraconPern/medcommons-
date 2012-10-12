<?
$template->extend("base.tpl.php");
$template->set("title","Confirm Add to Group");
?>

<?block("head")?>
<style type='text/css'>
  @import url('register.css');
  
  table {
      width: 100%;
  }
  
  hr {
      clear: both;
  }
  
  h4 {
      margin-top: 6px;
  }
  
  #submitButton {
      float: right;
  }
  
  #inviteEmails {
      width: 80%;
      margin-left: 20%;
  }
  
  #registerContents {
      margin-left: 10%;
      margin-top: 15px; 
      width: 540px;
  }
  p.error,span.error { color: red; }
  #registerContents h4 img {
      float: right;
      position: relative;
      top: 5px;
      cursor: pointer;
  }
  
  p.instr {
      font-style: italic;
      font-size: 95%;
      display: none;
  }
</style>
<script type='text/javascript' src='acct_all.js'></script>

<?end_block("head")?>

<?block("content")?>
<h2>Confirm Add to Group</h2> 

<p>You have been invited to join a new group but you are already a member of an existing group.  
You can be a member of both groups, but you can only have a single active group at a time.  You 
can switch between active groups using the options
on your <a href='settings.php?page=personalAccount'>Settings Page</a>.</p>
<p>If you want to remove yourself from one of the groups, go to your <a href='settings.php?page=groups'>Group Settings</a>
   and delete your account from the group there.</p>

<form action='settings.php?page=personalAccount' method='POST'>
    <input type='submit' value='Continue to Settings Page to Select Group'/>
</form>
<?end_block("content")?>
