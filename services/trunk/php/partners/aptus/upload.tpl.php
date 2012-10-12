<?
    $template->extend("simpledod.tpl.php");
?>

<?section("extraScript")?>
<script type='text/javascript'>
    /// This section duplicated in simpledod.tpl.php
    uploadOptions.showPrevious = false; 
    if(window.opener && window.opener != window) {
        replaceChildNodes($('simpleCloseButton'),'Close Window');
    }
    connect('simpleCloseButton', 'onclick', closeWindow);
    connect(ddlEvents, 'uploadStarted', function(evt) {
         appearX('endinfo');
         hide('waitstartmsg');
    });
    /// end duplication
    
    uploadParams.anonProfile = 'aptus';
    uploadParams.accessionNumber = order.accession_number;
    uploadParams.patientId = order.patient_id;
</script>
<?end_section("extraScript")?>
