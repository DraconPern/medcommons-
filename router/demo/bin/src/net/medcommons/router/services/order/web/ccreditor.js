/**
 * MedCommons CCR Viewer/Editor Support Functions.
 *
 * Copyright 2006 - 2007 MedCommons Inc.   All Rights Reserved.
 *
 * @author ssadedin@medcommons.net
 */

function updatePin() {
  var pin = '';
  forEach($('pinFields').getElementsByTagName('input'), function(t) { pin += t.value; });
  el('assignedPin').value = pin;
  return pin;
}

function checkPin() {
  var pin = updatePin();

  var ifInvalid=function() {
    el('pinFields').style.borderStyle='solid';
    el('pinFields').style.borderColor='red';
    el('pin0').focus();
    el('pin0').select();
  }

  if($('destAcctId').value.match(/[0-9]{16}/)) {
    if((pin != '') && (pin != pin.match(/[0-9]{5}/))) {
      alert('Your assigned PIN is not valid.  Please enter a 5 digit number for your PIN.');
      ifInvalid();
      return false;
    }
    return true;
  }

  if((pin == null) || (pin != pin.match(/[0-9]{5}/))) {
    if(pin == '') {
      // alert('Please enter a 5 digit number for your PIN.');
      if(confirm('You have specified one or more recipients who are not MedCommons Account holders.\n\n'
           +'If you do not enter a PIN, the recipient(s) will have to register to receive this CCR.\n\n'
           +'Press OK to continue without a PIN or Cancel to return and edit the PIN'))
        return true;
    }
    else
      alert('Your assigned PIN is not valid.  Please enter a 5 digit number for your PIN.');

    ifInvalid();
    return false;
  }
  return true;
}

function nextPinField() {
  // focus next field
  var fields = $('pinFields').getElementsByTagName('input');
  var index = findIdentical(fields,this);
  if((index >= 0) && (index < fields.length-1)) {
    fields[index+1].select();
    fields[index+1].focus();
  }
  else 
  if(index == fields.length - 1) {
    document.referralForm.ccrPurpose.select();
    document.referralForm.ccrPurpose.focus();
  }
  else {
    log("Pin field " + index + " not found or out of range");
  }
}

function fill_ccr_purpose(i,r) {
  auto_complete_fill_value(i,r);
  var p = $('ccrPurpose').value;
  $('ccrPurpose').value = p.replace("[Recipient]",$('toEmail').value); 
}

function onPinKeyDown(e) {
  var code = e.key().code;
  if((48<=code) && (58>code)) { 
    this.value = String.fromCharCode(code);
    nextPinField.apply(this);
    e.preventDefault();
    e.stopPropagation();
  }
  else
  if(findValue([9], e.key().code)>=0) {
  }
  else {
    e.preventDefault();
    e.stopPropagation();
  }    
}

function initPinFields() {
  forEach($('pinFields').getElementsByTagName('input'), function(f) {
    connect(f,'onkeydown', onPinKeyDown);
    connect(f,'onfocus', function() { this.select(); } );
    connect(f,'onblur', updatePin);
  });
}

function privacyUnhighlight() {
  removeElementClass(this,'privacyHighlight');
  hide('privacyWarning');
}

function privacyHighlight(field) {
  highlight(field);
  addElementClass(field,'privacyHighlight');
  connect(field,'onblur',field,privacyUnhighlight);
  show('privacyWarning');
}

function unhighlight() {
  removeElementClass(this,'highlighted');
  syncAgeSex();
}

function highlight(field) {
  if(field.getAttribute('readonly')) {
    return;
  }
  if(field.select != null)
    field.select();
  field.onblur=unhighlight;
  addElementClass(field,'highlighted');
}

function checkNotFixed() {
  if(storageMode == 'FIXED') {
    if(confirm('You cannot modify this CCR because it has already been Saved.\r\n\r\nDo you want to edit this CCR as a New CCR?')) {
      document.referralForm.restore.value='true';
      sendAsNew();
    }
    return false;
  }
  return true;
}

function onFieldChange(evt) {
  if(!checkNotFixed()) 
    return;

  syncAgeSex();
  if(ccrGuid) {
    if(displayMode != "eccr" && $('trackingNumber')) {
      $('trackingNumber').innerHTML='New # will be Created';
    }
    ccrGuid = null;
  }

  setModified();
}

if(window.parent!=null) {
  if(window.parent.showToolPalette != null) {
    window.parent.showToolPalette();
  }
}



function syncAgeSex() {
  var f = document.referralForm;
  if(f.patientGender.value.match(/^male$/i))
    f.patientGender.value = 'Male';
  else
  if(f.patientGender.value.match(/^female$/i))
    f.patientGender.value = 'Female';
  else
    f.patientGender.value = 'Unknown';

  f.ageSex.value = ageSex(f.patientAge.value,f.patientGender.value);
}

function headerSync() {
  syncAgeSex();

  // We also synch the date of birth, if we can
  var f = document.referralForm;
  var dob = f.patientDateOfBirth.value;
  var d = null;
  if(dob.match(/^[0-9]{4}-[0-9]{1,2}-[0-9]{1,2}/)) {
    d = isoTimestamp(dob);
  }
  else   
  if(dob.match(/^[0-9]{1,2}\/[0-9]{1,2}\/[0-9]{4}/)) { // Couldn't parse iso date, try american form
    d = americanDate(dob);
  }
  else { // Hmph.  See if Date can parse it
    /*var autoParse = new Date(dob);
    if(autoParse != "NaN")
      d = autoParse;
      */
  }

  f.headerDob.value= d ? toAmericanDate(d) : '';
  return true;
}

/**************************************
 * Support for basic Save / Send
 */
function saveCcr() {
  if(empty(accId) && !checkPin()) {
    return;
  }

  if(displayMode=="eccr") {
    submitSave(saveEmergencyCcr);
    document.referralForm.newAcct.value='false';
  }
  else {
    if(registry && (document.referralForm.newAcct.value=='true')) {
      newAccountDlg(function() { submitSave(saveFinished); });
      return;
    }
    else
      document.referralForm.newAcct.value='false';
    submitSave(saveFinished);
  }
}

function submitSave(callback) {
  updatePin();
  window.referralComplete = callback;
  window.document.referralForm.target = 'resultsframe';
  window.document.referralForm.action = 'SaveCCR.action?_sourcePage=sendResult.jsp';
  window.document.referralForm.submit();
}

function saveFinished(statusForm) {
  if(!checkSaveStatus(statusForm)) {
    return false;
  }

  storageMode = statusForm.storageMode.value;

  // calculate 24 hours from now
  var expires = new Date( (new Date()).getTime() + (24 * 60 * 60 * 1000) );
  var savedtn = window.resultsframe.document.results.trackingNumber.value;
  setCookie('savedtn',savedtn,expires);
  if(displayMode != 'eccr') {
    if($('trackingNumber'))
      $('trackingNumber').innerHTML = savedtn;
  }
  resetTabText(statusForm);
  ccrCreateDateTime=statusForm.createDateTime.value;

  var r = document.referralForm;
  setPatientTitle( r.patientGivenName.value, r.patientFamilyName.value);

  if(statusForm.acctId) {
    patientMedCommonsId = statusForm.acctId.value;
    addNewId({type:"MedCommons Account Id",value: statusForm.acctId.value});
  }

  if(registry)
    fetchRegistryPatients();
  

  updateTabType(statusForm);

  ce_signal('openCCRUpdated',ccrGuid);
  window.parent.signal(window.parent.events,'openCCRUpdated',ccrGuid,getCcr());
}

function onSwitchTab(url) {
  if(!checkDirtySection()) {
    log("user cancelled in dirty section check");
    return false;
  }
  document.referralForm.action=url;
  document.referralForm.target='';
  document.referralForm.submit();
  return true;
}

function onParentSize() {
  updateCCRFrameSize();
}

function updateCCRFrameSize() {
  var pos = elementPosition('ccrframe');
  var h = viewportSize().h - 20;
  var ccrHeight = h - pos.y;
  if(ccrHeight < 40) // Minimum height 
    ccrHeight = 40;
  $('ccrframe').style.height = ccrHeight+'px';
  $('ccrframe').height = (h - pos.y);
}

function updateCCRFrame(section) {
  // Reload CCR Frame
  doSimpleXMLHttpRequest("DisplayCCR.action", {ccrIndex: parent.currentTab.ccrIndex}).addCallbacks(function(r) {
    window.ccrHTML = r.responseText.substring(r.responseText.indexOf('<body>')+7, r.responseText.indexOf('</body>'));
    window.ccrframe.document.body.innerHTML = '';
    window.setTimeout(function() {window.ccrframe.document.body.innerHTML = window.ccrHTML;addCCRTools(ccrframe); if(section) highlightSection(section);},0);
  },genericErrorHandler);
}

function goDesktop() {
  document.referralForm.action='updateCcr.do?forward=desktop';
  document.referralForm.target='';
  document.referralForm.submit();
}

 function goViewer(initialSeriesGuid,initialSeriesIndex) {
  document.referralForm.action='updateCcr.do?forward=viewer&mode=view';
  document.referralForm.initialSeriesGuid.value=initialSeriesGuid;
  document.referralForm.initialSeriesIndex.value=initialSeriesIndex;
  document.referralForm.target='';
  document.referralForm.submit();
}

function checkSaveStatus(statusForm) {
  var ves = window.parent.validationErrors ? window.parent.validationErrors : window.validationErrors;
  // First check validation
  if(ves && (ves.length>0)) {
    forEach(ves, function(field) {
      var v = eval('document.referralForm.'+field);
      if(v) {
        v.style.borderStyle='solid';
        v.style.borderColor='red';
        v.focus();
      }
    });
    alert('One or more fields that you submitted contained invalid data. Please check the highlighted fields and try again.');
    return false;
  }

  var status = statusForm.status.value;
  ccrGuid = window.resultsframe.document.results.guid.value;
  if(status == 'INSUFFICIENTCREDIT') {
    var evt = { quantity: 1, type: 'NEW_ACCOUNT' };
    noCreditDlg(evt, window.resultsframe.counters);
    return false;
  }
  if(status == 'FAILED') {
    window.document.referralForm.action = 'confirmReferral.do';
    alert('A problem occurred while attempting to save your CCR.\r\n\r\n'+
        'Error: ' + statusForm.error.value);
    return false;
  }
  return true;
}


/**
 * Create an object with essential attributes of the displayed CCR
 */
function getCcr() {
  var r = document.referralForm;
  var c =  {
    guid: ccrGuid,
    logicalType: parent.currentTab.ccr.documentType,
    createDateTime: ccrCreateDateTime, 
    patient: {
      medCommonsId: patientMedCommonsId,
      givenName: r.patientGivenName.value, 
      middleName: r.patientMiddleName.value, 
      familyName: r.patientFamilyName.value,
      ageSex: r.ageSex.value,
      dateOfBirth: r.headerDob.value,
      ids: patientIds
    }
  };
  return c;
}

function saveEmergencyCcr(statusForm) {
  if(!checkSaveStatus(statusForm)) {
    return false;
  }
  var savedtn = window.resultsframe.document.results.trackingNumber.value;
  window.parent.setTabText(window.parent.currentTab, 'Emergency CCR');
  var ccrIndex = window.parent.currentTab.ccrIndex; 
  var deferred = doSimpleXMLHttpRequest("/router/setEmergencyCCR.do", {ccrIndex: ccrIndex});
  deferred.addCallbacks(saveEmergencyCCRSuccess, genericErrorHandler);
}

function saveEmergencyCCRSuccess() {
  alert("Your Emergency CCR has been saved.");
}

function sendAsNew() {
  if(!checkNewCCR()) 
    return;

  // Create a new tab
  var url = 'viewEditCCR.do';
  var tab = window.parent.addTab('New CCR',url,'',window.parent.nextTab());
  tab.documentType = 'NEWCCR';
  window.parent.highlightTab(tab);
  submitAction('EditAsNew.action');
}

function saveAsFixed() {
  // Create a new tab
  var url = 'viewEditCCR.do';
  window.parent.addTab(formatTabDate(new Date()),url,'',window.parent.nextTab());
  window.parent.highlightTab(window.parent.nextTab());
  updatePin();
  submitAction('SaveAsFixed.action');
}


function newTrackingNumberSuccess(succ) {
  try {
    log(succ.responseText);
    eval('ntRes='+succ.responseText);

    if(ntRes.status != 'ok') {
      throw new GenericError(ntRes.error);
    }

    // set the track# in display 
    var tn = ntRes.tn.substr(0,4) + ' ' + ntRes.tn.substr(4,4) + ' ' + ntRes.tn.substr(8,4);
    if($('trackingNumber'))
      $('trackingNumber').innerHTML = tn;

    // update to new ccr index 
    window.parent.currentTab.ccrIndex = ntRes.ccrIndex;
    document.referralForm.updateIndex.value=ntRes.ccrIndex;

    // blank guid because we now have a new tn 
    ccrGuid = null;

  }
  catch(e) {
    var val = 'A problem occured allocating a new tracking number for your CCR:\r\n\r\n';
    for(i in e) {
      val += i + ' : '+eval('e.'+i);
      val += '\r\n';
    }
    alert(val);

    // Abort the call chain - prevent recursive calls, eg. via showAddDocument
    return new Deferred();
  }
}

function newTrackingNumberFail(err) {
  alert('A problem occurred in creating a new tracking number.\n\nStatus:\t' +
    + err.req.status + '\n\nReason:\t' + err.req.statusText );

  // Simple way to abort call chain
  // Need to prevent recursive calls - eg. in showAddDocument
  return new Deferred();
}

function sendTrackingNumberSuccess(succ) {
  newTrackingNumberSuccess(succ);
  // retry send because tracking numbers are only created using this method as a prelude to a send 
  goReferralEmail();
}

/**
 * Shows a frame to allow user to add a document to the order/series for the current CCR
 */
function showAddDocument(req) {

  // Check that the CCR is not already fixed content, if so, must create new
  if(ccrGuid != null) {
    var ccrIndex = window.parent.currentTab.ccrIndex;
    var deferred = doSimpleXMLHttpRequest("newTrackingNumber.do?ccrIndex="+ccrIndex);
    // update gui with new track#
    deferred.addCallbacks(newTrackingNumberSuccess, newTrackingNumberFail);

    // if successful, retry
    deferred.addCallback(showAddDocument);

    // wait for new tn before continuing 
    return;
  }

  if(window.addDocumentWindow) {
    window.addDocumentWindow.focus();
    window.addDocumentWindow = null;
  }

  document.referralForm.action='updateCcr.do?forward=showAddDocument';
  document.referralForm.target = 'addDocument';
  document.referralForm.submit();
}

function downloadDicomReference(){
  if(!patientMedCommonsId || (patientMedCommonsId == null)) {
    // TODO: offer login 
    alert("No Patient Account ID is active");
  }
  var storageId = removeSpaces(patientMedCommonsId);
  if (ccrGuid == null){
    alert("This CCR must be saved before you can download DICOM.  Please Save the CCR and try again.");
  } 
  else {
     clearAuthorizationContext();
    downloadDocumentAttachments (storageId,ccrGuid, cxp2Protocol, cxp2Host, cxp2Port, cxp2Path, groupName,groupId, accId, auth);
    
  }
}

function mergeIntoCurrentCCR() {
  addElementClass($('mgDesc').parentNode,'invisible');
  $('mgStatus').innerHTML = 'MERGING';
  show('mergeStatusDiv');
  execJSONRequest('MergeWithCurrentCCR.action', queryString(document.referralForm), function(result) {
    if(result.status == "ok") {
      $('mgStatus').innerHTML='SUCCESS';
    }
    else {
      $('mgStatus').innerHTML='FAILED';
      $('mgDesc').innerHTML=result.error?result.error : 'None';
      removeElementClass($('mgDesc').parentNode,'invisible');
    }
  });
}

function addToAccount(statusForm) {
  // statusForm is set if the user chose to save and this function was called as a result of the save operation
  if(statusForm) {
    if(!checkSaveStatus(statusForm)) {
      return false;
    }
    alert("The CCR has been saved and added to " + (registry ? "'"+groupName+"' Worklist" : "Account"));
    removeAddToAccountTool();
    return; // Saving adds to the account anyway, so no need to do anything else
  }

  if(!accId || (accId == null)) {
    // TODO: offer login
    alert("You must be logged in to add a CCR to your account");
  }

  // cannot add unsaved CCR to an account 
  if(ccrGuid == null) {
    if(confirm("This CCR is unsaved. You must save it before you can add it to your account.\r\n\r\nDo you want to save and continue?")) {
      submitSave(addToAccount);
    }
    return;
  }

  // don't pester them again if they already clicked a confirm to save
  if(!statusForm && !window.confirmPinChange) {
    var msg = "Add this CCR to your Account #"+prettyAcctId(accId) + "?";
    var header = msg;
    if(registry) {
      msg = "Add this Patient to '" + groupName + "' Worklist?";
      header = msg;
    }
    else
    if(accountSettings.vouchersEnabled) {
      header = "Add this patient to your Patient List?" 
      msg = "<p>Please confirm that you want to add this patient to your Patient List:</p>"
      + "<table class='dialogTable' style='width 70%; margin:10px 15%;'><th>Your account</th><td>" + prettyAcctId(accId)+ "</td></tr>" 
          + "<tr><th>Your Email</th><td>"+htmlentities(accountSettings.email)+"</td></tr>"
          + "<tr><th colspan='2'>&nbsp;</th></tr>" 
          + "<tr><th>Patient Name</th><td>"+htmlentities(document.referralForm.patientGivenName.value + " " + document.referralForm.patientFamilyName.value)+"</td></tr>"
          + "<tr><th>Patient Account</th><td>"+prettyAcctId(patientSettings.accountId)+"</td></tr>"
          + "<tr><th>Patient Email</th><td>"+(patientSettings.email?(htmlentities(patientSettings.email)):"N/A")+"</td></tr>"
          + "</table>";
    }

    dialog('confirmAddToAccountDlg', header, msg, 450, [ {text:'Add to Patient List', handler: function() {
      var url ="AddToAccount.action";
      var params = "ccrIndex="+parent.currentTab.ccrIndex+'&' +queryString(document.referralForm);
      if(window.confirmPinChange) {
        url += "&pinChangeConfirmed=true";
        window.confirmPinChange = false;
      }
      execJSONRequest(url, params, addToAccountSuccess);
      this.destroy(); 
      if(accountSettings.vouchersEnabled)
        ce_signal('voucher_change_state');
    }},
    {text:"Cancel",handler: function() { this.destroy(); }}]
    );
  }

}

function viewCcr() {
  if(!checkDirtySection())
    return;

  window.parent.currentTab.mode = 'view';
  document.referralForm.action='updateCcr.do?forward=transaction&ccrIndex='+window.parent.currentTab.ccrIndex+'&mode=view';
  document.referralForm.target = '';
  document.referralForm.submit();
}

function waitForAttachments() {
  execJSONRequest('ListenForAttachments.action',{ccrIndex:parent.currentTab.ccrIndex,guid:ccrGuid},function(result) {
      if(result.result == "found") {
        alert('This CCR has been updated.  Click OK to reload your view');
        // Force reload of window in firefox. parent.location.reload() appears to have some other side effects
        // (such as invoking addDicomReference() a second time).
        parent.location.href = parent.location.href;
      }
      else
      if(result.result == "timeout") {
        log("attachments timed out -> rescheduling attachments listener ...");
        setTimeout(waitForAttachments,1000);
      }
      else {
        alert('A problem occurred while waiting for your DICOM attachments to arrive.');
      }
  });
}

function addDicomReference(){

  if(!patientMedCommonsId || (patientMedCommonsId == null)) {
      // TODO: offer login
      alert("No Patient Account ID is active");
  }

  var storageId = removeSpaces(patientMedCommonsId);
  if (ccrGuid == null) {
    alert("You must 'Save' the CCR before adding DICOM");
  }
  else {
    var evt = { quantity: 1, type: 'DICOM' };
    paymentRequiredDlg(evt, function() {
      execJSONRequest('Billing.action',{ccrIndex: parent.currentTab.ccrIndex,type:'DICOM',count:1},function(result) {
        window.result = result;
        if(result.status == "ok") {
          if(result.credit == "ok") {
          	clearAuthorizationContext();
            setDocumentFocus(storageId, ccrGuid, cxp2Protocol, cxp2Host, cxp2Port, cxp2Path, groupName,groupId, accId, auth,'NOBROWSER');
            waitForAttachments();
          }
          else {
            noCreditDlg(evt, result.counters);
          }
        }
        else {
          alert("An error occurred while validating your account's credit.\r\n\r\n"+result.error);
        }
      });
    });
  }
}

function viewCcr() {
  if(!checkDirtySection())
    return;

  window.parent.currentTab.mode = 'view';
  document.referralForm.action='updateCcr.do?forward=transaction&ccrIndex='+window.parent.currentTab.ccrIndex+'&mode=view';
  document.referralForm.target = '';
  document.referralForm.submit();
}

 

function addContextManager(tools) {
    if(!window.isContextManagerAvailable) {
      tools.push([CCR_ATTACHMENTS_MENU,"Add DICOM","addDicomReference();"]);
      tools.push([CCR_ATTACHMENTS_MENU,"Download DICOM","downloadDicomReference();"]);
      window.isContextManagerAvailable=true;
    }
}

function createReplyCCR() {
  var ccrIndex = window.parent.currentTab.ccrIndex;
  var url='createReplyCCR.do?mode=edit&ccrIndex='+ccrIndex;
  var tab = window.parent.addTab("Reply CCR *",url,"Reply CCR",window.parent.currentTab);
  window.parent.showTab(tab)
}

function newAccountDlg(okHandler) {
  if(!empty(patientMedCommonsId)) {
    okHandler();
    return;
  }

  dialog('newacctd','Create New Patient Account?', 
         '<p>Do you want to create a new account for this patient?</p>',
        400,
        [ { text: 'Create Account', handler: function() { okHandler(); this.destroy(); } }, 
        { text: 'Cancel', handler: function() { this.destroy(); } } ]);
}

function goReferralEmail() {
  // Check Email Address is supplied 
  var toEmail = document.getElementById('toEmail');
  if(trim(toEmail.value)=='') {
    toEmail.style.borderStyle='solid';
    toEmail.style.borderColor='red';
    toEmail.focus();
    alert('Please enter an Email address to notify!');
    return;
  }
  toEmail.style.borderStyle='none';
  toEmail.style.borderColor='black';

  // Check attachments 
  var pin = el('assignedPin').value;

  // Check PIN is valid 
  if(!checkPin())
    return;

  el('assignedPin').style.borderStyle='none';

  // if the assigned PIN is inside the subject, warn the user 
  if((pin!='') && (el('ccrPurpose').value.indexOf(pin) >= 0)) {
    if(!confirm('Warning!\n\nYou are sending the PIN in an insecure manner.\n\nPlease acknowledge that the contents are demo data with no patient\nidentifiable information.')) {
      return;
    }
  }

  // check the terms of use checkbox is checked 
  if(!checkTermsOfUse())
    return;

  // Check that the CCR is not already fixed content 
  if(ccrGuid != null) {
    var deferred = doSimpleXMLHttpRequest("newTrackingNumber.do?ccrIndex="+window.parent.currentTab.ccrIndex);
    deferred.addCallbacks(sendTrackingNumberSuccess, newTrackingNumberFail);
    // wait for new tn before continuing 
    return;
  }

  if(registry && (document.referralForm.newAcct.value=='true')) {
    newAccountDlg(function() { submitSaveAction('SENDING', 'confirmReferral.do', sendFinished); });
    return;
  }
  else
    document.referralForm.newAcct.value='false';

  submitSaveAction('SENDING', 'confirmReferral.do', sendFinished);
}


/**
 * Opens URL in such a way as to ensure that the existing CCR
 * is updated with any changes the user has made.
 */
function submitAction(actionUrl) {
  document.referralForm.action = actionUrl;
  document.referralForm.target = '';
  document.referralForm.submit();
}

/**************************************
 * Support for Web References
 */
function addWebReference() {
  show('addWebReferenceDiv');
  document.addWebReferenceForm.webRefUrl.focus();
  document.addWebReferenceForm.webRefUrl.select();
}

function addWebRefCancel() {
  hide('addWebReferenceDiv');
}

function addWebRefSubmit() {
  var url = 'addWebReference.do?ccrIndex='+window.parent.currentTab.ccrIndex+'&'+queryString(document.addWebReferenceForm);
  log('adWebRefSubmit loading url ' + url);
  loadJSONDoc(url).addCallbacks(addWebRefSuccess,genericErrorHandler);
}

function addWebRefSuccess(result) {
  if(result.status == "ok") {
    refreshPage();
    /*var cnt = 1;
    nodeWalk($('referencesTableBody'), function(n) { alert(n.tagName); if(n.tagName.toUpperCase() == 'TR') cnt++; return n.childNodes; }); 
    if(cnt == 1) {
      alert(1);
      refreshPage();
    }
    else {
      appendChildNodes($('referencesTableBody'),
        TR(null,TD({'class':'referenceIndexCell'},cnt),
                TD(null,A({href:result.url},result.url)),
                TD(null,'URL'),
                TD(null,' '),
                TD(null,A({href:'javascript:deleteSeries('+cnt+')'},IMG({src:'images/delete_reference.png',border:'0'})) )));
    }
    */
  }
  else
    alert("A problem occurred while adding the Web Reference:" + result.error);
  hide('addWebReferenceDiv');
}

function refreshPage() {
  document.referralForm.action='updateCcr.do?forward=transaction&mode=edit';
  document.referralForm.target='';
  document.referralForm.submit();
}

function deleteSeries(i) {
  window.referralComplete = function(results) {
    if(results.status.value == "ok") {
      el('referencesTable').innerHTML = resultsframe.document.getElementById('referencesTable').innerHTML;
      setModified();
      ccrGuid = null;
    }
    else {
      alert("Unable to delete series:\r\n\r\n  " + results.error.value);
    }
  }
  window.resultsframe.location.href='RemoveSeries.action?ccrIndex='+parent.currentTab.ccrIndex + '&seriesIndex=' + i;
}



/**************************************
 * Support for setting Emergency CCR
 */
function setEmergencyCCR() {
  log('setting ccr ' + ccrGuid + ' as emergency CCR');
  if((!ccrGuid) || (ccrGuid == '')) {
    alert('Before you can set this CCR as your Emergency CCR you must Send or Save it.\r\n\r\n'
         +'Please select "Save" or "Send" from the menu and then select the Set Emergency CCR option again.' );
    return;
  }
  var ccrIndex = window.parent.currentTab.ccrIndex; 
  execJSONRequest("SetEmergencyCCR.action", {ccrIndex: ccrIndex}, function(result) {
    window.parent.replaceTab('Emergency CCR','viewEditCCR.do?ccrIndex='+(result.ccrIndex),'',window.parent.nextTab());
    alert('This CCR has been set as Emergency CCR for Account #' + prettyAcctId(removeSpaces(patientMedCommonsId)));
  });
}

var patientIdIndex = -1;
function patientIdUpClick() {
  if(patientIds.length==0)
    return;
  patientIdIndex--;
  if(patientIdIndex<0)
    patientIdIndex = patientIds.length-1;
  displayPatientId();
}

function patientIdDownClick() {
  showPatientIdMenu();
  /* Old code - used to display next patient id
  if(patientIds.length==0)
    return;
  patientIdIndex++;
  if(patientIdIndex>patientIds.length-1)
    patientIdIndex = 0;

  displayPatientId();
  */
}

function displayPatientId() {
  if(patientIds.length<=1) {
    //hide('patientIdDown');
  }
  if(patientIds.length==0) {
    document.referralForm.patientId.value = '';
    document.referralForm.patientId.title =  '(No Patient ID Available)';
    if($('patientIdLabel')) 
      $('patientIdLabel').innerHTML = document.referralForm.patientId.title; 
    return;
  }
  else {
    if(patientIdIndex<0) {
      patientIdIndex = 0;
    }
  }

  document.referralForm.patientId.value = patientIds[patientIdIndex].value;
  document.referralForm.patientId.title = patientIds[patientIdIndex].type;
  if($('patientIdLabel')) {
    if(patientIds.length>1) {
      $('patientIdLabel').innerHTML = 
        patientIds[patientIdIndex].type + " (" + (patientIdIndex+1)+ " of " + patientIds.length + ")";
    }
    else
      $('patientIdLabel').innerHTML = patientIds[patientIdIndex].type;
  }
}

function addNewId(newId) {
  document.referralForm.newAcct.value = false;
  patientIds.push(newId);
  if(patientIdIndex<0)
    patientIdIndex = 0;
  displayPatientId();
}

var editPid = null;
function editPatientId() {
  
  if(patientIdIndex<0) {
    patientIds.push( { type: 'Patient ID', value: '' });
    patientIdIndex = patientIds.length-1;
  }

  editPid = patientIds[patientIdIndex];
  document.editPatientIdForm.patientId.value = editPid.value;
  document.editPatientIdForm.patientIdType.value = editPid.type;
  show('editPatientIdDiv');
  document.editPatientIdForm.patientId.select();
  document.editPatientIdForm.patientId.focus();
}

function editPatientIdSubmit(replace) {
  
  var url = 'PatientId.action?saveId=true';
  if(replace && (replace == true))
    url = 'PatientId.action?replaceId=true';

  url += '&ccrIndex='+
    parent.currentTab.ccrIndex+'&'+
    queryString(document.editPatientIdForm)+
    "&patientIdIndex="+patientIdIndex;

  execJSONRequest(url, null, function(result) {
    try {
      if(result.status == "ok") {
        if(editPid) {
          editPid.type = document.editPatientIdForm.patientIdType.value;
          editPid.value = document.editPatientIdForm.patientId.value;;
          if(editPid.type == 'MedCommons Account Id') {
            patientMedCommonsId = editPid.value; 
            storageId = patientMedCommonsId;
            window.parent.disableTools(function(txt) { return txt == mergeTool[0]; },false);
          }
          if(result.writeable != undefined) {
            setWriteable(result.writeable);
          }
          displayPatientId();
          updateCCRFrame();
        }
        hide('editPatientIdDiv');
        $('patientIdDown').style.display='inline';
        parent.updatePatientHeader(null,getCcr());
      }
      else {
        alert("Saving your Patient ID failed:\r\n\r\n  "+result.error);
      }
    }
    catch(e) {
      dump(e);
    }
  });
}

function deletePatientIdSubmit() {
  var url = 'deletePatientId.do?ccrIndex='+
    window.parent.currentTab.ccrIndex+'&'+
    queryString(document.editPatientIdForm)+
    "&patientIdIndex="+patientIdIndex;
  loadJSONDoc(url).addCallbacks(editPatientIdDeleteSuccess, genericErrorHandler);
}


function editPatientIdDeleteSuccess(result) {
  if(result.status == "ok") {
    log("editPatientIdDeleteSuccess");
    if(editPid) {
      try {
        editPid.type = document.editPatientIdForm.patientIdType.value;
        editPid.value = document.editPatientIdForm.patientId.value;;
        var pidIndex = -1;
        for(i=0;i<patientIds.length;i++) {
          if(patientIds[i]==editPid) {
            pidIndex = i;
            break;
          }
        }
        if(pidIndex>=0) {
          patientIds.splice(pidIndex,1);
        }

        if(patientIdIndex >= patientIds.length) {
          patientIdIndex = patientIds.length>0?0:-1;
        }
        displayPatientId();
        if(result.writeable != undefined) {
            setWriteable(result.writeable);
        }
      }
      catch(e) {
        dump(e);
      }
    }
    hide('editPatientIdDiv');
    updateCCRFrame();
  }
  else {
    alert("Deleting your Patient ID failed:\r\n\r\n  "+result.error);
  }
}

function deletePatientIdsSuccess(result) {
  if(result.status == "ok") {
    patientIds = new Array();
    patientIdIndex = -1;
    displayPatientId();
  }
  else {
    alert("Warning! Deleting Patient IDs failed:\r\n\r\n  "+result.error);
  }
}

function showPatientIdMenu() {
  var patientIdMenu = new Array();
  forEach(patientIds, function(i) {
    patientIdMenu.push(i.type + " - " + i.value);
  });
  patientIdMenu.push("Add a new ID");
  init_autocomplete(document.referralForm.patientId, patientIdMenu);

  document.referralForm.patientId.autocomplete = autocomplete;
  autocompleteBehavior.show_all = true;
  autocompleteBehavior.fill_value = patientIdSelect;
  autocompleteBehavior.offsetX = -20;
  autocompleteBehavior.offsetY = 10;
  autocompleteBehavior.message = "<img src='images/closebutton.gif' style='margin-top:4px; cursor: pointer;' onclick='patientIdSelect(-1)'/>&nbsp;&nbsp;Click on an ID Type to select it";
  document.referralForm.patientId.autocomplete();
}

function patientIdSelect(i) {
  var acdiv = document.getElementById('acdiv');
  acdiv.style.display='none';
  auto_complete_reset_behavior();

  if(i >= 0) {
    if(i>=patientIds.length) { // user chose "add an id" option
      patientIdIndex = -1; // cause edit to add a new patient
      editPatientId();
      return;
    }

    patientIdIndex = i;
    displayPatientId();
  }
}

/*****************************************************************/
/* Support for sending CCRs                                      */
/*****************************************************************/

function updateStatusWindow(statusForm) {
  if(!statusForm) {
    el('ssStatus').innerHTML = 'INTERNAL ERROR <br/><a style="font-size:xx-small;" href="javascript:viewInternalError();">view detail</a>';
    return;
  }
  el('ssStatus').innerHTML = statusForm.status.value;
  el('ssTrackingNumber').innerHTML = statusForm.trackingNumber.value;
  el('ssPin').innerHTML = statusForm.pin.value;
  ccrGuid = statusForm.guid.value;

  if(statusForm.acctId) {
    el('ssAcctCreated').innerHTML=prettyAcctId(statusForm.acctId.value);
    el('ssAcctCreatedLabel').innerHTML="Account";
    patientMedCommonsId = statusForm.acctId.value;
    addNewId({type:"MedCommons Account Id",value: statusForm.acctId.value});
  }

  sendError = statusForm.error.value;

  if(statusForm.status.value == 'INSUFFICIENTCREDIT') {
    var evt = { quantity: 1, type: 'NEW_ACCOUNT' };
    noCreditDlg(evt, window.resultsframe.counters);
    return false;
  }
  else
  if(statusForm.status.value == 'FAILED') {
    el('ssStatus').innerHTML = 'FAILED<br/><a style="font-size:xx-small;" href="javascript:viewFailedReason();">view detail</a>';
    return false;
  }
  if($('trackingNumber'))
    $('trackingNumber').innerHTML=statusForm.trackingNumber.value;

  ccrCreateDateTime = statusForm.createDateTime.value;
  resetTabText(statusForm);
  return true;
}

var sendError = null
function sendFinished(statusForm) {
  if(!updateStatusWindow(statusForm))
    return false;

  if(registry)
    fetchRegistryPatients();
  else // if registry then title will be set after load
    setPatientTitle(document.referralForm.patientGivenName.value,document.referralForm.patientFamilyName.value);

  var copyIndex = statusForm.ccrIndex.value;
  var url = 'updateCcr.do?forward=viewer&ccrIndex='+copyIndex;
  ccrCreateDateTime = statusForm.createDateTime.value;
  var dts =  ccrCreateDateTime.split(/ /)[0];
  window.parent.addTab(formatTabDate(americanDate(dts)),url,'',window.parent.nextTab());

  // Special case: when NEWCCR is saved, tab should be removed.
  var ccr = parent.currentTab.ccr;
  if((storageMode == 'LOGICAL') && (ccr && ccr.documentType == 'NEWCCR')) {
    removeNewCCRTabs();
  }
  else {
    updateTabType(statusForm);
  }

  ce_signal('openCCRUpdated',ccrGuid);
}

/**
 * Remove all tabs representing 'New CCR' tabs.
 */
function removeNewCCRTabs() {
  // Hardest thing is we have to find another good tab to highlight
  var ccr = parent.currentTab.ccr;
  var cccrTab = null;
  var otherTab = null;
  forEach(window.parent.getTabs(), function(t) {
      if(t.ccr && t.ccr.documentType == 'CURRENTCCR')
      cccrTab = t;
      if(t.ccr && (t.ccr.documentType != 'NEWCCR'))
      otherTab = t;
      });
  var newTab = cccrTab ? cccrTab : otherTab;
  if(newTab) {
    window.parent.removeTabs( function(t) { return  (t && t.ccr && (t.ccr.documentType == 'NEWCCR')); } );
    window.parent.showTab(newTab);
  }
}

/**
 * Update tab with correct name in case the document 
 * type changed with a send / save / other operation.
 */
function updateTabType(sf) {
  // Update document type
  var ccr = parent.currentTab.ccr;
  if(ccr.documentType != sf.savedLogicalType.value) {
    ccr.documentType = sf.savedLogicalType.value;
    ccr.storageMode = sf.savedStorageMode.value;
    parent.setTabText(null,patientTabText(ccr));
  }
}

function printResults() {
  window.resultsframe.focus();
  window.resultsframe.print();
}

function viewFailedReason() {
  el('ssError').innerHTML = sendError;
}

function viewInternalError() {
  $('ssError').innerHTML = window.resultsframe.document.body.innerHTML;
}

/**
 * Save this CCR as Patient's Current CCR
 */
function saveCurrentCcr() {
  if(confirm("Setting this CCR as Current CCR will replace the existing Current CCR completely.\r\n\r\nAre you sure you want to continue?"))
    submitSaveAction("SAVING", 'SaveCCR.action?forward&next=SetCurrentCCR.action&merge=false', saveCurrentCcrFinished, "Save as Current CCR");
}

function saveCurrentCcrFinished(statusForm) {
  updateStatusWindow(statusForm);
  window.parent.eval('clearTabCurrentCCR()');
  window.parent.setTabText(null,"Current CCR");
  currentCcrGuid = statusForm.guid.value;
  isCCCR = true;
  storageMode = 'LOGICAL';
  setWriteable(true);
}



// Tools only available when user has permissions to modify patient account 
var writeTools = [ 'Create Reply', 'Edit With HealthBook', 'Add PDF Document','Add Web Reference','Add DICOM', 'Create Order', 'Upload Account Files' ];

// Tools only available when not fixed content
var notFixedWriteTools = [ 'Add PDF Document','Add Web Reference','Add DICOM', 'Create Order', 'Upload Account Files' ];

// Tools only available if not "live" content
var notLiveWriteTools = [ 'Create Reply' ];

function contains(ar,txt) {
  return findValue(ar,txt) >= 0;
}

function updateTools() {
  forEach(window.parent.currentTools, function(t) {
    var txt = window.parent.toolText(t);
    var enabled = true;
    if(!writeable && contains(writeTools,txt))
      enabled = false;

    if((storageMode == 'FIXED') && contains(notFixedWriteTools,txt)) {
      enabled = false;
    }

    if((storageMode == 'LOGICAL') && contains(notLiveWriteTools,txt)) {
      enabled = false;
    }

    window.parent.enableTool(t,enabled);
  });

  // Override for some special cases
  parent.enableTool(saveAsFixedTool,storageMode == 'LOGICAL');
  log("Enabling tool " + createReplyTool[0] + " in mode " + storageMode + " with " + (storageMode == 'FIXED'));
  parent.enableTool(createReplyTool,storageMode == 'FIXED');
  parent.enableTool('Hide CCR',storageMode == 'FIXED');
  if(isCCCR || !currentCcrGuid) {
    window.parent.disableTools(function(txt) { return txt == mergeTool[0]; },true);
  }
  if(empty(patientMedCommonsId)) {
    window.parent.disableTools(function(txt) { return txt == setAsCurrentCCRTool[0]; },true);
  }

  //var newTools = clone(window.parent.currentTools);
  
  /*

  var toDisable = writeTools;

  // If we have write perms but we can still not write because of fixed content, disable subset of tools 
  if(writeable && (storageMode == "FIXED")) {
    toDisable = notFixedWriteTools;
  }

  window.parent.disableTools( function(txt) { 
      return findValue(toDisable,txt)>=0; 
    }, !(writeable && (storageMode != "FIXED")) );
  */

  // Enable setting as cccr if writable AND we have a patient account id
  window.parent.enableTool(setAsCurrentCCRTool,writeable && !empty(patientMedCommonsId));
  window.parent.enableTool(editExternally,writeable && (ccrGuid != null));
}

function hideCCR() {
  execJSONRequest('HideCCR.action',
     queryString({ccrIndex: parent.currentTab.ccrIndex,profileId:parent.currentTab.ccr.profileId}), 
       function(result) {
          if(result.status == "ok") {
            alert('CCR hidden successfully.  This CCR will not be shown in future for this Patient\'s PHR');
            var prev = window.parent.previousTab();
            if(prev) {
              window.parent.hideTab(window.parent.currentTab.id);
              window.parent.showTab(prev);
            }
          }
          else {
            alert('A problem occurred while hiding this CCR:\r\n\r\n' + result.error);
          }
  });
}

/**
 * Cancel edits applied to this page.
 */
function cancelCcr() {
  if(parent.currentTab.ccr.documentType == 'NEWCCR') {
    if(!confirm("Cancelling this New CCR will remove it from display for this Patient Account.  Are you sure you want to continue?")) 
      return;

    execJSONRequest('CancelNewCCR.action?ccrIndex='+parent.currentTab.ccrIndex,null,
      function(res) {
        if(res.status == "success") {
          alert('Your New CCR has been Cancelled');
          removeNewCCRTabs();
        }
        else {
          alert("A problem occurred in cancelling your New CCR.\r\n\r\n  "+res.error);
        }
      });
    return;
  }

  if(ccrGuid == null) {
      if(!confirm("Changes you have made to this CCR will be lost.\r\n\r\nAre you sure you want to continue?"))
        return;
      setUnmodified();
  }

  if(window.parent.opener != null) {
    window.parent.close();
  }
  else
    window.location.href='viewEditCCR.do?mode=edit&ccrIndex='+window.parent.currentTab.ccrIndex;
}

/**
 * Causes the document to be saved and submits to the requested action
 */
function submitSaveAction(initialStatus, action, callback, statusTitle) {
  if(!statusTitle) {
    statusTitle = "Send Status";
  }
  $('sendStatusTitle').innerHTML = statusTitle;
  referralComplete = callback;
  document.referralForm.target = 'resultsframe';
  $('ssStatus').innerHTML = initialStatus;
  $('ssTrackingNumber').innerHTML = '-';
  $('ssPin').innerHTML = '-';
  $('ssError').innerHTML = "";
  show('sendStatusDiv');
  log("Sending to " + document.referralForm.action);
  document.referralForm.action = action;
  document.referralForm.submit();
}

/**
 * Called when invisible frame containing save or send results loads.
 * Notifies registered callback function and if save was successful,
 * sends general signal out that save succeeded.
 */
function resultReady() {
  var sendSignal = true;
  if(window.referralComplete) { 
    sendSignal = window.referralComplete(resultsframe.document.results);
  } 

  if((typeof sendSignal == 'undefined') || (sendSignal == true))
    signal(window,'ccrSaved',resultsframe.document.results);
}

/*****************************************************************/
/* Support for Registry Patients
/*****************************************************************/
function fetchRegistryPatients() {
  registryPatients = null;
  registryPatientNames = new Array();
  hide('patientFindImg');
  loadJSONDoc('FetchRegistryPatients.action',{ccrIndex:window.parent.currentTab.ccrIndex}).addCallbacks(fetchRegistryPatientsSuccess,XHRErrorHandler);
}

/**
 * Return a formatted String representing the patient
 */
function patientListEntry(p) {
  // Convert create date time from string thru int to Date
  if(!p.CreationDateTime.getDate)
    p.CreationDateTime = new Date(parseInt(p.CreationDateTime)*1000);

  var e = new Array();
  e.push(prettyTrack(p.ConfirmationCode));
  e.push(formatLocalDateTime(p.CreationDateTime));
  if(p.PatientGivenName || p.PatientFamilyName)
    e.push(prettyTrack(p.ConfirmationCode));

  var ageSex = '';

  if(p.PatientAge)
    ageSex = p.PatientAge;

  if(p.PatientSex) {
    if(p.PatientSex=="Male")
      ageSex += "M";
    else
    if(p.PatientSex=="Female")
      ageSex += "F";
    else
      ageSex += "?";
  }
  if(ageSex)
    e.push(ageSex);

  if(p.Status) 
    e.push(p.Status);

  return e.join(" - ");
}

/**
 * If the loaded CCR is found in the registry then this variable
 * is set to that patient record.  Otherwise it is null.
 */
var loadedRegistryPatient = null;

var registryPatients = null;
var registryPatientNames = window.parent.registryPatientNames = new Array();


var patientListAutocompleteBehavior = {
  show_all: true,
  fill_value: patientSelect,
  offsetX: -20,
  offsetY: 20,
  message: "<img src='images/closebutton.gif' style='margin-top:4px; cursor: pointer;' onclick='patientSelect(-1)'/>&nbsp;&nbsp;Click on an entry to select ",
  auto_show: false
};

function fetchRegistryPatientsSuccess(patients) {
  registryPatients = patients.patients;
  forEach(registryPatients, function(p) {
    if(p.Guid == loadedGuid) {
      loadedRegistryPatient = p;
      window.parent.document.title = 'PHR: ' + p.PatientGivenName + ' ' + p.PatientFamilyName;
      if($('trackingNumber'))
        $('trackingNumber').innerHTML = prettyTrack(p.ConfirmationCode);
    }
    var entry = patientListEntry(p);
    registryPatientNames.push(entry); 
  });

  if(registryPatientNames.length > 0) {
    log("initializing registry patient name tab auto completes");
    if($('patientFindImg'))
      $('patientFindImg').style.display = 'inline';
    init_autocomplete(document.referralForm.patientGivenName, registryPatientNames);
    var behavior = setdefault(clone(patientListAutocompleteBehavior), autocompleteBehavior);
    behavior.fill_value = cleanPatientSelect;
    document.referralForm.patientGivenName.autocompleteBehavior = behavior;
  }
}

function findPatient() {
  document.referralForm.patientGivenName.autocomplete = autocomplete;
  document.referralForm.patientGivenName.autocomplete(); 
}

function cleanPatientSelect(i) {
  log("Clean patient " + i);
  patientSelect(i,true);
}

function patientSelectKeyDown(evt) {
  if(evt == null) {
    evt = window.event;
  }

  var keyCode = evt.keyCode;
  log(keyCode);
  if(keyCode == 27) {
    patientSelect(-1); 
  }
}

/*************************************************************************************
 * TODIR SUPPORT
 */

var toDirList = new Array();

function fetchToDir() {
  doSimpleXMLHttpRequest('GetToDir.action').addCallbacks(fetchToDirSuccess,XHRErrorHandler);
}

var toDirMenu = null;
function fetchToDirSuccess(req) {
  try {
    log('got todir: ' + req.responseText);
    var result = eval(req.responseText);
  
    toDirList = result.todir;
    toDirList = toDirList.concat(result.ccrDir);

    toDirMenu = new Array();
    if((toDirList == null) || (toDirList.length==0)) {
      log("No todir returned.");
      return;
    }

    forEach(toDirList, function(entry) {
      var entryText = '';
      var cacheEntry = { isGroup: false };
      if(!entry.accid) {
        // entryText += '<img src="images/padlock.jpg"/><span> ';
      }
      else
      if(entry.context) { // Note: only displays if member of SAME group
        // entryText += '<img src="images/group.png"/><span>  ';
        cacheEntry.accountId = groupId;
        cacheEntry.msg = 'toGroupMsg';
        cacheEntry.isGroup = true;
      }
      else {
        // entryText += '<img src="images/person.png"/><span>  ';
        cacheEntry.accountId = entry.accid;
        cacheEntry.msg = 'toAcctMsg';
      }  

      if(!entry.contact) {
        entry.contact = prettyAcctId(entry.accid);
      }

      if(!resolveEmailCache[entry.contact]) {
        resolveEmailCache[entry.contact]=cacheEntry;
      }
      toDirMenu.push( entry.contact /*+"</span>"*/  );
    });
    log("built todir menu with " + toDirMenu.length + " entries ");

    log("Creating To field data source");
    yuiLoader().insert(function() {
      var toDS = new YAHOO.widget.DS_JSArray(toDirMenu); 
      var ac = new YAHOO.widget.AutoComplete('toEmail', 'toACContainer', toDS); 
      ac.minQueryLength = 0;
      ac.forceSelection = false;
      ac.formatResult = function(email) {
        var entry = resolveEmailCache[email];
        var entryText = '';
        if(entry.accountId) 
          entryText +='<img src="images/person.png"/> &nbsp;'+email+' (Pre-Authorized)';
        else 
          entryText +='<img src="images/padlock.png"/> &nbsp;'+email+' (PIN Required)';

        return entryText;
      }
      ac.itemSelectEvent.subscribe(onFieldChange);
    });
/*    ac.prehighlightClassName = "yui-ac-prehighlight";
    ac.typeAhead = true;
    ac.useShadow = true;
    ac.minQueryLength = 0;
    */
    log("Initialized to autocomplete");
  }
  catch(e) {
    dump(e);
  }
}

function clearDirTo() {
  $('destAcctId').value='';
}

function toDirSelect(i,keepAC) {
  var acdiv = document.getElementById('acdiv');
  if(!keepAC) {
    acdiv.style.display='none';
    auto_complete_reset_behavior();
  }

  var doFocus = true;
  if((i>=0) && (i<toDirList.length)) {
    document.referralForm.toEmail.value = toDirList[i].contact;
    document.referralForm.toEmail.originalValue = document.referralForm.toEmail.value;
    // If PIN entry selected, show the warning message
    if(!toDirList[i].accid) {
      var pin = el('assignedPin').value;
      if((pin == null) || (pin != pin.match(/[0-9]{5}/))) {
        alert('A PIN will be required. Please enter a 5 digit PIN and be sure to tell it to the recipient.\r\n\r\nThis CCR will not be released without the 5 digit PIN. ');
        document.referralForm.pin0.focus();
        document.referralForm.pin0.select();
        doFocus=false;
      }
    }
    else { // some kind of account id included
      log("filling account " + toDirList[i].accid + " with context " + toDirList[i].context);

      if(toDirList[i].context) { // group id included
        hide('toAcctMsg');
        updateResolvedEmail(toDirList[i].context,'toGroupMsg');
      }
      else {
        updateResolvedEmail(toDirList[i].accid); // only account id included
      }
    }
  }
  if(doFocus)
    document.referralForm.toEmail.focus();
}

if(!window.parent.resolveEmailCache) {
  window.parent.resolveEmailCache = new Array();
}

var resolveEmailCache = window.parent.resolveEmailCache;

function resolveToEmail(evt) {
  if($('acdiv').style.display == 'block') 
    return;

  var email = $('toEmail').value;
  log("looking for email " + email);
  if(resolveEmailCache[email]) {
    updateResolvedEmail(resolveEmailCache[email].accountId);
    return;
  }

  log("Resolving email address " + $('toEmail').value);
  doSimpleXMLHttpRequest('ResolveEmail.action',{toEmail:email}).addCallbacks(resolveEmailSuccess, XHRErrorHandler);
}

function resolveEmailSuccess(req) {
  var accountId = eval(req.responseText);
  var email = $('toEmail').value;
  if(!resolveEmailCache[email])
      resolveEmailCache[email]={ 'accountId': accountId, msg: 'toAcctMsg' };
  updateResolvedEmail(accountId);
}

function updateResolvedEmail(accountId) {
  hide('toAcctMsg','toGroupMsg');
  var email = $('toEmail').value;
  var entry = resolveEmailCache[email];
  
  if(entry && entry.accountId && entry.accountId.match(/[0-9]{16}/)) {
    log("updating dest acct to " + accountId);
    $('destAcctId').value = accountId;
    show(entry.msg);
  }
  else {
    log("clearing dest acct");
    $('destAcctId').value = '';
    resolveEmailCache[email]={ 'accountId': '', type: '' };
  }
}


var ccrLinkButton = { text: '', url: '', id: 'ccrLink', action: 'parent.ccrLink()' };

var sendButtons = [ 
  { text: 'Send',   action: 'goReferralEmail()', tip: "Click to Send this CCR to the Recipient(s) in the TO field"  },
  /*
  { text: 'Share',   action: 'shareCcr()', img: 'images/openid-icon-tiny.gif',tip: "Click to Share this CCR with another person via their OpenID"  },
  */
  { text: 'Save',   action: 'saveCcr()', tip: "Click to Save this CCR"  },
  { text: 'Cancel', action: 'cancelCcr()' },
  ccrLinkButton
];

function setWriteable(w) {
  writeable = w ? true : false;
  if(writeable && empty(accId)) {
    show('termsOfUseLabel');
  }

  if(writeable && storageMode != "FIXED") {
    parent.setButtons(sendButtons);
    hide('notopbuttons');
    hide('nobuttonsfixed');
  }
  else {
    parent.setButtons([ccrLinkButton]);
    hide('termsOfUseLabel');
    if(storageMode == "FIXED")
      show('notopbuttonsfixed');
    else
      show('notopbuttons');
  }
  forEach(getElementsByTagAndClassName("*","writeOnly"), function (e) {
    e.style.display=writeable ? 'block' : 'none';
  });
  
  updateTools();

}

function checkTermsOfUse() {
  if(empty(accId) && !$('termsOfUseCheckBox').checked) {
    alert('Please confirm that you have read the Terms of Use by checking the box provided.');
    update($('termsOfUseLabel').style, { padding:'4px',borderColor:'red',borderStyle:'solid'});
    return false;
  }
  else
    return true;
}

/*************************************************************************************
 * Account Tools
 */
function addToAccountSuccess(result) {
  if(result.success) {
    removeAddToAccountTool();
  }
  else {
    if(result.pinChangeConfirmationRequired) {
      if(confirm("You have edited the PIN for this Tracking Number.\r\n\r\nSaving with a new PIN will prevent others who have the original PIN from accessing the document.\r\n\r\nDo you want to continue?")) {
        window.confirmPinChange=true;
        addToAccount();
      }
    }
    else {
      alert("A problem occurred adding your CCR to your account:\r\n\r\n"+result.error);
    }
  }
}

function removeAddToAccountTool() {
  tools = new Array();
  forEach(defaultTools, function(o){tools.push(o);});
  tools.push(emergencyCcrTool);
  window.parent.setTools(tools);
  setWriteable(writeable);
}

function canEditDemographics() {
  return writeable && storageMode != "FIXED";
}

function showDemographicsForm(f) {
  if(canEditDemographics) {
    visibility('referralFormContainer',false);
    visibility('patientCard',true);
    updateCCRFrameSize();
  }
  else {
    highlightSection('demographics');
  }
}

function hidePatientDemographics() {
  visibility('referralFormContainer',true);
  visibility('patientCard',false);
}

function demographicsOK() { 
  hidePatientDemographics();
  parent.updatePatientHeader(null,getCcr());
  updateCCRFrameSize();
  if(parent.currentTab.modified)
    saveCcr();
}


/*************************************************************************************
 * CCR EDITING SUPPORT
 */
var activeEditor = null;
function editBodySection(evt,section) {
  addBodySection(evt,section);
}

var activeEditorName = null;
function addBodySection(evt,section) {
  var editor = $(section+'Editor');
  hide('activeSectionBox');
  currentActiveSection = null;
  if(editor) {
    log("Add/Edit " + section);
    if(activeEditor != null) {
      hideEditor();
    }
    activeEditorName = section;
    activeEditor = editor;
    var tab = $(section+'Table');
    if(tab && !tab.initialized) {
      eval(section+'Init()');
    }
    show(section+'Editor');
    //var yPos = evt.clientY + document.body.scrollTop - (elementDimensions(editor).h);
    if(evt) {
      var yPos = elementPosition($(section+'Heading')).y - (elementDimensions(editor).h);
      setElementPosition(editor,{ x: 200, y: yPos });
    }
  }
}

function hideEditor() {
  hide(activeEditor.id);
  disableKeyCapture=false;
  activeEditor = null;
}

/**
 * Adds a row to the editor table, automatically assigning ids and styles 
 * by deducing them from the header row.  Values to populate in the columns can be
 * passed as arguments, otherwise a default label will be inserted.
 */
function addEditorRow() {
  // Get the heading row and use that to create a new child row
  var thr = activeEditor.getElementsByTagName('THEAD')[0].getElementsByTagName('TR')[0];
  var tb = activeEditor.getElementsByTagName('TBODY')[0];
  var count = tb.getElementsByTagName('TR').length;
  var aen = activeEditorName;

  // Iterate over cells, create new body row
  var firstCell = null;
  var rowId = aen+'['+count+']';
  var newRow = TR({id: rowId});
  var i = 0;
  var setFocus = true;
  //var objectId = arguments.shift();
  var params = arguments;
  forEach(thr.getElementsByTagName('TH'), function(th) {
    var tdId = th.id != '' ? aen+'['+count+'].'+th.id : '';
    var td =  TD({id: tdId,'class':th.cellClass?th.cellClass : 'freetext'});
    if(th.title != 'Add Row') {
      initCell(td);
      if(!firstCell)
        firstCell = td;
      if((i<params.length) && (params[i]!=null)) {
        appendChildNodes(td,params[i]);
        setFocus = false; // don't set focus if we are initializing with data
      }
      else {
        // special handling for date fields - add today's date
        if(td.id.match(/Date$/) || td.id.match(/dateTime$/))
          appendChildNodes(td,toAmericanDate(new Date()));
        else {
          td.emptyField=true;
          appendChildNodes(td,'Type Here');
        }
      }

      ++i;
    }
    else {
      appendChildNodes(td,
        A({href:"javascript:deleteRow('"+rowId+"');", title:"Delete Row"}, IMG({src:"images/delete.gif",border:"0"})));
    }
    appendChildNodes(newRow,td);
  });
  appendChildNodes(tb,newRow);
  if(setFocus) {
    firstCell.focus();
    firstCell.edit();
  }
  activeEditor.dirty = true;
}

function deleteRow(rowId) {
  var tr = $(rowId);;
  removeElement(tr);
}

function editorSave(type) {
  // Build up the query string and send it
  var tb = activeEditor.getElementsByTagName('TBODY')[0];
  var ccrIndex = 0;
  if(window.parent && window.parent.currentTab) {
    ccrIndex = window.parent.currentTab.ccrIndex;
  } 
  var queryString = "ccrIndex="+ccrIndex;
  forEach(tb.getElementsByTagName('TR'), function(tr) {
    queryString += editorRowQueryString(tr);
  });
  //alert(queryString);

  var  req = getXMLHttpRequest();
  req.open("POST", 'Save'+type+'Edit.action', true);
  req.setRequestHeader('Content-Type', 'application/x-www-form-urlencoded');
  sendXMLHttpRequest(req, queryString).addCallbacks(editorSaveSuccess, genericErrorHandler);
 
  //doSimpleXMLHttpRequest('Save'+type+'Edit.action?'+queryString).addCallbacks(editorSaveSuccess, genericErrorHandler);
}

function editorSaveSuccess(req) {
  var result = eval(req.responseText);
  if(result == "ok") {
    try {
      if(window.ccrframe)
        window.ccrframe.location.reload();

      // If there are rows in the table then highlight the table
      activeEditor.dirty=false;
      var tb = activeEditor.getElementsByTagName('TBODY')[0];
      var count = tb.getElementsByTagName('TR').length;
      if($(activeEditorName+'Heading')) {
        if(count>0) {
          $(activeEditorName+'Heading').className='clickable bold';
        }
        else {
          $(activeEditorName+'Heading').className='missing';
        }
      }
      hideEditor();
      if(window.onEditorSaveSuccess){
        window.onEditorSaveSuccess();
      }
    }
    catch(e) {
      dump(e);
    }
  }
  else {
    alert("A problem occurred while saving:\r\n\r\n: " + result);
  }
}

var editedCell = null;

function editorRowQueryString(tr) {
  var qs = "";
  forEach(tr.getElementsByTagName('TD'), function(td) {
    if(td.id && (td.id!='') && (td.title!='Delete Row')) {
      //if(td.emptyField) log("field " + td.id + " is empty field");
      qs+="&"+td.id+"="+(td.emptyField?"":urlEncode(scrapeText(td)));
    }
  });
  log(qs);
  return qs;
}

function handleEditorKeyPress(evt) {
  if(evt == null)
    evt = window.event;
  if(evt.keyCode == 27) { // escape key
    hideEditor();
  }
}

function cellChanged() {
  var cell = this.parentNode;
  log("cell " + cell.id + " changed");
  cell.emptyField=false;
}

function selFrequency() {
  if((this.editMode == null) || !this.editMode) {
    this.editMode=true;
    editedCell = this;
    connect(this,'onkeydown',editCellKeyDown);
    this.innerHTML='<select class="tableSelect" onblur="unedit(this);" onchange="uneditSel(this);"><option>tid</option><option>qd</option><option>bid</option><option>qod</option></select>';
  }
}

function uneditSel(sel) {
  var cell = sel.parentNode;
  cell.innerHTML=sel.options[sel.selectedIndex].innerHTML;
  initCell(cell);
  cell.editMode=false;
  cell.emptyField=false;
}

function selFreeText() {
  if(editedCell && (editedCell != this)) {
    var f = bind(unedit,editedCell);
    f();
  }
  if((this.editMode == null) || !this.editMode) {
    this.editMode=true;
    editedCell = this;
    connect(this,'onkeydown',editCellKeyDown);
    this.oldValue=scrapeText(this);
    this.innerHTML='<input type="text" class="tableInput" style="width:80%; padding: 0px; margin: 0px;" onblur="unedit(this);" value="'+this.oldValue+'"/>';
    this.childNodes[0].focus();
    this.childNodes[0].select();
  }
}

function editCellKeyDown(evt) {
  var keyCode = evt.key().code;
  if(keyCode == 9) { // tab key
    // Move to next field, if there is one
    var tr = editedCell.parentNode;
    var oldCell = editedCell;
    var editCellIndex = findIdentical(tr.childNodes,editedCell);
    if(evt.modifier().shift) { // shift down, move to prev field instead
      editCellIndex--;
      if((editCellIndex>=0) && tr.childNodes[editCellIndex].edit) {
        tr.childNodes[editCellIndex].edit();
      }
      else
        bind(unedit,oldCell)();
    }
    else {
      if(editCellIndex>=0) {
        editCellIndex++;
        if(editCellIndex<tr.childNodes.length) {
          if(tr.childNodes[editCellIndex].edit) { 
            tr.childNodes[editCellIndex].edit();
          }
          else
           bind(unedit,oldCell)();
        }
      }
    }
    evt.stopPropagation();
    evt.preventDefault();
  }
  else
  if((keyCode == 13) || (keyCode == 27)) { // enter key
    log("unedit");
    bind(unedit,editedCell)()
    cancelEventBubble(evt);
  }

}

function unedit(inp) {
  if(!inp) {
    inp = this.getElementsByTagName('INPUT')[0];
  }
  var cell = inp.parentNode;
  disconnectAll(cell);
  replaceChildNodes(cell,inp.value);
  cell.editMode=false;
  // if oldValue is set, use it to check if field was modified
  // this ensures the default "type here" message doesn't get
  // sent as a real value to the server
  if((cell.oldValue==null) || (cell.oldValue != inp.value)) {
    activeEditor.dirty = true;
    cell.emptyField=false;
  }

  initCell(cell);
  if(editedCell==cell)
    editedCell = null;
}

function overCell() {
  if(!this.defaultBgColor) {
    this.defaultBgColor = getStyle(this,'backgroundColor','background-color');
  }
  this.style.backgroundColor='#eeeeee';
}

function editCell() {
  this.edit();
}

function initCell(cell) {
  if(cell.className=="frequency")
    cell.edit=selFrequency;
  else
  if(cell.className=="freetext")
    cell.edit=selFreeText;

  if(cell.edit) {
    cell.onclick = editCell;
    cell.onmouseover=overCell;
    cell.onmouseout=outCell;        
  }
}

function outCell() {
  //this.style.backgroundColor='#c0cfc4';
  this.style.backgroundColor=this.defaultBgColor;

}

function initTable(tab) {
  if(tab.initialized != null)
    return;

  // Iterate rows in table and add mouseovers
  for(i=1;i<tab.rows.length;i++){
    var row = tab.rows[i];
    for(j=0;j<row.cells.length;j++) {
      var cell=row.cells[j];
      log("initializing cell " + i + "," + j);
      initCell(cell);
    }
  }
  tab.initialized = true;
}

function checkDirtySection() {
  if(activeEditor && activeEditor.dirty) {
    return confirm("You have unsaved edits in one or more Section Editors for this CCR.\r\n\r\nIf you continue these edits will be lost.\r\n\r\nAre you sure you want to continue and lose your changes?");
  }
  else
    return true;
}


/**
 * Attempts to populate form f with values from object obj
 *
 * Each form field in the form is examined and an attempt is made
 * to map its name to a property on the object.  This is done by
 * parsing the dot notation and walking the object property 
 * hierarchy.
 */
function populate(f,obj) {
  nodeWalk( f, function(n) {
    if(n.name) {
      var path = n.name.substring(n.name.indexOf('.')+1);
      // Lower case first char of each segment
      path = map(function(p) {
               return p.charAt(0).toLowerCase() + p.substring(1);
             }, path.split(".")).join(".");

      var v = resolve(obj,path);

      if(v != null) {
         n.value= v;
      }
    }
    return n.childNodes;
  });
}


/*************************************************************************************
 * del.icio.us
 */
function delicious(href) {

  if(!$('trackingNumber')) {
    alert('You cannot create a de.icio.us bookmark for this CCR'); // todo: we can really, but have to figure out the right url
  }

  var tn = $('trackingNumber').innerHTML.replace(/ /g,'');
  var pin = el('assignedPin').value;
  var href=remoteAccessAddress+'/tracking.jsp?tracking='+tn+'&p='+hex_sha1(pin);
  //var href=commonsUrl.replace(/\/ws$/,"")+'/gwredirguid.php?guid='+ccrGuid;
  var name = getCookie("mcdelname");
  if(!name) {
    name=window.prompt("Please enter your del.icio.us user name:","");
    var expires = new Date( (new Date()).getTime() + (30*24 * 60 * 60 * 1000) );
    setCookie("mcdelname", name, expires);
  }
  var notes="guid="+ccrGuid+" subject="+$('ccrPurpose').value+" tracking="+tn;
  var title="MedCommons CCR "+ccrGuid;
  window.parent.location.href='http://del.icio.us/'+escape(name)+'?v=3&title='+escape(title)+'&notes='+escape(notes)+'&tags=MedCommons&url='+encodeURIComponent(href); 
  return false;
} 

function addDelicious() {
  if(!window.isDelicious) {
    window.parent.currentTools.push(["Post to del.icio.us","delicious(commonsUrl);"]);
    window.parent.setTools(window.parent.currentTools);
    setWriteable(writeable);
    window.isDelicious=true;
  }
}

function highlightSection(s) {
  var secId = sectionIdFromName(s);
  if(ccrframe.document.getElementById(secId)) {
    window.ccrframe.setTimeout(
      "window.scrollTo(0,window.parent.findPosY(document.getElementById('"+secId+"'))-18)",0);
  }
}

/********************************************************************************************
 * 
 *  Order Form
 */
function showOrderForm(o,okHandler) {
  visibility('referralForm',false);
  visibility('dialogDiv',true);
  yuiLoader().insert(
      function() { 
        appendChildNodes($('dialogDiv'), 
          DIV({id: 'orderForm'},
            DIV({'class':'hd'},'New Order'),
            DIV({'class':'bd'}, 
              FORM(null,
                INPUT({type:'hidden',name:'ccrDataObjectID'}),
                TABLE(null,
                  TBODY(null,
                    TR(null,TH('Referring Physician: '),TD(null,SELECT({name:'physicianActorId',id:'refPhys'}))),
                    TR(null,TH('Procedure: '),TD(null,INPUT({id: 'procedureText',name:'plan[0].OrderRequest.Procedures.Procedure.Description.Text',type: 'text'}))),
                    TR(null,TH('Type: '),TD(null,INPUT({name:'plan[0].OrderRequest.Procedures.Procedure.Type.Text',type: 'text',value:'Imaging'}))),
                    TR(null,TH('Accession #: '),TD(null,INPUT({name:'plan[0].OrderRequest.Procedures.Procedure.IDs.ID',type: 'text'}))),
                    TR(null,TH('Relevant History: '),TD(null,TEXTAREA({name:'plan[0].OrderRequest.Description.Text',rows: '4'})))
                  )
                ) // end table
              ) // end form
            ) // end bd
          )
        );

        var dialogWidth = Math.round(viewportSize().w * 0.75);
        var left = Math.round((viewportSize().w - dialogWidth) / 2);
        window.orderForm = new YAHOO.widget.Dialog('orderForm', { 
          draggable: true, 
          zIndex: 200000,
          width: dialogWidth+'px', 
          monitorresize: false,
          x: left + 'px',
          draggable: false,
          buttons: [ 
            { text: 'OK', isDefault: true, handler: okHandler },
            { text: 'Cancel', handler: hideOrderForm }
            ]
          });

        // Only consider actors with attributes we can display
        var displayableActors = filter(function(a) { return a.givenName || a.familyName || a.email;}, ccr.actors );
        displayableActors = filter(function(a) { return a.actorObjectID != ccr.patient.actorId;}, displayableActors );
        var actors = map(function(a) { 
                           return OPTION({value:a.actorObjectID},a.givenName + ' ' + a.familyName + (a.email ? (' - ' +a.email) : '')); 
                         }, displayableActors);

        forEach(actors, function(a) {
            appendChildNodes($('refPhys'),a);
        });

        var loggedInActor = filter(function(a) { a.medCommonsId == accId; }, ccr.actors);
        var fromActor = ccr.getFromActor();

        if(o) {
          var actorID = resolve(o,'source.actor.actorID');
          selectOption($('refPhys'),actorID);
        }
        else
        if(loggedInActor.length>0)
          selectOption($('refPhys'),a.actorObjectID);
        else 
        if(fromActor) {
          selectOption($('refPhys'),fromActor.actorObjectID);
        }
        orderForm.render();

        if(o) {
          populate($('orderForm'), o);
        }
        $('dialogDiv').style.left = left + 'px';
        $('dialogDiv').style.width = dialogWidth + 'px';
        visibility('dialogDiv',true);
        window.setTimeout("$('procedureText').focus();",0);
     });
}

function hideOrderForm() {
  replaceChildNodes('dialogDiv');
  visibility('referralForm','referralFormContainer',true);
  visibility('dialogDiv','patientCard',false);
  updateCCRFrameSize();
}


function createOrderCCR() {
  var ccrIndex = window.parent.currentTab.ccrIndex;
  var url='EditOrder.action?newccr&ccrIndex='+ccrIndex;
  var tab = window.parent.addTab("Order CCR *",url,"Order CCR",window.parent.currentTab);
  window.parent.showTab(tab)
}

function saveNewOrder() {
  execJSONRequest('EditOrder.action?ccrIndex='+parent.currentTab.ccrIndex, queryString($('dialogDiv').getElementsByTagName('form')[0]), function(r) {
    if(r == "ok") {
      setModified();
      hideOrderForm();
      updateCCRFrame('planofcareorders');
    }
    else {
      alert("A problem occurred when creating your order.\r\n\r\n"+r);
    }
  });
}

function editCCRRow(e) {
  var ccrObjectId = e.src().parentNode.parentNode.id;
  execJSONRequest('EditOrder.action?open&ccrIndex='+parent.currentTab.ccrIndex, queryString({ccrDataObjectID:ccrObjectId}), 
    function(r) {
      if(r.status == "ok") {
         var order = eval('z='+r.order);
        showOrderForm(order,saveEditedCCRRow);
      }
      else {
        alert("A problem occurred when editing this item.\r\n\r\n"+r.error);
      }
    });
}

function saveEditedCCRRow() {
  execJSONRequest('EditOrder.action?update&ccrIndex='+parent.currentTab.ccrIndex, queryString($('dialogDiv').getElementsByTagName('form')[0]), function(r) {
    if(r.status == "ok") {
      setModified();
      hideOrderForm();
      updateCCRFrame('planofcareorders');
    }
    else {
      alert("A problem occurred when updating your order.\r\n\r\n"+r.error);
    }
  });
}

function deleteCCRRow(secId,e) {
  var tr = e.src().parentNode.parentNode;
  var ccrObjectId = tr.id;
  var section = sectionFromId(secId);
  yuiLoader().insert(function() {
    var w = Math.round(Math.min(viewportSize().w-50, 1.1 * elementDimensions(tr).w))+'px';
    confirmDlg = new YAHOO.widget.SimpleDialog("confirmDlg", { 
      width: w,
      fixedcenter:true,
      modal:true,
      visible:false,
      draggable:false,
      buttons: [ { text: 'OK', handler: 
        function() {
          execJSONRequest('DeleteCCRObject.action?ccrIndex='+parent.currentTab.ccrIndex, queryString({ccrDataObjectID:ccrObjectId,section:section}), 
            function(r) {
              if(r == "ok") {
                updateCCRFrame(secId);
                setModified();
              }
              else {
                if(r.match(/^.*Exception/))
                  r = r.substring(r.match(/.*Exception./)[0].length);
                alert("A problem occurred when deleting the item:\r\n\r\n"+r);
              }
              confirmDlg.destroy();
            });
        }
      },
      { text: 'Cancel', handler: function() { confirmDlg.destroy(); } } ]
    });
    confirmDlg.setHeader("Warning!");
    confirmDlg.setBody("<p>You are about to delete the following object (" + ccrObjectId + ") from this CCR:</p>" 
      + "<table class='dialogTable'><tr>" + tr.parentNode.getElementsByTagName('tr')[0].innerHTML+"</tr><tr>"+ tr.innerHTML 
      + "</tr></table><p>Are you sure you want to delete this item?");
    confirmDlg.cfg.setProperty("icon",YAHOO.widget.SimpleDialog.ICON_WARN);
    confirmDlg.render(document.body);
    confirmDlg.show();
  });
}

function deleteCCRSection(secId,e) {
  var section = sectionFromId(secId);

  var ccrObjectIDs = '';
  withWindow(ccrframe, function() {
    var objects = filter(function(n) { return n.tagName && n.tagName.match(/tr/i) && n.id; } , $(secId).getElementsByTagName('tbody')[0].childNodes);
    var ids = map(function(tr) { return tr.id; }, objects);
    ccrObjectIDs = ids.join(',');
  });

  yuiLoader().insert(function() {
    var w = '400px';
    confirmDlg = new YAHOO.widget.SimpleDialog("confirmDlg", { 
      width: w,
      fixedcenter:true,
      modal:true,
      visible:false,
      draggable:false,
      buttons: [ { text: 'OK', handler: 
        function() {
          execJSONRequest('DeleteCCRObject.action?deleteSection&ccrIndex='+parent.currentTab.ccrIndex, queryString({section:section,ccrDataObjectIDs:ccrObjectIDs}), 
            function(r) {
              if(r == "ok") {
                withWindow(ccrframe, function() {
                  if($(secId+'row'))
                    removeElement($(secId+'row'));
                  else
                    updateCCRFrame();
                });
                setModified();
              }
              else {
                if(r.match(/^.*Exception/))
                  r = r.substring(r.match(/.*Exception./)[0].length);
                alert("A problem occurred when deleting the item:\r\n\r\n"+r);
              }
              confirmDlg.destroy();
            });
        }
      },
      { text: 'Cancel', handler: function() { confirmDlg.destroy(); } } ]
    });
    confirmDlg.setHeader("Warning!");
    confirmDlg.setBody("<p>You are about to delete the following section from this CCR:<p style='margin-left: 45px;'><b>"
                        +section+"</b></p><p style='margin-left: 27px;'>Are you sure you want to delete this item?");
    confirmDlg.cfg.setProperty("icon",YAHOO.widget.SimpleDialog.ICON_WARN);
    confirmDlg.render(document.body);
    confirmDlg.show();
  });
}

/**
 * Add Edit and Delete tools to each row of the given CCR section in the CCR style sheet
 */
function addCCRTools(w) {
  var editableSections = ["planofcareorders"];
  withWindow(w, function() {
    $('editDemoGraphicsLink').style.display='inline';
    forEach(CCR_SECTIONS, function(s) {
      // transform name to id form
      var secId = sectionIdFromName(s);

      log('Annotating section ' + s + ' with id ' + secId);
      if(!$(secId))
        return;

      var sec = $(secId);
      var  tb = sec.getElementsByTagName('tbody')[0];
      if(!tb)
        return;

      var trs = filter(function(tr){return tr.parentNode == tb;},tb.getElementsByTagName('tr'));
      if(trs.length == 0)
        return;

      var delImg = null;
      var editImg = null;

      appendChildNodes(trs[0], TH({'class':'ccredittool'},delImg=IMG({src:'images/whitecross.gif',title:'Click to Delete this entry'})));
      connect(delImg,'onclick',partial(deleteCCRSection,secId));

      for(var i=1; i<trs.length; ++i) {
        if(findValue(editableSections,secId)>=0) { // editable section - add edit as well as del
          appendChildNodes(trs[i], TD({'class':'ccredittool'},editImg=IMG({src:'images/editpad.gif',title:'Click to Edit this entry'}),delImg=IMG({src:'images/delete.gif',title:'Click to Delete this entry'})));
          connect(editImg,'onclick',editCCRRow);
        }
        else 
          appendChildNodes(trs[i], TD({'class':'ccredittool'},delImg=IMG({src:'images/delete.gif',title:'Click to Delete this entry'})));
        connect(delImg,'onclick',partial(deleteCCRRow,secId));
      }
    });
  });
}

function updateStorageId() {
  if(resultsframe && resultsframe.document && resultsframe.document.results && resultsframe.document.results.acctId) {
    storageId = resultsframe.document.results.acctId.value;
    log("new storage id =>  " + storageId);
  }
}

/******************** Initialization functions ***********************
 *
 * Init function not reliant on any JSP dynamic content
 */
function static_init() {

  connect(window,'ccrSaved',partial(updateCCRFrame,false));
  addCCRTools(ccrframe);
  connect($('ccrframe'),'onload', function(){addCCRTools(ccrframe);});

  connect($('toggleToFromImg'),'onmouseover', function() {
    $('toggleToFromImg').src='images/toggle_active.png';
  });
  connect($('toggleToFromImg'),'onmouseout', function() {
    $('toggleToFromImg').src='images/toggle_inactive.png';
  });
  connect($('toggleToFromImg'),'onclick', function() {
    if(!checkNotFixed())
      return;
    var origToEmail = $('toEmail').value;
    $('toEmail').value = $('sourceEmail').value;
    $('sourceEmail').value = origToEmail;
    setModified();
    resolveToEmail();
  });

  yuiLoader().insert( function() {
    window.ageSexAC = new YAHOO.widget.DS_JSArray(['Male','Female','Unknown']); 
    var ac = new YAHOO.widget.AutoComplete('patientGender', 'genderACContainer', ageSexAC); 
    ac.prehighlightClassName = "yui-ac-prehighlight";
    ac.typeAhead = true;
    ac.useShadow = true;
    ac.minQueryLength = 0;
    ac.itemSelectEvent.subscribe(onFieldChange);


    var purposeDS = new YAHOO.widget.DS_JSArray(ccrPurposes); 
    var purposeAC = new YAHOO.widget.AutoComplete('ccrPurpose', 'purposesACContainer', purposeDS); 
    purposeAC.prehighlightClassName = "yui-ac-prehighlight";
    purposeAC.typeAhead = true;
    purposeAC.useShadow = true;
    purposeAC.minQueryLength = 0;
    purposeAC.alwayShowContainer = true;
    purposeAC.itemSelectEvent.subscribe(onFieldChange);
    purposeAC.itemSelectEvent.subscribe(function() {
      setTimeout(function() {$('ccrPurpose').value = $('ccrPurpose').value.replace("[Recipient]",$('toEmail').value);},0);
    });
  });

  // Some operations close their tab.  In that case the server will
  // have set ccrIndex to the value of the tab we should show instead
  // and 
  if(closeTab) {
    var replacementTab = 
      filter(function(t) { return (t != parent.currentTab) && (t.ccrIndex == parent.currentTab.ccrIndex); }, parent.getTabs());

    parent.removeTab(parent.currentTab);
    if(replacementTab.length > 0)
      parent.highlightTab(replacementTab[0],false);
  }
  showStartupMessage();
}


