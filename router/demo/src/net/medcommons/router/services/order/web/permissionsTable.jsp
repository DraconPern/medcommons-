<%@ include file="taglibs.inc.jsp" %>
<c:set var='acctsUrl'><mc:config property='AccountsBaseUrl'/></c:set>
<table class="stdTable smallTable" width="100%">
  <thead>
  </thead>
  <tbody>
  <c:forEach items='${actionBean.shares}' var='p'>

    <%-- construct a unique name for the combo box that indicates which select is modified --%>
    <c:set var='selname'><c:choose>
      <c:when test='${not empty p.applicationToken}'>at_${p.applicationToken}</c:when>
      <c:when test='${not empty p.esId}'>es_${p.esId}</c:when>
      <c:otherwise>${p.groupAcctId}</c:otherwise>
      </c:choose></c:set>
    <tr class="practice">
      <td align="left" style="text-align: left;">
        <c:out value="${p.practiceName}"/><c:if test='${p.identityType == "Application"}'> (Application)</c:if>
      </td>
      <td>
        <c:if test='${p.practiceName != "Individuals"}'>
          <a class='groupInfoLink' href="javascript:consentInfo('${selname}')">?</a>
        </c:if>
        <c:choose>
          <c:when test='${not empty p.applicationToken}'>Application</c:when>
          <c:when test='${p.practiceName == "Individuals"}'>
          <mc:enabled name='consents.shareByOpenID'>      
          <a id='shareLink' href="javascript:shareOpenId();"
                 class='iconLink'
                 style='background-position: 0px -80px;' 
                 >&nbsp;&nbsp;&nbsp;&nbsp;</a>&nbsp;
           <a id='shareLink' href="javascript:shareOpenId();"> Share with Open ID</a>
           &nbsp;
          </mc:enabled>
          <a href="javascript:sharePhone();"
                 class='iconLink' title='Share by sending a code to a Phone'
                 style='background-position: 0px -100px;' 
                 >&nbsp;&nbsp;&nbsp;&nbsp;</a>&nbsp;
               <a id='sharePhoneLink' href="javascript:sharePhone();"
                  title='Share by sending a code to a Phone'> Share by Phone</a>
          </c:when>
          <c:when test='${p.practiceName == "Tracking Number / PIN Access"}'>&nbsp;
          </c:when>
          <c:otherwise>Group @ <c:out value='${acctsUrl}'/></c:otherwise>
        </c:choose>
      </td>
      <td>
        <c:choose>
          <c:when test='${p.practiceName != "Individuals" and p.practiceName != "Tracking Number / PIN Access"}'>
            <select name="${selname}"
                  style="font-size: 10px;" onchange="fillConsentGroup(this.name,this.value); saveConsents();">
            <c:forEach items="${actionBean.rightsValues}" var="opt">
              <option value="${opt.key}" <c:if test='${p.accessRights==opt.key}'>selected="true"</c:if> >${opt.value}</option>
            </c:forEach>
            </select>
          </c:when>
          <c:when test='${p.practiceName == "Tracking Number / PIN Access" }'>&nbsp;</c:when>
          <c:otherwise>
            &nbsp;
            <c:set var='individuals' value='true'/>
          </c:otherwise>
        </c:choose>
      </td>
    </tr>
    <c:forEach items="${p.accounts}" var="u">
        <c:set var="rowId"><c:choose><c:when test='${p.identityType != "Application"}'>${p.groupAcctId}.${u.accountId}></c:when><c:otherwise>${p.applicationToken}</c:otherwise></c:choose></c:set>
        <tr id="${rowId}" class="detailRow">
            <td>&nbsp;</td>
            <td align="left" style="text-align: left;">
              <c:choose>
                <c:when test='${u.identityType == "PIN"}'>
                  <dt:format pattern="MM/dd/yyyy">${u.createDateTime.time}</dt:format>  -
                  <c:set var='tracknum' value='${fn:substring(u.accountId,0,12)}'/>
                  <a href="${acctsUrl}tracking/${tracknum}" target="_ccr"><mc:tracknum>${tracknum}</mc:tracknum></a>  PIN: ${fn:substring(u.accountId,13,18)} 
                </c:when>
                <c:when test='${u.identityType == "Phone"}'><c:out value='${u.firstName}'/> <c:out value='${u.lastName}'/> - Ph: ${mc:formatPhoneNumber(u.accountId)}</c:when>
                <c:when test='${not empty u.esId and not empty u.accountId}'>${u.accountId}</c:when>
                <c:otherwise>
                  <c:set var='name'><c:out value="${u.firstName}"/>  <c:out value="${u.lastName}"/></c:set>
                  ${name}
                  <c:if test='${not empty u.email and not empty fn:trim(name)}'> / </c:if><c:out value="${u.email}"/>
                </c:otherwise>
              </c:choose>
            </td>  
            <td>
              <select name="<c:choose><c:when test='${empty u.esId}'>${u.accountId}</c:when><c:otherwise>es_${u.esId}</c:otherwise></c:choose>" style="font-size: 10px;" onchange="saveConsents();">
                <c:forEach items="${actionBean.rightsValues}" var="opt">
                  <option value="${opt.key}" <c:if test='${u.rights==opt.key}'>selected="true"</c:if> >${opt.value}</option>
                </c:forEach>
              </select>
            </td>
        </tr>
    </c:forEach>
    <%-- make sure there's some space between entries if no accounts --%>
      <tr><td colspan="3">&nbsp;</td></tr>
  </c:forEach>

  <%-- if no individuals defined then render a dummy row --%>
  <c:if test='${empty individuals}'>
    <tr class="practice">
      <td>Individuals</td>
      <td>
        <mc:enabled name='consents.shareByOpenID'>      
        <a id='shareLink' href="javascript:shareOpenId();"
           class='iconLink'
           style='background-position: 0px -80px;' 
           >&nbsp;&nbsp;&nbsp;&nbsp;</a>&nbsp;&nbsp;
        <a id='shareLink' href="javascript:shareOpenId();">Share with Open ID</a>
        </mc:enabled>
           &nbsp;
          <a href="javascript:sharePhone();"
                 class='iconLink' title='Share by sending a code to a Phone'
                 style='background-position: 0px -100px;' 
                 >&nbsp;&nbsp;&nbsp;&nbsp;</a>&nbsp;
           <a id='sharePhoneLink' href="javascript:sharePhone();"
                  title='Share by sending a code to a Phone'> Share by Phone</a>
      </td>
      <td>&nbsp;</td>
    </tr>
    <tr class="detailRow" style='height: 2em;'>
      <td colspan="3" style='text-align: center; color: gray;'>- no individual consents defined -</td>
    </tr>
  </c:if>
  </tbody>
</table>
