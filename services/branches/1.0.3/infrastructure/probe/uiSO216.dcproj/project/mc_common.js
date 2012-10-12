    //common routines for mobile stuff, lifted from base.js - can probably be trimmed further
    
    function $(id) {
      return document.getElementById(id);
    }
    function el(id) {
      return document.getElementById(id);
    }
      function setvalue(id,innerhtml)
    {
    	if ($( id ))
    	$( id ).innerHTML =innerhtml;
    }
    /* shows a particlur div */
    function showinline(id)
    {
    	if ($( id ))
    	$( id ).style.display = 'inline';
    }
    function showblock(id)
    {
    	if ($( id ))
    	$( id ).style.display = 'block';
    }
    function dontshow(id)
    {
    	if ($( id ))
    	$( id ).style.display = 'none';
    }
        function toggle(id)
    {
    	if ($( id ))
    	{
    	if  ($( id ).style.display == 'none')
    	$( id ).style.display = 'block'; else
    	$( id ).style.display = 'none';
    	}
    	return true;
    		
    }
    /**
    * gets the value of the specified cookie.
    */
    function getCookie(name)
    {
    	var dc = document.cookie;
    	var prefix = name + "=";
    	var begin = dc.indexOf("; " + prefix);
    	if (begin == -1) {
    		begin = dc.indexOf(prefix);
    		if (begin != 0) return null;
    	}
    	else {
    		begin += 2;
    	}
    	var end = document.cookie.indexOf(";", begin);
    	if (end == -1) {
    		end = dc.length;
    	}
    	return unescape(dc.substring(begin + prefix.length, end));
    }

    // This function is included to overcome a bug in Netscape's implementation
    // of the escape () function:

    function myunescape (str)
    {
    	str = '' + str;
    	while (true)
    	{
    		var i = str . indexOf ('+');
    		if (i < 0)
    		break;
    		str = str . substring (0, i) + ' ' + str . substring (i + 1, str . length);
    	}
    	return unescape (str);
    }

    // This function creates the args [] array and populates it with data
    // found in the URL's search string:

    var args;

    function args_init ()
    {
    	args = new Array ();
    	var argstring = window . location . search;
    	if (argstring . charAt (0) != '?')
    	return;
    	argstring = argstring . substring (1, argstring . length);
    	var argarray = argstring . split ('&');
    	var i;
    	var singlearg;
    	for (i = 0; i < argarray . length; ++ i)
    	{
    		singlearg = argarray [i] . split ('=');
    		if (singlearg . length != 2)
    		continue;
    		var key = myunescape (singlearg [0]);
    		var value = myunescape (singlearg [1]);
    		args [key] = value;
    	}
    }

    function get_mc_attribute(att) {
      var mc = getCookie('mc');
      if(!mc)
        return null;
      var atts = mc.split(',');
      for(var i=0; i<atts.length; ++i) { 
        if(atts[i].match(new RegExp('^'+att+'='))) {
          return atts[i].split('=')[1];
        }
      }
      return null;
    }
    //
    // processes error arguments and places them in fields
    //
    
    function wsinit()
    {
	   	args_init();

    	for (ix = 0; ix<10 ; ++ ix)
    	{
    		if (args['e'+ix])
    	
    		if (args['p'+ix])
    		{
    			p = args['p'+ix];
    			$(p).innerHTML = '** ' + args['e'+ix] + '**';
    		}
    		
    	}
   
      }// end of wsinit()
       
function post_upstream( url,op,names ) {

var onloadHandler = function() { 	

if (navigator && navigator.notification) navigator.notification.loadingStop(); 
if (navigator && navigator.notification) debug.log ("Finished Request to MedCommons url "+url+"op "+op+" bytes:" +names.length);

//alert ("MedCommons has processed your request");
 };	// The function to call when loaded;

// XMLHttpRequest setup code for upstream post
g_startTime = (new Date()).valueOf();
var params = "time="+g_startTime+"&op="+op+"&data="+names;

var xmlRequest = new XMLHttpRequest();
xmlRequest.onload = onloadHandler;
xmlRequest.open("POST", url,true);  // different for the prober
//Send the proper header information along with the request
xmlRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
//xmlRequest.setRequestHeader("Content-length", params.length);
//xmlRequest.setRequestHeader("Connection", "close");
xmlRequest.send(params);
//show the loadingscreen for a few seconds
showLoadingScreen(2);
} 

function test_post_upstream()
{
post_upstream (mc_rest('ws/pushdata.php'),'testcase','testing data goes here');
}
/*
    function watchAccel() {
      debug.log("watchAccel");
      var suc = function(a){
        document.getElementById('x').innerHTML = roundNumber(a.x);
        document.getElementById('y').innerHTML = roundNumber(a.y);
        document.getElementById('z').innerHTML = roundNumber(a.z);
      };
      var fail = function(){};
      var opt = {};
      opt.frequency = 100;
      timer = navigator.accelerometer.watchAcceleration(suc,fail,opt);
    }
      
    function roundNumber(num) {
      var dec = 3;
      var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
      return result;
    }

    function preventBehavior(e) { 
      e.preventDefault(); 
    };

    PhoneGap.addConstructor(function(){
      document.addEventListener("touchmove", preventBehavior, false);
      deviceInfo();
      document.addEventListener('orientationChanged', function(e) { debug.log("Orientation changed to " + e.orientation); }, false);
    });
    */
 
    function showLoadingScreen(durationInSeconds){
		//if (!durationInSeconds) {
			//durationInSeconds = prompt("Enter the load duration in seconds", 3);
		//}

		if (durationInSeconds) {
			options = { 'duration': durationInSeconds };
                 if (navigator&&navigator.notification) 
			navigator.notification.loadingStart(options);
		} else {
			return;
		}
	}   
    
