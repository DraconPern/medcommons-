<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page isELIgnored="false" %> 
<%
  response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
  response.setHeader("Pragma","no-cache"); // HTTP 1.0
%>
<%-- 
  If hpass is provided then this is a redirect from a central logon
 --%>
<c:if test='${!empty param["hpass"]}'>
  <c:set var="autoLogon" value="true" scope="session"/>
  <c:set var="autoLogonUsername" value='${param["username"]}' scope="session"/>
  <c:set var="hpass" value='${param["hpass"]}' scope="session"/>
  <c:set var="initialContents" scope="request" value="tab5"/>
  <jsp:forward page="platform.jsp"/>
</c:if>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <c:set var="demo"><mc:config property="DemoMode" default="false"/></c:set>
  <head>
    <title>MedCommons Login</title>
    <link rel="icon" href="./favicon.ico" type="image/x-icon">  
    <link rel="shortcut icon" href="./favicon.ico" type="image/x-icon"> 
    <link rel="Favorites Icon" href="./favicon.ico">
    <link href="main.css" rel="stylesheet" type="text/css">
    <style type="text/css">
      body {
        font-family: arial,helvetica;
      }

      td {
        font-size: 12px;
        text-align: left;
      }
    </style>
    <script language="JavaScript" src="sha1.js"></script>
    <script language="javascript">
      function init() {
        window.parent.hideToolPalette();
      <c:if test='${!empty trackingNumber}'>
        <logic:notPresent name="logonForm" property="userid">
          document.logonForm.trackingNumber.value='${trackingNumber}';
        </logic:notPresent> </c:if>
         
        <c:if test='${demo and !empty trackingNumber}'>
          if(document.logonForm != null) {
            document.logonForm.pin.value='00000';
          }
      </c:if>
      }
    </script>
  </head>
  <body style="padding: 20px;" onload="init();">

    <logic:present name="message" scope="request">
      <p><%=request.getAttribute("message")%></p>
    </logic:present>

    <h3 class="headline">Welcome to MedCommons.</h3>
      <c:if test='${empty desktop.ownerMedCommonsId }'>
        <c:if test='${!empty trackingNumber}'>
          <p style="color: red;">Please enter the PIN and click 'View' to continue.</p>
        </c:if>
        <% if(request.getAttribute("invalid")!=null) { %>
            <p style="color: red;"><html:errors/></p>
        <% } else { %>      
              <c:if test='${empty trackingNumber}'>
                <p>Please enter your login credentials:</p>
              </c:if>
        <% } %>
        <span style="position: relative; left: 20px;">
          <html:form name="logonForm" scope="session" type="net.medcommons.router.services.wado.actions.LogonForm" action="logon.do" method="POST">
            <table>
              <html:hidden property="password"/>
              <c:if test='${empty trackingNumber}'>
                <tr><td>User Id:</td><td><html:text property="userid" size="20"/></td></tr>
                <tr><td>Password:</td><td><input type="password" name="txtPassword" size="20"/></td></tr>
                <tr><td></td><td><input type="submit" id="logonButton" onclick="document.logonForm.password.value=hex_sha1(document.logonForm.txtPassword.value);" value="Logon" /></td></tr>
                <tr><td>&nbsp;</td><td>&nbsp;</td></tr>
              </c:if>            
              <tr><td>Tracking Number:</td><td><html:text property="trackingNumber" size="20"/></td></tr>
              <tr><td>PIN:</td><td><html:password property="pin" size="20"/></td></tr>
              <tr><td></td><td><input type="submit" id="trackingButton" onclick="document.logonForm.action='track.do';" value="View" /></td></tr>
              <c:if test='${demo and !empty autoTrackingNumber}'>
                 <tr><td></td><td><html:checkbox property="demoLogin" onclick="if(!this.checked) {document.logonForm.pin.value='';} else {document.logonForm.pin.value='00000';}">DEMO Login</html:checkbox></td></tr>
              </c:if>
            </table>
          </html:form>
      </c:if>

      <c:if test='${!empty desktop.ownerMedCommonsId }'>
        <p>You are logged in as <b><mc:medcommonsId>${desktop.ownerMedCommonsId}</mc:medcommonsId></b></p> 
      </c:if>
      
    </span>
    <c:if test='${demo and ( !empty autoTrackingNumber or desktop.currentCcr.demo ) }'>
        <font size="-1"><p><i><b>Note:</b> as this is a DEMO account, the PIN has
        been automatically filled for you.  Please click 'View' to
        continue.</i></p>
        <p style="font-size: 16px; font-weight: bold; color: 527463;"><i>
          Demo images courtesy of Gordon J. Harris, PhD<br/>MGH Radiology 3D Imaging Service</i></p></font>
    </c:if>
    <%-- 
      Used to remove this so that normal logon form shows after track# entered, however
      Adrian has asked that only the track# login shows.
      <c:remove var="trackingNumber"/> 
     --%>
    <c:remove var="autoTrackingNumber"/>
  </body>
</html>

