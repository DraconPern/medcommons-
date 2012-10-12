function $(id) { return document.getElementById(id); }

function getCookie(name)
{
	var dc = document.cookie;
	var prefix = name + "=";
	var begin = dc.indexOf("; " + prefix);
	if (begin == -1) {
		begin = dc.indexOf(prefix);
		if (begin != 0) return null;
	}
	else {
		begin += 2;
	}
	var end = document.cookie.indexOf(";", begin);
	if (end == -1) {
		end = dc.length;
	}
	return unescape(dc.substring(begin + prefix.length, end));
}
if(typeof queryString == 'undefined') { // don't override mochikit's version
    window.queryString = function(o) {
        var args = [];
        for(var i in o) 
             args.push(encodeURIComponent(i)+'='+encodeURIComponent(o[i]));
        return args.join('&');
    };
}
function authQueryString(x) {
    if(typeof hex_sha1 == 'undefined')
        throw 'Missing dependency: sha1.js';
    return queryString(x)+'&enc='+encodeURIComponent(hex_sha1(getCookie('mc')));
}
/*
function lightbox(hd,bd) {
    Y.all('#underlay').remove();
    Y.use('overlay', function(Y) {
        Y.one(document.body).prepend('<div id=underlay class=underlay>');
        var overlay = new Y.Overlay({
            headerContent:hd,
            bodyContent:bd,
            width: '15em',
            centered: true
        });
        overlay.render('#underlay');
        Y.one('#underlay').setStyle('opacity',0.5);
    });    
}*/
function dump(o) {
    var x = '';
    for(i in o) {
      x += i + ' : ' + o[i] + '\n';
    }  
    return x;
}
function add_nav(n,before) {
  for(var i=0; i<top_nav.length; ++i) {
      if(before == top_nav[i].text) {
          top_nav.splice(i,0,[n]);
          return;
      }
  }
  top_nav.push(n);
}
function render_top_nav() {
  var h = '';
  for(var i=0; i<top_nav.length; ++i) {
    var item = top_nav[top_nav.length-i-1];

    var hidden = (item.hidden && (typeof item.hidden == 'function')) ? item.hidden() : item.hidden;
    if(hidden)
      continue;
    if(item.disabled)
      h += '<div class="button disabled">'+item.text+'</div>';
    else 
      h += '<div class="button"><a class="menu_'+item.text.toLowerCase().replace(/ /g,'')+'" href="'+item.href+'"'
            +(item.target?'target="'+item.target+'"':'')
            +'>'+(item.render?item.render():item.text)+'</a></div>';
  }
  $('navcontainer').innerHTML=( h );
}

function nav(txt) {
  for(var i=0; i<top_nav.length; ++i) {
    if(top_nav[i].text == txt)
      return top_nav[i];
  }
  return {};
}

function get_mc_attribute(att) {
  var mc = getCookie('mc');
  if(!mc)
    return null;
  var atts = mc.split(',');
  for(var i=0; i<atts.length; ++i) { 
    if(atts[i].match(new RegExp('^'+att+'='))) {
      return atts[i].split('=')[1];
    }
  }
  return null;
}

(function() {
    var idleSince = (new Date().getTime());
    var timeoutHandler = null;
    var timeoutTimer = null;

    function onActivity() {
      idleSince = (new Date().getTime());
    }
    function checkTimeout() {
      var now = (new Date().getTime());
      if(now - idleSince > MAX_IDLE_TIME) {
        if(timeoutHandler) 
          timeoutHandler();
      }
    }
    function listen(evt, handler) {
      var d = document;
      if(d.addEventListener)   // DOM Level 2 Event Model
        d.addEventListener(evt, handler, true);
      else
      if(d.attachEvent)  // IE 5+ Event Model
        d.attachEvent("on"+evt, handler);
    }
    window.setIdleTimeout = function(handler) {
      timeoutHandler = handler;
      if(!timeoutTimer) {
	      listen("mousemove", onActivity);
	      listen("keydown", onActivity);
	      timeoutTimer = setInterval(checkTimeout, 10000);
      }
    }
  })();

  Y.on("domready", function() {
      if(getCookie('mc')) {
          signed_on = true;
          nav('Logout').hidden = false;
          nav('Dashboard').hidden = false;
          nav('Settings').hidden = false;
          nav('Sign In').hidden = true;
          var s = get_mc_attribute('s');
          if((s & 2) > 0) {
            nav('Services').hidden = false;
          }
          if((s & 4) > 0) {
            nav('Settings').hidden = true;
          }
          if((s & 16) > 0) {
            nav('Simtrak Admin').hidden = false;
          }
          if((s & 32) > 0) {
            nav('Simtrak Admin').hidden = true;
            nav('Services').hidden = true;
            nav('Settings').hidden = false;
          }
          if((s & 64) > 0) {
            $('logoImg').onload = function() {
              $('logoImg').style.visibility = 'visible';
            };
            $('logoImg').src='/acct/logo.php';
          }
          else
              $('logoImg').style.visibility = 'visible';
              
          setIdleTimeout(function() {location.href='/acct/logout.php?next=%2Facct%2Flogin.php%3Fprompt%3Dexpiredsession';}); 
      }
      else {
          signed_on = false;
          nav('Sign In').hidden = false;
          for(i in menu=['Dashboard','Logout','Settings', 'Services']) {
            nav(menu[i]).hidden = true;
          }
          $('logoImg').style.visibility = 'visible';
      }
      if(typeof customize_nav != 'undefined')
          customize_nav();
      render_top_nav();
  });  
