<%@ page language="java"%><%@ include file="taglibs.inc.jsp" %><s:layout-definition>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.modules.utils.*" %>
<%--

  MedCommons WADO Viewer JSP

  This file defines the overall structure and layout for the WADO viewer frame.

--%>
<c:if test='${empty ccr}'><c:set var="ccr" value="${actionBean.ccr}" scope="request"/></c:if>
<c:set var="currentCcrDOM" value="${ccr.JDOMDocument}" scope="request"/>

<%@page import="net.medcommons.router.services.wado.WADOImage2"%>
<html class='yui-skin-mc'>
   <head>
    <title>MedCommons WADO Viewer</title>
    <mc:base/>
    <meta name="viewport" content="width=1024,initial-scale=1.0" />
    <pack:style enabled='true'>
      <src>common.css</src>
      <src>WADO.css</src> 
      <src>forms.css</src> 
      <src>WADOLandscape.css</src>
      <src>yui-2.8.2r1/menu/assets/skins/mc/menu.css</src>
      ${css}
    </pack:style>

    <pack:script enabled='true'>
      <src>yui-2.8.2r1/yahoo-dom-event/yahoo-dom-event.js</src>
      <src>yui-2.8.2r1/container/container_core-min.js</src>
      <src>yui-2.8.2r1/menu/menu-min.js</src>
      <src>yui3/3.2.0/yui/yui-min.js</src> 
      <src>mochikit/MochiKit.js</src>
      <src>utils.js</src>
      <src>common.js</src>
      <src>WADO.js</src>
      <src>tools.js</src> 
      <src>forms.js</src>  
      ${scripts}
     </pack:script>
     <!-- 
    <script type="text/javascript" src="https://getfirebug.com/firebug-lite.js"></script>
     -->
    <script type="text/javascript">
    

      <jsp:include page="accountDocumentsJavascript.jsp"/> 
      <jsp:include page="patientJavascript.jsp"/> 
      <jsp:include page="ccrDetailJavaScript.jsp"/> 

    
	  var ccrIndex = ${ccrIndex};
      var features = {
              editMode: <mc:enabled name='viewer.editMode' flag='true'/>,
              editAndSendCCR: <mc:enabled name='viewer.menu.edit.andSendCCR' flag='true'/>,
              editAsNewCCR: <mc:enabled name='viewer.menu.edit.asNewCCR' flag='true'/>,
              createReply: <mc:enabled name='viewer.menu.edit.createReply' flag='true'/>,
              setEmergencyCCR: <mc:enabled name='viewer.menu.edit.setEmergencyCCR' flag='true'/>
      };
      
      var initShare = false;
      <c:if test='${share}'>initShare = true;</c:if>

      signal(events,"beforeInit");

      var cleanPatient = ${! empty param['clean'] or ! empty cleanPatient};
      var enableBilling = <mc:config property='EnableBilling' default="false"/>;
      var initialSeriesGuid = "${viewerForm.initialSeriesGuid}";
      var initialSeriesIndex = <c:choose><c:when test='${!empty viewerForm.initialSeriesIndex}'>${viewerForm.initialSeriesIndex}</c:when><c:otherwise>null</c:otherwise></c:choose>;
      setPatientTitle('${mc:jsEscape(ccr.patientGivenName)}', '${mc:jsEscape(ccr.patientFamilyName)}'); 
      var DEFAULT_MAX_SPRITE_HEIGHT = <%= WADOImage2.MAX_SPRITE_HEIGHT %>; 
      var MAX_SPRITE_HEIGHT =  isMobileBrowser() ? 8192 : DEFAULT_MAX_SPRITE_HEIGHT;
       
      var framed = (window.parent != this);
      currentThumb = ${viewerForm.selectedThumbnail};
      if(currentThumb > 0) {
        currentSeriesIndex = ${viewerForm.selectedThumbnail};
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
      var mobile = false;
      <c:if test='${mobile}'>mobile = true;</c:if>

      var cxp2Protocol = '<mc:config property="RemoteProtocol"/>';
      var cxp2Port =  '<mc:config property="RemotePort"/>';
      var cxp2Host =  '<mc:config property="RemoteHost"/>';
      var cxp2Path =  '<mc:config property="CXP2Path"/>';
      var groupId = '${desktop.accountSettings.groupId}';
      var groupName = '${mc:jsEscape(desktop.accountSettings.groupName)}';
      var tipState = ${desktop.accountSettings.tipState};

      initTabInfo(${ccrIndex},'${ccr.logicalType}','${ccr.storageMode}','view');

      var context = {
              cleanPatient : ${! empty param['clean'] or ! empty cleanPatient},
              enableBilling : <mc:config property='EnableBilling' default="false"/>
          };

      var view = {
          initialSeriesGuid : "${viewerForm.initialSeriesGuid}",
          initialSeriesIndex : <c:choose><c:when test='${!empty viewerForm.initialSeriesIndex}'>${viewerForm.initialSeriesIndex}</c:when><c:otherwise>null</c:otherwise></c:choose>,
          currentThumb: ${viewerForm.selectedThumbnail},
          currentSeriesIndex: 0,
          enableCacheDisplay : <bean:write name="viewerForm" property="cacheDisplay"/>
      };

      <%-- Support for CCRs - the current CCR info --%>
      var ccr = ccrs['${ccr.seriesList[0].mcGUID}'];
      var storageId = '${ccr.storageId}';
      var enableQuickReply = ${! empty showQuickReply};
      var auth='${desktop.authenticationToken}';
      <c:remove var='showQuickReply'/>
      ccr.guid = '${ccr.guid}';
      var ccrGuid = ccr.guid;
      <c:set var="patient" value="${ccr.patient}"/>
      ccr.trackingNumber = '${ccr.trackingNumber}';
      var accountsBaseURL = '<mc:config property="AccountsBaseUrl"/>';

      thumbnails[0]=new CCRThumbnail(ccr, p.studies[0].series[0]);      
      
      <%-- Initialize thumbnails --%>
      if(initialSeriesGuid == thumbnails[0].series.mcGUID) {
        currentSeriesIndex = 0;
        currentThumb = 0;
      }

      var thumbnailIndex = 1;
      //for(i=0;i<p.studies[0].series.length;i++) {
      for(i=p.studies[0].series.length-1;i>=0;--i) {
        <%-- use mime type to determine which sort of thumbnail to add --%>
        var series = p.studies[0].series[i];
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
          thumbnails[thumbnailIndex]=new WebReferenceThumbnail(p.studies[0].series[i]);
          log("Added WebReferenceThumbnail for series " + thumbnailIndex);
        }
        else { <%-- anything not dicom assumed to be generic 'document' --%>
          thumbnails[thumbnailIndex]=new DocumentThumbnail(p.studies[0].series[i]);
          log("Added DocumentThumbnail for series " + thumbnailIndex);
        }          

        // Check if the series is requested as the initial series. If so, record the index.
        if((initialSeriesGuid && (series.mcGUID == initialSeriesGuid)) && ((initialSeriesIndex == NaN) || (initialSeriesIndex == i))) {
          log("Found initial series = " + i);
          currentSeriesIndex = i;
          currentThumb = thumbnailIndex;
          if(mimeType=='application/dicom') {
              currentImage = 0;
          }
        }

        thumbnails[thumbnailIndex].series = series;
        log("Initialized thumbnail " + thumbnailIndex + " to " + thumbnails[thumbnailIndex].series);
        
        thumbnailIndex++;
      }

      if(currentSeriesIndex >= p.studies[0].series.length) {
        currentSeriesIndex = p.studies[0].series.length-1;
        currentThumb = currentSeriesIndex;
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

    ${inlineScripts}

  </head> 
  <body  onMousewheel="return captureMousewheel();"  class='' >

    ${prebodyHTML}
  
    <%-- main body div --%>
    <div id='ViewerArea' class="ViewerArea invisible">
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

      
      <%-- main zoom rectangle:  displayed on the main image while the user is dragging --%>
      <div id="mainImageZoomRect"></div>

      <%-- the main image area --%>
      <div id="mainImage" class="mainImage" style="z-index: 10;">
        <jsp:include page='WADOBody.jsp'/>
      </div>
    </div> <%-- ViewerArea --%>
 
    <jsp:include page='changeNotificationDiv.jsp'/>

    <div id='notificationsTableCache'>&nbsp;</div>
    
    <div id='thumbnailGrid' class='hidden'></div>
    
    ${postBodyHTML}
    
    <%--
        These dependencies allow use of YAHOO.util.Date.format
        however they are too heavy for this purpose (52k minimised).
        
        <src>yui-2.8.2r1/yahoo/yahoo.js</src>
        <src>yui-2.8.2r1/event/event-min.js</src>
        <src>yui-2.8.2r1/datasource/datasource.js</src> 
     --%>
      <pack:script enabled='true'>
        <src>yui-2.8.2r1/yuiloader/yuiloader.js</src>
    </pack:script>
  </body>
</html>
<c:remove var="cleanPatient"/>
</s:layout-definition>
