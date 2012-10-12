var
rotate = function(a /*array*/, p /* integer, positive integer rotate to the right, negative to the left... */){ //v1.0
    for(var l = a.length, p = (Math.abs(p) >= l && (p %= l), p < 0 && (p += l), p), i, x; p; p = (Math.ceil(l / p) - 1) * p - l + (l = p))
        for(i = l; i > p; x = a[--i], a[i] = a[i - p], a[i - p] = x);
    return a;
};
   var g_startTime = 0;
   var iteration = 0;
   var responsetimes = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]; // 40

   var servicetimes = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]; // 40
      
   var clienttimes = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]; // 40
     
   var loadavgs = [0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0]; // 40

function xmlLoaded(req) {
// put a standard set of stuff around 
 // alert("got called back state "+req.readyState+" status"+ req.status);
  if (req.readyState == 4 && req.status == 200 ) 
  {
      var buildStart = (new Date()).valueOf();
     var responsetime = (buildStart - g_startTime );
     // set the basic color scheme back
     	document.getElementById("proberpanel").style.color= 'black';
     
     
     
    rotate (responsetimes,-1);// slide left
   
    
   responsetimes [responsetimes.length-1] = responsetime;

   
    if (document.getElementById('downloadSize'))    
     document.getElementById('downloadSize').innerHTML = req.responseText.length ;


	iteration++;
	if (document.getElementById('iteration'))
	document.getElementById('iteration').innerHTML = iteration;
 
 
  if (document.getElementById('downloadt')){     
    document.getElementById('downloadt').innerHTML =  responsetime +'ms';
    if (  $('.downloadTime') )  //see whats up
    $('.downloadTime'). sparkline(responsetimes, {type: 'bar', barColor: 'blue'} );
  }
   	// everything else comes from JSON
   	
	JSONData = req.responseText;
	t=JSONData.parseJSON();  
	
   handleJsonResponse(t); //side effects
    
    if (document.getElementById('service'))
    document.getElementById('service').innerHTML = t.service;
       if ( document.getElementById('serverName'))
	document.getElementById('serverName').innerHTML = t.servername;
   if ( document.getElementById('serverTime'))
	document.getElementById('serverTime').innerHTML = t.servertime;
	if ( document.getElementById('ipaddr'))
	document.getElementById('ipaddr').innerHTML = t.ipaddr;
	
	
		if ( document.getElementById('loadavg'))
		{
	rotate (loadavgs,-1); // slide everything left
  	loadavgs [loadavgs.length-1] = t.loadavg ;
  	document.getElementById('loadavg').innerHTML = t.loadavg;
    if ($('.loadAvg'))
    $('.loadAvg').sparkline(loadavgs, {type: 'bar', barColor: 'black'} );
    }
	
  	if( 	document.getElementById('servicet')){
  	rotate (servicetimes,-1); // slide everything left
  	servicetimes [servicetimes.length-1] = t.servicetime;
  	
  	document.getElementById('servicet').innerHTML = t.servicetime + 'ms';
    if  ($('.serviceTime'))
    $('.serviceTime').sparkline(servicetimes, {type: 'bar', barColor: 'red'} );
  }
          
	//
	//elapsed client time
	//
	if ( document.getElementById('buildt')){
	 var elapsed = ( (new Date()).valueOf() - buildStart );
	  rotate (clienttimes,-1); // slide everything left
	 clienttimes [clienttimes.length-1] = elapsed;
     document.getElementById('buildt').innerHTML =  elapsed +'ms';
     if ( $('.buildTime'))
     $('.buildTime').sparkline(clienttimes, {type: 'bar', barColor: 'green'});
     }
    
  }
 else {
 		document.getElementById("proberpanel").style.color= 'red';
		
	}
	
return true; //
}
var params; // arglist for post is built only once at startup

function loadPerf( url ) {
var onloadHandler = function(event ) {

 xmlLoaded(xmlRequest);
 if (perfMachineRefreshID ==0 ) flipToPerf(event); // showing nothing must bring it up

 };	// The function to call when the feed is loaded;

// XMLHttpRequest setup code

g_startTime = (new Date()).valueOf();
var xmlRequest = new XMLHttpRequest();
xmlRequest.onload = onloadHandler;
xmlRequest.open("POST", url,true);  // different for the prober
//Send the proper header information along with the request
xmlRequest.setRequestHeader("Content-type", "application/x-www-form-urlencoded");
//xmlRequest.setRequestHeader("Content-length", params.length);
//xmlRequest.setRequestHeader("Connection", "close");
xmlRequest.send(params);
}

function perf_machine (url,secs,pow2)
	{
	  var bytes = '1'; //only once
      for ( i=0; i<pow2; i++)     bytes = bytes+bytes; // build a big block
       params = "lorem=ipsum&data=" + bytes; // set as args to post

	// this is where the ajax call goes to get JSON
	 perfMachineRefreshID= setInterval(function(){ loadPerf( url ) }, 1000*secs); // every 10 seconds?
	 
	   loadPerf( url ) //*Call once to get started */
       return  perfMachineRefreshID;
	} // the stuff down below is specific to dashcode on the iphone





function handleJsonResponse(stuff)
{
   
 	var t = stuff.data;
 	
 	//alert ("Dispatch");
//	if (stuff.service != 'mc_appliance_probe') alert ("MC_Appliance_Probe: Bad service type "+stuff.service);
 	
 	
 		if ( document.getElementById('disktotalspace'))
     document.getElementById('disktotalspace').innerHTML = stuff.disktotalspace+'GB';
     
 		if ( document.getElementById('diskfreespace'))
  	 document.getElementById('diskfreespace').innerHTML = stuff.diskfreespace +'GB';
  	 
  	 if(t) 

     for( var i = 0; i < t.length; i++ )
     {
     
     if (t[i].table=='document') if (document.getElementById('document_count') )
                   document.getElementById('document_count').innerHTML = t[i].count;
                   
     if (t[i].table=='groupinstances') if (document.getElementById('group_count') )
                   document.getElementById('group_count').innerHTML = t[i].count;
                   
                   
     if (t[i].table=='users') if (document.getElementById('user_count') )
                   document.getElementById('user_count').innerHTML = t[i].count;
                   
                   
     if (t[i].table=='modcoupons') if (document.getElementById('transaction_count') )
                   document.getElementById('transaction_count').innerHTML = t[i].count;
     }
     
}   

// The following block implements the string.parseJSON method
(function (s) {
  // This prototype has been released into the Public Domain, 2007-03-20
  // Original Authorship: Douglas Crockford
  // Originating Website: http://www.JSON.org
  // Originating URL    : http://www.JSON.org/JSON.js

  // Augment String.prototype. We do this in an immediate anonymous function to
  // avoid defining global variables.

  // m is a table of character substitutions.

  var m = {
    '\b': '\\b',
    '\t': '\\t',
    '\n': '\\n',
    '\f': '\\f',
    '\r': '\\r',
    '"' : '\\"',
    '\\': '\\\\'
  };

  s.parseJSON = function (filter) {

    // Parsing happens in three stages. In the first stage, we run the text against
    // a regular expression which looks for non-JSON characters. We are especially
    // concerned with '()' and 'new' because they can cause invocation, and '='
    // because it can cause mutation. But just to be safe, we will reject all
    // unexpected characters.

    try {
      if (/^("(\\.|[^"\\\n\r])*?"|[,:{}\[\]0-9.\-+Eaeflnr-u \n\r\t])+?$/.
        test(this)) {

          // In the second stage we use the eval function to compile the text into a
          // JavaScript structure. The '{' operator is subject to a syntactic ambiguity
          // in JavaScript: it can begin a block or an object literal. We wrap the text
          // in parens to eliminate the ambiguity.

          var j = eval('(' + this + ')');

          // In the optional third stage, we recursively walk the new structure, passing
          // each name/value pair to a filter function for possible transformation.

          if (typeof filter === 'function') {

            function walk(k, v) {
              if (v && typeof v === 'object') {
                for (var i in v) {
                  if (v.hasOwnProperty(i)) {
                    v[i] = walk(i, v[i]);
                  }
                }
              }
              return filter(k, v);
            }

            j = walk('', j);
          }
          return j;
        }
      } catch (e) {

      // Fall through if the regexp test fails.

      }
      throw new SyntaxError("parseJSON");
    };
  }
) (String.prototype);
// End public domain parseJSON block  