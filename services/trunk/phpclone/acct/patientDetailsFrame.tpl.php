<?          
  if($activeGateway) {
    dbg("active gw");
    $displayUrl = $activeGateway."/CurrentCCRWidget.action?combined&accid=".$info->accid."&auth=".$info->auth."&returnUrl=".urlencode(gpath('Accounts_Url')."/clearPatientDetails.php?cleargw=true");
  }
  elseif($cccrGuid && !$info->practice) {
    dbg("cccr and patient mode ");
    $displayUrl = gpath('Commons_Url')."/gwredirguid.php?guid=$cccrGuid&nopage&dest=".urlencode("CurrentCCRWidget.action?combined&accid=".$info->accid);
  }
  else {
    $displayUrl = false;
  }
?>
<div id='patientDetails'>
  <?if($displayUrl):?>
    <span id="patientDetailFrameMarker"></span>
    <iframe src='<?=$displayUrl?>' name='patientDetailsFrame' width='98%' allowtransparency='true' background-color='transparent' frameborder='0' scrolling='no' height='1000px'>Your browser doesn't support iframes.</iframe>
  <?else:?>
    <?if($info->practice):?>
      <?if(isset($patientCount) && ($patientCount==0)):?>
      <script type='text/javascript'>addLoadEvent(function() {
          addMessage("You don't have any patients yet.  Use the 'New' button to create patients or upload DICOM");
      });</script>
      <?else:?>
        <p>Click a patient name to open their Current CCR.</p>
      <?endif;?>
    <?else:?>
      <br/>
      <p>You do not yet have a Current CCR associated with your account.</p>
      <br/>
      <p>To get started, <a href="<?=new_ccr_url($info->accid,$info->auth, "new")."&am=p"?>" title="Create a new CCR" target="ccr">Create</a>
      or <a href="<?=gpath('Commons_Url')?>/gwredir.php?a=ImportCCR" title="Create a CCR by uploading a file" target="ccr">Import</a>
       a CCR, and set it as your Current CCR.</p>
    <?endif;?>
    <br/>
    <br/>
  <?endif;?>
</div>
