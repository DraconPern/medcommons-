//localhost/var/folders/4n/4n2ZAOIXGfCzdaNq6fpLK++++TI/-Tmp-/Dashcode/.uiSO216.dcprojZXDaFF/uiSO227.dcproj/project/mc_views.js// all of the lists pretty much work the same way and might be generalized, but not sure what the true benefit would be


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
		rowElement.onclick = function () { 
         //gotoPatient(patientListDataSource._clickData[rowIndex])
             var p = patientListDataSource._clickData[rowIndex];
                var out = '../interpage/index.php?db=yes&first='+   p.PatientGivenName + '&last=' + p.PatientFamilyName + 
                 '&dob='+p.PatientAge+'&hurl='+p.ViewerURL+
                 '&mc='+ctx.accid+'|'+ctx.email+'|'+ctx.providerid;
                 document.location = out;
         };
	
	}
};
var updatePatientList = function (user)
            {
           _rowData=[];
           _clickData=[];
            var counter = user.practice.patients.length; 
            for( var i = 0; i < counter; i++ )
                { 
                var p = user.practice.patients[i];
                patientListDataSource._rowData[i] = 
             p.PatientGivenName + ' ' + p.PatientFamilyName + ' - '+p.PatientAge+' '+p.PatientSex+'\n '
                 + p.Status +' '+p.ViewStatus+'\n'
                  + (new Date(p.CreationDateTime*1000)).toLocaleString() +'';
                                  
                patientListDataSource._clickData[i] = p;
                mc.log (patientListDataSource._rowData[i]);                
               
                }                         
                document.getElementById("patientList").object.reloadData();
                 mc.log ("Update patient list count is "+ counter);
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
                    mc.log('An error occurred with patientlist: \n\n'+res.message);
                 
                    remoteError('Patient List',res.message);
                    return true;
                    }
                    else
                    {    //when we complete, make sure we are still in this view before refreshing the screen
                    if (currentView=='patients')   patientCount= updatePatientList (res.result); // = user
                           
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
            
        _rowData=[];
         _clickData=[]; // start fresh
            for( var i = 0; i <ccrs.length; i++ )
                { 
                var p = ccrs[i];
                activityListDataSource._rowData[i] =  
                  p.date+ ' '+ p.status + ' '
                  + p.tracking ;
                  
                                  
                activityListDataSource._clickData[i] = p;
                mc.log (activityListDataSource._rowData[i]);                
               
                }                         
                document.getElementById("activityList").object.reloadData(); 
            };
            
var loadActivityList =  function ()
            { // this gets called every N seconds
            jQuery.getJSON(mc_rest('ws/ijsactivitylist.php'),
                    {accid: ctx.patientid} , //careful
                function(res) {
                if(res.status != 'ok') {                
                    mc.log('An error occurred with activitylist: \n\n'+res.message);
                   remoteError('Activity List',res.message);
                    return true;
                    }
                    else
                    {     //when we complete, make sure we are still in this view before refreshing the screen
                    if (currentView=='activities')      updateActivityList (res.ccrs); // = user
                    }
                   });
                 } //load activityList            
                 
                 
                    /// consentsList/Log
                
// This object implements the dataSource methods for the list.
var consentsListRefreshID = 0;
var consentsListDataSource = {
	
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
		if (templateElements.consentsListItemLabel) {
			templateElements.consentsListItemLabel.innerHTML = this._rowData[rowIndex];
		}

		// this function was setup when the data was inserted.
		rowElement.onclick = function () {
                var p = consentsListDataSource._clickData[rowIndex];
                var out = 'consents on '+  
                  p.date+ ' '+ p.status + ' '
                  + p.tracking  +'';
                alert (out)
                  };
	
	}
};
var updateConsentsList = function (ccrs)
            {
            
        _rowData=[];
         _clickData=[]; // start fresh
            for( var i = 0; i <ccrs.length; i++ )
                { 
                var p = ccrs[i];
                consentsListDataSource._rowData[i] =  
                  p.date+ ' '+ p.status + ' '
                  + p.tracking ;
                  
                                  
                consentsListDataSource._clickData[i] = p;
                mc.log (consentsListDataSource._rowData[i]);                
               
                }                         
                document.getElementById("consentsList").object.reloadData(); 
            };
            
var loadConsentsList =  function ()
            { // this gets called every N seconds
            return; 
            jQuery.getJSON(mc_rest('ws/ijsconsentslist.php'),
                    {accid: ctx.patientid} , //careful
                function(res) {
                if(res.status != 'ok') {                
                    mc.log('An error occurred with consentslist: \n\n'+res.message);
                   remoteError('ConsentsList', res.message);
                    return true;
                    }
                    else
                    {          //when we complete, make sure we are still in this view before refreshing the screen
                    if (currentView=='consents') updateConsentsList (res.ccrs); // = user
                    }
                   });
                 } //load consentsList
                       
                                
                    /// documentsList/Log
                
// This object implements the dataSource methods for the list.
var documentsListRefreshID = 0;
var documentsListDataSource = {
	
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
		if (templateElements.documentsListItemLabel) {
			templateElements.documentsListItemLabel.innerHTML = this._rowData[rowIndex];
		}

		// this function was setup when the data was inserted.
		rowElement.onclick = function () {
                var p = documentsListDataSource._clickData[rowIndex];
                var out = 'documents on '+  
                  p.dt_create_date_time+ ' '+ p.dt_type + ' '
                  + p.dt_comment  +'';
                alert (out)
                  };
	
	}
};
var updateDocumentsList = function (ccrs)
            {
            
        _rowData=[];
         _clickData=[]; // start fresh
            for( var i = 0; i <ccrs.length; i++ )
                { 
                var p = ccrs[i];
                documentsListDataSource._rowData[i] =  
            p.dt_create_date_time+ ' '+ p.dt_type + ' '
                  + p.dt_comment  +'';
                  
                                  
                documentsListDataSource._clickData[i] = p;
                mc.log (documentsListDataSource._rowData[i]);                
               
                }                         
                document.getElementById("documentsList").object.reloadData(); 
            };
            
var loadDocumentsList =  function ()
            { // this gets called every N seconds
            jQuery.getJSON(mc_rest('ws/ijsdocumentslist.php'),
                    {accid: ctx.patientid} , //careful
                function(res) {
                if(res.status != 'ok') {                
                    mc.log('An error occurred with documentslist: \n\n'+res.message);
                   remoteError('documentslist',res.message);
                    return true;
                    }
                    else
                    {      //when we complete, make sure we are still in this view before refreshing the screen
                    if (currentView=='documents')     updateDocumentsList (res.ccrs); // = user
                    }
                   });
                 } //load documentsList
                       
  