<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">

<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <body>
    <h1>Order Hack Screen</h1>

    <p>This screen lets you directly hack order data in the database</p>
     
    <html:form name="addStudyForm"  type="net.medcommons.router.services.dicom.util.web.StudyForm" action="/addStudy.do">    
      Patient Name: <html:text property="order.patientName"/><br/>
      Order Guid: <html:text property="order.orderGuid"/><br/>
      Description: <html:text property="order.description"/><br/>
      <input type="submit" value="Add Order"/>
    </html:form>

    <p>Existing orders:</p>
    <table>    
    <tr style="font-weight:bold"><td>Patient Name</td><td>Order Guid</td><td>Description</td></tr>
    <logic:present name="orders">
      <logic:iterate name="orders" id="order">
          <tr><td><bean:write name="order" property="patientName"/></td><td><bean:write name="order" property="orderGuid"/></td><td><bean:write name="order" property="description"/></td></tr>
      </logic:iterate>
    </logic:present>
    </table>
  </body>
</html>
