<?php
function getactorinfo($xml)
{	
	$name = $xml->Name;
	$cn = $name->CurrentName;
	return "<Given>".$cn->Given."</Given><Middle>".$cn->Middle."</Middle><Family>".$cn->Family."</Family>";
}
	
	$ccr=file_get_contents("../serverdata/txn168417654370/txn168417654370.ccrdata.xml");
	$opcode = "IMPLIEDTRANSFER";
	$ccrdata = simplexml_load_string($ccr);
	$dt = $ccrdata->DateTime->ExactDateTime;
	//dig the patient and provider out of the CCR
	$patientlink = $ccrdata->Patient->ActorID; $patientinfo="not found";
	$fromlink = $ccrdata->From->ActorLink->ActorID; $frominfo="not found";
	//now go thru all the actors, and figure out who is whom
		$actors = $ccrdata->Actors;

	foreach ( $actors->Actor as $actor)
	{
				
	switch ($actor->ActorObjectID){
	case $patientlink: $patientinfo = getactorinfo($actor->Person); break;
	case $fromlink: $frominfo = getactorinfo($actor->Person); break;
	default :
	}
	}
	echo "date $dt \r\n";
	echo "PatientInfo $patientinfo \r\n";
	echo "FromInfo $frominfo \r\n";
?>