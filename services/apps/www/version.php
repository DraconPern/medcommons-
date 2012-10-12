<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="author" content="MedCommons, Inc." />
    <meta name="description" content="MedCommons Website Version 1.0 Page $$$htmltitle$$$" />
    <meta name="keywords" 
    content="medcommons, personal health records,ccr, phr, privacy, patient, health, records, medical records,
						emergencyccr"/>
    <meta name="robots" content="all"/>
    <meta name="viewport" content="width=320" />
<title></title>
   
<link rel="shortcut icon" href="../images/favicon.gif" type="image/gif" />
    <link media="all" href="../css/medCommonsStyles.css" type="text/css" rel="stylesheet" />
    <script type="text/javascript">
    function el(id) {
      return document.getElementById(id);
    }
    function popuploginpage(page) {
    	window.open(  page, "IdentityWindow", "status = 1, height = 400, width = 750, resizable = 0" );
    	return false;
    }
      function setvalue(id,innerhtml)
    {
    	if (el( id ))
    	el( id ).innerHTML =innerhtml;
    }
    /* shows a particlur div */
    function showinline(id)
    {
    	if (el( id ))
    	el( id ).style.display = 'inline';
    }
    function showblock(id)
    {
    	if (el( id ))
    	el( id ).style.display = 'block';
    }
    function dontshow(id)
    {
    	if (el( id ))
    	el( id ).style.display = 'none';
    }
        function toggle(id)
    {
    	if (el( id ))
    	{
    	if  (el( id ).style.display == 'none')
    	el( id ).style.display = 'block'; else
    	el( id ).style.display = 'none';
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

    var signed_on; 

    var top_nav = [
      { text: 'Help', href: '/help.php' },
      { text: 'Dashboard', href: '/acct/home.php' },
      { text: 'Services', href: '/mod/svcsetup.php', hidden: true },
      { text: 'Vouchers', href: '/mod/voucherlist.php', hidden: true },
      { text: 'Settings', href: '/acct/settings.php' },
      { text: 'Sign In', href: '/site/personal.php', hidden: true },
      { text: 'Logout', href: '/acct/logout.php' }
    ];

    function render_top_nav() {
      var h = '';
      for(var i=0; i<top_nav.length; ++i) {
        var item = top_nav[i];
        if(item.hidden)
          continue;
        if(i!=0)
          h += '&nbsp;|&nbsp; '; // trailing space helps wrapping
        if(item.disabled)
          h += '<li class="light">'+item.text+'</li>';
        else 
          h += '<li><a class="menu_'+item.text.toLowerCase().replace(/ /g,'')+'" href="'+item.href+'">'+item.text+'</a></li>';
      }
      el('navcontainer').innerHTML=('<ul id="navlist" class="listinlinetiny">' + h +'</ul>');
    }

    function nav(txt) {
      for(var i=0; i<top_nav.length; ++i) {
        if(top_nav[i].text == txt)
          return top_nav[i];
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

    function wsinit()
    {
    	args_init();
    	if (getCookie('mc'))	{
    		signed_on = true;
    		showblock('loggedon');
    		dontshow('notloggedon');
    		showinline('visi');
        nav('Logout').hidden = false;
        nav('Dashboard').hidden = false;
        nav('Settings').hidden = false;
        nav('Sign In').hidden = true;
        var s = get_mc_attribute('s');
        if((s & 2) > 0) {
          nav('Vouchers').hidden = false;
          nav('Services').hidden = false;
        }
        if((s & 4) > 0) {
          nav('Settings').hidden = true;
        }
    	}
    	else
    	{
    		signed_on = false;
    		showblock('notloggedon');
    		dontshow('loggedon');
    		dontshow('visi');
        nav('Sign In').hidden = false;
        for(i in menu=['Dashboard','Logout','Settings','Vouchers', 'Services']) {
          nav(menu[i]).hidden = true;
        }
    	}
     render_top_nav();

    	for (ix = 0; ix<10 ; ++ ix)
    	{
    		if (args['e'+ix])
    	
    		if (args['p'+ix])
    		{
    			p = args['p'+ix];
    			el(p).innerHTML = '** ' + args['e'+ix] + '**';
    		}
    		
    	}
    }

    </script>
    
<style type='text/css'><!--
table td, table th {
  text-align: left;
  padding: 5px 30px;
}

// --></style>

    <style type='text/css'>
      #main h2 {
	color:#3B5269;
	font-family: Rockwell,Georgia,"Times New Roman",Times,serif;
	font-size:170%;
	margin-top:1em;
	font-weight: normal;
	margin-bottom: 0.5em;
      }
    </style>
    </head>
<body  id='$$$pageid$$$' >
<table id='topheader'><tr>
<td id ='topleft' >
<a href='http://www.medcommons.net/' title='MedCommons Homepage'><img  border="0" src="/images/MEDcommons_logo_246x50.gif" /></a><div id="navcontainer"></div></td>
<td width=100% >&nbsp;</td>
  <td  id='topright' >
 <span id='visi' class=right >
                <a href='../acct/home.php'><img alt='' border=0 id='stamp' src='../acct/stamp.php'  /></a>
 </span>
</td>
 </tr></table>
        <div id='main'>
                   
<? require_once "urls.inc.php"; ?>
<?
  function getRemoteLastModified( $uri ) {
      // default
      $unixtime = 0;
      $fp = fopen( $uri, "r" );
      if( !$fp ) {
        return "< unknown >";
      }
      $metaData = stream_get_meta_data( $fp );
      $unixtime = false;
      foreach( $metaData['wrapper_data'] as $response ) {
          if( substr( strtolower($response), 0, 15 ) == 'last-modified: ' ) {
              $unixtime = strtotime( substr($response, 15) );
              break;
          }
      }
      fclose( $fp );
      if($unixtime === false) {
        return "< unknown >";
      }
      else
        return strftime('%Y-%m-%d %H:%M:%S',$unixtime);
  }
?>
<br style='clear:both;'/>
<h2>Appliance Version Information</h2>
<p style='text-align: center;'>
  <table border='1'>
    <tr><th>Component</th><th>Revision</th><th>Revision Timestamp</th></tr>
    <tr>
      <td>Console</td>
      <td><?=file_get_contents('/console/media/revision.txt')?></td>
      <td><?=getRemoteLastModified('/console/media/revision.txt')?></td>
    </tr>
    <tr>
      <td>Account Service</td>
      <td><?=file_get_contents('/acct/revision.txt')?></td>
      <td><?=getRemoteLastModified('/acct/revision.txt')?></td>
    </tr>
    <tr>
      <td>Secure Service</td>
      <td><?=file_get_contents('/secure/revision.txt')?></td>
      <td><?=getRemoteLastModified('/secure/revision.txt')?></td>
    </tr>
    <tr>
      <td>Gateway</td>
      <td><?=file_get_contents($GLOBALS['Default_Repository']."/revision.jsp")?></td>
      <td><?=file_get_contents($GLOBALS['Default_Repository']."/revisionTimestamp.jsp")?></td>
    </tr>
  </table>
</p>

        </div>

            
   
    &nbsp;<br/>
     <div id="footer">
	<ul class="listinlinetiny">
			<li>Copyright &copy; 2008 MedCommons, Inc.</li>&nbsp;&nbsp;&nbsp;&nbsp;
                   <li><a href="http://www.medcommons.net/index.php">Home</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
                     <li><a href="http://www.medcommons.net/termsofuse.php">Terms of Use</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
                         <li><a href='http://www.medcommons.net/privacy.php'>Privacy Policy</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
                         <li><a target='_new' href="http://ccrcommons.com">Public HealthURLs</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
                            <li><a href="http://forum.medcommons.net/">Forums</a></li>&nbsp;&nbsp;|&nbsp;&nbsp;
                       <li><a href='mailto:info@medcommons.net'>Support</a></li> &nbsp;&nbsp;|&nbsp;&nbsp;    
                    <li class="notiPhone"><a href="http://www.medcommons.net/contact.php">Contact Us</a></li>
		</ul>

        </div> <!-- footer  end -->
              <script  type="text/javascript">wsinit();</script>
  
  <script type="text/javascript">
  // prevent original www_init() defined in base.js from being called
  function www_init() {
  }
  if(getCookie('mc'))
    document.write("<style type='text/css'> #loggedon, #visi { display: block; }</style>");
  </script>
   
</body>
</html>
