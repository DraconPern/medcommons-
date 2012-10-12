<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page isELIgnored="false" %> 
<% request.setAttribute("props", System.getProperties()); %>
<div leftmargin="0" topmargin="0" marginheight="0" marginwidth="0">
  <span class="statusLabel">Overall:</span> <span class="${status}">${status}</span><br/>
  <span class="statusLabel">Database:</span>  <span class="${database}">${database}</span><br/>
  <span class="statusLabel">Version:</span>   <span class="statusValue">${version}</span><br/>
  <span class="statusLabel">Revision:</span>  <span class="statusValue">${revision}</span><br/>
  <hr/>
  <span class="statusLabel">Running Since:</span> <fmt:formatDate type="both" dateStyle="full" value="${runningSince}"/><br/>
  <span class="statusLabel">Total Memory:</span>  <span class="statusValue">${totalMemory}</span><br/>
  <span class="statusLabel">Max Memory:</span>  <span class="statusValue">${maxMemory}</span><br/>
  <span class="statusLabel">Free Memory:</span>  <span class="statusValue">${freeMemory}</span><br/>
  <span class="statusLabel">Free Repository Space:</span>  <span class="statusValue">${freeRepositorySpace}</span><br/>
  <span class="statusLabel">Time Since Last Space Update:</span>  <span class="statusValue">${freeRepositorySpaceAge} seconds</span><br/>
  <hr/>
    <span class="statusLabel">Last Repository Modification:</span>  <span class="statusValue"><fmt:formatDate type="both" dateStyle="full" value="${lastModification}"/></span><br/>
    <span class="statusLabel">CXP Transactions:</span>  <span class="statusValue">${cxpTransactions}</span><br/>
    <span class="statusLabel">CXP Errors:</span>  <span class="statusValue">${cxpErrors}</span><br/>
    <span class="statusLabel">CXP Transfers:</span>  <span class="statusValue">${cxpTransfers}</span><br/>
    <span class="statusLabel">CXP Gets:</span>  <span class="statusValue">${cxpGets}</span><br/>
    <span class="statusLabel">CXP Deletes:</span>  <span class="statusValue">${cxpDeletes}</span><br/>
  <hr/>
          <span class="statusLabel">Images Encoded</span>: <span class="statusValue">${imagesEncoded}</span>
          <span class="statusLabel">Bytes Sent</span>: <span class="statusValue">${bytesEncoded}</span>
          <span class="statusLabel">Throughput</span>: <span class="statusValue"><bean:write name="bytesPerSecond" format="##.#"/> KBytes/sec</span>
  <hr/>
    <span class="statusLabel">Total Files Backed Up</span>: <span class="statusValue">${backupsCompleted}</span><br/>
    <span class="statusLabel">Average Backup Queue Time (ms)</span>: <span class="statusValue">${averageBackupQueueDelayMs}</span><br/>
    <span class="statusLabel">Average Backup Time (ms)</span>: <span class="statusValue">${averageBackupTimeMs}</span><br/>
    <span class="statusLabel">Backup Retries</span>: <span class="statusValue">${backupRetries}</span><br/>
    <span class="statusLabel">Fatal Backup Failures</span>: <span class="statusValue">${backupFailures}</span><br/>
    <span class="statusLabel">Files Restored</span>: <span class="statusValue">${filesRestored}</span><br/>
    <span class="statusLabel">Restore Failures</span>: <span class="statusValue">${restoreFailures}</span><br/>
  <hr/>
  <c:forTokens var="p" items="os.name,java.runtime.version,user.timezone,java.vm.vendor,os.name,os.arch" delims=",">
      <span class="statusLabel">${p} : </span>${props[p]}<br/>
  </c:forTokens>
  <hr/>
  <%-- debug stuff
  <c:forEach var="p" items="${props}">
    <c:if test='${fn:startsWith(p,"")'>
      <span class="statusLabel">${p.key} : </span>${p.value}<br/>
    </c:if>
  </c:forEach>
  --%>
</div>
