<%@ page import="net.medcommons.central.log.Log" %>

<%
  Log log = Log.getInstance();
  String filter = request.getParameter("filter");
  if(filter == null) {
    filter = "";
  }
  String[] results = log.readLog(filter);
%>

<html>
  <head>
    <title>Log Viewer</title>
    <style type="text/css">
      body {
        background:	white;
        color:		black;
        font:		14px tahoma, arial, helvetica, sans serif;
      }
    </style>
  </head>
  <body>
    <img src="logo.jpg"/>
    <h1>HIPAA Log Entries:</h1>
    <table border="1">
      <tr>
        <td>Date</td>
        <td>Name</td>
        <td>Action</td>
        <td>Tracking</td>
        <td>Guid</td>
      </tr>
      <% for(int i = 0; i < results.length; i++) { %>
        <%= results[i] %>
      <% } %>
    </table>
  </body>
</html>