<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/taglibs.inc.jsp" %>
<%@ page language="java"%>
<html class='yui-skin-mc'>
<head>
   <mc:base/>
   <meta name="viewport" content="width=1024" />
   <meta name="apple-mobile-web-app-capable" content="yes">
   <title>MedCommons Viewer</title>
   
   <pack:style enabled="true">
    <src>yui-2.8.2r1/resize/assets/skins/mc/resize.css</src>
    <src>yui-2.8.2r1/layout/assets/skins/mc/layout.css</src>
    <src>yui-2.8.2r1/tabview/assets/skins/mc/tabview.css</src>
    <src>yui-2.8.2r1/menu/assets/skins/mc/menu.css</src>
    <src>forms.css</src>
    <src>viewer.css</src>
    <src>ccrbody.css</src>
   </pack:style>    
   
   <% if(request.getHeader("User-Agent").indexOf("MSIE ")<0) { %>
   <pack:script enabled="true">
     <src>yui-2.8.2r1/utilities/utilities.js</src>
     <src>yui-2.8.2r1/yahoo-dom-event/yahoo-dom-event.js</src>
     <src>yui-2.8.2r1/dragdrop/dragdrop.js</src>
     <src>yui-2.8.2r1/element/element.js</src>
     <src>yui-2.8.2r1/container/container_core.js</src>
     <src>yui-2.8.2r1/tabview/tabview.js</src>
     <src>yui-2.8.2r1/resize/resize.js</src>
     <src>yui-2.8.2r1/layout/layout.js</src>
     <src>yui-2.8.2r1/menu/menu.js</src>
     <src>utils.js</src>
     <src>mochikit/Base.js</src>
     <src>mochikit/DOM.js</src>
     <src>mochikit/Async.js</src>
     <src>mochikit/DateTime.js</src>
     <src>common.js</src>
     <src>forms.js</src>
     <src>patient.js</src>
     <src>viewer.js</src>
     <src>iscroll.js</src>
   </pack:script>   
   <% } else { %>
   <pack:script enabled="true">
     <src>yui-2.8.2r1/utilities/utilities.js</src>
     <src>yui-2.8.2r1/yahoo-dom-event/yahoo-dom-event.js</src>
     <src>yui-2.8.2r1/dragdrop/dragdrop.js</src>
     <src>yui-2.8.2r1/element/element.js</src>
     <src>yui-2.8.2r1/container/container_core.js</src>
     <src>yui-2.8.2r1/tabview/tabview.js</src>
     <src>yui-2.8.2r1/resize/resize.js</src>
     <src>yui-2.8.2r1/layout/layout.js</src>
     <src>yui-2.8.2r1/menu/menu.js</src>
     <src>utils.js</src>
     <src>mochikit/Base.js</src>
     <src>mochikit/DOM.js</src>
     <src>mochikit/Async.js</src>
     <src>mochikit/DateTime.js</src>
     <src>common.js</src>
     <src>forms.js</src>
     <src>patient.js</src>
     <src>viewer.js</src>
   </pack:script>   
   <% } %>
   
   <style type='text/css'>
   
    #wrapper {
        position:relative;  /* needed */
        z-index:1;          /* needed and important */
        height:100%;       /* needed */
        margin: 0;
        background-color: white;
        overflow: auto;
        padding-left: 10px;
        padding-right: 10px;
    }
     
     #center {
         background-color: white;
        height:100%;       /* needed */
     }
    
 
    .yui-skin-mc .yui-layout #yui-gen1 div.yui-layout-bd {
        border: none;
    }   
    
    .yui-layout-doc {
        position: relative;
        top: -2px;
    }

    #productsandservices2 {        
        position: static;
    }
 
    /*
        For IE 6: trigger "haslayout" for the anchor elements in the root Menu by 
        setting the "zoom" property to 1.  This ensures that the selected state of 
        MenuItems doesn't get dropped when the user mouses off of the text node of 
        the anchor element that represents a MenuItem's text label.
    */
 
    #productsandservices2 .yuimenuitemlabel {
        _zoom: 1;
    }
 
    #productsandservices2 .yuimenu .yuimenuitemlabel {
        _zoom: normal;
    }
    /*
        Change some of the Menu colors
    */
    .yui-skin-mc .yuimenu .bd {
        background-color: #F2F2F2;
    }
    #productsandservices2 .bd {
        border: none;
    }
    #productsandservices2 .bd .first-of-type .bd {
        border: 1px solid #808080;
    }
    
    .yui-skin-mc .yui-layout {
        background-color: #f9f9f6;
    }        
    
    /* override ccr.css */
    html, body {
        margin: 0 !important;
    }
    
    .ccr td , .ccr th {
        border: none;
    }
    
    .ccr table.list td {
        border:thin solid #CCCCCC;
        padding:5px;
    }    
    
    .ccr table.internal td {
        border:medium none;
        padding:1px;
    }        
   </style>
   
</head>
<body class='yui-layout'>

<div id='top1'></div>

<div id='lefttop'>Loading ...</div>

<div id="center"> 
    <div id='wrapper'>
        <div id='ccr' class='ccr'>
        Loading ...
           
        </div>
    </div>
</div> 
 
<div id='buttonPanel'>
    <button id='addPDFButton'>Add PDF</button>
    <button id='addImagingButton'>Add Imaging</button>
    <hr/>
    <button id='replyButton'>Reply</button>
    <button id='faxCoversButton'>Fax Covers</button>
    <button id='shareButton'>Share</button>
</div>

<script type='text/javascript'>

var scroller;

function loaded() {
    setTimeout( function() {
    }, 100);

    
}


(function() {
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;
 
    Event.onDOMReady(function() {
        var layout = new YAHOO.widget.Layout({
            units: [
                { position: 'top', height: 1, body: 'top1', scroll: null, zIndex: 2 },
                { position: 'left',  width: 300,  gutter: '5', scroll: null, zIndex: 1,  animate: false },
                { position: 'center', body: 'center', gutter: '5 5', header: '<div class="ccrHeader">'+htmlentities(p.PatientName) + ' - Current CCR ' + formatDateOfBirth(new Date(ccr.createDateTime))+'</div>'  }
            ]
        });

        var height = 500;
        if(YAHOO.env.ua.mobile == 'Apple') {
            height = 200;
        }
        
        layout.on('render', function() {
                var el = layout.getUnitByPosition('left').get('wrap');
                var layout2 = new YAHOO.widget.Layout(el, {
                    parent: layout,
                    units: [
                        { position: 'top',header: '<a href="#">&lt;&lt;</a> '+settings.groupName, body: 'lefttop', width: 290,  height: height, gutter: '2px', maxHeight: 500 },
                        { position: 'center', body: 'buttonPanel', gutter: '2px', scroll: true }
                    ]
                });
                layout2.render();
        });
        
        layout.render();

        
        YUI({
            base: 'yui3/3.2.0/', // the base path to the YUI install.  Usually not needed because the default is the same base path as the yui.js include file
            filter: 'raw' // apply a filter to load the raw or debug version of YUI files
        }).use('node', 'event', function(Y) {
            window.Y = Y;
            loadCCRs();
        });
        
        $Y('shareButton').on('click', showShareDialog);

        YAHOO.util.Connect.asyncRequest('GET', 'DisplayCCR.action?ccrIndex=0&body=true', {
                        success: function(r) { 
                            $('ccr').innerHTML = r.responseText; 
                            if(isMobileBrowser()) {
                                document.ontouchmove = function(e) { e.preventDefault(); return false; };
                                scroller = new iScroll( document.getElementById('ccr') );
                                scroller.refresh(); 
                            }
                        },
                        failure: function(r) { alert('failed'); }
        } , "");
        setTimeout(function() { window.scrollTo(0, 1) }, 100);
    });
})();

<jsp:include page="accountDocumentsJavascript.jsp"/>
<jsp:include page="patientJavascript.jsp"/>
<jsp:include page="ccrDetailJavaScript.jsp"/>
var settings = ${actionBean.session.accountSettings.JSON};
</script>

<script src="yui/3.2.0/yui-min.js"></script> 

<script type="text/javascript">
</script>

</body>
</html>