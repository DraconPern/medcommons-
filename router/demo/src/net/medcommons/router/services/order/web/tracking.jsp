<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%@ include file="nocache.jsp" %>
<c:set var="initialContents" scope="request" value="tab6"/>
<c:set var="trackingNumber" scope="session" value='${param["tracking"]}'/>
<c:if test='${param["accid"] != "0000000000000000"}'>
  <c:set var="accid" scope="session" value='${param["accid"]}'/>
  <c:set var="auth" scope="session" value='${param["auth"]}'/>
</c:if>
<c:set var="pin" scope="session" value='${param["p"]}'/>
<c:if test="${trackingNumber == 'import'}">
  <c:set var="initialContents" scope="request" value="tab4"/>
  <c:set var="initialContentsUrl" scope="request" value="import.jsp?am=${param['am']}"/>
</c:if>
<c:if test='${!empty pin}'>
  <c:set var="autoLogon" value="true" scope="session"/>
  <c:set var="initialContents" scope="request" value="tab6"/>
  <c:set var="initialContentsUrl" scope="request" value="autoLogin.do"/>
</c:if>
<c:set var="registry" scope="session" value='${param["registry"]}'/>
<c:set var="cleanPatient" scope="session" value="true"/>

<c:remove var="medcommonsId"/>
<c:remove var="notificationForm"/>
<c:remove var="desktop" scope="session"/>
<% net.medcommons.router.services.UserSession.clean(request); %>
<c:choose>
  <c:when test="${trackingNumber == 'new'}">
    <c:set var="initialContents" scope="request" value="tab4"/>
    <c:set var="initialCCRFrom" scope="session" value="${param['from']}"/>
    <jsp:forward page="NewCCR.action"/>
  </c:when>
  <c:otherwise>
    <c:if test='${empty initialContentsUrl}'> 
      <c:set var="initialContentsUrl" scope="request" value="Track.action?show"/> 
      <c:if test='${param["expired"]}'>
        <c:set var="initialContentsUrl" scope="request" value="Track.action?expired=true"/> 
      </c:if>
    </c:if>
    <jsp:forward page="platform.jsp"/>
  </c:otherwise>
</c:choose>
