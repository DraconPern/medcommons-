<%@ include file="taglibs.inc.jsp" %>
<s:layout-render name="/page.jsp" title="Share Patient Account">

    <s:layout-component name="head">
	    <pack:style enabled='true'>
	      <src>yui-2.8.2r1/container/assets/skins/mc/container.css</src>
	      <src>yui-2.8.2r1/button/assets/skins/mc/button.css</src>
          <src>forms.css</src>
	    </pack:style>
	    <% if(request.getHeader("User-Agent").indexOf("iPhone")>=0) { %>
        <meta name="viewport" content="width=620,initial-scale=0.6,maximum-scale=0.6" />
        <script type='text/javascript'>
        </script>
        <% } %>
    </s:layout-component>
    
    <s:layout-component name="body">
	    <pack:script enabled='true'>
	      <src>yui-2.8.2r1/yahoo-dom-event/yahoo-dom-event.js</src>
	      <src>yui-2.8.2r1/container/container.js</src>
	      <src>yui-2.8.2r1/element/element.js</src>
	      <src>yui-2.8.2r1/button/button.js</src>
	      <src>mochikit/Base.js</src>
	      <src>mochikit/DOM.js</src>
	      <src>mochikit/Async.js</src>
	      <src>common.js</src>
	      <src>utils.js</src>
	      <src>forms.js</src>
	    </pack:script>
	    <script type='text/javascript'>
	    var options = <mc:json src='${actionBean.options}'/>;
	    window.YUI_initialized=true;
	    YAHOO.util.Event.onDOMReady(function() {
		    showSharingDialog(options, {h: 500, w: 1200});
		    if(YAHOO.env.ua.mobile && YAHOO.env.ua.webkit)
    		    YAHOO.util.Dom.setY('shareDlg_c',0);		    
	    }); 
	    </script>
    </s:layout-component>
</s:layout-render>