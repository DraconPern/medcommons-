<#include "basicPage.ftl">
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign s=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<@basic_layout title="Account ID Mismatch">
  <style type="text/css">
  form input {
    margin: 0px 5px 0px 20px;
  }
  table.reftable {
    min-width: 400px;
    margin: 10px 50px;
    border-collapse: collapse;
  }
  table.reftable th {
    font-size: 13px;
    border-bottom: 2px solid #555;
  }
  table.reftable td {
    font-size: 12px;
    padding: 3px 20px;
  }
  </style>
  <h3 class="headline">Missing References</h3>
  <p>The CCR you are importing has references to content for which data is missing.</p>
    <table class='reftable'>
      <tr><th>Name</th><th>Type</th></tr>
      <#list actionBean.missingSeries as ref>
        <#if ref.type != 'application/x-medcommons-ccr-history'>
        <tr><td>${ref.displayName}</td><td>${ref.type}</td></tr>
        </#if>
      </#list>
    </table>

  <p>These references cannot be included in the imported CCR unless the reference data is 
     first uploaded.</p>

  <p>Please select an option:
  <@s.form name="updateCCRIDForm" action="Import.action" method="post">
    <input type='hidden' name='ccrIndex' value='${ccrIndex}'/>
    <@s.hidden name='patientId'/>
    <@s.submit name='removeReferences' value="Remove References"/>
    <@s.submit name='cancelImport' value="Cancel Import"/>
  </@s.form>
</@basic_layout>
