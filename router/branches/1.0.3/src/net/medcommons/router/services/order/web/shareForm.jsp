<%@ include file="taglibs.inc.jsp" %>
<s:layout-render name="/page.jsp" title="Share Patient Account">

    <s:layout-component name="head">
	    <pack:style enabled='true'>
	      <src>yui-2.8.0r4/container/assets/skins/mc/container.css</src>
	      <src>yui-2.8.0r4/button/assets/skins/mc/button.css</src>
          <src>forms.css</src>
	    </pack:style>
    </s:layout-component>
    
    <s:layout-component name="body">
	    <pack:script enabled='true'>
	      <src>yui-2.8.0r4/yahoo-dom-event/yahoo-dom-event.js</src>
	      <src>yui-2.8.0r4/container/container.js</src>
	      <src>yui-2.8.0r4/element/element.js</src>
	      <src>yui-2.8.0r4/button/button.js</src>
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
		    showSharingDialog(options);
	    }); 
	    </script>
    </s:layout-component>
</s:layout-render>