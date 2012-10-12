<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %> 

<tiles:useAttribute name="ccrs" id="ccrs"/>
<tiles:useAttribute name="variableName" id="varName"/>

var ${varName} = new Array();      
<% int count=0; %>
<c:forEach var="ccr" items="${ccrs}" varStatus="status">
  var ccr = new Object();
  ccr.index = ${status.index};
  ccr.series = new Array();
  ccr.orderGuid = "${ccr.guid}";
  ccr.orderId = "${ccr.order.id}";
  ccr.creationDate = "<dt:format pattern="MM/dd/yyyy">${ccr.createTimeMs}</dt:format>";
  ccr.creationTime = "<dt:format pattern="HH:mm:ss">${ccr.createTimeMs}</dt:format>";
  <c:choose>
    <c:when test="${ccr==desktop.currentCcr}">
        ccr.open=true;
        initialOpenFolderIndex=${status.index};
    </c:when>
    <c:otherwise>ccr.open=false;</c:otherwise>
  </c:choose>
  <c:forEach var="series" items="${ccr.seriesList}" varStatus="seriesStatus">
    <bean:define id="instances" name="series" property="instances" type="java.util.Map"/>
    series = new Object();
    new Object();
    series.guid="${series.mcGUID}";
    series.description="${series.seriesDescription}";
    series.numImages=<%=String.valueOf(instances.size())%>;
    series.firstInstanceFile="${series.firstInstance.referencedFileID}";
    series.mimeType="${series.firstInstance.mimeType}";
    series.index=${seriesStatus.index};
    series.globalIndex=<%=count%>;
    series.ccr=ccr;
    ccr.series[${seriesStatus.index}] = series;
  </c:forEach>
  ${varName}[${status.index}]=ccr;
</c:forEach>

