<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<%
  response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
  response.setHeader("Pragma","no-cache"); // HTTP 1.0
%>
<%--
  Section Editor

  Renders an editor for a requested section of a CCR.

  @author Simon Sadedin, MedCommons Inc.
--%>
<html>
  <head>
    <link href="ereferralLetter.css" rel="stylesheet" type="text/css"/>
    <link href="autoComplete.css" rel="stylesheet" type="text/css"/>
    <script src="mochikit/MochiKit.js"></script>
    <script type="text/javascript" src="autoComplete.js"></script>
    <script src="utils.js"></script>
    <script src="patientTabs.js"></script>
    <script type="text/javascript" src="ccreditor.js"></script>
    <script type="text/javascript">
      var activeEditor = null;
      var activeEditorName = null;

      function init() {
        addBodySection(null,'${sectionName}');        
      }
      function onEditorSaveSuccess() {
        var savePin = "${param['pin']}";
        doSimpleXMLHttpRequest('SaveCCR.action?saveJson&ccrIndex=0&replyPin='+savePin).addCallbacks(onCCRSaveSuccess, genericErrorHandler);
      }
      function onCCRSaveSuccess(req) {
        var returnUrl = "${param['returnUrl']}";
        if(returnUrl.indexOf('?')<0) {
          returnUrl += "?";
        }
        var result = eval(req.responseText);
        returnUrl+='&status='+urlEncode(result.status)+'&trackingNumber='+urlEncode(result.trackingNumber);
        window.location.href = returnUrl;
      }
    </script>
    <style type="text/css">
      .editorOuter {
        position: relative;
        top: 0px;
        left: 0px;
        display: none;
        background-color: transparent;
      }
    </style>
  </head>
  <body onload="init();" style="overflow: auto;">
    <tiles:insert page='${sectionName}Editor.jsp'/>
  </body>
</html>
