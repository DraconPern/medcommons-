<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<title>Basic Loopback (10sec) from host foo <?php $_SERVER['SERVER_NAME']?> bar </title>
<meta name="viewport" content="minimum-scale=1.0, width=device-width, maximum-scale=1.0, user-scalable=no">
    <meta name="apple-mobile-web-app-capable" content="YES">
    <link rel="apple-touch-icon" href="iphone.png">
    <link rel="stylesheet" href="probe.css">
    <script type="text/javascript" src="js/imcappprobe.js" charset="utf-8"></script>
    <script type="text/javascript" src="js/jquery-1.3.1.min.js"></script>
    <script type="text/javascript" src="js/jquery.sparkline.min.js"></script>
</head>
<body onload = 'ajax_machine2("ws/ijsprobernil.php",10,0)' >
<center>
<div id=pagecontent >
 <img src='custom/logo60by60.png' />
         <table>
                <tbody>
                    <tr class="noshow">
                        <td><div class="rowtag">Not Used</div></td>
                        <td><span id="downloadSize"></span></td>
                    </tr>
                     <tr >
                        <td><div class="rowtag">Test Case</div></td>
                        <td><span id="service"></span> - <span id="iteration"></span></td>
                    </tr>
                          <tr>
                        <td><div class="rowtag">Remote Server</div></td>
                        <td id="serverName"></td>
                    </tr>
                    <tr>
                        <td><div class="rowtag">Remote Time</div></td>
                        <td id="serverTime"></td>
                    </tr>
                    <tr>
                        <td><div class="rowtag">IP Addrs</div></td>
                        <td class=plaincount id="ipaddr"></td>
                    </tr>
                    <tr>
                        <td><div class="rowtag">Round Trip Time</div></td>
                        <td><div id="downloadt" class="sparknum"></div><span class="downloadTime">Loading..</span></td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">Elapsed at Server</div></td>
                        <td><div id="servicet" class="sparknum"></div><span class="serviceTime">Loading..</span></td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">Elapsed in Browser</div></td>
                        <td><div id="buildt" class="sparknum"></div><span class="buildTime">Loading..</span></td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">Server Name</div></td>
			<td><div><?=$_SERVER['SERVER_NAME']?></div></td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">HTTP Host</div></td>
			<td><div><?=$_SERVER['HTTP_HOST']?></div><td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">Request URI</div></td>
			<td><div><?=$_SERVER['REQUEST_URI']?></div><td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">Server Address</div></td>
			<td><div><?=$_SERVER['SERVER_ADDR']?></div><td>
                    </tr>
                    <tr >
                        <td><div class="rowtag">Remote Address</div></td>
			<td><div><?=$_SERVER['REMOTE_ADDR']?></div><td>
                    </tr>
                </tbody>
            </table>
</div>
</center>
</body>
</html>
