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
    
    <h3 class="headline">Import Blue Button File</h3>
    
    <s:errors/>
    
    <p>Please browse to select the Blue Button File that you would like to import and then click on "Import"</p>
      <s:form name="uploadDocumentForm" action="/blue">
        <input type="hidden" name="am" value="${param['am']}"/>
        <div style="width: 100%;"> 
            <s:file style="width: 75%" name="uploadedFile" size="60"/>
        </div>
        <br/>
        <s:submit name='uploadBlue' value="Import"/>&nbsp;<input type="button" value="Cancel" onclick="cancelImport();"/>
      </s:form>
      <pack:script src="utils.js"/>
      <script type="text/javascript">
        var remoteAccessAddress = '<mc:config property="RemoteAccessAddress"/>';
      </script>
  </body>
</html>

