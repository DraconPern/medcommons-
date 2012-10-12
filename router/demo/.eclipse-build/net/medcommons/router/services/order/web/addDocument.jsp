<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ page isELIgnored="false" %> 
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
    <logic:present name="message" scope="request">
      <p><%=request.getAttribute("message")%></p>
    </logic:present>
    <h3 class="headline">Add Document</h3>
    <p>Please select your document and click on "Submit"</p>
      <html:form name="uploadDocumentForm" 
            scope="session" 
            type="net.medcommons.router.services.wado.actions.UploadFileForm" 
            action="addDocument.do" 
            enctype="multipart/form-data"
            onsubmit="lock();"
            >
      <input type="hidden" name="ccrIndex" value="${ccrIndex}"/>
      <div style="width: 100%;"> 
          <html:file style="width: 100%" property="uploadedFile" size="60"/>
      </div>
      <br/>
      <html:submit styleId="submitButton"/>&nbsp;<input type="button" name="cancelButton" value="Cancel" onclick="window.close();"/>
      </html:form>
  </body>
</html>

