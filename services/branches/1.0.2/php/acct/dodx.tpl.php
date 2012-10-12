<?php 
global $Secure_Url;
$template->extend("dod.tpl.php");
section("css");
?>
<style type='text/css'>
  @import url('dod.css');

  table#orderSummary {
      width: 600px;
      padding-bottom: 10px;
      padding-top: 10px;
  }
 
  table#dataSourceLayout tr {
    margin-top: 5px;
  }
  
  table#dataSourceLayout {
      width: 100%;
  }
  
  table#dataSourceLayout th {
    text-align: left;
  }
  table.dodx th  {
    background-color: #bbbbaa;
    padding: 0px 3px;
  }
  table#patientDataLayout tbody td {
      padding-left: 13px;
  }
  
  table#patientDataLayout, #voucherDetailsStep table#voucherTable {
      margin-left: auto;
      margin-right: auto;
      width: 500px;
  }
  
  #patientDataLayout table th {
    background-color: #fffcdd;
    padding: 1px 3px;
  }
   table#dataSourceLayout td {
    vertical-align: middle;
   }
   
   table#dataSourceLayout td.first {
    padding-top: 5px;
  }
  
  #wholeStep1 {
      xmargin-top: 20px;
  }
  #wholeDataSourceStep {
  }
  textarea {
    overflow: auto;
  }
  #dicomOrderFormTable th {
    text-align: right;
  }
  #dicomOrderFormTable td {
    padding: 8px;
  }
  table#patientData {
    width: 100%;
  }
  table tr td.match {
      text-align: center;
  }
  .invisible {
    visibility: hidden;
  }

  table#patientData tr.mismatch td, 
  table#patientData tr.mismatch th {
    background-color: #f7f0cc;
  }
  p.error {
    background-color: #fff0f0;
    padding: 7px !important;
    border: solid 1px #a00;
  }
  #selectSourceStep {
      font-size: 12px;
      margin-left: 10px;
      padding-bottom: 10px;
  }
  body {
  }

  #orderSummaryContents {
      position: relative;
      top: -20px;
      margin: 0px 10px;
      width: 80%;
  }
  #cdinstructions, #selectSourceStep {
      height: 15em;
  }
  #cdinstructionlist {
      margin-bottom: 10px;
  }
  #mismatchMsg {
      color: #ee2222;
  }
  
  #selectImages {
      padding: 0px 1px;
      height: 24px;
      margin-left: 6px;  
  }
  
  .selectImagesTable {
      border-spacing: 10px 15px;
  }
  select#series {
    max-height: 13em;    
    height: 10em;
  }
  #beginUploadButton {
    width: 40%;
    margin: 0% 60% 0% 0%;
  }
  #matchCheckBoxMsg {
	  position: relative;
      top: -2px;
      margin-left: 3px;
  }
  
  </style>
<?end_section("css")?>

<div id='upload'>

<?section("topheader")?>
<h2>DICOM On Demand Image Upload</h2>
<?end_section()?>

<?section("toptext")?>
<?end_section()?>

<?section("startDDLHeading")?>
<h3 id='startDDLHeading'>1. Start DICOM Transfer Utility on your Computer</h3>
<?end_section("startDDLHeading")?>

<?section("dicomSourceHeading")?>
 <h3 id='dicomSourceHeading'>2. DICOM Source</h3>
<?end_section("dicomSourceHeading")?>

<?section("selectData");?>
<h3 id='patientDataHeading'>3. HealthURL Content</h3>
<div id='patientDataBlock' class='hidden'>
  <p>Please select a study and one or more series.  Check that patient details match expected values and click 
     the checkbox to confirm.</p>
      
    <table id='patientDataLayout' class='dodx'>
        <thead>
           <tr><th>Patient / DICOM</th></tr>
       </thead>
       <tbody>
        <tr>
        <td>
           <table id='patientData'>
               <tr><th>Patient</th><td id='patientName' colspan='1'></td><th>Date of Birth:</th><td id='patientDateOfBirth'></td></tr>
               <tr> <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td> </tr>
               <tr><th>Send</th><td colspan='3'><span id='selectionDescription'>All Images Found</span> <button id='selectImages'>Change</button></td></tr>
               <tr> <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td> </tr>
               <tr>
                   <th>Comment</th>
                   <td colspan='2'>
                       <textarea id='order_comments' name='order_comments' rows='2' cols='32' ><?=ent($order->comments)?></textarea>
                   </td><td>&nbsp;</td> 
               </tr>
               <tr> <td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td><td>&nbsp;</td> </tr>
               <tr class='invisible even matchRow' id='titleRow'>
                   <th>&nbsp;</th><th>DICOM</th><th>Order</th><th class='match'>Match</th>
               </tr>
               <tr class='invisible odd matchRow' id='patientIdRow'>
                   <th>Patient ID</th> <td id='patientId'></td> 
                   <td><?=ent($order->patient_id)?></td>
                   <td class='match'><img src='images/greentick.gif'/></td>
               </tr>
               <tr class='invisible even matchRow' id='scanDateRow'>
                   <th>Scan Date</th>
                   <td id='scanDate'></td> 
                   <td><?=ent(strftime('%m/%d/%Y %H:%M',strtotime($order->scan_date_time)))?></td>
                   <td class='match'><img src='images/greentick.gif'/></td>
               </tr>
               <tr class='invisible odd matchRow' id='modalityRow'>
                   <th>Modality</th>
                   <td id='modality'></td> 
                   <td><?=ent($order->modality)?></td>
                  <td class='match'><img src='images/greentick.gif'/></td> 
               </tr>
               <tr id='patientDataPadding' class='hidden matchRow'>
                   <th>&nbsp;</th> 
                   <td>&nbsp;</td>
                   <td>&nbsp;</td>
                   <td>&nbsp;</td>
               </tr>
             </table>
        </td>
        </tr>
        <?if(!$loggedIn):?>
        <tr class='buttonRow'>
        </tr>
        <?endif;?>        <tr class='buttonRow'>
            <td colspan='2'>
                <div id='confirmBlock'>
                <p id='mismatchMsg' class='hidden'>
                  <img src='images/redcross.gif'/> Indicates a mismatch between the DICOM and the Order. 
                  To continue anyway, please acknowledge below.
                </p>
                <p class='indent'>
                   <input type='checkbox' value='matched' id='matched' disabled='true' name='matched'/><span id='matchCheckBoxMsg'
                   >I confirm that this is the correct patient</span>
                   <span class='hidden'>
                   <br/>
                    <input type='checkbox' id='termsOfUse' name='termsOfUse' value='true' checked='true'/> 
                         I accept the <a id='termsOfUseLink' href="http://www.medcommons.net/termsofuse.php" target='_new' class='middled'>Terms of Use</a>
                         </span>
                </p>
                <p class='indent'>
                   <button id='beginUploadButton'  title='Please check the box to confirm patient details match' disabled='true'>Begin Upload</button>
                </p>
               </div>
            </td>
        </tr>
        </tbody>
     </table>
     
    
    <div id='selectImagesHTML' class='hidden'>
    
        <p style='margin-left: 10px;'>Please select images to upload:</p>
    
        <table class='selectImagesTable'>
           <tr>
               <th>Study</th>
               <td class='first' colspan='4'><select id='studies'>
                           <option>Please Select a Source</option>
                       </select>
               </td>
           </tr>
           <tr><th>Series</th>
               <td colspan='2'>
               <select multiple='true' id='series'>
                       <option>Please Select a Source</option>
               </select>
               </td>
           </tr>
           <tr><td>&nbsp;</td><td id='seriesCount' style='font-weight: bold; text-align: center;'>  </td></tr>
      </table> 
    </div>
         
</div>
<?end_section("selectData")?>
      
<?section("voucherDetailsHeading")?>
<?end_section("voucherDetailsHeading")?>

<?section("voucherTableMessage")?>
<?end_section("voucherTableMessage")?>
       
<?section("thankyouMessage")?>    
<?end_section()?>    

<?section("extraScript")?>
<script type='text/javascript'>

// Make the order reference get appended to the cookie
var extraCookieValues = { callersOrderReference: callersOrderReference };


disconnectAll('restartButton', 'onclick');
connect('restartButton', 'onclick', function() { 
    setCookie('upload','');
    window.close();
});


connect(window,'uploadFinished', function() {
    if(window.opener) {
        removeElementClass('restartButtonWrapper','hidden');
    }
});


addLoadEvent(function() {
    
    disconnectAll('beginUploadButton');
    connect('beginUploadButton','onclick',function() {

        var series = getSelectedSeries();

        var up = ['patientDataPadding','modalityRow','scanDateRow','patientIdRow','titleRow'];
        forEach(up, blindUp);
            
        blindUp('confirmBlock');
        
        log("Sending " + series.length + " series");
        startUpload(sourceLocation,{history:$('order_comments').value},'uploaddicom',{ 
            callers_order_reference: callersOrderReference,
            series: series.join(','), 
            groupAccountId: groupAccountId,
            jsonp: 'onUploadDICOMResult'
        });
        
        disable('order_comments','sourcePACS','sourceCD','studies','series','matched','beginUploadButton','selectImages');
        
        showVoucherDetails();
    });
});

function onUploadDICOMResult(result) {
    if(result.status != 'ok') {
        alert('A problem occurred uploading your data: \r\n\r\n' + result.error);
        return;
    }
    tick('patientDataHeading');
    connectUploadEvents();

    removeElementClass('inprogressMessage', 'hidden');
    
    execJSONRequest('update_order_status.php', 
        queryString({callers_order_reference:callersOrderReference, 
                     status:'DDL_ORDER_XMITING', 
                     comments:$('order_comments').value, 
                     desc:'Upload initiated from web form'
            }), 
        function(response) {
            if(response.status != 'ok') {
                alert('A problem occurred updating status on your order: \r\n\r\n' + response.error);
            }
        });
}

function isSaveRestorable(upload) {
    return upload.callersOrderReference == callersOrderReference;
}

function validate(id, match) {
    var src = match ? 'images/greentick.gif' : 'images/redcross.gif';
    $$('#' + id + 'Row img')[0].src = src;
    addElementClass($(id+'Row'), match ? 'matched' : 'mismatch');
    removeElementClass($(id+'Row'), match ? 'mismatch' : 'matched');
    return match;
}

disconnectAll('studies');

disconnectAll(commands,'scanfolderComplete');
connect(commands,'scanfolderComplete',function(result) {

    pingIntervalMs = 4000;
    
    window.result = result;

    $('scanningMsg').innerHTML = '';

    if(result.status != 'ok') {
        alert('There was a problem scanning the folder you selected: \r\n\r\n' + result.error);
        show('browseFilesButton');
        return;
    }
    
    execJSONRequest('/router/ddl?getResult', 'ddlid='+ddlId+'&key='+result.key+'&cmd=scanfolder', function(res) {
        
        if(res.status != 'ok') {
            alert('A problem occurred scanning your files:\n\n'+res.error);
            show('browseFilesButton');
            enable('browseFilesButton');
            return;
        }
        
        result = eval('x='+res.data);
        window.result = result;
        
        studies = [];
        for(var i in result.studies) {
            studies.push(result.studies[i]);
        }
    
        if(studies.length == 0) {
            alert('No DICOM Images were found on this CD.  Please try again.');
            show('browseFilesButton');
            return;
        }
    
        sourceLocation = result.selectedLocation;
        if(!sourceLocation) {
            alert('No source location was selected.  Please try again.');
            show('browseFilesButton');
            return;
        }
        
        transition('fillOutFormStep', 'patientDataBlock', function() {
            // Hack: IE seems to have a weird layout issue
            // Just triggering a reflow fixes it, which
            // seems to happen just by setting this property
            $('dicomSourceHeading').style.height = 'auto';        
        });
        tick('dicomSourceHeading');
        
        var options = map(function(s) {  return OPTION(s.description); }, studies);
        options.splice(0,0,[OPTION('All Images Found')]);
        
        partial(replaceChildNodes,'studies').apply(window,options);
    
        replaceChildNodes('series', OPTION('Please Select a Study'));
    
        connect('studies','onchange', partial(updateSeries, $('studies'), $('series')));
    
        connect($('matched'),'onclick',function() { 
            enableDisable();
            $('beginUploadButton').title = 'Click to Send Image Data';
        });
    
        if($('termsOfUse')) {
            connect('termsOfUse', 'onclick', enableDisable);
        }
        
        signal($('studies'),'onchange');
  });
});

function updateCounts(studySelect, seriesSelect) {
    // Count total of all series
    var total = 0;
    forEach(studies, function(s) { for(var ser in s.series) {total++; }});

    var selCount = 0;
    forEach(seriesSelect.options, function(o) { if(o.selected) selCount++; });
    
    $('seriesCount').innerHTML = selCount + ' of ' + total + ' series selected';

    var studyOption = studySelect.options[studySelect.selectedIndex];
    if(selCount != seriesSelect.options.length) {
        if(!studyOption.innerHTML.match(/- Partial Set$/)) {
            studyOption.innerHTML += ' - Partial Set';
        }   
    }
    else {
        studyOption.innerHTML = studyOption.innerHTML.replace(/- Partial Set$/,"");
    }
}

function updateSeries(studySelect, seriesSelect) {

    if(studySelect.selectedIndex == 0) {
        study = studies[ studySelect.selectedIndex ];
        replaceChildNodes(seriesSelect);
        for(var i = 0; i<studySelect.options.length-1; ++i) {
            populateSeries(studies[i], true);
        }
    }
    else {
        study = studies[studySelect.selectedIndex-1];
        populateSeries(study);
    }

    if($('seriesCount')) 
        updateCounts(studySelect, seriesSelect);
   
    forEach($$('#patientData tr'), function(tr) {removeElementClass(tr,'invisible'); });
    enable('matched');
    if(study.patient) {
        replaceChildNodes('patientName',study.patient.name);
        // Note used to use study.patient.id, but MGH uses name for id
        replaceChildNodes('patientId',study.patient.name);
    }
    replaceChildNodes('patientDateOfBirth',study.patient.dateOfBirth);
    replaceChildNodes('modality',study.modality);
    if(study.date)
        replaceChildNodes('scanDate',formatLocalDateTime(new Date(study.date)));

    var matched = true;
    if(study.patient && study.patient.id) 
        matched &= validate('patientId',(study.patient.id == order.patient_id));
    
    if(study.modality) 
       matched &= validate('modality',(study.modality == order.modality));
    
    if(study.date) {
        var od = isoTimestamp(order.scan_date_time);
        var dd = new Date(study.date);
        var dateMatch = (od.getFullYear() == dd.getFullYear()) && (od.getMonth() == dd.getMonth()) && (od.getDate() == dd.getDate());
        var timeMatch = (od.getHours() == dd.getHours()) && (od.getMinutes() == dd.getMinutes());
       matched &= validate('scanDate', dateMatch && timeMatch);
    }

    hide('scanningMsgRow');
    show('patientDataPadding');

    if(!matched) {
        removeElementClass('mismatchMsg','hidden');
    }
    else {
        addElementClass($('matched').parentNode,'hidden');
        enable('beginUploadButton');
    }
}

function onImagesSelected() {

    $('selectImagesHTML').innerHTML = $('selectImagesDlgContent').innerHTML;

    var studySelect = $$('#selectImagesDlg #studies')[0];
    var studyOption = studySelect.options[studySelect.selectedIndex];
    $('selectionDescription').innerHTML = studyOption.innerHTML + ' (' +$('seriesCount').innerHTML.replace(' selected','') + ')';

    dlg.destroy();
    enable('selectImages');
}

connect('selectImages', 'onclick', function() {
    disable('selectImages');
    addElementClass(document.body, 'yui-skin-sam');
    yuiLoader().insert(function() {
        window.dlg = new YAHOO.widget.SimpleDialog('selectImagesDlg', { 
            width: '500px',
            fixedcenter:true,
            modal:true,
            visible:true,
            draggable:true,
            buttons: [ {text: 'OK', handler: onImagesSelected}, 
                       { text: 'Cancel', handler: function(){dlg.destroy();}} ]
        });
        dlg.setHeader('Select Images to Upload');
        dlg.setBody('<div id="selectImagesDlgContent">'+$('selectImagesHTML').innerHTML+'</div>');
        dlg.render(document.body);


        var studiesEl = $$('#selectImagesDlg #studies')[0];
        var seriesEl = $$('#selectImagesDlg #series')[0];
        
        connect(studiesEl,'onchange', partial(updateSeries, studiesEl, seriesEl));
        connect(seriesEl, 'onchange', partial(updateCounts, studiesEl, seriesEl));
    });
});

function enableDisable() {
    var enabled = $('matched').checked;
    if($('termsOfUse') && !$('termsOfUse').checked)
        enabled = false;
            
    if(enabled)
        enable('beginUploadButton'); 
    else
        disable('beginUploadButton'); 
}


// No need to force printing on this page, so pretend the user already did it
printed = true;
</script>
<script type='text/javascript' src='/yui/2.7.0/yuiloader/yuiloader-min.js'></script>
<?end_section("extraScript");?>
