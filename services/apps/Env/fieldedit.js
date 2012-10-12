/**
 * MedCommons Simtrak Field Editor
 *
 * Copyright 2008, MedCommons Inc.
 *
 * Author:  Simon Sadedin <ssadedin@medcommons.net>, MedCommons Inc.
 */ 

/******************************* Utility Functions *******************************/

/**
 * log - useful to send log statements to external logger
 * (sends to Badboy / Firefox console)
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
 var s = "";
 for (var a = arguments.caller; a !=null; a = a.caller) {
   s += "->"+funcname(a.callee) + "\n";
   if (a.caller == a) {s+="*"; break;}
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
      display +=name;
      display += ":";
      display += obj[name];
      display += "\n";
  }
  display+="\n";
  display+="at\n\n";
  display+=stacktrace();

  alert(display);
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
    ce_known_windows[ce_src_id] = new Object();
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

/**
 * Utility function for bootstrapping YUI framework
 */
function yuiLoader() {
    
    // If YUI loader not included on page, don't break
    if((typeof YAHOO == "undefined") || !YAHOO.util || !YAHOO.util.YUILoader)
      return;

    if(window.YUI_initialized) {
      return { insert: function(f) { f(); } };
    }

    // Instantiate and configure Loader:
    if(!window.loader) {
      window.loader = new YAHOO.util.YUILoader({

          // Identify the components you want to load.  Loader will automatically identify
          // any additional dependencies required for the specified components.
          require: ["connection", "utilities","button","container","autocomplete"],

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
      loader.insert({'base':'yui-2.6.0/'});
    }

    return {
      insert: function(f) {
        yuiListeners.push(f);
      }
    };
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

/**
 * Commonly used shortcut for getting elements by id
 */
if(typeof $ == undefined) {
  window.$ = function(id) {
    return document.getElementById(id);
  }
}

/******************************* Editor *******************************/

/**
 * Initialization - adds editor icons to the editable fields
 * on the page.
 */
(function() 
{
  
  var dom = YAHOO.util.Dom;
  var el = function(e,a) { 
     var e = new YAHOO.util.Element(e,a); 
     YAHOO.lang.augmentObject(e.DOM_EVENTS,{change:true,focus:true,blur:true}); 
     return e;
  };
  var inputEx = YAHOO.inputEx;

  /**
   * Create an anchor ("a") element with specified image buried inside.
   * @src   src of image
   * @atts  attributes to add to link
   */
  function imgLink(src,atts) {
    if(!atts)
      atts = {};
    YAHOO.lang.augmentObject(atts,{href:'#'});
    var a = el(document.createElement('a'), atts);
    var img = el(document.createElement('img'), {src: src, border: '0'});
    img.appendTo(a);
    return a;
  }

  /**
   * Check if the given input has a validate method and if so whether it's
   * value is valid.
   */
  function isValid(inp) {
    if(inp.field) {
      if(inp.field.validate)
        return inp.field.validate();
    }
    if(inp.validate) {
      return inp.validate();
    }
    return true; // no validation method == always valid
  }

  /**
   * Gathers the modified fields from the specified pane and 
   * submits them to server to save them.
   */
  function savePane(evt,pane) {
    try {
      var args = [];
      var invalid = [];
      forEach(pane.getElementsByClassName('modified'), function(inp) {
          if(!isValid(inp))
            invalid.push(inp);
          args.push(encodeURIComponent(inp.id)+"="+encodeURIComponent(inp.value));
      });

      if(invalid.length != 0) {
        alert('One or more fields are not valid.\n\nPlease check the highlighted entries and try again.');
        return false;
      }

      var qs = args.join('&');
      log("sending data " + qs);
      YAHOO.util.Connect.asyncRequest('POST', 'updatesimtrak.php?recordid='+recordId+'&patientid='+playerId,
          {
            success: function(r) {
              //alert(r.responseText);
              forEach(pane.getElementsByClassName('modified'), function(inp) {
                el(inp).removeClass('modified');
              });
              pane.get('element').modified = false;
              forEach(pane.getElementsByClassName('paneSaveImg'),function(n) { el(n.parentNode).removeChild(n); });
            },
            failure: function(r) {
              var result = YAHOO.lang.JSON.parse(r.responseText);
              var msg = result ? result.error : "Unknown Error";
              alert('A problem occurred while updating the fields you modified.\r\n\r\n'+
                    '  '+msg+'\r\n\r\n' +
                    'Please try again or contact support for help.');
            }
          },qs);

      return true;
    }
    catch(e) {
      dump("Failure while saving",e);
    }
  }

  /**
   * Handles onchange events on fields.  Marks the fields
   * as modified by coloring them and adds a 'save' icon
   * to the page.
   */
  function fieldChanged(evt,inp) {
    try {
      log("field changed");
      inp.addClass('modified');
      // Add save icon in the rh corner
      var pane = el(dom.getAncestorByClassName(inp.get('element'),'pane'));
      if(!pane.get('element').modified) {
        var a = imgLink('images/save.png',{className:'paneSaveImg',title:'This pane has been modified: click to save.'});
        pane.appendChild(a);
        a.on('click',savePane,pane);
        pane.get('element').modified = true;
      }
    }
    catch(e) {
      dump('failed to handle field change:',e);
    }
  }

  /**
   * Check if any tabs have modified content and warn the user
   * before they leave the page.
   */
  function checkUnsaved() { 
    var panes = tabView.getElementsByClassName('pane');
    var unsaved = false;
    forEach(panes, function(p) { if(p.modified) { unsaved = true; } });
    if(unsaved) {
      return "One or more panes has unsaved changes.\r\n\r\nIf you leave the page now your changes will be lost.";
    }
  };

  function checkPaneModified() {
    var tab = tabView.get('activeTab');
    var panes = el(tabView.get('activeTab').get('contentEl')).getElementsByClassName('pane');
    for(var i =0; i<panes.length; ++i) {
      if(panes[i].modified) {
        return confirm('You have made one or more changes on this page.\r\n\r\nIf you switch to another page you will need to remember to come back and save them here.'+
              'Click OK to continue or Cancel to abort.')
      }
    }
    return true;
  }

  /**
   * Replace the given text field with the given inputEx enhanced field.
   */
  function replaceInputWithField(inp,f) {
    var value = inp.value;
    inp.parentNode.replaceChild(f.getEl(),inp);
    inp = f.getEl().getElementsByTagName('input')[0];
    f.setValue(value);
    inp.field = f;
    return inp;
  }

  function instrumentDate(inp) {
    return replaceInputWithField(inp,new inputEx.DateField({dateFormat: 'm/d/Y',showMsg: true, typeInvite:'m/d/Y'}));
  }

  function replaceInputWithChoice(inp,choice) {
    var sel = new inputEx.SelectField({selectValues:choice});
    inp.parentNode.replaceChild(sel.getEl(),inp);
    sel.setValue(inp.value);
    return sel.getEl().getElementsByTagName('select')[0];
  }

  function instrumentGender(inp) {
    return replaceInputWithChoice(inp,['Male','Female']);
  }

  function instrumentEmail(inp) {
    return replaceInputWithField(inp,  new inputEx.EmailField({showMsg: true}));
  }

  function instrumentPhone(inp) {
    // A hack - prevent invalid fields being flagged for blank values
    // really need to train the regex field to accept them, since simtrak does
    if(inp.value.match(/^[^0-9]*$/))
      inp.value = ''; 

    return replaceInputWithField(inp,new inputEx.StringField({showMsg: true, 
          regexp: /^((\+\d{1,3}(-| )?\(?\d\)?(-| )?\d{1,5})|(\(?\d{2,6}\)?))(-| )?(\d{3,4})(-| )?(\d{4})((\/| x| ext)(\d{1,5}){0,1})?$/ })); 
  }

  /**
   * Introspect the given field and enhance it based on certain rules of 
   * thumb, mainly based on the id of the field.
   */
  function instrumentInput(inp) {
    var id = inp.id;
    if(inp.id.match(/DATE/)) {
      inp = instrumentDate(inp);
    }
    else
    if(inp.id.match(/EMAIL/)) {
      inp = instrumentEmail(inp);
    }
    if(inp.id.match(/GENDER/)) {
      inp = instrumentGender(inp);
    }
    else
    if(inp.id.match(/ZIP$/)) {
      inp = replaceInputWithField(inp,new inputEx.StringField({showMsg: true, regexp: /^\d{5}([\-]\d{4})?$/})); 
    }
    else
    if(inp.id.match(/SOCSEC$/)) {
      inp = replaceInputWithField(inp,new inputEx.StringField({showMsg: true, regexp: /^(?!000)([0-6]\d{2}|7([0-6]\d|7[012]))([ -]?)(?!00)\d\d\3(?!0000)\d{4}$/})); 
    }
    else
    if(inp.id.match(/CELL$/) || inp.id.match(/FAX$/) || inp.id.match(/PAGER$/) || inp.id.match(/PHONE$/)) {
      inp = instrumentPhone(inp);
    }
    else
    if(inp.id.match(/A_DOMINANCE$/) || (inp.id == 'A_BATS') || (inp.id=='A_SHOOTS') || (inp.id=='A_KICKS') || (inp.id == 'A_THROWS')) {
      inp = replaceInputWithChoice(inp, ['Right','Left','']);
    }
    else
    if(inp.id == 'A_STATUS') {
      inp = replaceInputWithChoice(inp, ['Active','Inactive','Former']);
    }
    else {
      i = el(inp);
      i.set('disabled',false);
      i.get('element').removeAttribute('readonly');
      i.removeClass('invisible');
    }
    // Make sure that whatever happens in the instrumentation we still
    // have the right id on the end object
    inp.id = id;
    el(inp).on('change',fieldChanged,el(inp));
  }

  var initialized = false;

  window.simtrakEditor = 
  {
    /**
     * Initialize the editor - walk the DOM looking for suitable nodes
     * to augment with editor behavior.
     */
    init: function() {

      // Copy the array because in some browsers it gets modified 'live'
      // as nodes get inserted / removed

      var tabs = tabView.get('tabs');
      for(var i=0; i<tabs.length; ++i) {
        var t = tabs[i];
        var p = t.get('contentEl');
        if(!p || p.editorInitialized) 
          continue;

        p.editorInitialized = true;

        var inps = [];
        forEach(p.getElementsByTagName('input'),function(inp) {inps.push(inp); });
        forEach(p.getElementsByTagName('textarea'),function(inp) {inps.push(inp); });
        forEach(inps, function(inp) {
            if(inp.className.match('valuef')) {
              instrumentInput(inp);
            }
        });

        window.setTimeout(window.simtrakEditor.init,300);
        break;
      }

      // Listen to the tab control for tab switch events
      // so we can intercept and prompt for save
      if(!initialized)
        tabView.addListener('beforeActiveTabChange', checkPaneModified); 

      window.onbeforeunload = checkUnsaved;
      initialized = true;


      // tabView.getElementsByTagName('ul')[0].parentNode.appendChild(imgLink('images/savem.png',{className:'saveAllIcon'}).get('element'));
    }
  }
})();
