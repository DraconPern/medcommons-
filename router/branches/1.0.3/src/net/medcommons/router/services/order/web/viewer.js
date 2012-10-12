

/**
 * Array of information about all the CCRs belonging to the patient
 */

function loadCCRs() {
    execJSONRequest('QueryPatientCCRs.action',{ccrIndex:ccr.index}, function(result) {
        
        if(!result || (result.status!="ok")) {
          alert("An error occurred while retrieving other CCRs for this patient:\r\n\r\n"+result.message);
          return;
        }
        
        log("====> Adding CCRs " + result.profiles); 
        
        patientCCRs = addDocumentInfo(result.profiles);
        
        var nodes = map(ccrNode, patientCCRs);
        nodes.unshift({'class':'folderList'});
        replaceChildNodes($('lefttop'), DIV({}, DIV.apply(window,nodes)));
    });
}

/**
 * Return a DOM node representing a CCR and its 
 * series in a tree style
 * 
 * @param pf
 * @return
 */
function ccrNode(pf) {
    var title = tabDateTitle(pf);
    if(pf.documentType) {
        // url += '&l='+pf.documentType;
        title = documentTypes[pf.documentType];
    }
    return DIV({'class':'encounterRow'},IMG({src:'images/folder.gif'}),title);
}

var shareTabs;
function showShareDialog() {
    var theDialog;
    var buttons = [{ text: 'Send Invitation', handler: function() { 
        var form = shareTabs.get("activeTab").get("contentEl").getElementsByTagName('form')[0];
        forEach(theDialog.getButtons(), function(b) {b.set("disabled",true);});
        form.executeShare(theDialog);
    }}];
    
    dialog('shareDlg', 'Share', '<div id="shareContent"></div>', 600, buttons, function(dlg) {
        theDialog = dlg;
        shareTabs = new YAHOO.widget.TabView();
        
        var Tab = YAHOO.widget.Tab;
        shareTabs.addTab(new Tab({
            label: 'Share by Email',
            content: '<div id="emailTab"></div>',
            active: true
        }));
         
        shareTabs.addTab(new Tab({
            label: 'Share by Fax',
            content: '<div id="faxTab"></div>'
        }));
         
        shareTabs.addTab(new Tab({ 
            label: 'Share by SMS',
            content: '<div id="smsTab"></div>'
        }));
         
        shareTabs.appendTo($('shareContent'));
        
        replaceChildNodes('emailTab', emailShareForm());
        replaceChildNodes('faxTab', faxShareForm());
        replaceChildNodes('smsTab', smsShareForm());
    });
}
