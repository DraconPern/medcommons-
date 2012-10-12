<#include "basicPage.ftl">
<#assign stripes=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<#assign c=JspTaglibs["http://java.sun.com/jsp/jstl/core"]>
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign head>
  <style type='text/css'>
    body {
      margin: 0px;
      font-size: 10pt;
    }
    #content {
      margin: 10px;
    }
    fieldset {
      float: left;
      padding: 7px 1em;
    }
    </style>
</#assign>
<@basic_layout title="Import Account" head=head>
  <#escape x as x?html>
    <div id='content'>
      <h3 class='headline' style='margin: 4px 0px; padding: 0px 0px'>Problem Merging to Account</h3>
      <p>You are not authorized to merge content into the account specified.</p>
      <p>It may be that you specified the account incorrectly or that consent for you to access
         the account has not been granted or was removed.</p>
      <p>Please check the HealthURL you entered and that you have access to modify the specified account
         from the account owner.</p>
         
       <p><button onclick='window.history.back()'>Back</button></p>
    </#escape>
    <script type='text/javascript' src='utils.js'></script>
    <#--
    <@pack.script>
      <src>utils.js</src>
      <src>common.js</src>
    </@pack.script>
    -->
    <script type='text/javascript'>
        parent.setTools([]);
    </script>
</@basic_layout>