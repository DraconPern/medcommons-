<#include "basicPage.ftl">
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign s=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<@basic_layout title="Account ID Mismatch">
  <style type="text/css">
  form input {
    margin: 0px 5px 0px 20px;
  }
  </style>
  <h3 class="headline">Account ID Mismatch</h3>
  <p>The patient in the CCR you are importing has MedCommons Account ID different to that of 
     <#if desktop.patientMode>
      your own account.
     <#else>
      the patient account you are importing into.
     </#if>
  A MedCommons CCR cannot have more than one MedCommons Account ID.</p>
  <p>Please select an option:
  <@s.form name="updateCCRIDForm" action="Import.action" method="post">
    <@s.hidden name='patientId'/>
    <input type='hidden' name='ccrIndex' value='${ccrIndex}'/>
    <input name="idaction" type="radio" value="replace" checked="true">Replace the Patient Account ID with that of the account you are currently using</input>
    <br/>
    <br/>
    <input name="idaction" type="radio" value="remove">Remove the Patient Account ID from the CCR</input>
    <br/>
    <br/>
    <input name="idaction" type="radio" value="cancelImport">Cancel this Import</input>
    <br/>
    <br/>
    &nbsp;&nbsp;&nbsp;<input type="submit" name="Continue" value="Continue"/>
  </@s.form>
</@basic_layout>
