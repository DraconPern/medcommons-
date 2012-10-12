<?php

require 'settings.php';
require 'mc.inc.php';

if (isset($_REQUEST['from']))
  $from = $_REQUEST['from'];
else if (isset($_SERVER['HTTP_REFERER']))
  $from = $_SERVER['HTTP_REFERER'];
else
  $from = False;

if ($from) {
  if ($i = strrpos($from, '?'))
    $from = substr($from, 0, $i);
}

if (isset($_REQUEST['p']))
  $args = '?p=' . $_REQUEST['p'] . '&error=';
else
  $args = '?error=';

if (isset($_REQUEST['q'])) {
  $q = $_REQUEST['q'];

  if (strncasecmp($q, 'http://', 7) == 0 ||
      strncasecmp($q, 'https://', 8) == 0) {
    $q = parse_url($q, PHP_URL_PATH);
    if ($q[0] == '/') $q = substr($q, 1);
  }

  if (is_valid_mcid($q)) {
    $mcid = clean_mcid($q);
    $db = new PDO($CENTRAL_PDO, $CENTRAL_USER, $CENTRAL_PASS, $DB_SETTINGS);

    $sql = "SELECT appliances.name ".
      "FROM alloc_log, appliances, alloc_numbers ".
      "WHERE alloc_log.numbers_id = alloc_numbers.id AND ".
      "      alloc_numbers.name = 'mcid' AND ".
      "      alloc_log.seed = ($mcid - alloc_numbers.base) div ".
      "                       alloc_numbers.leap AND ".
      "      appliances.id = alloc_log.appliance_id";

    $s = $db->prepare($sql);

    if (!$s) {
      $e = $db->errorInfo();
      $error = $e[2];
    }
    else if (!$s->execute()) {
      $e = $s->errorInfo();
      $error = $e[2];
    }
    else {
      $row = $s->fetch();

      if ($row) {
        $url = 'https://' . $row['name'];
	$location = $url . '/acct/login.php?mcid=' . pretty_mcid($mcid);

        header('Location: ' . $location);
?><html>
 <head>
  <link rel='openid.server' href='<?= $url ?>/server/server.php' />
 </head>
 <body>
  <p>If not redirected automatically, click <a href='<?= $location ?>'>here</a>.
  </p>
 </body>
</html>
<?php 
        exit;
      }

      $error = "Unknown MCID";
    }
  }
  else if (is_valid_tracking_number($q)) {
    $tn = clean_tracking_number($q);
    $db = new PDO($CENTRAL_PDO, $CENTRAL_USER, $CENTRAL_PASS, $DB_SETTINGS);

    $s = $db->prepare("SELECT appliances.name ".
 		      "FROM alloc_log, appliances, alloc_numbers ".
		      "WHERE alloc_log.numbers_id = alloc_numbers.id AND ".
		      "      alloc_numbers.name = 'tracking_number' AND ".
		      "      alloc_log.seed = (:tn - alloc_numbers.base) div ".
		      "                       alloc_numbers.leap AND ".
		      "      appliances.id = alloc_log.appliance_id");

    if (!$s) {
      $e = $db->errorInfo();
      $error = $e[2];
    }
    else if (!$s->execute(array("tn" => $tn))) {
      $e = $s->errorInfo();
      $error = $e[2];
    }
    else {
      $row = $s->fetch();

      if ($row) {
	$url = 'https://' . $row['name'] . '/secure/trackemail.php?a=' . $tn;
	header('Location: ' . $url);
	exit;
      }

      $error = 'Unknown Tracking Number';
    }
  }
  else
    $error = 'Unknown tracking number or MCID';

  if ($from) {
    header('Location: ' . $from . $args . urlencode($error));
    exit;
  }
}
else
  $error = $q = '';

?><html>
  <head>
    <title>MCID Search</title>
  </head>
  <body>
<?php if (isset($error)) { ?>
    <div class='error'>
      <p><?= $error ?></p>
    </div>
<?php } ?>
    <form method='get' action='q.php'>
      <input type='text' name='q' value='<?= $q ?>' />
      <input type='submit' value='Search' />
    </form>
  </body>
</html>
