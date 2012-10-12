<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page import="net.medcommons.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>Encoder Status</title>
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
  </head>
  <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
    <tiles:insert page="header.jsp"/>

    <logic:present name="message" scope="request">
      <p><%=request.getAttribute("message")%></p>
    </logic:present>

    <h3 class="headline">Statistics</h3>
    <table border="0" cellspacing="1" cellpadding="0" bordercolor="#000000" style="position: relative;  left: 30px;">
      <tr><td width="180px"><b>Name</b></td><td width="90px"><b>Value</b></td></tr>
        <tr><td>Images Encoded</td><td><%=Metric.getInstance("imagesEncoded").getValue().toString()%></td></tr>
        <tr><td>Bytes Sent</td><td><%=Metric.getInstance("bytesEncoded").getValue().toString()%></td></tr>
        <% request.setAttribute("bytesPerSecond", new Double(((TimeSampledMetric)Metric.getInstance("imageBytesPerSecond")).getGradient().doubleValue() / 1024.0)); %>
        <tr><td>Throughput</td><td><bean:write name="bytesPerSecond" format="##.#"/> KBytes/sec</td></tr>
    </table>

    <h3 class="headline">Active Jobs</h3>
    <table border="0" cellspacing="1" cellpadding="0" bordercolor="#000000">
      <tr><td width="50px"><b>&nbsp;</b></td><td width="90px"><b>Id</b></td><td width="150px"><b>Client</b></td><td width="120px"><b>Priority</b></td><td width="90px"><b>Age (ms)</b></td><td>&nbsp;</td></tr>
      <logic:iterate id="job" indexId="index" name="activeJobs">
        <tr><td><%=index%></td>
        <td>&nbsp;<bean:write name="job" property="id"/></td>
        <td>&nbsp;<bean:write name="job" property="clientId"/></td>
        <td>&nbsp;<bean:write name="job" property="priority"/></td>
        <td>&nbsp;<bean:write name="job" property="age"/></td>
        <td>&nbsp;<a href="killEncodeJob.do?jobIndex=<%=index%>">kill</a></td></tr>
      </logic:iterate>
    </table>

    <h3 class="headline">Waiting Jobs</h3>
    <table border="0" cellspacing="0" cellpadding="0" bordercolor="#000000">
      <tr><td width="50px"><b>&nbsp;</b></td><td width="90px"><b>Id</b></td><td width="150px"><b>Client</b></td><td width="120px"><b>Priority</b></td><td width="90px"><b>Age (ms)</b></td></tr>
      <logic:iterate id="job" indexId="index" name="waitingJobs">
        <tr><td><%=index%>
        <td>&nbsp;<bean:write name="job" property="id"/></td>
        <td>&nbsp;<bean:write name="job" property="clientId"/></td>
        <td>&nbsp;<bean:write name="job" property="priority"/></td>
        <td>&nbsp;<bean:write name="job" property="age"/></td></tr>
      </logic:iterate>
    </table>
  </body>
</html>

