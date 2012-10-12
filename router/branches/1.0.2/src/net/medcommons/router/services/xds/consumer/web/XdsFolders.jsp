<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page import="net.medcommons.router.services.xds.consumer.web.action.*" %>
<%@ page import="net.medcommons.*" %>
<%@ page isELIgnored="false" %> 
<%--
  MedCommons Patient Desktop

  This page renders a desktop containing a patient's folders which are
  expected to be in the session as a List under name 'ccrs'.

  The page shows one closed folder for each patient folder, and one
  'global' folder that is open on the desktop.  The user can
  open any of the closed folders which is implemented by copying 
  (otherwise hidden) html content from each of the closed folders into 
  the open folder.

  @author Simon Sadedin, MedCommons Inc.
--%>

<%
response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
response.setHeader("Pragma","no-cache"); // HTTP 1.0
response.setDateHeader ("Expires", 0); // prevents caching at the proxy server
%>

<c:set var='thumbImageSize' value='45'/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>MedCommons Patient Record</title>
    <link href="main.css" rel="stylesheet" type="text/css"/>
    <link href="xds.css" rel="stylesheet" type="text/css"/>
    <script language=JavaScript src="utils.js"></script>
    <script language=JavaScript src="xds.js"></script>

    <script language="JavaScript">
      <bean:define id="ccrs" name="desktop" property="ccrs" type="java.util.ArrayList"/>

      var highlightTextColors = [ 'white', 'black', 'red', 'blue', 'gray', 'green', 'purple', 'orange', 'yellow' ];
      var highlightImages = [ 'images/closedfolderhl.gif', 'images/closedfolderhl2.gif','images/closedfolderhl4.gif', 'images/closedfolder.gif' ];
      var currentHlColor=0;
      var currentHlImage=0;
      
      var numFolders = <%=String.valueOf(ccrs.size())%>;    
      var currentCcr=0;
      var thumbImageSize = ${thumbImageSize};
      var series;
      var singleFolderView = false;
      var initialOpenFolderIndex = 0;
      
      <logic:present name="singleFolderView">singleFolderView=true</logic:present>

      <%-- Regular CCRs --%>
      <tiles:insert page="ccrJavaScript.jsp">
        <tiles:put name="ccrs" beanName="ccrs"/>
        <tiles:put name="variableName">ccrs</tiles:put>
      </tiles:insert>

      if(window.parent!=null) {
        if(window.parent.showToolPalette != null) {
          window.parent.showToolPalette();
        }
      }

      <logic:present name="desktop" property="currentCcr">      
        window.parent.setAdvertisedCcr('${desktop.ownerMedCommonsId}','${desktop.accessPin}');
      </logic:present>

      <%-- desktop may be hidden by default so ensure now it is visible --%>
      window.parent.unhideTab('tab5');

    </script>
  </head>
  <body leftmargin="0" topmargin="0" marginheight="0" marginwidth="0" onLoad="if(window.parent){window.parent.highlightTabById('tab5');} initialize();" onKeyDown="handleKey(event);"> 
    <%-- The Hipaa Log --%>
    <%--
      Removed 10/13/2005 for initial release. 
    <tiles:insert page="hipaa.jsp"/>
    --%>

    <form name="ccrForm" style="display: none;" method="post">
      <input type="hidden" name="ccrIndex"/>
    </form>
    <%--
      Removed 10/13/2005 for initial release. 
    <div id="contactsbar">
      <tiles:insert page="contactsList.jsp"/>
    </div>
    --%>
      <div id="folderArea">
        <div id="allFolders">

          <%-- The pending CCRs, if there are any --%>
          <c:set var="closedFolderXOffset" value="0"/>
          <c:set var="closedFolderYOffset" value="0"/>
          <c:if test='${!empty desktop.pendingCcrs}'>
            <div id="pending-ccrRecord-0" style="display: block; position: absolute; top: 0; left: 12; z-index: 59;">      
              <div id="pending-folder-0" class="pendingFolder">
                <div id="pending-folderTab-0" class="pendingFolderTab" onclick="togglePendingFolder();">
                  <img id="pending-foldertabbutton-0" class="folderTabButton" src="images/minusbutton.png"/>
                  &#160;&#160;<span id="pending-creationDate-0">Unvalidated</span>
                </div>
                <tiles:insert page="thumbnailContainer.jsp">
                  <tiles:put name="maxThumbs" value="20"/>
                  <tiles:put name="index" value="0"/>
                  <tiles:put name="idPrefix" value="pending-"/>
                  <tiles:put name="enclosures" value="${desktop.pendingSeriesCount} Unvalidated Item(s)"/>
                </tiles:insert>
                <div id="pending-moreButton-0" class="moreButton"><img src="images/more.gif" onclick="showWado(0,0,'pending-');"/></div>
              </div>
            </div>
            <c:set var="closedFolderXOffset" value="90"/>
            <c:set var="closedFolderYOffset" value="-80"/>
          </c:if>

          <c:if test='${!empty ccrs or !empty desktop.pendingCcrs }'>
          <c:import var="xslt" url="ccrFolder.xsl" />
          <c:import var="emptyCCRXml" url="EmptyCCR.xml"/>

          <%--
          <x:transform xslt="${xslt}" xml="${emptyCCRXml}">
            <x:param name="index" value="0"/>
            <x:param name="numSeries" value="8"/>
            <x:param name="creationDate"><dt:format pattern="MM/dd/yyyy">${ccr.createTimeMs}</dt:format></x:param>
            <x:param name="patientMedcommonsId" value="${medcommonsId}"/>
            <x:param name="xOffset" value="12"/> 
            <x:param name="idPrefix" value="open-"/>
          </x:transform>
          --%>
          <c:set var="openCcr" value="${desktop.currentCcr}"/>

          <tiles:insert page="ccrFolder.jsp"  flush="false">
            <tiles:put name="index" value="0"/>
            <tiles:put name="numSeries" value="${fn:length(openCcr.seriesList)}"/>
            <tiles:put name="idPrefix" value="open-"/>
            <tiles:put name="ccr" beanName="openCcr"/>
            <tiles:put name="xOffset" value="12"/>
            <tiles:put name="display" value="block"/>
            <tiles:put name="maxThumbs" value="60"/>
            <tiles:put name="showContent" value="false"/>
          </tiles:insert>
          <logic:iterate id="ccr" indexId="index" name="ccrs">
            <tiles:insert page="ccrFolder.jsp"  flush="false">
              <tiles:put name="index" value="${index}"/>
              <tiles:put name="numSeries" value="${fn:length(seriesList)}"/>
              <tiles:put name="idPrefix" value=""/>
              <tiles:put name="ccr" beanName="ccr"/>
              <tiles:put name="display" value="none"/>
              <tiles:put name="xOffset" value="${closedFolderXOffset}"/>
              <tiles:put name="yOffset" value="${closedFolderYOffset}"/>
            </tiles:insert>
          </logic:iterate>
          </c:if>
        </div>
      </div>
  </body>
</html>

