<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.*" %>
<%@ page isELIgnored="false" %> 
<% request.setAttribute("props", System.getProperties()); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>Active Users</title>
    <link href="main.css" rel="stylesheet" type="text/css">
    <style type="text/css">
      .OK {
        color: green;
      }
      .PROBLEM {
        color: red;
      }
      .statusLabel {
        font-weight: bold;
      }
      fieldset {
        margin-top: 10px;
        margin-bottom: 15px;
        padding-left: 10px;
      }
      body {
        padding-left: 10px;
      }
      table tr td {
        text-align: left;
      }
      table tr td, table tr th {
        font-size: 11px;
        padding: 3px;
      }
    </style>
  </head>
  <% request.setAttribute("gw", request.getParameter("gw")); %>
  <body topmargin="0" marginheight="0">
   <h3>Active Users for Node <mc:config property="NodeName" default="(undefined)"/></h3>
   <table border="1">
    <tr><th>User</th><th>Group</th><th>CCRs Opened</th><th>Since</th></tr>
     <c:forEach var="d" items="${desktops}">
     <tr>
      <td>${d.ownerMedCommonsId}</td>
      <td style="text-align: center;">
        <c:choose><c:when test='${! empty d.accountSettings.groupName}'>${d.accountSettings.groupName}</c:when><c:otherwise>n/a</c:otherwise></c:choose>
      </td>
      <td style="text-align: center;">${fn:length(d.ccrs)}</td>
      <td><dt:format pattern="MM/dd/yyyy K:mm a, z">${d.createDateTimeMs}</dt:format></td></tr>
     </c:forEach>
   </table>
  </body>
</html>

