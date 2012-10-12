<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign app=actionBean.application>
<table class='shareInfoTable' style='width: 100%;'>
  <tr><th>Application:</th><td>${app.name}</td></tr>
  <tr><th>Code:</th><td>${app.code}</td></tr>
  <#if app.email?exists>
  <tr><th>Email:</th><td>${app.email}</td></tr>
  </#if>
  <#if app.websiteUrl?exists>
  <tr><th>Web Site:</th><td>${app.websiteUrl}</td></tr>
  </#if>
  <tr><th>Key:</th><td>${app.key}</td></tr>
  <#if app.createDateTime?exists>
  <tr><th>Created:</th><td>${app.createDateTime?date}</td></tr>
  </#if>
</table>
