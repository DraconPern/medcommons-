XXX;
				 	
							 
				 
$stat = @mail($recipient, $subjectline, 
		$message,
     "From: MedCommons@{$_SERVER['SERVER_NAME']}\r\n" .
     "Reply-To: cmo.medcommons.net\r\n" .
     "bcc: cmo@medcommons.net\r\n".
     "Content-Type: text/html; charset= iso-8859-1;\r\n"
     );
if($stat) return "ok"; else return "send mail failure";
}
