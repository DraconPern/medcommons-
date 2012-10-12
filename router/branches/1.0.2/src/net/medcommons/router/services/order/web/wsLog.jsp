<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ page isELIgnored="false" %> 
<% request.setAttribute("callLog", net.medcommons.rest.RESTUtil.callLog); %>
<html> 
<head>
  <style type="text/css">
    body {
      font-family: arial;
    }
    table tr td {
      font-size: 10px;
      padding: 2px;
    }
  </style>
</head>
<body>
  <table border="1" cellpadding="0" cellspacing="0">
    <tr><th width="120">Date/Time</th><th width="180">Call</th><th width="140">Params</th></th><th width="200">Result</th></tr>
    <c:forEach items="${callLog}" var="c">
      <tr><td><dt:format pattern="MM/dd/yyyy HH:mm:ss">${c.timeStamp}</dt:format></td><td>${c.service}&nbsp; (<a href="${c.url}">url</a>)</td><td>${c.params}</td><td>${c.result}</td></tr>
    </c:forEach>
  </table>
</body>
</html>
