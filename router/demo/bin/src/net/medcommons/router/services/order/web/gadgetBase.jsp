<%@ include file="/taglibs.inc.jsp" %>
<s:layout-definition>
  <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
  <html>
  <%--
    Base Layout for MedCommons Gadgets Displayed by Gateway

    This page renders a number of panes showing information about the current patient.

    @author Simon Sadedin, MedCommons Inc.
  --%>
    <head>
      <pack:style>
        <src>gadgetSmall.css</src>
        <src>gadgetMedium.css</src>
      </pack:style>
      <pack:script>
        <src>utils.js</src>
        <src>sha1.js</src>
      </pack:script>
      <s:layout-component name="head"/>
    </head>
    <body class="yui-skin-sam"  <c:if test='${!empty actionBean and mc:has(actionBean,"margin")}'>style="margin: ${actionBean.margin}px;"</c:if> >
      <s:layout-component name="body"/>
    </body>
</html>
</s:layout-definition>
