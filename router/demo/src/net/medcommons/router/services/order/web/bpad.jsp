<%@ page language="java"%><%@ include file="taglibs.inc.jsp" %> 
<%--
  MedCommons WADO Viewer JSP  -  iPad Emulation Edition

  This is a special version of the viewer that is designed to 
  emulate the iPad with a YUI layout.  The main purpose of this
  is to make development a little easier and also to (perhaps)
  facilitate automated testing a bit better.
--%>
<s:layout-render name="/wado.jsp">
    <s:layout-component name="css">
       <src>ipadviewer.css</src>  
       <src>yui-2.8.2r1/resize/assets/skins/mc/resize.css</src>
       <src>yui-2.8.2r1/layout/assets/skins/mc/layout.css</src>
       <src>yui-2.8.2r1/tabview/assets/skins/mc/tabview.css</src>
       <src>yui-2.8.2r1/menu/assets/skins/mc/menu.css</src>
       <src>yui-2.8.2r1/button/assets/skins/mc/button.css</src>
       <src>yui-2.8.2r1/button/assets/skins/mc/button-skin.css</src>
       <src>yui-2.8.2r1/logger/assets/skins/mc/logger.css</src>
       
       <src>viewer.css</src>
       <src>bpadviewer.css</src>
    </s:layout-component>
    <s:layout-component name="scripts">
     <src>yui-2.8.2r1/utilities/utilities.js</src>
     <src>yui-2.8.2r1/yahoo-dom-event/yahoo-dom-event.js</src>
     <src>yui-2.8.2r1/dragdrop/dragdrop.js</src>
     <src>yui-2.8.2r1/element/element.js</src>
     <src>yui-2.8.2r1/container/container_core.js</src>
     <src>yui-2.8.2r1/tabview/tabview.js</src>
     <src>yui-2.8.2r1/resize/resize.js</src>
     <src>yui-2.8.2r1/layout/layout.js</src>
     <src>yui-2.8.2r1/menu/menu.js</src>
     <src>yui-2.8.2r1/button/button.js</src>
     <src>yui-2.8.2r1/logger/logger.js</src>
     <src>ipadviewer.js</src>  
     <src>bpadviewer.js</src>  
    </s:layout-component>


    <s:layout-component name="inlineScripts">
      <script type='text/javascript'> 
      var settings = ${actionBean.session.accountSettings.JSON};
      </script>
      <script src="yui3/3.2.0/yui/yui-min.js"></script> 
    </s:layout-component>

    <s:layout-component name="prebodyHTML">

      <div id='lefttop'>Loading ...</div>
      
      <div id='right' class='toolPalette'></div>
      
      <div id='footer' class='yui3-skin-sam'></div>
      
    </s:layout-component>
</s:layout-render>
