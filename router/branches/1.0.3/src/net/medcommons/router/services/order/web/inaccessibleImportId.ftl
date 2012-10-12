<#include "basicPage.ftl">
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign s=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<@basic_layout title="Inaccessible Patient Account">
  <style type="text/css">
  form input {
    margin: 0px 5px 0px 20px;
  }
  </style>
  <h3 class="headline">Inaccessible Patient Account</h3>
  <p>The patient in the CCR you are importing is an existing patient that you do not 
     currently have consent to access.   You may continue to import the CCR, however
     the patient MedCommons Account ID will be removed and a new patient will 
     created for this CCR when you save it.
  <@s.form name="updateCCRIDForm" action="Import.action" method="post">
    <input type='hidden' name='ccrIndex' value='${ccrIndex}'/>
    <input type='hidden' name='patientId' value='${patientId}'/>
    <input type='hidden' name='idaction' value='delete'/>
    <input type='hidden' name='clean' value='true'/>
    <br/>
    &nbsp;&nbsp;&nbsp;<input type="submit" name="updateId" value="Continue Import"/>
  </@s.form>
</@basic_layout>
