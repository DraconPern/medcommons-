<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %> 
<%--
  MedCommons Ereferral Letter

  This page shows the letter allows a user
  to send a referral to another user via email.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="patient" value="${ccr.patient}"/>
<mc:xnode bean="notificationForm" path="patientPerson" name="patientPerson"/>
<html>
  <head>
    <link href="consentForm.css" rel="stylesheet" type="text/css"/>
    <script language="JavaScript">
      /**
       * log - useful to send log statements to external logger
       * (used with Badboy to log statements to log file)
       */
      var enableLog = true;
      function log(msg) {
        try {
          if(enableLog) {
            window.external.info(msg);
          }
        }
        catch(er) {
          enableLog=false;
        }
      }
  
      function trim(aValue) {
        return aValue.replace(/^\s+/g, "").replace(/\s+$/g, "");
      }
    
      function onSwitchTab(url) {
        document.referralForm.action=url; 
        document.referralForm.target='';
        document.referralForm.submit();
        return true;
      }

      function goDesktop() {
        document.referralForm.action='updateCcr.do?ccrIndex=${ccrIndex}&forward=desktop'; 
        document.referralForm.target='';
        document.referralForm.submit();
      }

      function unhighlight() {
        //this.style.backgroundColor='#f3f3f3';
        this.style.backgroundColor='#ffffff';
      }

      function highlight(field) {
        if(field.select != null)
          field.select();
        field.onblur=unhighlight;
        field.style.backgroundColor='#e6e6e6';
      }
      
      function init() {
        window.parent.setTools( [ 
            // ["Show Contacts *","parent.niy();"],
            // ["Digital Signature *","parent.niy();"],
            ["Print","print();"]
            // ["Create CCR","parent.niy();"],
            // ["Save *","parent.niy();"],
            // ["Clear Form","activeForm.reset();"]
            ]);
        window.activeForm=document.referralForm;
      }

      <logic:present name="ccr">      
        window.parent.setAdvertisedCcr('${desktop.ownerMedCommonsId}','${desktop.accessPin}');
      </logic:present>
    </script>
  </head>
  <body onload="init();">
  <h3>CONSENT FORM FOR REFERRING PRACTITIONER TO PROVISION A PATIENT</h3>
  <div style="z-index: 5;">
  <form name="referralForm" method="post" onsubmit="return goReferralEmail();" action="/router/updateCcr.do">
    <div id="businessCardDiv">
      <img id="businessCard" src="images/businesscard_gropper.png"/>
    </div>
    <div id="dividerLine">&nbsp;</div>
    <div id="leftnav">
      <table id="ccrTable" cellpadding="0" cellspacing="1" width="370px">
      

      <tr><td class="leftnav">PATIENT</td><td class="ltrtxt"><mc:xvalue bean="notificationForm" path="patientFamilyName"/>, <mc:xvalue bean="notificationForm" path="patientGivenName"/> <mc:xvalue bean="notificationForm" path="patientMiddleName"/></td></tr>
      <tr> <mc:xnode bean="notificationForm" path="patientAge" name="patientAge"/> 
           <td class="leftnav">DOB</td>
           <td class="ltrtxt"><mc:xvalue bean="notificationForm" path="patientDateOfBirth" /></td>
      </tr>
      <tr><td class="leftnav">AGE SEX</td>
      <td class="ltrtxt"><c:if test="${ (!empty patientAge) && (!empty patient.dateOfBirth) }">/</c:if >${patientAge.textTrim}<c:choose><c:when test='${patient.gender=="Male"}'>M</c:when><c:when test='${patient.gender=="Female"}'>F</c:when><c:otherwise>?</c:otherwise></c:choose></td></tr>
      <tr><td class="leftnav"><b>PATIENT ID</b></td><td class="ltrtxt bolded">
                <mc:xvalue bean="notificationForm" path="patientDICOMId" />
      </td></tr>
      <tr><td class="leftnav">&nbsp</td><td class="ltrtxt">&nbsp</td></tr>
      <tr>
          <td class="leftnav">ADDRESS</td>
          <td class="ltrtxt">
                <mc:xvalue bean="notificationForm" path="patientAddress1" />,<br/><mc:xvalue bean="patientPerson" path="personCityStatePostal" />
          </td>
      </tr>
      <tr>
          <td class="leftnav <c:if test='${empty patient.phoneNumbers}'>missing</c:if>">PHONE</td>
          <td class="ltrtxt"><c:if test='${!empty patient.phoneNumbers}'>
             <mc:xvalue bean="notificationForm" path="patientPhoneNumber"/>
           </c:if></td>
      </tr>

      
      <tr><td class="leftnav">&nbsp</td><td class="ltrtxt">&nbsp</td></tr>
    </table>

        <div id="consentTextDiv" style="font-size: 11px;"><ul>
          Privacy, Policy and Terms of Use<br/>
          June 1, 2005
            <li>	NOT FOR CLINICAL USE - This is a trial version of the MedCommons system. Please send comments to mailto:cso@medcommons.org</li>
            <li>	The patient is MedCommons principal customer and your privacy is key to maintaining your trust as your health information bank. Your information will not be sold, mined, aggregated unless required by a court of law.</li>
            <li>	MedCommons encrypts your private information that we manage on your behalf. The current version of the system is secured with a PIN code that you must save. We do not have a copy and cannot recover the information if you lose the PIN.</li>
            <li>	Each CCR in MedCommons is private and uniquely meant for a specific destination. Sharing of accounts and widespread distribution of PINs defeats the purpose of MedCommons and is a violation of our Terms of Use. </li>
            <li>	Long-term health record accounts are not available during the trial period. PINs and the information they control will expire without notice.</li>
            <li>	Email addresses we collect during this trial period will be used to notify users when long-term accounts become available. The addresses will not be sold or passed on to any other party and we will gladly remove your email from our list at any time.</li>
            <li>	MedCommons security practices meet or exceed HIPAA requirements. To avoid unauthorized use, email notification of information transfer is sent to all parties to a transfer that have supplied an email address on the Consent form.</li>
            <li>	This policy will be updated as the trial period continues and long-term accounts are introduced. Please check this page and the associated documentation.</li>
            <li>	MedCommons is a transport and interoperability service for physicians and patients. Use MedCommons only to communicate with and according to the instructions of your health care provider. NOT FOR EMERGENCY USE. IN CASE OF EMERGENCY, DIAL 911.</li>
         </ul>
          <span style="text-align: justify; margin-left: 30px; font-size: 14px;">
            <input type="checkbox" name="accept" value="Accept">I accept</input>
            <table style="margin-left: 30px; margin-top: 5px;">
              <tr><td width="100">Print Name:</td><td style="border-bottom: solid; border-width: 1px; border-color: black;" width="200">&nbsp;</td>
                  <td>&nbsp;</td></tr>
              <tr><td>Signature:</td><td style="border-bottom: solid; border-width: 1px; border-color: black;" width="200">&nbsp;</td>
                  <td align="right">&nbsp;</td></tr>
              <tr><td>Date:</td><td style="border-bottom: solid; border-width: 1px; border-color: black;" width="200" align="center">&nbsp;</td>
                  <td align="right" width="80"><input type="button" name="print" value=" Print " style="font-size: 10px;" onclick="window.print();"/></td></tr>
            </table>
          </span>
         </div>
    </div>

    </form>
    </div>
  </body>
</html>
