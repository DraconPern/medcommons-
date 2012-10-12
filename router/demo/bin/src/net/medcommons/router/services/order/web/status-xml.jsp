<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/xml"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page isELIgnored="false" %> 
<% request.setAttribute("props", System.getProperties()); %>
<gateway-status>
    <overall>${status}</overall>
    <database>${database}</database>
    <version>${version}</version>
    <revision>${revision}</revision>
    <os><%=System.getProperty("os.name")%></os>
    <arch><%=System.getProperty("os.arch")%></arch>
    <java-version><%=System.getProperty("java.version")%></java-version>
    <total-memory><%=Runtime.getRuntime().totalMemory()%></total-memory>
    <max-memory><%=Runtime.getRuntime().maxMemory()%></max-memory>
    <free-memory><%=Runtime.getRuntime().freeMemory()%></free-memory>
    <processors><%=Runtime.getRuntime().availableProcessors()%></processors>
    <running-since> <fmt:formatDate type="both" dateStyle="full" value="${runningSince}"/></running-since>
    <free-repository-space>${freeRepositorySpace}</free-repository-space>
    <time-since-last-space-update>${freeRepositorySpaceAge}</time-since-last-space-update>
    <last-repository-modification>  <fmt:formatDate type="both" dateStyle="full" value="${lastModification}"/></last-repository-modification>
    <cxp-transactions>  ${cxpTransactions}</cxp-transactions>
    <cxp-errors>  ${cxpErrors}</cxp-errors>
    <cxp-transfers>  ${cxpTransfers}</cxp-transfers>
    <cxp-gets>  ${cxpGets}</cxp-gets>
    <cxp-deletes>  ${cxpDeletes}</cxp-deletes>
    <images-encoded> ${imagesEncoded}</images-encoded>
    <bytes-sent> ${bytesEncoded}</bytes-sent>
    <throughput>${bytesPerSecond}</throughput>

    <small-file-backup-service>
      <totalBackups>${backupsCompleted}</totalBackups>
      <averageBackupQueueDelayMs>${averageBackupQueueDelayMs}</averageBackupQueueDelayMs>
      <averageBackupTimeMs>${averageBackupTimeMs}</averageBackupTimeMs>
      <backupRetries>${backupRetries}</backupRetries>
      <backupFailures>${backupFailures}</backupFailures>
      <filesRestored>${filesRestored}</filesRestored>
      <restoreFailures>${restoreFailures}</restoreFailures>
    </small-file-backup-service>

    <c:forTokens var="p" items="os.name,java.runtime.version,user.timezone,java.vm.vendor,os.name,os.arch" delims=",">
        <${p}>${props[p]}</${p}>
    </c:forTokens>
</gateway-status>
