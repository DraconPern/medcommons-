<%@ include file="taglibs.inc.jsp" %>
<s:layout-definition>
  <!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
  <html>
  <%--
    Base Layout for Simple Page

    @author Simon Sadedin, MedCommons Inc.
  --%>
    <head>
	  <mc:base/>
      <title><c:out value="${title}"/></title>
      <s:layout-component name="css">
	      <pack:style enabled='true'>
		      <src>common.css</src>
	      </pack:style>
      </s:layout-component>
      <s:layout-component name="head"/>
    </head>
    <body class="yui-skin-mc"  <c:if test='${!empty actionBean and mc:has(actionBean,"margin")}'>style="margin: ${actionBean.margin}px;"</c:if> >
      <s:layout-component name="body"/>
    </body>
</html>
</s:layout-definition>
