<?
 require_once "settings.php";
 global $Secure_Url;
 $now = time();   
 
 // YAHOO does not allow images to be https
 $baseUrl =  str_replace("https://","http://",$Secure_Url);
 $hurlimg = $baseUrl."/images/tinyhurl.png";
 $search = req('searchPatientName','');
?>

<h4 id='gadgetTitle'><?=htmlentities($group->name)?> Patients</h4> 

<a href='<?=$baseUrl?>/acct/home.php' style='float: left; position: relative; top: 2px; left: 3px;' target='_new'><img src='<?=$baseUrl?>/images/arrow_up.gif'/></a>
<div id='search' style='text-align:right; font-size: 9px; margin-right: 4em;'> 
  <input type='text' style='width: 60%; font-size: 9px;' id='searchPatientName' name='searchPatientName' value='<?=htmlentities($search, ENT_QUOTES)?>' />
  <input style='font-size: 9px;' type='submit' value='Go' onclick='updateSearch();'/>
</div>

<table style='width: 95%;'>
<? foreach($rows as $l): ?>
<?
    $viewerurl = $l->ViewerURL."&a=$accid&at=$auth"; // add our account id and auth token to viewer url
    $purpose = htmlspecialchars($l->Purpose);
    if($l->PatientIdentifier) {
      $href = $Secure_Url."/".$l->PatientIdentifier;
      if($l->couponstatus && ($l->couponstatus=='issued'))
        $href.="?c=iv";
    }
    else
      $href = $viewerurl;
      
    $patientName = htmlentities($l->PatientGivenName,ENT_QUOTES).' '.htmlentities($l->PatientFamilyName,ENT_QUOTES);
    if(trim($patientName)=="")
        $patientName = "<i style='color: gray;'>No name</i>";

    $ageSex = '';
    if($l->PatientAge)
        $ageSex = $l->PatientAge;
        
    switch($l->PatientSex) {
        case 'Male':
            $ageSex .= 'M';
            break;
        case 'Female':
            $ageSex .= 'F';
            break;
        default:
    }
    
    $ct = $l->CreationDateTime;
    $dateTime = $template->formatAge($ct,$now);
?>

  <tr> 
    <td><a href='<?=$href?>' target='ccr'><img style='border: none;' src='<?=$hurlimg?>'/></a> <?=$patientName?></td><td><?=$ageSex?></td><td><?=$dateTime?></td>
  </tr>
<?endforeach;?>
  <tr>
      <td>&nbsp;</td><td colspan='2' style='font-weight: bold;'>Showing <?=$displayedCount?> of <?=$allCount?></td>
  </tr>
  </table>
