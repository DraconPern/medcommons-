<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons CCR Widget

  This page renders a small widget intended for embedding
  on external pages.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="ccrXml" value="${ccr.JDOMDocument}"/>
<c:set var="history" value="${ccr.lastChangeNotifications}"/>
<c:set var="creationDateTime"><dt:format pattern="MM/dd/yyyy K:mm a">${ccr.createTimeMs}</dt:format></c:set>
<c:set var="creationDate"><dt:format pattern="MM/dd/yyyy">${ccr.createTimeMs}</dt:format></c:set>
<c:set var="sex" value="${ccr.patientGender=='Male'?'M': ccr.patientGender=='Female'?'F':''}"/>
<mc:xnode bean='ccrXml' path='patientFirstNonMedCommonsId' name='nonMcPatientId'/>
<c:choose>
  <c:when test='${embedded}'><c:set var='layoutName' value='/embeddedGadget.jsp'/><c:set var="hideTitle" value="true"/></c:when>
  <c:otherwise><c:set var='layoutName' value='/gadgetBase.jsp'/></c:otherwise>
</c:choose>
<s:layout-render name="${layoutName}" title="Current CCR" hideTitle="${hideTitle}">
  <s:layout-component name="head">
    <style type="text/css">
      #ccrheaderLinks img {
        padding: 2px 4px;
        position: relative;
        top: 3px;
      }
    </style>
    <script type="text/javascript">
      //addLoadEvent(function() { if($('ccrheader'))roundElement('ccrheader'); });
      var ccrGuid = '${ccr.guid}';
      var accessGuid = '${desktop.accessGuid}';
      if(accessGuid == '')
        accessGuid = null;

      var openInProgress = false;
      function cccr_onCloseCCR(guid) {
        if(openInProgress) {
          log("Ignoring close CCR event: new open in progress");
          return;
        }

        log("Received closeCCR in for guid " + guid);

        if(guid == accessGuid) {
          log("CCR " + accessGuid + " closed.  Reverting to return url");
          window.location.href="<mc:config property='AccountServer'/>/../CCCRGadget.php?cleargw=true";
        } 
      }

      function cccr_onOpenCCR(guid) {
          <%-- if different guid opened, return to account page where we will get sent to correct gateway --%>
          if(guid != accessGuid) {
           window.location.href="<mc:config property='AccountServer'/>/../CCCRGadget.php";
           //log("Opened new CCR " + guid + " replacing old CCR " + accessGuid);  
          }
          else {
            log("Finished open");
            openInProgress = false;
          }
      }

      function openCcrWindow(url) {
        // Attempt to deal with FF's moronic tab handling - if there is a CCR tab, KILL IT
        // Unfortunately, this nutso javascript drives IE beserk
        // so, hack upon hack, only do this for FireFox itself.
        if(isFireFox) {
          var ccr = window.open('','ccr');
          ccr.close();
        }
        //window.pendingCCROpen = function() { window.ccr = window.open(url,'ccr');};
        //window.setTimeout(pendingCCROpen, 100);
        window.ccr = window.open(url,'ccr');
        openInProgress = true;
        return false;
      }

      function init() {
        <c:if test='${!embedded}'>
          ce_connect('closeCCR',cccr_onCloseCCR);
          ce_connect('openCCR',cccr_onOpenCCR);
          addHeightMonitor();
        </c:if>
        <c:set var='accountServer'><mc:config property='AccountServer'/></c:set>
        ce_add_server('/ce_signal.php');
        ce_signal('newPatient',
                  '${ccr.patientMedCommonsId}',
                  '<c:out value="${ccr.patientGivenName}"/>',
                  '<c:out value="${patientFamilyName}"/>',
                  '${patientAge}',
                  '${patientGender}',
                  '<c:out value='${ccr.patientEmail}'/>' );
      }

      var originalSessionId = getCookie('JSESSIONID');
      addLoadEvent(init);
    </script>
  </s:layout-component>
  <s:layout-component name="body">
    <s:layout-render name='/healthURL.jsp' mcid='${ccr.patientMedCommonsId}'/>
    <c:set var="createUrl">tracking.jsp?tracking=new&accid=${desktop.ownerMedCommonsId}&auth=${desktop.authenticationToken}</c:set>
    <c:set var="importUrl">tracking.jsp?tracking=import&accid=${desktop.ownerMedCommonsId}&auth=${desktop.authenticationToken}</c:set>
    <c:if test='${!empty ccrXml}'>
      <div id="ccrheader">
        <c:if test='${embedded}'>
          <div id='ccrheaderLinks'>
            <c:if test='${! empty ccr.guid}'>
            <a href="${hurl}" onfocus="blur();" title="Open/Edit CCR" target="ccr" onclick="return openCcrWindow('${hurl}');"><img src='images/hurl.png'/></a>&nbsp;<a href="${hurl}" onfocus="blur();" title="Open/Edit CCR" target="ccr" onclick="return openCcrWindow('${hurl}');">HealthURL</a>
            </c:if>
            <c:if test='${desktop.ownerMedCommonsId == ccr.patientMedCommonsId}'>
                |
                <a href="${importUrl}" onfocus="blur();" title="Import a new CCR" target="ccr" onclick="return openCcrWindow('${importUrl}');">Import</a>
                |
                <a href="${createUrl}" onfocus="blur();" title="Create a new CCR" target="ccr" onclick="return openCcrWindow('${createUrl}');">Create</a>
            </c:if>
          </div>
        </c:if>
        <h5><c:if test='${!embedded}'><mc:xvalue bean="ccrXml" path="patientFamilyName"/> <mc:xvalue bean="ccrXml" path="patientGivenName"/> - <mc:xvalue bean="ccrXml" path="patientAge"/>${sex}</c:if></h5>
        <table cellspacing="5" style="clear: both;" width="100%">
          <tr>
            <th width="72">ID:</th>
            <td>
              <c:choose>
                <c:when test='${not empty nonMcPatientId}'><mc:xvalue bean="nonMcPatientId" path="idType"/> - <mc:xvalue bean="nonMcPatientId" path="idValue"/></c:when>
                <c:when test='${not empty ccr.patientMedCommonsId}'>#<mc:medcommonsId>${ccr.patientMedCommonsId}</mc:medcommonsId> - MedCommons Account Id</c:when>
              </c:choose>
            </td>
          </tr>
          <tr><th>Current CCR:</th><td>Size = 
            <c:choose>
              <c:when test='${ccr.totalLoadedSizeBytes lt 10000000 }'>
                <fmt:formatNumber maxFractionDigits="1">${ccr.totalLoadedSizeBytes / 1024}</fmt:formatNumber> KB
              </c:when>
              <c:otherwise>
                <fmt:formatNumber maxFractionDigits="1">${ccr.totalLoadedSizeBytes / 1048576}</fmt:formatNumber> MB
              </c:otherwise>
            </c:choose></td></tr>
              <tr>
                <th>Activity:</th>
                <c:choose>
                <c:when test='${fn:length(history)>0}'>
                  <td>${fn:length(history)} Update<c:if test="${fn:length(history)>1}">s</c:if>,
                  <c:choose>
                  <c:when test='${history[0].age < 0}'>Unknown time ago</c:when>
                  <c:when test='${history[0].age > 86400000}'>
                    <fmt:formatNumber type="number" maxFractionDigits="0">${history[0].age / 86400000}</fmt:formatNumber> days 
                  </c:when>
                  <c:otherwise>
                    <fmt:formatNumber type="number" maxFractionDigits="0">${(history[0].age % 86400000)/3600000}</fmt:formatNumber> hrs 
                    <fmt:formatNumber type="number" maxFractionDigits="0">${(history[0].age % 3600000)/60000}</fmt:formatNumber> mins
                  </c:otherwise>
                </c:choose>
                ago
                </div>

                </td>
            </c:when>
            <c:otherwise>
              <td>
                <c:if test='${!embedded}'>
                <a style="float: right;" href="<mc:config property='AccountServer'/>/../expandFeature.php?feature=currentccr">History</a>
                </c:if>
                Created on ${creationDate}
              </td>
            </c:otherwise>
          </c:choose>
          </tr>
          <tr id="eccr">
            <c:set var="eccrGuid" value="${actionBean.patientSettings.emergencyCcrGuid}"/>
            <c:choose>
              <c:when test='${! empty eccrGuid}'>
                <th valign="top"><a href="access?g=${eccrGuid}&a=${desktop.ownerMedCommonsId}" onfocus="blur();" title="Open/Edit CCR" target="ccr"><img title="Open/Edit CCR" border="0" src="images/smalleccr.gif"/></a></td>
                <td>Emergency CCR Available</td>
              </c:when>
              <c:otherwise>
                <th><img title="No Emergency CCR has been set.  Open or Create a CCR to set it as your Emergency CCR" border="0" src="images/smalleccr.gif"/></th>
                <td>No Emergency CCR Set</td>
              </c:otherwise>
            </c:choose>
          </tr>
        </table>
        <%-- &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;${creationDateTime} --%>
      </div>
    </c:if>
    <c:if test='${empty ccrXml}'>
      No CCR currently open.
    </c:if>
  </s:layout-component>
</s:layout-render>

