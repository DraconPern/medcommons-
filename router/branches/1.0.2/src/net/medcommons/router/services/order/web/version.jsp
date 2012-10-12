<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>MedCommons Gateway Status</title>
    <link href="main.css" rel="stylesheet" type="text/css">
  </head>
  <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
  <tiles:insert page="header.jsp">
    <tiles:put name="hideLinks" value="true"/>
  </tiles:insert>
    <h2>MedCommons Gateway Version</h2>

    <div style="position: relative; left: 30px;">
    <p><b>Version</b>:   <%=Version.getVersionString()%></p>
    <p><b>Revision:</b>   <%=Version.getRevision()%></p>
    <p><b>Timestamp:</b>  <%=Version.getBuildTime()%></p>
    <p><b>Release Notes:</b> 
    <div name="releaseNotes" style="position: relative; left: 30px;">
      <pre>
<%=Version.getReleaseNotes()%>
      </pre>
    </div>
    </div>
    </p>
  </body>
</html>


