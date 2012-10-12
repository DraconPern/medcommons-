function setHtml(id, content) {
  document.getElementById(id).innerHTML=content;
}

function selectContact(index) {
  log("Clicked contact " + index);
  for(i=0; i<contacts.length; ++i) {
    var contact = contacts[i];
    log("Contact " + i + " selected = " + contact.selected);
    if(contact.selected && (i!=index)) {
      log("Unselected contact " + i);
      var contactTr=document.getElementById('contactRow'+i);
      var contactTr2=document.getElementById('contactRow2-'+i);
      contactTr.className=oldContactClass;
      contactTr2.className=oldContactClass;
      contact.selected=false;
    }
  }

  if(!contacts[index].selected) {
    var contact = contacts[index];
    var contactTr=document.getElementById('contactRow'+index);
    var contactTr2=document.getElementById('contactRow2-'+index);
    log("Selected contact " + index + " old class = " + contactTr.className);
    oldContactClass = contactTr.className;
    contact.selected=true;
    contactTr.className = 'contactsRowSelected contactsRow';
    contactTr2.className = 'contactsRowSelected contactsRow';

    // Set the new content in the display card
    setHtml('clName',contact.givenName + '&nbsp;' + contact.familyName);
    if(contact.emails.length > 0) {
      setHtml('clEmail',contact.emails[0]);
    }
    else {
      setHtml('clEmail','&nbsp;');
    }
    if(contact.phoneNumbers.length > 0) {
      setHtml('clPhone',contact.phoneNumbers[0].type + ': ' + contact.phoneNumbers[0].value);
    }
    else {
      setHtml('clPhone','&nbsp;');
    }
    setHtml('clAddress',contact.address1  + '<br/>' 
      + contact.city + '&nbsp;' + contact.state + '&nbsp;' + contact.postalCode);

    setHtml('clOrg',contact.organization);
    document.getElementById('contactlistCard').style.display='block';
    document.getElementById('contactsDetails').style.height=515;
  }
}


