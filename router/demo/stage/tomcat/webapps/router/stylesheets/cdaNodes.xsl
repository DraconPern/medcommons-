<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
                version="1.0" >

<!-- Formats results from registry-->

<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->



 <xsl:template match="/RegistryResponse">
  <html>

  <head>
    <title>XDS Registry Response</title>
    <link href="main.css" rel="stylesheet" type="text/css"/>

    <style type="text/css">
      body {
        font-family: arial,helvetica;
      }

      td {
        font-size: 12px;
        text-align: left;
      }
    </style>
    <script language="Javascript">
      function checkClose() {
        
      }
    </script>
  </head>
   <body style="position: relative; left: 10px;" onload="checkClose();">

<table border="0" cellpadding="0" cellspacing="0" width="100%" bgcolor="#7F9991">
        <tr>
        <td align="left" valign=" top" rowspan="3" height="50" width="246">
          <img src="images/MEDcommons_logo_246x50.gif" height="50" width="246"/>
          </td>
        </tr>

        <tr><td align="right" valign="bottom" class =" text"></td></tr>
        <tr>
          <td align="right" valign="bottom"><p>
          
          </p></td>
        </tr>
   </table>
   
   <h3 class="headline">XDS Registry Results</h3>
		<xsl:variable name='status' select='@status'/>
		Registry Reponse is 
		<xsl:value-of select='$status'/>
		<xsl:apply-templates/>
	</body>
	</html>
 </xsl:template>
<xsl:template match="AdhocQueryResponse">

	<xsl:apply-templates/>
</xsl:template>
<xsl:template match="SQLQueryResult">
	<ul>
	<xsl:apply-templates/>
	</ul>
</xsl:template>


	
<xsl:template match="ExternalLink">
	<xsl:variable name='externalURI' select='@externalURI'/>
	<ul>
		<li> <a href="/router/TransformXSLT?sourceURL={$externalURI}&amp;stylesheet=cda2htm.xsl"> 
			Display document </a> <xsl:value-of select='$externalURI'/>
				
		</li>
		<li>
		<a href="{$externalURI}"> 
			Download document </a> <xsl:value-of select='$externalURI'/>
			
		</li>
	</ul>
	
</xsl:template>

</xsl:stylesheet> 
