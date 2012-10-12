<?PHP

setcookie("MCID","",time() - 3600);
setcookie("MCGW","",time() - 3600);
echo "MCID and MCGW cookies have been removed";
?>