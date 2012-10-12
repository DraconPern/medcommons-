<#include "basicPage.ftl">
<#assign stripes=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<#assign c=JspTaglibs["http://java.sun.com/jsp/jstl/core"]>
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#-- <#assign pack=JspTaglibs["http://packtag.sf.net"]> -->
<#assign action=importAction?default(actionBean)>
<#assign sourceCCR=action.sourceCCR>
<#assign head>
  <style type='text/css'>
    body {
      margin: 0px;
      <#-- don't know why bug FF has layout problem when showing dialog 
           unless we define this (!?) -->
      border: solid 1px white;
      font-size: 10pt;
    }
    #content {
      margin: 10px;
    }

    fieldset {
      float: left;
      padding: 7px 1em;
    }

    #importDlgContents {
      height: 120px;
    }

    .progressBar {
      position: relative;
      border: solid 1px black;
      text-align: left;
      width: 88%;
    }
    #importProgressInner *, #importProgressText {
      vertical-align: middle;
    }
    #importProgressInner {
      background-color: #ccc;
      background-color: #B2DB81;
      width: 0%;
      height: 28px;
      text-align: center;
      padding: 0px;
    }
    #importProgressText {
      position: absolute;
      left: 45%;
      top: 0px;
      padding: 6px;
      height: 100%;
    }
    #importStatus, .progressBar {
      margin: 5px 10px 5px 10px;
    }
    #importStatus {
      margin-top: 16px;
    }
    #importDlg p {
      margin-top: 15px;
      margin-left: 10px;
    }
    #importFinished {
      display: none;
      border: 1px solid #aaa;
      border-right: 1px solid #999;
      border-bottom: 1px solid #999;
      padding: 10px 5px;
      background-color: #F7F5CD;
      margin: 4px 10px;
    }
    #importFinished * {
      vertical-align: middle;
    }
    legend {
      font-weight: bold;
    }
    table {
      background-color: #f0f0f0;
      background-color: white;
      margin: 0% 10%;
    }
    table tr * {
      font-size: 10pt;
      color: #333;
    }
    table tbody tr th {
      max-width: 23em;
      text-align: right;
      padding-right: 2em;
      vertical-align: middle;
    }
    table thead tr th {
      text-align: left;
    }
    table tr td, table tr th {
      padding: 5px 20px;
    }
    table tr {
      border-bottom: 1px solid #999;
      margin: 0px;
    }
    table tr td a {
      color: #97AA97;
      text-decoration: none;
    }
    a:hover {
      text-decoration: underline;
    }
    h3.headline {
      font-size: 15px;
      margin-left: 6px;
    }
    table tr td.match {
      text-align: center;
    }
  </style>
  <script type='text/javascript'>
    function resetImport() {
      if(el('importButton'))
        el('importButton').disabled=false; 
      window.importFinished = false;
    }
    function goUrl(url) {
      window.parent.location = url;
    }
    function goCurrentCCR() {
     goUrl('ccrs/'+accid+'?auth='+auth);
    }
    function refreshMatches() {
       if(sourceWindow)
         sourceWindow.close();
       var yuic = YAHOO.util.Connect;
       yuic.setForm(document.importForm);
       yuic.asyncRequest('POST', 'AccountImport.action?refresh', {
         success: function(r) {
           el('tableWrapper').innerHTML = r.responseText;
         }
       });
    }
    function beginImport() {
      if(el('importButton'))
        el('importButton').disabled=true;
      if(sourceWindow) {
        sourceWindow.close();
        window.parent.disable_auto_close = false;
      }
      dialog('importDlg', 'Importing Account Contents',  
        '<div id="importDlgContents">'
      + '<p>Please wait while your account is being imported.</p>'
      + '<div id="importProgress" class="progressBar"><div id="importProgressInner"></div><div id="importProgressText">0 %</div></div>'
      + '<div id="importStatus"></div>'
      + '<div id="importFinished"><img src="images/tick.png"> Your import was successful.  Click OK to continue.</div>'
      + '</div>',
         460,
         [
          { text:'OK', handler: function() { if(window.resultURL) goUrl(resultURL); else goCurrentCCR(); } },
          { text:'Cancel', handler: function() { if(!window.importFinished) { YAHOO.util.Connect.asyncRequest('GET', 'AccountImport.action?cancel'); } else { resetImport(); this.destroy(); } } }
         ],
        function() {
          var yuic = YAHOO.util.Connect;
          importDlg.getButtons()[0].set("disabled",true);
          importDlg.cancelEvent.subscribe(resetImport);
          yuic.setForm(document.importForm);
          yuic.asyncRequest('POST', 'AccountImport.action?begin', {
            success: function(req) {
                var sts = eval(req.responseText);
                window.resultURL = sts.resultURL;
            }
          });
          var intervalId = window.setInterval(function() {
            yuic.setForm(document.importForm);
            yuic.asyncRequest('POST', 'AccountImport.action?status', {
              success: function(req) {
                log("received import status " + req.responseText);
                var status = eval(req.responseText);
                var pct = (parseFloat(status.progress)*100);
                el('importProgressText').innerHTML = Math.round(pct) + '%';
                el('importProgressInner').style.width =  pct + '%';
                el('importStatus').innerHTML = status.status;
                if((pct >= 100.0) || status.errorFlag) {
                  clearInterval(intervalId);
                  window.importFinished = true;
                  if(status.state.name == 'FAILED_MERGE') {
                    el('importDlgContents').style.height = '160px';
                  }
                  else
                  if(!status.errorFlag && status.state.name != 'CANCELLED') {
                    show('importFinished');
                    hide('importProgress','importStatus');
                    importDlg.getButtons()[0].set("disabled",false);
                    importDlg.getButtons()[1].set("disabled",true);
                  }
                }
              }
            });
          },2000);
        });
    }
    var sourceAuth = '${action.sourceAuth?js_string}';
    var sourceUrl = '${action.sourceUrl?js_string}';
    var sourceWindow = null;
    function open_source_hurl() {
      window.parent.disable_auto_close = true;
      if(sourceWindow)
        sourceWindow.close();
      sourceWindow = window.open(sourceUrl+'?auth='+sourceAuth+'&mode=editdg');
      return false;
    }
    function open_target_hurl() {
      window.parent.disable_auto_close = true;
      if(sourceWindow)
        sourceWindow.close();
      sourceWindow = window.open('CurrentCCR.action?m=editdg&a=${desktop.ownerMedCommonsId?js_string}&auth=${desktop.authenticationToken?js_string}');
      return false;
    }
    var accid = '${desktop.ownerPrincipal.mcId?js_string}';
    var auth = '${desktop.authenticationToken?js_string}'; 

    function init() {
      <#if action.auto >
        yuiLoader().insert(beginImport);
      <#else>
        setTimeout(yuiLoader,1000); 
      </#if>
    }
  </script>
</#assign>
<@basic_layout title="Import Account" head=head onload='init();'>
  <#escape x as x?html>
    <div id='content'>
      <h3 class='headline' style='margin: 4px 0px; padding: 0px 0px'>Account Import</h3>
      <#if action.mismatches?has_content>
      <p>The CCR you are importing has demographic data that mismatches that of 
         health record you are importing into.</p>
      <p>Please check the information below and use the HealthURL links to modify
         your CCRs to match before proceeding with import.</p>
      </#if>
      <hr/>
      <#if action.auto>
        <p>Starting import ... please wait</p>
      </#if>
      <@stripes.errors/>
      <#if !action.hasValidationErrors >
        <div id='tableWrapper'>
        <#if !action.auto>
          <#include "demographicMatchTable.ftl"/>
        </#if>
        </div>
        <form name='importForm' action='AccountImport.action'>
          <input type='hidden' name='auth' value='${desktop.authenticationToken}'/>
          <input type='hidden' name='sourceUrl' value='${action.sourceUrl}'/>
          <input type='hidden' name='sourceAuth' value='${action.sourceAuth}'/>
          <input type='hidden' name='sourceSecret' value='${action.sourceSecret?default("")}'/>
        </form>
        <hr/>
      </#if>
      </div>
    </#escape>
    <script type='text/javascript' src='yui-2.6.0/yuiloader/yuiloader-min.js'></script>
    <script type='text/javascript' src='utils.js'></script>
    <script type='text/javascript' src='common.js'></script>
    <#--
    <@pack.script>
      <src>utils.js</src>
      <src>common.js</src>
    </@pack.script>
    -->
</@basic_layout>
<@c.remove var='importAction'/>
