<%@ page language="java"%><%@ include file="taglibs.inc.jsp" %> 
<%--
  MedCommons WADO Viewer JSP  -  iPad Edition

  This is a special version of the viewer that is designed to be
  embedded in the MedCommons iPad app.
--%>
<s:layout-render name="/wado.jsp">
    <s:layout-component name="css">
    <%--
       <src>yui-2.8.2r1/logger/assets/skins/mc/logger.css</src>
        --%>
       <src>ipadviewer.css</src>  
    </s:layout-component>
    <s:layout-component name="scripts">

    <%--
       <src>yui-2.8.2r1/logger/logger-min.js</src>
        --%>
       <src>ipadviewer.js</src>  
    </s:layout-component>
</s:layout-render>
