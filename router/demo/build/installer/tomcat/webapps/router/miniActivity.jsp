<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons Account Small Activity Widget

  This page renders a table showing updates to the account of the patient
  of the currently open / active CCR.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="ccr" value="${actionBean.currentCcr}"/>
<c:set var="ccrXml" value="${ccr.JDOMDocument}"/>
<c:set var="phrUrl">access?g=${ccr.guid}&a=${desktop.ownerMedCommonsId}</c:set>
<c:set var="guid" value="${ccr.guid}"/>
<c:set var="sex" value="${ccr.patientGender=='Male'?'M': ccr.patientGender=='Female'?'F':''}"/>
<mc:xnode bean='ccrXml' path='patientFirstNonMedCommonsId' name='nonMcPatientId'/>
    <script type="text/javascript">
      var returnUrl = '${widgetReturnUrl}';
      var ccrGuid = '${ccr.guid}';
      var accessGuid = '${desktop.accessGuid}';
      if(accessGuid == '')
        accessGuid = null;

      function track(tn) {
        openCcrWindow('access?a=${desktop.ownerMedCommonsId}&t='+tn);
        return false;
      }
    
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
      }
    </script>
    <c:set var='gwUrl'><mc:config property="RemoteAccessAddress"/></c:set>
    <c:set var='httpUrl' value='${fn:replace(gwUrl,"https://","http://")}'/>
    <style type="text/css">
      .hidden {
        display: none;
      }
      a.expandLink {
        text-decoration: none;
        font-weight: bold;
        color: #4b719e;
      }
      table tr td,  table tr th {
          font-size: 10px;
          text-align: left;
      }
      #updatesTable {
          margin-top: 5px;
      }
      #updatesTable table {
          width: 85%;
          padding: 0px;
          margin: 0px;
      }
      p#links {
          font-size: 12px;
          color: gray;
          margin: 0px 0px;
      }
      #updatesTable table tr th {
          text-align: left;
          font-weight: bold;
          color: #444;
          background-image: url('${httpUrl}/images/lbgrad.png'); 
     }
    </style>
  <c:set var='accountServer'><mc:config property="AccountServer"/>/../..</c:set>
  
  <h4 id='gadgetTitle'>Activity for 
      <mc:xvalue bean="ccrXml" path="patientGivenName"/> <mc:xvalue bean="ccrXml" path="patientFamilyName"/> 
         <c:if test='${not empty sex}'>-</c:if> <mc:xvalue bean="ccrXml" path="patientAge"/>${sex}
  </h4> 
  <p id='links'>
    <a href='${accountServer}/${ccr.patientMedCommonsId}' target='_new'><img src='${httpUrl}/images/hurl.png'/><span style='text-decoration: none;'> </span>HealthURL</a>
    |<a href='${accountServer}/acct/home.php' target='_new'><img style='position: relative; top:2px;' src='${httpUrl}/images/arrow_up_2.gif'/><span style='text-decoration: none;'> </span>Dashboard</a>
  </p> 
  <c:if test='${not empty nonMcPatientId}'>
      <p>
        <mc:xvalue bean="nonMcPatientId" path="idType"/> <b><mc:xvalue bean="nonMcPatientId" path="idValue"/></b>
      </p>
  </c:if>
  <div id="updatesTable">
    <table border="0" style="padding-left: 5px;" cellspacing="3">
      <tr><th>Date</th><th>Activity</th><th>By</th><th class='tracking'>Tracking</th></tr> 
    <c:choose>
      <c:when test='${!empty events}'>
            <c:forEach items="${actionBean.activitySessions}" var="as" varStatus="s">
              <c:if test="${s.index<7}">
                <c:set var="summary" value="${as.summary}"/>
                <tr class="row${s.index%2}">
                  <td><mc:age>${summary.timeStampMs}</mc:age></td> 
                  <td style="text-align: left;"><c:out value="${summary.description}"/></td>
                    <c:choose>
                      <c:when test='${summary.sourceAccountId=="0000000000000000"}'>Anonymous</c:when>
                      <c:otherwise>
                      <c:set var='src'><mc:translate spec='${summary.sourceAccount}'/></c:set>
                      </c:otherwise>
                    </c:choose>
                  <td title='<c:out value="${src}"/>'>
                      <c:out value='${mc:trunc(src,18)}'/>
                  </td>
                  <td class='tracking'>
                    <a href="${accountServer}/tracking/${summary.trackingNumber}" onclick="return track('${summary.trackingNumber}');" target="ccr"><mc:tracknum>${summary.trackingNumber}</mc:tracknum></a>
                  </td>
                </tr> 
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