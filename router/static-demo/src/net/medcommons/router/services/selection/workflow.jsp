<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
<head>
  <title>MedCommons Demo</title>
  <meta http-equiv="Content-Type"
 content="text/html; charset=iso-8859-1">
  <meta content="none" name="robots">
  <link rel="stylesheet" type="text/css" href="css/medcommons.css">
  <link href="main.css" rel="stylesheet" type="text/css">
  <script type="text/javascript" src="cookies.js"></script>
  <style type="text/css">
      
      BODY {
      PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; MARGIN: 0px; OVERFLOW: hidden; PADDING-TOP: 0px; BACKGROUND-COLOR: #ffffff
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
      }
    </style>
  <script language="JavaScript">
      function checkSelected() {
        i=0;
        count=0;
        while( document.getElementById('studies'+i) != null ) {
          if(document.getElementById('studies'+i).checked) {
            count++;
          }
          i++;
        }
        
        if(count == 0) {
          alert('Please select one or more studies to view');
          return false;
        }        
        document.studyForm.submit();
        return false;
      }

      function uncheckOthers(checkboxToSelect) {
        i=0;        
        while( document.getElementById('studies'+i) != null ) {
          var cbox = document.getElementById('studies'+i);
          if(cbox.id != checkboxToSelect.id) {
            cbox.checked = false;
          }
          i++;
        }        
      }

      var orderButton = new Image(); orderButton.src = 'images/orderbutton.png';
      var orderButton_ro = new Image(); orderButton_ro.src = 'images/orderbutton_ro.png';
    </script>
</head>
<body onload="loggedInAs();">
<table width="100%">
  <tbody>
    <tr>
      <td bgcolor="#dff2f7" width="100%">
       <%@ include file="header.jsp" %>
      </td>
    </tr>
  </tbody>
</table>
<form name="studyForm" method="post"
 action="/router/viewStudies.do">
  <div id="selectionPane">
  <p class="selTitle">Selection Screen - Router<br>
  </p>
  <table width="100%">
    <tbody>
      <tr>
        <td class="selHeader">&nbsp;</td>
        <td class="selHeader">Patient</td>
        <td class="selHeader">ID</td>
        <td class="selHeader">Date/Time</td>
        <td class="selHeader">Description</td>
        <td class="selHeader">Status</td>
      </tr>
      <tr>
        <td class="selRow"><input name="studies[0].selected" value="on"
 onclick="uncheckOthers( this );" id="studies0" type="checkbox"></td>
        <td style="font-style: italic;" class="selRow">Smith John A</td>
        <td style="font-style: italic;" class="selRow">123454321</td>
        <td class="selRow"><a
 href="http://www.medcommons.net:9080/router/WADOViewer.jsp?guid=19098c6d4b0559a5df2d1ecf752adc1e&amp;name=Joe+User&amp;tracking=19098C6D4B05&amp;address=123%20Lucky%20St&amp;state=MT&amp;city=Butte&amp;zip=83132&amp;cardnumber=7817574478133225&amp;amount=150.00&amp;tax=12.00&amp;charge=162.00&amp;expiration=12/09&amp;copyto=agropper@medcommons.org&amp;comments=%20CERVICAL%20SPINE%20&amp;history=%3cunknown%3e&amp;signature1=MedCommons%20%3confile%3e&amp;signature2=Joes%20Imaging%20Centres%20Inc%20%3confile%3e">07/18/2000
02:11 PM</a> </td>
        <td style="font-style: italic;" class="selRow">Stone Protocol</td>
        <td class="selRow"><span style="font-style: italic;">Transfer</span><br>
        </td>
      </tr>
      <tr>
        <td class="selRow"><input name="studies[1].selected" value="on"
 onclick="uncheckOthers( this );" id="studies1" type="checkbox"></td>
        <td class="selRow">Doe Joseph B</td>
        <td class="selRow">543212345</td>
        <td class="selRow"><a
 href="http://www.medcommons.net:9080/router/WADOViewer.jsp?guid=59b5be93b8a1782277b0ffec44cbf168&amp;name=Joe+User&amp;tracking=59B5BE93B8A1&amp;address=123%20Lucky%20St&amp;state=MT&amp;city=Butte&amp;zip=83132&amp;cardnumber=7817574478133225&amp;amount=150.00&amp;tax=12.00&amp;charge=162.00&amp;expiration=12/09&amp;copyto=agropper@medcommons.org&amp;comments=%20CERVICAL%20SPINE%20&amp;history=%3cunknown%3e&amp;signature1=MedCommons%20%3confile%3e&amp;signature2=Joes%20Imaging%20Centres%20Inc%20%3confile%3e">07/18/2004
01:30 PM</a> </td>
        <td class="selRow">CERVICAL SPINE</td>
        <td class="selRow">Available<br>
        </td>
      </tr>
      <tr class="selRow">
        <td style="vertical-align: top;"><input
 name="studies[1].selected" value="on" onclick="uncheckOthers( this );"
 id="studies1" type="checkbox"></td>
        <td style="vertical-align: top;">Jones, John<br>
        </td>
        <td style="vertical-align: top;">54524545<br>
        </td>
        <td style="vertical-align: top;"><a
 href="http://www.medcommons.net:9080/router/WADOViewer.jsp?guid=59b5be93b8a1782277b0ffec44cbf168&amp;name=Joe+User&amp;tracking=59B5BE93B8A1&amp;address=123%20Lucky%20St&amp;state=MT&amp;city=Butte&amp;zip=83132&amp;cardnumber=7817574478133225&amp;amount=150.00&amp;tax=12.00&amp;charge=162.00&amp;expiration=12/09&amp;copyto=agropper@medcommons.org&amp;comments=%20CERVICAL%20SPINE%20&amp;history=%3cunknown%3e&amp;signature1=MedCommons%20%3confile%3e&amp;signature2=Joes%20Imaging%20Centres%20Inc%20%3confile%3e">07/18/2004
01:17 PM</a></td>
        <td style="vertical-align: top;" class="selRow">Head CT<br>
        </td>
        <td style="vertical-align: top;" class="selRow"><span style="font-weight: bold;">Complete</span><br>
        </td>
      </tr>
    </tbody>
  </table>
  </div>
  <div id="selectionButtons">
  <table width="100%">
    <tbody>
      <tr>
        <td class="instructionText" colspan="2">&nbsp;</td>
      </tr>
      <tr>
        <td>
        <p>Select studies and then click the View button to view and/or
order these studies. </p>
        </td>
        <td valign="center"> <a href="" border="0"> <img alt="View"
 id="orderB" src="images/button_view.gif"
 onclick="return checkSelected();" border="0"></a></td>
      </tr>
<!--spacer--> <tr>
        <td colspan="2"><br>
        </td>
      </tr>
      <tr class="selTitle">
        <td align="left">
        <p>&nbsp; Select studies and then click the Get button to send
these studies to your DICOM workstation&nbsp; </p>
        </td>
        <td>&nbsp;GET</td>
      </tr>
      <tr>
        <td style="vertical-align: top;"><p>Note: studies in italics are being transferred and can not be selected until available</p>
        </td>
        <td style="vertical-align: top;"><br>
        </td>
      </tr>
    </tbody>
  </table>
  </div>
</form>
</body>
</html>
