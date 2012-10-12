<?
/*
 * Example callback handler for receiving updates 
 * about MedCommons on Demand orders.
 *
 * This example simply receives the notification and translates 
 * it into an email.  You need to set the $email variable
 * below to your email address so that you receive the emails.
 *
 * Author:  Simon Sadedin, ssadedin@medcommons.net
 */

if(!isset($_GET['callers_order_reference']))
  die("No order reference supplied");

$ref =  $_GET['callers_order_reference'];

if(!isset($_GET['status'])) 
  die("No status parameter supplied");

$status = $_GET['status'];

$email = "youremail@yourcompany.com";
$email = "ssadedin@gmail.com";

// Send an email about the received order
$headers = "From:  $email
Reply-To: $email
User-Agent: Order Status Mailer 1.0
MIME-Version: 1.0
Content-Type: text/plain;
";

$msg = "
Order $ref has changed to status $status.

";

mail($email, "Order $ref changed to status $status", $msg, $headers);


echo "Processed Successfully";
