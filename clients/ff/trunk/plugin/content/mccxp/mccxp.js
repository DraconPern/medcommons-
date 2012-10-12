/**********************************************************************
 * 
 * MedCommons CXP XUL Client
 *
 * Copyright MedCommons Inc. 2005-2006
 *
 * @author Simon Sadedin, MedCommons Inc.
 */

logging__ = true;
xpathdebug__ = false;

const CCR_NS = "urn:astm-org:CCR";
const CCR_MIME_TYPE = 'application/x-ccr+xml';
const WARN_REFERENCE_FILE_LENGTH = 180*1024;
const VENDOR_INFO_BASE_URL = "https://secure.private.medcommons.net/";
const VENDOR_PIN_LABEL = "(No PIN Required)";

var mccxp_log = new Array();
var medcommonsNotificationActorName = "MedCommons Notification";
var medcommonsNotificationActorType = "Repository";
var notificationDescription = "Notification";
var notificationReplyDescription = "Notification reply";
var informationSystem = "";
var cxpVersion = "<CXPVersion>0.9.3</CXPVersion>";
var default_cxp_service_url = "https://cxp.medcommons.net/router/CxpServlet";

function mccxpLog(msg) {
  var entry = new Object();
  entry.date = new Date();
  entry.msg = msg;
  mccxp_log.push(entry);
}

function mccxpAlert(msg) {
  mccxpLog(msg);
  alert(msg);
}

/**
 * Parses xml to create a DOM document using the native
 * Mozilla parser.
 */
function mccxp_xmlParse(xml) {
  // parse the xml
  var parser = new DOMParser();
  var doc = parser.parseFromString(xml, "text/xml");
  var roottag = doc.documentElement;
  if ((roottag.tagName == "parserError") ||
      (roottag.namespaceURI == "http://www.mozilla.org/newlayout/xml/parsererror.xml")){
    return null;
  }
  return doc;
}

//include (jslib_file);

var mccxp_history = new Array();

/**
 * History item that will be used to populate problem report
 */
var mccxp_problem_history;

/**
 * Create MCCXPHistoryEntry object
 */
function MCCXPHistoryEntry(guid,pin,date,destUrl,toEmail,tn) {
  this.guid = guid;
  this.pin = pin;
  this.date = date;
  this.destUrl=destUrl;
  this.toEmail = toEmail;
  this.tn = tn;
  if(!this.toEmail) {
    this.toEmail = '';
  }
}

/**
 * Convert MCCXPHistoryEntry to XML format
 */
MCCXPHistoryEntry.prototype.toXml = function() {
  return "<HistoryEntry><guid>"+this.guid+"</guid><pin>"
    +this.pin+"</pin><date>"+this.date.getTime()+'</date><dest>'+this.destUrl+'</dest>' 
    +'<toEmail>'+escape(this.toEmail)+'</toEmail><tn>'+this.tn+'</tn></HistoryEntry>';
}

/**
 * Parse MCCXPHistoryEntry from XML string
 */
MCCXPHistoryEntry.prototype.fromXml = function(xml) {

  var dom = xmlParse(xml);
  var ctx = new ExprContext(dom);
  this.guid = xpaths.guid.evaluate(ctx).stringValue();
  this.date=new Date();
  this.date.setTime(xpaths.date.evaluate(ctx).stringValue());
  this.pin = xpaths.pin.evaluate(ctx).stringValue();
  this.destUrl = xpaths.dest.evaluate(ctx).stringValue();
  this.toEmail = unescape(xpaths.toEmail.evaluate(ctx).stringValue());
  this.tn = xpaths.tn.evaluate(ctx).stringValue();
  return this;
}

var cxpServer = '';
var mccxp_version;
var mccxp_version_info;
var mccxp_version_hash;
var mccxp_client_id;

function mccxp_init() {
  mccxpLog("Starting.");
  mccxp_version = mcxelv('version');
  mccxp_version_info = mccxp_version.split('.');
  if(mccxp_version_info.length >= 3) {
    mccxp_version_hash = 
      parseInt(mccxp_version_info[2]) 
     + parseInt(mccxp_version_info[1]*100) 
     + parseInt(mccxp_version_info[0]*100000);
  }
  else {
   mccxpLog('Warning: unable to parse version info for version ' + mccxp_version);
   mccxp_version_hash=0;
  }

  mccxpLog("Installed version " + mccxp_version_hash);
  try {
    var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
    var mccxpBranch = prefService.getBranch("mccxp.");
    var prev_version = 0;

    if(mccxpBranch.prefHasUserValue("mccxp-version")) {
      prev_version = parseInt(mccxpBranch.getCharPref("mccxp-version"));
      mccxpLog("Previously installed version " + prev_version);
     
      
    }
    else {
      // Clear history when upgrading to the version that includes version numbers
      // this happened at 0.9.5 => 0.9.6
      if(!prev_version || (prev_version == '')) {
        mccxp_history = new Array();
        mccxp_saveHistory();
      }
    }

    mccxpBranch.setCharPref("mccxp-version",mccxp_version_hash);
    
    if (prev_version < 932){
          mccxpBranch.setCharPref("default-cxp-host", default_cxp_service_url );
    }

    if(mccxpBranch.prefHasUserValue("default-cxp-host")) {
      cxpService = mccxpBranch.getCharPref("default-cxp-host");
    }
    else {
      cxpService = default_cxp_service_url;
    }
    
    if(mccxpBranch.prefHasUserValue("default-file")) {
      document.getElementById("filename").value = mccxpBranch.getCharPref("default-file");
    }

    if(mccxpBranch.prefHasUserValue("history")) {
      var historyXml = mccxpBranch.getCharPref("history");
      if(historyXml != '') {
        var entries = historyXml.split(',');
        mccxpLog('Loading ' + entries.length + ' history entries');
        for(i=0;i<entries.length; ++i) {
          var newHistory = new MCCXPHistoryEntry().fromXml(entries[i]);
          mccxp_history.push(newHistory);
        }
        try {
          mccxp_historyInit();
        }
        catch(e) {
          alert("An error occurred loading your history:\n\n  "+e);
        }
      }
    }

    if(mccxp_history.length == 0) {
      mcxel('getOptionGroup').selectedItem = mcxel('getTrackNumber');
      mcxel('getFromHistory').disabled = true;
    }

    if(mccxpBranch.prefHasUserValue("default-subject")) {
      mcxel('subject').value=mccxpBranch.getCharPref("default-subject");
    }
    else {
      mccxpBranch.setCharPref("default-subject",'MedCommons Notification for $send_to_recipient');
    }

    // Set client id, if there is one
    if(mccxpBranch.prefHasUserValue("client-id")) {
      mccxp_client_id = mccxpBranch.getCharPref("client-id");
    }
    else {
      // Manufacture a new client id
      mccxp_client_id = mccxp_generateClientId();
      mccxpBranch.setCharPref("client-id",mccxp_client_id);
    }

    mccxp_checkMedCommonsWindows();
    window.setInterval('mccxp_checkMedCommonsWindows()',500);

    mccxp_loadFileDetails();
    mccxp_updateSubject();

    // Load previously used assigned PIN
    if(mccxpBranch.prefHasUserValue("default-pin")) {
      mcxel('assignedPin').value=mccxpBranch.getCharPref("default-pin");
    }
    else {
      var pin = mccxp_generatePin();
      mcxel('assignedPin').value=pin;
      mccxpBranch.setCharPref("default-pin",pin);
    }

    if(mccxpBranch.prefHasUserValue("mccxp-terms-of-use")) {
      mcxel('termsOfUse').checked=(mccxpBranch.getCharPref("mccxp-terms-of-use") == 'true');
    }
    informationSystem = "<InformationSystem><Name>MedCommons</Name><Type>CXP Client</Type><Version>" + 
        mcxelv('version') + "</Version></InformationSystem>";

    // Check if vendor branding needs to be applied
    mccxp_updateVendor();

    // Fetch known vendor list
    mccxp_fetchVendors();

    mccxpLog('Initialized.');
  }
  catch(e) {
    mccxpAlert(e);
  }
}

/**
 * Queries for a list of known vendors and if found,
 * adds them as trusted recipients to the Notify list
 */
function mccxp_fetchVendors() {
  var url = VENDOR_INFO_BASE_URL + 'rls/dropdown.xml';

  mccxpLog("Fetching vendors from " + url);

  // Try and retrieve the vendor information
  doSimpleXMLHttpRequest(url)
      .addCallbacks(mccxp_fetchVendorsSuccess, mccxp_fetchVendorsFail);
}

var mccxp_vendors = new Array();

function mccxp_fetchVendorsSuccess(req) {
  var vendor = null;
  try {
    nodeWalk(req.responseXML, function(n) {
      if(n.tagName == "name") {
        vendor = { name: n.firstChild.nodeValue };
        mccxp_vendors.push(vendor); 
      }
      else
      if(n.tagName == "vurl") {
        vendor.vurl = n.firstChild.nodeValue;
      }
      return n.childNodes;
    });

    forEach(mccxp_vendors, function(v) { 
      mccxpLog("Found vendor: " + v.name + " vurl: " + v.vurl); 
      addVendor(v);
    });
  }
  catch(e) {
    mccxpLog("Error initializing vendors: " + e.message);
    mccxp_dump(e);
  }
}

function mccxp_fetchVendorsFail(e) {
  // This may happen if the user is on a local network
  // or not connected to the internet.
  // mccxp_dump(e);
  alert("Warning:  preconfigured vendors could not be loaded from the internet:\r\n\r\n"+e.message);
}

/**
 * Adds the given vendor to the dropdown as a trusted Vendor.
 * This causes an outgoing request to fetch the xml of the vendors name.
 */
function addVendor(v) {
  var url = VENDOR_INFO_BASE_URL + 'rls/'+v.name+'/ccrclientini.xml';

  //mccxpLog("Fetching vendor with url " + url);

  // Try and retrieve the vendor information
  doSimpleXMLHttpRequest(url)
      .addCallbacks(bind(mccxp_fetchVendorSuccess,v), bind(mccxp_fetchVendorFail,v));
}


function mccxp_fetchVendorSuccess(req) {
  var vendor = null;
  try {
    var nikname = req.responseXML.getElementsByTagName("nikname")[0].firstChild.nodeValue;
    $('recipient').appendItem(nikname + " " + VENDOR_PIN_LABEL).vendor = this;
  }
  catch(e) {
    mccxpLog("Error initializing vendors: " + e.message);
    mccxp_dump(e);
  }
}

function mccxp_fetchVendorFail(e) {
  mccxpLog("Unable to retrieve info for vendor " + this.name + ": " + e.message + " Code: " + e.number); 
}

/**
 * A simple poll function that checks if any MedCommons windows
 * are open and if so, enables appropriate options in the UI
 */
function mccxp_checkMedCommonsWindows() {
  if(mccxp_findMedCommonsWindow()) {
    if(mcxel('getViewer').disabled) {
      mcxel('getOptionGroup').selectedItem = mcxel('getViewer');
    }
     mcxel('getViewer').disabled = false;
  }
  else
     mcxel('getViewer').disabled = true;
}


// These cached values help us not wipe out the user's 
// edits when they didn't change either the template or the
// send-to field
var mccxp_prevTemplate;
var mccxp_prevSendTo;

/**
 * Reads the subject template from preferences and populates it
 * to the subject box, replacing any template parameters with
 * their values.
 */
function mccxp_updateSubject() {
  var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
  var mccxpBranch = prefService.getBranch("mccxp.");
  var currSubject = mccxpBranch.getCharPref("default-subject");

  if((currSubject == mccxp_prevTemplate) && (mcxelv('recipient') == mccxp_prevSendTo)) 
    return;

  mcxel('subject').value = 
    currSubject.replace(/\$send_to_recipient/,mcxelv('recipient'));

  mccxp_prevSendTo = mcxelv('recipient');
  mccxp_prevTemplate = currSubject;
}

/**
 * Basic entry point from Moz/FF menu
 */
function mccxp_send() {
  window.open("chrome://mccxp/content/mccxp.xul","mcsend","chrome,toolbar=no,resizable=yes,width=480,height=560");
}

function mccxp_saveHistory() {
  // Save new txid/pin
  var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
  var mccxpBranch = prefService.getBranch("mccxp.");

  var xml="";
  for(i=0;i<mccxp_history.length;++i) {
    if(i>0) {
      xml += ',';
    }
    xml += mccxp_history[i].toXml();
  }
  mccxpBranch.setCharPref("history", xml );
}


function mccxp_clearHistory() {
  if(confirm('Really clear history?')) {
    mccxp_history = new Array();
    mccxp_saveHistory();
    try {
      var historyTable = document.getElementById('historyTable');
      while(historyTable.getRowCount() > 0) {
        historyTable.removeItemAt(2);
      }
    }
    catch(e) {
      mccxpAlert(e);
    }
  }
}

/**
 * Handles successful response from CXP server for transfer
 */
function mccxp_transferSuccess(req) {
  try {
    mccxpLog("Received " + req.responseText.length + " bytes response data");
    var dom = xmlParse(req.responseText);
    var ctx = new ExprContext(dom);
    var status = xpaths.transferStatus.evaluate(ctx).stringValue();
    var reason = xpaths.reason.evaluate(ctx).stringValue();
    var progDoc = mccxp_progressWindow.document;
    var statusValue = parseInt(status);
    
    
    
    if(!progDoc) {
      return; // user closed window, forget it
    }
    
    if(statusValue < 299) {
      var txId = xpaths.txId.evaluate(ctx).stringValue();
      var uid = xpaths.uid.eval(dom).stringValue();
      if(uid == '') { // legacy case, old protocol did not have uid, only txId
        uid = txId;
      }
      mccxpLog("CXP Transaction successful with status=" + status + " uid="+ uid +" id=" + txId);
      progDoc.getElementById("status").value=status;
      var tn = txId.substring(0,txId.length-5);
      progDoc.getElementById("txid").value=tn;
      var pin = txId.substring(txId.length - 5);
      progDoc.getElementById("pin").value=pin;
      
      progDoc.getElementById("reason").value=reason;
      // Find the ccr recipient
      var ccrDom = req.ccrXmlDom;
      var toEmail = xpaths.ccrToEmailMedCommons.eval1(ccrDom);

      var h = new MCCXPHistoryEntry(uid, pin, new Date(), cxpService,toEmail,tn);
      if($('printReceipt').checked) {
        $('printwindow').contentDocument.getElementById('trackingNumber').innerHTML=tn;
        mccxp_print();
      }
      mccxp_history.push(h);
      mccxp_saveHistory();
      mccxp_addHistoryEntry(h);
      if ((statusValue == 210) || (statusValue == 220)) // Multiple email destinations, XML schema validation problems
        mccxp_progressWindow.alert("Warning:\r\n" + reason);
    }
    else if ((status == 420) || (status == 430)){ // Out of date version; should upgrade
      mccxpLog("Server returned status " + status);
      mccxpLog("Full server response " + req.responseText);
      
      mccxp_progressWindow.alert("CXP transaction failed: client software out of date \r\n" + reason +
        '\r\n Please go to https://secure.medcommons.net/public/hp.html for the latest client.'
      );
      progDoc.getElementById("status").value=status;
      progDoc.getElementById("reason").value=reason;
    }
    else if(statusValue < 499){
      mccxpLog("Server returned status " + status);
      mccxpLog("Full server response " + req.responseText);
      mccxp_progressWindow.alert("Transfer Failed\r\n\r\nClient Error:  " + status + "\r\n\r\nFull server response:\r\n" + reason);
      progDoc.getElementById("status").value=status;
      progDoc.getElementById("reason").value=reason;
    }
    else {
      mccxpLog("Server returned status " + status);
      mccxpLog("Full server response " + req.responseText);
      mccxp_progressWindow.alert("Transfer Failed\r\n\r\nServer Error:  " + status + "\r\n\r\nFull server response:\r\n" + reason);
      progDoc.getElementById("status").value=status;
      progDoc.getElementById("reason").value=reason;
    }
  }
  catch(e) {
    mccxpLog("Error processing response: " + e);
    alert("An error occurred processing the response:\r\n\r\n" + e 
          + "\n\nResponse content: \r\n\r\n" + req.responseText );
  }
 }

var mccxp_progressWindow;

/**
 * Constructs a CXP Transfer and sends it to the configured CXP server
 * for the current CCR.
 */
function mccxp_upload() {
  var fileName = mcxelv('filename');  
  var mccxpBranch = null;

  if(!mccxp_checkTermsOfUse()) {
    return;
  }

  if(!confirm("About to send " + fileName))
    return;
  
  try{
    mccxpLog("Loading file " + fileName);
    var ccrXml = readFile(fileName);
    mccxpLog("Loaded  " + ccrXml.length + ' bytes');
    }
    catch(e){
     mccxpLog('File not found:' + fileName);
     alert("CCR can not be found on disk:\n" + filename);
     return;
    }
    try {
    var req = getXMLHttpRequest();

    // What happens if this fails because server not reachable?
    mccxpLog('Opening request to ' + cxpService);
    req.open("POST", cxpService, true);

    req.setRequestHeader("Content-Type","application/x-www-form-urlencoded");

    var pin = mcxelv('assignedPin');
    if(pin != '') {
      if(pin.length < 5) {
        alert('Your assigned PIN is too short.  Please enter 5 digits.');
        mcxel('assignedPin').focus();
        mcxel('assignedPin').select();
        return;
      }
    }
   
    
    req.ccrXmlDom = mccxp_xmlParse(ccrXml);

    // Always update date and time and the CCR document id. In the future
    // perhaps these should be updated only when the internals of the 
    // document have changed.
    updateCcrDocumentObjectID(req.ccrXmlDom);
    updateDateTime(req.ccrXmlDom);
    var actorIDList = xpaths.actorIDList.evaln(req.ccrXmlDom);
    if (actorIDList && actorIDList.length > 0){
        //alert("Number of actors is " + actorIDList.length);
        var actorArray = new Array(actorIDList.length);
        
        //var lastActor = actorIDList[actorIDList.length -1];
        
        var actorList = "Actors ( number of actors = " + actorIDList.length + ")\n";
        for (i in actorIDList){
            anActor = actorIDList[i];
            actorList += "\n" + anActor.textContent;
            
        }
        //alert (actorList);
        for (i in actorIDList){
            actorArray[i] = actorIDList[i].textContent;
        }
        //var newActorID = generateUniqueActorObjectID();
        //alert(newActorID);    
    }
    
    // Replace/Add the toEmail
    var toEmail = mcxelv('recipient');
    if(toEmail && (toEmail != '')) {
      if(!mccxp_insertNotification(req.ccrXmlDom,xpaths.ccrToMedCommonsActor,xpaths.ccrTo, toEmail,notificationDescription)) {
        alert('Warning:  your configured To address was not able to be\r\nadded to the CCR.  A suitable To section could not be found or created');
        //TODO create <To>
      }
      
    }

    var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
    mccxpBranch = prefService.getBranch("mccxp.");

    // If user provided a from address, replace that
    if(mccxpBranch.prefHasUserValue("from-email")) {
      var fromEmail = mccxpBranch.getCharPref("from-email");
      if((fromEmail != null) && (fromEmail != '')) {
        if(!mccxp_insertNotification(req.ccrXmlDom,xpaths.ccrFromMedCommonsActor,xpaths.ccrFrom,fromEmail, notificationReplyDescription)) {
          alert('Warning:  your configured From address was not able to be\r\nadded to the CCR.  A suitable From section could not be found or created');
        }
      }
    }
    
    // Add references if they are found in the same directory
    //var dir = fileName.replace(/[^\/\\]*$/, '');
    var xmldata = mccxp_createReferences(req.ccrXmlDom);
    ccrXml = new XMLSerializer().serializeToString(req.ccrXmlDom);
  }
  catch(e) {
      alert("CCR Client error processing CCR: please ensure that CCR is valid.\n\n" + e);
      return;
  }
  try {
    // debug
    // note: don't leave this in, it breaks on Macs/Linux
    // mccxp_writeFile(ccrXml,"c:\\test.xml");

    //var data = "ccrdata="+escape(ccrXml);    
    
    
    var data = "clientId="+mccxp_client_id;
    mccxpLog('Attaching client id ' + mccxp_client_id + ' to CXP message');
    data += "&ccrdata="+encodeURIComponent(ccrXml);    
    if(xmldata != null) {
      mccxpLog("Computed " + xmldata.length + " bytes of xmldata"); 
      data+="&xmldata="+encodeURIComponent(xmldata);
    }
    data += "&subject="+encodeURIComponent(mcxelv('subject'));  
    // Set registry on/off  
    var enableRegistry = document.getElementById('agreeRegistry').innerHTML=$('agreeRegistry').checked? "TEPR" : "NONE";
    data += "&registryEnabled=" + enableRegistry;
    mccxpLog("Registry setting is " + enableRegistry);
    
    
     var receiverProviderId = mcxelv('receiverProviderId');
    if(receiverProviderId != '') {
        data+="&ReceiverProviderId="+encodeURIComponent(receiverProviderId);
      }
     
    var commonsId = mcxelv('commonsId');
    if(commonsId != '') {
        data+="&CommonsId="+encodeURIComponent(commonsId);
      }
    
    var senderProviderID = mccxp_getPref("SenderProviderID");

    if(senderProviderID || (senderProviderID != '')) {
        data +="&SenderProviderId=" + senderProviderID;
        mccxpLog("SenderProviderId " + senderProviderID);
    }
    if(pin != '') {
      data+="&pin="+encodeURIComponent(pin);
      
      if(mcxelv('subject').indexOf(pin) >= 0) {
        window.open("chrome://mccxp/content/pinwarning.xul","mcpinwarning","chrome,toolbar=no,resizable=yes,width=350,height=130,dialog,modal");
        if(!window.mccxp_dialog_result) {
          alert('Send Cancelled!');
          return;
        }
      }
    }
    
    mccxpLog('Sending CXP Request');
    var deferred = MochiKit.Async.sendXMLHttpRequest(req,data);    
    var err = function(req) { mccxpAlert("Error sending request"); };
    mccxp_progressWindow = window.open("chrome://mccxp/content/progress.xul","mcprogress","chrome,toolbar=no,resizable=yes,width=250,height=150");
    deferred.addCallback( mccxp_transferSuccess );
    deferred.addErrback( err );

    // Save preferences
    mccxpBranch.setCharPref("default-cxp-host", cxpService );
    mccxpBranch.setCharPref("default-file", fileName );
    if(pin != '') {
      mccxpBranch.setCharPref("default-pin", pin);
    }
    
    mccxp_progressWindow.focus();
  }
  catch(e) {
    mccxpAlert("CCR Client Error transmitting CCR:\n\n " + e);
  }
}

var mccxp_attachWindow;
var mccxp_references = new Array();

function mccxp_initAttach() {
    mccxp_attachWindow = window.open("chrome://mccxp/content/attach.xul","mcattach","chrome,toolbar=no,resizable=yes,width=450,height=400,dialog,modal");
    mccxp_updateAttachmentsLabel();
}

/**
 * Updates the label showing how many attachments have been added. Call if the
 * number of attachments might have been modified.
 */
function mccxp_updateAttachmentsLabel() {
  if(mccxp_references.length > 0) {
    mcxel('attachments').value='(' + mccxp_references.length + ' attachment)';
  }
  else
    mcxel('attachments').value='';
}

var mccxp_historyWindow;
function mccxp_showHistory() {
    mccxp_historyWindow = window.open("chrome://mccxp/content/history.xul","mchistory","chrome,toolbar=no,resizable=yes,width=450,height=400");
}

function mccxp_historyInit() {
  var doc = document;
  var historyTable = doc.getElementById('historyTable');
  const XUL="http://www.mozilla.org/keymaster/gatekeeper/there.is.only.xul";    
  //var history = window.opener.mccxp_history; 
  var history = mccxp_history; 

  var toEmails = new Array();

  try {
    for(i=0;i<history.length;++i) {
      mccxp_addHistoryEntry(history[i]);
      var toEmail = history[i].toEmail;
      if(toEmails[toEmail]==null) {
        // Do not add trusted vendors even if in history - they will get verified
        // and downloaded separately
        if(toEmail.indexOf(VENDOR_PIN_LABEL) < 0) {
          var item = $('recipient').appendItem(toEmail);
        }
        toEmails[toEmail]=1;
      }
    }
  }
  catch(e) {
    mccxp_dump(e);
  }
}

/**
 * Adds a new entry to the history table for the given item
 */
function mccxp_addHistoryEntry(h) {
  var doc = document;
  var historyTable = doc.getElementById('historyTable');
  if(!historyTable) { // allow call from child window
    historyTable = window.opener.document.getElementById('historyTable');
    window.opener.mccxp_problem_history = h;
  }
  else
    mccxp_problem_history = h;

  var d = h.date;
  var ds = mccxp_formatDate(h.date);;
  var row = doc.createElement('listitem');
  row.appendChild(doc.createElement('listcell')).setAttribute('label', ds);
  row.appendChild(doc.createElement('listcell')).setAttribute('label', h.toEmail);
  var guidCell = doc.createElement('listcell');
  // If MedCommons tn present, display that, otherwise guid (yuck)
  if((h.tn != null) && (h.tn != '')) {
    guidCell.setAttribute('label', h.tn);
  }
  else
    guidCell.setAttribute('label', h.guid); // note: old history entries have tn as guid, so this works for them

  guidCell.setAttribute('class', 'guidLink');
  row.appendChild(guidCell);
  row.setAttribute('ondblclick', 'mccxp_displayHistoryItem('+(historyTable.getRowCount())+');');
  // why onselect doesnt work?
  row.setAttribute('onclick', 'mccxp_selectHistoryItem(event,'+(historyTable.getRowCount())+');');
  row.appendChild(doc.createElement('listcell')).setAttribute('label', h.pin);
  // commented out version will make it sort with oldest entries first 
  // instead of newest entries first
  //historyTable.appendChild(row);
  historyTable.insertBefore(row, historyTable.childNodes[2] );
  mcxel('getFromHistory').disabled=false;
}

/**
 * Finds a history item by index and displays it
 */
function mccxp_displayHistoryItem(index) {
  var h = mccxp_history[index];
  mccxpLog("Displaying history entry " + h.guid + " on host " + h.destUrl);
  mccxp_displayHistoryObject(h);
}

var mccxp_currentHistoryItem;
function mccxp_selectHistoryItem(evt, index) {

  // Make up for listbox not allowing deselect by implementing that ourselves
  // NB: sadly this does not work at all
  if(evt.ctrlKey) {
    if(mcxel('historyTable').selectedIndex == index) {
      mcxel('historyTable').selectedIndex = -1;
      mccxp_currentHistoryItem = null;
      evt.preventDefault();
      evt.stopPropagation();
      return;
    }
  }
  
  mccxp_currentHistoryItem = mccxp_history[index];
  mccxp_problem_history = mccxp_currentHistoryItem;
  mcxel('getOptionGroup').selectedItem=mcxel('getFromHistory');
  document.getElementById('getCxpButton').disabled=false;
  mcxel('deleteButton').disabled=false;
}

/**
 * Displays the document for a particular history object in a browser
 */
function mccxp_displayHistoryObject(h) {
  var trackNum = h.tn;
  // legacy support
  if((trackNum == null) || (trackNum == '')) 
    trackNum = guid.substring(0,h.guid.length-5);

  var pin = h.pin;
  // temp hack until PIN issues resolved:  PIN is last 5 digits of guid
  if((pin == '') || (pin == null)) {
    pin = h.guid.substring(h.guid.length-5);
  }

  var gwUrl =  h.destUrl.replace(/\/router\/.*$/,'/router/tracking.jsp?tracking='+trackNum+'&p='+pin);  
  window.open(gwUrl,'mccxp_tracking');
}

function mccxp_getHistoryItem() {
  try {
    var historyTable = document.getElementById('historyTable');
    var h = mccxp_history[ mccxp_history.length - 1 - historyTable.selectedIndex ];
    var req = getXMLHttpRequest();
    mccxpLog("Sending CXP Get to " + h.destUrl + " for guid " + h.guid);
    alert("Get " + h.destUrl + " guid=" + h.guid + " tn="+h.tn);
        req.open("POST", h.destUrl, true);
    req.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
    req.history = h;
    req.guid = h.guid;
    req.onSuccess=mccxp_getHistorySuccess;
    
    var queryXml = '<?xml version="1.0" encoding="UTF-8"?><CXP><OperationCode>QUERY</OperationCode><QueryString>'+h.guid+'</QueryString>';
    queryXml += cxpVersion;
    queryXml += informationSystem;
    queryXml += '</CXP>';
    
    var postData ='xmldata='+escape(queryXml);
    var deferred = MochiKit.Async.sendXMLHttpRequest(req,postData);    
    var err = function(e) { alert("GET failed:\n\n  " + e); };
    deferred.addCallback( mccxp_getSuccess );
    deferred.addErrback( err );
  }
  catch(e) {
    mccxpAlert(e);
  }
}

function mccxp_getHistorySuccess(req,ccrXml) {
  try {
    var d = new Date();
    var ds = (d.getMonth()+1)+'_'+d.getDate()+'_'+d.getFullYear()+'-'+d.getHours()+'_'+ (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes());
    ds += ".xml";
    var fileName = mccxp_selectSaveFile(ds);
    if(fileName) {
      mccxp_writeFile(ccrXml, fileName);
      alert('CCR saved as file ' + fileName);
    }
    else {
      alert('file save canceled');
    }
  }
  catch(e) {
    alert('Failed to save file:\r\n\r\n  '+e);
  }
}


/**
 * General function for receiving CXP GET
 * Parses response and hands off to secondary handler
 */
function mccxp_getSuccess(req) {
  var dom = xmlParse(req.responseText);
  //alert(req.responseText);
  req.responseDom = dom;
  var ctx = new ExprContext(dom);
  var status = xpaths.transferStatus.evaluate(ctx).stringValue();
  var reason = xpaths.reason.evaluate(ctx).stringValue();
  var statusValue = parseInt(status);
  //alert("transfer status is " + status);
  if(status < 299) {
    var ccrXml = xpaths.cxpGetResult.evaluate(ctx);
    if((ccrXml == null) || (ccrXml == '')) {
      alert('Error:  document missing from CXP response');
    }
    else {
      var ccrXmlUnencoded = unescape(ccrXml.stringValue());
      // Secondary handler
      req.onSuccess(req,ccrXmlUnencoded);
    }
  }
  else {
    alert('Document ' + req.guid + ' could not be retrieved.\r\n\r\nStatus:   ' + status + '\n\n\rReason:' + reason);
  }
}

/**
 * Perform get from remove server.  No longer shows dialog,
 * the kind of get is determed by radio buttons on the page.
 */
function mccxp_cxpGet() {
  var radio = mcxel('getOptionGroup').selectedItem;
  if(radio == mcxel('getViewer')) {

    var win = mccxp_findMedCommonsWindow();
    if(win) {
      var tn = win.content.medcommons_advertised_ownerId.substr(4);
      var pin = win.content.medcommons_advertised_pin;
      var gwUrl = win.content.medcommons_advertised_cxpUrl;
      //mccxpAlert('Found CCR: ' + tn + ' at gw ' + gwUrl + ' pin = ' + pin);
      mccxp_getTrackingNumber(tn,pin,gwUrl);
    }
    return;
  }
  else
  if(radio == mcxel('getFromHistory')) {
    var historyTable = document.getElementById('historyTable');
    if(historyTable.selectedIndex >= 0) {
      mccxp_getHistoryItem();
    }
    else
      alert('Please select an item in the history list to retrieve');
  }
  else
  if(radio == mcxel('getTrackNumber')) {
    mccxp_getTrackingNumber();
  }
  // If an item is selected in history, get that one, otherwise prompt the user
  //else
  //  mccxp_getWindow = window.open("chrome://mccxp/content/get.xul","mccxp_getWindow","chrome,toolbar=no,resizable=yes,width=300,height=150");
}

/**
 * Does a download of a user specified guid from a remote server.
 */
function mccxp_getTrackingNumber(cxpTxId,pin,gwUrl) {
   
  try {
    // Get the CCR
    if(!cxpTxId) 
      cxpTxId = mcxelv('trackingNumber');

    if(!pin)
      pin = mcxelv('pin');

    if(!gwUrl) {
      gwUrl = cxpService;
    } 
    //alert("getTrackingNumber " + cxpTxId + ", " + pin + ", " + gwUrl);

    if(!cxpTxId || (cxpTxId=='')) {
      alert('Please enter a tracking number in the area provided.');
      mcxel('trackingNumber').focus();
      return;
    }
      
    // hack: until PIN issues resolvd, PIN is last 5 digits of txId
    if((cxpTxId.length > 12) && (pin == cxpTxId.substring(12))) {
      guid = cxpTxId;
    }
    else
      guid=cxpTxId+pin;

    // HACK:  Workaround for server problem with short guid
    while(guid.length < 17) {
      guid = "0" + guid;
    }

    var log;
    if((window.opener != null) && (window.opener.mccxpLog != null)) {
      log = window.opener.mccxpLog;
    }
    else {
      log = mccxpLog;
    }

    var req = getXMLHttpRequest();

    log("Sending CXP Get to " + gwUrl);
    log("QueryString = " + guid);
        req.open("POST", gwUrl, true);
    req.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
    req.onSuccess=mccxp_getDownloadSuccess;
    req.guid = guid;
    req.pin = pin;
    req.tn = cxpTxId;
    

    var queryXml = '<?xml version="1.0" encoding="UTF-8"?><CXP><OperationCode>QUERY</OperationCode><QueryString>'+
        guid+'</QueryString>';

    queryXml += '<TXID>'+  cxpTxId + "</TXID>";
    queryXml += '<PIN>' +pin + '</PIN>';
    queryXml += cxpVersion;
    queryXml += informationSystem;
    queryXml += '</CXP>'; 

    var postData ='xmldata='+escape(queryXml);
    var deferred = MochiKit.Async.sendXMLHttpRequest(req,postData);    
    var err = function(e) { alert("GET failed: \n\n  " + e); };
    deferred.addCallback( mccxp_getSuccess );
    deferred.addErrback( err );
  }
  catch(e) {
    mccxpAlert(e);
  }
}

function mccxp_getDownloadSuccess(req,ccrXml) {

  var mainWnd = window.opener;
  if((!window.opener) || (!window.opener.cxpService)) {
    mainWnd = window;
  }
  // http://gateway001.private.medcommons.net:9080/router/CxpServlet

  var dom = mccxp_xmlParse(ccrXml);
  var toEmail = null;
  try {
    xpaths.ccrToEmail.eval1(dom);
  }
  catch(e) {
    alert(e);
  }
  var cxpTxId = req.tn; //mainWnd.document.getElementById("trackingNumber").value;
  var pin = req.pin; // mainWnd.document.getElementById("pin").value;
  var uid = xpaths.uid.eval(req.responseDom).stringValue();
  
  // Add to history
  var h = new MCCXPHistoryEntry(uid, pin, new Date(), mainWnd.cxpService, toEmail, cxpTxId);
  
  mainWnd.mccxp_history.push(h);
  mainWnd.mccxp_addHistoryEntry(h);

  // Saves file
  mccxp_getHistorySuccess(req,ccrXml);

  // Close window (note xul bug, window.close doesnt work)
  if(window.opener == mainWnd)
    window.setTimeout('window.close()',500);
}

function mccxp_checkTermsOfUse() {
  if(mcxel('termsOfUse').checked) 
    return true;
  else {
    alert('Please read the Terms of Use and check the box provided to indicate you accept them.');
    return;
  }
}

/**
 * Utility function returns preference or null if it doesn't exist
 */
function mccxp_getPref(p) {
  var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
  var mccxpBranch = prefService.getBranch("mccxp.");
  if(mccxpBranch.prefHasUserValue(p)) {
    return mccxpBranch.getCharPref(p);
  }
  else
    return null;
}

function mccxp_updateVendor() {
  var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
  var mccxpBranch = prefService.getBranch("mccxp.");
  var vendorId = mccxp_getPref("SenderProviderID");
  mccxpLog("Preference for SenderProviderId: " +vendorId);
  if(!vendorId || (vendorId == '')) {
    mccxp_setMedCommonsVendor();
  }
  
  var url = VENDOR_INFO_BASE_URL + 'rls/'+vendorId+'/ccrclientini.xml';
  //alert("vendorId = " + vendorId + " url = " + url); 

  // Try and retrieve the vendor information
  doSimpleXMLHttpRequest(url)
      .addCallbacks(mccxp_updateVendorSuccess, mccxp_updateVendorFail);
}

/**
 * Brands the client based on vendor
 *
 * Return XML has form such as:
 *  <vendor>
 *    <name>NextGen</name>
 *    <icon>https://www.nextgen.com/images/pic.gif</icon>
 *    <title>NextGen CCR Client</title>
 *    <subtitle>
 *      <h2>Some subtitle.</h2>
 *    </subtitle>
 *    <nikname>NextGen Demo Doctor</nikname>
 *  </vendor>
 */
function mccxp_updateVendorSuccess(req) {
  try {
    var icon = req.responseXML.getElementsByTagName("icon")[0].firstChild.nodeValue;
    var title = req.responseXML.getElementsByTagName("title")[0].firstChild.nodeValue;
    $('logo').src=icon;
    window.title = title;
  }
  catch(e) {
    mccxpLog("Unable to set vendor branding: " + e.message);
    mccxp_setMedCommonsVendor();
    //mccxp_dump(e);
  }
}

function mccxp_updateVendorFail(e) {
  mccxpLog("Vendor information retrieval failed: " + e.message + " Code: " + e.number);
  mccxp_setMedCommonsVendor();
}

function mccxp_setMedCommonsVendor() {
  $('logo').src = 'images/logo.gif';
  window.title = "MedCommons CCR Client";
}

function mccxp_changeSettings() {
    mccxp_settingsWindow = window.open("chrome://mccxp/content/settings.xul","mccxp_settings","chrome,toolbar=no,resizable=yes,width=400,height=270,dialog,modal");
    mccxp_updateSubject();
    mccxp_updateVendor();
}

/**
 * Search the CCR for references that are present on the local
 * disk and add them in the CCR Data Block
 */
function mccxp_createReferences(ccrDom) {
  var sendCancelledError = new Error("User Cancelled Send");
   var xmlData = "<CXP><OperationCode>TRANSFER</OperationCode>";
  
   xmlData += cxpVersion;
   xmlData += informationSystem;
   xmlData +="<SenderID>1234</SenderID>";
  try {
    var refs = mccxp_references;
    var fromActorID = xpaths.ccrFromMedCommonsActorID.eval1(ccrDom);
  
    if(refs.length == 0){
        xmlData +="</CXP>";
      return xmlData;
      }

    xmlData += "\n<Files>";
  
    // Look for/create the references DOM node    
    var refsResult = xpaths.references.evaln(ccrDom);
    var refsNode;
    if(!refsResult || (refsResult.length == 0)) {
      mccxpLog('CCR has no References node.  Creating one.');
      refsNode = ccrDom.createElementNS(CCR_NS,"References"); 
      var actors = xpaths.actors.evaln(ccrDom);
      mccxpLog('will attempt to add after actors node ' + actors);
      mccxpLog('parent node of actors is ' + actors.parentNode);
      // References must follow actors and be before comments.
      insertAfter(actors[actors.length-1], refsNode);
      //ccrDom.documentElement.appendChild(refsNode);
      
    }
    else {
      refsNode = refsResult[0];
      mccxpLog("Found existing references node " + refsNode);
    }

    for(i=0;i<refs.length; ++i) {
      var ref = refs[i];

      try {
        var fileBits = readFile(ref);
        if(fileBits.length > WARN_REFERENCE_FILE_LENGTH) {
          var kbLength = Math.round(fileBits.length / 1024);
          var msg = 
            "You have attached a file that is " + kbLength + " KB in size.\n\n"
            +"Because of its size Firefox may show warning messages while this attachment is processed\n"
            +"for upload. It is safe to ignore these messages by pressing 'Cancel'.\n\n"
            +"Please select OK to continue or Cancel to abort.";
          if(!confirm(msg)) {
            throw sendCancelledError;
          }
        }
        var sha1 = hex_sha1(fileBits);

        // hack: trim directories
        var fileName = ref.replace(/(.*\\)([^\\]*$)/,'$2' ) .replace(/(.*\/)([^\/]*$)/,'$2'); 

        // Figure out content type
        var contentType = null;
        var fileNameLC = fileName.toLowerCase();
        if(fileNameLC.match(/.pdf$/)) {
         contentType = 'application/pdf';
        }
        else 
        if(fileNameLC.match(/.txt$/)) {
         contentType = 'text/plain';
        }
        else 
        if(fileNameLC.match(/.xml$/)) {
          // We want to send CCRs as the appropriate mime-type
          // To do this we check for two things:
          // a) the word ContinuityOfCareRecord
          // b) the pattern xmlns.*urn:astm-org:CCR
          // in the first 300 chars
          // If they are found, we assume it is a CCR
          var sniffChars = fileBits.substr(0,300);
          if((sniffChars.indexOf('ContinuityOfCareRecord')>0) && 
                sniffChars.match(/xmlns[^<>]*urn:astm-org:CCR/)) {
            contentType=CCR_MIME_TYPE;
            mccxpLog("Identified attachment " + fileName + " as a CCR");
          }
          else
           contentType = 'text/xml';
        }
        else 
        if(fileNameLC.match(/.png$/)) {
         contentType = 'image/png';
        }
        else 
        if(fileNameLC.match(/.jpg$/)) {
         contentType = 'image/jpg';
        }
        else 
        if(fileNameLC.match(/.doc$/)) {
         contentType = 'application/msword';
        }
        else
         contentType = 'application/octet-stream';

        mccxpLog('Reference ' + i + ' of type ' + contentType +' has ' + fileBits.length + ' bytes of data');


        // Add the fileBits to the xml-data blob
        xmlData += "\n  <File>\n    <FileName>"+fileName+"</FileName>\n    <FileType>" 
          + contentType + "</FileType>\n    "
          + "<SHA1>" + sha1 + "</SHA1>\n    <FileContents>"
          //+ Base64.encode(fileBits) + "</bits>\n  </file>";
          + mccxp_encode64(fileBits) + "</FileContents>\n  </File>";

        mccxp_writeFile(xmlData, 'c:\\test.xml');

        // Now add a corresponding reference to the CCR dom
        var refNode = ccrDom.createElementNS(CCR_NS,"Reference"); 
        refsNode.appendChild(refNode);
        
        // Add ReferenceObjectID
        var refObjId = ccrDom.createElementNS(CCR_NS,"ReferenceObjectID")
        refNode.appendChild(refObjId).appendChild(ccrDom.createTextNode("mcref-"+i));

        // Add content type
        var refType = ccrDom.createElementNS(CCR_NS,"Type");
        refNode.appendChild(refType).appendChild(ccrDom.createElementNS(CCR_NS,"Text")).appendChild(ccrDom.createTextNode(contentType));

    
        // Add source
        var sourceType = ccrDom.createElementNS(CCR_NS,"Source");
        refNode.appendChild(sourceType);
        var actorIDType = ccrDom.createElementNS(CCR_NS,"ActorID");
        actorIDType.appendChild(ccrDom.createTextNode(fromActorID));
        sourceType.appendChild(actorIDType);
        
        
       // appendChild(ccrDom.createElementNS(CCR_NS,"Text")).appendChild(ccrDom.createTextNode(contentType));

        // Add Location node
        var refLocDesc = refNode.appendChild(ccrDom.createElementNS(CCR_NS,"Locations")).
          appendChild(ccrDom.createElementNS(CCR_NS,"Location")).
          appendChild(ccrDom.createElementNS(CCR_NS,"Description"))
          ;

        // Add location URL
        var urlAtt = refLocDesc.appendChild(ccrDom.createElementNS(CCR_NS,"ObjectAttribute"));
        urlAtt.appendChild(ccrDom.createElementNS(CCR_NS,"Attribute")).appendChild(ccrDom.createTextNode("URL"));
       
        var urlAttValue = ccrDom.createElementNS(CCR_NS,"AttributeValue");
        urlAtt.appendChild(urlAttValue);
        var urlAttValueValue = ccrDom.createElementNS(CCR_NS,"Value");
        urlAttValue.appendChild(urlAttValueValue);
        urlAttValueValue.appendChild(ccrDom.createTextNode("mcid://" + sha1));

        // Add display name
        var dnAtt = refLocDesc.appendChild(ccrDom.createElementNS(CCR_NS,"ObjectAttribute"));
        dnAtt.appendChild(ccrDom.createElementNS(CCR_NS,"Attribute")).appendChild(ccrDom.createTextNode("DisplayName"));
        var dnAttValue = ccrDom.createElementNS(CCR_NS,"AttributeValue");
        dnAtt.appendChild(dnAttValue);
        var dnAttValueValue= ccrDom.createElementNS(CCR_NS,"Value");
        dnAttValue.appendChild(dnAttValueValue);
        dnAttValueValue.appendChild(ccrDom.createTextNode(fileName));
      }
      catch(e) {
        if(e == sendCancelledError) throw sendCancelledError;
        mccxpAlert(e);
      }
    }
    xmlData += "\n</Files>\n";
    xmlData += "</CXP>";
    return xmlData;
  }
  catch(e) {
    if(e == sendCancelledError) throw sendCancelledError;
    mccxpAlert(e);
  }
}


function CCRNSResolver(prefix) {
  if(prefix == 'c') {
    return 'urn:astm-org:CCR';
  }
  else  {
    // shouldnt ever happen
    return null;
  }
}

var mccxp_xpath_last_ctx;
var mccxp_xpath_last_dom;

function MCXPath(path) {
  this.path = path;
  this.xpath = xpathParse(path);
}

MCXPath.prototype.eval = function(dom) {
  if(mccxp_xpath_last_dom != dom) {
    mccxp_xpath_last_ctx = new ExprContext(dom);
  }
  return this.xpath.evaluate(mccxp_xpath_last_ctx);
}

/**
 * Evaluates the expression and returns the first result converted
 * to a string value
 */
MCXPath.prototype.eval1= function(aNode) {
  var xpe = new XPathEvaluator();
  var result = xpe.evaluate(this.path, aNode, CCRNSResolver, 0, null);

  if(result.resultType == 4 /* Unordered collection */) {
    var res = result.iterateNext();
    if(res)
      return res.textContent;
    else
      return null;
  }
  else {
    return result.stringValue;
  }
}

/**
 * Evaluates the expression and returns the resultant nodeset as an
 * array of Nodes.
 */
MCXPath.prototype.evaln = function(aNode) {
  var xpe = new XPathEvaluator();
  var nsResolver = xpe.createNSResolver(aNode.ownerDocument == null ?
    aNode.documentElement : aNode.ownerDocument.documentElement);
  var result = xpe.evaluate(this.path, aNode, CCRNSResolver, 0, null);
  var found = [];
  while (res = result.iterateNext()) {
    found.push(res);
  }
  return found;
}

var xpaths = new Object();

// Note: there are 2 different xpaths libraries being used below.  One is Google AjaxXSLT.  This
// is a simplistic library and only works for XML without namespaces.  It should not be
// used for parsing CCRs.   However it is completely cross-browser because it is pure javascript.
//
// The other library is an internal one implemented (currently) as a thin wrapper around
// the Mozilla DOM parser.  It is encapsulated in an MCXPath() class defined above.  Note
// that the Mozillar DOM parser (MCXPath) requires the CCR namespace to be specified in 
// XPaths as 'c:'.  AjaxXSLT doesnt require this.
//
// Core CCR information
xpaths.ccrDate        = new MCXPath("/c:ContinuityOfCareRecord/c:DateTime");

xpaths.ccrExactDate = new MCXPath("/c:ContinuityOfCareRecord/c:DateTime/c:ExactDateTime");
xpaths.patientActor = "/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:Patient/c:ActorID]";
xpaths.patientName = new MCXPath("concat("+ xpaths.patientActor + "/c:Person/c:Name/c:CurrentName/c:Given,' ',"+ xpaths.patientActor + "/c:Person/c:Name/c:CurrentName/c:Family)");
xpaths.ccrToEmail = new MCXPath("/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:To/c:ActorLink[1]/c:ActorID]/c:EMail/c:Value");
xpaths.ccrToEmailMedCommons = new MCXPath('/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:To/c:ActorLink/c:ActorID and (c:InformationSystem/c:Name[string()="MedCommons Notification"])]/c:EMail/c:Value');
xpaths.ccrActorEmail = new MCXPath("c:EMail/c:Value");
xpaths.ccrToActor = new MCXPath("/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:To/c:ActorLink/c:ActorID]");
xpaths.ccrActorAge = new MCXPath("c:Person/c:DateOfBirth/c:Age/c:Value");
xpaths.ccrActorSex = new MCXPath("c:Person/c:Gender/c:Text");

//xpaths.ccrTo = new MCXPath("/c:ContinuityOfCareRecord/c:To");
//xpaths.ccrFromActor = new MCXPath("/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:From/c:ActorLink/c:ActorID]");

xpaths.ccrToMedCommonsActor =     new MCXPath('/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:To/c:ActorLink/c:ActorID and (c:InformationSystem/c:Name[string()="MedCommons Notification"])]');
xpaths.ccrFromMedCommonsActor =   new MCXPath('/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:From/c:ActorLink/c:ActorID and (c:InformationSystem/c:Name[string()="MedCommons Notification"])]');
xpaths.ccrFromMedCommonsActorID = new MCXPath('/c:ContinuityOfCareRecord/c:Actors/c:Actor[c:ActorObjectID=/c:ContinuityOfCareRecord/c:From/c:ActorLink/c:ActorID and (c:InformationSystem/c:Name[string()="MedCommons Notification"])]/c:ActorObjectID');
///ccr:ContinuityOfCareRecord/ccr:Actors[1]/ccr:Actor[6]/ccr:ActorObjectID[1]
xpaths.ccrPatient = new MCXPath('/c:ContinuityOfCareRecord/c:Patient');
xpaths.ccrTo = new MCXPath('/c:ContinuityOfCareRecord/c:To');
xpaths.ccrFrom = new MCXPath('/c:ContinuityOfCareRecord/c:From');
xpaths.ccrActors = new MCXPath('/c:ContinuityOfCareRecord/c:Actors');
xpaths.ccrDocumentObjectID= new MCXPath("/c:ContinuityOfCareRecord/c:CCRDocumentObjectID");
// References
xpaths.references = new MCXPath("/c:ContinuityOfCareRecord/c:References");
xpaths.referenceType = new MCXPath("/c:Type/c:Text");
xpaths.referenceLocation = new MCXPath("/c:Locations/c:Location/c:Description/c:ObjectAttribute[c:Attribute='FileName']/c:AttributeValue/c:Value");

xpaths.actors = new MCXPath("/c:ContinuityOfCareRecord/c:Actors");
xpaths.actorIDList = new MCXPath("/c:ContinuityOfCareRecord/c:Actors/c:Actor/c:ActorObjectID");
xpaths.getActorID = new MCXPath("string(/c:ActorObjectID)");
// CXP
xpaths.transferStatus = xpathParse("/CXP/Status");
xpaths.txId = xpathParse("/CXP/TXID");
xpaths.cxpGetResult = xpathParse("/CXP/Files/File/FileContents");
xpaths.uid = new MCXPath("/CXP/UID");
xpaths.reason = xpathParse("/CXP/Reason");

// History
xpaths.guid = xpathParse("/HistoryEntry/guid");
xpaths.date = xpathParse("/HistoryEntry/date");
xpaths.pin = xpathParse("/HistoryEntry/pin");
xpaths.dest = xpathParse("/HistoryEntry/dest");
xpaths.toEmail = xpathParse("/HistoryEntry/toEmail");
xpaths.tn = xpathParse("/HistoryEntry/tn");

/*
<ActorLink>
            <ActorID>AA0006</ActorID>
            <ActorRole>
                <Text>Notification </Text>
            </ActorRole>
        </ActorLink>
*/
function mccxp_makeActorLink(ccrDom, actorID,  description){
    actorNode = ccrDom.createElementNS(CCR_NS,"ActorLink"); 
    actorIDNode = ccrDom.createElementNS(CCR_NS,"ActorID");
    actorIDNode.appendChild(ccrDom.createTextNode(actorID));
    actorNode.appendChild(actorIDNode);
    
    actorRoleNode = ccrDom.createElementNS(CCR_NS,"ActorRole"); 
    textNode = ccrDom.createElementNS(CCR_NS,"Text"); 
    textNode.appendChild(ccrDom.createTextNode(description));
    actorRoleNode.appendChild(textNode);
    actorNode.appendChild(actorRoleNode);
    
    return(actorNode);
    }
    
/*
        + '<ActorObjectID>'
        + actorId
        +  '</ActorObjectID>'  
        + '<InformationSystem>'
        + '<Name>' + actorName + '</Name>'
        + '<Type>' + actorType + '</Type>'
        + '<Version> + mcxelv('version') + '</Version>
        + '</InformationSystem>'
        + '<Email><Value>' 
        + actorEmail 
        + '</Value></Email>'
        + '<Source><Actor><ActorID>'
        + actorId
        + '</ActorID></Actor></Source>'
        + '</Actor>';
        */
        
function mccxp_makeNotificationActor(ccrDom, actorID,  actorEmail){
    actorNode = ccrDom.createElementNS(CCR_NS,"Actor"); 
    actorIDNode = ccrDom.createElementNS(CCR_NS,"ActorObjectID");
    actorIDNode.appendChild(ccrDom.createTextNode(actorID));
    actorNode.appendChild(actorIDNode);
    
    infosystemNode = ccrDom.createElementNS(CCR_NS, "InformationSystem");
    nameNode = ccrDom.createElementNS(CCR_NS, "Name");
    nameNode.appendChild(ccrDom.createTextNode(medcommonsNotificationActorName));
    typeNode = ccrDom.createElementNS(CCR_NS, "Type");
    typeNode.appendChild(ccrDom.createTextNode(medcommonsNotificationActorType));
    versionNode =ccrDom.createElementNS(CCR_NS, "Version");
    versionNode.appendChild(ccrDom.createTextNode(mcxelv('version')));
    
    infosystemNode.appendChild(nameNode);
    infosystemNode.appendChild(typeNode);
    infosystemNode.appendChild(versionNode);
    actorNode.appendChild(infosystemNode);
    
    emailNode = ccrDom.createElementNS(CCR_NS, "EMail");
    valueNode = ccrDom.createElementNS(CCR_NS, "Value");
    emailNode.appendChild(valueNode);
    valueNode.appendChild(ccrDom.createTextNode(actorEmail));
    
    actorNode.appendChild(emailNode);
    
    sourceNode = ccrDom.createElementNS(CCR_NS, "Source");
    actorRefNode = ccrDom.createElementNS(CCR_NS, "Actor");
    sourceNode.appendChild(actorRefNode);
    actorRefIDNode = ccrDom.createElementNS(CCR_NS, "ActorID");
    actorRefNode.appendChild(actorRefIDNode);
    actorRefIDNode.appendChild(ccrDom.createTextNode(actorID));
    
    actorNode.appendChild(sourceNode);
    
 
   return(actorNode);
}

/*
 * Algorithm:
 * If there is an existing email notification node (as defined by the actorXPath)
 * then replace the <Email><Value> with the new setting.
 * If there is not an existing email notification node then it must be added.
 *
 * Note that there are two email notification nodes referred to by the <To> and <From>
 * ActorLinks. 
*/

function mccxp_insertNotification(ccrDom, actorXPath, rootXPath, emailValue, notificationType) {
    var actorResult = actorXPath.evaln(ccrDom);
    var actors = xpaths.ccrActors.evaln(ccrDom);
    
    if(!actorResult || (actorResult.length == 0)) { // Did not find existing MedCommons Notification Actor
        
        var rootPathResult = rootXPath.evaln(ccrDom);
        if (!rootPathResult || rootPathResult.length == 0){ // There is no <To> or <From> ActorLinks defined
            if (rootXPath == xpaths.ccrTo){
                var toNode = ccrDom.createElementNS(CCR_NS, 'To');
                var afterNode = xpaths.ccrFrom.evaln(ccrDom);
                //alert("Afternode is " + afterNode);
                if (!afterNode || afterNode.length ==0)
                    afterNode = xpaths.ccrPatient.evaln(ccrDom);
                insertAfter(afterNode[afterNode.length-1], toNode);
            
            }
            else if (rootXPath == xpaths.ccrFrom){
                var fromNode = ccrDom.createElementNS(CCR_NS, 'From');
                ccrDom.documentElement.appendChild(fromNode);
                
            }
            else{
                alert("Internal programming error: missing either <To> or <From> section");
                return(false);
            }
            rootPathResult = rootXPath.evaln(ccrDom);
            if (!rootPathResult)
                alert("RootPathResult is null after adding <To> or <From> section");
            }
        else{
            ;//alert("To or From node already exists: " + rootPathResult);
        }
        var actorID = generateUniqueActorObjectID()
        
        var notificationActor = mccxp_makeNotificationActor(ccrDom, actorID, emailValue );
        actors[0].appendChild(notificationActor);
        
        var actorLink = mccxp_makeActorLink(ccrDom, actorID, notificationType);
        rootPathResult[0].appendChild(actorLink);
     
    
    }
    else{ // MedCommons Notification Actor was found
        actorNode = actorResult[0];

        var emailResult = xpaths.ccrActorEmail.evaln(actorNode);
        if(emailResult && (emailResult.length > 0)) {
            // Remove old email value
            emailResult[0].removeChild(emailResult[0].childNodes[0]);

            // Add new Email Value
            emailResult[0].appendChild(ccrDom.createTextNode(emailValue));
            mccxpLog("Replaced email address in existing node");
        }
        else { // no nodes, add 0th node
            var emailNode = ccrDom.createElementNS(CCR_NS, 'EMail');
            var valueNode = ccrDom.createElementNS(CCR_NS, 'Value');
            valueNode.appendChild(ccrDom.createTextNode(emailValue));
            emailNode.appendChild(valueNode);
            actorNode.appendChild(emailNode); 
            mccxpLog("Replaced email address as new node");
        }
    }
    return true;
}

function mccxp_generatePin() {
  var pin = '';
  for(i=0; i<5; i++) {
    pin += Math.floor(Math.random()*10);
  }
  return pin;
}

function mccxp_generateClientId() {
  var clientId = '';
  for(i=0; i<16; i++) {
    clientId += Math.floor(Math.random()*10);
  }
  return clientId;
}

function mccxp_selectFile() {
  var fn = mccxp_browseFile();
  if(fn) {
    document.getElementById('filename').value = fn;
    mccxp_loadFileDetails();
    mccxp_updateSubject();
  }
}

function mccxp_browseFile() {
  var nsIFilePicker = Components.interfaces.nsIFilePicker;
  var fp = Components.classes["@mozilla.org/filepicker;1"]
          .createInstance(nsIFilePicker);
  fp.init(window, "Select a File", nsIFilePicker.modeOpen);
  var res = fp.show();
  if (res == nsIFilePicker.returnOK) {
    return fp.file.path;
  }
  return null;
}

function mccxp_selectSaveFile(fn) {
  var nsIFilePicker = Components.interfaces.nsIFilePicker;
  var fp = Components.classes["@mozilla.org/filepicker;1"]
          .createInstance(nsIFilePicker);
  fp.init(window, "Save As", nsIFilePicker.modeSave);
  
  fp.defaultString = fn;
  
  var res = fp.show();
  if ((res == nsIFilePicker.returnOK) || (res == nsIFilePicker.returnReplace)){
    return fp.file.path;
  }
  return null;
}

function mccxp_loadFileDetails() {

  var filePath = document.getElementById('filename').value;
  if(filePath.length == 0) {
    document.getElementById("ccrDate").value='';
    document.getElementById("patientName").value='';
    //document.getElementById("ccrSize").value='' = "";
    mcxel('ccrAgeSex').value='';
    document.getElementById("recipient").value='';
    return;
  }
  
  try {
    var ccrXml = readFile(filePath,true);
    var dom = mccxp_xmlParse(ccrXml);
    document.getElementById("ccrDate").value=strip(xpaths.ccrDate.eval1(dom));
    document.getElementById("patientName").value=xpaths.patientName.eval1(dom);
    //document.getElementById("ccrSize").value=ccrXml.length + " bytes";
    
    var patientXPath = new MCXPath(xpaths.patientActor);
    var patient = patientXPath.evaln(dom);
    if(patient.length > 0) {
      var ccrAge = xpaths.ccrActorAge.eval1(patient[0]);
      var ccrSex = xpaths.ccrActorSex.eval1(patient[0]);
      mcxel('ccrAgeSex').value=(ccrAge ? ccrAge : '') + ' / ' + (ccrSex ? ccrSex : '');
    }
    else 
      mcxel('ccrAgeSex').value='-';
      

    document.getElementById("recipient").value=xpaths.ccrToEmail.eval1(dom);
  }
  catch(e) {
    mccxpLog('Error reading previous file: ' + e);
  }
}

var mccxp_problemWindow;
function mccxp_reportProblem() {
    mccxp_problemWindow = window.open("chrome://mccxp/content/problem.xul","mccxp_problem","chrome,toolbar=no,resizable=yes,width=500,height=510");
}

function mccxp_problemInit() {
  try {
    var h = window.opener.mccxp_problem_history;
    if(h) {
      document.getElementById('trackingNumber').value=( (h.tn && h.tn != '') ? h.tn : h.guid );
      document.getElementById('pin').value=h.pin;
    }
    var act = document.getElementById('activityList');
    var log = window.opener.mccxp_log;
    for(i=0; i<log.length; i++) {
      var d = log[i].date;
      var ds = (d.getMonth()+1)+'/'+d.getDate()+'/'+d.getFullYear()+' '+d.getHours()+':'+ (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes());
      act.appendItem(ds + ' - ' + log[i].msg);
    }

    document.getElementById('problemDescription').select();
    document.getElementById('problemDescription').focus();
  }
  catch(e) {
    alert(e);
  }

}

/**
 * shortcut for 'document.getElementById'
 */
function mcxel(id) {
  return document.getElementById(id);
}

/**
 * shortcut for 'document.getElementById(id).value,
 * converts nulls to blanks
 */
function mcxelv(id) {
  var el = mcxel(id);
  return el ? (el.value ? el.value : '') : '';
}

function mccxp_uploadProblem() {

    /*
    var ccrData = '< data not available >';
    try {
      ccrData = readFile(document.getElementById('filename').value);
    }
    catch(e) {
      ccrData = '[ error while retrieving data: ' + e + ']';
    }
    */
  
    var req = getXMLHttpRequest();
        req.open("POST", "https://secure.medcommons.net/problem.php", true);
    req.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
    var xml = '<?xml version="1.0" encoding="UTF-8"?>'
        + '<problemreport><sender>'+mcxelv('email')+'</sender>\n'
        +  '<description>'+escape(mcxelv('problemDescription'))+'</description>\n'
        +  '<version>'+mcxelv('version')+'</version>\n'
        +  '<trackingnumber>'+mcxelv('trackingNumber')+'</trackingnumber>\n'
        +  '<pin>'+mcxelv('pin')+'</pin>\n'
        +  '<log>';

    if(document.getElementById('sendLog').checked) {
      var log = window.opener.mccxp_log;
      for(i=0; i<log.length; i++) {
        var d = log[i].date;
        var ds = d.getDate()+'/'+(d.getMonth()+1)+'/'+d.getFullYear()+' '+d.getHours()+':'+ (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes());
        xml += '<entry>'+escape(ds + '-' + log[i].msg) + '</entry>\n';
      }
    }
    xml += '</log></problemreport>';

    // debug 
    // note: don't leave this in, it breaks on Macs/Linux
    // mccxp_writeFile(xml, 'c:\\test.xml');

    try {
      var postData ='problemdata='+escape(xml);
      postData += '&trackingnumber='+escape(mcxelv('trackingNumber'));
      postData += '&pin='+escape(mcxelv('pin'));
      postData += '&version='+escape(mcxelv('version'));
      postData += '&email='+escape(mcxelv('email'));
      postData += '&useragent='+escape(navigator.userAgent);
      postData += '&description='+escape(mcxelv('problemDescription'));

      var deferred = MochiKit.Async.sendXMLHttpRequest(req,postData);    
      deferred.addCallback( function(req) { alert("Thank you for reporting your problem!"); setTimeout('window.close()',300); });
      deferred.addErrback( function(e) { alert("An error occurred sending your problem report.\n\n  " + e); setTimeout('window.close()',300); });
    }
    catch(e) {
      alert(e);
    }
}

function mccxp_deleteTrackingNumber() {
  window.open("chrome://mccxp/content/deletewarning.xul","mcdeletewarning","chrome,toolbar=no,resizable=yes,width=400,height=130,dialog,modal");
  if(!window.mccxp_dialog_result) {
    alert('Delete Cancelled!');
    return;
  }

  try {
    var historyTable = document.getElementById('historyTable');
    var h = mccxp_history[ mccxp_history.length - 1 - historyTable.selectedIndex ];
    var req = getXMLHttpRequest();
    mccxpLog("Sending Delete command to " + h.destUrl + " for guid " + h.guid);
    req.open("POST", h.destUrl, true);
    req.setRequestHeader("Content-Type","application/x-www-form-urlencoded");
    req.history = h;
    req.guid = h.guid;
    req.onSuccess=mccxp_getHistorySuccess;
    var queryXml = '<?xml version="1.0" encoding="UTF-8"?><CXP><OperationCode>DELETE</OperationCode>' 
      + cxpVersion + informationSystem + '"<QueryString>'+h.guid+'</QueryString><TXID>'+h.tn+'</TXID><PIN>'+h.pin+'</PIN></CXP>';
    var postData ='xmldata='+escape(queryXml);
    var deferred = MochiKit.Async.sendXMLHttpRequest(req,postData);    
    var err = function(e) { alert("DELETE failed:\n\n  " + e); };
    deferred.addCallback( mccxp_deleteSuccess );
    deferred.addErrback( err );
  }
  catch(e) {
    mccxpAlert(e);
  }
}

/**
 * General function for receiving CXP GET
 * Parses response and hands off to secondary handler
 */
function mccxp_deleteSuccess(req) {
  var dom = xmlParse(req.responseText);
  req.responseDom = dom;
  var ctx = new ExprContext(dom);
  var status = xpaths.transferStatus.evaluate(ctx).stringValue();
  var reason = xpaths.reason.evaluate(ctx).stringValue();
  var statusValue = parseInt(status);
  if(status >= 400) {
    alert('Tracking number ' + req.history.tn + ' could not be deleted.\r\n\r\nStatus:   ' + status + '\n\n\rReason:    ' + reason);
  }
  else {
    alert("Tracking number " + req.history.tn + " has been deleted.");
  }
}

function mccxp_updateTermsFlag() {
try {
  var prefService = Components.classes["@mozilla.org/preferences-service;1"].getService(Components.interfaces.nsIPrefService);
    var mccxpBranch = prefService.getBranch("mccxp.");
    mccxpBranch.setCharPref("mccxp-terms-of-use",mcxel('termsOfUse').checked);
    mccxpLog('saved terms of use ' + mcxel('termsOfUse').checked);
  }
  catch(e) {
    mccxpAlert(e);
  }
}

function mccxp_findMedCommonsWindow() {
  var wm = Components.classes["@mozilla.org/appshell/window-mediator;1"]
                     .getService(Components.interfaces.nsIWindowMediator);
  var enumerator = wm.getEnumerator("");
  while(enumerator.hasMoreElements()) {
    var win = enumerator.getNext();
    if(win.content) {
      if(win.content.medcommons_advertised_ownerId) {
        return win;
      }
    }
  }
}

function mccxp_formatDate(d) {
  try { 
    var ds = (d.getMonth()+1)+'/';
    if(d.getDate() < 10)
      ds += "0";

    ds += d.getDate()+'/'+d.getFullYear()+' '
      + d.getHours()+':'+ (d.getMinutes() < 10 ? '0' + d.getMinutes() : d.getMinutes())

    if(d.getMonth()<9) {
      ds = "0"+ds;
    }
  }
  catch(e) {
    mccxpAlert(e);
  }
  return ds;
}

function mccxp_pinInput(inp) {
  try {
    var newValue = '';
    for(i=0; i<inp.value.length; ++i) {
      if(i>4) {
        break;
      }

      if((inp.value.charCodeAt(i) < 48) || (inp.value.charCodeAt(i) > 57)) {
        continue;
      }
      newValue += inp.value[i];
    }

  }
  catch(e) {
    alert(e);
  }
  inp.value=newValue;
}

function mccxp_encode64(input) {
  const keyStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=";
  var output = "";
  var chr1, chr2, chr3 = "";
  var enc1, enc2, enc3, enc4 = "";
  var i = 0;
  do {
     chr1 = input.charCodeAt(i++);
     chr2 = input.charCodeAt(i++);
     chr3 = input.charCodeAt(i++);

     enc1 = chr1 >> 2;
     enc2 = ((chr1 & 3) << 4) | (chr2 >> 4);
     enc3 = ((chr2 & 15) << 2) | (chr3 >> 6);
     enc4 = chr3 & 63;

     if (isNaN(chr2)) {
        enc3 = enc4 = 64;
     } else if (isNaN(chr3)) {
        enc4 = 64;
     }

     output = output + 
        keyStr.charAt(enc1) + 
        keyStr.charAt(enc2) + 
        keyStr.charAt(enc3) + 
        keyStr.charAt(enc4);
     chr1 = chr2 = chr3 = "";
     enc1 = enc2 = enc3 = enc4 = "";
  } while (i < input.length);

  return output;
}


function readFile(str_Filename,ignoreError) { 
/*
  // This code reads text files but will NOT successfully read
  // binary files

  try{
     var obj_File = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
     obj_File.initWithPath(str_Filename);

     var obj_InputStream =  Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance(Components.interfaces.nsIFileInputStream);
     obj_InputStream.init(obj_File,0x01,0444,null);

     var obj_ScriptableIO = Components.classes["@mozilla.org/scriptableinputstream;1"].createInstance(Components.interfaces.nsIScriptableInputStream);
     obj_ScriptableIO.init(obj_InputStream);
   } 
   catch (e) { 
    mccxpAlert(e); 
   }

  try {
    var str = obj_ScriptableIO.read(obj_File.fileSize);
    obj_ScriptableIO.close();
    obj_InputStream.close();
    return str;
  } 
  catch (e) { 
    mccxp_dump(e); 
  }
*/

  // This code reads binary files
  try {
    var files = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
    files.initWithPath( str_Filename );
    if ( files.exists() == false ) { 
      if(!ignoreError) {
        alert("File does not exist"); 
      }
      return null;
    }

    var is = Components.classes["@mozilla.org/network/file-input-stream;1"].createInstance( Components.interfaces.nsIFileInputStream );
    is.init( files, 0x01, 00004, null);

    var sis = Components.classes["@mozilla.org/binaryinputstream;1"].createInstance( Components.interfaces.nsIBinaryInputStream );
    sis.setInputStream( is );
    var output = sis.readBytes ( sis.available() );
    return output;
   } 
   catch (e) { 
     if(!ignoreError)
      mccxpAlert(e); 
     return null;
   }

/*
  // This code reads binary files using JSLib - it's easy 
  // but we don't want the user to have to install JSLib
  var file = new File(str_Filename);
  file.open();
  return file.read();
*/
}



function mccxp_writeFile(str_Buffer,str_Filename)
{
    try{
        var obj_File = Components.classes["@mozilla.org/file/local;1"].createInstance(Components.interfaces.nsILocalFile);
        obj_File.initWithPath(str_Filename);
        if(obj_File.exists())
          obj_File.remove( false );

         obj_File.create(0x00,0644);
    } 
    catch (e) { 
      mccxpAlert(e); 
    }
    try {
        var obj_Transport = Components.classes["@mozilla.org/network/file-output-stream;1"].createInstance(Components.interfaces.nsIFileOutputStream);
        obj_Transport.init( obj_File, 0x04 | 0x08 | 0x10, 064, 0 );
        obj_Transport.write(str_Buffer,str_Buffer.length);
        obj_Transport.close();
    } 
    catch (e) {
        mccxpAlert(e);
    }
}



function mccxp_debug() {
  try {
    window.eval(mcxel('debug').value);
  }
  catch(e) {
    mccxp_dump(e);
  }

}

function mccxp_print() {

  // Update the data in the print window
  var fields = [ 'patientName', 'ccrDate', 'assignedPin','recipient' ];
  forEach(fields, function(f) {
    if($(f) &&  $('printwindow').contentDocument.getElementById(f)) {
      $('printwindow').contentDocument.getElementById(f).innerHTML = $(f).value;
    }
  });
  $('printwindow').contentDocument.getElementById('agreeRegistry').innerHTML=$('agreeRegistry').checked? "Yes" : "No";
  $('printwindow').contentDocument.getElementById('logo').src = $('logo').src;

  try {    
    //$('printwindow').style.display = 'block';
    var ifreq = mcxel('printwindow').contentWindow.QueryInterface(Components.interfaces.nsIInterfaceRequestor);
    var webBrowserPrint = ifreq.getInterface(Components.interfaces.nsIWebBrowserPrint);
    var gPrintSettings = webBrowserPrint.globalPrintSettings;
    gPrintSettings.printSilent = true;
    gPrintSettings.title = '';
    gPrintSettings.docURL = '';
    gPrintSettings.headerStrLeft = '';
    gPrintSettings.headerStrRight = '';
    gPrintSettings.headerStrCenter = '';
    gPrintSettings.footerStrLeft = '';
    gPrintSettings.footerStrCenter = '';
    gPrintSettings.footerStrRight = '';
    gPrintSettings.marginTop = 0.0;
    gPrintSettings.marginLeft = 0.0;
    gPrintSettings.marginRight = 0.0;
    webBrowserPrint.print(gPrintSettings, null);    
  } 
  catch(e) {    
    mccxp_dump(e);  
  }

}


var maxActorIDIndex = Math.pow(2,32);
var maxDocumentIDIndex = Math.pow(2,64);

function generateUniqueActorObjectID(){
    var r = Math.round(Math.random() * maxActorIDIndex);
    return("AA" + r);
}
function generateUniqueDocumentObjectID(){
    var r = Math.round(Math.random() * maxDocumentIDIndex);
    return("AA" + r);
}

/*
* For some reason the standard DOM API doesn't have an 'insertAfter' method
* (although there is an 'insertBefore'). 
*/
function insertAfter(currentElement,newElement) { 
         var nextElement; 
         if (nextElement = currentElement.nextSibling) 
                 currentElement.parentNode.insertBefore(newElement, nextElement); 
         else 
                 currentElement.parentNode.appendChild(newElement); 
     } 

function updateCcrDocumentObjectID(ccrDom){
    var newObjectID = generateUniqueDocumentObjectID();
    //alert (newObjectID);
    var ccrDocumentObjectID = xpaths.ccrDocumentObjectID.evaln(ccrDom);
    //alert(ccrDocumentObjectID);
    try{
    ccrDocumentObjectID[0].removeChild(ccrDocumentObjectID[0].childNodes[0]);
    }
    catch(e)
    {
    }
    ccrDocumentObjectID[0].appendChild(ccrDom.createTextNode(newObjectID));
}
function updateDateTime(ccrDom){
    var newDate = iso8601Date(new Date());
    //alert(newDate);
    var dateNode = xpaths.ccrExactDate.evaln(ccrDom);
    //alert(dateNode);
    dateNode[0].removeChild(dateNode[0].childNodes[0]);
    dateNode[0].appendChild(ccrDom.createTextNode(newDate));
    
}
//2004-01-12T13:30:00-05:00
function iso8601Date(date){
    var y, m, d, h,min,s;
    y=date.getFullYear();
    m=date.getMonth()+1;
    d=date.getUTCDate();
    h= date.getUTCHours();
    min = date.getUTCMinutes();
    s = date.getUTCSeconds();
    m=(m < 10) ? "0"+m : m;
    d=(d < 10) ? "0"+d : d;
    h=(h<10) ? "0" + h : h;
    min=(min<10) ? "0" + min : min;
    s = (s<10) ? "0" + s: s;
    
    return y+"-"+m+"-"+d + "T" + h + ":" + min + ":" + s + ".0Z";
}

/**
 * Creates an alert box containing the properties of any JavaScript object.
 */
function mccxp_dump(description, obj){
  //alert(mccxp_stacktrace());
  if(!obj) {
    obj = description;
    description = '';
  }

  var display = "Dumping properties ";
  display+= description;
  display+= "\n";
  for (var name in obj) {
      display +=name;
      display += ":";
      display += obj[name];
      display += "\n";
      }
  alert(display);
}


function mccxp_stacktrace() {
 var s = "";
 for (var a = arguments.caller; a !=null; a = a.caller) {
   s += "->"+funcname(a.callee) + "\n";
   if (a.caller == a) {s+="*"; break;}
 }
 return s;
}

