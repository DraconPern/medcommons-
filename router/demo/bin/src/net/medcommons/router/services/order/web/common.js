/**
 * Copyright 2006 - 2007 MedCommons Inc.   All Rights Reserved.
 */

var HEALTHBOOK_NOT_INSTALLED = 1;
var HEALTHBOOK_INSTALLED = 2;
var HEALTHBOOK_CAN_NOT_BE_DETECTED = 3;

/**
 * Set title of window according to name of patient
 */
function setPatientTitle(given, family) {
      window.parent.document.title = 'CCR: ' + given + ' ' + family;
}

/********************************************************************************************
 * 
 *  Patient Selection support
 */
function findCurrentPatient(align) {
  log("got autocomplete css ...");

  // Make it so that this function can be called from either
  // parent or content window
  var w = window.parent==window ? window : window.parent;

  var pccrs = w.patientPrevCCRsAC;
  if(align=="left") {
    pccrs.autocompleteBehavior.offsetX=30;
  }
  else {
    pccrs = w.patientLaterCCRsAC;
    pccrs.autocompleteBehavior.offsetX=document.body.scrollWidth-270;
  }

  pccrs.autocomplete = w.autocomplete;
  log("autocompleting");
  pccrs.autocomplete(); 
}

function prevPatientSelect(i) {
  log("Prev patient " + i);
  var w = window.parent==window ? window : window.parent;
  patientSelect(i,false,w.patientPrevCCRs);
}

function laterPatientSelect(i) {
  log("Later patient " + i);
  var w = window.parent==window ? window : window.parent;
  patientSelect(i,false,w.patientLaterCCRs);
}

function patientSelect(i,newPatient,patientList) {
  var w = window.parent==window ? window : window.parent;
  currentCompletion = -1;
  autocompleteField = null;

  var acdiv = document.getElementById('acdiv');
  acdiv.style.display='none';
  show('ccrPurpose');
  auto_complete_reset_behavior();
  if(i>=0) {
    var p = patientList ? w.ccrCache[patientList[i]] : registryPatients[i];
    //var url = 'track.do?trackingNumber='+p.ConfirmationCode+'&pin='+p.RegistrySecret;
    var url = 'access?tryload&g='+p.guid;
    if(newPatient) {
      url += '&clean=true';
    }
    w.contents.document.location.href=url;
  }
  return false;
}

function tabDateTitle(p) {
  if(p.createDateTime) {
      return ccr.createDateTime.replace(/ .*$/,'');
  }
  else
    return toAmericanDate(p.date).replace(/\//g," / ");
}

/**
 * Sets the current tab's text back to the date of it's associated CCR
 */
function clearTabCurrentCCR() {
  log("clearing existing current ccr tab title");
  if(currentCcrTab) {
    if(currentCcrTab.ccr) {
      setTabText(currentCcrTab,tabDateTitle(currentCcrTab.ccr));
    }
  }
}

/**
 * Resets tab's text to its default state after it may have been
 * modified while editing (eg. adding the '*' to show modified)
 */
function resetTabText(statusForm) {
  if(window.tabText)
    window.parent.setTabText(null, tabText);
  setUnmodified();
}

/**
 * Removes annotation indicating the contents of this tab have been modified.
 */
function setUnmodified() {
  tabText = window.parent.getTabText().replace(/[ \*]*$/,"");
  window.parent.setTabText(null,tabText);
  window.parent.currentTab.modified = false;
}

/**
 * Adds annotation indicating that hte contents of this tab have been modified
 */
function setModified() {
  resetTabText();
  signal(window,'ccrModified');
  window.parent.setTabText(window.parent.currentTab, tabText+' *');        
  window.parent.currentTab.modified = true;
}



/**
 * Check if there is an existing New CCR and if so, warn the user and remove it
 */
function checkNewCCR() {
  // Check if there is already a CCR of this type
  var newCCR = [];
  if(window.patientCCRs) {
    newCCR = filter(function(c) { return c.documentType == 'NEWCCR'; },patientCCRs);
  }
  var tabWithNewCCR = filter(function(t) { return t.documentType == 'NEWCCR'; },window.parent.getTabs());
  if((newCCR.length != 0) || (tabWithNewCCR.length != 0)) {
    if(!confirm('There is an existing New CCR.  If you continue, it will be replaced.\r\n\r\nDo you want to replace it?')) {
      return false;
    }

    // Remove the old New CCR tab
    window.parent.removeTabs( function(t) { return  t && (scrapeText(t).match('New CCR') || (t.ccr && t.ccr.documentType == 'NEWCCR')); } );
  }
  return true;
}

var currentCcrTab = null;

/********************************************************************************************
 * 
 *  Menu Support
 */
function showToolPalette() {
  show('menuholder');
}

function hideToolPalette() {
  hide('menuholder');
}

/**
 * Exceutes given tool specified by index
 */
function execTool(index) {
  var codeIndex = 1;

  if(typeof currentTools[index][0] == 'number')
    codeIndex = 2;

  // Check if there is a parent window script to execute
  if(currentTools[index].length>(codeIndex+1)) {
    window.eval(currentTools[index][codeIndex+1]);
  }
  window.contents.setTimeout(currentTools[index][codeIndex],0);    
  return false;
}

/**
 * Disables or enables tools filtered by a predicate on the tool name.
 */
function disableTools(p,value) {
    value = value ? true : false;
    forEach(
      filter(function(a) { return p(toolText(a)); },currentTools),
      function(t) { toolMenuItem(t).cfg.setProperty("disabled", !value); }
    );
}

/**
 * Returns text of the given tool.
 */
function toolText(t) {
  var txtObj = (typeof t[0] == 'number') ? t[1] : t[0];
  if(typeof txtObj == 'object') { // explicit yui menu data
    return txtObj.text;
  }
  return txtObj; // plain text item
}

/**
 * Return the index of the top level menu in which this tool resides.
 */
function toolMenuIndex(t) {
  return (typeof t[0] == 'number') ? t[0] : 0;
}

/**
 * Enable / Disable tool with given text
 */
function enableTool(tool,enabled) {
  var txt = isArrayLike(tool) ? toolText(tool) : tool;
  // log("==> Enabling tool " + txt + " : " + enabled);
  var found = false;
  if(enabled == undefined)
    enabled = true;

  forEach(currentTools,function(t) {
    //log("Checking tool " + toolText(t));
    if(toolText(t) == txt) {
      //log('Found tool ' + txt + ' Setting enabled to ' + enabled);
      found = true;
      toolMenuItem(t).cfg.setProperty("disabled", !enabled);
    }
  });
}

/**
 * Find the YUI MenuItem associated with the given tool
 */
function toolMenuItem(t) {
  var submenu = menu.getItem(toolMenuIndex(t)).cfg.getProperty("submenu");
  var txt = isArrayLike(t) ? toolText(t) : t;
  var mi = null;
  forEach(submenu.getItems(), function(m) {
      if(m.cfg.getProperty("text") == txt) {
        mi = m;
      }
  });
  return mi;
}

function toolMenuFromText(txt) {
  for(var i=0; i<menu.getItems().length; ++i) {
     var m = menu.getItem(i).cfg.getProperty("submenu");
     for(var j = 0;  j<m.getItems().length; ++j) {
        var sm = m.getItem(j);
        if(sm.cfg.getProperty("text") == txt) {
          return sm;
        }    
     }
  }
  return null;
}

/**
 * Return true if the given tool (passed by text or actual tool object) is enabled
 */
function isToolEnabled(tool) {
  var txt = isArrayLike(tool) ? toolText(tool) : tool;
  var sm = toolMenuFromText(txt);
  if(sm)
    return !sm.cfg.getProperty("disabled");
  else
    return null;
}

/**
 * Current set of tools.  Avoid manipulating this directly, use API
 */
var currentTools;

function addSubMenu() {
  var submenu = menu.getItem(0).cfg.getProperty('submenu');
  submenu.addItem( { text: 'hello2',  submenu: 
    {
       id: 'import',
       itemdata: [ "foo", "bar", "tree"]
    }
  });
  menu.render();
}

/**
 * sets the given tools (array of tool/function mappings) in the tool window 
 */
function setTools(tools) {
  log("setting tools");
  currentTools = tools;

  // Allow for 4 submenus
  var submenus = [ [], [], [], [] ];

  for(i=0;i<tools.length;i++) {
    var t = tools[i];      
    var def = (typeof t[0] == 'integer') ? t[1] : t[0]; 
    if(typeof def == 'object') {
      submenus[toolMenuIndex(t)].push(def);
    }
    else
      submenus[toolMenuIndex(t)].push( { text: toolText(t), onclick: { fn: partial(execTool, i) } } );
  } 

  var smIndex=0;
  forEach(submenus, function(s) {
    var topMenu = menu.getItem(smIndex);
    if(!topMenu)
      return;
    var oldSubmenu = topMenu.cfg.getProperty("submenu");
    if(oldSubmenu) {
      oldSubmenu.clearContent();
      forEach(s, function(mi) {oldSubmenu.addItem(mi);});
    }
    else 
      topMenu.cfg.setProperty('submenu',{ id: 'menu'+smIndex, itemdata: s});

    ++smIndex;
  });

  menu.render();

  showToolPalette();
  log("done tools");
}

/**
 * Constants indicating top level menu order
 */
var CCR_FILE_MENU = 0;
var CCR_ATTACHMENTS_MENU = 1;
var CCR_SECTIONS_MENU = 2;
var CCR_WINDOW_LEVEL_MENU = 3;

/**
 * Support for adding CCR sections
 */
var CCR_SECTIONS = [ 'Insurance','Medications','Advance Directives','Functional Status','Support Providers',
                     'Vital Signs','Immunizations','Procedures','Problems','Encounters','Family History',
                     'Plan Of Care Recommendations','Plan Of Care Orders','Results','Results Report',
                     'Social History','Alerts','Health Care Providers', 'People', 'Organizations'  ];

/**
 * For most sections the id of the section in the report can be inferred from the name itself.
 * For some however this does not work
 *
 *  - some map to multiple names
 *  - some map to names that are different to their section names in the CCR
 *
 * These ones are specified here.
 */
var CCR_SECTION_ID_MAPPINGS = {
  'Insurance' : { id: 'payers', section: 'Payers' },
  'Results Report' : { id: 'resultsreport', section: 'Results' },
  'Plan Of Care Recommendations' : { id: 'planofcare', section: 'PlanOfCare', filter: 'Type/Text="Treatment Recommendation"' },
  'Plan Of Care Orders' : { id: 'planofcareorders', section: 'PlanOfCare', filter: 'Type/Text="Order"' },
  'People' : { id: 'actors', section: 'Actors'},
  'Organizations' : { id: 'organizations', section: 'Actors' }
};

function sectionIdFromName(n) {
  if(CCR_SECTION_ID_MAPPINGS[n])
    return CCR_SECTION_ID_MAPPINGS[n].id;
 
  // default
  return removeSpaces(n.toLowerCase());
};

var CCR_SECTION_REVERSE_MAPPINGS = null;

function sectionFromId(i) {
  if(!CCR_SECTION_REVERSE_MAPPINGS) {
    CCR_SECTION_REVERSE_MAPPINGS = {};
    forEach(CCR_SECTIONS, function(s) {
      CCR_SECTION_REVERSE_MAPPINGS[ sectionIdFromName(s) ] = (CCR_SECTION_ID_MAPPINGS[s] ? CCR_SECTION_ID_MAPPINGS[s].section : removeSpaces(s));
    });
  }
  
  return CCR_SECTION_REVERSE_MAPPINGS[i];
}

/**
 * Check if menu for Sections is already created.  If not, create it.
 */
function addSectionMenus(tools) {
  forEach(CCR_SECTIONS, function(s) {
      // log('Adding section menu ' + s);
      tools.push([ CCR_SECTIONS_MENU, s, 'highlightSection("'+s+'")']);
  }); 
}

/********************************************************************************************
 * 
 *  Menu buttons.
 *
 *  Buttons are displayed in the menu bar on the right hand side
 *
 * Each button can have properties:
 *   
 *   text   - text to display (required)
 *   action - javascript action to invoke (required if url not provided)
 *   url    - link to navigate to (required if action not provided)
 *   img    - icon / image (optional, not implemented yet)
 *   tip    - tip text to display (optional)
 *   id     - id given to url (optional)
 */
var currentButtons = [];

/**
 * Set the given array of buttons as the currently displayed buttons.
 */
function setButtons(buttons) {
  currentButtons = clone(buttons);
  var s = SPAN();
  for(var i = 0; i<currentButtons.length; ++i) { 
    if(i != 0) 
      appendChildNodes(s,' | ');

    var b = currentButtons[i];
    var link = {  href:(b.url?b.url:'#'), 
                  'id':(b.id?b.id:''), 
                  title: (b.tip ? b.tip : ''),
                  onclick:(b.action?'return execButton('+i+')':''),
                  style: 'vertical-align: middle;'
                };
    if(b.img)
      appendChildNodes(s, A(link,IMG({src:b.img,border:0, style: 'vertical-align: middle; margin:0px 3px;'}),b.text));
    else
      appendChildNodes(s, A(link,b.text));
  }
  replaceChildNodes($('topbuttons'), s);
}

/**
 * Executes button for given index in currentButtons array
 */
function execButton(index) {
  // Check if there is a parent window script to execute
  var target = currentButtons[index].actionTarget ? currentButtons[index].actionTarget : 'contents';
  if (target == '_top'){
 	window.eval(currentButtons[index].action);
  }
  else{
  	window[target].setTimeout(currentButtons[index].action,0);  
  }  
  return false;
}

/********************************************************************************************
 * 
 *  Patient Header
 */
function addPatientHeader(ccr, container) {
  if(!container) {
    container = $('patientHeader');
  }

  var h = TABLE({id:'patientHeaderTable'},
            THEAD(null,
              TR(null,
                TH(null),TH('Date / Time'), TH('First Name'), TH('Family Name'), TH('Age / Sex'), TH('DOB'), TH({id:'idLabel'},'ID')
              )
            ),
            TBODY(null,
              TR(null,
                TH('CCR'),
                TH({id:'ccrDateTime'},''),
                TH({id:'givenName'},''),
                TH({id:'familyName'},''), 
                TH({id:'ageSex'},''),
                TH({id:'dateOfBirth'},''),
                TH({id:'ids'},'')
              )
            )
          );

  replaceChildNodes(container, h);

  window.hasPatientHeader = true;

  initContentsHeight();

  if(window.events == null) 
    window.events = {};

  updatePatientHeader('',ccr);

  visibility('patientHeader', true);
  show('patientHeader');

  connect(window.events, 'openCCRUpdated', updatePatientHeader);
}

function hidePatientHeader() {
  parent.hide('patientHeader');
  hide('patientHeader');
}

function updatePatientHeader(guid, ccr) { 
  $('ccrDateTime').innerHTML = ccr.createDateTime;
  for(i in ccr.patient) { 
    if((typeof ccr.patient[i] == 'string') && $(i))
      $(i).innerHTML = ccr.patient[i];
  }

  if(ccr.patient.ids.length == 0) {
    $('ids').innerHTML = '';
    $('idLabel').innerHTML = 'ID';
  }
  else {
    var pid = null;
    // Try to find non-medcommons id if we can
    for(var n = 0; n<ccr.patient.ids.length; ++n) {
      if(ccr.patient.ids[n].type != 'MedCommons Account Id') {
        pid = ccr.patient.ids[n];
        break;
      }
    }
    if(pid) {
      $('ids').innerHTML = pid.value;
      $('idLabel').innerHTML = pid.type + " (" + (n+1) + " of " + ccr.patient.ids.length + ")";
    }
    else {
      $('ids').innerHTML = ccr.patient.ids[0].value;
      $('idLabel').innerHTML = ccr.patient.ids[0].value;
    }
  }
  $('patientHeader').style.visibility = 'visible';

  updateCCRLink(ccr);
}

/**
 * Whether to write out urls to CCRs using 'clean' format
 * which relies on apache mod-rewrite being present on local central
 */
var useCleanURLs = true;

function updateCCRLink(ccr) {
  var g = ccr.guid;

  if(!$('ccrLink'))
    return;

  visibility('ccrLink',g!=null);
  if(ccr.logicalType=='CURRENTCCR') {
    $('ccrLink').innerHTML = '<img src="images/hurl.png" style="position: relative; top: 1px;"/> Health URL';
    if(useCleanURLs) {
      $('ccrLink').href=acctServer + '/../../'+removeSpaces(ccr.patient.medCommonsId);
    }
    else {
      $('ccrLink').href=acctServer + '/../cccrredir.php?accid='+removeSpaces(ccr.patient.medCommonsId);
    }
  }
  else {
    $('ccrLink').href=acctServer + '/../ccrredir.php?guid='+g;
    $('ccrLink').innerHTML = 'CCR Link';
  }
  $('ccrLink').title = 'Click to Copy a permanent link for this CCR or PHR to your clipboard';
}

function ccrLink() {
  if (window.clipboardData) {
    window.clipboardData.setData("Text",$('ccrLink').href);
    alert('A link to this CCR has been copied to your Clipboard.');
  }
  else {
    alert('Your browser does not allow web pages to copy data to the Clipboard.\r\n\r\nTo copy this link, right click on it and choose "Copy Link Location" or similar option from your menu.');
  }
  return false;
}

function ageSex(age,gender) {
  var sex = "";  
  if(gender == "Male") {
    sex = "M";
  }
  else
  if(gender == "Female") {
    sex="F";
  }
  return age +  sex;
}

/********************************************************************************************
 * 
 *  CCR Operations
 */
var CCR = {

  getActor: function(actorID) {
    for(var i =0; i<this.actors.length;++i) {
      if(this.actors[i].actorObjectID == actorID) {
        return this.actors[i];
      }
    }
    return null;
  },
  getFromActor: function () {
    var fromActor = null;
    var ccr = this;
    var froms = map(function(al) { return ccr.getActor(al.actorID); },this.get('from.actorLink',[]));
    var emailActors = filter(function(a) { return a.email != ''; }, froms);
    if(emailActors.length > 1)
        return emailActors[0];
    else
      return froms.length > 0 ? froms[0] : null;
  }
}

/**
 * Document types
 */
var documentTypes = new Array();
documentTypes['CURRENTCCR'] = 'Current CCR';
documentTypes['REPLYCCR'] = 'Reply CCR';
documentTypes['EMERGENCYCCR'] = 'Emergency CCR';
documentTypes['NEWCCR'] = 'New CCR';

/*****************************************************************/
/* Support for Patient CCRs
/*****************************************************************/
window.parent.patientPrevCCRsAC = new Object();
window.parent.patientLaterCCRsAC = new Object();

/**
 * Cached array of CCRs for this patient.
 */
var patientCCRs = new Array();

function fetchPatientCCRs() {

  patientPrevCCRs = new Array();
  patientLaterCCRs = new Array();

  log("checking patient ccrs");
  execJSONRequest('QueryPatientCCRs.action',{ccrIndex:parent.currentTab.ccrIndex}, function(result) {
    try {
      patientCCRs = new Array();
      if(!result || (result.status!="ok")) {
        alert("An error occurred while retrieving other CCRs for this patient:\r\n\r\n"+result.message);
        return;
      }

      log("====> Adding CCRs " + result.profiles);
      var logicalDocuments = new Array();
      forEach(result.profiles, function(entry) {
        entry.date =  entry.date ? new Date(entry.date) : new Date();
        log("processing profile " + entry.name + " date = " + entry.date + " guid = " + entry.guid);
        entry.label = entry.name ? entry.name : formatTabDate(entry.date);
        for(var t in accountDocuments) {
          var d = accountDocuments[t];
          log("type: " + d.type + " entry name: " + entry.name);
          if(((d.guid == entry.guid) || (entry.name == d.type)) && !logicalDocuments[d.type]) {
            entry.documentType = d.type;
            log("Found document of type " + entry.documentType + " with guid " + entry.guid);
            logicalDocuments[entry.documentType] = entry;
          }
        }

        if(!entry.documentType) {
          log("push " + entry);
          patientCCRs.push(entry);
        }
      });

      log("# of entries = " + patientCCRs.length);

      forEach( [ 'NEWCCR','EMERGENCYCCR','CURRENTCCR' ], // note: reverse order to how we want them to appear
        function(docType) {
          if(logicalDocuments[docType]) {
            log("Splicing doc of type " + docType + " at position 0");
            patientCCRs.splice(0,0,logicalDocuments[docType]);
          }
      });

      // Only add tabs if we are the "main" CCR.
      if(cleanPatient) {
        addPatientTabs();
      }
      else {
        log("Unclean patient");
      }

      if(patientCCRs.length > 0) {
        log("initializing tab auto completes");
        if(window.parent.patientPrevCCRs) {
          var behavior = setdefault(clone(overflowCCRsAutocompleteBehavior), window.parent.autocompleteBehavior);
          behavior.fill_value = window.parent.prevPatientSelect;
          behavior.offsetY = 40;
          window.parent.init_autocomplete(window.parent.patientPrevCCRsAC,window.parent.patientPrevCCRs,behavior);
        }
        if(window.parent.patientLaterCCRs) {
          var behavior = setdefault(clone(overflowCCRsAutocompleteBehavior), window.parent.autocompleteBehavior);
          behavior.fill_value = window.parent.laterPatientSelect;
          behavior.offsetY = 40;
          window.parent.init_autocomplete(window.parent.patientLaterCCRsAC,window.parent.patientLaterCCRs,behavior);
        }
        log("done");
      }
    }
    catch(e) {
      dump(e);
    }
  });
}

var overflowCCRsAutocompleteBehavior = {
  show_all: true,
  fill_value: patientSelect,
  offsetX: -20,
  offsetY: 20,
  message: "<img src='images/closebutton.gif' style='margin-top:4px; cursor: pointer;' onclick='patientSelect(-1)'/>&nbsp;&nbsp;Click on an entry to select ",
  auto_show: false
};

/**
 * Removes any old tabs created by previous CCRs
 */
function cleanTabs() {
  window.parent.removeTabs( function(t) { return  t != window.parent.currentTab; } );
  window.parent.registryTabs = new Array();
  window.parent.setTabText(null,"Received CCR");
  window.parent.patientTabsAdded = false;
}

function formatTabDate(d) {
  if(d == null)
    d = new Date();
  return (typeof d == 'string') ? formatTabDate(isoDate(d)) : toAmericanDate(d).replace(/\//g," / ");
}

/**
 * Accepts various forms of CCR object and determines appropriate
 * tab text for them.  If a logical CCR then returns the logical
 * type as a name, otherise returns the date
 */
function patientTabText(ccr) {
  var entry ='CCR';
  if(ccr.documentType) {
    entry = documentTypes[ccr.documentType];
  }
  else 
  if(ccr.date) {
    if(!ccr.date.getDate)
      ccr.date = new Date(parseInt(ccr.date)*1000);
    entry = formatLocalDateTime(ccr.date);
  }
  else 
  if(ccr.createDateTime) {
    entry = ccr.createDateTime.replace(/ .*$/,'');
  }
  return entry;
}

function initTabInfo(ccrIndex,logicalType, storageMode, mode) {
  var tab = window.parent.currentTab;
  if(tab.ccr == null) {
    tab.ccr = {};
  }
  tab.ccrIndex = ccrIndex;
  tab.ccr.documentType = logicalType;
  tab.ccr.storageMode = storageMode;
  tab.mode = mode;
}

/**
 * Builds tabs linked to each CCR in the patient's history, 
 * as many as will fit across the top of the page.
 */
function addPatientTabs() {  
  //try {
    if(window.parent.patientTabsAdded)
      return;
    
    // First get all the tabs for this patient
    var ccrs = patientCCRs; // filter(function(p) { return (p.PatientIdentifier == loadedRegistryPatient.PatientIdentifier); }, registryPatients);

    if(ccrs.length == 0)
      return;

    // Find the currently displayed CCR
    var loadedCCRIndex = 0; // findIdentical(ccrs,loadedRegistryPatient);
    var i = 0;
    var loadedCCRFound = false;
    log("Searching for loaded guid " + loadedGuid + " in " + ccrs.length + " CCRs");
    for(i=0; i<ccrs.length; ++i) {
      log("ccr " + i + " type = " + ccrs[i].documentType + ' name = ' + ccrs[i].name);
      if((ccrs[i].guid == loadedGuid) || ccrs[i].documentType && (ccrs[i].documentType == parent.currentTab.ccr.documentType)) {
        ccrs[i].isLoadedCCR = true;
        loadedCCRFound = true;
        loadedCCRIndex = i;
        break;
      }
    }

    if(!loadedCCRFound) {
      log("WARN: loaded CCR not found in ccr log");
      return;
    } 
    else
      log("Found loaded CCR at loadedCCRIndex " + loadedCCRIndex);

    // Clean out the cached patients
    window.parent.patientPrevCCRs = new Array();
    window.parent.patientLaterCCRs = new Array();
    window.parent.ccrCache = new Array();
    var patientPrevCCRs = window.parent.patientPrevCCRs;
    var patientLaterCCRs = window.parent.patientLaterCCRs;

    if(!window.parent.registryTabs) {
      window.parent.registryTabs = new Array();
    }

    var numTabs = Math.floor(document.body.scrollWidth/(elementDimensions(window.parent.currentTab).w+13)) - 1; // subtract one to leave room for new tab
    var start = loadedCCRIndex - Math.floor(numTabs/2);
    var end = loadedCCRIndex + Math.floor(numTabs/2)
    while(start < 0) {
      start++;
      end++;
    }

    if(end >= ccrs.length)
      end = ccrs.length - 1;

    log("Start: " + start + " End: " + end);
    if(start > 0) {
      window.parent.addTab(
         (start+1) + ' newer', 
        'javascript:window.parent.findCurrentPatient("left")',
        'Click to view details',
        window.parent.currentTab).secondaryTab=true;
      // Make room for "more" tab
      start++;
    }
    else
    if(end<ccrs.length-1) { // Make room for "more" tab
      log("making end room");
      end--;
    }

    for(var i=0; i<start; ++i) {
      var entry = patientTabText(ccrs[i]);
      patientPrevCCRs.push(entry);
      window.parent.ccrCache[entry]=ccrs[i];
    }

    for(var i=start; i<=end; ++i) {
      var p = ccrs[i];
      log("adding tab " + i + " in " + ccrs.length + " ccrs p=" + p.guid + " loaded?" + p.isLoadedCCR);
      if(p.isLoadedCCR) {
        log("tab " + i + " is loaded ccr  type = " + p.name);
        window.parent.currentTab.ccr = p;
        if(p.name) {
          log("loaded ccr is logical document");
          window.parent.setTabText(null, documentTypes[p.name] ? documentTypes[p.name] : p.name);
        }
        continue;
      }

      var url = 'UpdateCCR.action?forward=access&tryload'+(p.guid?'&g='+p.guid:'&l='+p.name);
      var tab = null;
      var title = tabDateTitle(p);
      if(p.documentType) {
          url += '&l='+p.documentType;
          title = documentTypes[p.documentType];
      }

      var tip = formatLocalDateTime(p.date);
      if(p.Guid == currentCcrGuid) { // special labelling for current ccr 
        tab = window.parent.addTab(title, url,tip,parent.getTabs()[0]);
        parent.currentCcrTab = tab;
      }
      else
      if(i<loadedCCRIndex) {
        tab = window.parent.addTab(title, url,tip,window.parent.currentTab);
      }
      else {
        tab = window.parent.addTab(title, url,tip);	
      }
      tab.ccr = p;
      p.tab = tab;
      tab.secondaryTab=true;	
    }

    for(var i=end; i<ccrs.length; ++i) {
      var entry = patientTabText(ccrs[i]);
      window.parent.ccrCache[entry]=ccrs[i];
      patientLaterCCRs.push(entry);
      // Maximum of 20 - prevent scrolling off the page
      if(i-end > 30) {
        // hack here - because we don't want the entry to be clickable,
        // close of the <a> tag that is going to be added in the text string
        patientLaterCCRs.push( "</a>( " + (ccrs.length - i) + " more ... )<a>" );
        break;
      }
    }

    if(end<ccrs.length-1) {
      var t = window.parent.addTab( (ccrs.length-end) + ' older', '', 'Click to view details');
      t.secondaryTab=true;
      t.onclick=function() { findCurrentPatient('right'); window.parent.highlightTab(this,false); return false; };
    }
    window.parent.patientTabsAdded = true;
  //}
  //catch(e) {
  //  dump(e);
  //}
}
 
/******************************************************
 * Handling for Quick Reply feature - displays
 * a dialog allowing user to enter simple comment and 
 * click button to reply without leaving current screen.
 */
var quickReplyLoaded = false;
function showQuickReply() {

  if(ccr.getFromActor() == null) {
    alert('You can not reply to this CCR because it does not contain a From actor.\r\n\r\n'
        + 'Please use the "Edit" option to create your reply instead.');
    return;
  }

  if(quickReplyLoaded) {
    quickReply();
  }
  else {
    doSimpleXMLHttpRequest('QuickReply.action?create&ccrIndex='+parent.currentTab.ccrIndex).addCallbacks(function(r) {
        quickReplyLoaded = true;
        $('notificationsTableCache').innerHTML = r.responseText;
        quickReply();
    }, genericErrorHandler); 
  }
}

function quickReply() {
  var hd = null;
  var bt = null;
  var cbt = null;
  var ta = null;
  visibility(parent.$('topcenterbuttons'),false);
  var dlg = DIV({'class':'dialog',id:'quickReplyDlg'}, 
              hd = DIV({'class':'hd'},createDOM('H4',null,"Quick Reply")),
              DIV({'class':'dlgContent'},  
                  DIV({id: 'notifications'}),
                  ta = TEXTAREA({id:'replyComment',name:'replyComment', rows:10},'Type a Reply Comment Here'),
                  bt=BUTTON(null,'Send'),
                  cbt=BUTTON({style:'margin-right:7px;'},'Cancel'),
                  DIV({id:'replyStatus'})
                  )
      );
  roundElement(hd,{corners:'top', color: '#898770'});
  appendChildNodes(document.body,dlg);
  $('notifications').innerHTML = $('notificationsTableCache').innerHTML;
  ta.style.width = elementDimensions(findChildElements(dlg,['.notificationsTable'])[0]).w + 'px';
  ta.style.marginLeft = (parseInt(getStyle(findChildElements($('quickReplyDlg'),['.ccrTable tr th'])[0],'width'),10)+7)+'px';
  $('replyComment').focus();
  $('replyComment').select();

  connect(cbt,'onclick',function() { removeElement(dlg); });
  
  forEach($('notifications').getElementsByTagName('input'), function(i) { i.disabled = true; });
  connect(bt,'onclick', function() {
      $('replyStatus').innerHTML='Sending ...';
      var i = setInterval(function(){if($('replyStatus')) { $('replyStatus').innerHTML=$('replyStatus').innerHTML+'.'} else { clearInterval(i); } },1000);
      $('replyComment').disabled = true;
      bt.disabled = true;
      execJSONRequest('QuickReply.action',queryString({ccrIndex:$('ccrTableCCRIndex').value,comment:$('replyComment').value}), function(result) {
        clearInterval(i);
        if(result.status == 'ok') {
          var url = 'viewEditCCR.do';
          var tab = window.parent.addTab(formatTabDate(),url,'',window.parent.nextTab());
          tab.ccrIndex = result.ccrIndex;
          tab.ccr = { Guid: result.guid, documentType: 'REPLYCCR' };
          $('replyStatus').innerHTML='<img src="images/tick.png" style="position: relative; top: 4px;"/> Succeeded';
          findChildElements(dlg,['#trackingNumber'])[0].innerHTML=prettyTrack(result.trackingNumber);
          bt.innerHTML='Close';
          disconnectAll(bt);
          connect(bt,'onclick',function() { removeElement(dlg); });
        }
        else {
          alert('A problem occurred in sending your Reply:\r\n\r\n'+result.error);
        }
        bt.disabled = false;
      });
  });
}

/*****************************************************************/
/* Support for Change Notifications                              */
/*****************************************************************/
function showChangeNotifications() {
  var tbl = $('changeNotificationTable');
  var body = tbl.getElementsByTagName('TBODY')[0];
  var child;
  while ((child = body.firstChild)) {
      body.removeChild(child);
  }

  var summary = new Object();
  var bodyExpr = /^\/ContinuityOfCareRecord\/Body\/([^\/]*).*$/;
  forEach(changeNotifications, function(c){
    log(c.location);
    var label = "Miscellaneous Changes";
    if(c.location.match(/^\/ContinuityOfCareRecord\/Actor/)) {
      label = "Actors";
    }
    else
    if(c.location.match(/^\/ContinuityOfCareRecord\/References/)) {
      label = "References";
    }
    else
    if(bodyExpr.test(c.location)) {
      label = bodyExpr.exec(c.location)[1];      
    }
    else {
      label = "Miscellaneous Changes";
    }
    log(label);
    if(summary[label])
      summary[label]++;
    else
      summary[label]=1;
  });
  for(key in summary) {
    appendChildNodes(body,TR(null, TD({'class':'cnotifyLabel'},key), TD(null,summary[key])));
    log('appended ' + key);
  }
  show('changeNotificationDiv');
  $$('#changeNotificationDiv input')[0].focus();
}

function clearChangeNotifications() {
  execJSONRequest('ClearChangeNotifications.action',{ccrIndex:window.parent.currentTab.ccrIndex}, function(result) {
    if(result.status!="ok") {
      alert("A problem occurred while clearing the notifications for changes in your CCR:\r\n\r\n"+result.message);
    }
    hide('changeNotificationDiv');
    changeNotifications=new Array();
  });
}

/*****************************************************************/


function generateCCRExternaEditURL(){
  var patientId = storageId;
  var url = "/router/getPHREditSession?storageId=" + patientId + "&useSchema=11" + "&auth=" + auth;
  if(ccr.storageMode == 'LOGICAL') {
    url += "&reference="+ccr.logicalType;
  }
  else {
    url += "&reference="+encodeURIComponent(ccr.guid);
  }
  return(url);
}
function editCCRExternally(){
	var editUrl = generateCCRExternaEditURL();
    document.location.href=editUrl;
}

/**
 * Update the top frame URL to reflect the CCR index and current mode
 */
function updateFragment() {
  if(parent.location.hash) {
    var parts = /#([0-9])*([ev])/.exec(parent.location.hash);
    if(parts.length == 3) {
      var m = parent.currentTab.mode == 'edit' ? 'e' : 'v';
      var newHash = '#'+parent.currentTab.ccrIndex + m;
      if(newHash != parent.location.hash) {
        if(parent.YAHOO.env.ua.ie == 0) {
          log("updating window hash from " + parent.location.hash + " to " + newHash);
          parent.location.hash = newHash;
          parent.originalHash = parent.location.hash;
        }
      }
    }
    else
      log("no query fragment");
  }
}

function dialog(id, header, contents, width, buttons, init) {
  if(!buttons)
    buttons = [ { text: 'OK', handler: function() { this.destroy(); } } ];

  if(!width)
    width = 300;

  yuiLoader().insert(function() {
    var viewWidth = viewportSize().w;
    var w = Math.round(Math.min(viewWidth, 1.1 * width));
    var dlg = new YAHOO.widget.SimpleDialog(id, { 
        width: w+'px',
        x: (viewWidth - w) / 2,
        y: 100,
        modal:true,
        visible:false,
        draggable:true,
        buttons: buttons,
        zIndex: 30
    });
    dlg.setHeader(header);
    dlg.setBody(contents);
    dlg.render(document.body);
    document.getElementById(id).style.fontSize = '13px';
    dlg.show();
    // document.getElementById(id).parentNode.style.top = '100px';
    window[id]=dlg;
    if(init) 
      init(dlg);
  });
}

var PAYMENT_DESCRIPTIONS = {
    INBOUND_FAX: {title:'Inbound Fax',billingType:'faxin', units:'Pages'},
    NEW_ACCOUNT: {title:'New Account',billingType:'acc'},
    DICOM: {title:'DICOM Upload',billingType:'dicom'}
};

/**
 * PaymentDialog shows a dialog that tells the user they will be charged
 * for a transaction.
 */
function paymentRequiredDlg(evt, okHandler, customProperties) {

  // If billing is disabled, do not show the dialog 
  if(!enableBilling) {
    okHandler();
    return;
  }

  var props = { title: 'Payment Required', desc: 'This operation requires a payment in order to proceed:'};

  if(customProperties) {
    props = merge(props,customProperties);
  }

  var desc = PAYMENT_DESCRIPTIONS[evt.type];
  var units = desc.units?desc.units : '';
  var content ='<p>'+props.desc+'</p>'; 
      content += '<table class="dialogTable" id="paymentTable"><tr><th>Type</th><th>Charge Amount</th></tr>';
      content += '<tr><td>'+desc.title+'</td><td>'+evt.quantity+' '+ units +  '</td></tr></table>';
      content += '<br/>If you would like to have this charged to your account press OK, otherwise press Cancel.';

  dialog('payreqd',props.title, 
    content,
    500,
    [ { text: 'OK', handler: function() { okHandler(); this.destroy(); } }, 
      { text: 'Cancel', handler: function() { this.destroy(); } } ]);
}

function noCreditDlg(evt, counters, okHandler) {
  var desc = PAYMENT_DESCRIPTIONS[evt.type];
  var units = desc.units?desc.units : '';
  var content ='<p>Your account did not have sufficient credit to pay for the operation you attempted.</p>';
      content += '<p>You require ' + evt.quantity + ' ' + desc.title + ' credits to proceed. '; 

      if(counters) {
        content += 'Your current balance is reported as:</p>';
        content += '<table class="dialogTable" id="countersTable"><tr>';

        for(n in PAYMENT_DESCRIPTIONS) {
            var d = PAYMENT_DESCRIPTIONS[n];
            content += '<th>'+d.title+'</th>';
        }
        content += '</tr><tr>';
        for(n in PAYMENT_DESCRIPTIONS) {
            var d = PAYMENT_DESCRIPTIONS[n];
            content += '<td>'+counters[d.billingType]+'</td>';
        }
        content += '</tr></table>';
      }
      content += '<br/>Please go to Settings > Purchased Services to add the desired credit to your account.';

  dialog('nocreditDlg','Insufficient Credit', 
    content,
    500,
    [ { text: 'Close', handler: function() { if(okHandler) okHandler(); this.destroy(); } } ]);
}
function authenticateImport() {
  execJSONRequest('AccountImport.action?authenticate=true', queryString($('accountImportForm')), function(result) {
     if(result.status == 'ok') {
       window.location='AccountImport.action?displayConfirm&sourceUrl='
                     + encodeURIComponent(result.sourceUrl)
                     +'&sourceAuth='+encodeURIComponent(result.token);
     }
     else {
      alert("There was a problem authenticating the credentials you provided:\r\n\r\n"+result.error);
     }
  });
}
function importCCR() {
  var tab = parent.addTab("Import CCR *","import.jsp?ccrIndex="+parent.currentTab.ccrIndex,parent.currentTab);
  parent.showTab(tab);
}

function importAccount() {
  // Display dialog asking for details
  dialog('importDlg', 'Import Account', 
      '<p>You can import contents from another account into this account.'
    + 'To do this you need to enter your credentials for the other account below:</p>'
    + '<form id="accountImportForm">'
    + '<table><tr><th>HealthURL</th><td><input type="text" name="sourceUrl" style="width:300px;"/></td>'
    + '<tr><th>Password / PIN</th><td><input type="password" name="password" size="15"/></td></table></form>',
      420, // width
      [ 
        { text: 'Import', handler: authenticateImport},
        { text: 'Cancel', handler: function() { this.destroy(); } }
      ]);
}

function importVoucher() {
  // Display dialog asking for details
  dialog('importDlg', 'Import Voucher', 
      '<p>You can import contents from a MedCommons Voucher.'
    + 'To do this you need to enter the Voucher details below:</p>'
    + '<form id="voucherImportForm">'
    + '<table><tr><th>Voucher ID</th><td><input type="text" name="voucherId" style="width:90px;"/></td>'
    + '<tr><th>Password</th><td><input type="password" name="password" size="15"/></td></table></form>',
      420, // width
      [ 
        { text: 'Import', handler: authenticateVoucher},
        { text: 'Cancel', handler: function() { this.destroy(); } }
      ]);
}



function authenticateVoucher() {
  execJSONRequest('AccountImport.action?authenticateVoucher=true', queryString($('voucherImportForm')), function(result) {
     if(result.status == 'ok') {
       window.location='AccountImport.action?displayConfirm&sourceUrl='
                     + encodeURIComponent(result.sourceUrl)
                     +'&sourceAuth='+encodeURIComponent(result.token);
     }
     else {
      alert("There was a problem authenticating the credentials you provided:\r\n\r\n"+result.error);
     }
  });
}

function uploadAccountFiles() {
    window.open('viewEditCCR.do?ccrIndex='+window.parent.currentTab.ccrIndex+'&mode=uploadAccountFiles', "Upload", "menubar=1,resizable=1,width=610,height=300");
}
 
/**
 Directs to page where HealthFrame installer is located.
*/
function installHealthBook(){
	var url = "http://www.medcommons.net/healthbook.php"; // Don't want to hard code version here.
  window.top.setTools([]);
  window.top.setButtons([{text: 'Return to CCR', action: 'window.contents.location="viewEditCCR.do?mode=view&ccrIndex='+parent.currentTab.ccrIndex+'"', tip: 'Return to editing / Viewing your CCR', actionTarget: 'top'}]);
	document.location.href=url;
}

function launchHealthFrameURL(urlToLaunchHealthbook){
        return("<a href='" + 
            urlToLaunchHealthbook +
            "'>Edit using HealthBook </a>");
}
function installHealthFrameURL(urlToInstallHealthbook){
        return("Please <a href='" + 
                urlToInstallHealthbook +
                "'>install </a> HealthBook");
}

function isHealthBookInstalled(){
 	
 	
 	var checkHealthBook=  $('HBCHECK'); //el("HBCHECK"); //document.HBTEST
 	if ((checkHealthBook == null) && (self != top))
 		checkHealthBook = top.document.HBCHECK;
 	//alert ("checkHealthBook = " + checkHealthBook);
    var installed = HEALTHBOOK_NOT_INSTALLED;
    if (navigator.mimeTypes.length>0){ // Mozilla
    	 var healthFrameDetected = navigator.mimeTypes["application/x-healthbook-url"];
         
         if (healthFrameDetected){
            installed = HEALTHBOOK_INSTALLED;
         }
         else{
         	installed = HEALTHBOOK_NOT_INSTALLED;
         }
    }
    else if (checkHealthBook == null || !checkHealthBook.HealthBookOCXRunning) {
         installed = HEALTHBOOK_CAN_NOT_BE_DETECTED; // Possibly activeX controls disabled.
        // alert("ActiveX controls may be disabled");
    }
    else if (checkHealthBook.HealthBookInstalled) {
        installed = HEALTHBOOK_INSTALLED;
        //alert("HealthBook installed (IE)");
    }
    else {
    	//alert ("else ...  healthbook installed = false");
        installed = HEALTHBOOK_CAN_NOT_BE_DETECTED;
    }
  // alert("returning " + installed);
   return(installed);

}

function healthBookLink(urlToLaunchHealthbook, urlToInstallHealthbook){
    var installed = isHealthBookInstalled();
    var htmlFragment = null;
    if (installed == true){
    	 htmlFragment = launchHealthFrameURL(urlToLaunchHealthbook);
    }
    else{
    	htmlFragment = installHealthFrameURL(urlToInstallHealthbook);
    }
    
   return(htmlFragment);
}

function completeVoucher() {
  execJSONRequest('Voucher.action',{ccrIndex:0}, function(r) {
      if(r.status == "ok") {
        $('voucherStatus').innerHTML = '<b>Completed</b>';
        ce_signal('voucher_change_state');
      }
      else {
        alert("An error occurred while completing  the voucher request (status = "+r.status+"):\n\n"+r.message);
      }
  });
}

function showStartupMessage() {
  if((window != window.top) && window.parent.startupMsg) {
    dialog('startupMsg','One Time Use Link',window.parent.startupMsg, 400, null, function(dlg) {
      dlg.cfg.setProperty("icon",YAHOO.widget.SimpleDialog.ICON_WARN);
    });
    window.parent.startMsg = null;
  }
}

function warnIncompletePatient() {
    dialog('incompletePatientWarning','Incomplete Patient Data',
           'The data for this patient has not finished uploading.<p style="margin-left:31px;">Images or other information may be missing, '
          +'and visible data may change as further content is uploaded.</p>', 460, null, function(dlg) {
      dlg.cfg.setProperty("icon",YAHOO.widget.SimpleDialog.ICON_WARN);
    });
}

