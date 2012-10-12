<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<c:set var="initialContents" scope="request" value="tab6"/>
<c:set var="guid" scope="session" value='${param["guid"]}'/>
<c:set var="pin" scope="session" value='${param["p"]}'/>
<c:set var="trackingNumber" scope="session" value='${param["tracking"]}'/>
<%-- ssadedin: avoid setting 0000000000000 as an accid id as it is used for pops --%>
<c:if test='${param["accid"] != "0000000000000000"}'>
  <c:set var="accid" scope="session" value='${param["accid"]}'/>
</c:if>
<c:set var="idp" scope="session" value='${param["idp"]}'/>
<c:set var="autoLogon" value="true" scope="session"/>
<c:set var="initialContents" scope="request" value="tab6"/>
<c:set var="initialContentsUrl" scope="request" value="autoLogin.do?"/>
<c:if test='${! empty param["context"]}'>
  <c:set var="initialContentsUrl" scope="request" value="autoLogin.do?displayMode=${param['context']}"/>
</c:if>
<c:if test='${! empty param["mode"]}'>
  <c:set var="initialContentsUrl" scope="request" value="${initialContentsUrl}&mode=${param['mode']}"/>
</c:if>
<c:remove var="medcommonsId"/>
<c:remove var="notificationForm"/>
<c:remove var="desktop"/>
<jsp:forward page="platform.jsp"/>
