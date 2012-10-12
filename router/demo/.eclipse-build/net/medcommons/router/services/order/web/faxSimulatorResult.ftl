<#include "basicPage.ftl">
<@basic_layout title="Fax Send Result">
    <h3>Fax Send Result</h3>
    <p>Cover sheet id <b>${actionBean.coverId}</b> was created for account ${actionBean.accid}.</p>
    <p>Fax was delivered to ${actionBean.faxServletUrl} in ${actionBean.faxTimeMillis} ms</p>
    <p>Fax Servlet Response:</p>
<pre>${actionBean.faxServletResponse}</pre>
    <p><a href='faxsimulator'>Return to Fax Simulator</a>&nbsp;<a href='${actionBean.acctUrl}'>Open Current CCR</a></p>
</@basic_layout>

