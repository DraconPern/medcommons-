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
<%--
  $Id: xdsSubmit.jsp 579 2005-03-23 09:41:23Z ssadedin $ 

  Renders a form containing details for XDS submission.  

--%>
<!--
 Copyright 2005 MedCommons Inc.   All Rights Reserved.
-->
<form method="post" id="xdsDocumentSubmissionForm" name="xdsDocumentSubmissionForm" action="/router/XdsSubmitDocument" enctype="multipart/form-data">

  <%-- hidden fields --%>
  <input name="sourcePatientId" type="hidden"/>
  <input name="sourcePatientInfo" type="hidden"/>
  <input name="authorPerson" type="hidden" value="^Wynn^Donald^^^Dr^MD"/>
  <input name="authorDepartment" type="hidden" value="Laboratory"/>
  <input name="authorInstitution" type="hidden" value="MedCommons Clinic"/>
  <input name="legalAuthenticator" type="hidden" value="^Wynn^Donald^^^Dr^MD"/>
  <input name="folderName" type="hidden" value="Referral"/>
  <input name="folderDescription" type="hidden" value="Referral From MedCommons"/>
  <input name="folderUuid" type="hidden" value="theFolder"/>
  <input name="servicesStartTime" type="hidden" value="20050101125959"/>
  <input name="servicesStopTime" type="hidden" value="20050102125959"/>
  <input name="creationTime" type="hidden" value="20050101120000"/>
  <input name="languageCode" type="hidden" value="en-us"/>
  <input name="id" type="hidden" value=""/>
  <input name="uniqueIdentifier" type="hidden" value="36784664"/>

    <%--
     One of:

      Communication (Communication)
      Evaluation and management (Evaluation and management)
      Conference (Conference)
      Case conference (Case conference)
      Consult (Consult)
      Confirmatory consultation (Confirmatory consultation)
      Counseling (Counseling)
      Group counseling (Group counseling)
      Education (Education)
      History and Physical (History and Physical)
      Admission history and physical (Admission history and physical)
      Comprehensive history and physical (Comprehensive history and physical)
      Targeted history and physical (Targeted history and physical)
      Initial evaluation (Initial evaluation)
      Admission evaluation (Admission evaluation)
      Pre-operative evaluation and management (Pre-operative evaluation and management)
      Subsequent evaluation (Subsequent evaluation)
      Summarization of episode (Summarization of episode)
      Transfer summarization (Transfer summarization)
      Discharge summarization (Discharge summarization)
      Summary of death (Summary of death)
      Transfer of care referral (Transfer of care referral)
      Supervisory direction (Supervisory direction)
      Telephone encounter (Telephone encounter)
      Interventional Procedure (Interventional Procedure)
      Operative (Operative)
      Pathology Procedure (Pathology Procedure)
      Autopsy (Autopsy)
     --%>
    <input type="hidden" name="contentTypeCode" value="Communication"/>

    <%--
      One of:
        Home (Home )
        Assisted Living (Assisted Living )
        Home Health Care (Home Health Care )
        Hospital Setting (Hospital Setting )
        Acute care hospital (Acute care hospital )
        Hospital Unit (Hospital Unit )
        Critical Care Unit (Critical Care Unit )
        Emergency Department (Emergency Department )
        Observation Ward (Observation Ward )
        Rehabilitation hospital (Rehabilitation hospital )
        Nursing Home (Nursing Home )
        Skilled Nursing Facility (Skilled Nursing Facility )
        Outpatient (Outpatient)
    --%>

  	<input type="hidden" name="healthCareFacilityTypeCode" value="Outpatient"/>

    <%--
    One of:
      C (Celebrity )
      D (Clinician )
      I (Individual )
      N (Normal )
      R (Restricted )
      S (Sensitive )
      T (Taboo)
    --%>
   <input type="hidden" name="confidentialityCode" value="T"/>

    <%--
      One of:
        Case Manager (Case Manager )
        Chemotherapy (Chemotherapy )
        Chiropractic (Chiropractic )
        Critical Care (Critical Care )
        Diabetology (Diabetology )
        Dialysis (Dialysis )
        Emergency (Emergency )
        Endocrinology (Endocrinology )
        Gastroenterology (Gastroenterology )
        General Medicine (General Medicine )
        General Surgery (General Surgery )
        Labor and Delivery (Labor and Delivery )
        Laboratory (Laboratory )
        Multidisciplinary (Multidisciplinary )
        Neonatal Intensive Care (Neonatal Intensive Care )
        Neurosurgery (Neurosurgery )
        Nursery (Nursery )
        Nursing (Nursing )
        Obstetrics (Obstetrics )
        Occupational Therapy (Occupational Therapy )
        Ophthalmology (Ophthalmology )
        Optometry (Optometry )
        Orthopedics (Orthopedics )
        Otorhinolaryngology (Otorhinolaryngology )
        Pathology (Pathology )
        Perioperative (Perioperative )
        Pharmacacy (Pharmacacy )
        Physical Medicine (Physical Medicine )
        Plastic Surgery (Plastic Surgery )
        Podiatry (Podiatry )
        Psychiatry (Psychiatry )
        Pulmonary (Pulmonary )
        Radiology (Radiology )
        Social Services (Social Services )
        Speech Therapy (Speech Therapy )
        Thyroidology (Thyroidology )
        Tumor Board (Tumor Board )
        Urology (Urology )
        Veterinary Medicine (Veterinary Medicine )
    --%>
   <input type="hidden" name="practiceSettingCode" value="Case Manager"/>

    <%--
    One of:
      Communication (Communication)
      Evaluation and management (Evaluation and management)
      Conference (Conference)
      Case conference (Case conference)
      Consult (Consult)
      Confirmatory consultation (Confirmatory consultation)
      Counseling (Counseling)
      Group counseling (Group counseling)
      Education (Education)
      History and Physical (History and Physical)
      Admission history and physical (Admission history and physical)
      Comprehensive history and physical (Comprehensive history and physical)
      Targeted history and physical (Targeted history and physical)
      Initial evaluation (Initial evaluation)
      Admission evaluation (Admission evaluation)
      Pre-operative evaluation and management (Pre-operative evaluation and management)
      Subsequent evaluation (Subsequent evaluation)
      Summarization of episode (Summarization of episode)
      Transfer summarization (Transfer summarization)
      Discharge summarization (Discharge summarization)
      Summary of death (Summary of death)
      Transfer of care referral (Transfer of care referral)
      Supervisory direction (Supervisory direction)
      Telephone encounter (Telephone encounter)
      Interventional Procedure (Interventional Procedure)
      Operative (Operative)
      Pathology Procedure (Pathology Procedure)
      Autopsy (Autopsy)
    --%>
    <input type="hidden" name="classCode" value="Consult"/>

  <%--
   <input type="file" name="uploadedFile1" size="100" value=""/>
   --%>

  <%--
    One of:
    application/pdf (application/pdf)
    text/x-cdar1+xml" (text/x-cdar1+xml)
    application/x-ccr+xml" (application/x-ccr+xml)
    application/x-hl7" (application/x-hl7)
    --%>
  <input type="hidden" name="mimeType1" value="application/x-ccr+xml"/>

  <%--
    One of:
  	PDF/IHE 1.x (PDF/IHE 1.x)
    CDA/IHE 1.0" (CDA/IHE 1.0)
    CCR/IHE 0.9" (CCR/IHE 0.9)
  --%>
  <input type="hidden" name="formatCode1"/>

  <%--
 	<input type="file" name="uploadedFile2" size="100" value=""/>
  --%>

  <%--
    One of
    application/pdf SELECTED (application/pdf)
    text/x-cdar1+xml (text/x-cdar1+xml)
    application/x-ccr+xml" (application/x-ccr+xml)
    application/x-hl7 (application/x-hl7)
   --%>

  <input type="hidden" name="mimeType2" value="text/x-cdar1+xml"/>

    <%-- One of 
    PDF/IHE 1.x (PDF/IHE 1.x)
    CDA/IHE 1.0 (CDA/IHE 1.0)
    CCR/IHE 0.9 (CCR/IHE 0.9)
    --%> 
  <input type="hidden" name="formatCode2"/>

  <%-- eg. ObjectNodes.xsl --%>
  <input name="stylesheet" value="ObjectNodes.xsl" type="hidden"/>

  <%-- Use to submit document from session variable --%>
  <input type="hidden" id="attachSessionCcrDoc" name="attachSessionCcrDoc"/>

  <%-- Use to submit document from session variable --%>
  <input type="hidden" id="attachSessionCdaDoc" name="attachSessionCDADoc"/>

  <%-- optional parameter to request forwarding to a jsp instead of a stylesheet --%>
  <input type="hidden" name="forward"/>
</form>
