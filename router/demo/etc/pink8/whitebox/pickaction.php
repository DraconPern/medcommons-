<?php
require_once("../whitebox/wbsubs.inc");
require_once("actions.inc");
session_start();
$selected = zlink("selected","selectfilter.php",false,true);
$wbheader = wbheader('pickaction',"Pick an Action",true);
$x=<<<XXX
$wbheader

<form>
Choose an action
<br><br>
<input type="radio" 
name="Sex" value="0">No action
<br><br>
<input type="radio" checked
name="Sex" value="1">Send everything to:
<br><br>
<input type="radio"
name="Sex" value="2">Get everything from:
<br><br>
<input type="radio"
name="Sex" value="3">Send $selected studies to:
<br><br>
<input type="radio"
name="Sex" value="4">Gets $selected studies from:
<br><br>
<input type="radio"
name="Sex" value="5">Allows Query/Retrieve from:
<br><br>
<input type="radio"
name="Sex" value="6">Can Query/Retrieve from:
<br><br>

</form>

<p>
<input type="submit" name="submit" value="submit">
</p>

</body>
</html>
XXX;
echo $x;
?>

