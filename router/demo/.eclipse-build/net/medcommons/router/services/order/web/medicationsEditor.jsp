<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://stripes.sourceforge.net/stripes.tld" prefix="stripes" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%--
  Medications Editor

  Renders a div containing a form for editing medications

  See Javascript in ccreditor.js

  @author Simon Sadedin, MedCommons Inc.
--%>
<stripes:layout-render name="/sectionEditor.jsp" sectionName="Medications" sectionPrefix="medications" sectionTitle="Medications">
  <stripes:layout-component name="initialize">
    <mc:xnode bean='notificationForm' path='/x:ContinuityOfCareRecord/x:Body/x:Medications/x:Medication' forceList="true" name='medications'/>
    <c:forEach items="${medications}" var="med">
       <c:set var='dt'><mc:xvalue bean='med' path='x:DateTime/x:ExactDateTime'/></c:set>
       <c:set var='approxDt'><mc:xvalue bean='med' path='x:DateTime/x:ApproximateDateTime/x:Text'/></c:set>
       addEditorRow( 
          <%-- '<mc:xvalue bean="med" path="x:CCRDataObjectID"/>', ) --%>
          '${dt}${approxDt}',
          '<mc:xvalue bean="med" path="x:Product/x:ProductName/x:Text"/>',
          '<mc:xvalue bean="med" path="x:Product/x:BrandName/x:Text"/>',
          '<mc:xvalue bean="med" path="x:Product/x:Strength[1]/x:Value"/>',
          '<mc:xvalue bean="med" path="x:Product/x:Strength[1]/x:Units/x:Unit"/>',
          '<mc:xvalue bean="med" path="x:Product/x:Form/x:Text"/>',
          '<mc:xvalue bean="med" path="x:Status/x:Text"/>'
          );
    </c:forEach>
  </stripes:layout-component>
  <stripes:layout-component name="tableHead">
              <th id='dateTime'>Date</th>
              <th id='product.productName.text'>Name</th>
              <th id='product.brandName.text'>Brand</th>
              <th id='product.strength.value'>Strength</th>
              <th id='product.strength.units.unit'>Units</th>
              <th id='product.form.text'>Form</th>
              <th id='status.text'>Status</th>
  </stripes:layout-component>
</stripes:layout-render>
