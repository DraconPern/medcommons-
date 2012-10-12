<?
    require_once "template.inc.php";
    require_once "utils.inc.php";
    
    $next = req('next');
    
    template("base.tpl.php")->extend();
?>
<?block("head")?>
<link rel='stylesheet' type='text/css' href='acct_all.css'>
<?end_block("head")?>

<?block("content")?>
<h2>Email Verification Required</h2>

<script type='text/javascript'>
  var checkUrl = 'check_confirm_account.php?next=<?=urlencode($next)?>&fail='+encodeURIComponent(window.location.href);
  window.setTimeout(function() { window.location.href=checkUrl; }, 20000);
</script>

<div style='padding: 30px;'>
  <div class='dashboardmsg'>
    <? include "confirm_email_msg.tpl.php" ?>
  </div>
  <p style='text-align: center;'><a href='javascript:window.location.href=checkUrl'>Refresh Page</a></p>
</div>

<?end_block()?>