<?php

require 'template.inc.php';
require 'settings.php';
require 'login.inc.php';
require 'verify.inc.php';
require_once "utils.inc.php";

$mcid = login_required('setemail.php');

$t = template($acTemplateFolder . 'setemail.tpl.php');

if (isset($_POST['email'])) {
  $email = trim($_POST['email']);

  if (is_valid_email($email)) {
    verify_email($mcid, $email);
    redirect('setemail.php?success');
  }

  $t->set('error', "Please enter a valid email address");
  $t->esc('new_email', $email);
}
else 
if(req('success')!==null) {
    echo template("set_email_success.tpl.php")->fetch();
    exit;
}
else {
  $t->set('new_email', '');
}

$db = new PDO($IDENTITY_PDO, $IDENTITY_USER, $IDENTITY_PASS, $DB_SETTINGS);

$stmt = $db->prepare("SELECT email FROM users WHERE mcid = :mcid");
$stmt->execute(array("mcid" => $mcid));

$row = $stmt->fetch();
$t->esc('email', $row['email']);

echo $t->fetch();
?>
