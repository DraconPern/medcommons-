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
    hideAddReport();
  } 

  function hideAddReport() {
    document.getElementById("xdsAddReportForm").style.display='none';
    disableKeyCapture=false;
  }

  function handleAddReportKeyPress(evt) {
    if(evt == null)
      evt = window.event;
    if(evt.keyCode == 27) { // escape key
      hideAddReport();
    }
  }
</script>

<p>Select a code and enter a description for your submission:</p>


<form name="addReportForm" id="addReportForm" 
      action="/router/addReport.do" 
      onsubmit="setOrderWindowAsMainImageContent();" 
      target="orderWindow"
      onkeypress="handleAddReportKeyPress();">

  <p>Purpose code:&nbsp;&nbsp;
  <select name="codeType" style="position: relative; top: 1px;" >
    <option value="Patient Use">Patient Use</option>
    <option value="Referral">Referral</option>
    <option value="Return from Referral">Return from Referral</option>
    <option value="Extract">Extract</option>
  </select></p>

  <p>Description:</p> 
  <textarea cols="60" rows="10" name="purposeText">
  </textarea>
  </br>
  </br>
  <input type="submit" value="Add Report"/>&nbsp;
  <input type="button" value="Cancel" onclick="hideAddReport();"/>
</form>


