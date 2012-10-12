<?PHP
//
//
// test the tellpatientmsg template
//
//   inputs are destination email
//    it might be good to put some error checking in here, but for now, it's// just for our own use
//


$x="../notifierservice.php?";
$x.="mcid="."1842795154199729"."&";
$x.="m1=".$_REQUEST['m']."&";
$x.="t1="."welcomemsg"."&";
$x.="a1=".$_REQUEST['m']."&";
$x.="b1="."just about anything can go here";
$x.="c1="."Dr Gropper";

header ("Location: $x");
echo "Redirecting to $x";


?>