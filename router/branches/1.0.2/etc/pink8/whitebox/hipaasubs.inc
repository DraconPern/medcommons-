<?php
require_once("../whitebox/wbsubs.inc");


//
// display part of the order table in HIPAA format
//

function order_as_hipaa ($gwurl,$guid, $tracking, $origin, $dest, $time, $status, $description,
$patientname, $patientid, $modality, $series, $nimages)
{
	// first figure out the 'Order URL

	$url = makewadourl ($gwurl,$tracking,$guid);
	if ($GLOBALS['hipaapreamble']!="")
	{$x = $GLOBALS['hipaapreamble'];
	$GLOBALS['hipaapreamble']="";}
	else $x="";
	$x.=<<<BBB
            <tr>
              <td class="selRow">$time</td>
              <td class="selRow"><a href = $url>$tracking</a></td>
              <td class="selRow">$origin</td>
              <td class="selRow">$patientname</td>
              <td class="selRow">$modality</td>
              <td class="selRow">$series</td>
              <td class="selRow">$nimages</td>
               <td class="selRow">$status</td>
            </tr>
BBB;
	return $x;
};

function recent_hipaa($limit, &$total,$lowertime,$uppertime){

	
$GLOBALS['hipaapreamble'] .= <<<BBB

<table>
            <tr>
              <th class="selRow">Date and Time</th>
              <th class="selRow">MC Tracking Number</th>
              <th class="selRow">    Sender    </th>
              <th class="selRow">    Patient    </th>
              <th class="selRow">    Modality  </th>
              <th class="selRow">  Series  </th>
              <th class="selRow">  Images  </th>
               <th class="selRow">  Status</th>
            </tr>
BBB;
// pass in wide date ranges for now	
        		
		$toret = getordermatches('order_as_hipaa',$limit,$lowertime,$uppertime, $total).
			"</table>";
	
//$total = $GLOBALS['total'];

return $toret;
}
function getrange($month,$year,&$lower,&$upper)
{
  $monthstart=array("01","02","03","04","05","06","07","08","09","10","11","12");
  
  $lower='20010101';
  $upper='20291231'; //mysql is very fussy about the upper date
 
  
  if ($year != 0) {  // already set if year is zero
   $year+=2000; //make it clean

  if ($month == 0) {
  	
  	$lower = $year."0101";
  	$upper = $year."1231";
}
  	else {	
  		$lower = $year.$monthstart[$month-1]."01";
  		$upper = $year.$monthstart[$month-1]."31";// close enough
  	}
  }
    $lower .= "000000";
  	$upper .= "235959"; 
}
function hipaa_log($limit,&$count, $month,$year)
{ // if month is zero, pick the whole year
  // if year is zero pick everything

  	getrange($month,$year,$lower,$upper);
	return recent_hipaa( $limit,$count,$lower,$upper);

}
function hipaa_title($month,$year)
{
	  $pmonth = array("jan","feb","mar","apr","may","jun","jul","aug","sep","oct","nov","dec");

	 $range = "Returning all records";
	 if ($year !=0) {
	 	$year +=2000;
	 	if ($month==0)
	 		$range = "Returning all records from  $year";
	 		else {$pretty = $pmonth[$month-1];
  		  	$range = "Returning all records from $pretty, $year";}
	 }
	 		
	   	$range = "<h4>$range</h4>";
	   	
	   	$GLOBALS['hipaapreamble']=$range; // if they asked for a title, give them one
		
		
}



				

?>