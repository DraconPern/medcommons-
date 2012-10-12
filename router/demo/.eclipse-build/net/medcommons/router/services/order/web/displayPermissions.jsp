<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons CCR Sharing / Consents Display Widget

  This page renders a read only display showing the permissions that the
  current gateway user has for the account of the patient of the CCR
  that they currently have open in the browser.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="patientId"><mc:medcommonsId>${ccr.patientMedCommonsId}</mc:medcommonsId></c:set>
<c:choose>
  <c:when test='${embedded}'><c:set var='layoutName' value='/embeddedGadget.jsp'/></c:when>
  <c:otherwise><c:set var='layoutName' value='/gadgetBase.jsp'/></c:otherwise>
</c:choose>
<s:layout-render name="${layoutName}" title="Access Authorizations for Account ID ${patientId}">
  <s:layout-component name="head">
    <script type="text/javascript">
      var returnUrl = '${param["returnUrl"]}';
      ce_connect('closeCCR', function() {
        if(returnUrl != '') {
          window.location.href = returnUrl;
        }
      });
    </script>
  </s:layout-component>
  <s:layout-component name="body">
    <div id="privacyWidget">
      <c:set var='ccr' value='${actionBean.currentCcr}'/>
      <c:choose>
        <c:when test='${not empty actionBean.sharedRights}'>
          <a id='dlAllLink' style='float:right; margin-right: 20px;' href="javascript:document.hipaaDownloadForm.submit();">Download All Documents</a>
          <p>You have <b>${actionBean.sharedRights}</b> access to 
          <c:choose>
            <c:when test='${not empty ccr.patientFamilyName}'>the account of <c:out value="${ccr.patientGivenName}"/> <c:out value="${ccr.patientFamilyName}"/></c:when>
            <c:otherwise>Account ${patientId}</c:otherwise>
          </c:choose>
          .
          </p>
        </c:when>
        <c:otherwise>
          <p>The patient of the currently open CCR is not sharing their PHR with you.</p>
        </c:otherwise>
      </c:choose>
      <div style="display:none;">
         <form name="hipaaDownloadForm" action="PersonalBackup" method="post" target="_new">
             <input type="hidden" class="text" size="5" name="storageId" value="${ccr.patientMedCommonsId}"/>&nbsp;&nbsp;&nbsp;
             <input type="submit" value="Get My Records"/>
          </form>
      </div>
    </div>
  </s:layout-component>
</s:layout-render>
