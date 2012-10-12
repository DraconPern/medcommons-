function getContactsPrompt(){
      debug.log("getContactsPrompt");
	  
		var pageSize = prompt("Page size", 10);
		if (pageSize) {
			var pageNumber = prompt("Page number", 1);
			if (pageNumber) {
				var nameFilter = prompt("Name filter", null);
				getContacts(parseInt(pageSize), parseInt(pageNumber), nameFilter);
			}
		}
    }

		
    function getContacts(pageSize, pageNumber, nameFilter){
  
      var fail = function(){};
	  
	  var options = {};
	  if (pageSize)
		options.pageSize = pageSize;
	  if (pageNumber)
		options.pageNumber = pageNumber;
	  if (nameFilter)
		options.nameFilter = nameFilter;
	  
	  var durationOptions = { minDuration : 2 };
	  if (navigator) navigator.notification.loadingStart(durationOptions);
          if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else 
                    navigator.ContactManager.getAllContacts(getContacts_Return, fail, options);
    }
	
	function getContacts_Return(contactsArray)
	{
		var names = "";
		
		for (var i = 0; i < contactsArray.length; i++) {
			var con = new Contact();
			con.firstName = contactsArray[i].firstName;
			con.lastName = contactsArray[i].lastName;
			con.phoneNumber = contactsArray[i].phoneNumber;
			con.address = contactsArray[i].address;	
			names += con.displayName();
			
			if (i+1 != contactsArray.length)
				names += ",";
		}
		//send list up to medcommons
		//debug.log("getContacts send to MedCommons");		
		post_upstream(url,'getContacts', names);
		}
		
		

	var __editDisplayFirstContact = false;
	function displayFirstContact(allowsEditing)
	{
	  var options = { pageSize : 1, pageNumber: 1 };
	  __editDisplayFirstContact = allowsEditing;
             if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
	  navigator.ContactManager.getAllContacts(displayFirstContact_Return, null, options);
	}
	
	function displayFirstContact_Return(contactsArray)
	{
		var options = { allowsEditing: __editDisplayFirstContact };
		
		for (var i = 0; i < contactsArray.length; i++) {    
           if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
			navigator.ContactManager.displayContact(contactsArray[i].recordID, null, options);
		}
		//d//ebug.log("displayFirstContact_Return send to MedCommons");
		//send list up to medcommons
		
		post_upstream(url,'displayFirstContact',names);
	}

	function contactsCount(){
      //debug.log("contactCount");
             if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
      navigator.ContactManager.contactsCount(showContactsCount);
    }
	
	function showContactsCount(count){
		alert("Number of contacts: " + count);
	}

	function addContact(gui){
		var sample_contact = { 'firstName': 'your', 'lastName' : 'name', 'phoneNumber': '555-5555' };
	  if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else 
		if (gui) {
			navigator.ContactManager.newContact(sample_contact,false, { 'gui': true });
			addContact_Return(contact);// simulate completion
		} else {
			var firstName = prompt("Enter a first name", sample_contact.firstName);
			if (firstName) {
				var lastName = prompt("Enter a last name", sample_contact.lastName);
				if (lastName) {
					var phoneNumber = prompt("Enter a phone number", sample_contact.phoneNumber);
					if (phoneNumber) {
						sample_contact = { 'firstName': firstName, 'lastName' : lastName, 'phoneNumber' : phoneNumber };
                               if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
						navigator.ContactManager.newContact(sample_contact,addContact_Return);
			
					}
				}
			}
		}
	}
    
    function addContactGui() { addContact(true); }
    
    function addContact_Return(contact)
	{
		if (contact) {
         var x =  document.getElementById('topbox').innerHTML;
			post_upstream(url,'NewContact ',                            
                            '<p>added: '+contact.firstName + " " + contact.lastName +' phone: '+ contact.phoneNumber +
                            '</p> invitor: '+x);
	
		}
	}
    
	function chooseContact(allowsEditing)
	{
		var options = { allowsEditing: allowsEditing };
               if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
		navigator.ContactManager.chooseContact(chooseContact_Return, options);
	}
		function chooseContact_Return(contact)
	{
		if (contact) {
	
         var x =  document.getElementById('topbox').innerHTML;
		

		post_upstream(url,'chooseContact ',x);
		}
	}

	function mcChooseContactsFilterNotify(nameFilter,allowsEditing)
	{
	
	var contactsNotifyCallback = 	function (contact)
					{
					if (contact) {			
							post_upstream(url,'NotifyContacts','filter:'+nameFilter+' name:'+contact.firstName + " " + contact.lastName);
								}
					};
		var options = { allowsEditing: allowsEditing };
		
		options.nameFilter = nameFilter;
               if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else     
                            navigator.ContactManager.chooseContact(	contactsNotifyCallback,	options);
	}
    
function contactInviteCallback(contact)
					{
					debug.log ('contact invite callback: '+contact.firstName + " " + contact.lastName);
					if (contact) {	                 
                              
                var y = 'contact info:' + JSON.stringify(contact, function (key, value) {    return value;});      
                var x = '<br/><br/>user info:' + document.getElementById('topbox').innerHTML + 
                         '<br/><br/>practice info:' + document.getElementById('bottombox').innerHTML;	
                                     
			   post_upstream(url,'InviteContact', y+x);
								}
					}
	
function mcChooseContactInvite()
	{
		var options = {  };
	
               if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
		{ 	debug.log ('choose contact invite');
        
           navigator.ContactManager.chooseContact(	contactInviteCallback, 	options);
             }
	}
    
    
    
	function contactRegisterCallback(contact)
					{
   
					debug.log ('register contact callback :'); //contact.firstName + " " + contact.lastName);
					if (contact) {		
                    
               var y = 'contact info:' + JSON.stringify(contact, function (key, value) {    return value;});      
                var x = '<br/><br/>user info:' + document.getElementById('topbox').innerHTML + 
                         '<br/><br/>practice info:' + document.getElementById('bottombox').innerHTML;	
                                     
			   post_upstream(url,'RegisterContact', y+x);
									}
					}

  function mcChooseContactRegister()
	{
		var options = {};	
               if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
               {
               
			debug.log ('choose contact register');
        	navigator.ContactManager.chooseContact(	contactRegisterCallback,				options);
            }
	}


	

	
	

    
	function removeContact()
	{
       if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
                        navigator.ContactManager.chooseContact(removeContact_Return, null);
	}
	
	function removeContact_Return(contact)
	{
		if (contact) {
               if ( !(navigator && navigator.ContactManager) ) alert ("only in phoneGap"); else  
                            navigator.ContactManager.removeContact(contact.recordID, removeContact_Success, null);
		}
	}

	function removeContact_Success(contact)
	{
		if (contact) {
	      
               var y = 'removed:' + JSON.stringify(contact, function (key, value) {    return value;});      
                var x = '<br/><br/>user info:' + document.getElementById('topbox').innerHTML + 
                         '<br/><br/>practice info:' + document.getElementById('bottombox').innerHTML;	
		
		post_upstream(url,'removeContact', y+x);
		}
	}
	
    
    // Camera Stuff
  function getPhoto_Return(imageData)
 {
   

   var y = "<img src='data:image/jpeg;base64," + imageData + "' height=480px width=320px alt='camera img' />";
                var x = '<br/><br/>user info:' + document.getElementById('topbox').innerHTML + 
                         '<br/><br/>practice info:' + document.getElementById('bottombox').innerHTML;	
                                     
			   post_upstream(url,'PhotoData', y+x);
 }

function getPhotoButtonClicked(event)
{
    // Insert Code Here
              if ( !(navigator && navigator.camera )) alert ("only in phoneGap"); else 
    navigator.camera.getPicture(getPhoto_Return);
}
    
    
    
   