<?php

require 'login.inc.php';
require 'mc.inc.php';
require 'settings.php';

if (isset($_POST) && isset($_POST['openid_url']))
  $openid_url = trim($_POST['openid_url']);
else
  $openid_url = '';

$error = False;

function find_server_url($pdo, $q, $type) {
  global $error;

  $s = $pdo->prepare("SELECT appliances.name, appliances.url ".
                     "FROM alloc_log, appliances, alloc_numbers ".
                     "WHERE alloc_numbers.name = '$type' AND ".
                     "      alloc_log.seed = (:q - alloc_numbers.base) DIV ".
                     "                       alloc_numbers.leap AND ".
                     "      alloc_log.numbers_id = alloc_numbers.id AND ".
                     "      appliances.id = alloc_log.appliance_id");
  if (!$s) {
    $e = $db->errorInfo();
    $error = $e[2];
  }
  else if (!$s->execute(array("q" => $q))) {
    $e = $db->errorInfo();
    $error = $e[2];
  }
  else {
    $row = $s->fetch();

    if ($row) {
      if ($row['url'])
        return $row['url'];
      return 'https://' . $row['name'];
    }
  }

  $error = 'Unknown name';

  return False;
}

if (is_mcid($openid_url)) {
  $mcid = clean_mcid($openid_url);
  $db = new PDO($CENTRAL_PDO, $CENTRAL_USER, $CENTRAL_PASS, $DB_SETTINGS);

  $url = find_server_url($db, $mcid, 'mcid');
  if ($url) {
    $location = $url . '/acct/login.php?mcid=' . pretty_mcid($mcid);

    redirect($location);
  }
}
else if (is_tracking_number($openid_url)) {
  $tn = clean_tracking_number($openid_url);
  $db = new PDO($CENTRAL_PDO, $CENTRAL_USER, $CENTRAL_PASS, $DB_SETTINGS);

  $url = find_server_url($db, $tn, 'tracking_number');
  if ($url) {
    $location = $url . '/secure/trackemail.php?a=' . $tn;

    redirect($location);
  }
}
?><!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN"
	  "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<html xmlns='http://www.w3.org/1999/xhtml' xml:lang='en'>
  <head>
    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8' />
    <title></title>
   <link rel='stylesheet' href='media/css/blueprint/screen.css' type='text/css'
         media='screen, projection' />
   <link rel='stylesheet' href='media/css/blueprint/print.css' type='text/css'
         media='print'> 
   <!--[if IE]><link rel="stylesheet" href="media/css/blueprint/ie.css" type="text/css" media="screen, projection"><![endif]-->

   <!-- link rel='stylesheet' href='media/css/medCommonsStyles.css' type='text/css' / -->

<style type='text/css'><!--
#openid_url {
	background: #ffffff url('media/img/openid-icon-small.gif')
	            no-repeat scroll 0pt 50%;
	padding-left: 18px;
	width: 210px;
}

h1, h2, h3, h4 {
	font-family: "Lucida Grande", "Lucida Sans Unicode", Verdana, sans-serif;
}
// --></style>

  </head>

  <body>
    <div class='container'>


<div class='span-12 prepend-6 last'>
  <img src='media/img/MEDcommons_logo_246x50.gif' width='246' height='50'
       alt='Medcommons, Inc.' />
</div>

<h2 class='span-6 prepend-10'>Sign In</h2>
<form method='post' action='http://globals.medcommons.net/login/loginhandler.php'>
 <div class='span-6 prepend-10'>
    <input type='text' name='openid_url' id='openid_url' value="<?= htmlspecialchars($openid_url) ?>" />
    <em>
      <div class='last'>http://username.myopenid.com</div>
      <div class='last'>user@email.com</div>
      <div class='last'>1583-2972-1421-5508</div>
    </em>
<? if ($error) { ?>
    <span class='error'><?= htmlspecialchars($error) ?></span>
<? } ?>
  </div>
  <div class='span-4 last'>
    <input type='submit' value='Sign In' />
  </div>
</form>

    </div>
  </body>
</html>
