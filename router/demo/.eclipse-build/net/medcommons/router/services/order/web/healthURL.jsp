<%@ include file="/taglibs.inc.jsp" %>
<%--
  Renders no output, defines a single variable called "url" with the 
  properly normalized and constituted url of the current ccr for the
  specified 'mcid' parameter.

  @param mcid - Account id to define HealthURL link for
--%>
<s:layout-definition>
  <c:set var='EnableModRewriteURLs'><mc:config property="EnableModRewriteURLs" default="true"/></c:set>
  <c:set var='accountServer'><mc:config property='AccountServer'/></c:set>
  <c:choose>
    <c:when test='${EnableModRewriteURLs == "true"}'>
      <c:set var="hurl" value="${accountServer}/../../${mcid}" scope='request'/>
    </c:when>
    <c:otherwise>
      <c:url var="hurl" value="${accountServer}/../cccrredir.php" scope='request'>
        <c:param name="accid" value="${mcid}" />
      </c:url>
    </c:otherwise>
  </c:choose>
</s:layout-definition>

