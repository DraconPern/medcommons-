

var running = true;

var displayErrorToggle = true;

function displayError(msg){
  if (displayErrorToggle){
    alert(msg);
  }
  displayErrorToggle = false;
}

function resetDisplayErrorToggle(){
  displayErrorToggle = true;
}

var rowCounter=1000;
function preprocessRow(row){
  //dojo.debug("Processing  " + row.name);
  row.Id = rowCounter--;


}
//function loadconfigurations(){
function loadDemographics(){

        dojo.io.bind({
                       url: 'DatabaseDump.action?getPatientDemographics',
                       load: demographicsCallback,
                       preventCache: true,
            error: function() {
            displayError('demographicsCallback Failed');
            running = false;
            }
        });

}
function loadCCRReferences(){
	dojo.io.bind({
                       url: 'DatabaseDump.action?getCCRReferences',
                       load: ccrReferencesCallback,
                       preventCache: true,
            error: function() {
            displayError('ccrReferencesCallback Failed');
            running = false;
            }
        });
}
function loadPatientIdentifiers(){
	dojo.io.bind({
                       url: 'DatabaseDump.action?getPatientIdentifiers',
                       load: patientIdentfiersCallback,
                       preventCache: true,
            error: function() {
            displayError('patientIdentfiersCallback Failed');
            running = false;
            }
        });
}
function loadDownloadQueue(){
	dojo.io.bind({
                       url: 'DatabaseDump.action?getDownloadQueue',
                       load: downloadQueueCallback,
                       preventCache: true,
            error: function() {
            displayError('downloadQueueCallback Failed');
            running = false;
            }
        });
}

function loadContextState(){
	dojo.io.bind({
                       url: 'DatabaseDump.action?getContextState',
                       load: contextStateCallback,
                       preventCache: true,
            error: function() {
            displayError('contextStateCallback Failed');
            running = false;
            }
        });
}

function loadCxpTransactions(){
	dojo.io.bind({
                       url: 'DatabaseDump.action?getCxpTransactions',
                       load: cxpTransactionsCallback,
                       preventCache: true,
            error: function() {
            displayError('cxpTransactionsCallback Failed');
            running = false;
            }
        });
}
function loadDicomMetadata(){

        dojo.io.bind({
                       url: 'DatabaseDump.action?getDicomMetadata',
                       load: dicomMetadataCallback,
                       preventCache: true,
            error: function() {
            displayError('dicomMetadataCallback Failed');
            running = false;
            }
        });

}
function loadDicomTransaction(){

        dojo.io.bind({
                       url: 'DatabaseDump.action?getDicomTransaction',
                       load: dicomTransactionCallback,
                       preventCache: true,
            error: function() {
            displayError('dicomTransactionCallback Failed');
            running = false;
            }
        });

}

function downloadQueueCallback(type, data, evt){

  dojo.debug("downloadQueueCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('downloadQueue', data);

  }
  }
function demographicsCallback(type, data, evt){

  dojo.debug("demographicsCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('demographics', data);

  }
  }

function ccrReferencesCallback(type, data, evt){

  dojo.debug("ccrReferencesCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('ccrReferences',data);

  }
}


function contextStateCallback(type, data, evt){

  dojo.debug("contextStateCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('contextState',data);

  }
}

function cxpTransactionsCallback(type, data, evt){

  dojo.debug("cxpTransactionsCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('cxpTransactions',data);

  }
}

function patientIdentfiersCallback(type, data, evt){

  dojo.debug("patientIdentfiersCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('patientIdentifiers',data);

  }
}
function dicomMetadataCallback(type, data, evt){

  dojo.debug("dicomMetadataCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('dicomMetadata',data);

  }
}
function dicomTransactionCallback(type, data, evt){

  dojo.debug("dicomTransactionCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving data from the server!');
    }

  }
  else{

    populateTable('dicomTransaction',data);

  }
}
/*
function updateCallback(type, data, evt){

  dojo.debug("updateCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
     running = false;
      displayError('Error when retrieving updating configuration');

    }
  }
  else{

   loadconfigurations();  // Reload the configs
  }
  }
*/
/**
 * Continue to poll if running = true;
**/
function poll(){
  //this.pollTime = new Date();
  if (running){
    triggerObject.check();
    dojo.lang.setTimeout(poll,  50);
  }
}

var triggerObject = new function(){
  var counter = 0;
    function check(){
    //dojo.debug("check counter:" + counter);
        this.counter++;
    };
}

function init(){
    dojo.debug("init()");


        dojo.event.connect(triggerObject, 'check', 'loadDemographics');
        dojo.event.connect(triggerObject, 'check', 'loadCCRReferences');
        dojo.event.connect(triggerObject, 'check', 'loadPatientIdentifiers');
        dojo.event.connect(triggerObject, 'check', 'loadCxpTransactions');
 		dojo.event.connect(triggerObject, 'check', 'loadDownloadQueue');
 		dojo.event.connect(triggerObject, 'check', 'loadDicomMetadata');
		dojo.event.connect(triggerObject, 'check', 'loadDicomTransaction');
		dojo.event.connect(triggerObject, 'check', 'loadContextState');
		

    triggerObject.check();
    //poll();
      }



  // generic enumeration
Function.prototype.forEach = function(object, block, context) {
  for (var key in object) {
    if (typeof this.prototype[key] == "undefined") {
      block.call(context, object[key], key, object);
    }
  }
};

// globally resolve forEach enumeration
var forEach = function(object, block, context) {
  if (object) {
    var resolve = Object; // default
    if (object instanceof Function) {
      // functions have a "length" property
      resolve = Function;
    } else if (object.forEach instanceof Function) {
      // the object implements a custom forEach method so use that
      object.forEach(block, context);
      return;
    } else if (typeof object.length == "number") {
      // the object is array-like
      resolve = Array;
    }
    resolve.forEach(object, block, context);
  }
};

var mySortFunction = function (a, b, direction) { return(true); }



function populateTable(tableId, theData){
    var contents = null;
    var evaledData = null;

      try {

        var demoTable=dojo.widget.byId(tableId);

		demoTable.store.clearData();

        dojo.debug(theData);


        evaledData= eval(theData);
		//dojo.debug(evaledData);
        contents = evaledData.contents;
		//dojo.debug(contents);
        demoTable.store.setData(contents);

      }
      catch(e){
        running = false;
         dojo.debug("An error was thrown");
         dojo.debug(e);
         dojo.debug(stacktrace());

      }



    }


  dojo.addOnLoad(init);
