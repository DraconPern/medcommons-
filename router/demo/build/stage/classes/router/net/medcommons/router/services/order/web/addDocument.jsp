<%@ include file="taglibs.inc.jsp" %><%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2010 MedCommons Inc.   All Rights Reserved.
-->
  <head>
    <title>Document Upload</title>
    <mc:base/>
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
    </style>
    <script language="Javascript">
      function checkClose() {
        <% if(request.getParameter("close")!=null) { %>
          var contentWinFrame = window.opener.document.getElementById('contents');
          var contentWin = window.opener;
          if(contentWinFrame != null) {
            contentWin = contentWinFrame.contentWindow;
          }

          if(contentWin.refreshPage != null) {
            contentWin.setModified();
            contentWin.refreshPage();
          }
          window.close();
        <%}%>
      } 
      function lock() { 
        //for(i=0; i<document.uploadDocumentForm.elements.length;i++) { 
        //}
        document.uploadDocumentForm.submitButton.disabled=true;
        document.uploadDocumentForm.cancelButton.disabled=true;
      }
    </script>
  </head>
  <body style="position: relative; left: 10px;" onload="checkClose();">
    <%if(request.getAttribute("message") != null) { %>
      <p><%=request.getAttribute("message")%></p>
    <% } %>
    <h3 class="headline">Add Document</h3>
    <p>Please select your document and click on "Submit"</p>
      <s:form name="uploadDocumentForm" 
            action="AddDocument.action" 
            enctype="multipart/form-data"
            onsubmit="lock();"
            >
      <input type="hidden" name="ccrIndex" value="${ccrIndex}"/>
      <div style="width: 100%;"> 
          <s:file style="width: 100%" name="uploadedFile" size="60"/>
      </div>
      <br/>
      <s:submit name="submitButton" id="submitButton" value="submit"/>&nbsp;<input type="button" name="cancelButton" value="Cancel" onclick="window.close();"/>
      </s:form>
  </body>
</html>

