<%@ include file="/taglibs.inc.jsp" %>
<s:layout-definition>
  <%--
    MedCommons Patient Info Display 

    This single large gadget renders a number of smaller gadgets in
    a set of panes. 

    @author Simon Sadedin, MedCommons Inc.
  --%>
  <s:layout-render name="/gadgetBase.jsp">
    <s:layout-component  name="head">

      <style type="text/css">
       hr.gadgetSeparator {
         margin: 8px 1px 0px 1px;
       }
       h3.headerText {
        color: #104cb0;
        color: white;
        margin: 0px 5px 0px 5px;
        padding: 0px 5px 4px 5px;
        position: relative;
       }
       body {
        padding: 0px;
        margin: 0px;
       }
       a:link, a:visited {
        text-decoration: none;
        color: #5987AC;
       }
       a:hover, a:active {
        text-decoration: underline;
       }
      </style>
      
       <c:set var='styleSheet'><mc:config property='acStyleSheet'/></c:set>
       <c:if test='${not empty styleSheet}'><link rel="stylesheet" href="<mc:config property='AccountServer'/>/../..${styleSheet}"/></c:if>
      <script type="text/javascript">

       var acctServer = '<mc:config property='AccountServer'/>';
       ce_add_server(acctServer+'/ce_signal.php?auth=${desktop.authenticationToken}');
       addLoadEvent(addHeightMonitor);

       ce_connect('openCCRUpdated', function() {
           window.location.reload();
       });


      function openCcrWindow(url) {
        <%--// Attempt to deal with FF's moronic tab handling - if there is a CCR tab, KILL IT --%>
        if(YAHOO.env.ua.ie == 0) {
          var ccr = window.open('','ccr');
          ccr.close();
        }
        window.ccr = window.open(url,'ccr');
        return false;
      }

      </script>
    </s:layout-component>
    <s:layout-component  name="body">
      <c:choose>
        <c:when test='${!empty ccr}'>
          <c:set var="ccrXml" value="${ccr.JDOMDocument}"/>
          <c:set var="sex" value="${ccr.patientGender=='Male'?'M': ccr.patientGender=='Female'?'F':''}"/>
          <c:set var="ageSex"><mc:xvalue bean="ccrXml" path="patientAge"/> ${sex}</c:set>
          <div id="gadgetPaneHeader" class="gadgetBorderColor">
            <DIV style="BACKGROUND-COLOR: rgb(255,255,255)"><SPAN style="BORDER-RIGHT: rgb(170,170,170) 2px solid; BORDER-TOP: rgb(170,170,170) 0px solid; DISPLAY: block; FONT-SIZE: 1px; MARGIN-LEFT: 3px; OVERFLOW: hidden; BORDER-LEFT: rgb(170,170,170) 2px solid; MARGIN-RIGHT: 3px; BORDER-BOTTOM: rgb(170,170,170) 0px solid; HEIGHT: 1px; BACKGROUND-COLOR: rgb(85,85,85)"></SPAN><SPAN style="BORDER-RIGHT: rgb(170,170,170) 1px solid; BORDER-TOP: rgb(170,170,170) 0px solid; DISPLAY: block; FONT-SIZE: 1px; MARGIN-LEFT: 2px; OVERFLOW: hidden; BORDER-LEFT: rgb(170,170,170) 1px solid; MARGIN-RIGHT: 2px; BORDER-BOTTOM: rgb(170,170,170) 0px solid; HEIGHT: 1px; BACKGROUND-COLOR: rgb(85,85,85)"></SPAN><SPAN style="BORDER-RIGHT: rgb(170,170,170) 1px solid; BORDER-TOP: rgb(170,170,170) 0px solid; DISPLAY: block; FONT-SIZE: 1px; MARGIN-LEFT: 1px; OVERFLOW: hidden; BORDER-LEFT: rgb(170,170,170) 1px solid; MARGIN-RIGHT: 1px; BORDER-BOTTOM: rgb(170,170,170) 0px solid; HEIGHT: 1px; BACKGROUND-COLOR: rgb(85,85,85)"></SPAN><SPAN style="BORDER-RIGHT: rgb(170,170,170) 1px solid; BORDER-TOP: rgb(170,170,170) 0px solid; DISPLAY: block; FONT-SIZE: 1px; MARGIN-LEFT: 0px; OVERFLOW: hidden; BORDER-LEFT: rgb(170,170,170) 1px solid; MARGIN-RIGHT: 0px; BORDER-BOTTOM: rgb(170,170,170) 0px solid; HEIGHT: 2px; BACKGROUND-COLOR: rgb(85,85,85)"></SPAN></DIV>
            <h3 class="headerText">${headerTitle}
              <c:if test='${! empty ccr.guid}'>
              <span class='gadgetHeaderRightButton'>
                ${rightLinks}
              </span>
              </c:if>
            </h3>
          </div>
          <div id="gadgetPane">
            <%-- embedded flag causes the included pages to render in a compatible way for being embedded in this outer layout --%>
            ${gadgets}
          </div>
        </c:when>
        <c:otherwise>
         <script type='text/javascript'>
           addLoadEvent(function() { window.setTimeout(function() { ce_signal('clearGateway',"<mc:config property='RemoteAccessAddress'/>");}); },1000);
         </script>
         <p></p>
        </c:otherwise>
      </c:choose>
      <pack:script src="yui-2.6.0/yuiloader/yuiloader-min.js"/>
      <script type='text/javascript'>
        addLoadEvent(function() {
             YAHOO.util.Get.script("zip?resource=mochikit/MochiKit");
        });
      </script>
    </s:layout-component>
  </s:layout-render>
</s:layout-definition>

