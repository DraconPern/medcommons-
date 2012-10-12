<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ page isELIgnored="false" %> 
<%--
  Renders an XML document from a session variable using a stylesheet.

  - pass the name of the session variable as parameter "source"
  - pass the name of the stylesheet sans ".xsl" and "stylesheets/" directory
    as parameter "stylesheet"
--%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->

<c:set var="stylesheetPath">stylesheets/<%=request.getParameter("stylesheet")%>.xsl</c:set>
<c:import var="xslt" url='${stylesheetPath}' />
<%--
<bean:define id="xslt" name="xslt"/>
<% log( xslt.toString()); %>
--%>

<%  
  String source=request.getParameter("source");
  log(source.substring(0,source.indexOf('.')));
  request.setAttribute("sourceXmlBean",request.getSession().getAttribute(source.substring(0,source.indexOf('.')))); 
%> 

<x:transform xslt="${xslt}">
  <bean:write name="sourceXmlBean" property="<%=source.substring(source.indexOf('.')+1)%>" filter="false"/>
</x:transform>
