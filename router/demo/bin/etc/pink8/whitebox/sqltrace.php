<?PHP

session_start();
if ($_SESSION['sqltrace']!="") $s="checked"; else $s="";
if ($_SESSION['freelogin']!="") $f="checked"; else $f="";
$x = <<<XXX
<h4>Set Whitebox Options</h4>
<form method=get action=sqltracehandler.php >
<input type=checkbox $s name='sqltrace'>Trace sql statements
<input type=checkbox $f name='freelogin'>Allow this user to bypass whitebox login mechanism
<input type=submit name=submit value='set options'>
</form>

XXX;
echo $x;





