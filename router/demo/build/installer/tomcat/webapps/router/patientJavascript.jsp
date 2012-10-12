<%@ include file="/taglibs.inc.jsp" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page isELIgnored="false" %> 
<%--

  MedCommons WADO Viewer JSP

  This file outputs javascript to define a Patient object

--%>
<c:set var="ccrDOM" value="${ccr.JDOMDocument}" scope="page"/>
<script language="javascript">
  var newObj;
  var newSeries;
  p = new Patient("<mc:xvalue bean="ccrDOM" path="patientName"/>","<mc:xvalue bean="ccrDOM" path="patientMedCommonsId"/>");
  p.StudiesArray[0] =  new Study(
      "Study",
    "${ccr.guid}", 
    "<dt:format pattern="MM/dd/yyyy">${ccr.createTimeMs}</dt:format>",
    "<dt:format pattern="MM/dd/yyyy">${ccr.createTimeMs}</dt:format>");
  p.StudiesArray[0].pending = ${! ccr.validated };

<% int index = 0; %><%-- formatting deliberately screwed up to save space! --%>
<logic:iterate id="series" name="ccr" property="seriesList" type="net.medcommons.router.services.dicom.util.MCSeries">
n = new Series("<%= series.SeriesDescription %>","<%= series.mcGUID %>","<%= series.SeriesInstanceUID %>","<%= series.Modality %>","<%= series.SeriesNumber %>");
n.validationRequired = ${series.validationRequired};
n.index = <%= index %>;
n.storageId = "<%= series.storageId %>";
p.StudiesArray[0].SeriesArray.push(n);
n.presets = [];
n.paymentRequired = ${series.paymentRequired};
n.billingEvent = <%= series.getPendingBillingEvent() != null ? series.getPendingBillingEvent().toJSON() :  null %>;
var _i = Instance;
<c:forEach items="${series.presets}" var="preset"> n.presets.push( { 'name': '${mc:jsEscape(preset.name)}', 'window': ${preset.window}, 'level':${preset.level} }); </c:forEach>
<% MCInstance firstInstance = null; %> var i = n.InstanceArray;
<logic:iterate id="entry" name="series" property="instances" type="net.medcommons.router.services.dicom.util.MCInstance"><% if(firstInstance==null) firstInstance = entry; %>i.push(new _i("<%="application/dicom".equals(firstInstance.mimeType)?"":entry.SOPInstanceUID %>","<%= entry.InstanceNumber %>","<%= entry.ReferencedFileID %>","<%= entry.window %>","<%= entry.level %>"));</logic:iterate>n.mimeType='<%=firstInstance.mimeType%>';<% ++index; %></logic:iterate>
</script>
