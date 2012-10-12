<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%--
  Procedures Editor

  Renders a div containing a form for editing procedures

  See Javascript in ccreditor.js

  @author Simon Sadedin, MedCommons Inc.
--%>
<stripes:layout-render name="/sectionEditor.jsp" sectionName="Procedures" sectionPrefix="procedures" sectionTitle="Procedures">
  <stripes:layout-component name="initialize">
    <mc:xnode bean='notificationForm' path='/x:ContinuityOfCareRecord/x:Body/x:Procedures/x:Procedure' forceList="true" name='procedures'/>
    <c:forEach items="${procedures}" var="proc">
       <c:set var='dt'><mc:xvalue bean='proc' path='x:DateTime/x:ExactDateTime'/></c:set>
       <c:set var='approxDt'><mc:xvalue bean='proc' path='x:DateTime/x:ApproximateDateTime/x:Text'/></c:set>
       addEditorRow( 
          '${dt}${approxDt}',
          '<mc:xvalue bean="proc" path="x:Type/x:Text"/>',
          '<mc:xvalue bean="proc" path="x:Description/x:Code/x:Value"/>',
          '<mc:xvalue bean="proc" path="x:Description/x:Text"/>',
          '<mc:xvalue bean="proc" path="x:Locations/x:Location[1=position()]/x:Description/x:Text"/>',
          '<mc:xvalue bean="proc" path="x:Status/x:Text"/>'
          );
    </c:forEach>
  </stripes:layout-component>
  <stripes:layout-component name="tableHead">
              <th id='dateTime'>Date</th>
              <th id='type.text'>Type</th>
              <th id='description.code.value'>Code</th>
              <th id='description.text'>Description</th>
              <th id='locations.location.description.text'>Location</th>
              <th id='status.text'>Status</th>
  </stripes:layout-component>
</stripes:layout-render>
