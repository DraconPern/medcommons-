var currentView ;
var loggedIn = false;
function notLoggedIn() {

 if (loggedIn==false) {
 mc.log ("should be logged in");
 return true;
 }
 else return false;
 }
 //
function internalError(msg)
{
    var views = document.getElementById('views');
    var loginerror = document.getElementById('loginerror');
       document.getElementById('errbox').innerHTML =  msg;
    if (views && views.object && loginerror) {
            currentView = 'loginerror';
             views.object.setCurrentView(loginerror, false,true);
    }
}
function remoteError(categorystring,msg)
{ loginError(categorystring + ' => ' + msg); }
function loginError(msg)

{ internalError('Error logging in' + ' => ' + msg); }
 
//
// Function: flipToFront(event)
// Flip to the front view to show the normal utility view
//
function flipToFront(event)
{
    var views = document.getElementById('views');
    var front = document.getElementById('front');
    if (views && views.object && front) {
      if (getCookie('mc'))// (false)
       { mc.log ("Already logged in Cookie set accid is "+ctx.accid);
          
          loggedIn = true;
                  // throw the practicename at the top of the patient list
        document.getElementById('patientListTitle').innerHTML = 
                ctx.providername +' @ '+ctx.practicename;
                        document.getElementById('patientListTitleNone').innerHTML = 
                ctx.providername +' @ '+ctx.practicename;
                
                document.getElementById('onePatientTitle').innerHTML = 
                ctx.providername +' @ '+ctx.practicename;
          return flipToPatientList(event);
       
          }
          else {
          loggedIn =false;
           mc.log ("Presenting Front View to User with no Cookie");
        cancelOutstandingTimedRequests();
        currentView='front';
        views.object.setCurrentView(front, false,true);
            }
       }
}


function flipToOpenView(event)
{
      var views = document.getElementById('views');
    var openview = document.getElementById('openview');
    if (views && views.object && openview) {
    
        mc.log ("Presenting Open View to User");
           cancelOutstandingTimedRequests();
           currentView='openview';
        views.object.setCurrentView(openview,false,true);
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
    
        mc.log ("Presenting Settings View to User");
           cancelOutstandingTimedRequests();
           currentView='settings';
        views.object.setCurrentView(settings,false,true);
    }
 }
}
//
// Function: flipToSettings(event)
// Flip to the back view to present user customizable settings
//
function flipToActivites(event)
{
  if (notLoggedIn()) flipToFront(event); else {
    var views = document.getElementById('views');
    var activities = document.getElementById('activities');
    if (views && views.object && activities) {
    
        mc.log ("Presenting activities View to User");
           cancelOutstandingTimedRequests();
              // start the ajax request going for the activity list
        loadActivityList (ctx.patientid); // = user
    // and ask for a refresh every now and then
	  activityListRefreshID =  setInterval(function(){ loadActivityList(ctx.patientid) }, 1000*20); // every 10 seconds? 
        currentView = 'activities';
        views.object.setCurrentView(activities,false,true);
    }
 }
}
function flipToConsents(event)
{
  if (notLoggedIn()) flipToFront(event); else {
    var views = document.getElementById('views');
    var consents = document.getElementById('consents');
    if (views && views.object && consents) {
    
        mc.log ("Presenting consents View to User");
           cancelOutstandingTimedRequests();
        loadConsentsList (ctx.patientid); // = user
    // and ask for a refresh every now and then
	  consentsListRefreshID =  setInterval(function(){ loadConsentsList(ctx.patientid) }, 1000*20); // every 10 seconds? 
        currentView = 'consents';
        views.object.setCurrentView(consents,false,true);
    }
 }
}
function flipToDocuments(event)
{
  if (notLoggedIn()) flipToFront(event); else {
    var views = document.getElementById('views');
    var documents = document.getElementById('documents');
    if (views && views.object && documents) {
    
        mc.log ("Presenting documents View to User");
           cancelOutstandingTimedRequests();
        loadDocumentsList (ctx.patientid); // = user
    // and ask for a refresh every now and then
	  documentsListRefreshID =  setInterval(function(){ loadDocumentsList(ctx.patientid) }, 1000*20); // every 10 seconds? 
         currentView = 'documents';
        views.object.setCurrentView(documents,false,true);
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
    
        mc.log ("Presenting Debug Info  View to User");
           cancelOutstandingTimedRequests();
           currentView='info';
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
        mc.log ("Presenting Patient List View to User");
        cancelOutstandingTimedRequests();
           
        loadPatientList (); // start one going
                
	     patientListRefreshID = setInterval(
         function(){ var count = loadPatientList(); }, 
         1000*20); // every 10 seconds?  
         //if ( patientCount == 0)  
            // views.object.setCurrentView(nopatients,false,true); else
            currentView = 'patients';
             views.object.setCurrentView(patients,false,true);
    }
  }
}
var perfMachineRefreshID = 0;

function flipToPerf(event)
{

//  if (notLoggedIn()) flipToFront(event); else 
  {

    var views = document.getElementById('views');
    var perf = document.getElementById('perf');
    if (views && views.object && perf) {
    
        mc.log ("Presenting Perf View to User");
           cancelOutstandingTimedRequests();
           
        perfMachineRefreshID = perf_machine (mc_rest("ws/ijsprobermini.php"),10,0);
        currentView = 'perf';
        views.object.setCurrentView(perf,true,true);
    }
    }
}

function gotoHURLButtonClicked ()
{

  if (notLoggedIn()) flipToFront(event); else {
   cancelOutstandingTimedRequests(); document.location = ctx.patientviewer; // that should be all she wrote
            }

}
function gotoPatient (p)
{

  if (notLoggedIn()) flipToFront(event); else {
// make the patient described in p be the current patient
    ctx.patientid=p.PatientIdentifier;
    ctx.patientname=p.PatientGivenName + ' ' + p.PatientFamilyName;    
    ctx.patientdemog=p.PatientAge + ' ' + p.PatientSex;
    ctx.patientviewer=p.ViewerURL;
    ctx.patienttime = p.CreationDateTime;
    gotoCurrentPatient()
    }
 
 }
 function gotoCurrentPatient()
 {
 
    cancelOutstandingTimedRequests();
   document.getElementById('patientMcid').innerText=ctx.patientid;
    document.getElementById('patientDemogD').innerText=    document.getElementById('patientDemogA').innerText=    
    document.getElementById('patientDemogB').innerText=    
    document.getElementById('patientDemogC').innerText=ctx.patientname
    +' '+ ctx.patientdemog;
    document.getElementById('patientTimeD').innerHTML= 
    document.getElementById('patientTimeA').innerHTML= 
    document.getElementById('patientTimeB').innerHTML= 
    document.getElementById('patientTimeC').innerHTML= '<small>'
                                +(new Date(ctx.patienttime*1000)).toLocaleString()+'</small>';            
             
     
    mc.log ("Present Patient View for "+ctx.patientid); 
    currentView='onepatient';
    views.object.setCurrentView(document.getElementById('onepatient'),true,true);
}
function cancelOutstandingTimedRequests ()
{
// call this whenever switching views to calm things down :=)
if (patientListRefreshID!=0) clearInterval(patientListRefreshID);

if (activityListRefreshID!=0) clearInterval(activityListRefreshID);


if (consentsListRefreshID!=0) clearInterval(consentsListRefreshID);


if (documentsListRefreshID!=0) clearInterval(documentsListRefreshID);


if (perfMachineRefreshID!=0) clearInterval (perfMachineRefreshID);

}
               
var buttonLoginClicked = function (event)
{
ctx.email = document.getElementById('email').value;
ctx.password = document.getElementById('password').value;
    mc.log ("buttonLoginclicked loggedin is " + loggedIn);
    jQuery.getJSON(mc_rest('ws/ijslogin.php'),
        {email: ctx.email, password: ctx.password } ,
        function(res) {
        mc.log ("login status " + res.status);
                if(res.status != 'ok') {                
                    mc.log('An error occurred with  your login: \n\n'+res.message);
              loginError(res.message);
                    return true;
                    }
                    else
                    {    
                      window.result = res.result;
                    var result = res.result;  
   //absord fields as they come back
   ctx.auth = result.auth;
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
  flipToFront(event);
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
                mc.log ("Logout: " + result.status); 
                flipToFront(event);  // wait for logout to complete
                });

  return true;
};
