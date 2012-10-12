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
       
