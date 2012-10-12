<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib prefix="s" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ page isELIgnored="false" %> 
<!--
 Copyright 2005 MedCommons Inc.   All Rights Reserved.
-->
<%-- This page is a wrapper around the other contents of the MedCommons System --%>
<html>
  <head>
    <title>MedCommons CCR Scriptlet Editor
      <c:if test='${not empty accid}'> - Acct #<mc:medcommonsId>${accid}</mc:medcommonsId></c:if></title>
    <link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
    <style type="text/css">
      body { 
        font-family: arial, verdana;
      }
    </style>
  </head>
  <body onload="initTabs(); preloadImages();" style="height: 100%; overflow: hidden;" >
  <h2>Edit your script below:</h2>
  <s:form action="/Scriptlet.action" name="scriptEditorForm">
    <s:hidden name="ccrIndex"/>
    <s:textarea name="scriptlet" cols="100" rows="20"/>
    <br/>
    <s:submit name="exec" value="Save Script"/>
  </s:form>
  <p>Script results appear below.</p>
  <hr/>
  <div id="scriptletContents">
    ${actionBean.scriptletOutput}
  </div>
  </body>
</html>

