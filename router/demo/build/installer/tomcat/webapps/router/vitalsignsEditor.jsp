<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%--
  Vital Signs Editor

  Renders a div containing a form for editing vital signs

  See Javascript in ccreditor.js

  @author Simon Sadedin, MedCommons Inc.
--%>
<stripes:layout-render name="/sectionEditor.jsp" sectionName="VitalSigns" sectionPrefix="vitalsigns" sectionTitle="Vital Signs">
  <stripes:layout-component name="initialize">
    <mc:xnode bean='notificationForm' path='/x:ContinuityOfCareRecord/x:Body/x:VitalSigns/x:Result' forceList="true" name='vitalSigns'/>
    <c:forEach items="${vitalSigns}" var="vs">
       <c:set var='dt'><mc:xvalue bean='vs' path='x:DateTime/x:ExactDateTime'/></c:set>
       <c:set var='approxDt'><mc:xvalue bean='vs' path='x:DateTime/x:ApproximateDateTime/x:Text'/></c:set>
       addEditorRow( 
          '${dt}${approxDt}',
          '<mc:xvalue bean="vs" path="x:Description/x:Text"/>',
          '<mc:xvalue bean="vs" path="x:Test[1]/x:Description/x:Text"/>',
          '<mc:xvalue bean="vs" path="x:Test[1]/x:TestResult[1]/x:Value"/>',
          '<mc:xvalue bean="vs" path="x:Test[1]/x:TestResult[1]/x:Units/x:Unit"/>'
          );
    </c:forEach>
  </stripes:layout-component>
  <stripes:layout-component name="tableHead">
              <th id='dateTime'>Date</th>
              <th id='description.text'>Description</th>
              <th id='test.description.text'>Test</th>
              <th id='test.testResult.value'>Result</th>
              <th id='test.testResult.units.unit'>Units</th>
  </stripes:layout-component>
</stripes:layout-render>
