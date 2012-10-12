

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
function loadconfigurations(){


        dojo.io.bind({
                       url: 'Configure.action?getConfigurations',
                       load: configurationCallback,
                       preventCache: true,
           			   error: function(type, error) {
						displayError('configurationCallback Failed ' + error.message);
						running = false;
			            }
        });

}



 function updateConfigurations(formValues){

    var contentValues = new Array();

    for (i in formValues){

    	e = formValues[i];
    	contentValues[e.name] = e.value ;
    }

/*
 *  dojo.io.bind({
			url: 'ConfigUpdate.action?update',
			load: updateCallback,
			content: contentValues,
			preventCache: true,
			sync: "true",
			error: function(type, error) {
			displayError('updateCallback Failed ' + error.message);
			running = false;
            }
       });
 */
        dojo.io.bind({
			url: 'ConfigUpdate.action?update',
			content: contentValues,
			preventCache: true,
			sync: "true",
			error: function(type, error) {
			displayError('updateCallback Failed ' + error.message);
			running = false;
            }
       });

}

function configUpdate(formId){
		var updateValues = dojo.byId(formId);
		if (null == updateValues){
			alert("Form " + formId + " not defined; no callback possible");
		}
		var formElements = new Array();
		for (var i in updateValues.elements){
	    	var e = updateValues.elements[i];
	    	if ((e!=null) && (e.name != undefined) && (e.value != undefined)){
		    	var values = new Object();
		    	values.name=e.name;
		    	values.value=e.value;
		    	formElements[i] = values;
	    	}

		}
		dojo.debug("formId=" + formId + ", updateValues= " + formElements);
		updateConfigurations(formElements);
}
function configurationCallback(type, data, evt){

  dojo.debug("configurationCallback type = " + type + ", evt = " + evt);
  if (type == 'error'){
    if (running){
      running = false;
      displayError('Error when retrieving  cxp upload data from the server!');
    }

  }
  else{

    populateConfigurationTables(data);

  }
  }

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
        this.counter++;
    }
    
};

function getConfigEntry(data, name, description){
	var match = null;
	for (var i in data){
	    var row = data[i];
		if ( row["name"] == name){
			preprocessRow(row);
			row["description"]=description;
			match=row;
           // dojo.debug(match);
			break;
		}
	}
	return(match);
}

function getConfigTimeEntry(data, name, description){
	var match = null;
	for (var i in data){
	    var row = data[i];
		if ( row["name"] == name){
			preprocessRow(row);
			row["description"]=description;
			match=row;
			var originalValue = row.value;
			row.value=new Date(parseInt(row.value)).toLocaleString();

           // dojo.debug(match);
			break;
		}
	}
	return(match);
}
/*
 *
 * <input type="text" name="city" class="medium"
							dojoType="ValidationTextBox"
							trim="true"
							required="true"
							ucfirst="true" />
 */
function getEditableConfigEntry(data, name, description){
	var match = null;
	for (var i in data){
	    var row = data[i];
		if ( row["name"] == name){
			preprocessRow(row);
			row["description"]= description;
			var originalValue = row.value;
			row.value="<input type=\"text\" name=\"" + name + "\" dojoType=\"ValidationTextBox\"  value=\"" +
			  originalValue + "\" trim=\"true\" />";
			match=row;
          // dojo.debug(match);
			break;
		}
	}
	return(match);
}
function getFileConfigEntry(data, name, description){
	var match = null;
	for (var i in data){
	    var row = data[i];
		if ( row["name"] == name){
			preprocessRow(row);
			row["description"]=description;
			var originalValue = row.value;
			row.value="<input type=\"file\" name=\"" + name + "\"  value=\"" +
			  originalValue + "\" trim=\"true\" />";
			match=row;
          // dojo.debug(match);
			break;
		}
	}
	return(match);
}
function getExportMethodCombobox(data, name, description){
	var match = null;
	for (var i in data){
	    var row = data[i];
	    var originalValue = row.value;
		if ( row["name"] == name){
			preprocessRow(row);
			row["description"]=description;
			var combo = "";
			combo += "<select name=\"" + name +"\">";
			if (row.value=="CSTORE"){
			 	combo += "<option value=\"CSTORE\" SELECTED>CSTORE</option>";
			 	combo += "<option value=\"FILE\">FILE</option>";
			}
			else{
				combo += "<option value=\"CSTORE\">CSTORE</option>";
			 	combo += "<option value=\"FILE\" SELECTED>FILE</option>";
			}
		 	combo += "</select>";
			row.value=combo;
			match=row;
          // dojo.debug(match);
			break;
		}
	}
	return(match);


  
    }
  /*
function patientNameFilter(name){
      return (true);
    }
function applyPatientName(key){
      dojo.widget.byId(key).setFilter("patientName", nameFilter);
    }
  */
function init(){
    dojo.debug("init()");


        dojo.event.connect(triggerObject, 'check', 'loadconfigurations');
    //dojo.event.connect(triggerObject, 'check', 'loadcontexts');
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

var mySortFunction = function (a, b, direction) { return(true); };



function populateConfigurationTables(theData){
    var contents = null;
      try {

        var ddlTable=dojo.widget.byId("ddlConfiguration");
		var dicomTable=dojo.widget.byId("dicomConfiguration");
		ddlTable.store.clearData();
		dicomTable.store.clearData();



        dojo.debug(theData);


        evaledData= eval(theData);

        contents = evaledData.contents;

      }
      catch(e){
        running = false;
         dojo.debug("An error was thrown");
         dojo.debug(e);
         dojo.debug(stacktrace());

      }

      try{
      	var val;
      	val =  getEditableConfigEntry(contents, "dicomRemoteHost", "Hostname of the DICOM device images are sent to");
		dicomTable.store.addData(val);
		val = getEditableConfigEntry(contents, "dicomRemoteAeTitle", "AE title of the DICOM device that images are sent to");
		dicomTable.store.addData(val);
		val = getEditableConfigEntry(contents, "dicomRemotePort", "Port of the DICOM device that images are sent to");
		dicomTable.store.addData(val);
		val = getEditableConfigEntry(contents, "dicomLocalAeTitle", "AE title used by DDL; other devices require this in their configurations");
		dicomTable.store.addData(val);
		val = getEditableConfigEntry(contents, "dicomLocalPort", "DICOM Port used by DDL; other devices require this in their configurations");
		dicomTable.store.addData(val);
		// Set this to editable once true timeout time can be calculated.
		val = getConfigEntry(contents, "dicomTimeout", "Timeout (in seconds) that the DDL DICOM SCP waits before declaring series complete");
		dicomTable.store.addData(val);
		val = getConfigEntry(contents, "Hostaddress", "This is the host address of the DDL");
		dicomTable.store.addData(val);
		val = getConfigEntry(contents, "DICOMEcho","Results from DICOM echo to remote device in current configurations");
		dicomTable.store.addData(val);
		val = getEditableConfigEntry(contents, "exportDirectory",
		"Directory data will be placed in if the export method is file. For Windows this is a string of the form C:\\Downloads, for Macintoshes it is of the form /Users/username/Downloads. The directory must exist. ");
		dicomTable.store.addData(val);
		val = getExportMethodCombobox(contents, "exportMethod", "FILE for saving downloaded DICOM to a folder; CSTORE for sending downloaded DICOM to remote DICOM device");
		dicomTable.store.addData(val);
		
		val = getEditableConfigEntry(contents, "AutomaticUploadToVoucher",
		"'true' means that incoming DICOM studies are automatically uploaded to a voucher-generated account; false means they stay in the queue for future user action.");
		dicomTable.store.addData(val);
		
		val = getEditableConfigEntry(contents, "useRawFileUploads",
		"'true' means that DICOM will be uploaded using the more efficient multipart form protocol");
		dicomTable.store.addData(val);
		
		val = getEditableConfigEntry(contents, "proxyAddress", "Name of HTTP proxy DDL should use for accessing the internet");
		dicomTable.store.addData(val);
		
		val = getEditableConfigEntry(contents, "proxyAuthUserName", "User name (if any) that DDL should authenticate to proxy with.");
		dicomTable.store.addData(val);
		
		val = getEditableConfigEntry(contents, "proxyAuthPassword", "Password (if any) that DDL should authenticate to proxy with.");
		dicomTable.store.addData(val);
		

		val = getConfigEntry(contents, "version", "Version of the DDL");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "DDLIdentity", "Identity of this particular DDL");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "gatewayRoot", "Current appliance");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "configurationFile", "Location of current configuration file");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "baseDirectory", "Directory used by DDL on local machine");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "localHttpPort", "Port used by DDL to listen for commands");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "senderAccountId", "Currently logged in user identifier");
		ddlTable.store.addData(val);
		val = getConfigTimeEntry(contents, "buildTime", "Time DDL was built");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "version", "Version of the DDL");
		ddlTable.store.addData(val);


		val = getConfigEntry(contents, "displayUploadedCCRPopup", "Is popup displayed after CXP upload?");
		ddlTable.store.addData(val);

		val = getConfigEntry(contents, "auth", "Authorization string");
		ddlTable.store.addData(val);
		/*
		val = getConfigEntry(contents, "cxpHost");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "cxpPath");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "cxpPort");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "cxpProtocol");
		ddlTable.store.addData(val);
		*/
		val = getConfigEntry(contents, "groupAccountId", "Global identifier for the group uploaded data will be placed in");
		ddlTable.store.addData(val);
		val = getConfigEntry(contents, "groupName", "Name of the group uploaded data will be placed in");
		ddlTable.store.addData(val);


      }
      catch(e){
          running = false;
        dojo.debug("Error setting data to widget");
        dojo.debug(e);
          dojo.debug(stacktrace());
      }

    }


  dojo.addOnLoad(init);
