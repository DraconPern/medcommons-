<%@ page language="java"%>
<%@ page isELIgnored="false" %> 
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%
response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
response.setHeader("Pragma","no-cache"); // HTTP 1.0
response.setDateHeader ("Expires", 0); // prevents caching at the proxy server
%>
<!--
 Copyright 2005 MedCommons Inc.   All Rights Reserved.
-->
  <c:import var="xslt" url="stylesheets/ccr2htm.xsl"/>
  <c:catch var="e">
    <x:transform xslt="${xslt}" xml="${ccr.xml}" xsltSystemId="stylesheets/ccr2htm.xsl">
    </x:transform>    
  </c:catch>
  <c:if test="${not empty e}">
    <bean:define id="e" name="e" type="java.lang.Throwable"/>
    <% log("Error occurred transforming CCR: " + e.getMessage());  e.printStackTrace(System.err); %>
    <html>
    <head>
      <link href="main.css" rel="stylesheet" type="text/css"/>
    </head>
    <body>
      <h1 style="font-family: arial, helvetica">Unable to Display CCR</h1>
      <p>A problem was encounted displaying this CCR.  The CCR may have invalid
      content.</p>
    <p>The following error was returned:</p>
      <pre style="font-size: x-small; width: 300px; margin-left: 50px;">${e.message}</pre>
    </body>
    </html>
  </c:if>
