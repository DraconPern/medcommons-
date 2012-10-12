<?php

require_once 'login.inc.php';
require_once 'settings.php';
require_once 'mc.inc.php';
require_once 'OpenID.php';
require_once "utils.inc.php";

require_once 'common.php';

$mcid = login_required('/acct/settings.php');

$db = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS);

session_start();

$msg = '';

if (count($_POST) != 0 && isset($_POST['openid_url']) && isset($_POST['idp'])) {

  $openid_url = trim($_POST['openid_url']);
  $idp = trim($_POST['idp']);

  $stmt = $db->prepare("SELECT id, format, name FROM identity_providers WHERE source_id = :source_id");
  $stmt->execute(array("source_id" => $idp));
  $row = $stmt->fetch();

  $_SESSION['mc_idp'] = $row['id'];

  $a = explode('%', $row['format'], 2);
  $head = $a[0];
  $tail = $a[1];

  if (substr_compare($openid_url, $head, 0, strlen($head)) != 0)
    $openid_url = $head . $openid_url;

  if (substr_compare($openid_url, $tail, -strlen($tail)) != 0)
    $openid_url .= $tail;

  $scheme = 'http';

  $stmt->closeCursor();

  if (isset($_SERVER['HTTPS']) and $_SERVER['HTTPS'] == 'on') {
    $defaultPort = 443;
    $scheme .= 's';
  }
  else {
    $defaultPort = 80;
  }

  $trust_root = $scheme . '://' . $_SERVER['SERVER_NAME'];

  if ($_SERVER['SERVER_PORT'] != $defaultPort)
    $trust_root .= ':' . $_SERVER['SERVER_PORT'];

  $trust_root .= dirname($_SERVER['PHP_SELF']);
  $process_url = combine_urls($trust_root, "acct/link_user.php");
  $auth_request = $consumer->begin($openid_url);

  if($auth_request) {
    dbg("redirecting using trust_root $trust_root and process_url $process_url");
    redirect($auth_request->redirectUrl($trust_root, $process_url));
  }

  // throw new Exception("Unable to process OpenID URL $openid_url.  Provider $idp did not return valid request. Please check URL.");
  
  $msg = 'OpenID Authentication Error';
}
else {
    
  $response = $consumer->complete(get_request_url());

  if ($response->status == Auth_OpenID_CANCEL) {
    $msg = 'Verification cancelled';
    dbg($msg);
  }
  else if ($response->status == Auth_OpenID_FAILURE) {
    $msg = "OpenID authentication failed: " . $response->message;
    dbg($msg);
  }
  else if ($response->status == Auth_OpenID_SUCCESS) {
    $openid = $response->identity_url;
    dbg("linking id $mcid to $openid");
    link_openid_to_mcid($db, $mcid, $openid, $_SESSION['mc_idp']);

    redirect('/acct/settings.php?page=identities');
  }
  else {
    $msg = "Unknown OpenID response";
    dbg($msg);
  }
}

if(isset($msg) && $msg != "") {
    $identity_msg = $msg;
}

include "./settings.php";

?>
