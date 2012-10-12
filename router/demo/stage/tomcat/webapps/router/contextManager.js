/**
 * contextManager.js
 * <p>
 * This library facilitates communication between a local application 
 * running on the user's computer and a browser instance the user is 
 * looking at.   Currently there are two such applications:  
 * <p>
 *    - the DDL / DICOM Service
 *    - Osirix 
 * <p> 
 * In the current implementation, communication is effected by
 * adding script tags to the page which allow JSON responses
 * to be sent back to the page with return data from the DDL.
 * <p>
 * When a page that wishes to use the DDL loads it should initiate
 * communications with the DDL by executing the pingDDL function.
 * This will cause a repetitive call to go to the DDL, checking
 * it's status.
 * <p>
 * Commands are typically sent to the DDL using the sendCommand 
 * function.
 * <p>
 * There are some useful event objects:
 * <ul>
 *      <li>ddlEvents   -   receive notifications about DDL 
 *                          starting, stopping and other things
 *      <li>commands    -   receive results of commands
 *                          you send to the DDL.
 * </ul>
 * <p>
 * It is important to understand that all commands are handled asynchronously
 * by the DDL and thus all responses to the initial command sent are
 * instantaneous.   Although JSONP is supported to receive notifications
 * about whether your command launched successfully, it typically won't have
 * any useful result. The only way to see results of completion of a command
 * is to bind to the commands event object using the MochiKit signal library.
 */
var portNumber = true;

var baseURL = "http://localhost:16092/";

var ddlRunning = null;
var pingTimer = null;
var pongTimer = null;
var noping = false;
var pingIntervalMs = 3000;

/**
 * Sends a 'ping' command to the local DDL to test if it is 
 * alive and get current status info in reply.
 */
function pingDDL(callback) {

    if(noping) {
        setTimeout(pingDDL,5000);
        return;
    }

    if(pongTimer) {
        clearTimeout(pongTimer);
        pongTimer = null;
    }

    sendCommand("ping", {jsonp: 'pongDDL'});
    pingTimer = setTimeout(partial(pingTimeout,callback),4000);
}

/**
 * Object to be the target of command results from DDL.
 * Event listeners should bind to this object to receive asynchronous
 * command results.
 */
var commands = {};

/**
 * Object to which various events are bound relating to UI changes
 * and progress.
 */
var ddlEvents = {};

function pongDDL(response) {
    log("pong => events= " + response.events + ", commands= " + response.result );
    
    clearTimeout(pingTimer);
    pingTimer = null;

    if(pongTimer) { // already in pong phase ... must have been a timeout
        return;
    }
    
    window.response = response;
    
    if(response.events) {
        forEach(response.events.split(','),function(e) {
            signal(window,e);
        });
    }
    
    if(response.result) {
        for(var c in response.result) {
            signal(commands, c + 'Complete', response.result[c]);
        };
    }
    
    signal(ddlEvents,'pong', response);

    if(!ddlRunning) {
        
        // Check version
        if(response.version) {
            ddlVersion = parseInt(response.version,10);
        }
            
        
        if((typeof requiredDDLVersion != 'undefined') && (ddlVersion < requiredDDLVersion)) {
            signal(ddlEvents,'restartRequired', response);
        }
        else {
            log("DDL started");
            signal(ddlEvents,'ddlStarted');
        }
        setCookie('mcddl','true', new Date( new Date().getTime() + 24 * 3600 * 1000 * 30), '/');
	    if(window.render_top_nav)
	        render_top_nav();
        
    }
    ddlRunning = true;
    
    pongTimer = setTimeout(pingDDL,pingIntervalMs);
}

function pingTimeout(callback) {

    if(noping)
        return;

    log("Ping timeout at " + (new Date()).getTime());
    
    if(ddlRunning) {
        log("DDL stopped"); 
        signal(ddlEvents,'ddlStopped');
    }
    ddlRunning = false;
    
    if(callback && (typeof callback == 'function')) {
        callback();
    }
    
    signal(ddlEvents, 'pingTimeout');
    
    pongTimer = setTimeout(pingDDL,3000);
    setCookie('mcddl','false',null, '/');
    if(window.render_top_nav)
        render_top_nav();
}

/**
 * A unique id for this window so that multiple windows can talk to a single
 * DDL and not get commands or their results confused.
 */
var ctxMgrWindowId = (new Date().getTime()) + parseInt(Math.random() *100000);

function initContextManager() {
    alert('Deprecated!');
}

/**
 * Sets the Authorization context. Currently this is simply the MedCommons ID 
 * of the authenticated user; perhaps something more complex will be needed in
 * the future. It is typically called from the body's onload handler.
 */
function setAuthorizationContext (accountId) {
	//alert ("Authorization Context: " + accountId);
	setContextURL(baseURL + "setAuthorizationContext?accountId="
		+ accountId +
		"&groupName=" + groupName +
		"&auth=" + auth
		);
		
 	return (true);
}

/**
 * loadDocument is a utility function that sets the document focus and then 
 * requests the context manager to download a particular document from  
 * a gateway.
*/
function loadDocument(storageId, guid, cxpprotocol, cxphost, cxpport, cxppath){
	 //          setDocumentFocus( storageId, guid, cxpprotocol, cxphost, cxpport, cxppath);
	downloadDocumentAttachments( storageId, guid, cxpprotocol, cxphost, cxpport, cxppath);
	//triggerLoad();
}

/**
 * setDocumentFocus sets the document focus of the context manager.
 * The focus includes all parameters needed to access a particular document.
 * These are:
 * storageId - the MedCommonsID where the data is stored.
 * guid - the document's SHA-1 identifier within the context of that storageId.
 * cxphost - the hostname for a particular gateway.
 * cxpport - the port that the gateway is using.
 * 
 * Note: perhaps need to specify protocol (http vs. https) as for the cxp endpoint
 * as well. 
*/
function setDocumentFocus (storageId,guid, cxpprotocol, cxphost, cxpport, cxppath, groupName,groupAccountId, accountId, auth,flags) {

	setContextURL(baseURL + "setDocumentFocus?storageId=" + storageId +
		"&guid=" + guid +
		"&cxpprotocol=" + cxpprotocol +
		"&cxphost=" + cxphost +
		"&cxpport=" + cxpport +
		"&cxppath=" + cxppath +
		"&accountId=" + accountId +
		"&auth=" +auth +
		"&groupAccountId=" + groupAccountId +
		"&groupName=" + groupName +
	    "&flags="+flags
	    +"&rand=" + new Date().getTime()
		);
	return(true);
}
function downloadDocumentAttachments (storageId,guid, cxpprotocol, cxphost, cxpport, cxppath, groupName,groupAccountId, accountId, auth) {


	setContextURL(baseURL + "downloadDocument?storageId=" + storageId +
		"&guid=" + guid +
		"&cxpprotocol=" + cxpprotocol +
		"&cxphost=" + cxphost +
		"&cxpport=" + cxpport +
		"&cxppath=" + cxppath +
		"&accountId=" + accountId +
		"&auth=" +auth +
		"&groupAccountId=" + groupAccountId +
		"&groupName=" + groupName +
		"&rand=" + (new Date()).getTime()
		);
	return(true);
}

/**
 * Send the given command to the local DDL.
 * <p>
 * If opts is supplied then will be passed as parameters.
 * <p>
 * If opt 'gwUrl' is provided then it will be parsed and translated
 * into constituent parts.
 * <p>
 * Supports jsonp = supply opts in form { jsonp: 'someFunctionToCall'}
 */
function sendCommand(cmd, opts) {
  var args = [];
  if(opts) {
    if(opts.gwUrl) 
      parseCXPUrl(opts.gwUrl, opts);
    for(var i in opts) {
      args.push(i + '=' + encodeURIComponent(opts[i]));
    }
  }
  args.push("rand="+(new Date().getTime()) + Math.round(Math.random()*10000));
  args.push("windowId="+ctxMgrWindowId);
  loadContextManagerURL(baseURL + "CommandServlet/?command="+cmd + '&' + args.join('&'));
}

function parseCXPUrl(gwUrl,opts) {
  var host = /:\/\/([^\/]*)\//.exec(gwUrl)[1];
  var path = /:\/\/([^\/]*)(\/.*$)/.exec(gwUrl)[2];
  var protocol =   gwUrl.substring(0,gwUrl.indexOf(':'));
  opts.cxpprotocol = protocol;
  opts.cxphost = host.match(":")?host.substring(0,host.indexOf(':')) : host;
  opts.cxpport = host.match(":")?host.substring(host.indexOf(':')+1) : (protocol=='https'?'443':'80')
  opts.cxppath = path;
  return opts;
}

/**
 * Sets the account focus
 */
function setAccountFocus(accountId, groupAccountId, groupName, auth, host, port, protocol, path, callback) {
	
  if(!callback)
	  callback = "confirmAccountFocus";

  var url = baseURL + "setAccountFocus/?"+
    "accountId=" + accountId +
    "&auth=" + auth +
    "&groupAccountId=" + groupAccountId +
    "&groupName=" + groupName +
    "&cxpprotocol=" + protocol +
    "&cxphost=" + host +
    "&cxpport=" + port +
    "&cxppath=" + path +
    "&jsonp=confirmAccountFocus"
    ;

  loadContextManagerURL(url);
	return true;
}

/**
 * Load the specified URL in a script tag
 */
function loadContextManagerURL(url) {
  log('loading url: ' + url);
  var script = document.createElement("script");
  script.setAttribute("type", "text/javascript");
  script.setAttribute("src", url);
  script.className = 'ctxmgr';
  var head = document.getElementsByTagName("head").item(0);
  head.appendChild(script);
}

function vacuumContextManagerScripts() {
	forEach(document.getElementsByTagName('script'), function(s) {
		if(s.className == 'ctxmgr') {
	    	removeElement(s);
		}
	});
}

var ddlDetected = false;

function confirmAccountFocus(result) {
  ddlDetected = true;
  if(window.onDDLDetected)
    onDDLDetected();
}

/**
 * Clears the currentAuthorization context.
*/
function clearAuthorizationContext(){
	setContextURL(baseURL + "clearAuthorizationContext");
}

/**
 * Clears the current document focus.
*/
function clearDocumentFocus(){
	setContextURL(baseURL + "clearDocumentFocus");
}
/**
 * Utility method for communication with the contextFrame
 */
function setContextURL(url){
    loadContextManagerURL(url);
}

/**
 * Utility method for communication with the loadframe.
*/
/*
function triggerLoad(){
  if(!window.loadFrame) {
    initContextManager();
  }
	loadFrame.location.href=baseURL + "tnum";
}
*/
