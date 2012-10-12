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
  <c:set var='headerTitle'>Patient Activity - <mc:xvalue bean="ccrXml" path="patientGivenName"/> <mc:xvalue bean="ccrXml" path="patientFamilyName"/> 
  <c:if test='${! empty ageSex}'>- ${ageSex}</c:if></c:set>

  <c:set var='gadgets'>
    <c:set var='embedded' scope='request' value='true'/>
    <c:set var='first' scope='request' value='true'/>
    <jsp:include page="/accountActivity.jsp"/>
    <c:set var='first' scope='request' value='false'/>
    <jsp:include page="/managePermissions.jsp"/>
  </c:set>

  <s:layout-render name='/healthURL.jsp' mcid='${ccr.patientMedCommonsId}'/>

  <c:set var='rightLinks'>
    <a href="${hurl}" onfocus="blur();" 
      title="Open/Edit CCR" 
      target="ccr" onclick="return openCcrWindow('${hurl}');"
      class='iconLink'
      style='background-position: 0px -41px;' 
      >&nbsp;&nbsp;&nbsp;&nbsp;</a>
      &nbsp;<a href="${hurl}" onfocus="blur();" title="Open/Edit CCR" target="ccr" onclick="return openCcrWindow('${hurl}');">HealthURL</a>

    &nbsp;
    <c:set var='formsLink' value='?framedForms=true'/>
    <a href="${formsLink}" onfocus="blur();" title="Forms" 
      class='iconLink'
      style='background-position: 0px -60px;' 
      >&nbsp;&nbsp;&nbsp;&nbsp;</a>
      <a href="${formsLink}" onfocus="blur();" title="Forms">Forms</a>

    &nbsp;
    <c:set var='docsLink' value='?framedDocuments=true'/>
    <a href="${docsLink}" onfocus="blur();" title="Documents List" 
      class='iconLink'
      style='background-position: 0px 0px;' 
      >&nbsp;&nbsp;&nbsp;&nbsp;</a>
      <a href="${docsLink}" onfocus="blur();" title="Documents List">Documents</a>
  </c:set>
</c:if>

<s:layout-render name='/tiledGadgets.jsp' noheader='${noheader}' headerTitle='${headerTitle}' gadgets='${gadgets}' rightLinks='${rightLinks}'/>
