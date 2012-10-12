<?php
require_once("../lib/config.inc");
require_once("../whitebox/wbsubs.inc");

function buildselectionline ($gw,$guid,$tracking,$address,$patient,$id,
	$comments,$history,$datetime,$description,$status)
{   
$url = makewadourl ($gw,$tracking,$guid);
$dt = "<a href = $url>$datetime</a>";
  
	$x=<<<BBB
            <tr>
              <td class="selRow">$patient</td>
              <td class="selRow">$id</td>
              <td class="selRow">$dt</td>
              <td class="selRow">$description</td>
              <td class="selRow">$status</td>
            </tr>
BBB;
return $x;
}
readconfig(); // get connected
$wbh = wbheader('selection',"XDS Registry Dump");
$zz=<<<AAA
</table>
      </div>
      <div id="selectionButtons">
        <table width="100%">
          <tr><td class="instructionText" colspan="2">&nbsp;</td></tr>
          <tr><td>
              <p>Choose Date/Time to View</p></td>
           </td></tr>
          <!--spacer-->
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr class="selTitle">
            <td align="left"> </td><td>&nbsp;</td></tr>
        </table>
      </div>
 
  </body>
</html>
AAA;


$x=<<<XXX

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>MedCommons Selection Screen</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta content="none" name="robots">
    <link rel="stylesheet" type="text/css" href="css/medcommons.css">
    <link href="main.css" rel="stylesheet" type="text/css">    
    <script type="text/javascript" src="cookies.js"></script>    
    <style type="text/css">
      
      BODY {
      PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; MARGIN: 0px; PADDING-TOP: 0px; BACKGROUND-COLOR: #ffffff
      }
      
      #headerPane
      {
      position: absolute;
      left: 30;
      top:  0.2in;
      height: 70;
      width:  718;
      background-color:  #dff2f7
      } 
      
      #selectionPane
      {
      position: relative;
      left: 30;
      top:  0.2in;
      height: 300;
      width:  718;
      }
      
      #selectionButtons
      {
      position: relative;
      left: 30;
      top:  0.2in;
      height: 200;
      width:  718;
      }
      
      .selHeader {
      FONT-WEIGHT: bold; FONT-SIZE: 15px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; BORDER-BOTTOM: solid black
      }
      .selTitle {
      FONT-WEIGHT: bold; FONT-SIZE: 18px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
      }
      .selRow {
      FONT-WEIGHT: normal; FONT-SIZE: 12px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
      }
      .selBottom {
      FONT-WEIGHT: bold; FONT-SIZE: 15px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; BORDER-TOP: solid black 3px
      }
      .instructionText {
      FONT-WEIGHT: normal; FONT-SIZE: 12px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; BORDER-TOP: solid black 3px
      }=
    </style>
    
  </head>
  <body >
  <table width="100%">
$wbh
  </table>
      <div id="selectionPane">
        <p class="selTitle">XDS Registry Dump (not for external release)</p>
        <table width="100%">
          <tr>
            <td class="selHeader">Patient</td>
            <td class="selHeader">ID</td>
            <td class="selHeader">Date/Time</td>
            <td class="selHeader">Description</td>
            <td class="selHeader">Status</td>
          </tr>
XXX;
sqltraceon();
echo $x.getregistrymatches('buildselectionline').$zz;
sqltracedump();
?>