<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page isELIgnored="false" %> 

<tiles:useAttribute name="seriesList" classname="java.util.List"/>
<c:set var="ccrDOM" value="${ccr.JDOMDocument}" scope="request"/>

<span class="ImageCount" width="100%">
  <FORM style="float: right;" name="form1" action="" method=post>
  <span class="MenuItems">
  <SELECT id="selectionMenu" name="selectionMenu" class="MenuItems" onChange="menuSelect(this.options[this.selectedIndex].value)" style="z-index: 10;"> 
    <OPTION value="">Select One ...</OPTION> 
     <% 
      int selectionIndex=0;
     for (int i=0; i < seriesList.size();  i++) {
      MCSeries series = (MCSeries) seriesList.get(i);
    %>
        <OPTION id="thumbOption<%=(selectionIndex)%>" value="<%=selectionIndex%>" >
          <%= series.SeriesDescription + " - " + series.size() + " images "%> - <dt:format pattern="MM/dd/yyyy"><%=series.getDate().getTime()%></dt:format>
        </OPTION> 
      <%
      selectionIndex++;
    } 
    %>
    <%-- <OPTION value="">--------</OPTION> --%>
    <%--<script language="Javascript">orders[<%=(selectionIndex+2)%>]='';</script>
    <% selectionIndex++; %>
    <OPTION value="ORDER">New Order</OPTION> 
    <script language="Javascript">orders[<%=(selectionIndex+2)%>]='jgipson@3drinc.com';</script>
    <OPTION value="ORDER<%=selectionIndex++%>">Send to 3DR</OPTION>
    <logic:iterate id="menuItem" name="viewerForm" property="menuItems">
      <script language="Javascript">orders[<%=(selectionIndex+2)%>]='jgipson@3drinc.com';</script>
      <OPTION value='key-<bean:write name="menuItem" property="key"/>'><bean:write name="menuItem" property="label"/></OPTION> 
    </logic:iterate>  --%>
    <%-- 
    Removed because these options have migrated to the floating tool window now.
    <OPTION value="">--------</OPTION> 
    <OPTION value="addReport" id="addReportOption">Add Report<OPTION> 
    <OPTION value="">--------</OPTION> 
    <OPTION value="ABOUT">About</OPTION> 
    <OPTION value="HELP">Help</OPTION>
    --%>
    </SELECT> 
    </span>
    </FORM>
    <div class="footerLabel"><br/><%= seriesList.size() %> Series total - <mc:xvalue bean="ccrDOM" path="patientName"/> / <mc:xvalue bean="ccrDOM" path="patientMedCommonsId"/></div>
</span>
