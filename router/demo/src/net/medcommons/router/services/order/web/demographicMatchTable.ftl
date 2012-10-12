<#assign stripes=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<#assign c=JspTaglibs["http://java.sun.com/jsp/jstl/core"]>
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign action=importAction?default(actionBean)>
<#assign sourceCCR=action.sourceCCR>
<table>
<thead>
<tr><th>&nbsp;</th><th>Import Source</th><th>Import Destination</th><th>Match?</th></tr>
</thead>
<tbody>
<tr class='row0'>
  <th>Account</th>
  <td><a href='${action.sourceUrl}' onclick='return open_source_hurl();' target='_new'><img src='images/hurl.png'/> <@mc.medcommonsId>${action.sourceAccount?default('')}</@></a></td>
  <td>
    <#if action.targetCCR?exists >
    <a href='<@mc.config property='RemoteAccessAddress'/>/ccrs/${action.toAccount}'
       onclick='return open_target_hurl();'><img src='images/hurl.png'/> <@mc.medcommonsId>${action.toAccount}</@></a>
    <#else>
       <@mc.medcommonsId>${action.toAccount}</@>  
    </#if>
  </td>
  <td>&nbsp;</td>
</tr>
<#macro tdmatch name=''>
  <td class='match'><#if action.mismatches[name]?exists><img title='This information is different between the CCR you are importing and the Current CCR in the account you are importing into' src='images/icon_deletelink.gif'/><#else><img title='This information matches in both CCRs' src='images/smalltick.png'/></#if></td>
</#macro>
<tr class='row1'>
  <th>Patient Name</th>
  <td>${sourceCCR.patientGivenName} ${sourceCCR.patientFamilyName}</td>
  <td>
    <#if action.targetCCR??>
       ${action.targetCCR.patientGivenName} ${action.targetCCR.patientFamilyName}
      <#else>
      ${action.targetSettings.firstName?default('')} ${action.targetSettings.lastName?default('')}
   </#if>
  </td>
  <@tdmatch name='patientName'/>
</tr>
<tr class='row0'>
  <th>Sex</th>
  <td>${sourceCCR.patientGender?default('Unknown')}</td>
  <td><#if action.targetCCR??>${action.targetCCR.patientGender?default('Unknown')}<#else>N/A</#if></td>
  <@tdmatch name='patientSex'/>
</tr>
<tr class='row1'>
  <th>Date of Birth</th>
  <td>${(sourceCCR.patientDateOfBirth?date)!'Unknown'}</td>
  <td><#if action.targetCCR??>${(action.targetCCR.patientDateOfBirth?date)!'Unknown'}<#else>N/A</#if></td>
  <@tdmatch name='patientExactDateOfBirth'/>
</tr>
<tr><th colspan="3">&nbsp;</th></tr>
<tr>
  <th>&nbsp;</th>
  <#if (action.mismatches?size > 0) >
  <th><input id='refreshButton' type='button' value='Refresh Data' onclick='refreshMatches()'/></th>
  <#else>
  <th><input id='importButton' type='button' value='Import Account Contents' onclick='beginImport()'/></th>
  </#if>
  <th><input id='cancelButton' type='button' value='Cancel' title='Aborts the account import and takes you to your Current CCR' onclick='goCurrentCCR();'/></th>
</tr>
</tbody>
</table>
