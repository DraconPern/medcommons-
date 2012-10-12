<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<table style='width: 100%;'>
  <tr><th>Name:</th><td>${actionBean.groupInfo.groupName}</td></tr>
  <tr><th>Email:</th><td>${actionBean.groupInfo.email}</td></tr>
  <tr><th>Domain:</th><td><@mc.config property='AccountsBaseUrl'/></td></tr>
  <#if actionBean.groupInfo.groupCreateDateTime?exists>
  <tr><th>Created:</th><td>${actionBean.groupInfo.groupCreateDateTime}</td></tr>
  </#if>
</table>
