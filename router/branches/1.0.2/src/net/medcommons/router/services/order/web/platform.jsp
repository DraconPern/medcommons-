<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/taglibs.inc.jsp" %>
<!--
 Copyright 2010 MedCommons Inc.   All Rights Reserved.
-->
<%-- This page is a wrapper around the other contents of the MedCommons System --%>
<c:set var="remoteAccessAddress"><mc:config property='RemoteAccessAddress'/></c:set>
<c:set var="brandName"><mc:config property="BrandName" default="MedCommons"/></c:set>
<html>
  <head>
    <mc:base/>
    <title>${brandName} CCR Transfer and Repository Services
      <c:if test='${not empty accid}'> - Acct #<mc:medcommonsId>${accid}</mc:medcommonsId></c:if></title>
    <meta name="viewport" content="width=1024" />
    <link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
    <c:set var="accountsBaseUrl"><mc:config property='AccountsBaseUrl'/></c:set>
    <c:set var="accountsUrl"><mc:config property='AccountServer'/></c:set>
    <c:if test='${not empty desktop.rssId}'><link rel="alternate" type="application/rss+xml" title="Activity Log for Acct #<mc:medcommonsId>${desktop.rssId}</mc:medcommonsId>" href="${accountsUrl}/../gwrss.php?a=${desktop.rssId}"></c:if>

    <pack:style>
      <src>autoComplete.css</src>
      <src>top.css</src>
    </pack:style>

    <%--
    <link rel='stylesheet' type='text/css' href='yui-2.6.0/menu/assets/skins/mc/menu.css'/>
    --%>
    <pack:style src="yui-2.6.0/menu/assets/skins/mc/menu.css"/>
    <c:url var="mcgwUrl" value="${accountsUrl}/setmcgw.php">
      <c:param name="gw" value="${remoteAccessAddress}"/>
    </c:url>
    <%--
      Browser Specific Hacks - Yuck
    --%>
    <style type="text/css">
      <% if(request.getHeader("User-Agent").indexOf("Safari")>=0) { %>
      span.currentTab {
        top: 0px; /* Safari only hack */
      }
      <%}%>
    </style>
    <!--[if IE]>
    <style type="text/css">
    #tabborder {
        position: static;
    }
    #nav li {
      display: inline; 
    } 
    </style>
    <![endif]-->

    <script language="JavaScript" type="text/JavaScript">

      <%--
        Empty by default.  However this is checked for each rollover to let
        a page change its tab image by inserting a value here.
      --%>
      var tabImages = new Array();

      var acctServer = '<mc:config property='AccountServer'/>';

      function rollOver(img) {
        if(!img)
	      return;
        var index = parseInt(/[0-9]/.exec(img.id));
        var overSrc = 'images/mmtab_over_0'+index+'.gif'; // default
        if(tabImages[index] != null) {
          overSrc = tabImages[index].overSrc;
        }
        img.src=overSrc;
      }

      function rollOut(img,index) {
        if(!img)
          return;
	      /*
        var index = parseInt(/[0-9]/.exec(img.id));
        var offSrc = 'images/mmtab_off_0'+index+'.gif'; // default
        if(tabImages[index] != null) {
          offSrc = tabImages[index].offSrc;
        }

        var tab = $('tab'+(index));
        if(!tab.current) {
          img.src=offSrc;
        }
        */
      }
    </script>


    <script language="JavaScript">
      <%-------------------------------------------------------------
           Tab Functions.

           These are inline to minimize round trips to the server.

           - Tabs are represented by <span> elements inserted into the DOM.
           - The active tab is called "currentTab".
           - The URL of a tab is set as the 'url' property on the <span> element.
           - Various functions are available to show / hide / modify / insert tabs

      ---------------------------------------------------------------%>
      var tablist = new Array();

      function showThisTab() {
        showTab(this);
      }

      function showTabById(id) {
        showTab($(id),true);
      }

      function highlightTabById(id,seturl) {
        highlightTab($(id),seturl);
      }

      function showTab(tabToShow) {
        var onSwitchTab;
        try {
          onSwitchTab = contents.onSwitchTab;
        }
        catch(ex) {
          onSwitchTab = null;
        }

        if(onSwitchTab!=null) {
          if(contents.onSwitchTab(getTabUrl(tabToShow))) { // allow tab window to cancel
            if(tabToShow != currentTab)
              $('patientHeader').style.visibility = 'hidden';
            highlightTab(tabToShow,false);
            setButtons([]);
          }
        }
        else  {
          if(tabToShow != currentTab)
            $('patientHeader').style.visibility = 'hidden';
          highlightTab(tabToShow,true);
          setButtons([]);
        }

      }

      <%--
         Computes a url for the tab based on it's configured url but
         also adding a parameter to indicate the mode of the tab
         (view vs edit).
       --%>
      function getTabUrl(tab) {
        var url = tab.url;
        var params = '';
        var mode = tab.mode;
        if(mode != null) {
          // toggle mode
          if(tab.current) {
            if(mode=="edit")
              mode = "view";
            else {
              <%-- If user not allowed to edit then don't auto switch them --%>
              <mc:enabled name='viewer.editMode'>
	              mode = "edit";
              </mc:enabled>
            }
          }
          params += 'mode='+mode;
        }
        if(tab.ccrIndex != null) {
          params += "&ccrIndex=" + tab.ccrIndex;
        }
        if(!url.match(/^javascript:/) && (params != '')) {
          if(url.indexOf('?')<0) {
            url += "?";
          }
          else
            url += "&";
          url += params;
        }
        <%-- 
          We make relative URLs absolute because 
          of a freaky behavior of IE:  when the base url
          is set in the parent frame it somehow affects
          URLs that are navigated to by the child frame
          if the child javascript is invoked by the parent.
        --%>
        if(url.match(/^http/)==null) {
          url = '${baseUrl}' + url; 
        }

        return url;
      }

      <%--
         The current tab is tracked and kept up to date here.
         This means any child window code can store properties
         in here to persist them and remember them in the session
         independent of the server.
      --%>
      var currentTab = null;

      function searchHighlightTab(tabToShow,tabParent,seturl) {
        for(var i=0; i<tabParent.childNodes.length; i++) {
          var tab = tabParent.childNodes[i];
          searchHighlightTab(tabToShow,tab,seturl);

          if(tab.nodeType!=1)//element
            continue;

          if((tab.id == null) || (tab.id == '')) // ignore misc elements, only want ones that we gave an id
            continue;

          if(tab.tagName == 'IMG') // ignore misc elements, only want ones that we gave an id
            continue;

          //log('found tab.url='+tab.url);

          var index = parseInt(/[0-9]/.exec(tab.id));
          if(tab == tabToShow) {
            tab.className="currentTab";
            if(!isNaN(index)) {
              rollOver($('tabImg'+index));
            }

            setTabImages(tab,'images/tablefthl.png', 'images/tabrighthl.png','4','currentTab');
            currentTab = tab;
            if(seturl) {
              var url = getTabUrl(tabToShow);
              el("contents").src=url;
            }
            tab.current = true;
            unhideTab(tabToShow.id);
          }
          else
          if((tab.tagName == 'SPAN') || (tab.tagName == 'span')) {
            tab.className="normalTab";
            tab.current = false;
            if(!isNaN(index)) {
              rollOut($('tabImg'+index));
            }
            setTabImages(tab,'images/tableft.png', 'images/tabright.png', '3','normalTab');
          }
        }
      }

      function getVisibleTabs() {
        var tabs = $('tabsContainer');
        return tabs.getElementsByTagName('span');
      }

      function setTabImages(tab, srcleft,srcright,top,c) {
        var tabParent = tab.parentNode;
        var left = null;
        for(var i=0; i<tabParent.childNodes.length; ++i) {
          var img = tabParent.childNodes[i];
          if((img.tagName != 'IMG') && (img.tagName != 'img'))
            continue;

          if(img.src.indexOf('tab')<0)
            continue;

          img.style.position = 'relative';
          img.style.top = top;

          if(!left) {
            left = img;
            left.src = srcleft;
            left.className=c;
          }
          else {
            img.src = srcright;
            img.className=c;
            break;
          }
        }
      }


      function setTabIcon(tab, img) {
        if(tab == null) {
          tab = currentTab;
        }
        if(!tab.id) {
          tab = el(tab);
        }
        tab.img = img;
        setTabText(tab, getTabText());
      }

      function setTabText(tab,text) {
        log("setting tab text to " + text + ": " + stacktrace());

        if((tab == undefined) || (tab == null)) {
          tab = currentTab;
        }

        if(!tab.id) {
          tab = el(tab);
        }

        tab.text = text;
        if(tab.img)
          tab.innerHTML=text+'&nbsp;' + tab.img;
        else
          tab.innerHTML=text;
      }

      function getTabText(tab) {
        if(tab == null) {
          tab = currentTab;
        }

        if(!tab.id) {
          tab = el(tab);
        }
        if(tab.text)
          return tab.text;
        else
          return tab.innerHTML;
      }

      function highlightTab(tabToShow,seturl) {
        $('tabs').style.height='30px';

        <%-- Iterate tabs, highlight tab, hide others --%>
        var tabs = $('tabsContainer');
        searchHighlightTab(tabToShow,tabs,seturl);
      }

      <%--
         Completely removes the tab from the display
       --%>
      function hideTab(id) {
        var tab = el(id);
        if(tab) {
          var li = tab.parentNode;
          while(li.tagName.toLowerCase() != 'li') {
            li = li.parentNode;
          }
          li.style.display = 'none';
          tab.hidden = true;
        }
      }

      function removeTab(t) {
        removeElement(t.parentNode);
      }

      <%--
        Remove all tabs satisfying condition f
      --%>
      function removeTabs(f) {
        var toRemove = filter(f, $('nav').getElementsByTagName('SPAN'));
        forEach(toRemove, function(t) {
            log("removing tab " + t.id);
            removeTab(t);
        });
      }

      function unhideTab(id) {
        var tab = el(id);
        if(tab) {
          var li = tab.parentNode;
          while(li.tagName.toLowerCase() != 'li') {
            li = li.parentNode;
          }
          if((YAHOO.env.ua.ie > 0) || (YAHOO.env.ua.gecko<=1.8))
            li.style.display = 'inline';
          else
            li.style.display = 'inline-block'; // helps slightly in some browsers when zoomed
        }
      }

      <%-- Find and return next tab on left) --%>
      function previousTab() {
        var previous = null;
        var found =false;
        nodeWalk($('nav'),function(n) {
          if(n == currentTab) {
            found = true;
            return null;
          }

          if(!found && n.id && (!n.hidden) && n.id.match(/^tab[0-9]*$/))
            previous = n;

          return n.childNodes;

        });

        return previous;
      }

      <%-- Find and return next tab on right) --%>
      function nextTab() {
        var next = null;
        var found = false;
        var currFound = false;
        nodeWalk($('nav'),function(n) {
          if(found)
            return null;

          if(n == currentTab) {
            currFound = true;
          }
          else
          if(currFound && n.id && (!n.hidden) && n.id.match(/^tab[0-9]*$/)) {
            found = true;
            next = n;
            return null;
          }
          return n.childNodes;
        });

        return next;
      }

      function initContentsHeight() {
        // note, ideally would calculate toolsHeight as below, but it is not available
        // at load time, and want to avoid things "jumping" around by resizing afterwards.
        // var toolsHeight = (elementPosition('menu').y + elementDimensions('menu').h);
        if(typeof viewportSize == 'undefined')
          return;
        var toolsHeight = 60;
        if($('topmessage'))
          toolsHeight += elementDimensions($('topmessage')).h;

        var availableHeight = viewportSize().h - toolsHeight;
        if($('patientHeader'))
          availableHeight -= elementDimensions('patientHeader').h;

        try {
          $('contents').height = availableHeight;
          if(window.contents.viewportSize)
            log("Available height = " + availableHeight + " internal height = " + window.contents.viewportSize().h);
          if(contents.onParentSize) {
            window.contents.eval('onParentSize();');
          }
        }
        catch(e) {
          log("error adjusting content height: " + e);
        }

      }

      // Set resize handler to keep updating window when size changes
      window.onresize = initContentsHeight;

      var tabcount = 0;

      function addTabHandlers(tabParent) {
        for(var i=0; i<tabParent.childNodes.length;i++) {
          var tab = tabParent.childNodes[i];
          if(tab != tabParent)
            addTabHandlers(tab);

          if(tab.nodeType!=1)  // must be element
            continue;

          if((tab.id == null) || (tab.id == '')) // ignore misc elements, only want ones that we gave an id
            continue;


          if((tab.tagName == 'span') || (tab.tagName == 'SPAN')) {
            // Set handlers for this tab
            tab.onclick=showThisTab;
            tab.url=tablist[tab.id];
            //log('initing tab with id='+tab.id);
          }
        }
      }

      var tabCounter = null;
      function getTabs() {
        return $$('ul#nav li span');
      }

      function replaceTab(txt,url,tip,before) {
        found = false;
        forEach(getVisibleTabs(), function(t) {
          if(getTabText(t) == txt) {
            found = true;
            t.url = url;
            t.tip = tip; // todo, need to set tip in DOM
          }
        });

        if(!found) 
          addTab(txt,url,tip,before);
      }

      function addTab(txt,url,tip,before) {
        if(!tabCounter)
            tabCounter = counter(10);

        var tabId = 'tab'+tabCounter();
        if(!tip) {
          tip = "";
        }
        var tabSpan = SPAN({'id':tabId,'class':'normalTab',title: tip}, txt);
        var newTab = LI(null,IMG({src:'images/tableft.png'}),
                      tabSpan,
                      IMG({src:'images/tabright.png'}));

        log(before);
        if(before == null) {
          appendChildNodes($('nav'),newTab);
        }
        else {
          $('nav').insertBefore(newTab,before.parentNode);
        }

        tablist[tabId] = url;
        addTabHandlers($('tabsContainer'));
        return tabSpan;
      }

      <c:set var="smallLogSrc"><mc:config property="SmallLogoImg" default="${remoteAccessAddress}/images/logo_small.png"/></c:set>
      <c:set var="brandText"><mc:config property="BrandText" default="MedCommons"/></c:set>

      var initialContentsUrl = '${initialContentsUrl}';
      var originalHash  = null;

      <c:if test='${readFragment}'>
        var modes = { e: 'edit', v: 'view' };
        if(window.location.hash) {
          originalHash = window.location.hash;
          var parts = /#([0-9])*([ev])/.exec(window.location.hash);
          if(parts.length == 3) {
            if(initialContentsUrl.indexOf('?')>0)
              initialContentsUrl += '&';
            else
              initialContentsUrl += '?';

            initialContentsUrl += 'ccrIndex='+parts[1] + '&mode=' + modes[parts[2]] + '&clean=true';

            setInterval(function() {
              if(originalHash != window.location.hash) {
                originalHash = window.location.hash;
                window.location.reload();
              }
            }, 300);
          }
        }
      </c:if>


      var startupMsg = null;

      function initTabs() {
        checkIE();
        ce_connect('openCCR', onOpenCCR);
        ce_connect('newCCR', onOpenCCR);
        ce_add_server(acctServer+'/ce_signal.php');
        var tabs=$("tabsContainer");
        addTabHandlers(tabs);
        log("initial contents = " + initialContentsUrl);
        <c:if test='${empty initialContents}'>
          <c:set var='initialContents'>tab6</c:set>
        </c:if>
        <c:choose>
          <c:when test='${empty NodeID}'>
                log("setting initial tab = "+initialContentsUrl);
                highlightTab($("${initialContents}"),false);
                $("contents").src="keyConfig.ftl";
          </c:when>
          <c:when test='${!empty initialContents}'>
            <c:choose>
              <c:when test='${!empty initialContentsUrl}'>
                log("setting initial tab = ${initialContentsUrl}");
                highlightTab($("${initialContents}"),false);
                $("contents").src=initialContentsUrl;
              </c:when>
              <c:otherwise>
                showTab($("${initialContents}"));
              </c:otherwise>
            </c:choose>
          </c:when>
          <c:otherwise>
            showTab($("tab2"));
          </c:otherwise>
        </c:choose>

        window.menu = new YAHOO.widget.MenuBar('menu', { iframe:true, autosubmenudisplay: true });
        menu.render();

        var tzOffset = -1*(new Date()).getTimezoneOffset();
        var tz = 'GMT'+ (tzOffset>=0?'+':'-')+Math.floor(Math.abs(tzOffset/60))+':' + numberFormatter("00")(Math.abs(tzOffset%60));
        setCookie('mctz',tz);
        loadScript('${mcgwUrl}');
        window.focus();
        <c:if test='${not empty desktop.accessTrackingReference and desktop.accessTrackingReference.constraint == "EXPIRED"}'>
        startupMsg = 'The link you have used to open this CCR is a one-time-use link.<br/><br/>'
                     +'You should send, save or add this CCR to your own account if you wish to access it in the future';
        </c:if>
      }

      function checkModified() {
        closeSession();
        // Check if any tabs modified
        if(some(getTabs(), function(t) { return t.modified == true; })) {
          return 'One or more CCRs have unsaved changes. You will lose these changes if you leave without saving them.';
        }
      }

      var is_closed = false;
      function closeSession() {
        if(is_closed)
          return;
        is_closed = true;
        <%-- note: used to test window.opener here but in FF closing the opener window will
             cause this reference to become null --%>
        if(window.opened) {
          if(window.name == 'ccr') {
            <%--// Hack: find context --%>
            var context = window.location.pathname.match(/(\/[A-Za-z]*)\/.*$/)[1];
            var expires = new Date( new Date().getTime()-100000 );
            log("sending closeCCR signal");
            ce_signal('closeCCR',window.contents.ccrGuid);
            // log("Deleting JSESSIONID cookie " + expires + " from path " + context);
            // setCookie('JSESSIONID', 'deleted', expires, context);
          }
        }
      }

      var disable_auto_close = false;
      function onOpenCCR(guid) {
        // Other CCR opened - close myself
        log("platform: onOpenCCR");
        if((ce_last_event_src != window.contents.ce_src_id) && (ce_last_event_src != window.ce_src_id) && !disable_auto_close) {
          window.contents.location.href='closed.jsp';
          window.close();
        }
      }

      /**
       * Sets the tracking number and PIN details that will be advertised
       * to clients as currently showing in this viewer. (eg. for FF CCR Client)
       */
      function setAdvertisedCcr(tn,pin) {
        window.medcommons_advertised_ownerId=tn;
        window.medcommons_advertised_pin=pin;
      }

      function checkIE() {
        if((YAHOO.env.ua.ie > 0) && (YAHOO.env.ua.ie < 7)) {
          alert( 'WARNING:  You are using Internet Explorer version 6.0 or older.\n\n'
                +'This version is not supported by this web site and you may experience problems using it.\n\n'
                +'For best experience we recommend upgrading to a supported browser such as: \n\n'
                +'   -  Internet Explorer 7.0 or later\n\n'
                +'   -  FireFox 2.0 or later\n\n'
                +'   -  Safari 3.0 or later'
                );
        }
      }

      tablist["tab1"]="updateCcr.do?forward=selection";
      tablist["tab2"]="updateCcr.do?forward=home";
      tablist["tab3"]="updateCcr.do?forward=consent";
      tablist["tab4"]="updateCcr.do?forward=transaction";
      tablist["tab5"]="updateCcr.do?forward=desktop";
      tablist["tab6"]="updateCcr.do?forward=viewer";
      tablist["tab7"]="updateCcr.do?forward=logon";

      // Allows FF client to "see" the url of the gateway it is looking at
      window.medcommons_advertised_cxpUrl='<mc:config property="RemoteCxpUrl"/>';
      try {
        if(window.opener) {
          window.opened = true; // window.opener may disappear if user closes opener
        }
        window.onbeforeunload=checkModified;
        window.onunload=closeSession;
      }
      catch(e) {
      }
      useCleanURLs = <mc:config property="EnableModRewriteURLs" default="true"/>;
    </script>

    <%--
    <c:set var="themeCssUrl"><mc:config property="HomePageServer"/>/theme.css.php</c:set>
    <link rel="stylesheet" type="text/css" href="${themeCssUrl}"/>
    --%>
  </head>
  <body style="height: 100%; overflow: hidden;" class="yui-skin-mc" onload="initTabs();">
    <c:set var="homeUrl"><mc:config property="HomePageServer"/></c:set>
    <div id="tabs" class="lightBackground" style="height: 30px; width: 100%; vertical-align: bottom;" >
        <c:set var='accountsUrl'><mc:config property="AccountServer"/></c:set>
        <br/>
        <span id="tabsContainer" class="bottomAlign" style="width: 100%;">
          <%--
        <img src="images/topmenu_off_01.gif" id="bigLogoImg" style="display: none;" width="600" height="81"/>
          --%>
        <img src="images/transparentblank.gif" width="50" height="1px"/>

        <%-- The Tab List --%>
        <ul id="nav">
          <li style="display: none;" ><img src="images/tableft.png"/><span id="tab4" class="normalTab">CCR</span><img src="images/tabright.png"/></li>
          <li style="display: none;"><img src="images/tablefthl.png"/><span id="tab6" class="normalTab">Received CCR</span><img src="images/tabrighthl.png"/></li>
        </ul>
        <a href="${homeUrl}/" border="0"><img alt="<c:out value='${brandText}'/>"
             id="smallLogoImg"
             border="0"
             height="30"
             src="${smallLogSrc}"
             style="position: absolute;  right: 20px; bottom: 0px;"
             />
         </a>
      </span>
    </div>
    <div id="tabborder"></div>
    <div id="menu" class="yuimenubar yuimenubarnav" style='z-index: 1000000;'>
        <div class="bd">
            <ul class="first-of-type" style='border-style-top: none;'>
                <li class="yuimenubaritem first-of-type"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>CCR File</a></li>
                <li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>CCR Attached Documents</a></li>
                <li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>CCR Section</a></li>
                <c:set var='enableSymTrak'><mc:config property="EnableSymtrak" default="false"/></c:set>
                <c:if test='${enableSymTrak}'>
                  <li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>Simtrak</a></li>
                </c:if>
            </ul>
        </div>
    </div>
    <c:if test='${not empty expiryDate}'>
      <s:layout-render name="/infobar.jsp">
        <s:layout-component name="contents">
          <b>This is a Temporary HealthURL and will expire <mc:age>${expiryDate.time}</mc:age></b>
          <c:choose>
            <c:when test='${desktop.patientMode}'> - <a href='UpgradeAccount.action?ccrIndex=0' target='_top'>Copy to My HealthURL</a></c:when>
            <c:when test='${desktop.accessMode == "INCOMPLETE_VOUCHER"}'> - 
              <span id='voucherStatus'><a href='#completeVoucher' onclick='completeVoucher(); return false;'>Mark as Completed</a></span></c:when>
            <c:otherwise>&nbsp;</c:otherwise>
          </c:choose>
        </s:layout-component>
      </s:layout-render>
    </c:if>
    <c:if test='${desktop.accessMode == "ACCOUNT_IMPORT_RESULT"}'>
      <s:layout-render name="/infobar.jsp">
        <s:layout-component name="contents"><b>Verify the result of your import below. &nbsp;&nbsp;<a href='${accountsBaseUrl}acct/home.php'>Return to Dashboard</a></s:layout-component>
      </s:layout-render>
    </c:if>
    <div id="patientHeader" class='hidden'>&nbsp;</div>

    <iframe
      name="contents"
      id="contents"
      width="100%"
      height="100%"
      style="border-style: none; overflow: auto; z-index: 0;"
      src="blank.html"
      frameborder="0"
      onload='initContentsHeight();'
      >
      &nbsp;
    </iframe>
  </div>
  <div id="acdiv"></div>
  <div id='topbar'>
    <span id='topbuttons'>&nbsp;</span>
    <span id='topcenterbuttons' class='hidden'><a class='tcb' href='javascript:window.contents.showQuickReply();'>Click Here</a> to Quick Reply to this CCR</span>
  </div>
   <%--Container for OCX which detects HealthFrame --%>

  <%--

  Removed because it causes an annoying prompt that interferes with 
  some IE users

  <object
        ID="HBCHECK" Name="HBCHECK"
        WIDTH=1 HEIGHT=1
   classid="clsid:261B31E6-20A6-482C-A78E-46E68ACFB54B"
    CODEBASE="CheckForHealthBook.cab#version=1,0,0,1">
  </object>
  --%>

  <pack:script>
      <src>yui-2.6.0/yahoo-dom-event/yahoo-dom-event.js</src>
      <src>yui-2.6.0/container/container_core-min.js</src>
      <src>yui-2.6.0/menu/menu-min.js</src>
      <src>mochikit/MochiKit.js</src>
      <src>utils.js</src>
      <src>autoComplete.js</src>
      <src>common.js</src>
  </pack:script>
  </body>
</html>
<c:remove var='initialContentsUrl'/>
<c:remove var='initialContents'/>
