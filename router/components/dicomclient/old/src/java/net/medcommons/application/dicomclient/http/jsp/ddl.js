/*
Generic DDL JavaScript routines.
*/
dojo.require("dojo.i18n.number");
dojo.require("dojo.debug.console");
dojo.require("dojo.event.*");
dojo.require("dojo.io.*");
dojo.require("dojo.widget.*");
dojo.require("dojo.lang.*");
dojo.require("dojo.widget.Button");
dojo.require("dojo.rpc.JsonService");
dojo.require("dojo.Deferred");

dojo.require("dojo.widget.Button");
dojo.require("dojo.widget.FilteringTable");
dojo.require("dojo.io.*");
dojo.require("dojo.json");
dojo.hostenv.writeIncludes(true);
function stacktrace() {
var s = "";
for (var a = arguments.caller; a !=null; a = a.caller) {
  s += "->"+funcname(a.callee) + "\n";
  if (a.caller == a) {s+="*"; break;}
}
return s;
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

function invoke(form, event, container) {
     var params = Form.serialize(form);
     if (event != null) params = event + '&' + params;
      new Ajax.Updater(container, form.action, {method:'post', postBody:params});
}

/*
Logout returns to initial state; re-enables login.
*/
function CreateTable(response){

	   alert(respose);
	   return(newTable);
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

function initialize(){
  	dojo.debug("initialize..");
}

function loadRemotely(e) {

        var kw = {
            url:    "/localDDL/Status.action",
            load:    function(type, data, evt) {
                        document.myForm.myBox.value = data;
                        dojo.byId("boxLoadTime").innerHTML = new Date();
                    },
            method: "GET"
        };
        dojo.io.bind(kw);
    }

function initAjax() {
    dojo.event.connect(dojo.byId("loadIt"), "onclick", "loadRemotely");
}


/**
  Stripes invoker /prototype
  */
function invokeStripes(form, event, handler) {
	var params = Form.serialize(form);
	if (event != null) {
		params = event + '&' + params;
		new Ajax.Request(form.action, {method:'post', postBody:params, onSuccess: handler});
	}
}


/*
	Generic routine called by all Ajax/forms
	Container is the id of the object to replace in the dom.
*/
function invoke(containerId, action, form, event) {
	log("invoke containerId=" + containerId + "form=" + form + ", event =" + event);
	var url = document.location.protocol + "//" + document.location.host + "/mdl/" + action;
	  	 // var names = getFormNames(form);
	  	  //var values = getFormValues(form);
	 var args = form.tagName.match(/form/i) ? getFormArguments(form) : new Array() ;
	 new updateElementFromHTML(containerId, url, event, args);
	 log("invoke exits");
	 return(false); // Stops subsequent processing
 }

 //