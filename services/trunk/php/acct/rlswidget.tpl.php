<?
/**
 * Main widget page - renders outer HTML and BODY for the patient list / worklist.
 * The table body and data itself is rendered by a sub-template (rlstable.tpl.php) 
 * and is updated repeatedly by an ajax call.
 */
$sd = isset($GLOBALS['Script_Domain']) ? $GLOBALS['Script_Domain'] : "";

if(isset($GLOBALS['DEBUG_JS'])) {
  echo "<script type='text/javascript' src='ajlib.js'></script>";
  echo "<script type='text/javascript' src='contextManager.js'></script>";
}
?>
      <script type="text/javascript">
        var practiceId = '<?=$pid;?>';
        RLSParams = { pid: '<?=$pid;?>', page: 1 };

        function start() {
          if(window.parent && window.parent.setMainSectionHeading) {
            window.parent.setMainSectionHeading('<?=htmlentities($practicename, ENT_QUOTES)?>');
          }
          
          // Used by tests 
          var rlsInterval = 130000;
          if(getCookie('rlsinterval'))
            rlsInterval = parseInt(getCookie('rlsinterval',10));
          setInterval(refreshRLS, rlsInterval);
        }
        function calculateHeight() {
          if(el('acdiv').style.display == 'none') {
            return _calculateHeight();
          }
          else
            return (elementDimensions(el('acdiv')).h+elementDimensions($('records')).h);
        }
        addLoadEvent(start);
        
        addLoadEvent(function(){window.setTimeout(addHeightMonitor,300);window.setTimeout(function() {bc_last_height = 0; broadCastHeight();},1000);});
        
        function onDDLDetected() {
          if(!$('ctxf1'))
            appendChildNodes(document.body, createDOM('iframe',{'id':'ctxf1','name':'contextFrame', style:'display: none;'})); 

          if($('newDICOMLink')) {
	          var newDicomHref="http://localhost:16092/CommandServlet?command=upload";
	          $('newDICOMLink').href=newDicomHref;
	          $('newDICOMLink').target="contextFrame";
          }
          displayGroupMessages();
          startWatchingDDL("<?=$gwUrl?>");
          window.setTimeout(ddlCheck,5000);
        }
        function startedDownload(result) {
          if(result.status != 'ok') {
            alert('A problem occurred starting your download:\r\n\r\n' + result.error);
          }
        }
      </script>
      <style type="text/css">
        .hidden {
          display: none;
        }
        #records {
          padding-left: 2px;
        }
        #buttons a {
          float: right;
        }
        #roirForm {
          display: none;
        }
        input#roirid {
          width: 6em;
        }
        #worklistButtons * {
         vertical-align: middle;
          font-size: 11px;
        }
        #worklistButtons img {
          cursor: pointer;
        }

        #messagesWrapper h3 {
          background-color: #ebe5c8  ;
          padding: 2px 5px;
          color: #554f3d;
        }

        #messagesWrapper h3 a {
          position: absolute;
          color: #554f3d;
          right: 10px; 
        }
        #messagesWrapper h3 a:hover, 
        #messagesWrapper h3 a:active {
          color: red;
        }

        #messages {
            padding: 0px 0px;
            margin-top: 0px;
        }

        #messagesWrapper {
          position: relative;
          background-color: white;
          margin-right: 29px;
          margin-left: 1px;
          xborder: solid 2px #c5c0a1;
        }

        #messagesWrapper table th,
        #messagesWrapper table td {
          padding-left: 5px;
        }
        
        .cornerDot {
	        position: absolute;
	        background-color: white;
	        width: 1px;
	        height: 1px;
	        top: 0px;
        }
        
        #messagesWrapper table {
	        width: 100%;
            border-left: solid 2px #ebe5c8  ;
            border-bottom: solid 2px #ebe5c8  ;
            border-right: solid 2px #ebe5c8  ;
        }

        #messagesWrapper table td img {
          padding-right: 6px;
        }

        #messagesWrapper table th {
            text-align: left;
        }
        
        #messageTableWrapper {
            max-height: 300px;
            overflow-x: hidden;
            overflow-y: auto;
        }
        
        #messagesWrapper table .msgicon {
            padding-top: 2px;
            width: 19px;
        }
        #messagesWrapper table .msgcat {
            width: 5em;
        }
 
        #transferTip p {
            line-height: 14px;
        }

        #transferTip {
            display: none;
            position: absolute;
            border: solid 1px #bfb695;
            padding: 4px;
            font-size: 10px;
            background-color: #f3f6e5;
        }
        
        td.iconCell a img {
          position: relative;
          top: -3px;
        }
        td.statusCell {
           white-space: nowrap; 
           text-align: left !important;
        }
        
        table#registryTable tr td {
            font-size: 12px;
        }
        
        table#registryTable tbody tr:hover {
            background-color: #f7f6f0;
        }
        table#registryTable tbody tr.selected {
            background-color: #eae9e0;
        }
        table#registryTable tbody tr.detailsRow {
            background-color: #faf9f5;
        }
        
        /*
        table#registryTable td ,
        table#registryTable th, #messageTableWrapper {
            background-color: #fdfcf1;
        }
        table#registryTable tr.oddnew td ,
        table#registryTable tr.odd td {
            background-color: #f7f6f0;
        }
        */
        
        
        table#registryTable .leftCell {
            border-left: solid 2px #ccb;
            padding: 2px 5px;
        }
        table#registryTable th.leftCell {
            padding: 5px 0px 6px 5px;
            border-left: solid 2px #ccb;
        }
        table#registryTable tbody td.rightCell {
            border-right: solid 2px #ccb;
        }
        table#registryTable tr.searchRow th.leftCell {
            xborder-left: solid 2px #ccb;
        }
        table#registryTable tr.searchRow th.rightCell {
            border-right: solid 2px #ccb;
            margin-right: 3px;
        }
        input#searchPatientName {
            margin-left: 2%;
        }
        table thead tr {
	        border-left: solid 2px #958F6D;
	        border-right: solid 2px #958F6D;
        }
        table thead th.tableRight {
        }
        
        <? include "voucher_css.inc.php"; ?>

      </style>
    <div id="records">
    <script type="text/javascript">
      var isIE = false;
      statusValues = '<?=addSlashes($statusValues)?>'.split(',');
      addLoadEvent(function() {
        forEach($$('.searchRow input'), function(inp) {
          connect(inp,'onkeyup',updateSearch);
        });
        connect($('searchLastUpdate'), 'onchange', updateSearch);
      });
      // addLoadEvent(displayGroupMessages);
    </script>
    <h2>Dashboard - <?=htmlentities($practicename)?></h2>
    <table id='registryTable' class="liveTable stdTable" width="100%">
      <thead>
      <tr id='registryTableHeaderRow' class='roundedHeader'>
        <th title='Patient Name' class='tableLeft'><div class='registryTableHeader' style='position: relative;'>
	        <div class='cornerDot' style='left: -2px; top: 0px;'></div>
          <h5>Patient</h5></th>
        <th title='Time Since Last Update' valign='top'><div class='registryTableHeader nowrap'><h5>Last Update</h5></div></th>
        <th><div class='registryTableHeader'><h5>Purpose</h5></div></th>
        <th width='105px'><div class='registryTableHeader'><h5>Status</h5></div></th>
        <th><div class='registryTableHeader'><h5>&nbsp;</h5></div></th>
        <th class='tableRight'><div class='registryTableHeader'>
          <div style='position: relative;'>
	        <div class='cornerDot' style='right: -2px; top: -1px;'></div>
	      </div>
          
          <h5>&nbsp;</h5>
          </th>
      </tr>
      <tr class="searchRow">
        <th class='leftCell'><input id="searchPatientName" name="searchPatientName" type="text" value="<?=hsc($searchPatientName)?>"/></th>
        <th><select id="searchLastUpdate" name="searchLastUpdate">
          <option value='all'>All</option>
          <option value='week'>Last Week</option>
          <option value='month'>Last Month</option>
          <option value='year'>Last Year</option>
          </select>
        </th>
        <th><input name="searchPurpose" type="text"/></th>
        <th><input name="searchStatus" type="text"/></th>
        <th>&nbsp;<a class='deleteLink' style='top: 0px;' title='Clear search terms' href='javascript:clearRLSSearch();'>X</a></th>
        <th class='rightCell'>&nbsp;</th>
      </tr>
      </thead>
      <tbody id='rlsRows'>
      <?=$content?>
      </tbody>
      </table>
    </div>
    <?if(!$embed):?>
    <div id='messagesWrapper' style='display: none;'>
        <div id='messages'>
        </div>
    </div>
    <?endif;?>
    <div id='rlsParser' class='invisible'>&nbsp;</div>
    <div id='acdiv' ></div>
    <div id='transferTip'></div>
    <img src='images/tinyinfo.png' style='display: none;'/>
  <form id='roirForm' method='post' action='/mod/vouchersetup.php'>
    <input type='hidden' name='roirId' value=''/>
    <input type='hidden' name='svcnum' value=''/>
    <input type='hidden' name='servicename' value=''/>
    <input type='hidden' name='patientname' value=''/>
    <input type='hidden' name='patientemail' value=''/>
    <input type='hidden' name='patientnote' value=''/>
  </form>
  <div id='ddlEvents'></div>
  <script type='text/javascript'>
  addLoadEvent(function() {
    yuiLoader();
    if(typeof get_mc_attribute != 'undefined') {
      var orig_accid = get_mc_attribute('mcid'); 
      window.setInterval(function() {
         if(get_mc_attribute('mcid') != orig_accid)
             window.location.href = window.location.href;
      },4000);
    }
  });
  var VoucherState = {
    patientname: '',
    'status': '',
    standalone: true
  };
  addRemoteMessages(<?=$globalMessages?>);
  window.messageTimestamp = <?=$messageTimestamp?>;
  </script>
  <script type="text/javascript" src="/zip/yui/2.8.0r4/yahoo-dom-event/yahoo-dom-event.js,yui/2.8.0r4/dragdrop/dragdrop-min.js,yui/2.8.0r4/container/container-min.js"></script>
  <link media="all" href="/zip/yui/2.8.0r4/container/assets/skins/sam/container.css" type="text/css" rel="stylesheet" />
