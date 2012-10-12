<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons Account Activity Widget

  This page renders a table showing updates to the account of the patient
  of the currently open / active CCR.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="ccrXml" value="${actionBean.currentCcr.JDOMDocument}"/>
<c:set var="ccr" value="${actionBean.currentCcr}"/>
<c:set var="phrUrl">access?g=${ccr.guid}&a=${desktop.ownerMedCommonsId}</c:set>
<c:set var="guid" value="${ccr.guid}"/>
<c:set var="ccrXml" value="${ccr.JDOMDocument}"/>
<c:set var="sex" value="${ccr.patientGender=='Male'?'M': ccr.patientGender=='Female'?'F':''}"/>
<c:if test='${(not first) or (not noheader)}'>
    <c:set var="hideTitle" value="true"/>
</c:if>
<mc:xnode bean='ccrXml' path='patientFirstNonMedCommonsId' name='nonMcPatientId'/>
<c:choose>
  <c:when test='${embedded}'><c:set var='layoutName' value='/embeddedGadget.jsp'/></c:when>
  <c:otherwise><c:set var='layoutName' value='/gadgetBase.jsp'/></c:otherwise>
</c:choose>
<s:layout-render name="${layoutName}" title="Account Activity" hideTitle="${hideTitle}">
  <s:layout-component name="head">
    <script type="text/javascript">
      var returnUrl = '${widgetReturnUrl}';
      var ccrGuid = '${ccr.guid}';
      var accessGuid = '${desktop.accessGuid}';
      if(accessGuid == '')
        accessGuid = null;

      <c:if test='${!embedded}'>
      if(returnUrl) {
        ce_connect('closeCCR', function(guid) {
          log("CCR " + guid + " closed. [ Active CCR = " + accessGuid + "]");
          if(guid == accessGuid) {
            log("Active CCR " + accessGuid + " closed.  Reverting to return url " + returnUrl);
            window.location.href=returnUrl;
          } 
        });
      }
      </c:if>

      function track(tn) {
        openCcrWindow('access?a=${desktop.ownerMedCommonsId}&t='+tn);
        return false;
      }
    
      addLoadEvent(function() {
        ce_signal('patientActivityOpened',
                  '<c:if test='${not empty nonMcPatientId}'><mc:xvalue bean="nonMcPatientId" path="idType"/>: <mc:xvalue bean="nonMcPatientId" path="idValue"/></c:if>',
                  '<mc:xvalue bean="ccrXml" path="patientFamilyName"/>',
                  '<mc:xvalue bean="ccrXml" path="patientGivenName"/>',
                  '${ccr.patientAge}', 
                  '${ccr.patientGender}',
                  '${ccr.patientMedCommonsId}' );
        });

      var originalSessionId = getCookie('JSESSIONID');

      <%-- Only monitor height ourselves if running stand alone and not embedded in a page --%>
      <c:if test='${!embedded}'>
        addLoadEvent(addHeightMonitor);
        function calculateHeight() {
          return _calculateHeight() + 40; // give extra room
        }
      </c:if>
      function showDetail(sessionId) {
        forEach($$('.detailEvents'+sessionId), function(tr) {
            if(hasElementClass(tr,'hidden')) {
              removeElementClass(tr,'hidden');
              $('expand'+sessionId).innerHTML='-';
            }
            else {
              addElementClass(tr,'hidden');
              $('expand'+sessionId).innerHTML='+';
            }
        });
        broadCastHeight();
      }
    </script>
    <style type="text/css">
      .hidden {
        display: none;
      }
      a.expandLink {
        text-decoration: none;
        font-weight: bold;
        color: #4b719e;
      }
    </style>
    
  </s:layout-component>
  <s:layout-component name="body">
  <c:set var='accountServer'><mc:config property="AccountServer"/>/../..</c:set>
  <c:if test='${!empty ccrXml}'>
      <c:if test='${!embedded}'>
        <b><mc:xvalue bean="ccrXml" path="patientGivenName"/> <mc:xvalue bean="ccrXml" path="patientFamilyName"/> 
         - <mc:xvalue bean="ccrXml" path="patientAge"/>${sex}</b>
        <p><c:if test='${not empty nonMcPatientId}'>
          <mc:xvalue bean="nonMcPatientId" path="idType"/> <b><mc:xvalue bean="nonMcPatientId" path="idValue"/></b>
        </c:if>
        </p>
      </c:if>
  </c:if>
  <div id="updatesTable">
    <table border="0" style="width: 95%; padding-left: 5px;" cellspacing="3">
      <tr><th>&nbsp;</th><th>Date</th><th>Activity</th><th>By</th><th>Tracking Number / PIN</th></tr>
    <c:choose>
      <c:when test='${!empty events}'>
            <c:forEach items="${actionBean.activitySessions}" var="as" varStatus="s">
              <c:if test="${s.index<7}">
                <c:set var="summary" value="${as.summary}"/>
                <tr class="row${s.index%2}">
                  <td align='center'><c:choose>
                      <c:when test='${fn:length(as.events)>1}'>
                      <a href='javascript:showDetail("${as.sessionId}");' id='expand${as.sessionId}' class='expandLink' onfocus='this.blur();'>+</a>
                      </c:when>
                      <c:otherwise>&nbsp;</c:otherwise>
                    </c:choose>
                  </td>
                  <td><mc:age>${summary.timeStampMs}</mc:age></td> 
                  <td style="text-align: left;"><c:out value="${summary.description}"/></td>
                  <td>
                    <c:choose>
                      <c:when test='${summary.sourceAccountId=="0000000000000000"}'>Anonymous User</c:when>
                      <c:otherwise>
                      <mc:translate spec='${summary.sourceAccount}'/>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <a href="${accountServer}/tracking/${summary.trackingNumber}" onclick="return track('${summary.trackingNumber}');" target="ccr"><mc:tracknum>${summary.trackingNumber}</mc:tracknum></a>
                    <c:if test='${not empty summary.pin}'> / ${summary.pin}</c:if>
                  </td>
                </tr>
                <c:forEach items='${as.events}' var='event'>
                <tr class="row${s.index%2} detailEvents detailEvents${as.sessionId} hidden">
                  <td>&nbsp;</td> 
                  <td>&nbsp;</td> 
                  <td style="text-align: left;"><c:out value="${event.description}"/></td>
                  <td>
                    <c:choose>
                      <c:when test='${event.sourceAccountId=="000000000000000"}'>Anonymous User</c:when>
                      <c:otherwise>
                      <mc:translate spec='${event.sourceAccount}'/>
                      </c:otherwise>
                    </c:choose>
                  </td>
                  <td>
                    <a href="${accountServer}/tracking/${event.trackingNumber}" onclick="return track('${event.trackingNumber}');" target="ccr"><mc:tracknum>${event.trackingNumber}</mc:tracknum></a>
                    <c:if test='${not empty event.pin}'> / ${event.pin}</c:if>
                  </td>
                </tr>
                </c:forEach>
                <c:set var="guid" value='${changeSet.source}'/>
              </c:if>
            </c:forEach>
      </c:when>
      <c:otherwise>
        <tr><td colspan="4">No activity recorded for this account.</td></tr>
      </c:otherwise>
    </c:choose>
      </table>
    </div>
  </s:layout-component>
</s:layout-render>
