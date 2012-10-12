<#include "basicPage.ftl">
<@basic_layout title="OpenID Verification Failure">
    <h3>Unable to Access Document</h3>
    <p>We were unable to access the requested document using your OpenID.</p>
    <p>It may be that the verification failed or was cancelled or it is possible that
       the Identity that you used does not have access to this CCR.</p>
    <input type="button" value="Close Window" onclick="window.close();"/>
</@basic_layout>
