<?PHP
//
//
// build an argument string from the args that were posted
//
// it might be good to put some error checking in here, but for now, it's just for our own use
//


$x="../notifierservice.php?";
$x.="mcid=".$_POST['mcid']."&";
$x.="m1=".$_POST['m1']."&";
$x.="m2=".$_POST['m2']."&";
$x.="m3=".$_POST['m3']."&";
$x.="t1=".$_POST['t1']."&";
$x.="t2=".$_POST['t2']."&";
$x.="t3=".$_POST['t3']."&";
$x.="a1=".$_POST['a1']."&";
$x.="a2=".$_POST['a2']."&";
$x.="a3=".$_POST['a3']."&";
$x.="b1=".$_POST['b1']."&";
$x.="b2=".$_POST['b2']."&";
$x.="b3=".$_POST['b3']."&";
$x.="c1=".$_POST['c1']."&";
$x.="c2=".$_POST['c2']."&";
$x.="c3=".$_POST['c3']."&";
$x.="d1=".$_POST['d1']."&";
$x.="d2=".$_POST['d2']."&";
$x.="d3=".$_POST['d3']."&";
$x.="e1=".$_POST['e1']."&";
$x.="e2=".$_POST['e2']."&";
$x.="e3=".$_POST['e3']."&";
$x.="f1=".$_POST['f1']."&";
$x.="f2=".$_POST['f2']."&";
$x.="f3=".$_POST['f3']."&";
$x.="g1=".$_POST['g1']."&";
$x.="g2=".$_POST['g2']."&";
$x.="g3=".$_POST['g3'];

header ("Location: $x");
echo "Redirecting to $x";


?>