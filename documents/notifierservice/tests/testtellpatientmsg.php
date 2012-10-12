<?PHP
//
//
// test the tellpatientmsg template
//
//   inputs are destination email
//    it might be good to put some error checking in here, but for now, it's// just for our own use
//


$x="../notifierservice.php?";
$x.="mcid="."2222333344445550"."&";
$x.="m1=".$_REQUEST['m1']."&";
$x.="t1="."tellpatientmsg"."&";
$x.="a1=".$_REQUEST['m1']."&";
$x.="b1="."Dr. Gropper"."&";
$x.="c1="."a reply to your message"."&";
$x.="d1="."https://secure.medcommons.net/viewmessage.php?a=02&b=2222333344445550";

header ("Location: $x");
echo "Redirecting to $x";


?>