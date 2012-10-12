<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>

<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page import="net.medcommons.*" %>
<%@ page isELIgnored="false" %> 

<%
response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
response.setHeader("Pragma","no-cache"); // HTTP 1.0
response.setDateHeader ("Expires", 0); // prevents caching at the proxy server
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>MedCommons Patient Query</title>
    <link href="main.css" rel="stylesheet" type="text/css">
      <style type="text/css">
        body {
          font-size: 14px; 
          font-family: Arial, Helvetica;
        }
    </style>

    <script language="JavaScript">
      <%-- debug logging --%>
      var enableLog = true;
      function log(msg) {
        try {
          if(enableLog) {
            window.external.info(msg);
          }
        }
        catch(er) {
          enableLog=false;
        }
      }
    </script>
  </head>
  <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
    <tiles:insert page="header.jsp"/>

    <logic:present name="message" scope="request">
      <p><%=request.getAttribute("message")%></p>
    </logic:present>

    <h3 class="headline">Patient Query</h3>
    <html:form name="patientQueryForm" 
               action="showPatientFolders.do"
               type="net.medcommons.router.services.xds.consumer.web.action.PatientQueryForm">
      Please enter the patient id:
      <html:text name="patientQueryForm" property="patientId"/> 
      <html:submit/>
      
    </html:form>
  </body>
</html>

