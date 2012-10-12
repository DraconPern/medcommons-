<?PHP
session_start();

$sqltrace = ($_REQUEST['sqltrace']=="on")?true:false;
$freelogin =  ($_REQUEST['freelogin']=="on")?true:false;

echo "<br>Whitebox settings have been changed.";

unset ($_SESSION['sqltrace']);
unset ($_SESSION['freelogin']);

$_SESSION['sqltrace']=$sqltrace;
$_SESSION['freelogin']=$freelogin;
?>





