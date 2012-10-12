<?PHP

// probe a certificate authority (Verisign) and shred the returned data

function getcertinfo($certUrl,&$serial,&$identityprovider,&$name,&$email,&$status){
	$str= @file_get_contents($certUrl);
	if ($str==FALSE) return FALSE;

	$serialpos = strpos($str,"<TD><font size=-1 face=arial>Serial Number</TD>");
	$serial = substr($str,$serialpos+77,32);

	$identityproviderpos = strpos($str,"<TD><font size=-1 face=arial>Organization =");
	
	$indentityproviderend = strpos($str,"<BR>",$identityproviderpos+strlen("<TD><font size=-1 face=arial>Organization ="));
	$identityprovider = trim(substr($str,$identityproviderpos+44,$indentityproviderend-$identityproviderpos-44));

	$namepos = strpos($str,"<TD valign=bottom><font size=-1 face=arial>Name</TD>");
	$nameend = strpos($str,"<SCRIPT",$namepos+strlen("<TD valign=bottom><font size=-1 face=arial>Name</TD>"));
	$name = trim(substr($str,$namepos+82,$nameend-$namepos-82));

	$emailpos = strpos($str,"<TD><font size=-1 face=arial>Email</TD>");
	$emailend = strpos ($str,"</TD>",$emailpos+strlen("<TD><font size=-1 face=arial>Email</TD>"));
	$email = trim(substr($str,$emailpos+69,$emailend-$emailpos-69));

	$statuspos = strpos($str,"<TD><font size=-1 face=arial>Status</TD>");
	$statusint = strpos ($str,"<TD><font size=-1 face=arial><B>",$statuspos+strlen("<TD><font size=-1 face=arial>Status</TD>"));
	$statusend = strpos ($str, "<script",$statusint);

	$status = trim(substr($str,$statusint+32,$statusend-$statusint-32));
	return TRUE;

}

?>