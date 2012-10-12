<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ include file="/taglibs.inc.jsp" %>
<html>
  <head>
    <base href='<%=request.getScheme() + "://" +request.getServerName()+":"+request.getServerPort()+request.getContextPath()%>/'/>
    <link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
    <link href="autoComplete.css" rel="stylesheet" type="text/css"/>
    <link rel="stylesheet" type="text/css" href="yui-2.8.2r1/menu/assets/skins/sam/menu.css"> 

    <!-- Dependencies -->  
    <script type="text/javascript" src="yui-2.8.2r1/yahoo-dom-event/yahoo-dom-event.js"></script> 
    <script type="text/javascript" src="yui-2.8.2r1/container/container_core-min.js"></script> 
     
    <!-- Source File --> 
    <script type='text/javascript' src='yui-2.8.2r1/menu/menu-min.js'></script>

    <script type="text/javascript">
      function addMenus() {
        window.menu = new YAHOO.widget.MenuBar('menu', { iframe:true, autosubmenudisplay: true });
        menu.render();

        // Add submenus
        menu.getItem(0).cfg.setProperty("submenu", {id: "menu0", 
            itemdata: [ { text: 'sm1', onclick: function(){alert('sm1')} }, 
                        { text: 'sm2', onclick: function(){alert('sm2')} }, 
                        { text: 'sm3', onclick: function(){alert('sm3')} }, 
                        { text: 'sm4', onclick: function(){alert('sm4')} }  ] });
 
        menu.getItem(1).cfg.setProperty("submenu", {id: "menu1", 
            itemdata: [ { text: 'sm1', onclick: function(){alert('sm1')} }, 
                        { text: 'sm2', onclick: function(){alert('sm2')} }, 
                        { text: 'sm3', onclick: function(){alert('sm3')} }, 
                        { text: 'sm4', onclick: function(){alert('sm4')} }  ] });

        menu.getItem(2).cfg.setProperty("submenu", {id: "menu2", 
            itemdata: [ { text: 'sm1', onclick: function(){alert('sm1')} }, 
                        { text: 'sm2', onclick: function(){alert('sm2')} }, 
                        { text: 'sm3', onclick: function(){alert('sm3')} }, 
                        { text: 'sm4', onclick: function(){alert('sm4')} }, 
                        { text: 'sm5', onclick: function(){alert('sm5')} }, 
                        { text: 'sm6', onclick: function(){alert('sm6')} }, 
                        { text: 'sm7', onclick: function(){alert('sm7')} }, 
                        { text: 'sm8', onclick: function(){alert('sm8')} }, 
                        { text: 'sm9', onclick: function(){alert('sm9')} }, 
                        { text: 'sm10', onclick: function(){alert('sm10')} }, 
                        { text: 'sm11', onclick: function(){alert('sm11')} } 
                        ] });
        menu.render();

        window.toggleTimer = window.setInterval(toggleMenu,400);
      }

      function toggleMenu() {
        if(!menu.getItem(3)) {
          var m = menu.addItem( {
            text: "Foo Menu",
            onclick: function() { return false; }
          });
          m.cfg.setProperty("submenu", {id: 'foo', 	itemdata: [
            { text:'foo',onclick: function(){ alert(1); } },
            { text:'bar',onclick: function(){ alert(2); } }]});
          menu.render();
        }
        else
          menu.removeItem(3);
      }
    </script>
  </head>
  <body style="height: 100%; overflow: hidden;" class="yui-skin-sam" onload="addMenus();">
    <div id="menu" class="yuimenubar yuimenubarnav" style='z-index: 1000000;'>
        <div class="bd">
            <ul class="first-of-type" style='border-style-top: none;'>
                <li class="yuimenubaritem first-of-type"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>Top Menu 0</a></li>
                <li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>Top Menu 1</a></li>
                <li class="yuimenubaritem"><a class="yuimenubaritemlabel" href="#" onclick='return false;'>Top Menu 2</a></li>
            </ul>
        </div>
    </div>
    <br/>
    <br/>
    <br/>
    <br/>
    <br/>
    <button onclick="clearInterval(toggleTimer);">Click to Stop Toggling Menu</button>
  </body>
</html>
