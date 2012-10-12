<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons Patient Info Display 

  This single large gadget renders a number of smaller gadgets showing 
  information about the current patient.

  @author Simon Sadedin, MedCommons Inc.
--%>

<c:if test='${!empty ccr}'>
  <c:set var="ccrXml" value="${ccr.JDOMDocument}"/>
  <c:set var="sex" value="${ccr.patientGender=='Male'?'M': ccr.patientGender=='Female'?'F':''}"/>
  <c:set var="ageSex"><mc:xvalue bean="ccrXml" path="patientAge"/> ${sex}</c:set>
  <c:set var='headerTitle'>Forms - <mc:xvalue bean="ccrXml" path="patientFamilyName"/> <mc:xvalue bean="ccrXml" path="patientGivenName"/> <c:if test='${! empty ageSex}'>- ${ageSex}</c:if></c:set>

  <c:set var='gadgets'>
    <c:set var='embedded' scope='request' value='true'/>
    <c:set var='hideTitle' value='true' scope='request'/>
    <jsp:include page="/hipaaGadget.jsp"/>
  </c:set>

  <s:layout-render name='/healthURL.jsp' mcid='${ccr.patientMedCommonsId}'/>
  <c:set var='rightLinks'>
    <a href="${hurl}" onfocus="blur();" 
      title="Open/Edit CCR" 
      target="ccr" onclick="return openCcrWindow('${hurl}');"><img src='images/hurl.png'/></a>
      &nbsp;<a href="${hurl}" onfocus="blur();" title="Open/Edit CCR" target="ccr" onclick="return openCcrWindow('${hurl}');">HealthURL</a>

    &nbsp;
    <c:set var='activityLink' value='?combined=true'/>
    <a href="${activityLink}" onfocus="blur();" title="Activity"><img src='images/activity-icon.png'/></a>
      <a href="${activityLink}" onfocus="blur();" title="Activity">Activity</a>
  </c:set>
</c:if>

<s:layout-render name='/tiledGadgets.jsp' headerTitle='${headerTitle}' gadgets='${gadgets}' rightLinks='${rightLinks}'/>
