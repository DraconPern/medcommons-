<%@ page language="java"%>

<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>MedCommons Demo</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta content="none" name="robots">
    <link rel="stylesheet" type="text/css" href="css/medcommons.css">
    <link href="main.css" rel="stylesheet" type="text/css">    
    <script type="text/javascript" src="cookies.js"></script>    
    <script type="text/javascript">
      // 11801
      function objectsByPatientID(patientID){
        window.location="/router/initObjectsByPatientID?patientID=" + patientID;
        }
      
    </script>
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
  <body>
  <table width="100%">
  <tr><td bgcolor="#dff2f7" width="100%"><tiles:insert page="header.jsp"/></td></tr>
      </table>
    <html:form name="xdsPatientForm" type="net.medcommons.router.services.xds.consumer.action.PatientListForm" action="/viewStudies.do">
      <div id="selectionPane">
        <p class="selTitle">Selection Screen - MedCommons Central</p>
        <table width="100%">
          <tr>
            <td class="selHeader">&nbsp;</td>
            <td class="selHeader">Patient</td>
            <td class="selHeader">ID</td>
            <td class="selHeader">Document Type/Time</td>
            <td class="selHeader">Usage</td>
            <td class="selHeader">Document Count</td>
          </tr>
          
          <logic:iterate id="patient" indexId="index" name="xdsPatientForm" property="patients">
            <tr>
              <td class="selRow">&nbsp; </td>
              <td class="selRow">&nbsp; </td>
              <td class="selRow"><bean:write name="patient" property="patientId"/></td>
              <td class="selRow"><bean:write name="patient" property="usage"/></td>
              <td class="selRow"><bean:write name="patient" property="documentTypes"/></td>
              <td class="selRow">1</td>
            </tr>
          </logic:iterate>
        </table>
      </div>
      
      
    </html:form>
  </body>
</html>
