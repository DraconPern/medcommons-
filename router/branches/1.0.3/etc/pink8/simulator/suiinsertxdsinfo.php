<?php
// (c) 2004 MedCommons, Inc.
// wld 8/30/04
// suiinsertxdsinfo.php
require_once ("../lib/htmlsubs.inc");

function insertxds()
{
	htmltop("Insert Into XDS Registry",SIMULATOR_STYLE);


       
	htmlbody(
	
		formheader("../xmlws/wsinsertxds.php",
		"Insert into xds registry").
		
	
ft("studyguid", "studyguid").
ft("tracking", "tracking").
ft("vaetitle", "vaetitle").
ft("number of images", "nimages").
ft("number of series", "nseries").
ft("study description", "studydescription").
ft("studytime", "studytime").
ft("comments", "comments").
ft("modaility", "modality").
ft("affiliate", "affiliate").
ft("history", "history").
ft("patient", "patient").
ft("patient address", "patientaddr").
ft("intial status", "status").



		formsubmit("Submit XDS Registry Element").
		formend()
		);			
	echo htmlfooter();
}
insertxds();
?>
