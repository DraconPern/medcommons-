<%@ page language="java"%>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ page isELIgnored="false" %> 
<%--
  MedCommons CCR Widget

  This page renders a small widget intended for embedding
  on external pages.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="ccrXml" value="${actionBean.ccr.JDOMDocument}"/>
<c:set var="h" value="${actionBean.ccr.changeHistory.rootElement}"/>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
  <head>
    <script type="text/javascript" src="mochikit/MochiKit.js"></script>
    <script type="text/javascript" src="utils.js"></script>
    <script type="text/javascript">
      var returnUrl = '${widgetReturnUrl}';
      var accessGuid = '${ccr.guid}';
      if(accessGuid == '')
        accessGuid = null;

      function checkUpdate() {
        if(originalSessionId != getCookie('JSESSIONID')) {
          log("session changed. returning user to specified return url " + returnUrl);
          window.location.href=returnUrl;
        }
        log("session maintained old = " + originalSessionId + " new = " + getCookie('JSESSIONID'));
      }
      setInterval(checkUpdate,1000);
      var originalSessionId = getCookie('JSESSIONID');
    </script>
    <style type="text/css">
      body {
        font-family: verdana,arial;
        font-size: 12px;
        padding:2px 0px;
        margin:0px;
      }
      #updatesTable {
        text-align: left;
      }
      #ccrheader, #history {
        width: 175px;
      }
      #ccrheader {
        font-weight: bold;
        color: #333;
        margin-left: 2px;
        background-color: #9ce;
        background-color: white;
      }
      ul {
        font-size: 12px;
      }
      #history {
        font-size: 12px;
      }
      table tr.row1 {
        background-color: #f6f6f6;
      }
      table tr th {
        text-align: left;
        font-weight: bold;
        color: #444;
        background-image: url('images/lbgrad.png');
      }
      table tr td {
        padding: 0px 7px;
      }
      img {
        border: 0;
      }
      h4 {
        font-size: 14px;
      }
      h4 a img {
        position: relative;
        top: -1px;
        left: 5px;
      }
    </style>
  </head>
  <body>
  <h4>Updates to Current CCR <a href="<mc:config property="CommonsBase"/>/gwredirguid.php?guid=${actionBean.ccr.guid}" title="Open/Edit Current CCR" target="ccr"><img src="images/openccrp.png"/></a></h4>
  <c:set var="guid" value="${actionBean.ccr.guid}"/>
  <c:if test='${!empty ccrXml}'>
    <c:choose>
      <c:when test='${!empty changes}'>
      <div id="updatesTable">
        <table border="0" style="width: 95%; padding-left: 5px;" cellspacing="3">
          <tr><th>&nbsp;</th><th>Date/Time</th><th>Activity</th><th>Status</th></tr>
          <c:forEach items="${changes}" var="changeSet" varStatus="s">
            <c:if test="${s.index<5}">
              <c:set var="ucount" value="${changeSet.updateCount}"/>
              <c:set var="acount" value="${changeSet.addCount}"/>
              <tr class="row${s.index%2}">
                <td><a href="<mc:config property="CommonsBase"/>/gwredirguid.php?guid=${guid}" onfocus="blur();" title="Open/Edit CCR" target="ccr"><img title="Open/Edit CCR" border="0" src="images/editpadw.gif"/></a></td>
                <td><dt:format pattern="MM/dd/yyyy K:mm a">${changeSet.dateTime.time}</dt:format></td> 
                <td style="text-align: left;"><c:if test="${ucount>0}">${ucount} updates</c:if><c:if test="${ucount>0&&acount>0}">,</c:if> <c:if test="${acount>0}">${acount} additions</c:if></td>
                <td>${changeSet.notificationStatus}</td>
              </tr>
              <c:set var="guid" value='${changeSet.source}'/>
            </c:if>
          </c:forEach>
        </table>
      </div>
      </c:when>
      <c:otherwise>
        <p>There have been no updates to the Current CCR.</p>
      </c:otherwise>
    </c:choose>
  </c:if>
  <c:if test='${empty ccrXml}'>
    No Current CCR has been set.
  </c:if>
  </body>
</html>
