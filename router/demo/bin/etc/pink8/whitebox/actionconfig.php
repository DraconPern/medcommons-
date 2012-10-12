<?php
require_once("../whitebox/wbsubs.inc");

 readconfig();
 $checked = array("","","","","","","");

 $displayname = cleanreq('displayname'); // passed in or not, depending on whether we want an insert or not
 $user = cleanreq('user');
 $gateway = cleanreq('gateway');
 $readonly = (cleanreq('ro')=="yes"); // if we should run in readonly mode
 $rowid = cleanreq('rowid');

 $status = 
    getvrcpentry($rowid,$userid,$gateway,$createtime,$dicom,$dicomwado,$action,$filter,$mcdest,$mcdestwado,$description);
 if ($status == false) $action=0; //fake it if we can't find it

 if ($action <0) $action=1;
 $checked[$action]="checked";
 
 $wbheader = wbheader('pickaction',"Pick an Action",true);
 $x=<<<XXX
$wbheader

<form method = 'GET' action="actionhandler.php">
<input type='hidden' value='$displayname' name='displayname'>
<input type='hidden' value='$user' name='user'>
<input type='hidden' value='$gateway' name='gateway'>
<input type='hidden' value='$rowid' name='rowid'>


Choose an action
<br><br>
<input type="radio" $checked[0]
name="actioncode" value="0">No action
<br><br>
<input type="radio" $checked[1]
name="actioncode" value="1">Send everything to:
<br><br>
<input type="radio" $checked[2]
name="actioncode" value="2">Get everything from:
<br><br>
<input type="radio" $checked[3]
name="actioncode" value="3">Send <a href=pickstudies.php>selected</a> studies to:
<br><br>
<input type="radio" $checked[4]
name="actioncode" value="4">Gets <a href=pickstudies.php>selected</a> studies from:
<br><br>
<input type="radio" $checked[5]
name="actioncode" value="5">Allows Query/Retrieve from:
<br><br>
<input type="radio" $checked[6]
name="actioncode" value="6">Can Query/Retrieve from:
<br><br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<p>
<input type="submit" name="submit" value="submit">
</p>
</form>
</body>
</html>
XXX;
echo $x;
?>

