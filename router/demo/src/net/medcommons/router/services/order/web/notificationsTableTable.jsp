<%@ include file="/taglibs.inc.jsp" %>
<jsp:include page="sendStatus.jsp"/>
<table id="ccrTable" class="ccrTable" cellpadding="0" cellspacing="0">
  <input id='ccrTableCCRIndex' type='hidden' value='${ccrIndex}'/>
  <s:layout-render name='/notificationsTable.jsp' readonly='true'/>
</table>

