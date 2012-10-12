<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes-dynattr.tld" prefix="s" %>
<%@ page isELIgnored="false" %> 
<html>
  <head>
    <link href="ereferralLetter.css" rel="stylesheet" type="text/css"/>
    <style type="text/css" media="print">
      input {
        border: none;
      }
    </style>
    <script type="text/javascript">
      var validationErrors = new Array();
      <c:forEach items='${actionBean.context.validationErrors}' var="field">
        validationErrors.push('${field.key}');
      </c:forEach>
      <c:if test='${not empty counters}'>
        var counters = ${counters};
      </c:if>
    </script>
  </head>
  <body>
    <div id="validationFailures">
    </div>
    <form name="results">
      <table id="statusTable" style="margin-left: 10px;">
          <tr><th>Status</th><td id="ssStatus"><input type="text" name="status" value="${sendStatus}"/></td></tr>
          <tr><th>Tracking Number</th><td id="ssTrackingNumber"><input type="text" name="trackingNumber" value="<mc:tracknum>${notificationForm.trackingNumber}</mc:tracknum>"/></td></tr>
          <tr><th>PIN</th><td id="ssPin"><input type="text" name="pin"  value="${notificationForm.pin}"/></td></tr>
          <c:if test='${!empty accountCreatedId}'>
            <tr><th>Account</th><td id="ssAcctCreated"><input type="text" name="acctId"  value="${accountCreatedId}"/></td></tr>
          </c:if>
          <tr><td>&nbsp;</th><td><input type="text" name="guid" value="${ccr.guid}"/></td></tr>
      </table>  
      
      <c:set var="creationDateTime"><dt:format pattern="MM/dd/yyyy K:mm a, z">${ccr.createTimeMs}</dt:format></c:set>
      <c:set var="creationDate"><dt:format pattern="MM / dd / yyyy">${ccr.createTimeMs}</dt:format></c:set>
      <input type="hidden" name="savedStorageMode" value="${savedCcr.storageMode}"/>
      <input type="hidden" name="savedLogicalType" value="${savedCcr.logicalType}"/>
      <input type="hidden" name="storageMode" value="${ccr.storageMode}"/>
      <input type="hidden" name="logicalType" value="${ccr.logicalType}"/>
      <input type="hidden" name="ccrIndex" value="${ccrIndex}"/>
      <input type="hidden" name="createDate" value="${creationDate}"/>
      <input type="hidden" name="createDateTime" value="${creationDateTime}"/>
      <input type="hidden" name="error" value="${sendError}"/>
    </form>
  </body>
</html>
