<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004-2005 MedCommons Inc.   All Rights Reserved.
-->
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ include file="/taglibs.inc.jsp" %>
<%--

  Upload account files

  This file defines the overall structure and layout for the WADO viewer frame.

--%>

<c:set var="currentCcrDOM" value="${ccr.JDOMDocument}" scope="request"/>



<%-- <mc:hipaa description="Order Viewed"/> --%>
<html>
   <head>
    <title>Upload files to account</title>
    <pack:style>
      <src>common.css</src>
     
    </pack:style>



<c:set var="patientMedCommonsId"><mc:xvalue bean="notificationForm" path="patientMedCommonsId"/></c:set>
    
    <script type="text/javascript">
      var version='<%=net.medcommons.Version.getVersionString()%>';
      var buildDate='<%=net.medcommons.Version.getBuildTime()%>';
      var ccrIndex = '<%=request.getParameter("ccrIndex") %>';
      var auth = '${desktop.authenticationToken}';
      var storageId ='${patientMedCommonsId}';
    </script>


  </head> 
  <body>
     <APPLET 
  CODE="UploadApplet.class"
  CODEBASE="/DDL/app"
  WIDTH="600" HEIGHT="300"
   ARCHIVE = "medcommons-uploadAccountFiles-application.jar,xfire-all.jar,log4j.jar,wsdl4j.jar,medcommons-crypto.jar,medcommons-utils.jar,medcommons-cxplibrary.jar,medcommons-transfer.jar,commons-httpclient.jar,commons-logging.jar,jdom.jar,commons-codec.jar,mail.jar,xbean.jar,ccrxmlbean.jar">
  <PARAM NAME=cxpEndpoint VALUE="<mc:config property="RemoteProtocol"/>://<mc:config property="RemoteHost"/>:<mc:config property="RemotePort"/><mc:config property="CXP2Path"/>">
  <PARAM NAME=storageId VALUE="${patientMedCommonsId}">
  <PARAM NAME=auth VALUE="${desktop.authenticationToken}">
  <PARAM NAME=PaymentBypassToken VALUE="${desktop.authenticationToken}">
  <PARAM NAME=senderId VALUE="${desktop.ownerMedCommonsId}">
  <PARAM NAME=mergeCCR VALUE="NONE">
  <PARAM NAME=patientFamilyName VALUE="${ccr.patientFamilyName}">
  <PARAM NAME=patientGivenName VALUE="${ccr.patientGivenName}">
  <PARAM NAME=patientGender VALUE="${ccr.patientGender}">
  
Applet for uploading files from local machine to specified authenticated account.
</APPLET>
  </body>
</html>

