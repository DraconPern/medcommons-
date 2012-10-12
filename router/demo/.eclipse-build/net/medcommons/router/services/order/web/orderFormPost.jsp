<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%--

  MedCommons WADO Viewer JSP

  This page posts the order form to the remote URL.  An embedded form is used to send
  the data so that the details are not exposed directly in the URL.

--%>
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
<bean:define id="viewerForm" name="viewerForm" type="net.medcommons.router.services.wado.WADOViewerForm"/>
<% viewerForm.setSelectedMenuKey( request.getParameter("menuKey") ); %>

  <body onload="document.orderPostForm.submit();">
    <form name="orderPostForm" action='<bean:write name="viewerForm" property="orderUrl"/>' target="orderWindow">
      <html:hidden name="order" property="orderGuid"/>
      <html:hidden name="order" property="trackingNumber"/>
      <html:hidden name="order" property="patientName"/>
      <html:hidden name="order" property="patientSex"/>
      <html:hidden name="order" property="patientDob"/>
      <html:hidden name="order" property="patientAge"/>
      <html:hidden name="order" property="patientId"/>
      <html:hidden name="order" property="nimages"/>
      <html:hidden name="order" property="nseries"/>
      <html:hidden name="order" property="modality"/>
      <html:hidden name="order" property="description"/>
      <html:hidden name="viewerForm" property="dataGuids"/>
      <html:hidden name="viewerForm" property="protocol"/>
      <html:hidden name="viewerForm" property="itemType"/>
      <html:hidden name="viewerForm" property="globalStatus"/>
      <html:hidden name="viewerForm" property="location"/>
      <html:hidden name="viewerForm" property="actor"/>
      <input type="hidden" name="menuKey" value='<%=request.getParameter("menuKey")%>'/>
    </form>
  </body>
</html>
