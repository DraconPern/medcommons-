<%@ page language="java"%>
<%@ include file="/taglibs.inc.jsp" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>Document Upload</title>
    <link href="main.css" rel="stylesheet" type="text/css">
    <style type="text/css">
      body {
        font-family: arial,helvetica;
        margin: 8px;
      }

      td {
        font-size: 12px;
        text-align: left;
      }

      .errorbox p {
        color: red;
      }

      errorbox {
        margin-top: 10px;
      }
    </style>
    <script type='text/javascript'>
      parent.hidePatientHeader();
      function cancelImport() {
        if(parent.getTabs().length > 1) {
          // Highlight another tab and close this one
          var me = parent.currentTab;
          var tabs = parent.getTabs();
          for(var i = 0; i<tabs.length; ++i) {
              var t = tabs[i];
              if(t != parent.currentTab) {
                parent.removeTab(me);
                parent.showTab(t);
                break;
              }
          }
        }
        else
          document.location.href='logon.jsp';
      }
    </script>
  </head>
  <body style="position: relative; left: 10px;">
    <logic:present name="message" scope="request">
    <div id="errorbox" class="errorbox">
      <p><%=request.getAttribute("message")%></p>
      <logic:present name="errormessage" scope="request">
        <p><%=request.getAttribute("errormessage")%></p>
      </logic:present>

    </div>
    </logic:present>
    
    <h3 class="headline">Import CCR</h3>
    
    <s:errors/>
    
    <p>Please browse to select the CCR you would like to import and then click on "Import"</p>
      <s:form name="uploadDocumentForm" action="ImportCCR.action">
        <input type="hidden" name="am" value="${param['am']}"/>
        <c:if test='${not empty param["ccrIndex"]}'>
          <input type="hidden" name="ccrIndex" value="<c:out value='${param["ccrIndex"]}'/>"/>
        </c:if>
        <div style="width: 100%;"> 
            <s:file style="width: 75%" name="uploadedFile" size="60"/>
        </div>
        <br/>
        <s:submit name='upload' value="Import"/>&nbsp;<input type="button" value="Cancel" onclick="cancelImport();"/>
      </s:form>
      <pack:script src="utils.js"/>
      <script type="text/javascript">
        var remoteAccessAddress = '<mc:config property="RemoteAccessAddress"/>';
        function init() {
          <%-- HACK: do not send gw url as param, this prevents dashboard from attempting to replace
               patient info and makes it wait until the real CCR is available (from the import) --%>
          parent.ce_signal('openCCR','import'); // pass 'import' as guid
          parent.setTools([]);
        }
        addLoadEvent(init);
      </script>
  </body>
</html>

