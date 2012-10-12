<%@ include file="/taglibs.inc.jsp" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%--

  MedCommons Viewer CCR JSP

  This file outputs javascript to define a CCR object

--%>
  var ccrs = new Array();
  var ccr = null;
  var activeCCR = null;
  <dt:timeZone id="tz">${cookie['mctz']}</dt:timeZone>
  <c:forEach items="${ccr.seriesList}" var="series">
    <c:if test='${series.mimeType == "application/x-ccr+xml"}'>
      <c:set var="seriesCcr" value="${series.firstInstance.document}" scope="page"/>
      <c:set var="ccrDOM" value="${seriesCcr.JDOMDocument}" scope="request"/>
      <c:set var="patient" value="${seriesCcr.patient}"/>
       
      ccr = {
        guid: '${seriesCcr.guid}',
        uri: '${seriesCcr.uri}',
        <c:if test="${not empty seriesCcr.from}">from: ${seriesCcr.from.JSON},</c:if>
        patientId: '${patientId}',
        createDateTime: '<fmt:formatDate pattern="MM/dd/yyyy K:mm a" timeZone="${tz}" value="${ccr.createTime}"/>',
        trackingNumber: '<mc:tracknum>${seriesCcr.trackingNumber}</mc:tracknum>',
        logicalType: '${ccr.logicalType}',
        storageMode: '${ccr.storageMode}',
        actors: ${seriesCcr.actorsJSON},
        references: <c:choose><c:when test='${not empty seriesCcr.references}'>${seriesCcr.references.JSON}</c:when><c:otherwise>null</c:otherwise></c:choose>,
        patient: { 
          medCommonsId: '${patientId}',
          actorId: '<mc:xvalue bean="ccrDOM" path="patientActorID" />',
          givenName: '${mc:jsEscape(patient.givenName)}',
          familyName: '${mc:jsEscape(patient.familyName)}',
          dateOfBirth: '${mc:jsEscape(ccr.displayPatientDateOfBirth)}',
          age: '<mc:xvalue bean="ccrDOM" path="patientAge" />',
          address1: '${patient.line1}',
          city: '${patient.city}',
          state: '${patient.state}',
          postalCode: '${patient.postalCode}',
          country: '${patient.country}',
          organization: '${patient.organization}',
          gender: '${patient.gender}',
          emails: [],
          phoneNumbers: [],
          ids: [ <mc:xnode bean='ccrDOM' path='patientIds' forceList="true" name='patientIds'/>
              <c:forEach items="${patientIds}" var="patientId" varStatus="status">
                <c:if test='${status.index > 0}'>,</c:if>
                { type: '<mc:xvalue bean="patientId" path="idType"/>', value: '<mc:xvalue bean="patientId" path="idValue"/>' }
              </c:forEach>
            ],
	        status: '<mc:xvalue bean="ccrDOM" path="patientStatus"/>'
          }
      };
      ccr.patient.ageSex = ageSex(ccr.patient.age, ccr.patient.gender);
      ccrs["${series.mcGUID}"]=ccr;
      if(activeCCR == null) {
        activeCCR = update(ccr,CCR);
        activeCCR.get = partial(resolve,ccr);
      }
    </c:if>
  </c:forEach>
  ccr = activeCCR;
