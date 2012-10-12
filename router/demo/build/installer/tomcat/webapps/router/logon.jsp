<%@ page language="java"%>
<%@ include file="/taglibs.inc.jsp" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ page isELIgnored="false" %>
<%
  response.setHeader("Cache-Control","no-cache"); // HTTP 1.1
  response.setHeader("Pragma","no-cache"); // HTTP 1.0
%>
<%--
  If hpass is provided then this is a redirect from a central logon
 --%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
  <c:set var="RemoteAccessAddress"><mc:config property="RemoteAccessAddress"/></c:set>
  <c:set var="AccountsBaseUrl"><mc:config property="AccountsBaseUrl"/></c:set>
  <c:set var="brandName"><mc:config property="BrandName" default="MedCommons"/></c:set>

  <head>
    <title>${brandName}</title>
    <link rel="icon" href="./favicon.ico" type="image/x-icon">
    <link rel="shortcut icon" href="./favicon.ico" type="image/x-icon">
    <link rel="Favorites Icon" href="./favicon.ico">
    <link href="main.css" rel="stylesheet" type="text/css">
    <link href="login.css" rel="stylesheet" type="text/css">
    <style type="text/css">
      body {
        font-family: arial,helvetica;
      }

      td {
        font-size: 12px;
        text-align: left;
      }

      a:visited {
        color: blue;
      }

      a.disabledLink:visited {
        color: gray;
        cursor: default;
      }

      #newblock {
        display: none;
      }
      
      .error {
	      color: red;
      }

    </style>
    <script type="text/javaScript" src="sha1.js"></script>
    <script type="text/javaScript" src="utils.js"></script>
    <script type="text/javascript">
      function init() {
        parent.hideToolPalette();
      <c:choose>
        <c:when test='${!empty trackingNumber}'>
          <%-- when given a tracking number, hide the other options --%>
          if(document.logonForm)
          <c:choose>
            <c:when test='${trackingNumber == "saved"}'>
                openCcr();
            </c:when>
            <c:otherwise>
                document.logonForm.trackingNumber.value='<c:out value="${trackingNumber}"/>';
                show('orblock','openidblock');
            </c:otherwise>
          </c:choose>
        </c:when>
        <c:otherwise>
          show('orblock','newblock');
        </c:otherwise>
      </c:choose>
      
        var savedtn = getCookie('savedtn');
        if(savedtn != null) {
          el('savedtn').innerHTML=savedtn;
          el('openCcrLink').style.color = 'blue';
          el('openCcrLink').style.cursor = 'pointer';
        }
      }

      function chkpin(field) {
        if(field.value=='99999') {
          document.getElementById('credits').style.display='block';
        }
      }

      function openCcr() {
        var savedtn = getCookie('savedtn');
        if(savedtn != null) {
          document.logonForm.trackingNumber.value = savedtn;
          document.logonForm.pword.style.borderStyle='solid';
          document.logonForm.pword.style.borderColor='black';
          document.logonForm.pword.style.borderWidth='2px';
          document.logonForm.pword.focus();
          el('openCcrMessage').style.display='block';
        }
        else {
          document.logonForm.trackingNumber.focus();
        }

        if(el('messages')) {
          el('messages').style.display='none';
        }
      }

      function newCcr() {
        window.parent.highlightTabById('tab4',false);
        window.parent.hideTab('tab6');
        window.parent.document.location.href='NewCCR.action';
      }

      function submitOpenId() {
        window.open("validatingOpenId.ftl","openid_auth","width=800,height=600,location=no,menubar=no,status=no,toolbar=no");
        return false;
      }
    </script>
  </head>
  <body style="padding: 20px;" onload="init();">
    <h3 class="headline">Welcome to <span ondblclick="document.location.href='logonold.jsp';">${brandName}</span></h3>

    <logic:present name="message" scope="request">
      <p <c:if test='${actionBean.invalid}'>class='error'</c:if> >
      <%=request.getAttribute("message")%>
      </p>
    </logic:present>
    <s:errors/>
    <c:if test='${param["expired"]}'>
      <p style='color: red;'>Your session has expired.  Please re-enter your credentials to access this CCR.</p>
    </c:if>
      <span style="position: relative;">
      <%-- if they are not logged in already then display the login box and login prompt --%>
      <c:choose>
        <c:when test='${not desktop.hasAccount or  not empty trackingNumber}'>
          <c:choose>
            <c:when test='${invalid or loginAttempted}'>
            <span id="messages">
              <span style="color: red"><html:errors/></span>
              <br/>
              <br/>
            </span>
            </c:when>
            <c:otherwise>
              <%-- if there is no track# prompt them to enter it, otherwise just ask for pin --%>
              <c:choose>
                <c:when test='${empty trackingNumber}'>
                  <p style="width:300px;">Please enter a MedCommons Tracking Number and corresponding PIN to access your private medical communication.</p>
                </c:when>
                <c:when test='${trackingNumber == "saved"}'>
                  <c:if test='${empty cookie["savedtn"]}'>
                    A saved CCR could not be located from the computer you are using.  Please enter the MedCommons Tracking Number and
                    PIN to access your private medical communication.
                    <br/>
                    <br/>
                  </c:if>
                </c:when>
                <c:otherwise>
                  <p>Please enter your credentials to access this private medical communication.</p>
                </c:otherwise>
              </c:choose>
            </c:otherwise>
          </c:choose>
            <div id="openCcrMessage"><p>Please enter your PIN to access your saved CCR</p></div>
          </span>
          <div>
            <div id="loginblock" style="float:left">
              <s:form name="logonForm" action="/Track.action?track" method="POST">
                <s:text id="tracknum" name="trackingNumber" size="20"/>
                <s:password id="pword" name="pin" size="20" onkeyup="chkpin(this);"/>
                <div id="logbutton"><input type="image" src="images/logbutton_off.gif"></div>
              </s:form>
            </div>
            <div id="orblock" style="margin: 50px 50px 50px 50px; float: left; height: 100px;  vertical-align: middle;">
              <p>or</p>
            </div>
            <div id="openidblock" style="padding-top: 45px; height: 100px;">
              <form name="openIdForm" autocomplete="off" action="AuthenticateOpenID.action" target="openid_auth" method="POST">
                <input type="hidden" name="trackingNumber" value="${trackingNumber}" />
                <input id="openidUrl" name="openid_url" type="text"/>
                <div id="oidgoButton"><input type="image" onclick="return submitOpenId()" src="images/logbutton_blue.gif"></div>
              </form>
            </div>
            <div id="newblock" style="padding-top: 45px; height: 200px; vertical-align: middle;">
              <table>
                <tr><td valign="middle"><img src="images/newccr.png"/></td>
                    <td><span class="spanLink" onclick="newCcr();">Create a New CCR</span></td>
                </tr>
                <tr><td valign="middle"><img src="images/importccr.png"/></td>
                    <td><a class="iconlink" href="import.jsp">Import a CCR</a></td>
                </tr>
                <tr>
                  <td valign="middle"><img src="images/openccr.png"/></td>
                  <td><a id="openCcrLink" href="javascript:openCcr();" class="disabledLink">Open Saved CCR &nbsp;<span id="savedtn">&nbsp;</span></a></td>
                </tr>
              </table>
            </div>
          </div>
          <div style='clear:both; position: relative; top: -40px;'>
            <form name='accountLogon' method='post' target='_top' action='${AccountsBaseUrl}acct/login.php'>
              <input type='hidden' name='next' value='${AccountsBaseUrl}/<c:out value='${trackingNumber}'/>'/>
              <p>Have an account with access to this CCR?  <a href='#' onclick='document.accountLogon.submit();'>Logon to Your Account</a></p>
            </form>
          </div>
      </c:when>
      <c:otherwise>
        <p>You are logged in to Account <b><mc:medcommonsId>${desktop.ownerMedCommonsId}</mc:medcommonsId></b></p>
          <a href="Logout.action" style="font-size: 10px;">logout</a>
      </c:otherwise>
    </c:choose>
    </span>
    <br/>
    <c:remove var="autoTrackingNumber"/>
    <c:remove var="loginAttempted"/>
  </body>
</html>
