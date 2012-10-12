<?
    require_once "template.inc.php";
    require_once "alib.inc.php";
    
    if(isset($template)) {
        $template->extend("base.tpl.php");
    }
    else {
        echo template("ddl.php")->set("title","DICOM Service Control")->fetch();        
        exit;
    }
?>
<?block("head")?>
<style type="text/css">
#ddlOptionPane {
  xbackground-color: #F2F0E5;
  xborder: 1px solid #553;
  padding: 1em 3em 1em 1em;
  margin-left: 0.8em;
  float: left;
}
.middled * {
    vertical-align: middle;
}
.hidden {
    display: none;
}
#pollers {
    clear: both;
}
#pollers table {
  margin-left: 0.8em;
  border-collapse: collapse;
  width: 400px;
}

#pollers table td {
 text-align: center;
}

#pollers table td, 
#pollers table th {
  border: solid 1px #444;
  padding: 5px;
  background-color: #F2F0E5;
}

</style>
<?end_block("head")?>

<?block("content")?>
<h2>DDL Control Page</h2>

<p>This page allows you to control functions of a DICOM Service (DDL) that is running
on your computer.</p>

<div id='detecting'>
    <p class='middled'><img src='/yui/2.6.0/assets/skins/sam/loading.gif'/> Please wait while 
           we detect your current system setup!</p>
</div>

<div id='noddl' class='hidden'>
	<p>No DDL was detected running on your computer.</p>
	<p><button id='startDDLButton'>Start DDL</button></p>
</div>

<div id='ddlOptionPane' class='hidden'>
    <button id='stopDDL'>Shutdown DDL</button>
    <button id='showStatus'>Show Status Page</button>
    <button id='showConfiguration'>Show Configuration</button>
</div>

<div id='pollers'>

</div>

<?end_block("content")?>

<?block("endjs")?>
<?
if(isset($GLOBALS['DEBUG_JS'])) {
  echo "<script type='text/javascript' src='MochiKitDebug.js'></script>";
  echo "<script type='text/javascript' src='sha1.js'></script>";
  echo "<script type='text/javascript' src='utils.js'></script>";
  echo "<script type='text/javascript' src='ajlib.js'></script>";
  echo "<script type='text/javascript' src='contextManager.js'></script>";
}
else
  echo "<script type='text/javascript' src='acct_all.js'></script>";
?>

<script type='text/javascript'>
connect(ddlEvents, 'ddlStopped', function() {
    hide('detecting');
    appearX('noddl');
});
connect(ddlEvents, 'pingTimeout', function() {
    hide('detecting');
    appearX('noddl');
});

connect(ddlEvents, 'pong', function(result) {
    hide('detecting','noddl');
    appearX('ddlOptionPane');

    if(result.pollers) {
        var tbody;
        appendChildNodes('pollers',H3('Pollers'),
                TABLE({}, tbody=TBODY({}, 
                        TR({},TH('Group'),TH('Host'),TH('')))));

        // Note - we behave here as if multiple pollers are supported,
        // but in reality DDL only supports a single poller
        forEach(result.pollers, function(p) {
            var stop,pause;
            appendChildNodes(tbody,
                    TR({}, TD(p.groupName), TD(p.cxpHost), TD({}, pause=BUTTON('Pause'), stop=BUTTON('Stop'))));
            connect(pause,'onclick', function() {
                pause.disabled = true;
                sendCommand('stoppoller', {groupName:p.groupName, jsonp: 'stoppedPolling'});
            });
            connect(stop,'onclick', function() {
                stop.disabled = true;
                sendCommand('stoppoller', {groupName:p.groupName, permanent:'true', jsonp: 'stoppedPolling'});
            });
        });
    }
    disconnectAll(ddlEvents,'pong');
});

function stoppedPolling(result) {
    if(result.status == 'ok') {
	    alert('Poller is shutting down');
	    window.location.reload();
    }
    else
        alert('There was a problem shutting down your group poller:\n\n'+result.error);
}

connect('stopDDL','onclick',function() {
    if(!confirm('Are you sure you want to stop your DDL?\n\nIf you click OK your DDL will shut down and your page will refresh'))
        return;
        
    disable('stopDDL','showStatus', 'showConfiguration');
    sendCommand('shutdown',{jsonp: 'shuttingDown'});
});

connect('startDDLButton', 'onclick', function() {
    var ddlStartUrl = '<?=allocate_gateway(null)?>/ddl/start';
    appendChildNodes(document.body, createDOM('IFRAME',{src: ddlStartUrl, style:'display:none;'}));
    replaceChildNodes('noddl', P({},IMG({style:'vertical-align: middle;',src:'/yui/2.6.0/assets/skins/sam/loading.gif'}),
            SPAN({style:'vertical-align: middle;'},' Waiting for DDL to start')));
});

connect('showStatus', 'onclick', function() {
   window.open('http://localhost:16092/localDDL/status.html');
});
connect('showConfiguration', 'onclick', function() {
   window.open('http://localhost:16092/localDDL/configure.html');
});

connect(ddlEvents, 'ddlStopped', function() {
   window.location.reload(); 
});

function shuttingDown(result) {
    
}

pingDDL();
</script>
<?end_block("endjs")?>
