
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

function loadCxpUploads(){


        dojo.io.bind({
                       url: 'Status.action?getCxpUploads',
                       load: cxpUploadCallback,
                        preventCache: true,
                        sync: true,
					  error: function(type, error) {
						displayError("DDL does not appear to be running. \nRefresh this page after restarting DDL");
						dojo.debug("cxpUploadCallback " + error.message);
						running = false;
						}

                      });

}
function loadCxpDownloads(){


        dojo.io.bind({
                       url: 'Status.action?getCxpDownloads',
                       load: cxpDownloadCallback,
                        preventCache: true,
                        sync: true,
						error: function(type, error) {
							displayError("DDL does not appear to be running. \nRefresh this page after restarting DDL");
							dojo.debug("cxpDownloadCallback " + error.message);
							running = false;
						}
                      });

}
function loadCCRReferences(){
	dojo.io.bind({
			url: 'Status.action?getCCRReferences',
			load: ccrReferencesCallback,
			preventCache: true,
			sync: true,
			error: function(type, error) {
				displayError("DDL does not appear to be running. \nRefresh this page after restarting DDL");
				dojo.debug("ccrReferencesCallback " + error.message);
				running = false;
			}
        });
}
function loadDicomSCP(){

       //dojo.debug("loadDicomSCP");
        dojo.io.bind({
                       url: 'Status.action?getDicomSCP',
                       load: dicomScpCallback,
                       sync: true,
                        preventCache: true,
						error: function(type, error) {
								displayError("DDL does not appear to be running. \nRefresh this page after restarting DDL");
								dojo.debug("dicomScpCallback " + error.message);
								running = false;
						}
                      });

}

function loadDicomSCU(){

      // dojo.debug("loadDicomSCU");
        dojo.io.bind({
                       url: 'Status.action?getDicomSCU',
                       load: dicomScuCallback,
                        preventCache: true,
                        sync: true,
						error: function(type, error) {
							displayError("DDL does not appear to be running. \nRefresh this page after restarting DDL");
							dojo.debug("dicomScuCallback " + error.message);
							running = false;
						}
                      });

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
function cxpUploadCallback(type, data, evt){

	//dojo.debug("cxpUploadCallback type = " + type + ", evt = " + evt);
	if (type == 'error'){
	  if (running){
		  running = false;
		  displayError('Error when retrieving  cxp upload data from the server!');
		}

	}
	else{

		populateCxpTable("cxpUpload", data);
	}
  }

function allCallbacks(type, data, evt){
	dojo.debug("allCalbacks type = " + type + ", evt = " + evt);
	if (type == 'error'){
	  if (running){
		  running = false;
		  displayError('Error when retrieving updating status on the server!');
		}

	}
	else{
		dojo.debug("should update status next poll() iteration");
	}
}

function cxpDownloadCallback(type, data, evt){

	//dojo.debug("cxpDownloadCallback type = " + type + ", evt = " + evt);
	if (type == 'error'){
	  if (running){
		 running = false;
		  displayError('Error when retrieving  cxp download data from the server!');

		}
	}
	else{

		populateCxpTable("cxpDownload", data);
	}
  }
function dicomScpCallback(type, data, evt){

	//dojo.debug("dicomScpCallback type = " + type + ", evt = " + evt);
	if (type == 'error'){
	  if (running){
		 running = false;
		  displayError('Error when retrieving  dicomScpCallback from the server!');
		}
	}
	else{

		populateDicomTable("dicomScp", data);
	}
  }


function dicomScuCallback(type, data, evt){

	//dojo.debug("dicomScuCallback type = " + type + ", evt = " + evt);
	if (type == 'error'){
	  if (running){
		  running = false;
		  displayError('Error when retrieving  dicomScuCallback from the server!');

		}
	}
	else{

		populateDicomTable("dicomScu", data);
	}
  }

 function updateServerBind(contentValues){

        dojo.io.bind({
			url: 'StatusUpdate.action?update',
			load: allCallbacks,
			content: contentValues,
			 preventCache: true,
			error: function() {
				displayError('updateServerBind Failed');
				running = false;
            }
       });
 }

function cancelJob(jobType, id){
	var contentValues = new Array();
	contentValues["command"]="CANCEL_JOB";
	contentValues["jobType"]=jobType;
	contentValues["jobId"] = id;
	updateServerBind(contentValues);
	updateNow();

}

function retryJob(jobType, id){
	var contentValues = new Array();
	contentValues["command"]="RETRY_JOB";
	contentValues["jobType"]=jobType;
	contentValues["jobId"] = id;
	updateServerBind(contentValues);
	updateNow();

}

function deleteJob(jobType, id){
	var contentValues = new Array();
	contentValues["command"]="DELETE_JOB";
	contentValues["jobType"]=jobType;
	contentValues["jobId"] = id;
	updateServerBind(contentValues);
	updateNow();

}
function deletePendingJob(jobType, id){
	var contentValues = new Array();
	contentValues["command"]="DELETE_PENDING_JOB";
	contentValues["jobType"]=jobType;
	contentValues["jobId"] = id;
	updateServerBind(contentValues);
	updateNow();

}

function mergeToPending(jobType, id){
	var contentValues = new Array();
	contentValues["command"]="MERGE_TO_PENDING_QUEUE";
	contentValues["jobType"]=jobType;
	contentValues["jobId"] = id;
	updateServerBind(contentValues);
	updateNow();

}
function createVoucherAccount(jobType, id){
	var contentValues = new Array();
	contentValues["command"]="CREATE_VOUCHER_ACCOUNT";
	contentValues["jobType"]=jobType;
	contentValues["jobId"] = id;
	updateServerBind(contentValues);
	updateNow();

}
function clearAllJobs(){
	running = true;
 	var contentValues = new Array();
 	contentValues["command"]="CLEAR_ALL";
 	updateServerBind(contentValues);
 	updateNow();
  return false;
 }

function clearDemographics(){
	running = true;
 	var contentValues = new Array();
 	contentValues["command"]="CLEAR_DEMOGRAPHICS";
 	updateServerBind(contentValues);
 	updateNow();
  return false;
 }

function clearCompletedJobs(){
	running = true;
 	var contentValues = new Array();
 	contentValues["command"]="CLEAR_COMPLETED";
 	updateServerBind(contentValues);
 	updateNow();
  return false;
 }

 function updateServerStatus(formValues){

    var contentValues = new Array();

    for (i in formValues){

    	e = formValues[i];
    	contentValues[e.name] = e.value ;
    }

	updateServerBind(contentValues);

}

function updateNow(){
	triggerObject.check();
	//dojo.lang.setTimeout(poll,  100);
}
/**
 * Continue to poll if running = true;
**/
var firstTime = true;
function poll(){
	//this.pollTime = new Date();
	if (running){
		triggerObject.check();
		var timeDelay = 4000;
		if (firstTime){
			timeDelay = 500;
			firstTime = false;
		}
		dojo.lang.setTimeout(poll,  timeDelay);
	}
}

var triggerObject = new function(){
	var counter = 0;
    function check(){
        this.counter++;
    }
};

function patientNameFilter(name){
			return (true);
		}
function applyPatientName(key){
			dojo.widget.byId(key).setFilter("patientName", nameFilter);
		}

function reload(){
	loadCxpUploads();
	loadCxpDownloads();
	loadDicomSCP();
	loadDicomSCU();
	loadCCRReferences();

}
function init(){
		//dojo.debug("init()");

		dojo.event.connect(triggerObject, 'check', 'reload');
        //dojo.event.connect(triggerObject, 'check', 'loadCxpUploads');
        //dojo.event.connect(triggerObject, 'check', 'loadCCRReferences');
		//dojo.event.connect(triggerObject, 'check', 'loadCxpDownloads');
		//dojo.event.connect(triggerObject, 'check', 'loadDicomSCP');
		//dojo.event.connect(triggerObject, 'check', 'loadDicomSCU');
		poll();
      }
/*
 * var action="<button dojoType=\"Button\" onclick='" +
			  "retryJob('CXP',"  + row.id + "\")'>Retry</button>";
 */

function makeJobActionButton(actionType, actionString, jobType, jobId){
	var buttonString = "<button dojoType=\"Button\" onclick='";
	buttonString += actionType;
	buttonString +="(\"";
	buttonString +=jobType;
	buttonString +="\",\"";
	buttonString += jobId;
	buttonString += "\")'>";
	buttonString += actionString;
	buttonString +="</button>";
	return(buttonString);
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

	/*
	var _sj_1391112 = {
	bytesTransferred:27293617,
	displayName:"Prior 4 view screening",
	elapsedTime:0,
	id:8, nSeries:0,
	patientName:"IHEMammoTest^Current and prior 4 view different size pixels",
	retryCount:1, status:"Error", statusMessage:"Unable to query account settings for account 1117658438174637",
	timeStarted:1184101792851,
	totalBytes:27265028,
	totalImages:0,
	transactionType:"PUT"};
	*/
var cxpRowCounter = 1000;
function preprocessCXPRow(row){
	//dojo.debug("Processing CXP row " + row.patientName);
	if ((row == null) || (row.patientName == null) || (row.patientName == undefined)){
		dojo.debug("Row skipped for " + row);
	}
	else{
	    var currentTime = new Date().getTime();
	    var startedAgo = currentTime - row.timeStarted;

		var elapsedTime = row.elapsedTime;
		var bytesTransferred = row.bytesTransferred;
		var totalBytes = row.totalBytes;
		if (totalBytes == 0.0){
			row.formattedTotal = formatTotalBytes(bytesTransferred);
			if (row.status=="Complete")
				row.percentCompleted = "100.0%";
			else
				row.percentCompleted = "??%";
		}
		else{
			row.formattedTotal = formatTotalBytes(totalBytes);
			row.percentCompleted = formatPercentDone(bytesTransferred, totalBytes) + "%";
		}

		row.formattedTime = formatElapsedTime(elapsedTime);
		row.formattedStatus = row.status;
	    row.formatedStartedAgo=formatElapsedTime(startedAgo) + " ago";
	    row.statusAction="";

		if (row.patientName===null){
			row.patientName="";
		}
		if (row.status == "Error"){
			row.formattedStatus = row.status + "<br/>" + row.statusMessage;
			row.formattedTime = "";
	        row.percentCompleted = "";
	        //makeJobActionButton(actionType, actionString, jobType, jobId)
	        var action1= makeJobActionButton("retryJob", "Retry", "CXP", row.id);
	        var action2= makeJobActionButton("deleteJob", "Delete", "CXP", row.id);

	        row.statusAction=action1 + "<br/>" + action2;
		}
		if (row.status == "Permanent Error"){
			row.formattedStatus = row.status + "<br/>" + row.statusMessage;
			row.formattedTime = "";
	        row.percentCompleted = "";
	        //makeJobActionButton(actionType, actionString, jobType, jobId)
	        var action=makeJobActionButton("deleteJob", "Delete", "CXP", row.id);

	        row.statusAction=action;
		}
		else if (row.status == "Cancelled"){
			row.formattedStatus = row.status + "<br/>" + row.statusMessage;
			row.formattedTime = "";
	        row.percentCompleted = "";

	        var action1= makeJobActionButton("retryJob", "Retry", "CXP", row.id);
	        var action2= makeJobActionButton("deleteJob", "Delete", "CXP", row.id);

	        row.statusAction=action1 + "<br/>" + action2;
		}
		else if (row.status == "WaitPendingMatch"){
			row.formattedStatus = "Pending";
			row.formattedTime = "";
	        row.percentCompleted = "";

	       
	        var action1= makeJobActionButton("mergeToPending", "Merge to Pending", "CXP", row.id);
	        var action2= makeJobActionButton("deletePendingJob", "Delete", "CXP", row.id);
			var action3= makeJobActionButton("createVoucherAccount", "CreateVoucherAccount", "CXP", row.id);
	        row.statusAction= action1 + "<br/>" + action2 + "<br>" + action3;
		}
		else if (row.status == "Active"){
			//row.formattedStatus = row.status;
	        elapsedTime = currentTime - row.timeStarted;
	        row.formattedStatus = row.status;
	        if ((row.statusMessage != null) && (row.statusMessage != ""))
	         	row.formattedStatus += "<br/>" + row.statusMessage;
	        row.formattedTime = formatElapsedTime(elapsedTime);
	        row.statusAction=makeJobActionButton("cancelJob", "Cancel", "CXP", row.id);
		}
		else if (row.status == "Complete"){

	        row.statusAction=makeJobActionButton("deleteJob", "Delete", "CXP", row.id);
	        row.formattedStatus = row.status;
	        if ((row.statusMessage != null) && (row.statusMessage != ""))
	         	row.formattedStatus += "<br/>" + row.statusMessage;

	        if ((row.viewUrl != null) && (row.viewUrl != '')){
				row.patientName = "<a href=\"" + row.viewUrl + "\">" + row.patientName + "</a>";
	        }
		}
		else if (row.status == "Queued"){
			if (row.cxpEndpoint == "UNKNOWN"){
				row.formattedStatus = row.status + "<br/> Waiting; not logged in";
			}
	        row.statusAction=makeJobActionButton("deleteJob", "Delete", "CXP", row.id);

		}



	    row.Id = cxpRowCounter--;


		//dojo.debug("patient " +row.patientName + " totalBytes is " + totalBytes + ", formatted is " + row.formattedTotal);

	}

}

var dicomRowCounter=1000;
function preprocessDicomRow(row){
	//dojo.debug("Processing DICOM row " + row.patientName);
	var currentTime = new Date().getTime();


	var bytesTransferred = row.bytesTransferred;
	var totalBytes = row.totalBytes;
	row.formattedTotal = formatTotalBytes(totalBytes);
	row.percentCompleted = formatPercentDone(bytesTransferred, totalBytes) + "%";
	row.formattedCompletionTime = "";

	row.formattedDescription = row.studyDescription;
	if (row.seriesDescription !== undefined){
		row.formattedDescription+= " / " + row.seriesDescription;
	}
	row.formattedStatus = row.status;
	if (row.status == "Error"){
		row.formattedStatus = row.status + "<br/>" + row.statusMessage;
		row.percentCompleted = "";

		var action1 =makeJobActionButton("retryJob", "Retry", "DICOM", row.id);
		var action2= makeJobActionButton("deleteJob", "Delete", "DICOM", row.id);
		row.statusAction= action1 + "<br/>" + action2;
	}
	if (row.status == "Permanent Error"){
			row.formattedStatus = row.status + "<br/>" + row.statusMessage;
	        row.percentCompleted = "";
	        var action=makeJobActionButton("deleteJob", "Delete", "DICOM", row.id);

	        row.statusAction=action;
		}
	else if (row.status == "Active"){
		row.formattedStatus = row.status;
		var idleTime = currentTime - row.lastModifiedTime;
		var idleTimeSec = idleTime * 1000;
		// Need to get value of 15 from the web server - this should match the DICOMTimeout value.
		var timeoutSec = ((1000 * 15) - idleTime)/1000.0;
		if (timeoutSec < 0.0) timeoutSec = 0.0;
		if (idleTimeSec < 5.0){
			// Don't display timeout while series still arriving. 5 seconds is a guess.
			row.formattedCompletionTime = formatElapsedTime(timeoutSec*1000);
		}
		else{
			row.formattedCompletionTime = "Images arriving";
		}
		 row.statusAction=makeJobActionButton("cancelJob", "Cancel", "DICOM", row.id);
	}

	else if (row.status == "Complete"){

	        row.statusAction=makeJobActionButton("deleteJob", "Delete", "DICOM", row.id);
		}

	row.Id = dicomRowCounter--;
	row.Style="-moz-user-select: none;";
	//dojo.debug(" row is now :" + row);
	//dojo.debug("totalBytes is " + totalBytes + ", formatted is " + row.formattedTotal);
}

function populateCxpTable(tableId, theData){
	cxpRowCounter = 1000;
    var contents = null;
			try {

				w=dojo.widget.byId(tableId);
				//dojo.debug(theData);




			 	junk= eval(theData);

				contents = junk.contents;

			}
			catch(e){
				running = false;
				 dojo.debug("An error was thrown");
				 dojo.debug(e);
				 dojo.debug(stacktrace());

			}

			try{
				validRows = new Array();
				validRowCounter = 0;
				w.store.clearData();
				if (contents.length>0){
					  for (i=0;i<contents.length;i++){
						    if ((contents[i].patientName == undefined) || (contents[i].patientName == null)){
							  ;// skip

						  }
						  else{
							  preprocessCXPRow(contents[i]);
							  validRows[validRowCounter] = contents[i];
							  validRowCounter=validRowCounter+1;
							  try{
								  w.store.addData(contents[i]);
					          }
							  catch(e1){
								  dojo.debug("Error processing CXP row:" + e1);
								  dojo.debug(stacktrace());
								  forEach(contents[i],dojo.debug);
							  }

						  }
					  }

					 // dojo.debug("CXP Contents is of size " + contents.length);
					  //dojo.debug("  valid rows " + validRows.length);
					  /*
					  for (i=0;i<contents.length;i++){
						  aRow = contents[i];
						  for (j=0;j<aRow.length;j++){
							  dojo.debug("[" + i + "][" + j + "]=" + aRow[j]);
						  }
					  }
						*/

				}
				else{
					dojo.debug("Empty table");
				}

			}
			catch(e){
			    running = false;
				dojo.debug("Error setting data to widget");
				dojo.debug(e);
			    dojo.debug(stacktrace());
			}

		}

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

function populateDicomTable(tableId, theData){
	dicomRowCounter = 1000;
    var contents = null;
			try {
				w=dojo.widget.byId(tableId);

				//dojo.debug(theData);





			 	junk= eval(theData);

				contents = junk.contents;

			}
			catch(e){
			     running = false;
				 dojo.debug("An error was thrown");
				 dojo.debug(e);
				 dojo.debug(stacktrace());
			}

			try{
				validRows = new Array();
				validRowCounter = 0;
				w.store.clearData();
				if (contents.length>0){

					  for (i=0;i<contents.length;i++){
					   if ((contents[i].patientName == undefined) || (contents[i].patientName == null)){
							  ;// skip

						  }
						  else{
							 preprocessDicomRow(contents[i]);
							 validRows[validRowCounter] = contents[i];
							 validRowCounter=validRowCounter+1;
							try{
								  w.store.addData(contents[i]);
					          }
							  catch(e1){

								  forEach(contents[i],dojo.debug);
							  }
					       }
					  }

				}
				else{
					dojo.debug("Empty table");
				}

			}
			catch(e){
			    running = false;
				dojo.debug("Error setting data to widget");
				dojo.debug(e);
				dojo.debug(stacktrace());
			}

		}
	dojo.addOnLoad(init);



