<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="s" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%
  response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
  response.setHeader("Pragma","no-cache"); // HTTP 1.0
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
   <head>
      <base href='<%=request.getScheme() + "://" +request.getServerName()+":"+request.getServerPort()+request.getContextPath()%>/'/>
      <title>MedCommons - Error</title>
      <link href="main.css" rel="stylesheet" type="text/css"/>    
      <style type="text/css">
        #loadingDiv { 
          display: none; 
        }
        body {
          margin: 5px;
          margin-left: 30px;
          margin-right: 60px;
          background-color: white;
        }
        #details {
          display: none;
          border-style: solid;
          border-width: 1px;
          padding: 10px; 
        }
      </style>
      <script type="text/javascript">
        function showError() {
          document.getElementById('details').style.display='block';
        }
      </script>
    </head>
    <body style="margin-top: 10px;"
          onLoad="window.focus(); if(window.parent.document.getElementById('topmessage')) window.parent.hide('topmessage');">    
     <div id="errorDiv">
      <p class="headline">An error occurred processing your request.</p>
      <p style="margin-left: 0px; margin-top: 20px;"><html:errors/></p>
      <p style="margin-left: 0px; margin-top: 20px;"><s:errors/></p>
     </div>
      <logic:present name="org.apache.struts.action.EXCEPTION">
        <p><a href="javascript:showError()" title="Display Error Details">Display Details</a></p>
      </logic:present>
     <div id="details">
      <logic:present name="org.apache.struts.action.EXCEPTION">
      <bean:define id="ex" name="org.apache.struts.action.EXCEPTION" type="java.lang.Throwable"/>
       <pre>
        <bean:write name="org.apache.struts.action.EXCEPTION"/>
        <hr/>
        <br/>
        <% ex.printStackTrace(new java.io.PrintWriter(out)); %>
        <% System.err.println("Exception caught by jsp: " + ex.toString()); %>
        <% ex.printStackTrace(System.err); %>
       </pre>
      </logic:present>
     </div>
     <div id="loadingDiv">
      <h2>Loading.... Please Wait</h2>
      <html:errors/>
     </div>
    </body>
</html>


