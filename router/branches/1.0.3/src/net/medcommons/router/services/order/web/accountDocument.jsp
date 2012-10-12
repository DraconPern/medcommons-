<%@ include file="/taglibs.inc.jsp" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<!--
 Copyright 2007 MedCommons Inc.   All Rights Reserved.
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
    <script src="mochikit/MochiKit.js"></script>
    <script language="Javascript">
      function init() {
        <c:if test='${! empty param["returnUrl"]}'>
          document.uploadDocumentForm.returnUrl.value="${param['returnUrl']}";
        </c:if>
      }

      function check() {
        if(every(document.uploadDocumentForm.documentType,function(f) { return !f.checked })) {
          alert("Please select a document type.");
          return false;
        }
      }

    </script>
    <c:if test="${! empty params['css']}">
      <link href="${params['css']}" rel="stylesheet" type="text/css">
    </c:if>
  </head>
  <body style="position: relative; left: 10px;" onload="init();">
    <h3 class="headline">Add Account Document</h3>
    <c:if test='${! empty actionBean.message}'>
    <div id="errorbox" class="errorbox">
      <p><c:out value='${actionBean.message}'/></p>
      <c:if test='${! empty errormessage}'>
        <p><%=request.getAttribute("errormessage")%></p>
      </c:if>
    </div>
    </c:if>
    <s:errors/>
    <div style="color: red;"><html:errors/></div>
    <p>Please browse to select the document you would like to add and then click on "Submit"</p>
    <s:form name="uploadDocumentForm" onsubmit="return check();" style="width:80%" action="AccountDocument.action">
      <input type='hidden' name='returnUrl' value='${actionBean.returnUrl}'/>
      <input type='hidden' name='storageId' value='${actionBean.storageId}'/>
      <div id="fields" style="width: 80%;"> 
          <fieldset style="width: 300px;">
            <legend>Select a Document Type</legend>
            <s:radio name="documentType" value="LIVINGWILL"/>Living Will<br/>
            <s:radio name="documentType" value="DURABLEPOA"/>Durable Power of Attorney<br/>
            <s:radio name="documentType" value="DNR"/>Do Not Resuscitate Instructions<br/>
            <s:radio name="documentType" value="PATPHOTO"/>Patient Photo<br/>
          </fieldset>
          <p><s:file style="width: 75%" name="uploadedFile" size="60"/></p>
          <p>Optional Comment:</p>
          <p><s:text name="comment" size="60"/></p>
      </div>
      <br/>
      <s:submit name="add" value="Submit" />&nbsp;<input type="button" value="Cancel" onclick="document.location.href='logon.jsp';"/>
    </s:form>
  </body>
</html>
