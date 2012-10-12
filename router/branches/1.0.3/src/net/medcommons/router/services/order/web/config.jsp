<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page isELIgnored="false" %> 
<%
  response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
  response.setHeader("Pragma","no-cache"); // HTTP 1.0
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <c:set var="enableConfig"><mc:config property="EnableWebConfig" default="false"/></c:set>
  <head>
    <title>MedCommons Logon</title>
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
    <script language="JavaScript">
      function doPrompt(key,value) {
        var newValue = prompt(key,value);
        document.configForm.set.value=key;
        document.configForm.value.value=newValue;
        document.configForm.submit();
      }
    </script>
  </head>
  <body style="padding: 20px;">  
    <h3 class="headline">MedCommons Gateway Configuration.</h3>
    <form name="configForm" action="config.jsp" method="post">
      <input type="hidden" name="set"/>
      <input type="hidden" name="value"/>
    </form>
    <c:choose>
      <c:when  test='${!enableConfig}'>
        <p>Web configuration is disabled on this server.</p>
      </c:when>
      <c:otherwise>
        <% request.setAttribute("config",net.medcommons.modules.configuration.Configuration.getAllProperties().entrySet()); %>
        <% 
          if(request.getParameter("set")!=null) {
            net.medcommons.modules.configuration.Configuration.getAllProperties().put(request.getParameter("set"),request.getParameter("value"));
        %>
            <p>Configuration entry ${param['set']} updated to value ${param['value']}.</p>
        <% 
          }
        %>
        <p>There are ${fn:length(config)} configuration values.</p>
        <table>
          <c:forEach items="${config}" var="entry">
          <tr><td>${entry.key}</td>
              <td>${entry.value}</td>
              <td><input type="button" onclick='doPrompt("${entry.key}","${entry.value}");' value="Update"/></td></tr>
          </c:forEach>
        </table>
      </c:otherwise>
    </c:choose>
    
  </body>
</html>

