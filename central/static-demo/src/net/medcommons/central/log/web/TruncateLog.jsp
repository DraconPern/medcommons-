<%@ page import="net.medcommons.central.log.Log" %>
<%
  Log log = Log.getInstance();
  log.truncateLog();
%>
<html>
  <head>
    <title>Log Truncated</title>
  </head>
  <body>
    <h1>Congratulations!</h1>
    <h2>You've truncated the log.</h2>
  </body>
</html>