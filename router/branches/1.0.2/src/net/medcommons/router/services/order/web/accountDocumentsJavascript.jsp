<%@ page language="java"%>
<%-- 
  Includes a fragment of Javascript that defines the available account documents ("live" documents)
  for the active Patient.   Relies on patientAccountSettings being defined (see ViewEditCCRAction).
--%>
<%@ include file="/taglibs.inc.jsp" %>
var currentCcrGuid = null;
var _a = [];
<c:if test='${not empty patientAccountSettings}'>
  <c:forEach items="${patientAccountSettings.accountDocuments}" var="doc" varStatus="status">
    _a['${mc:jsEscape(doc.key)}']={ guid: '${doc.value}', type: '${doc.key}' };
  </c:forEach>
</c:if>

if(_a['CURRENTCCR'])
  currentCcrGuid = _a['CURRENTCCR'].guid;

accountDocuments = _a; _a = null; delete _a;
