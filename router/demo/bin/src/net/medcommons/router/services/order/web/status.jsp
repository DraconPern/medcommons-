<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.*" %>
<%@ page isELIgnored="false" %> 
<% request.setAttribute("props", System.getProperties()); %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>Status</title>
    <link href="main.css" rel="stylesheet" type="text/css">
    <style type="text/css">
      .OK {
        color: green;
      }
      .PROBLEM {
        color: red;
      }
      .statusLabel {
        font-weight: bold;
      }
      fieldset {
        margin-top: 10px;
        margin-bottom: 15px;
        padding-left: 10px;
      }
      body {
        padding-left: 10px;
      }
    </style>
  </head>
  <% request.setAttribute("gw", request.getParameter("gw")); %>
  <body topmargin="0" marginheight="0">
  <h3>Status Summary for Node <mc:config property="NodeName" default="(undefined)"/></h3>
  <fieldset>
    <legend>Summary</legend>
    <b>Version</b>:   <bean:write name="version"/> <br/>
    <b>Revision:</b>  <bean:write name="revision"/><br/>
    <span class="statusLabel">Running Since:</span> <fmt:formatDate type="both" dateStyle="full" value="${runningSince}"/><br/>
  </fieldset>
  <fieldset>
    <legend>Resources</legend>
    <span class="statusLabel">Total Memory:</span>  <span class="statusValue">${totalMemory}</span><br/>
    <span class="statusLabel">Max Memory:</span>  <span class="statusValue">${maxMemory}</span><br/>
    <span class="statusLabel">Free Memory:</span>  <span class="statusValue">${freeMemory}</span><br/>
    <span class="statusLabel">Active Session:</span>  <span class="statusValue">${fn:length(desktops)}</span><br/>
    <span class="statusLabel">Free Repository Space:</span>  <span class="statusValue">${freeRepositorySpace}</span><br/>
    <span class="statusLabel">Time Since Last Space Update:</span>  <span class="statusValue">${freeRepositorySpaceAge} seconds</span><br/>
  </fieldset>
  <fieldset>
    <legend>CXP</legend>
    <span class="statusLabel">Last Repository Modification:</span>  <span class="statusValue"><fmt:formatDate type="both" dateStyle="full" value="${lastModification}"/></span><br/>
    <span class="statusLabel">CXP Transactions:</span>  <span class="statusValue">${cxpTransactions}</span><br/>
    <span class="statusLabel">CXP Errors:</span>  <span class="statusValue">${cxpErrors}</span><br/>
    <span class="statusLabel">CXP Transfers:</span>  <span class="statusValue">${cxpTransfers}</span><br/>
    <span class="statusLabel">CXP Gets:</span>  <span class="statusValue">${cxpGets}</span><br/>
    <span class="statusLabel">CXP Deletes:</span>  <span class="statusValue">${cxpDeletes}</span><br/>
  </fieldset>
  <fieldset>
    <legend>Viewer</legend>
          <span class="statusLabel">Images Encoded</span>: <span class="statusValue">${imagesEncoded}</span>
          <span class="statusLabel">Bytes Sent</span>: <span class="statusValue">${bytesEncoded}</span>
          <span class="statusLabel">Throughput</span>: <span class="statusValue"><bean:write name="bytesPerSecond" format="##.#"/> KBytes/sec</span>
  </fieldset>
  <fieldset>
    <legend>Small File Backup Service</legend>
    <span class="statusLabel">Total Files Backed Up</span>: <span class="statusValue">${backupsCompleted}</span><br/>
    <span class="statusLabel">Average Backup Queue Time (ms)</span>: <span class="statusValue">${averageBackupQueueDelayMs}</span><br/>
    <span class="statusLabel">Average Backup Time (ms)</span>: <span class="statusValue">${averageBackupTimeMs}</span><br/>
    <span class="statusLabel">Backup Retries</span>: <span class="statusValue">${backupRetries}</span><br/>
    <span class="statusLabel">Fatal Backup Failures</span>: <span class="statusValue">${backupFailures}</span><br/>
    <span class="statusLabel">Files Restored</span>: <span class="statusValue">${filesRestored}</span><br/>
    <span class="statusLabel">Restore Failures</span>: <span class="statusValue">${restoreFailures}</span><br/>
  </fieldset>
  <fieldset>
    <legend>System</legend>
    <c:forTokens var="p" items="os.name,java.runtime.version,user.timezone,java.vm.vendor,os.name,os.arch" delims=",">
        <span class="statusLabel">${p} : </span>${props[p]}<br/>
    </c:forTokens>
  </fieldset>
  </body>
</html>

