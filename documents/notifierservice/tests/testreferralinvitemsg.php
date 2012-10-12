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
$x.="t1="."referralinvitemsg"."&";
$x.="a1="."12349876"."&";
$x.="b1="."Wo"."&";
$x.="c1="."Dr Gropper";

header ("Location: $x");
echo "Redirecting to $x";


?>