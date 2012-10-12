<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ page isELIgnored="false" %> 
<c:set var="patient" value="${ccr.patient}"/>
<html>
<head><title>MedCommons Transaction Status</title>
  <link href="main.css" rel="stylesheet" type="text/css"/>
  <link href="notificationForm.css" rel="stylesheet" type="text/css"/>
  <script language="JavaScript" src="utils.js"></script>
  <script language="JavaScript">
    var sendError = null
    function sendFinished(statusForm) {
      if(!statusForm) {
        el('status').innerHTML = 'INTERNAL ERROR <br/><a style="font-size:xx-small;" href="javascript:viewInternalError();">view detail</a>';
        return;
      }

      el('status').innerHTML = statusForm.status.value;
      el('trackingNumber').innerHTML = statusForm.trackingNumber.value;
      el('pin').innerHTML = statusForm.pin.value;
      if(statusForm.status.value == 'FAILED') {
        el('error').innerHTML = statusForm.error.value;
        el('errorrow').style.display = 'block';
      }
      window.opener.refreshPage();
      window.opener.parent.setTabText(window.parent.currentTab, 'Current CCR');        
      self.focus();
    }

    function viewError() {

    }
    
    function viewInternalError() {
      document.body.innerHTML = window.opener.resultsframe.document.body.innerHTML;
    }

    function init() {
      window.opener.referralComplete = sendFinished;
      window.opener.document.referralForm.target = 'resultsframe';
      window.opener.document.referralForm.submit();
    }
  </script>
</head>
<body class="emailBody" onload="init();">
  <h3>Status</h3>
  <div id="errorrow" style="border-style: solid; border-width: 2px; border-color: #aaa; background-color: #ccc; padding: 3px; margin: 10 10 10 10; display: none;"><b>Error:</b></span>&nbsp;&nbsp;<span id="error">-</span></div>
  <table id="statusTable" style="margin-left: 10px;">
      <tr><th>Status</th><td id="status">Sending</td></tr>
      <tr><th>Tracking Number</th><td id="trackingNumber">-</td></tr>
      <tr><th>PIN</th><td id="pin">-</td></tr>
      <tr><td>&nbsp;</th><td>&nbsp;</td></tr>
      <tr><th>&nbsp;</th><td><input type="button" value="Print" style="width: 80px" onClick="window.print();"/>&nbsp;<input type="button" value="OK" style="width: 80px" onClick="window.close();"></td></tr>
  </table>  

</body>
</html>
