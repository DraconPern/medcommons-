
function $(id) { return document.getElementById(id); }
function el(id) { return $(id); }
function showinline(id)
{
	if($(id))
		$(id).style.display = 'inline';
}
function showblock(id)
{
	if($(id))
		$(id).style.display = 'block';
}
function dontshow(id)
{
	if ($( id ))
	$( id ).style.display = 'none';
}
function toggle(id)
{
	if(!$(id)) 
		return true;
	if($(id).style.display == 'none')
		$(id).style.display = 'block'; 
	else
		$(id).style.display = 'none';
	return true;
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

function get_mc_cursor_attribute(att) {
  var mc = getCookie('mc_cursor');
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

function wsinit()
{
var groupName = '';
var patientName = '';
var logoutlink = '';
var email='';
var uploadlink = '<a href="/acct/uploadwww.php" title="Upload CD or DVD to MedCommons Help Desk"><span>Upload</span></a>';
var inboxlink = '<a href=/acct/index.php ><span>Sign On</span></a>';
var registerlink = '<a href=/acct/groupsetup.php ><span>Register</span></a>';
var healthurllink = '<a href=/acct/login.php ><span>Health Records</span></a>';
	if(getCookie('mc'))	{
		if (getCookie('mc_cursor'))
		{
		groupName = get_mc_cursor_attribute('groupName');
		
		patientName = get_mc_cursor_attribute('patientName');
		}
		else
		{groupName = 'no_cursor_cookie'; patientName=groupName;}
		
		
		registerlink = '<a href=/acct/psettings.php ><span>Settings</span></a>';
		uploadlink = 
			'<a href="/acct/uploadprivate.php" title="Upload CD or DVD to Current Inbox"><span>Upload to '+groupName+'</span></a>';
    	inboxlink = '<a href=/acct/index.php ><span>Inbox for '+groupName+'</span></a>';
		healthurllink = '<a '+
			'href="https://healthurl.medcommons.net/router/currentccr?a=1013062431111407&amp;aa=1117658438174637&g=3642665a324059089d0b989286eb9a9ced046e54&t=&amp;m=&amp;c=&amp;auth=c3faf3705e0e98c0984d1f86b96a789f3dba2638&at=c3faf3705e0e98c0984d1f86b96a789f3dba2638" '+
			'title="View All HealthURL Components"><span>Health Records for '+patientName+'</span></a>';
    	logoutlink = "&nbsp;|&nbsp; <a href='/acct/logout.php'>Sign Out</a>";
		 
	    email =get_mc_attribute ('email');
		signed_on = true;
		setIdleTimeout(function() {location.href='/acct/logout.php?next=%2Facct%2Flogin.php%3Fprompt%3Dexpiredsession';}); 
	}
	else {
		signed_on = false;
	}
	$('UserName').innerHTML = email; /*fn + ' ' + ln +'&nbsp;';*/
	$('LogoutLink').innerHTML = logoutlink;
	$('HealthURLLink').innerHTML = healthurllink;
	$('RegisterLink').innerHTML = registerlink;
	$('InboxLink').innerHTML = inboxlink;
	$('UploadLink').innerHTML = uploadlink;
}

(function() {
    var MAX_IDLE_TIME = 900 * 1000;
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