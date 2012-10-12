<?
require_once "settings.php";
global $Secure_Url;
$baseUrl = str_replace("https://","http://",$Secure_Url);
?>
<style>
.outer {border: 0; width:95%;  height:250px;font-family: Tahoma;}
.mcgadget {margin:0px; padding:0px; padding-top: 0px;}
.new_win {font-size: .7em;}
.healthurl {border:0}
.oneline {font-size: 0.6em;padding-top:1em; padding-bottom:1em;}
h2 {background-color:gray; color:white; font-size:.8em;margin:0 ;}
a img {border-style: none;}
html, body { margin: 0px;  padding: 0px;  border: 0; }
table tr td { font-size: 10px; }
h4 { font-size: 12px; }

/* Used by activity gadget */
#updatesTable {
  margin-top: 5px;
}
#updatesTable table {
  width: 85%;
  padding: 0px;
  margin: 0px;
}
p#links {
  font-size: 12px;
  color: gray;
  margin: 0px 0px;
}
#updatesTable table tr th {
  text-align: left;
  font-weight: bold;
  color: #444;
  font-size: 10px;
}

</style>
<script type="text/javascript">
function viewportSize() {
    if (typeof window.innerWidth != 'undefined') {
         w = window.innerWidth,
         h = window.innerHeight
    }
    // IE6 in standards compliant mode (i.e. with a valid doctype as the first line in the document)
    else if (typeof document.documentElement != 'undefined'
        && typeof document.documentElement.clientWidth !=
        'undefined' && document.documentElement.clientWidth != 0) {
          w = document.documentElement.clientWidth,
          h = document.documentElement.clientHeight
    }
    // older versions of IE
    else {
      w = document.getElementsByTagName('body')[0].clientWidth,
      h = document.getElementsByTagName('body')[0].clientHeight
    }
    return { w: w, h: h };
}
var baseUrl = "<?=$baseUrl?>/gadgets/yplain.php?nocache=0";
var params = {};  
params[gadgets.io.RequestParameters.AUTHORIZATION] = gadgets.io.AuthorizationType.SIGNED;
params[gadgets.io.RequestParameters.CONTENT_TYPE] = gadgets.io.ContentType.TEXT;  
params[gadgets.io.RequestParameters.METHOD] = gadgets.io.MethodType.GET;     
params["OAUTH_SERVICE_NAME"] = "HMAC";  //This is the critical part which forces the use of HMAC    
function extractGroupName() {
    var t = document.getElementById('gadgetTitle');
    if(t) {
        gadgets.window.setTitle(t.innerHTML);
        t.parentNode.removeChild(t);
    }
}

function loadContent() {
    gadgets.io.makeRequest(baseUrl, function(response) {
        document.getElementById('content').innerHTML = response.text;
        extractGroupName();
        if(viewportSize().w < 220) { 
            var remove = [];
            var tds = document.getElementsByTagName('td');
            for(var i=0; i<tds.length; ++i) {
                if(tds[i].className == 'tracking')
                    remove.push(tds[i]);
            }
            var ths = document.getElementsByTagName('th');
            for(var i=0; i<ths.length; ++i) {
                if(ths[i].className == 'tracking')
                    remove.push(ths[i]);
            }
            for(var i=0; i<remove.length; ++i) {
                remove[i].parentNode.removeChild(remove[i]);
            }
        }
    }, params);
}
function updateSearch() {
    var url = baseUrl + '&searchPatientName=' + encodeURIComponent(document.getElementById('searchPatientName').value);
    gadgets.io.makeRequest(url, function(response) {
        document.getElementById('content').innerHTML = response.text;
        extractGroupName();
        var search = document.getElementById('searchPatientName');
        search.select();
        search.focus();
    },params);
    return false;
}
gadgets.util.registerOnLoadHandler(loadContent);
</script>
<div id='content'>  
<i>Loading ...</i>
</div>