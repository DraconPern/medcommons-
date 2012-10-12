<?PHP

$m=$_REQUEST['m']; //get email address

$x=<<<XXX
<html>

<frameset  rows = "*,*,*,*,*" frameborder="0">

  
    <frame  src="testtelldocmsg.php?m=$m" />
    
      <frame  src="testtellpatientmsg.php?m=$m" />
      
        <frame  src="testreferralinvitemsg.php?m=$m" />
</frameset>

</html>
XXX;

echo $x;

?>