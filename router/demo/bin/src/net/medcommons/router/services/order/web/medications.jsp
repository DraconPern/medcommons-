<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ page isELIgnored="false" %> 
<!--
 Copyright 2005 MedCommons Inc.   All Rights Reserved.
-->
<script language="JavaScript">
  function setOrderWindowAsMainImageContent() {
    var mainImageDiv = document.getElementById('mainImage');
    mainImageDiv.innerHTML=
        "<iframe id='orderWindow' onload='window.parent.xdsLoaded();' src='about:blank' name='orderWindow' width='750' height='750' style='border-style: none;'/>";  
    hideEditor();
  } 

  function addMedRow() {
    alert("Not impelemented yet");
  }
  function delMedRow() {
    alert("Not impelemented yet");
  }


</script>

<div id="medicationsFormBox" class="ccrEditForm">
<p style="font-weight: bold;">Medications</p>
<p>Select a medication or click the "plus" sign to create a new one:</p>

  <div id="medicationsBox">
    <div style="border-style: solid; border-width: 1px; background-color: #c0cfc4; margin: 5px;" >
      <table class="editorTable" border="1" onmouseover="initTable(this);">
        <tr><th>Type</th><th>Product</th><th>Brand</th><th>Date</th><th>Status</th><th><a href="javascript:addMedRow();" title="Add Row"><img src="images/plus.gif"  border="0"/></a></th></tr>
        <tr id="med1"><td class="freetext">Medication</td><td class="freetext">Aspirin</td><td class="freetext">Bayer</td><td class="freetext">04/28/1995 - now taking</td><td class="freetext">Active</td><td><a href="javascript:delMedRow();" title="Delete Row"><img src="images/cross.gif"  border="0"/></a></td></tr>
        <%--
        <tr id="med2"><td class="freetext"> Supplemental Nutrition</td><td class="freetext">Vitamin C</td><td class="freetext">TabletsRUS</td><td class="freetext">4/23/1992</td><td class="freetext">Active</td><td><a href="javascript:delMedRow();" title="Delete Row"><img src="images/cross.gif"  border="0"/></a></td></tr>
        --%>
      </table>
    </div>
  </div>
  <br/>
  <hr/>
  <div id="medicationDetail" class="editorDetail" >
    <b>Medication Detail</b>
      <p><b>Description:</b></p> 
      <textarea cols="50" rows="4" name="purposeText"></textarea>
      </br>
    <form name="medicationsForm" id="medicationsForm" action="/router/updateCcr.do" 
          onsubmit="setOrderWindowAsMainImageContent();" 
          onkeypress="handleEditorKeyPress();">
       <b>Administered</b><br/>
      Form: <select name="medicationForm">
        <option value="Patient Use">Capsule</option>
        <option value="Referral">Tablet</option>
        <option value="Return from Referral">Injection</option>
        <option value="Extract">IV</option>
      </select>
      Strength: <input type="text" size="3" value="50">
       <select name="medicationStrengthUnits" value="mg">
        <option value="mg">mg</option>
        <option value="Referral">ml</option>
        <option value="Return from Referral">oz</option>
      </select>
      Concentration: <input type="text" size="3" value="50">
      <select name="medicationConcUnits" value="N/A">
        <option value="N/A">N/A</option>
        <option value="mg">mg/ml</option>
        <option value="Referral">ml/gal</option>
        <option value="Return from Referral">oz/gal</option>
      </select>
      <p><b>Dosage</b></p>
      <div style="border-style: solid; border-width: 1px; background-color: #c0cfc4; width: 70%; margin-left: 50px;">
        <table class="dosageTable" border="1" onmouseover="initTable(this);">
          <tr><th>Dose</th><th>Frequency</th><th>Calculation</th><th>Date</th><th><a href="javascript:addMedRow();" title="Add Row"><img src="images/plus.gif"  border="0"/></a></th></tr>
          <tr id="dose1"><td class="freetext">250mg</td><td class="frequency">tid</td><td class="freetext">250mg/day/1 dose</td><td class="freetext">04/28/1995 - now taking</td><td><a href="javascript:delMedRow();" title="Delete Row"><img src="images/cross.gif"  border="0"/></a></td></tr>
          <%-- ssadedin - removed to make demo data consistent with demo CCR --%>
          <%-- <tr id="dose2"><td class="freetext">60mg</td><td class="frequency">tid</td><td class="freetext">20mg/kg/day/3 doses</td><td class="freetext">After March</td><td><a href="javascript:delMedRow();" title="Delete Row"><img src="images/cross.gif"  border="0"/></a></td></tr> --%>
        </table>
      </div>
      </br>

    </form>
  </div>
    <br/>
      <input type="submit" value="Save"/>&nbsp;
      <input type="button" value="Cancel" onclick="hideEditor();"/>
</div>
