<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %> 
<%--
  MedCommons Tool Palette

  This jsp renders a floating div that contains links that perform
  operations across the various tabs. 

  Usage:
    Child frames can call window.parent.setTools(), passing an array defining 
    a menu of actions to perform.  Each element in the passed array needs to be
    an array itself with 2 elements for each menu item and also an optional
    third element which is needed only rarely.

    The structure is:

      [ 
        [<display text>,  <child frame action>,  <optional parent frame action> ],
        [<display text>,  <child frame action>,  <optional parent frame action> ],
        ...
      ]

    The "action"s provided can be any snippet of javascript you want executed.
    The parent frame action is mainly useful to open popup windows since
    the IE popup blocker will kill windows that are not opened directly by
    a frame experiencing a "click" event - hence such popups can only be
    opened by the parent window.

  @author Simon Sadedin, MedCommons Inc.
--%>
<script language="JavaScript">
  var dragEl;
  function showToolPalette() {
    $('toolPaletteDiv').style.display='block';
    show('menuholder');
    log("showing menu");
  }

  function hideToolPalette() {
    $('toolPaletteDiv').style.display='none';
    hide('menuholder');
  }

  function moveToolPalette(xpos,ypos) {
    // NOTE: weird FF bug causes size to change on move
    var size = elementDimensions('toolPaletteDiv');
    setElementPosition( 'toolPaletteDiv',{ x: xpos, y: ypos } );
    setElementDimensions( 'toolPaletteDiv', size);
  }

  function getToolPalettePosition() {
    return elementPosition($('toolPaletteDiv'));
  }

  function dragMove(evt) {
    if(!evt) evt=window.event;
    //log("Drag Move ("+evt.clientX+","+evt.clientY + ")");
    dragEl.style.left=evt.clientX-40;
    dragEl.style.top=evt.clientY-13;
    dragEl.style.right='';
    cancelEventBubble(evt);
  }
  function dragOut(evt) {
    if(!evt) evt=window.event;
    log("mouseout ("+evt.clientX+","+evt.clientY + ")");
    if(dragEl!=null) {
      dragMove(evt);
    }
  }
  function dragEnd() {
    if(dragEl != null) {
      log("Ending drag");
      window.document.body.onmousemove=null;
      window.document.body.onmouseup=null;
      window.document.body.onmouseout=null;
      dragEl.style.cursor="default";
      dragEl = null;
    }
    $('toolPaletteShim').style.display = 'none';
  }
  function dragStart(evt,el) {
    log("Beginning Drag");
    if(evt==null)
      evt=window.event;
    <%-- only for click in top 16px --%>
    var palY=findPosY($('toolPaletteDiv'));
    if(evt.clientY < palY + 16) {
      window.document.body.onmousemove=dragMove;
      window.document.body.onmouseup=dragEnd;
      var contents = $('contents');
        
      dragEl = el;
      dragEl.onmouseout=dragOut;
      dragEl.style.cursor="move";
      cancelEventBubble(evt);
      dragMove(evt);
    }
    $('toolPaletteShim').style.display = 'block';
  }

  function clearForm() {
    if(window.contents.activeForm != null) {
      window.contents.activeForm.reset();
    }
  }

  function execTool(index) {
    $('toolPaletteShim').style.display = 'none';    
    if(currentTools[index].length>2) {
      window.eval(currentTools[index][2]);
    }
    window.contents.setTimeout(currentTools[index][1],0);    
    return false;
  }

  <%-- Disable all tools where predicate p is true --%>
  function disableTools(p,value) {
      value = value ? true : false;
      forEach(
        ifilter(function(a) { return p(scrapeText(a)); },$$('#allTools a')),
        function(a) { a.parentNode.style.display = (value ? 'none' : 'inline'); }
      );
  }

  <%-- Add / enable the given tool --%>
  function enableTool(tool,enabled) {
    var txt = isArrayLike(tool) ? tool[0] : tool;
    var found = false;
    if(enabled == undefined)
      enabled = true;
    forEach($$('#allTools a'),function(t) {
      log("Checking tool " + scrapeText(t));
      if(scrapeText(t) == txt) {
        log('Found tool ' + txt + ' Setting enabled to ' + enabled);
        found = true;
        t.parentNode.style.display = enabled ? 'inline' : 'none';
      }
    });
  }

  function isToolEnabled(tool) {
    var txt = isArrayLike(tool) ? tool[0] : tool;
    return some($$('#allTools a'), function(t) { return scrapeText(t) == txt && (t.parentNode.style.display != 'none'); });
  }

  <%-- sets the given tools (array of tool/function mappings) in the tool window --%>
  var currentTools;
  function setTools(tools) {
    log("setting tools");
    currentTools = tools;
    var toolsHtml = "";
    showToolPalette();

    var submenu = [];
    for(i=0;i<tools.length;i++) {
      var tool = tools[i];      
      toolsHtml += "<span><a onfocus='this.blur();' href='#' onclick='return execTool("+i+");'>" + tool[0] + "</a><br/></span>";
      submenu.push( { text: tool[0], onclick: { fn: partial(execTool, i) } } );
    } 
    $("allTools").innerHTML=toolsHtml;    

    // FF bug - sometimes after page load the shim is on top.
    // fix by forcing to not display here.
    $('toolPaletteShim').style.display = 'none';

    var oldSubmenu = menu.getItem(0).cfg.getProperty("submenu");
    if(oldSubmenu) {
      while(oldSubmenu && oldSubmenu.getItems().length) {
            oldSubmenu.removeItem(0);
      }
      forEach(submenu, function(mi) {oldSubmenu.addItem(mi);});
    }
    else 
      menu.getItem(0).cfg.setProperty('submenu',{ id: 'ccrFileMenu', itemdata: submenu });

    menu.render();
  }

  function createYUIMenuItem(t) {
    return { text: t[0], onclick: t[1] };
  }

  function niy() {
    alert('This function is still under construction!');
  }
</script>
<style type="text/css">
  .toolLink a:visited {
    color: blue;
  }
  .toolLink a:hover {
    color: red;
  }
  <%-- remove comments to prevent underline of links
  .toolLink a {
    text-decoration: none;
  }--%>
  #allTools {
    position: relative;
    top:3px;
  }
</style>

<div id="toolPaletteShim" style="position: absolute; width: 100%; height: 100%; background-color: transparent; display: none; z-index: 1000000;">&nbsp;</div>

<div id="toolPaletteDiv" onmousedown="dragStart(event,this);" 
  style="display: none; z-index: 1000001; /*filter: alpha(opacity=90);*/ position: absolute; top: 182px; left: 10px; padding: 3px; border-style: solid; border-width: 16px 2px 2px 2px; border-color: #aaaaaa; color: #666699; font-family: arial; font-size: 10;  background-color: #fff39c;">
  <%-- Hack:  shift everything up 15px with rel positioning so that first row appears as title over border --%>
  <div id="toolPaletteTitle" style="background-color: transparent; position: relative; top: -16px;">
    <span style="cursor: move; color: white;">Tools</span><br/>
    <span class="toolLink" id="allTools">&nbsp;
    </span>
  </div>
</div>

