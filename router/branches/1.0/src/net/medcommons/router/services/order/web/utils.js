/**
 * log - useful to send log statements to external logger
 * (used with Badboy to log statements to log file)
 */
var enableLog = true;
window.log = function(msg) {
  try {
    if(enableLog) {
      if(window.console && window.console.log) { // Firebug
        window.console.log(msg);
      }
      else
      if(window.external) {
        window.external.info(msg); // Badboy
      }
      else
      if(logDebug)
        logDebug(msg);
    }
  }
  catch(er) {
    enableLog=false;
  }

}

/**
 * Shorthand for "document.getElementById()"
 */
function el(id) {
  return document.getElementById(id);
}

/**
 * Shorthand to hide one or more elements, allowing it might be null
 */
function hide() {
  for(i=0; i< arguments.length; ++i) {
    var id = arguments[i];
    var e = document.getElementById(id);
    if(e) e.style.display='none';
  }
}

/**
 * Shorthand to show an element, allowing it might be null
 */
function show() {
  for(i=0; i< arguments.length; ++i) {
    var id = arguments[i];
    var e = document.getElementById(id);
    if(e) e.style.display='block';
  }
}

/**
 * Control element visibility by setting class: all args except last are elements to show / hide
 * Last arg is true to show, false to hide.
 *
 * For this function to work you need to following CSS rule:
 *
 * .invisible {
 *   display: none;
 * }
 */
function visibility() {
  var len = arguments.length-1;
  for(i=0; i<len; ++i) {
    var id = arguments[i];
    var e = $(id);
    if(e) {
      if(arguments[len]) {
        removeElementClass(e,'invisible');
        removeElementClass(e,'hidden');
      }
      else
        addElementClass(e,'invisible');
    }
  }
}


/**
 * Selects option with value v in a select
 */
function selectOption(s,v) {
  var options = s.getElementsByTagName('option');
  for(var i=0; i<options.length; ++i) {
      if(options[i].value == v)  {
        s.selectedIndex = i;
        break;
      }
  }
}

/**
 * Attempt to resolve path against object, return default
 * if property does not exist.
 */
function nvl(x,path,def) {
  var r = resolve(path,x,def);
  return r!=null ? r : def;
}

/**
 * Attempt to resolve dot separated path against object x.
 * Optional default value returned if property not found.
 *
 * If a property maps to an array then an attempt will
 * be made to continue resolving child properties by
 * examining the zero'th element of the array.  
 *
 * Example:  resolve(x,'foo.bar.baz.fubar')
 *
 * This would resolve  x.foo.bar.baz.fubar  as well as
 * x.foo[0].bar.baz[0].fubar
 */
function resolve(x,path,def,i) {
  if(!isArrayLike(path))
    path = path.split("."); 

  if(def == undefined)
    def = null;

  if(!i)
    i = 0;

  if(i >= path.length)
    return x;

  // If array, try the first element
  if(x[0] != undefined) {
    x = x[0];
  }

  var o = x[path[i]];  
  if(o != undefined) {
    return resolve(o,path,def,i+1);
  }
  else
    return def;
}

function empty(x) {
  return (x==null) || (x=='');
}

/**
 * Return the absolute horizontal position of the given object within the page
 */
function findPosX(obj) {
  var curleft = 0;
  if (obj.offsetParent) {
    while (obj.offsetParent) {
      curleft += obj.offsetLeft
      obj = obj.offsetParent;
    }
  }
  else if (obj.x)
    curleft += obj.x;
  return curleft;
}

/**
 * Return the absolute vertical position of the given object within the page
 */
function findPosY(obj) {
  var curtop = 0;
  if (obj.offsetParent) {
    while (obj.offsetParent) {
      curtop += obj.offsetTop
      obj = obj.offsetParent;
    }
  }
  else if (obj.y)
    curtop += obj.y;
  return curtop;
}

/**
 * Utility function to prevent default handling of an event.
 * Supports IE and DOM Level 2 clients (Moz/FF)
 */
function cancelEventBubble(evtToCancel) {
  // We've handled this event.  Don't let anybody else see it.
  if (evtToCancel.stopPropagation)
    evtToCancel.stopPropagation(); // DOM Level 2
  else
     evtToCancel.cancelBubble = true; // IE
     
  if (evtToCancel.preventDefault)
    evtToCancel.preventDefault(); // DOM Level 2
  else
     evtToCancel.returnValue = false; // IE
}

function funcname(f) {
  var fmatch = f.toString().match(/function (\w*)/);
  if(fmatch && (fmatch.length > 0)) {
    var s = fmatch[1];
    if ((s == null) || (s.length==0)) return "anonymous";
    return s;
  }
  else {
    return "anonymous";
  }
}

function stacktrace() {
 var re = /function\W+([\w-]+)/i;
 var f = arguments.callee;
 var s = "";
 while (f) {
  s += (re.exec(f))[1] + '('; 
  for (i = 0; i < f.arguments.length - 1; i++) {
   s += "'" + f.arguments[i] + "', ";
  }
  if (arguments.length > 0) {
   s += "'" + f.arguments[i] + "'";
  }
  s += ")\n\n";
  f = f.arguments.callee.caller;
 }
 return s;
}

/**
 * Utility function to set a cookie
 */
function setCookie(name, value, expires, path, domain, secure)
{
  document.cookie= name + "=" + escape(value) +
      ((expires) ? "; expires=" + expires.toGMTString() : "") +
      ((path) ? "; path=" + path : "") +
      ((domain) ? "; domain=" + domain : "") +
      ((secure) ? "; secure" : "");
}

/**
 * gets the value of the specified cookie.
 */
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

/**
 * Converts 0-255 int value to corresponding hex char pair
 */
function int2Hex(val) {
  var rem = val % 16;
  var highVal = (val - rem)/16;
  return hexChar(highVal)+''+hexChar(rem);
}

/**
 * Converts a single 0-15 range number from decimal to corresponding hex char
 */
function hexChar(val) {
  if(val==10)
   return 'a';
  else
  if(val==11)
   return 'b';
  else
  if(val==12)
   return 'c';
  else
  if(val==13)
   return 'd';
  else
  if(val==14)
   return 'e';
  else
  if(val==15)
   return 'f';
  else
    return parseInt(val);
}

/**
 * Converts a single hex character to its decimal equivalent (0-15)
 */
function hex2Int(hex) {
  if(hex=='a')
   return 10;
  if(hex=='b')
   return 11;
  if(hex=='c')
   return 12;
  if(hex=='d')
   return 13;
  if(hex=='e')
   return 14;
  if(hex=='f')
   return 15;
  return parseInt(hex);
}

var colorKeyItem = null;

/**
 * A debug/design function.  Adds event handler which 
 * allows dynamic adjustment of color on a particular item.
 */
function hotColor(obj) {
  colorKeyItem = obj;
  window.document.body.onkeydown=handleColorKey;
}

function handleColorKey(event) {

  if(!colorKeyItem) 
    return;

  if (!event) event= window.event; // IE

  var keyCode =
    document.layers ? event.which :
    document.all ? event.keyCode :
    document.getElementById ? event.keyCode : 0;

  var adj = colorKeyItem;
  var bgColor = getStyle(adj,'backgroundColor'); // fn from mochikit
  if(bgColor==null || bgColor=='') {
    bgColor='#6988bb';
  }
  var r = hex2Int(bgColor.charAt(1))*16 + hex2Int(bgColor.charAt(2));
  var g = hex2Int(bgColor.charAt(3))*16 + hex2Int(bgColor.charAt(4));
  var b = hex2Int(bgColor.charAt(5))*16 + hex2Int(bgColor.charAt(6));
  log(bgColor + ' r='+r + ' g=' + g + ' b=' + b + ' key='+keyCode);
  if(keyCode == 82) {
    r+=2;
    if(r>255)
      r=0;
    var newColor='#' + int2Hex(r)+int2Hex(g)+int2Hex(b);
    log('new color is ' + newColor);
    adj.style.backgroundColor=newColor;
  }
  else
  if(keyCode == 71) {
    g+=2;
    if(g>255)
      g=0;
    var newColor='#' + int2Hex(r)+int2Hex(g)+int2Hex(b);
    log('new color is ' + newColor);
    adj.style.backgroundColor=newColor;
  }
  else
  if(keyCode == 66) {
    b+=2;
    if(b>255)
      b=0;
    var newColor='#' + int2Hex(r)+int2Hex(g)+int2Hex(b);
    log('new color is ' + newColor);
    adj.style.backgroundColor=newColor;
  }
}

/**
 * Removes white spaces from beginning and end of string value
 */
function trim(aValue) {
    return aValue.replace(/^\s+/g, "").replace(/\s+$/g, "");
}

function removeSpaces(aValue) {
    return aValue.replace(/\s/g, "");
}

/**
 * Trunc a value to specified maximum number of characters, appending an
 * ellipsis when truncated.
 */
function trunc(value, len) {
  if(value.length <= len)
    return value;

  return value.substring(0,len-3) + "...";  
}

/**
 * The currently active drag handler
 */
var currentDragHandler;

/**
 * Support for handling dragging
 */
function DragHandler(e,doc) {

    log("Starting drag handler");

    if(!doc) {
      doc = window.document;
    }

    currentDragHandler = this;

    // Register the event handlers that will respond to the mousemove events
    // and the mouseup event that follow this mousedown event.  
    if (doc.addEventListener) {  // DOM Level 2 Event Model
      // Register capturing event handlers
      doc.addEventListener("mousemove", DragMoveHandler, true);
      doc.addEventListener("mouseup", DragUpHandler, true);
    }
    else if (doc.attachEvent) {  // IE 5+ Event Model
      // In the IE Event model, we can't capture events, so these handlers
      // are triggered when only if the event bubbles up to them.
      // This assumes that there aren't any intervening elements that
      // handle the events and stop them from bubbling.
      doc.attachEvent("onmousemove", DragMoveHandler);
      doc.attachEvent("onmouseup", DragUpHandler);
    }

    if(e) {
      cancelEventBubble(e);
    }
}

function DragMoveHandler(e) {
  if (!e) e = window.event;  // IE event model

  currentDragHandler.handleMove(e);

  cancelEventBubble(e);
}

function DragUpHandler(e) {
  if (!e) 
    e = window.event;  // IE event model

  // Unregister the capturing event handlers.
  if (document.removeEventListener) {    // DOM Event Model
      document.removeEventListener("mouseup", DragUpHandler, true);
      document.removeEventListener("mousemove", DragMoveHandler, true);
  }
  else if (document.detachEvent) {       // IE 5+ Event Model
      document.detachEvent("onmouseup", DragUpHandler);
      document.detachEvent("onmousemove", DragMoveHandler);
  }

  currentDragHandler.handleUp(e);
  cancelEventBubble(e);
}

/**
 * Default handlers for move/up events
 */
DragHandler.prototype.handleUp=function(e) {};

DragHandler.prototype.handleMove=function(e) {};

/**
 * CSS Helpers
 */
function getRule(sheet,selector) {
  for(j=0; j<document.styleSheets.length;j++) {
    var ss = document.styleSheets[j];
    //log("found stylesheet " + ss.href); 
    if(ss.href.lastIndexOf(sheet)==(ss.href.length - sheet.length)) {
      var rules = ss.rules || ss.cssRules;
      for(i=rules.length-1; i>=0; i--) {
        //log("found rule " + rules[i].selectorText);
        if(rules[i].selectorText==selector) {
          return rules[i];
        }
      }
      log("WARN: Exhausted rules search");
      break;
    }
  }
}


/**
 * Creates an alert box containing the properties of any JavaScript object.
*/
function dumpProperties(description, obj){
  dump(description,obj);
}

function dump(description, obj){
	if (obj == null)
    obj = description;

  var display = description;
  display+= "\n";
  for (var name in obj) {
      try {
	      display +=name;
	      display += ":";
	      display += obj[name];
	      display += "\n";
      }
      catch(e) {
          display += "--- Error displaying attribute " + name;
      }
  }
  display+="\n";
  display+="at\n\n";
  display+=stacktrace();

  alert(display);
}

function formatSize(s) {
  if(s<1000)
    return s + ' bytes';
  else
  if(s<1024*1024) { 
    return Math.round(s / 1024) + ' KB';
  }
  else
    return Math.round(s/1024/1024) + ' MB';
}

function formatDateOfBirth(date){
	var formattedDate = (date.getMonth() + 1) + "/" + date.getDate() + "/" + (date.getFullYear());
	return(formattedDate);
}

function formatLocalDateTime(date){
	var formattedDate = formatDateOfBirth(date);
	formattedDate += " ";
  
  // note: toLocaleTimeString() includes seconds, but for compact
  // display we don't want that.
	//formattedDate += date.toLocaleTimeString();
  var twodigits = numberFormatter('00');
  formattedDate += twodigits(date.getHours()) + ':' + twodigits(date.getMinutes());
  if(date.getHours()<12)
    formattedDate += ' AM';
  else
    formattedDate += ' PM';

	return(formattedDate);
}

function prettyTrack(tn) {
  if(tn.length < 12)
    return "????????????"; // Invalid tracking number!

  return tn.substr(0,4) + ' ' + tn.substr(4,4) + ' ' + tn.substr(8,4); 
}

function prettyAcctId(accId) {
  if(accId.length < 16)
    return "????????????????"; // Invalid account id

  return accId.substr(0,4) + ' ' + accId.substr(4,4) + ' ' + accId.substr(8,4) + ' ' + accId.substr(12,4); 
}

function genericErrorHandler(e) {
  alert("An error occurred while performing last operation.\r\n\r\n"
   + "Error: " + e.message + "\r\n\r\n"
   + "Code: " + e.number + "\r\n\r\n"
   + "Try the operation again or contact support for help.");
  window.lastError = e;
}

function XHRErrorHandler(e) {
  if(e.number) {
    alert("An error occurred while performing last operation.\r\n\r\n"
     + "Error: " + e.message + "\r\n\r\n"
     + "Code: " + e.number + "\r\n\r\n"
     + "Try the operation again or contact support for help.");
  }
  else {
    log("XMLHttpRequest failed without error code. Aborted?");
  }
  window.lastError = e;
}

var modalMatch=/(^a$)|(input)|(textarea)/i;

function unmodal() {
  while(modalDisabled.length>0) {
    modalDisabled.pop().disabled = false;
  }
}

var modalDisabled = new Array();
function modal(x) {
  nodeWalk(document.body, function(n) {
    if(n.tagName && n.tagName.match(modalMatch)) {
      n.disabled=true;
      modalDisabled.push(n);
    }
    return n.childNodes;
  });
  nodeWalk(x, function(n) {
   if(n.tagName && n.tagName.match(modalMatch))
     n.disabled=false;
    return n.childNodes;
  });
}

var isFireFox = (navigator.userAgent.indexOf("Firefox")!=-1)
var isIE = (typeof document.all != undefined);

/******************* Support for JSON AJAX Calls via MochiKit *********************/

/**
 * MochiKit supports JSON calls itself, but a) it uses GET which is prone
 * to failure if the parameters overflow max url length and b) it doesn't 
 * interop with PHP JSON quite properly.  So this function adapts it
 * to work with around these problems.
 */
function execJSONRequest(url, postdata, success) {
  var  req = getXMLHttpRequest();

  if((postdata != null) && (typeof(postdata) != "string")) 
    postdata = queryString(postdata);

  if(!postdata) 
    postdata = "sid="+getCookie("JSESSIONID");
  else
    postdata += "&sid="+getCookie("JSESSIONID");

  log("Opening request " + url);
  req.open("POST", url, true);
  log("Opened post request");
  log("sending with post data: " + postdata);
  req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');

  sendXMLHttpRequest(req,postdata).addCallbacks( function(req) {
    try {
      log("Received response text: " + req.responseText);
      var js = null;
      if(req.responseText.match(/^{.*}$/g))
        js = eval('var x = '+req.responseText+'; x;');
      else
        js = eval(req.responseText);

      success(js);
    }
    catch(e) {
      dumpProperties("An error occurred while executing the last operation",e);
      log("Error while evaluating returned result: " + req.responseText);
      return;
    }
  }, genericErrorHandler);
}

/**
 * Adjusts the height of the given text area so that it doesn't show a scroll bar, up to the 
 * given maximum
 */
function adjustTextAreaHeight(t,max) {
  var h = "0";
  if(max) {
    h = t.scrollHeight<max?t.scrollHeight:max;
  }
  else
    h = t.scrollHeight;

  t.style.height = h + "px";
}

if(typeof forEach == 'undefined') {
  window.forEach = function(arr,f) {
    for(var i = 0; i<arr.length; ++i) {
      f(arr[i]);
    }
  }
}


/******************* Support for Cookie Events *********************/

var ce_events = new Object();
ce_events.id = Math.floor(Math.random()*100000);

var ce_last_event_src = null;

var ce_timer = null;

var ce_src_id = (new Date().getTime() + "" + Math.floor(Math.random() * 100000)).substring(6);

var ce_known_windows = new Array();

var MAX_CE_AGE = 4000;

var ce_begin_time = (new Date()).getTime();

var ce_is_loaded = false;

/**
 * List of all ce_events received
 */
var ce_history = new Array();

/**
 * Connect given event e to function o.f or just f (note, f can be 2nd arg)
 */
function ce_connect( e, o, f ) {

  if(!ce_events[e])
    ce_events[e] = [];

  ce_events[e].push( { obj: f ? o : null, fn: f ? f : o } );

  ce_init();
}

/**
 * Connect to specific window
 */
/*
function ce_connect_src( src, e, o, f ) {
  ce_init();
  connect( ce_known_windows[src], e, o, f );
}
*/

function ce_init_timer() {
  if(ce_timer == null) {
    ce_known_windows[ce_src_id] = [];
    ce_timer = setInterval(ce_poll,1000);
  }
}

function ce_init() {
  if(ce_is_loaded) {
    //log("ce_is_loaded true: initializing time");
    ce_init_timer();
  }
  else {
    //log("ce_is_loaded false: waiting for load");
    addLoadEvent(ce_init_timer);
  }
}

function ce_loaded() {
  //log("setting ce_loaded to true");
  ce_is_loaded = true;
}

if(window.addLoadEvent) {
  //log("adding ce_loaded event");
  addLoadEvent(ce_loaded);
}

var ce_old_ce = getCookie('ce');
if(!ce_old_ce) {
  ce_old_ce = '';
}

/**
 * Check if the given cookie event is one we already processed or if it's
 * too old to be interesting. 
 */
function is_old_ce(ce) {
  var t = parseInt(ce,10);
  var now = (new Date()).getTime();
  if(t < (now - MAX_CE_AGE)) {
    return true;
  }

  if(t < ce_begin_time) {
    return true;
  }

  // Is it in our history array?
  for(var i =0; i<ce_history.length; ++i) {
    if(ce_history[i] == ce)
      return true;
  }
  return false;
}

function ce_add_history(ce) {
  ce_history.push(ce);
  var maxAge = (new Date()).getTime() - MAX_CE_AGE;
  var i =0;
  while((i<ce_history.length) && (parseInt(ce_history[i],10)<maxAge)) {
    ++i;
  }
  if(i>0) {
    ce_history.splice(0,i);
  }
}

function ce_poll() {
  var current_ce = getCookie('ce');
  if((current_ce == null) || (current_ce == ce_old_ce)) { // no change since last poll
    return;
  }

  ce_old_ce = current_ce;

  // Something changed - check the cookie events in the cookie
  //log("examining new current_ce " + current_ce);
  var ces = current_ce != null ? current_ce.split(/;/) : new Array();
  forEach(ces, function(ce) {
    if(is_old_ce(ce)) {
      return;
    }

    // TODO: trim the ce_history array to prevent mem leaks
    ce_add_history(ce);

    // Parse the instruction
    var x = ce.split(/:/);
    var sig = x[0].split(/,/);
    var t = sig[0];
    ce_last_event_src=null;
    if((sig.length>1) && (sig[1]!=undefined)) {
      ce_last_event_src = sig[1];
      if(ce_known_windows[ce_last_event_src] == undefined) {
        ce_known_windows[ce_last_event_src] = [];
      }
    }

    if(ce_src_id != ce_last_event_src) {
      log('['+window.name+'] Received event ' + x[1] + ' at time ' + t + " from src: " + ce_last_event_src);
    }
    
    // Check for arguments
    var args = [];
    if(x[1]) {
      var encArgs = x[1].split(',');
      for(var i=0; i<encArgs.length; ++i) {
        args.push(unescape(encArgs[i]));
      }
    }

    // args.unshift(ce_events);
    //log("sending signal(" + args.join(",")+")");
    // signal.apply(this,args);

    // Find subscribers for this signal
    ce_notify(ce_events[x[1]],args);

    // If src provided, signal listeners subscribed to that src
    if(ce_last_event_src) {
      args.shift();
      ce_notify(ce_known_windows[ce_last_event_src],args);
      //args.unshift(ce_known_windows[ce_last_event_src]);
      // signal.apply(this,args);
    }
  });
}

function ce_notify(subscribers, args) {
  if(subscribers) {
    forEach(subscribers,function(s) {
      if(s.obj) {
        alert('not supported');
      }
      s.fn.apply(this,args);
    });
  }
}

/**
 * Sends a cookie event to all listeners by setting the ce cookie.
 *
 * The cookie is formatted in the following way which facilitates
 * multiplexing multiple events into the same cookie:
 *
 *   <time1>,<src1>:<event name>,<arg1>,<arg2>,...;<time2>,<src1>:<event name>,<arg1>,<arg2>,...
 */
function ce_signal( e ) {

  // Before signalling, check if there is any contention on the cookie channel
  ce_poll();

  // IE built in arguments array does not support join
  var x = new Array();
  for(var i=0; i<arguments.length; ++i) {
    x.push(encodeURIComponent(arguments[i]));
  }

  var current_ce = getCookie('ce');
  var ces = current_ce != null ? current_ce.split(/;/) : new Array();
  var now = (new Date()).getTime();
  var new_ces = new Array();
  forEach(ces, function(ce) {
    var t = parseInt(ce);
    if(t > (now - MAX_CE_AGE)) {
      new_ces.push(ce);
    }
  });
  new_ces.push(now + ',' + ce_src_id + ':' + x.join(','));
  var new_ce = new_ces.join(';');
  
  // Make sure we can't possibly overflow the cookie allowance of 4k
  while(new_ce.length > 1500) {
    var n = new_ce.indexOf(";");
    if(n>=0) {
      new_ce = new_ce.substring(n+1);
      log("WARN: ce overflow, abandoned " + n + " characters");
    }
    else
      break;
  }
 
  // Make sure we don't receive our own signal
  ce_old_ce = new_ce;

  log("adding signal to cookie channel, length="+new_ce.length);
  setCookie('ce', new_ce, null, '/');

  forEach(ce_servers, function(url) {
    var ceUrl = url + ((url.indexOf('?')==-1) ? '?' : '&') + 'e=' + encodeURIComponent(new_ce);
    log("sending signal to server: " + ceUrl);
    loadScript(ceUrl);
  });
}

var ce_servers = new Array();
function ce_add_server(url) {
  for(var i =0; i<ce_servers.length; ++i) {
    if(ce_servers[i] == url)
      return;
  }

  // Not found
  ce_servers.push(url);
}

function loadScript(src) {
    var head = document.getElementsByTagName("head").item(0);
    var script = document.createElement("script");
    script.setAttribute("type", "text/javascript");
    script.setAttribute("src", src);
    head.appendChild(script);
}


/************ Managing IFrame Heights ****************/

var broadCasting = false;

/**
 * Add a frame height monitor to monitor current window, presumed to be an iframe
 */
function addHeightMonitor() {
  //connect( document.body, 'onresize', broadCastHeight);
  log("Adding height monitor in window " + window.name);
  document.body.onresize = broadCastHeight;
  broadCastHeight();
  broadCasting = true;
}

function _calculateHeight() {
  if (document.body.scrollHeight > document.body.offsetHeight){ // all but Explorer Mac
    h = document.body.scrollHeight;
  } 
  else { // works in Explorer 6 Strict, Mozilla (not FF) and Safari
    h = document.body.offsetHeight;
  }

  /*
   Causes incorrect sizes in patient info widget
  if(h<document.body.parentNode.offsetHeight)
    h = document.body.parentNode.offsetHeight;

  */
  return h;
}

var bc_last_height = 0;
function broadCastHeight() {
  var h = 0;
  if(window.calculateHeight) { // custom defined?
    h = calculateHeight();
  }
  else { // use default
    h = _calculateHeight();
  }

  if(h != bc_last_height) {
    log("Calculated window " + window.name + " has height " + h);
    ce_signal( 'windowResized', window.name, h);
    bc_last_height = h;
  }
}

function addHeightSync() {
  ce_connect('windowResized', adjustFrameHeight);
}

function adjustFrameHeight(n, h) {

  var f = null;
  forEach(document.getElementsByTagName('iframe'),  function(frm) {
    if(frm.name == n) {
      f = frm;
    }
  });

  if(!f) {
    log("size reported for unknown child frame " + n + " : ignoring.");
    return;
  }

  if(f.height != h) {
    log("sizing ["+n+"] to " + h);
    f.height = h;
    f.style.height = h + 'px';
  }

  signal(ce_events,'ce_resized',f,h);

  if(broadCasting) {
    broadCastHeight();
  }
}

function viewportSize() {
 if (typeof window.innerWidth != 'undefined') {
      w = window.innerWidth,
      h = window.innerHeight
 }
 // IE6 in standards compliant mode (i.e. with a valid doctype as the first line in the document)
 else if (typeof document.documentElement != 'undefined'
     && typeof document.documentElement.clientWidth !=
     'undefined' && document.documentElement.clientWidth != 0) {
       w = document.documentElement.clientWidth,
       h = document.documentElement.clientHeight
 }
 // older versions of IE
 else {
   w = document.getElementsByTagName('body')[0].clientWidth,
   h = document.getElementsByTagName('body')[0].clientHeight
 }
 return { w: w, h: h };
}

var yuiListeners = [];

function hasYUILoader() {
  return ((typeof YAHOO == "undefined") || !YAHOO.util || !YAHOO.util.YUILoader);
}

/**
 * Utility function for bootstrapping YUI framework
 */
function yuiLoader() {
    
    // If YUI loader not included on page, don't break
    if(!hasYUILoader && !window.YUI_initialized)
	    return;

    if(window.YUI_initialized) {
      return { insert: function(f) { f(); } };
    }

    // Instantiate and configure Loader:
    if(!window.loader) {
      window.loader = new YAHOO.util.YUILoader({

          // Identify the components you want to load.  Loader will automatically identify
          // any additional dependencies required for the specified components.
          require: ["connection", "utilities","button","container","autocomplete","tabview"],
          
          skin: {
              
              // Specifies the location of the skin relative to the build
              // directory for the skinnable component.
              base: 'assets/skins/',
       
              // The default skin, which is automatically applied if not
              // overriden by a component-specific skin definition.
              // Change this in to apply a different skin globally 
              defaultSkin: 'mc'
	      },

          // Configure loader to pull in optional dependencies.  For example, animation
          // is an optional dependency for slider.
          loadOptional: true,

          // The function to call when all script/css resources have been loaded
          onSuccess: function() {
                log('YUI load complete');
                if (!window.YUI_initialized) {
                    window.YUI_initialized = true;
                    log('YUI loaded.  Signalling ' + yuiListeners.length + ' waiting components.');
                    for(var i=0; i<yuiListeners.length; ++i) {
                      log('Signalling function ' + yuiListeners[i]);
                      yuiListeners[i].apply(window);
                    }
                }
          }
      }); 
      loader.insert({'base':'yui-2.8.0r4/'});
    }

    return {
      insert: function(f) {
        yuiListeners.push(f);
      }
    };
}

/**
 * Not provided by MochiKit
 */
function prependChildNodes() {
  var args = [];
  forEach(arguments, function(a) { args.push(a) }); // IE array is not real

  if(args[0].childNodes.length == 0) {
    appendChildNodes.apply(window,args);
  }
  else {
    var p = args.shift();
    args.unshift(p.childNodes[0]);
    insertSiblingNodesBefore.apply(window,args);
  }
}

/**
 * Place a dot in specified corners to 'soften' them.
 */
function soften(obj,corners) {
    if(!corners)
        corners = {tl:true,bl:true,br:true,tr:true};
    if(corners.br)
        appendChildNodes(obj, DIV({'class':'cornerDot', style: 'bottom: -1px; right: -1px;'}));
    if(corners.tr)
        prependChildNodes(obj, DIV({'class':'cornerDot', style: 'top: -1px; right: -1px;'}));
    if(corners.tl)
        prependChildNodes(obj, DIV({'class':'cornerDot', style: 'top: -1px; left: -1px;'}));
    if(corners.bl)
        appendChildNodes(obj, DIV({'class':'cornerDot', style: 'bottom: -1px; left: -1px;'}));
}

// Returns the operating system that the client 
// is running in. Useful for non-browser actions
// (such as deciding to launch HealthFrame).
function clientOS(){
	var agent = navigator.userAgent;
	if (agent.indexOf("Win") != -1)
		return("Windows");
	else if (agent.indexOf("Mac") != -1)
		return("Macintosh");
	else if (agent.indexOf("Linux") != -1)
		return("Linux");
	else 
		return("Unknown");
}

var _he = { 34:'quot', 38:'amp', 60:'lt', 62:'gt', 160:'nbsp', 161:'iexcl', 162:'cent', 163:'pound', 164:'curren', 165:'yen',
  166:'brvbar', 167:'sect', 168:'uml', 169:'copy', 170:'ordf', 171:'laquo', 172:'not', 173:'shy', 174:'reg', 175:'macr',
  176:'deg', 177:'plusmn', 178:'sup2', 179:'sup3', 180:'acute', 181:'micro', 182:'para', 183:'middot', 184:'cedil', 185:'sup1',
  186:'ordm', 187:'raquo', 188:'frac14', 189:'frac12', 190:'frac34', 191:'iquest', 192:'Agrave', 193:'Aacute', 194:'Acirc',
  195:'Atilde', 196:'Auml', 197:'Aring', 198:'AElig', 199:'Ccedil', 200:'Egrave', 201:'Eacute', 202:'Ecirc', 203:'Euml',
  204:'Igrave', 205:'Iacute', 206:'Icirc', 207:'Iuml', 208:'ETH', 209:'Ntilde', 210:'Ograve', 211:'Oacute', 212:'Ocirc', 213:'Otilde',
  214:'Ouml', 215:'times', 216:'Oslash', 217:'Ugrave', 218:'Uacute', 219:'Ucirc', 220:'Uuml', 221:'Yacute', 222:'THORN', 223:'szlig',
  224:'agrave', 225:'aacute', 226:'acirc', 227:'atilde', 228:'auml', 229:'aring', 230:'aelig', 231:'ccedil', 232:'egrave', 233:'eacute',
  234:'ecirc', 235:'euml', 236:'igrave', 237:'iacute', 238:'icirc', 239:'iuml', 240:'eth', 241:'ntilde', 242:'ograve', 243:'oacute',
  244:'ocirc', 245:'otilde', 246:'ouml', 247:'divide', 248:'oslash', 249:'ugrave', 250:'uacute', 251:'ucirc', 252:'uuml',
  253:'yacute', 254:'thorn', 255:'yuml' };

function htmlentities(s){
    var code = 0, tmp_arr = [];    
    for (i = 0; i < s.length; ++i) {
        code = s.charCodeAt(i);
        if (code in _he) {
            tmp_arr[i] = '&'+_he[code]+';';
        } else {
            tmp_arr[i] = s.charAt(i);
        }
    }
    return tmp_arr.join('');
}

if(typeof addLoadEvent == 'undefined') {
  window.addLoadEvent = function(f) {
     var previousOnload = window.onload;
     window.onload = function() {
       if(previousOnload)
         previousOnload();
       f();
     };
  }
}

if(typeof $ == undefined) {
  window.$ = function(id) {
    return document.getElementById(id);
  }
}

/**
 * Simple "lightbox" effect with no dependencies on
 * YUI (usese MochiKit)
 */
function lightbox(body,dim) {
    var underlay = DIV({ 
        id:  'underlay',
        style: 'position: absolute; top: -10%; left: -10%; width: 120%; height: 120%; background-color: black; z-index: 100000; display: none;'});
    
    document.body.insertBefore(underlay,document.body.childNodes[0] );
    
    hide(body);
    appendChildNodes(document.body,body);
    
    
    body.style.position = 'absolute';
    
    var totalDim = getViewportDimensions();
    var bodyDim = dim ? dim : getElementDimensions(body);
    
    body.style.top = ((totalDim.h - bodyDim.h) / 2) + 'px';
    body.style.left = ((totalDim.w - bodyDim.w) / 2) + 'px';
    body.style.zIndex = 100001;
    
    setOpacity('underlay',0.3);
    show('underlay');
    appear(body, {duration: 0.3});
}

/**
 * Sets window.location.href while observing 
 * the value in the <base> tag (problem with IE).
 */
function load(loc) {
    var b = document.getElementsByTagName('base');
    if (b && b[0] && b[0].href) {
      if (b[0].href.substr(b[0].href.length-1) == '/' && loc.charAt(0) == '/')
        loc = loc.substr(1);
      loc = b[0].href + loc;
    }
    location.href = loc;
}

/**
 * Idle timeout after a set time
 *
 * Usage:   setIdleTimeout(function() {...});
 */
(function() {
  var MAX_IDLE_TIME = 60000 * 15; // 15 minutes
  var idleSince = (new Date().getTime());
  var timeoutHandler = null;

  function onActivity() {
    idleSince = (new Date().getTime());
  }
  function checkTimeout() {
    var now = (new Date().getTime());
    if(now - idleSince > MAX_IDLE_TIME) {
      if(timeoutHandler) 
        timeoutHandler();
    }
    document.getElementById('info').innerHTML = now - idleSince ;
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
    listen("mousemove", onActivity);
    listen("keydown", onActivity);
    setInterval(checkTimeout, 1000);
  }
})();
