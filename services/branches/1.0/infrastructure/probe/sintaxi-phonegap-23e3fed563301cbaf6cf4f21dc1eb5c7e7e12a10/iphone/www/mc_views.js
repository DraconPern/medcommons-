
var loggedIn = false;
function notLoggedIn() {

 if (loggedIn==false) {
 console.log ("should be logged in");
	 debug.log ("should be logged in");
 return true;
 }
 else return false;
 }
 //
function loginError(msg)
{
    var views = document.getElementById('views');
    var loginerror = document.getElementById('loginerror');
       document.getElementById('errbox').innerHTML =  msg;
    if (views && views.object && loginerror) {
		debug.log("Presenting login error "+msg);
             views.object.setCurrentView(loginerror, false,true);
    }
}
 
 
//
// Function: flipToFront(event)
// Flip to the front view to show the login form
//
function flipToFront(event)
{
    var views = document.getElementById('views');
    var front = document.getElementById('front');
    if (views && views.object && front) {
      if(getCookie('mc'))
       debug.log ("Presenting Front View to User with Cookie set"); else
           debug.log ("Presenting Front View to User with no Cookie");
        cancelOutstandingTimedRequests();
        views.object.setCurrentView(front, false,true);
    }
}

//
// Function: flipToSettings(event)
// Flip to the back view to present user customizable settings
//
function flipToSettings(event)
{
  if (notLoggedIn()) flipToFront(event); else {
    var views = document.getElementById('views');
    var settings = document.getElementById('settings');
    if (views && views.object && settings) {
    
        debug.log ("Presenting Settings View to User");
           cancelOutstandingTimedRequests();
        views.object.setCurrentView(settings,false,true);
    }
 }
}

//
// Function: flipToDebug(event)
// Flip to the back view to present user customizable settings
//
function flipToDebug(event)
{
  if (notLoggedIn()) flipToFront(event); else {

    var views = document.getElementById('views');
    var info = document.getElementById('info');
    if (views && views.object && info) {
    
       debug.log ("Presenting Debug Info  View to User");
           cancelOutstandingTimedRequests();
        views.object.setCurrentView(info,false,true);
    }
    }
}

//
// Function: flipToDebug(event)
// Flip to the back view to present user customizable settings
//
function flipToPatientList(event)
{
  
  if (notLoggedIn()) flipToFront(event); else {

    var views = document.getElementById('views');
    var patients = document.getElementById('patients');
  
    if (views && views.object && patients) {    
        debug.log ("Presenting Patient List View to User");
        cancelOutstandingTimedRequests();
           
        loadPatientList (); // start one going
                
	     patientListRefreshID = setInterval(
         function(){ var count = loadPatientList(); }, 
         1000*20); // every 10 seconds?  
         //if ( patientCount == 0)  
            // views.object.setCurrentView(nopatients,false,true); else
             views.object.setCurrentView(patients,false,true);
    }
  }
}
var perfMachineRefreshID = 0;

function flipToPerf(event)
{

  if (notLoggedIn()) flipToFront(event); else {

    var views = document.getElementById('views');
    var perf = document.getElementById('perf');
    if (views && views.object && perf) {
    
        debug.log ("Presenting Perf View to User");
           cancelOutstandingTimedRequests();
           
        perfMachineRefreshID = perf_machine (mc_rest("ws/ijsprobermini.php"),10,0);
        views.object.setCurrentView(perf,true,true);
    }
    }
}

function gotoHURLButtonClicked ()
{

  if (notLoggedIn()) flipToFront(event); else {
   cancelOutstandingTimedRequests(); document.location = ctx.patientviewer; // that should be all
            }

}
function gotoPatient (p)
{

  if (notLoggedIn()) flipToFront(event); else {

    cancelOutstandingTimedRequests();
    ctx.patientid=p.PatientIdentifier;
    ctx.patientname=p.PatientGivenName + ' ' + p.PatientFamilyName;    
    ctx.patientdemog=p.PatientAge + ' ' + p.PatientSex;
    ctx.patientviewer=p.ViewerURL;
    ctx.patienttime = p.CreationDateTime;
    document.getElementById('patientMcid').innerText=ctx.patientid;
    document.getElementById('patientDemog').innerText=ctx.patientname
    +' '+ ctx.patientdemog;
    document.getElementById('patientTime').innerHTML= '<small>'
                                +(new Date(p.CreationDateTime*1000)).toLocaleString()+'</small>';               
   // start the ajax request going for the activity list
        loadActivityList (ctx.patientid); // = user
    // and ask for a refresh every now and then
	  activityListRefreshID =  setInterval(function(){ loadActivityList(ctx.patientid) }, 1000*20); // every 10 seconds?              
      activityListDataSource._rowData=[];
      activityListDataSource._clickData=[];
    debug.log ("Present Patient View for "+p.PatientIdentifier); views.object.setCurrentView(document.getElementById('onepatient'),true,true);
 }
}
function cancelOutstandingTimedRequests ()
{
// call this whenever switching views to calm things down :=)
if (patientListRefreshID!=0) clearInterval(patientListRefreshID);

if (activityListRefreshID!=0) clearInterval(activityListRefreshID);

if (perfMachineRefreshID!=0) clearInterval (perfMachineRefreshID);

}



// This object implements the dataSource methods for the list.
var patientListRefreshID =0;
var patientCount =0;
var patientListDataSource = {
	
	// Sample data for the content of the list. 
	// Your application may also fetch this data remotely via XMLHttpRequest.
	_rowData: [],
    _clickData: [],
	
	// The List calls this method to find out how many rows should be in the list.
	numberOfRows: function() {
		return this._rowData.length;
	},
	
	// The List calls this method once for every row.
	prepareRow: function(rowElement, rowIndex, templateElements) {
		// templateElements contains references to all elements that have an id in the template row.
		// Ex: set the value of an element with id="label".
		if (templateElements.patientListItemLabel) {
			templateElements.patientListItemLabel.innerText = this._rowData[rowIndex];
		}

		// this function was setup when the data was inserted.
		rowElement.onclick = function () { gotoPatient(patientListDataSource._clickData[rowIndex]) };
	
	}
};
var updatePatientList = function (user)
            {
            var counter = user.practice.patients.length; 
            for( var i = 0; i < counter; i++ )
                { 
                var p = user.practice.patients[i];
                patientListDataSource._rowData[i] = p.PatientIdentifier + '\n ' 
                 + p.PatientGivenName + ' ' + p.PatientFamilyName + '\n '
                  + (new Date(p.CreationDateTime*1000)).toLocaleString() +'';
                                  
                patientListDataSource._clickData[i] = p;
                console.log (patientListDataSource._rowData[i]);                
               
                }                         
                document.getElementById("patientList").object.reloadData();
                 console.log ("Update patient list count is "+ counter);
                if (counter>0) views.object.setCurrentView(patients,false,true); //careful here
                else views.object.setCurrentView(nopatients,false,true); //careful here

                return counter; 
            };
            
var loadPatientList =  function ()
            { // this gets called every N seconds
     
            jQuery.getJSON(mc_rest('ws/ijspatientlist.php'),
                    {mcid: ctx.accid} ,
                function(res) {
                if(res.status != 'ok') {                
                    console.log('An error occurred with patientlist: \n\n'+res.message);
                 
                    loginError(res.message);
                    return true;
                    }
                    else
                    {        patientCount= updatePatientList (res.result); // = user
                           
                    }
                   });
              } //load PatientList
   /// ActivityList/Log
                
// This object implements the dataSource methods for the list.
var activityListRefreshID = 0;
var activityListDataSource = {
	
	// Sample data for the content of the list. 
	// Your application may also fetch this data remotely via XMLHttpRequest.
	_rowData: [],
    _clickData: [],
	
	// The List calls this method to find out how many rows should be in the list.
	numberOfRows: function() {
		return this._rowData.length;
	},
	
	// The List calls this method once for every row.
	prepareRow: function(rowElement, rowIndex, templateElements) {
		// templateElements contains references to all elements that have an id in the template row.
		// Ex: set the value of an element with id="label".
		if (templateElements.activityListItemLabel) {
			templateElements.activityListItemLabel.innerHTML = this._rowData[rowIndex];
		}

		// this function was setup when the data was inserted.
		rowElement.onclick = function () {
                var p = activityListDataSource._clickData[rowIndex];
                var out = 'activity on '+  
                  p.date+ ' '+ p.status + ' '
                  + p.tracking  +'';
                alert (out)
                  };
	
	}
};
var updateActivityList = function (ccrs)
            {
            for( var i = 0; i <ccrs.length; i++ )
                { 
                var p = ccrs[i];
                activityListDataSource._rowData[i] ='<small>'+  
                  p.date+ ' '+ p.status + ' '
                  + p.tracking  +'</small>';
                                  
                activityListDataSource._clickData[i] = p;
                //console.log (activityListDataSource._rowData[i]);                
               
                }                         
                document.getElementById("activityList").object.reloadData(); 
            };
            
var loadActivityList =  function ()
            { // this gets called every N seconds
            console.log ("loadactivitylist " );
            jQuery.getJSON(mc_rest('ws/ijsactivitylist.php'),
                    {mcid: ctx.accid} ,
                function(res) {
                if(res.status != 'ok') {                
                    debug.log('An error occurred with activitylist: \n\n'+res.message);
                   loginError(res.message);
                    return true;
                    }
                    else
                    {         updateActivityList (res.ccrs); // = user
                    }
                   });
                 } //load activityList
                       
                 
                 
var buttonLoginClicked = function (event)
{
ctx.email = document.getElementById('email').value;
ctx.password = document.getElementById('password').value;
    console.log ("buttonLoginclicked loggedin is " + loggedIn);
    jQuery.getJSON(mc_rest('ws/ijslogin.php'),
        {email: ctx.email, password: ctx.password } ,
        function(res) {
        debug.log ("login status " + res.status);
                if(res.status != 'ok') {                
                    debug.log('An error occurred with  your login: \n\n'+res.message);
              loginError(res.message);
                    return true;
                    }
                    else
                    {    
                      window.result = res.result;
                    var result = res.result;  
   //absord fields as they come back
    ctx.accid = result.accid;
    ctx.practiceid= result.practice.accid;
    ctx.practicename=result.practice.practicename;
    ctx.providerid=null;
    ctx.providername=result.fn+' '+result.ln;
    ctx.patientid=null;
    ctx.patientname=null;
    ctx.patientappliance=null;              
    // If we got here, login was successful, write email and password to sqlite
    updateDB();
                    // log stuff to debug console
        document.getElementById('topbox').innerHTML = 
                JSON.stringify(ctx, function (key, value) { return value; });
                     
                                            
        document.getElementById('bottombox').innerHTML = 
                JSON.stringify(result.practice, function (key, value) {  return value; });
                        
                // throw the practicename at the top of the patient list
        document.getElementById('patientListTitle').innerHTML = 
                ctx.providername +' @ '+ctx.practicename;
                        document.getElementById('patientListTitleNone').innerHTML = 
                ctx.providername +' @ '+ctx.practicename;
                
                document.getElementById('onePatientTitle').innerHTML = 
                ctx.providername +' @ '+ctx.practicename;
                      
// update the window
                
       loggedIn = true; // set safety flag   
                 
                    result = res.result; // its nested
                    //
                    // okay lets go onboard or offboard based upon returned values from appliance
                    if ((result.practice.accid) && (result.practice.accid!=''))
                    {
                      flipToPatientList(event);
                      return true;
                      }
                      //
                      // okay if its a patient then goto the patient APP
                      
                 
                    document.getElementById('errbox').innerHTML =  ''; //all beautiful now
    				setvalue ('showstamp',"<img border=0 id=stamp src=/acct/stamp.php />");
                    
                    flipToSettings(event);
                    return true;
                    }
                });
  // i dont think we need this flipToFront(event);
  return true;

};


var  buttonLogoutClicked=function(event)
{      // clean out the saved email and password 
                loggedIn = false; // mark as out of here 
                document.getElementById('email').value='';
                document.getElementById('password').value='';
                initCtx();
        	    updateDB();
  jQuery.getJSON(mc_rest('ws/ijslogout.php'),
        {} ,
        function(result) {
                console.log ("Logout: " + result.status); 
          
                });
  flipToFront(event);
  return true;
};
