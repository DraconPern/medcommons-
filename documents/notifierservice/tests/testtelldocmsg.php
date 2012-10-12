<?PHP
//
//
// test the telldocmsg template
//
//   inputs are destination email
//    it might be good to put some error checking in here, but for now, it's just for our own use
//


$x="../notifierservice.php?";
$x.="mcid="."1111222233334440"."&";
$x.="m1=".$_REQUEST['m1']."&";
$x.="t1="."telldocmsg"."&";
$x.="a1="."Gropper"."&";
$x.="b1=".$_REQUEST['m1']."&";
$x.="c1="."an urgent message"."&";
$x.="d1="."https://secure.medcommons.net/viewmessage.php?a=01&b=1111222233334440";

header ("Location: $x");
echo "Redirecting to $x";


?>