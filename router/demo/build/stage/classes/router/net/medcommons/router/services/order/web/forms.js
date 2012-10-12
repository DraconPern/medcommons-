/*
 * Quick Share Forms Javascript
 * <p>
 * Depends on MochiKit
 */


/***************************************************************
 * Utilities
 ****************************************************************/

function $Y(e) {
    return new YAHOO.util.Element(e); 
}

/**
 * Return a text field with the given name and id and
 * optional value.
 */
function text(id,value) {
    return input('text',id,value);
}

/**
 * Return an input field with the given type, name and id and
 * optional value.
 */
function input(type,id,value,title,label,options) {
    if(!title)
        title='';
    
    // Ensure that each radio button in a set gets a unique id
    var name=id;
    if(type=='radio')
        id=id+'_'+value;
    
    var atts = {type:type, name:name, id: id, value: value?value:'',title:title};
    if(options)
        update(atts,options);
    
    if(label)
	    return SPAN({},INPUT(atts),LABEL({'for':id, title: title},label)); 
    else
	    return INPUT(atts);
}

/**
 * Return a table wrapping the given body elements
 */
function table( /* b1, b2, ... */ ) {
    var args = [];
    forEach(arguments, function(x) { args.push(x); });
    
    var id = '';
    if((typeof args[0])=='string')
        id = args.shift();
    
    args.unshift({});
    
    return TABLE({id:id}, TBODY.apply(window, args));
}

/**
 * Return the given label and field wrapped in a table row
 * suitable for embedding in a form.
 * 
 * @param label     label for the form field
 * @param field     form field to embed  
 */
function formRow(label,field) {
    return TR({'class':'formTable'},TH({},label),TD({},field));
}

function showSharingDialog(options) {
    
    // Trying to show the form when it is already shown?
    if($('shareDlg'))
        return;
    
    var div;
    var optionRows = [];
    for(var i in options.customFields) {
        if(i.match('_label') && options.customFields[i]) {
            optionRows.push(formRow(options.customFields[i], options.customFields[i.replace('_label','')]));
        } 
    }
    
    var optionDiv = optionRows.length ? 
            DIV({}, DIV({'class':'lineHeader'},HR(), H4('Information'),BR()), table.apply(this, optionRows))
        : SPAN('');
	    
    dialog('shareDlg', 'Share Patient Account', 
		    div=DIV({},
				    optionDiv, 
				    DIV({'class':'lineHeader'},HR(), H4('Send'),BR()),
				    window.shareTable=DIV({},emailShareForm(options))), 500, 
		    {
               Share: function() {
                   window.dlg = this;
                   var form = $Y(div).getElementsByTagName('form')[0];
                   forEach(dlg.getButtons(), function(b) {b.set("disabled",true);});
                   window.form = form;
                   if(form.executeShare(dlg))
                       forEach(dlg.getButtons(), function(b) {b.set("disabled",false);});
			   },
			   
			   Cancel: function() {
				   this.destroy();
			   }
		    }
	);
}

/**
 * Output a row of 3 stars with a specified number of them filled
 */
function stars(count) {
    var container = SPAN({'class':'stars'});
    for(var i=0; i<3; ++i) {
        appendChildNodes(container,
		    i<count?IMG({src:'images/fullstar.png'}) : IMG({src:'images/emptystar.png'}));      
    }
    return container;
}

/***************************************************************
 * Forms
 ****************************************************************/

/**
 * Return a form for sharing by email
 */
function emailShareForm(options) {
    
    if(!options) 
        options = { patientId: '' };
    
    if(!window.loadingImg)
	    loadingImg = IMG({src:'yui-2.8.0r4/assets/skins/mc/wait.gif'});
    
    if(!window.sentImg)
	    sentImg = IMG({src:'images/tick.png'});
    
    var def,email,subject,message;
	var form =FORM({action: 'SharePHR.action'}, input('hidden','fromAccount',options.patientId), table( 
        formRow('Security', DIV({},
                def=input('radio','control','pin',
                      'A secret PIN will be created which you must communicate to the recipient', 
                          SPAN({},stars(3),'PIN'),
                          {checked:true}),
                      BR(),
                input('radio','control','register', 'The recipient must have or register an account with the email address specified to access the patient', 
                      SPAN({},stars(2),'Email Registration')),
                      BR(),
                input('radio','control','one', 
                      'The recipient will receive a link to let them view the data one time only.  They will be able to keep access to the patient by registering.', 
                      SPAN({},stars(1),'One-Time Open Link (can be forwarded)')),
                      BR(),
                input('radio','control','public', 'A link will be sent that works without any restriction.', 
                      SPAN({},stars(0),'None (Public)'))
                )),
        formRow('To', email=input('text','toEmail',options.to)),
        formRow('Subject', subject=text('subject',options.subject)), 
        formRow('Message', message=TEXTAREA({rows:5, cols: 40, name:'message'})) 
    )); 
	
	form.executeShare = function(dlg) {
	    try {
	        
            $Y(email).removeClass('invalid');
            var valid = true;
	        if(!isValidEmail(email.value)) {  
	            $Y(email).addClass('invalid');
	            valid = false; 
	        }
	        
	        if(subject.value=='') {
	            $Y(subject).addClass('invalid');
	            valid = false;
	        }
	        
	        if(!valid) {
	            alert('One or more fields was missing or incorrectly filled out.\n\nPlease check the highlighted values and try again.');
	            return true;
    	    }
	        
			var container = form.parentNode;
	        replaceChildNodes(container, DIV({id:'waitMessage'}, loadingImg,'Sending ...'));
	        var params = parseQueryString(queryString(form)); // hack
	        if(typeof ccrIndex != 'undefined') {
	            params.ccrIndex = ccrIndex;
	        }
	        execJSONRequest(form.action, queryString(params), function(result) {
	            window.result = result;
	            if(result.status == 'ok') { 
	                replaceChildNodes(container, 
	                        DIV({id:'finishedMessage'}, 
	                                DIV({id:'waitMessage'},sentImg,'Sent!')));
	                if(form.control && (params.control=='pin')) {
	                    appendChildNodes('waitMessage',  
	                            DIV({style:'margin-left: 2em;'},BR(),createDOM('B','Tracking Number: '),prettyTrack(result.trackingNumber),createDOM('B',' PIN: '), result.pin));
	                }
	                
                    if(options.next) {
		                dlg.cfg.setProperty("buttons", [{text:"Next",handler:function(){
		                        var parts = options.next.split('?'); 
		                        var info = queryString(result);
		                        var url;
		                        if(parts.length>1)
		                            url = options.next+'&'+info;
		                        else 
		                            url = options.next+'?'+info;
		                        window.location.href = url;
		                    }
		                }]);
	                }
                    else
		                dlg.cfg.setProperty("buttons", [{text:"Close",handler:function(){this.destroy();}}]);
	            }
	            else {
	                alert('There was a problem sharing the content:\n\n'+result.error);
	                dlg.destroy();
	            }
	        });
	    }
	    catch(e) {
	        dump("Failed to send share invitation", e);
	    }
        return false;
	};
	return form;
}

/**
 * Return a form for sharing by fax
 */
function faxShareForm() {
     return FORM({action: 'SharePHR.action?fax=true'}, table(
                        formRow('To', text('to')),
                        formRow('Fax Number', text('fax')),
                        formRow('Subject', text('from')),
                        formRow('Message', TEXTAREA({rows:5, cols: 40, name:'message'}))
            ));   
}

/**
 * Return a form for sharing by Phone / SMS
 */
function smsShareForm() {
    
    return FORM({action: 'ShareByPhone.action'}, table(
                        formRow('To', text('to')),
                        formRow('Phone Number', text('fax')),
                        formRow('Message', TEXTAREA({rows:5, cols: 40, name:'message'}))
           ));
}
