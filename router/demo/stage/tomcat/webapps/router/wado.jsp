<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004-2005 MedCommons Inc.   All Rights Reserved.
-->
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.modules.utils.*" %>
<%@ include file="/taglibs.inc.jsp" %>
<%--

  MedCommons WADO Viewer JSP

  This file defines the overall structure and layout for the WADO viewer frame.

--%>
<bean:define id="formType" name="viewerForm" property="formType"/>
<bean:define id="seriesList" name="ccr" property="seriesList" type="java.util.List"/>
<c:set var="currentCcrDOM" value="${ccr.JDOMDocument}" scope="request"/>
<html>
   <head>
    <title>MedCommons WADO Viewer</title>
    <pack:style>
      <src>common.css</src>
      <src>WADO.css</src>
      <src>WADOLandscape.css</src>
    </pack:style>

    <pack:script>
      <src>mochikit/MochiKit.js</src>
      <src>utils.js</src>
      <src>common.js</src>
      <src>WADO.js</src>
    </pack:script>
    
    <script type="text/javascript">
      <bean:define id="selectedThumb" name="viewerForm" property="selectedThumbnail" type="Integer"/>
      var cleanPatient = ${! empty param['clean'] or ! empty cleanPatient};
      var enableBilling = <mc:config property='EnableBilling' default="false"/>;
      var WADOFormType ="WADO";
      var OrderFormType="ORDER";
      var FormType = "<%=formType%>";
      var initialSeriesGuid = "${viewerForm.initialSeriesGuid}";
      var initialSeriesIndex = <c:choose><c:when test='${!empty viewerForm.initialSeriesIndex}'>${viewerForm.initialSeriesIndex}</c:when><c:otherwise>null</c:otherwise></c:choose>;
      window.parent.currentTab.ccrIndex = ${ccrIndex};
      window.parent.currentTab.mode = 'view';
      setPatientTitle('${mc:jsEscape(ccr.patientGivenName)}', '${mc:jsEscape(ccr.patientFamilyName)}'); 

      <mc:enabled name='viewer.editMode'>
	      window.parent.setTabIcon(null, '<img class="tabIcon" title="Switch to Edit Mode" style="position: relative; top: 1px; left: 3px;" src="images/tancycle.gif" onmouseout="this.src=\'images/tancycle.gif\'" onmouseover="this.src=\'images/redcycle.gif\'"/>');
      </mc:enabled>

      currentThumb = <%=selectedThumb.intValue()%>;
      if(currentThumb > 0) {
        currentSeries = <%=selectedThumb.intValue()%>;
      }
      var version='<%=net.medcommons.Version.getVersionString()%>';
      var buildDate='<%=net.medcommons.Version.getBuildTime()%>';

      var imageHeight = <bean:write name="viewerForm" property="imageHeight"/>;
      var imageWidth = 720;
      var tableWidth = imageWidth +2;
      maxRows=imageHeight;
      maxColumns=imageWidth;
      var editOnLoad='${param["editOnLoad"]}';
      var accId = '${desktop.ownerMedCommonsId}';
      var displayMode = '${ccr.displayMode}';

      var cxp2Protocol = '<mc:config property="RemoteProtocol"/>';
      var cxp2Port =  '<mc:config property="RemotePort"/>';
      var cxp2Host =  '<mc:config property="RemoteHost"/>';
      var cxp2Path =  '<mc:config property="CXP2Path"/>';
      var groupId = '${desktop.accountSettings.groupId}';
      var groupName = '${mc:jsEscape(desktop.accountSettings.groupName)}';

      var features = {
              editMode: <mc:enabled name='viewer.editMode' flag='true'/>,
              editAndSendCCR: <mc:enabled name='viewer.menu.edit.andSendCCR' flag='true'/>,
              editAsNewCCR: <mc:enabled name='viewer.menu.edit.asNewCCR' flag='true'/>,
              createReply: <mc:enabled name='viewer.menu.edit.createReply' flag='true'/>,
              setEmergencyCCR: <mc:enabled name='viewer.menu.edit.setEmergencyCCR' flag='true'/>
      };

      initTabInfo(${ccrIndex},'${ccr.logicalType}','${ccr.storageMode}','view');

      <jsp:include page="accountDocumentsJavascript.jsp"/>

    </script>

    <tiles:insert page="patientJavascript.jsp" flush="true"/>

    <script type="text/javascript">
      <tiles:insert page="ccrDetailJavaScript.jsp"/>
    </script>

    <script type="text/javascript">

      <%--var showAttributions = <c:choose><c:when test='${empty current.attributions}'>false</c:when><c:otherwise>true</c:otherwise></c:choose>;--%>
      <%--
      var showAttributions = ${!empty ccr.attributions};
      if(showAttributions) {
        window.open('attributions.jsp','attributions','height=200,width=300,toolbar=no,menubar=no');
      }
      --%>

      <%-- Support for CCRs - the current CCR info --%>
      var ccr = ccrs['${ccr.seriesList[0].mcGUID}'];
      var storageId = '${ccr.storageId}';
      var enableQuickReply = ${! empty showQuickReply};
      var auth='${desktop.authenticationToken}';
      <c:remove var='showQuickReply'/>
      ccr.guid = '${ccr.guid}';
      var ccrGuid = ccr.guid;
      <c:set var="patient" value="${ccr.patient}"/>
      <logic:present name="ccr">      
        window.parent.setAdvertisedCcr('${desktop.ownerMedCommonsId}','${desktop.accessPin}');
        ccr.trackingNumber = '${ccr.trackingNumber}';
      </logic:present>

      <%-- Initialize thumbnails --%>
      //thumbnails[0]=new CCRThumbnail(ccr);
      thumbnails[0]=new CautionThumbnail(ccr, p.StudiesArray[0].SeriesArray[0]);
      if(initialSeriesGuid == thumbnails[0].series.mcGUID) {
        currentSeries = 0;
        currentThumb = 0;
      }

      var thumbnailIndex = 1;
      //for(i=0;i<p.StudiesArray[0].SeriesArray.length;i++) {
      for(i=p.StudiesArray[0].SeriesArray.length-1;i>=0;--i) {
        <%-- use mime type to determine which sort of thumbnail to add --%>
        var series = p.StudiesArray[0].SeriesArray[i];
        var mimeType=series.mimeType;
        log("Series " + i + " has mime type " + mimeType);

        if(mimeType=='application/x-ccr+xml' && (i==0)) {
          continue;
        }
        else
        if(mimeType=='application/x-ccr+xml') {
          thumbnails[thumbnailIndex]=new CCRThumbnail(ccrs[series.mcGUID]);
          log("Added CCRThumbnail for series " + thumbnailIndex);
        }
        else
        if(mimeType=='application/dicom') {
          thumbnails[thumbnailIndex]=new SeriesThumbnail(i);
          log("Added SeriesThumbnail for series " + thumbnailIndex);
        }
        else
        if(mimeType=='URL') {
          thumbnails[thumbnailIndex]=new WebReferenceThumbnail(p.StudiesArray[0].SeriesArray[i]);
          log("Added WebReferenceThumbnail for series " + thumbnailIndex);
        }
        else { <%-- anything not dicom assumed to be generic 'document' --%>
          thumbnails[thumbnailIndex]=new DocumentThumbnail(p.StudiesArray[0].SeriesArray[i]);
          log("Added DocumentThumbnail for series " + thumbnailIndex);
        }          

        // Check if the series is requested as the initial series. If so, record the index.
        if((initialSeriesGuid && (series.mcGUID == initialSeriesGuid)) && ((initialSeriesIndex == NaN) || (initialSeriesIndex == i))) {
          log("Found initial series = " + i);
          currentSeries = i;
          currentThumb = thumbnailIndex;
        }

        thumbnails[thumbnailIndex].series = series;
        log("Initialized thumbnail " + thumbnailIndex + " to " + thumbnails[thumbnailIndex].series);
        
        thumbnailIndex++;
      }

      if(currentSeries >= p.StudiesArray[0].SeriesArray.length) {
        currentSeries = p.StudiesArray[0].SeriesArray.length-1;
        currentThumb = currentSeries;
      }

      <%-- WADO Order Entries.  These are declared here but added in the footer where the menu is written --%>
      var orders = new Array();
      imageCache = new ImageCache(<bean:write name="viewerForm" property="imageCacheSize"/>);

      enableCacheDisplay = <bean:write name="viewerForm" property="cacheDisplay"/>;

      var enableSimTrak = false;
      <mc:hasApp accountId='${ccr.patientMedCommonsId}' code='SIMTRAK'> 
        enableSimTrak = <mc:config property="EnableSimTrak" default="false"/>;
      </mc:hasApp>
      var simtrakURL = '<mc:config property="SimtrakURL" default="https://simtrak.medcommons.net/Env/stviewer.php"/>';
      var simtrakAdminURL = '<mc:config property="SimtrakAdminURL" default="https://simtrak.medcommons.net/Env/envision.php"/>';

      var loadedGuid = '${ccr.guid}';
      var storageMode = '${ccr.storageMode}';
      var remoteAccessAddress = '<mc:config property="RemoteAccessAddress"/>';
      <c:if test='${ ! empty ccr.guid }'>
        window.parent.ce_signal('openCCR',ccr.guid,remoteAccessAddress);
      </c:if>
      var touUrl = '<mc:config property="acTermsOfUseUrl" default="http://www.medcommons.net/termsofuse.php"/>';
      var TERMS_OF_USE_IMG_URL = '<mc:config property="acTermsOfUseLogo" default="images/caution.png"/>';

      <c:set var='isCurrentCCR' value='${not empty currentCcrGuid and (currentCcrGuid == ccr.guid)}'/>

      addLoadEvent(Initialize);

      var changeNotifications = null;
      <c:if test='${isCurrentCCR and  !empty changeNotifications}'>
         changeNotifications = ${changeNotifications};
          <%-- Don't show change notifications for patients who are still having data uploaded --%>
          if((changeNotifications.length>0) && (ccr.patient.status != 'INCOMPLETE')) {
             addLoadEvent(showChangeNotifications);
          }
      </c:if>

    </script>    

  </head> 
  <body 
      <logic:equal name="formType" value="WADO">
        onkeydown="handleKeyDown(event);"  
        onkeypress="handleKeyPress(event);"  
        onMousewheel="return captureMousewheel();" 
        ondblclick="javascript:handleDoubleClick();"
      </logic:equal>
        class='yui-skin-sam'
        >
    <%-- main body div --%>
    <div id='ViewerArea' class="ViewerArea invisible">
      <%-- thumbnails --%>
      <tiles:insert page="orderthumb.jsp">
        <tiles:put name="thumbIndex" value="0"/> 
      </tiles:insert>
      <tiles:insert page="thumbnail.jsp">
        <tiles:put name="seriesList" beanName="seriesList"/>
        <tiles:put name="seriesIndex" value="0"/> 
        <tiles:put name="thumbIndex" value="1"/> 
      </tiles:insert>
      <tiles:insert page="thumbnail.jsp">
        <tiles:put name="seriesList" beanName="seriesList"/>
        <tiles:put name="seriesIndex" value="1"/> 
        <tiles:put name="thumbIndex" value="2"/> 
      </tiles:insert>
      <tiles:insert page="thumbnail.jsp">
        <tiles:put name="seriesList" beanName="seriesList"/>
        <tiles:put name="seriesIndex" value="2"/> 
        <tiles:put name="thumbIndex" value="3"/> 
      </tiles:insert>

      <%-- pager --%>
      <%--
        <img id="pageuparrow" style="display: none;" src="images/arrow_up.gif" onclick="gotoPreviousPage();" style="cursor: hand" alt="Previous Page"/>
        <img id="pagedownarrow" style="display: none;" src="images/arrow_down.gif" onclick="gotoNextPage();" style="cursor: hand" alt="Next Page"/>
      --%>
      <div id="pager">
        <div id='pagerLinks' style='font-size: 12px; color: #cccccc; font-weight: bold;'>
          <%--
          Reference:&nbsp;&nbsp;
          <logic:iterate name="seriesList" id="theSeries" indexId="index">
            <bean:define id="index" name="index" type="java.lang.Integer"/>
            <% MCSeries series = (MCSeries) seriesList.get(index.intValue()); %>
            <span id='pagerCell<%=index%>'><a title='Series <%=(index.intValue()+1)%> - <%= series.SeriesDescription + " - " + series.size() + " images "%>' id='pagerLink<%=index%>' class='pagerLink' href='javascript:displaySelectedSeries(<%=index%>);' onfocus='this.blur();'><%=(index.intValue()+1)%></a>&nbsp;</span>
          </logic:iterate>
            <% int index = seriesList.size(); %>
            <span id='pagerCell<%=index%>'><a title='' style="display: none;" id='pagerLink<%=index%>' class='pagerLink' href='javascript:displaySelectedSeries(<%=index%>);' onfocus='this.blur();'><%=(index+1)%></a>&nbsp;</span>
            <span id='pagerCell<%=++index%>'><a title='' style="display: none;" id='pagerLink<%=index%>' class='pagerLink' href='javascript:displaySelectedSeries(<%=index%>);' onfocus='this.blur();'><%=(index+1)%></a>&nbsp;</span>
        --%>
        <div style='font-size: 11px; color: #cccccc; font-weight: bold;'>Reference:&nbsp;&nbsp;</div>
          <table border='0' cellpadding='0' cellspacing='0' id='pagerTable'><tr>
          <% int thumbnailIndex=0; %>
          <logic:iterate name="seriesList" id="theSeries" indexId="index">
            <bean:define id="index" name="index" type="java.lang.Integer"/>
            <% MCSeries series = (MCSeries) seriesList.get(index.intValue()); %>
            <c:if test="${index > 0}">
              <td id='pagerCell<%=index%>'>
              <a title='Series <%=index%> - <%= Str.escapeHTMLEntities(series.SeriesDescription) + " - " + series.size() + " images "%>' 
                 id='pagerLink<%=index%>' 
                 class='pagerLink' 
                 href='javascript:displaySelectedSeries(<%=index%>);' 
                 onfocus='this.blur();'><%=index%></a>
              <% thumbnailIndex++; %>
              </td>
              <c:if test='${index%9 == 0}'></tr><tr></c:if>
            </c:if>
          </logic:iterate>
          </tr></table>
        </div>      
      </div>
      
      <div id="tools" class="tools">
        <tiles:insert page="tools.jsp"/>
      </div>

      <%-- footer/menus --%>
      <div id="footer" class="footer">
        <tiles:insert page="footer.jsp">
          <tiles:put name="seriesList" beanName="seriesList"/>
          <tiles:put name="order" beanName="order"/>
        </tiles:insert>
      </div>
    
      <%-- logo --%>
      <%-- not displayed any more --%>
      <%-- <div id="mclogo"><a href="http://www.medcommons.net"><span id="mclogoimage" style="cursor: hand"></span></a></div> --%>

      <%-- support for zoom/drag --%>
      <div id="bgmap" 
           style="position: absolute; left: 0px; top: 0px; width: 140px;"
           onmousedown="beginZoomRectangleDrag(this.parentNode, event);">
        <div id="zoomMap" style="position:absolute; left:-100; top:-100; width:1;">
          <img id="zoomRectangle" name="zoomRectangle" src="rect.gif" width="40" height="40" border="0"/>
        </div>
      </div>

      <%-- error pane - shows error messages to the user, otherwise hidden --%>
      <div id="errorPane" style="z-index: 100"><p id="errorPaneMessage"></p></div>

      <div id="speedPane" style="z-index: 50"><span id="speedAmount"></span><span id="speedPercent">&nbsp;</span></div>
      <div id="cachedPane" style="z-index: 50"><span id="cachedAmount"></span><span id="cachedPercent">&nbsp;</span></div>

      <%-- drag scroll pane - shows while user is doing a drag scroll --%>
      <div id="dragScrollLabel"><img id="dragScrollIcon" src="images/nresize.gif"/><span id="dragScrollTotalImages">&nbsp;</span></div>
      <div id="dragScrollOuter" onmouseover="rollOverDragScroll();" onmouseout="rollOutDragScroll();">
        <div id="dragScrollPane" onmousedown="dragScrollPaneMouseDown(event);" onmouseup="dragScrollPaneMouseUp(event);"><span id="dragScrollImageNum">125</span></div>
        <div id="dragScrollDisplay"></div>
        <%--
        <div id="dragScrollTab" onmousedown="dragScrollMouseDown();" onmouseup="dragScrollMouseUp();">&nbsp;</div>
        --%>
      </div>
      
      <%-- main zoom rectangle:  displayed on the main image while the user is dragging --%>
      <div id="mainImageZoomRect"></div>

      <%-- Support for XDS submission - contains hidden form to send content to submission servlet --%>
      <div id="xdsAddReportForm"><tiles:insert page="addReportPopup.jsp"/></div>
      <div id="xdsPleaseWait"><p style="font-family: helvetica;">Please wait while your document is being submitted...</p></div>
      <div id="xdsSubmitDiv" style="display: none;"><tiles:insert page="xdsSubmit.jsp"/></div>
      <div id="xdsSubmitButton"><input type="button" id="xdsSubmit" style="display: none;" name="xdsSubmit" onclick="submitXdsDocument();" value="Submit Document"/></div>

      <%-- the main image area --%>
      <div id="mainImage" class="mainImage" style="z-index: 10;">
        <% 
          String formPage=formType + "Body.jsp"; 
          log("formPage="+formPage);
        %>
        <tiles:insert page="<%=formPage%>"/>
      </div>
    </div> <%-- ViewerArea --%>
 
    <jsp:include page='changeNotificationDiv.jsp'/>

    <div id='notificationsTableCache'>&nbsp;</div>
    <script src="yui-2.6.0/yuiloader/yuiloader-min.js"></script> 
  </body>
</html>
<c:remove var="cleanPatient"/>
