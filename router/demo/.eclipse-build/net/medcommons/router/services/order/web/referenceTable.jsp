<%@ page language="java"%>
<%@ include file="/taglibs.inc.jsp" %>
<%@ page import="net.medcommons.router.services.xds.consumer.web.action.*" %>
<table border="0" cellspacing="0" class="referencesTable">
<tbody id="referencesTableBody">
<% CCRDocument ccr  = (CCRDocument)request.getAttribute("ccr"); %>
<c:forEach items="${ccr.displayReferences}" var="series" varStatus="status">
    <jsp:useBean id='series' scope='page' class='java.lang.Object' />
    <% int seriesIndex = ccr.getSeriesList().indexOf(series); %>
    <tr <c:if test='${series.validationRequired}'>class="validationWarnRow"</c:if> >
      <script type="text/javascript">
        <c:if test='${series.validationRequired}'>hasUnvalidatedAttachments = true;</c:if>
      </script>
      <td class="referenceIndexCell">${status.index+1}</td>
      <td>
        <c:choose>
          <c:when test="${series.mimeType == 'URL'}"><a target="reference" href="${series.firstInstance.referencedFileID}">${mc:trunc(series.firstInstance.referencedFileID,60)}</a></c:when>
          <c:otherwise><a href='javascript:goViewer("${series.mcGUID}",<%=seriesIndex%>);' title="Open this Reference in the Viewer">${series.seriesDescription}</a></c:otherwise>
        </c:choose>
      </td>
      <td class="seriesTypeCell">
        <c:choose>
          <c:when test="${series.mimeType == 'application/x-ccr+xml'}">CCR</c:when>
          <c:when test="${series.mimeType == 'application/pdf'}">PDF</c:when>
          <c:otherwise>${series.mimeType}</c:otherwise>
        </c:choose>
      </td>
      <td>&nbsp;
          <c:if test="${series.mimeType != 'application/dicom' and series.mimeType != 'URL'}">
            ( <fmt:formatNumber maxFractionDigits="1">${series.firstInstance.contentSize / 1024}</fmt:formatNumber> KB)
          </c:if>
      </td>
      <td align='left' style='vertical-align: middle;' valign="center">
      <div style="position: relative;">
      <a class="deleteLink" href="javascript:deleteSeries(${status.index+1});" title="Delete this Reference">X</a>&nbsp;&nbsp;
      <c:if test='${series.validationRequired}'>
        <a href='javascript:goViewer("${series.mcGUID}",<%=seriesIndex%>);'
          ><img border="0" class='validationImg' title="Click here to view this item and confirm it is correct." src="images/emblem-important.png"/></a>
          &nbsp;&nbsp;<span style="white-space: nowrap;" id="valdreq" class="validationWarn">CONFIRMATION REQUIRED</span>
      </c:if>
      </div>
      </td>
    </tr>
</c:forEach>
</tbody>
</table>

