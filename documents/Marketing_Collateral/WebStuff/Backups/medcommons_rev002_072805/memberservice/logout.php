<?PHP

setcookie("MCID","",time() - 3600);
setcookie("MCGW","",time() - 3600);
setcookie("JSESSIONID","",time() - 3600,"","gateway001.test.medcommons.net");
echo "MCID, MCGW  and JSESSIONID cookies have been removed";
?>