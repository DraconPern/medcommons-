<?PHP

$x=<<<XXX
<h4>Import VRCP Configuration</h4>
<br>
<br>
<form action="../whitebox/importhandler.php" method=post>
Import from:  <input name='upload' type='file'>
<br><br>
<input name='submit' type='submit'>
</form>
XXX;

echo $x;

?>