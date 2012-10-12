<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page language="java"%>
<%@ include file="taglibs.inc.jsp" %>
<html class='yui-skin-mc'>
<head>
   <mc:base/>
   <meta name="viewport" content="width=1024" />
   <title>MedCommons Viewer</title>
   <pack:style enabled="false">
    <src>yui-2.8.0r4/reset-fonts-grids/reset-fonts-grids.css</src>
    <src>yui-2.8.0r4/base/base.css</src>
    <src>yui-2.8.0r4/resize/assets/skins/mc/resize.css</src>
    <src>yui-2.8.0r4/layout/assets/skins/mc/layout.css</src>
    <src>yui-2.8.0r4/tabview/assets/skins/mc/tabview.css</src>
    <src>yui-2.8.0r4/menu/assets/skins/mc/menu.css</src>
    <src>forms.css</src>
    <src>viewer.css</src>
   </pack:style>
   
   <%-- 
   <pack:style>
    <src>viewer.css</src>
   </pack:style>
   --%>
   
   <pack:script enabled="false">
     <src>yui-2.8.0r4/utilities/utilities.js</src>
     <src>yui-2.8.0r4/yahoo-dom-event/yahoo-dom-event.js</src>
     <src>yui-2.8.0r4/dragdrop/dragdrop.js</src>
     <src>yui-2.8.0r4/element/element.js</src>
     <src>yui-2.8.0r4/container/container_core.js</src>
     <src>yui-2.8.0r4/tabview/tabview.js</src>
     <src>yui-2.8.0r4/resize/resize.js</src>
     <src>yui-2.8.0r4/layout/layout.js</src>
     <src>yui-2.8.0r4/menu/menu.js</src>
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
   
   <style type="text/css">
   
    .yui-skin-mc .yui-layout #yui-gen1 div.yui-layout-bd {
        border: none;
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
   </style>
   
</head>
<body>

 
 
<div id="top1"> 
    <div id="productsandservices" class="yuimenubar yuimenubarnav"> 
        <div class="bd"> 
            <ul class="first-of-type"> 
 
                <li class="yuimenubaritem first-of-type"> 
                    <a class="yuimenubaritemlabel" href="#communication">File</a> 
                </li> 
                <li class="yuimenubaritem"> 
                    <a class="yuimenubaritemlabel" href="http://shopping.yahoo.com">Actions</a> 
                </li> 
                <li class="yuimenubaritem"> 
                    <a class="yuimenubaritemlabel" href="http://entertainment.yahoo.com">Settings</a> 
                </li> 
                <li class="yuimenubaritem"> 
                    <a class="yuimenubaritemlabel" href="#">Logout</a> 
                </li> 
            </ul> 
        </div> 
    </div> 
</div> 
<%-- 
<div id="left1"> 
    <div id="productsandservices2" class="yuimenu"> 
    <div class="bd"> 
        <ul class="first-of-type"> 
            <li class="yuimenuitem"> 
                <a class="yuimenuitemlabel" href="#communication2">Communication</a> 
            </li> 
            <li class="yuimenuitem"> 
                <a class="yuimenuitemlabel" href="http://shopping.yahoo.com">Shopping</a> 
            </li> 
 
            <li class="yuimenuitem"> 
                <a class="yuimenuitemlabel" href="http://entertainment.yahoo.com">Entertainment</a> 
            </li> 
            <li class="yuimenuitem"> 
                <a class="yuimenuitemlabel" href="#">Information</a> 
            </li> 
        </ul> 
    </div> 
 
</div> 

</div> 
--%>
<div id='lefttop'>Loading ...</div>

<div id="center" style='height: 100%;'> 
    <iframe src='DisplayCCR.action?ccrIndex=${ccrIndex}'
            name='contentFrame' id='contentFrame' width='100%' height='100%'
            frameborder='0'
            style='border-style: none; background-color: white; height: 100%;'
            ></iframe>
</div> 
 
<div id='buttonPanel'>
    <button id='addPDFButton'>Add PDF</button>
    <button id='addImagingButton'>Add Imaging</button>
    <hr/>
    <button id='replyButton'>Reply</button>
    <button id='faxCoversButton'>Fax Covers</button>
    <button id='shareButton'>Share</button>
</div>
 
<script> 
(function() {
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;
 
 
        var initTopMenu = function() {
                /*
                     Instantiate a MenuBar:  The first argument passed to the 
                     constructor is the id of the element in the page 
                     representing the MenuBar; the second is an object literal 
                     of configuration properties.
                */
 
                var oMenuBar = new YAHOO.widget.MenuBar("productsandservices", { 
                                                            autosubmenudisplay: true, 
                                                            hidedelay: 750, 
                                                            lazyload: true,
                                                            effect: { 
                                                                effect: YAHOO.widget.ContainerEffect.FADE,
                                                                duration: 0.25
                                                            } 
                                                        });
 
                /*
                     Define an array of object literals, each containing 
                     the data necessary to create a submenu.
                */
 
                var aSubmenuData = [
                
                    {
                        id: "communication", 
                        itemdata: [ 
                            { text: "360", url: "http://360.yahoo.com" },
                            { text: "Alerts", url: "http://alerts.yahoo.com" },
                            { text: "Avatars", url: "http://avatars.yahoo.com" },
                            { text: "Groups", url: "http://groups.yahoo.com " },
                            { text: "Internet Access", url: "http://promo.yahoo.com/broadband" },
                            {
                                text: "PIM", 
                                submenu: { 
                                            id: "pim", 
                                            itemdata: [
                                                { text: "Yahoo! Mail", url: "http://mail.yahoo.com" },
                                                { text: "Yahoo! Address Book", url: "http://addressbook.yahoo.com" },
                                                { text: "Yahoo! Calendar",  url: "http://calendar.yahoo.com" },
                                                { text: "Yahoo! Notepad", url: "http://notepad.yahoo.com" }
                                            ] 
                                        }
                            
                            }, 
                            { text: "Member Directory", url: "http://members.yahoo.com" },
                            { text: "Messenger", url: "http://messenger.yahoo.com" },
                            { text: "Mobile", url: "http://mobile.yahoo.com" },
                            { text: "Flickr Photo Sharing", url: "http://www.flickr.com" },
                        ]
                    },
 
                    {
                        id: "shopping", 
                        itemdata: [
                            { text: "Auctions", url: "http://auctions.shopping.yahoo.com" },
                            { text: "Autos", url: "http://autos.yahoo.com" },
                            { text: "Classifieds", url: "http://classifieds.yahoo.com" },
                            { text: "Flowers & Gifts", url: "http://shopping.yahoo.com/b:Flowers%20%26%20Gifts:20146735" },
                            { text: "Real Estate", url: "http://realestate.yahoo.com" },
                            { text: "Travel", url: "http://travel.yahoo.com" },
                            { text: "Wallet", url: "http://wallet.yahoo.com" },
                            { text: "Yellow Pages", url: "http://yp.yahoo.com" }                    
                        ]    
                    },
                    
                    {
                        id: "entertainment", 
                        itemdata: [
                            { text: "Fantasy Sports", url: "http://fantasysports.yahoo.com" },
                            { text: "Games", url: "http://games.yahoo.com" },
                            { text: "Kids", url: "http://www.yahooligans.com" },
                            { text: "Music", url: "http://music.yahoo.com" },
                            { text: "Movies", url: "http://movies.yahoo.com" },
                            { text: "Radio", url: "http://music.yahoo.com/launchcast" },
                            { text: "Travel", url: "http://travel.yahoo.com" },
                            { text: "TV", url: "http://tv.yahoo.com" }              
                        ] 
                    },
                    
                    {
                        id: "information",
                        itemdata: [
                            { text: "Downloads", url: "http://downloads.yahoo.com" },
                            { text: "Finance", url: "http://finance.yahoo.com" },
                            { text: "Health", url: "http://health.yahoo.com" },
                            { text: "Local", url: "http://local.yahoo.com" },
                            { text: "Maps & Directions", url: "http://maps.yahoo.com" },
                            { text: "My Yahoo!", url: "http://my.yahoo.com" },
                            { text: "News", url: "http://news.yahoo.com" },
                            { text: "Search", url: "http://search.yahoo.com" },
                            { text: "Small Business", url: "http://smallbusiness.yahoo.com" },
                            { text: "Weather", url: "http://weather.yahoo.com" }
                        ]
                    }                    
                ];
 
 
                /*
                     Subscribe to the "beforerender" event, adding a submenu 
                     to each of the items in the MenuBar instance.
                */
 
                oMenuBar.subscribe("beforeRender", function () {
 
                    if (this.getRoot() == this) {
 
                        this.getItem(0).cfg.setProperty("submenu", aSubmenuData[0]);
                        this.getItem(1).cfg.setProperty("submenu", aSubmenuData[1]);
                        this.getItem(2).cfg.setProperty("submenu", aSubmenuData[2]);
                        this.getItem(3).cfg.setProperty("submenu", aSubmenuData[3]);
 
                    }
 
                });
 
 
                /*
                     Call the "render" method with no arguments since the 
                     markup for this MenuBar instance is already exists in 
                     the page.
                */
 
                oMenuBar.render();         
        };
 
        var initLeftMenu = function() {
                /*
                     Instantiate a Menu:  The first argument passed to the 
                     constructor is the id of the element in the page 
                     representing the Menu; the second is an object literal 
                     of configuration properties.
                */
 
                var oMenu = new YAHOO.widget.Menu("productsandservices2", { 
                                                        position: "static", 
                                                        hidedelay:  750, 
                                                        lazyload: true,
                                                            effect: { 
                                                                effect: YAHOO.widget.ContainerEffect.FADE,
                                                                duration: 0.25
                                                            } 
                                                        });
 
 
                /*
                     Define an array of object literals, each containing 
                     the data necessary to create a submenu.
                */
 
                var aSubmenuData = [
                
                    {
                        id: "communication2", 
                        itemdata: [ 
                            { text: "360", url: "http://360.yahoo.com" },
                            { text: "Alerts", url: "http://alerts.yahoo.com" },
                            { text: "Avatars", url: "http://avatars.yahoo.com" },
                            { text: "Groups", url: "http://groups.yahoo.com " },
                            { text: "Internet Access", url: "http://promo.yahoo.com/broadband" },
                            {
                                text: "PIM", 
                                submenu: { 
                                            id: "pim2", 
                                            itemdata: [
                                                { text: "Yahoo! Mail", url: "http://mail.yahoo.com" },
                                                { text: "Yahoo! Address Book", url: "http://addressbook.yahoo.com" },
                                                { text: "Yahoo! Calendar",  url: "http://calendar.yahoo.com" },
                                                { text: "Yahoo! Notepad", url: "http://notepad.yahoo.com" }
                                            ] 
                                        }
                            
                            }, 
                            { text: "Member Directory", url: "http://members.yahoo.com" },
                            { text: "Messenger", url: "http://messenger.yahoo.com" },
                            { text: "Mobile", url: "http://mobile.yahoo.com" },
                            { text: "Flickr Photo Sharing", url: "http://www.flickr.com" },
                        ]
                    },
 
                    {
                        id: "shopping2", 
                        itemdata: [
                            { text: "Auctions", url: "http://auctions.shopping.yahoo.com" },
                            { text: "Autos", url: "http://autos.yahoo.com" },
                            { text: "Classifieds", url: "http://classifieds.yahoo.com" },
                            { text: "Flowers & Gifts", url: "http://shopping.yahoo.com/b:Flowers%20%26%20Gifts:20146735" },
                            { text: "Real Estate", url: "http://realestate.yahoo.com" },
                            { text: "Travel", url: "http://travel.yahoo.com" },
                            { text: "Wallet", url: "http://wallet.yahoo.com" },
                            { text: "Yellow Pages", url: "http://yp.yahoo.com" }                    
                        ]    
                    },
                    
                    {
                        id: "entertainment2", 
                        itemdata: [
                            { text: "Fantasy Sports", url: "http://fantasysports.yahoo.com" },
                            { text: "Games", url: "http://games.yahoo.com" },
                            { text: "Kids", url: "http://www.yahooligans.com" },
                            { text: "Music", url: "http://music.yahoo.com" },
                            { text: "Movies", url: "http://movies.yahoo.com" },
                            { text: "Radio", url: "http://music.yahoo.com/launchcast" },
                            { text: "Travel", url: "http://travel.yahoo.com" },
                            { text: "TV", url: "http://tv.yahoo.com" }              
                        ] 
                    },
                    
                    {
                        id: "information2",
                        itemdata: [
                            { text: "Downloads", url: "http://downloads.yahoo.com" },
                            { text: "Finance", url: "http://finance.yahoo.com" },
                            { text: "Health", url: "http://health.yahoo.com" },
                            { text: "Local", url: "http://local.yahoo.com" },
                            { text: "Maps & Directions", url: "http://maps.yahoo.com" },
                            { text: "My Yahoo!", url: "http://my.yahoo.com" },
                            { text: "News", url: "http://news.yahoo.com" },
                            { text: "Search", url: "http://search.yahoo.com" },
                            { text: "Small Business", url: "http://smallbusiness.yahoo.com" },
                            { text: "Weather", url: "http://weather.yahoo.com" }
                        ]
                    }                    
                ];
 
 
                // Subscribe to the Menu instance's "beforeRender" event
 
                oMenu.subscribe("beforeRender", function () {
 
                    if (this.getRoot() == this) {
 
                        this.getItem(0).cfg.setProperty("submenu", aSubmenuData[0]);
                        this.getItem(1).cfg.setProperty("submenu", aSubmenuData[1]);
                        this.getItem(2).cfg.setProperty("submenu", aSubmenuData[2]);
                        this.getItem(3).cfg.setProperty("submenu", aSubmenuData[3]);
 
                    }
 
                });
                oMenu.render();
        };
 
 
    Event.onDOMReady(function() {
        var layout = new YAHOO.widget.Layout({
            units: [
                { position: 'top', height: 28, body: 'top1', scroll: null, zIndex: 2 },
                { position: 'left',  width: 300,  gutter: '5', scroll: null, zIndex: 1,  animate: false },
                { position: 'center', body: 'center', gutter: '5 5', header: '<div class="ccrHeader">'+htmlentities(p.PatientName) + ' - Current CCR ' + formatDateOfBirth(new Date(ccr.createDateTime))+'</div>'  }
            ]
        });

        var height = 500;
        if(YAHOO.env.ua.mobile == 'Apple') {
            height = 200;
        }
        
        layout.on('render', function() {
            YAHOO.util.Event.onContentReady("productsandservices", initTopMenu);
            // YAHOO.util.Event.onContentReady("productsandservices2", initLeftMenu);        


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

        loadCCRs();

        $Y('shareButton').on('click', showShareDialog);

    });
})();

<jsp:include page="accountDocumentsJavascript.jsp"/>
<jsp:include page="patientJavascript.jsp"/>
<jsp:include page="ccrDetailJavaScript.jsp"/>
var settings = ${actionBean.session.accountSettings.JSON};
</script> 

</body>
</html>