<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%--
  Family History Editor

  Renders a div containing a form for editing family history

  See Javascript in ccreditor.js

  @author Simon Sadedin, MedCommons Inc.
--%>
<stripes:layout-render name="/sectionEditor.jsp" sectionName="FamilyHistory" sectionPrefix="familyhistory" sectionTitle="Family History">
  <stripes:layout-component name="initialize">
    <mc:xnode bean='notificationForm' path='/x:ContinuityOfCareRecord/x:Body/x:FamilyHistory/x:FamilyProblemHistory' forceList="true" name='familyHistories'/>
    <c:forEach items="${familyHistories}" var="fh">
       <c:set var='dt'><mc:xvalue bean='fh' path='x:DateTime/x:ExactDateTime'/></c:set>
       <c:set var='approxDt'><mc:xvalue bean='fh' path='x:DateTime/x:ApproximateDateTime/x:Text'/></c:set>
       addEditorRow( 
          '<mc:xvalue bean="fh" path="x:FamilyMember/x:ActorRole/x:Text"/>',
          '<mc:xvalue bean="fh" path="x:Problem/x:Description/x:Text"/>',
          '<mc:xvalue bean="fh" path="x:Problem/x:Description/x:Code/x:Value"/>',
          '${dt}${approxDt}'
          );
    </c:forEach>
  </stripes:layout-component>
  <stripes:layout-component name="tableHead">
    <th id='dateTime'>Date</th>
    <th id='familyMember.actorRole'>Relation</th>
    <th id='problem.description'>Problem Description</th>
    <th id='problem.code'>Code</th>
    <th id='dateTime'>Date/Time</th>
  </stripes:layout-component>
</stripes:layout-render>
