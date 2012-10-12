/**
 * Copyright 2004 MedCommons Inc.   All Rights Reserved.
 */
function SubmitRegisterForm() {
	
	if (document.RegisterForm.realname.value=="") {
		
		alert("please enter your name");
		document.RegisterForm.realname.focus();
		
	}
	else if (document.RegisterForm.email.value=="") {

		alert("please enter your email address");
		document.RegisterForm.email.focus();
		
	}
	else {
		
		document.RegisterForm.submit();
		
	}
	
}
	