<?
	require_once "settings.php";
?>
<noscript>
    <meta http-equiv="refresh" content="1; URL=nojs.tpl.php?referrer=<?=urlencode($_SERVER['REQUEST_URI'])?>"> </meta>
</noscript>
<?
if(isset($GLOBALS['DEBUG_JS'])) {
  echo "<script type='text/javascript' src='MochiKitDebug.js'></script>";
  echo "<script type='text/javascript' src='sha1.js'></script>";
  echo "<script type='text/javascript' src='utils.js'></script>";
  echo "<script type='text/javascript' src='ajlib.js'></script>";
  echo "<script type='text/javascript' src='contextManager.js'></script>";
}
else
  echo "<script type='text/javascript' src='acct_all.js'></script>";
?>