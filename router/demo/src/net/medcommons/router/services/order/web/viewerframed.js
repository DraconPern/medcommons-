/**
 * Instruments MedCommons Viewer with support for running in an embedded frame where 
 * the parent window has menus that are hooked into the viewer functions.
 */
connect(events, 'beforeInit', function() {
    window.parent.currentTab.ccrIndex = ccrIndex;
    window.parent.currentTab.mode = 'view';
    if(features.editMode)
        window.parent.setTabIcon(null, '<img class="tabIcon" title="Switch to Edit Mode" style="position: relative; top: 1px; left: 3px;" src="images/tancycle.gif" onmouseout="this.src=\'images/tancycle.gif\'" onmouseover="this.src=\'images/redcycle.gif\'"/>');
});

connect(events,'initialized', initializeFramedView);

/**
 * Add the custom menus and buttons that adorn the top of the viewer
 * when it is running in framed mode.
 */
function initializeFramedView() {
    
    parent.addPatientHeader(getCcr());
    parent.currentTab.url = 'viewEditCCR.do';
    
    // If clicking in header, show CCR (thumbnail 0)
    withWindow(parent, function() {
        connect($('patientHeaderTable'),'onclick',function() {
          thumbnails[0].display(0);
        });
    });
    
    var buttons = [
                   { text: 'Inbox', action: 'top.location.href=accountsBaseURL+"/acct/"'},
                   { text: 'Logout', action: 'top.location.href="Logout.action"'}
                  ];
    
    if(resolve(features,'viewer.share',true)) {
        buttons.push({ text: ' Send ',   
                       img: accountsBaseURL+'/acct/images/mail-forward.png', 
                       action: 'shareForm()', 
                       tip: "Click to share with another person by email"});
        
        buttons.push({ text: ' Send to Phone',   
                       img: '/router/images/phone-forward.png', 
                       action: 'sharePhone("'+ccr.patient.medCommonsId+'")', 
                       tip: "Click to share with another person by phone"});
    }    

    log("Initializing Healthbook Features");
    if(ccr.guid && features.editMode) {
      var operatingSystem = clientOS();
      if (operatingSystem == "Windows"){
        var installed = isHealthBookInstalled();
        var editURL = generateCCRExternaEditURL();
        log ("edit url is " + editURL);
         if (installed == HEALTHBOOK_INSTALLED){
           buttons.push({ text: 'Edit With HealthBook',   
             action: 'editCCRExternally()', 
             tip: "Click to Edit this CCR with HealthBook" , 
             actionTarget: '_top',
             url: editURL  });
        }
        else 
            log("Healthbook is not installed");
      }
      // else - show nothing. We only have an editor for windows.
    }
    
    parent.setButtons(buttons);
    if(cleanPatient) 
      cleanTabs();
    fetchPatientCCRs();
    
    
    if(features.editAndSendCCR)  {
        tools.push(["Edit","editCCR();"]);
        tools.push(["Send by Email","shareForm()"]);
    }
    
    if(features.editAsNew) 
        tools.push(EDIT_AS_NEW_TOOL = ["Edit as New CCR", "editAsNew()" ]);
    
    tools.push(["Show Activity and Consents","showActivity()"]);
    tools.push(["Download CCR","downloadCCR();"]);
    tools.push(["Show All Thumbnails","showThumbnailGrid();"]);
    tools.push([
                { text: "Download All Documents", id: 'downloadAll', onclick: {fn: function(){ downloadAllDocuments(); }} } ]); 
    
    forEach([
         // ["Change DICOM Overlay","toggleOverlay();"],
         ["Print Document","printWindow();"],
         ["Help","","window.contents.showViewerHelp();"]/* , 
         CONFIRM_SELECTION_TOOL =  [CCR_ATTACHMENTS_MENU, "Confirm Selection","validateSelectedSeries();"],
         DISCARD_SELECTION_TOOL =  [CCR_ATTACHMENTS_MENU,"Discard Selection","discardSeries();"],
                                   [CCR_ATTACHMENTS_MENU,"Download DICOM","downloadDICOM();"]
                                   */
    ], function(t){tools.push(t);});
    
    
    /*
    if(!window.parent.hasPatientHeader) {
      window.parent.addPatientHeader(getCcr());
    }
    */

    window.parent.setTools(tools);
    window.parent.enableTool(EDIT_AS_NEW_TOOL, ccr.storageMode == 'FIXED');
    
    addMergeMenu();
    addPDFHide();
}

connect(events, 'newSeriesSelected', function(series) {
    if(series.presets.length > 0) {
        var wlMenu = parent.menu.getItem(3);
        if(!wlMenu) 
          wlMenu = parent.menu.addItem( { text: "Window / Level Presets",  onclick: function() { return false;  } } );
    
        var oldSubmenu = wlMenu.cfg.getProperty("submenu");
        while(oldSubmenu && oldSubmenu.getItems().length) {
              oldSubmenu.removeItem(0);
        }
    
        var submenu = [];
        forEach(series.presets, function(preset) {
            submenu.push( { text: preset.name, onclick: { fn: function() { setWindowLevel(preset.window,preset.level);  displayCurrentImage(); return false; }} } );
        });
        wlMenu.cfg.setProperty("submenu", { id: 'wlpresets'+(new Date()).getTime(), itemdata: submenu });
        parent.menu.render();
      }
      else {
        parent.menu.removeItem(3); // Remove W/L menu
      }
});



/**
 * Hide PDF in main section of viewer if the menu is shown because
 * on some platforms it will overlay the menus
 */
function addPDFHide() {
    
    // PDF overlay doesn't seem to be an issue on windows
    if(!framed || (YAHOO.env.ua.os == 'windows')) 
        return;
    
    parent.menu.subscribe('show', function() {
        // If a PDF is showing, temporarily hide it
        if(thumbnails[currentThumb].series.mimeType == 'application/pdf') {
            addElementClass('mainImage','hidden');
            parent.menu.subscribe("hide", function() {
                removeElementClass('mainImage','hidden');
            });
        } 
    });
}

var roster;
function addMergeMenu() {
    log("adding merge menu");
    if(framed) {
        var pm =  new parent.YAHOO.widget.Menu('patientsmenu', { 
            id: 'patientsmenu',
            itemdata: [
                       {  text: 'Loading ...' }
                      ]
        });
        parent.menu.getItem(0).cfg.getProperty("submenu").addItem({
                    text:"Merge to Other Patient",
                    submenu: pm
        });
        
        window.pm = pm;
        
        // Lazy load the patients for the menu only when they
        // are actually needed.
        pm.subscribe("show", function() {
            if(roster)
                return; 
            execJSONRequest('QueryRosterMembers.action', null, function(result) {
                if(result.status != 'ok') {
                    alert('A problem occurred while querying for patients associated with your account\n\n'+result.error);
                    return;
                }
                roster = result.result.result;
                roster = filter(function(p) { return p.accid != ccr.patient.medCommonsId; }, roster);
                var loadingMenu = pm.getItem(0,0);
                var rosterMenu = map(function(p) { return { text: p.name, onclick: { fn: function() { 
                    window.parent.location.href = 'AccountImport.action?'+queryString({
                        toAccount: p.accid,
                        sourceUrl: accountsBaseURL+ccr.patient.medCommonsId,
                        sourceAuth: auth
                    });
                }}};}, roster);
                
                var groups = [
                              rosterMenu,
                              [{ text: 'Enter HealthURL', onclick: { fn: mergeToHealthURL }}]
                             ];
                
                
                pm.removeItem(loadingMenu);
                pm.addItems(groups);
                
                if(roster.length) {
                    pm.setItemGroupTitle("Patients", 0);
                    pm.setItemGroupTitle("Other", 1);
                }
                pm.render();
            });
            
        });
        parent.menu.render();
    }
}

