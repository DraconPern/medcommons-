<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons HIPAA Gadget

  This page renders a small display that allows creation of consents
  and fax cover sheets.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:choose>
  <%-- if there is a ccr available, always use the ccr patient id --%>
  <c:when test='${not empty actionBean and empty simulated }'>
    <c:set var='ccr' value='${actionBean.currentCcr}'/>
    <c:set var='hipaaPatientId' value='${ccr.patientMedCommonsId}'/>
  </c:when>
  <c:when test='${not empty simulated }'>
    <c:set var='hipaaPatientId' value='${desktop.ownerMedCommonsId}'/>
  </c:when>
  <c:otherwise>
    <c:set var='hipaaPatientId' value='${desktop.ownerMedCommonsId}'/>
  </c:otherwise>
</c:choose>

<c:choose>
  <c:when test='${embedded}'><c:set var='layoutName' value='/embeddedGadget.jsp'/></c:when>
  <c:otherwise><c:set var='layoutName' value='/gadgetBase.jsp'/></c:otherwise>
</c:choose>
<s:layout-render name="${layoutName}" title="HIPAA, Privacy and Consent">
  <s:layout-component name="body">
    <div id="privacyFeature">
      <c:if test='${not empty faxmsg}'>
        ${faxmsg}
      </c:if>
      <form name="coverForm" <c:if test='${empty simulated}'>action="<mc:config property="AccountServer"/>/../cover.php?createCover=true"</c:if> method="post" target="_new">
        <table>
          <tr><th>PHR Account Number <span class='reqfield'>*</span></th>
              <td><input type="text" name="accid" id="hippaPatientId" class="text" maxlength="19"
                value="<c:out value='${hipaaPatientId}'/>">
                </td></tr>
          <tr><th>Provider/Practice</th>
              <td><input type="text" name="coverProviderCode" class="text"
                value="<c:out value='${desktop.accountSettings.groupName}'/>"</td>
          </tr>
          <tr><th>Notification Email</th><td><input type="text" name="coverNotifyEmail" class="text"/></td></tr>

          <tr><th>Fax Title</th><td> <input type='text' name='title' class="text" value=''/></td></tr>
          <tr><th valign='top'>Fax Note</th>
              <td><textarea name='note' cols="40" rows="2"></textarea></td>
          </tr>

          <tr><th>PIN (5 Digits)</th>
              <td><input type="submit" name='fax' value="Print / Preview" style='float: right;'/><input type="text" class="text pin" size="5" name="coverPin" maxlength="5"/>
                  </td></tr>

        </table>
      </form>
    </div>
  </s:layout-component>
</s:layout-render>
