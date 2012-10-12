<%@ include file="/taglibs.inc.jsp" %>
<%--
  Renders a table showing To, From, Subject, Tracking Number and PIN for a CCR, which must
  be set in the context (page, request, or session) under the name "ccr".

  The contents are expected to be placed within an outer table.

  Supports a "readonly" parameter to disable interaction with the fields.
--%>
<c:set var="warnMsg">Do not put patient identifiable information in this field.</c:set>
<c:set var='isCurrentCCR' value='${not empty desktop.accountSettings.currentCcrGuid and (desktop.accountSettings.currentCcrGuid == ccr.guid)}'/>
<c:set var="notificationSubjectText"><mc:config property="NotificationSubjectText" default="MedCommons Notification"/></c:set>
<c:set var="ccrDOM" value="${ccr.JDOMDocument}"/>
<s:layout-definition>
  <tr><th style="padding-top: 5px;"><img id='toggleToFromImg' src='images/toggle_inactive.png' title='Swap To and From'/> TO</th>
    <td class="ltrtxt" rowspan="3">
      <table class="notificationsTable" cellspacing="0" width="98%">
        <tr>
          <td width="50%" style="border-right:none;">
            <%-- note: div necessary, FF will not allow rel pos on table cell --%>
            <div id='toEmailWrapper' style="position: relative;">
            <mc:xinput styleClass="insecureFormInput"
              title="Enter addresses to send to. Separate multiple addresses with commas."
              id="toEmail"
              join=","
              bean="ccrDOM"
              name="toEmails"
              />
              <img class="secureImg" title="${warnMsg}" src="images/padlock.gif"/>
              <div id='toACContainer'></div>
            </div>
            <div id="toAcctMsg" title="This User will be pre-authorized and will not require a PIN">&nbsp;&nbsp;<img src="images/person-tiny.png"/>&nbsp;User will be Pre-Authorized</div>
            <div id="toGroupMsg" title="This Group will be pre-authorized and will not require a PIN">&nbsp;&nbsp;<img src="images/tiny-group.png"/>&nbsp;Group will be Pre-Authorized</div>
           </td>
          <td style="border-bottom: none;"  style="position: relative;">
            <div style="position: relative;">
             <c:if test='${not isCurrentCCR}'>
               <span style="float: right; padding: 3px; padding-right: 18px; position: relative;" id="trackingNumberContainer" class="insecure">
                <div id="trackingNumber">
                  <c:choose>
                    <c:when test='${ccr.displayMode ==  "eccr"}'><span style="color: red; font-weight: bold;">Emergency CCR</span></c:when>
                    <c:when test='${empty ccr.trackingNumber}'>New # to be Created</c:when>
                    <c:otherwise>
                      <mc:tracknum>${ccr.trackingNumber}</mc:tracknum>
                    </c:otherwise>
                  </c:choose>
                  </div>
                  <img class="secureImg" title="${warnMsg}"  style="top: 3px;" src="images/padlock.gif"/>
               </span>
               </c:if>
               <span style="float: left; font-weight: bold;">Tracking Number</span>
             </div>
          </td>
        </tr>
        <tr><td style="border-top: none; border-bottom: none; border-right: none;">
          <%-- note; div necessary, FF bug will not allow rel pos on table cell --%>
          <div style="position: relative;">
            <mc:xinput
              styleClass="insecureFormInput"
              bean="ccrDOM"
              id="sourceEmail"
              name="sourceEmails"
              join=","
              />
              <img class="secureImg" title="${warnMsg}"  src="images/padlock.gif"/>
            </div>
          </td>
          <td style="border-top: none; border-bottom: none;">
            <c:set var='accessPin' value='${desktop.replyPin}'/>
            <c:if test='${not empty ccr.accessPin}'>
              <c:set var='accessPin' value='${ccr.accessPin}'/>
            </c:if>
            <span style="float: left; font-weight: bold;">Assigned PIN</span>
            <span id="pinFields" class="writeOnly" style="float: right;">
              <input type="text" class="pinInput" name="pin0" value="${fn:substring(accessPin,0,1)}" size="1"/>
              <input type="text" class="pinInput" name="pin1" size="1" value="${fn:substring(accessPin,1,2)}"/>
              <input type="text" class="pinInput" name="pin2" size="1" value="${fn:substring(accessPin,2,3)}"/>
              <input type="text" class="pinInput" name="pin3" size="1" value="${fn:substring(accessPin,3,4)}"/>
              <input type="text" class="pinInput" name="pin4" size="1" value="${fn:substring(accessPin,4,5)}"/>
            </span>
          </td>
        </tr>
        <tr>
          <td style="" colspan="2" >
          <div style="position: relative;">
            <mc:xnode bean="ccrDOM" path="toEmail" name="toEmail"/>
            <c:set var="ccrPurpose"><mc:xvalue bean="ccrDOM" path="ccrPurpose"/></c:set>
            <c:set var="replySubject"><c:choose><c:when test='${empty ccrPurpose}'><c:out value='${notificationSubjectText}'/> ${toEmail.textTrim}</c:when><c:otherwise>${ccrPurpose}</c:otherwise></c:choose></c:set>
            <input class="insecureFormInput"
                   id="ccrPurpose"
                   name="ccrPurpose"
                   style="width: 99%; padding-left: 4px; padding-bottom: 4px;"
                   value="${replySubject}"
                   />
              <img class="secureImg" title="${warnMsg}"  style="top: 3px;" src="images/padlock.gif"/>
              <div id='purposesACContainer'></div>
            </div>
          </td>
        </tr>
      </table>
    </td>
  </tr>
  <tr><th style="padding-top: 5px;">FROM</th></tr>
  <tr><th>PURPOSE</th></tr>
</s:layout-definition>
