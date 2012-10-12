/**
 * Read the JavaScript cookies tutorial at:
 *   http://www.netspade.com/articles/javascript/cookies.xml
 */

/**
 * Sets a Cookie with the given name and value.
 *
 * name       Name of the cookie
 * value      Value of the cookie
 * [expires]  Expiration date of the cookie (default: end of current session)
 * [path]     Path where the cookie is valid (default: path of calling document)
 * [domain]   Domain where the cookie is valid
 *              (default: domain of calling document)
 * [secure]   Boolean value indicating if the cookie transmission requires a
 *              secure transmission
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
 * Gets the value of the specified cookie.
 *
 * name  Name of the desired cookie.
 *
 * Returns a string containing value of specified cookie,
 *   or null if cookie does not exist.
 */
function getCookie(name)
{
    var dc = document.cookie;
    var prefix = name + "=";
    var begin = dc.indexOf("; " + prefix);
    if (begin == -1)
    {
        begin = dc.indexOf(prefix);
        if (begin != 0) return null;
    }
    else
    {
        begin += 2;
    }
    var end = document.cookie.indexOf(";", begin);
    if (end == -1)
    {
        end = dc.length;
    }
    return unescape(dc.substring(begin + prefix.length, end));
}

/**
 * Deletes the specified cookie.
 *
 * name      name of the cookie
 * [path]    path of the cookie (must be same as path used to create cookie)
 * [domain]  domain of the cookie (must be same as domain used to create cookie)
 */
function deleteCookie(name, path, domain)
{
    if (getCookie(name))
    {
        document.cookie = name + "=" + 
            ((path) ? "; path=" + path : "") +
            ((domain) ? "; domain=" + domain : "") +
            "; expires=Thu, 01-Jan-70 00:00:01 GMT";
    }
}

function setUsername() {
  if(getCookie('loginName') != null) {
    if(document.getElementById('username')) {
      document.getElementById('username').innerHTML=getCookie('loginName');
    }
    if(document.getElementById('username2')) {
      document.getElementById('username2').innerHTML=getCookie('loginName');
    }
  }
}

function loggedInAs() {
  if(getCookie('loginName') != null) {
    if(document.getElementById('username')) {
      document.getElementById('username').innerHTML='logged in as <b>' + getCookie('loginName') + '</b> &nbsp;';
    }
    if(document.getElementById('username2')) {
      document.getElementById('username2').innerHTML='logged in as <b>' + getCookie('loginName') + '</b>';
    }
    if(document.getElementById('logoutlink') != null) {
      document.getElementById('logoutlink').style['display']='inline';
    }
  }
}

function trackMessage() {
  if(getCookie('mctrackguid')) {
    if(document.getElementById('tracknumber')) {
      //document.getElementById('trackmessage').innerHTML=
      //  'The following Study is available for your review: <a href="javascript:doWado(' 
      //      + "'" + getCookie('mctrackguid') + "'" + ')">#' + getCookie('mctrack') + '</a><br/><br/>';
      document.getElementById('tracknumber').value=getCookie('mctrack');
    }
    if(getCookie('loginName') == null) {
      document.loginForm.email.value='mcommons@hospital.org';
      document.loginForm.password.value='password';
    }
    else {
      document.loginForm.email.value=getCookie('loginName');
      document.loginForm.password.value='password';
    }
  }
}


function doWado(guid) {
  if(getCookie('mctrack')==null) {
    alert("Please specify a tracking number.");
    return false;
  }
  doWadoUrl('/router/WADOViewer.jsp?guid=' 
    + guid + '&name=John+Smith&tracking='
    + getCookie('mctrack')
    + '&address=123%20Lucky%20St&state=MT&city=Butte&zip=83132&cardnumber=7817574478133225'
    + '&amount=150.00&tax=12.00&charge=162.00&history=%3Cunknown%3E&copyto=agropper@medcommons.org&expiration=12/09');
}

function doWadoUrl(url) {
  if(getCookie('loginName') == null) {
    setCookie('loginName', 'mcommons@hospital.org');
  }
  if(getCookie('mctrackguid')!=null) {
   deleteCookie('mctrackguid');
  }
  wado = window.open(url,'wadowindow','directories=0,toolbar=1,resizable=1,menubar=1,location=1,status=1,scrollbars=1,width='+(screen.width+1) + ',height='+screen.height +',fullscreen=0');
  wado.moveTo(-3,0);
}

function doTrack(guid,track) {
  setCookie('mctrackguid',guid);
  setCookie('mctrack',track);
  mcwindow = window.open('/router/index.jsp','mcwindow','directories=1,toolbar=1,resizable=1,menubar=1,location=1,status=1,scrollbars=1,width=750,height=800'); 
  // mcwindow.moveTo(-6,0);
}

