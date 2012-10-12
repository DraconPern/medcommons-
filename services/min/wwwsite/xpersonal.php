<? 
  require_once 'render.inc.php'; 

  // ssadedin: when invoked from gateway will get voucher details
  // passed which need to be forwarded to registration so that
  // it links them up at the end
  $randomappliance = select_random_appliance();
  $registerurl = "$randomappliance/acct/register.php";
  $vars = array("registerurl" => $registerurl);
  if(isset($_GET['src']) && isset($_GET['tid']) && isset($_GET['otp'])) {
    $src = $_GET['src'];
    $tid = $_GET['tid'];
    $otp = $_GET['otp'];
    $registerurl = "$src/mod/payment_processed.php?copy=true&"."c=".urlencode($tid)."&o=".urlencode($otp)."&vcopy"; 
    $vars["registerurl"] = $registerurl;
    // echo "using register url = ".$registerurl;
  }
  renderas_webpage(false,$vars); 
?>
