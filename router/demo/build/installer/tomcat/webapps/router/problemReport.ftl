<#include "basicPage.ftl">
<@basic_layout title="Problem Report">
  <style type='text/css'>
    body { 
      margin: 10px;
    }
    #log {
    }
  </style>

  <h3>Problem Report</h3>
  <h4>Description</h4>
  <p>${actionBean.description?html}</p>

  <h4>Log File</h4>
  <p id='log'>
    <#if actionBean.logFileLength gt 0 >
    <a href='ProblemReport.action?download=true&problemId=${actionBean.problemId}'>Click here to view Log File</a> 
      (${actionBean.logFileLength} bytes, ${actionBean.logFileWritten?datetime})
    <#else>
      No log file was submitted.
    </#if>
    </p>

  <h4>Javascript Log</h4>
  <p id='jslog'>
    <#if actionBean.jsLogLength gt 0 >
    <a href='ProblemReport.action?downloadjs=true&problemId=${actionBean.problemId}'>Click here to view Javascript Log</a> 
      (${actionBean.jsLogLength} bytes)
    <#else>
      No javascript log was submitted.
    </#if>
  </p>
</@basic_layout>
