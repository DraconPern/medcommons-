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
<xsl:template match="ObjectRef">
	<xsl:variable name='id' select='@id'/>

	<li> Object Reference 
		<a href="/router/XdsQueryObjectForMetadata1802?stylesheet=ObjectNodes.xsl&amp;id={$id}">
		<xsl:value-of select='$id'/>
		</a>
	</li>


</xsl:template>

<xsl:template match="ExtrinsicObject">
	<xsl:variable name='status' select='@status'/>
	<xsl:variable name='id' select='@id'/>
	<xsl:variable name='author' select='@authorPerson'/>
	<xsl:variable name='mimeType' select='@mimeType'/>
	<xsl:variable name="stylesheet">
			<xsl:choose>
				<xsl:when test="contains($mimeType,'pdf')">ObjectNodes.xsl</xsl:when>
				<xsl:when test="contains($mimeType,'ccr')">ccrNodes.xsl</xsl:when>
				<xsl:when test="contains($mimeType,'cda')">cdaNodes.xsl</xsl:when>
				<xsl:otherwise>ObjectNodes.xsl</xsl:otherwise>
			</xsl:choose>
	</xsl:variable>
	<ul>
		<li> Status = <xsl:value-of select='$status'/></li>
		<li> Name=<xsl:apply-templates select="Name"/></li>
		<li>
		 <a href="/router/XdsQueryObjectForExternalLinks1804?stylesheet={$stylesheet}&amp;id={$id}"> 
			Display 
		</a> document link. 
		
		</li>

		<li> mimeType = <xsl:value-of select='$mimeType'/></li>
		<li>
		<a href="/router/XdsQueryObjectForMetadata1802?stylesheet=ReplaceDocument.xsl&amp;id={$id}"> 
			Replace 
		</a> this document with a new one
		</li>
		<li> Document Attributes:
		<ul>
			<xsl:apply-templates select="Slot"/>
			<xsl:apply-templates select="Classification"/>
		</ul>
		</li>
	</ul>
</xsl:template>

<xsl:template match="Name">
	<b><xsl:apply-templates/></b>
</xsl:template>
<xsl:template match="LocalizedString">
	<xsl:variable name="value" select="@value"/>
	<xsl:value-of select="$value"/>
</xsl:template>
<xsl:template match="Slot">
	<xsl:variable name="name" select="@name"/>
	<xsl:variable name="value" select="ValueList/Value"/>
	<li><xsl:value-of select="$name"/> =<xsl:value-of select="$value"/></li>
</xsl:template>
<xsl:template match="ExternalLink">
	<xsl:variable name='externalURI' select='@externalURI'/>
	<ul>
		<li> <a href="{$externalURI}"> 
			<xsl:value-of select='$externalURI'/>
				</a>
		</li>
	</ul>
	
</xsl:template>
<!--
Existing format codes
"PDF/IHE 1.x"
"CDA/IHE 1.0"
"CCR/IHE 0.9"
-->
<xsl:template match="Classification">
	<xsl:variable name='classificationScheme' select='@classificationScheme'/>
	<xsl:variable name='classifiedObject' select='@classifiedObject'/>
	<xsl:variable name='nodeRepresentation' select='@nodeRepresentation'/>
	<xsl:variable name='id' select='@id'/>
	<li>Classification:  <xsl:apply-templates select="Name"/>
	<xsl:apply-templates select="Slot/ValueList" />
	</li>
</xsl:template>
<xsl:template match="Value">
	<xsl:value-of select="."/>
</xsl:template>
<xsl:template match="Classificationx">
	<xsl:variable name='nodeRepresentation' select='@nodeRepresentation'/>

	
		<xsl:choose>
			<xsl:when test="starts-with(.,'PDF')">
				<br> PDF Format code = <xsl:value-of select='$nodeRepresentation'/> </br>
			</xsl:when>
			<xsl:when test="starts-with(.,'CCR')">
				<br> CCR Format code = <xsl:value-of select='$nodeRepresentation'/> </br>
			</xsl:when>
			<xsl:when test="starts-with(.,'CDA')">
				<br> CDA Format code = <xsl:value-of select='$nodeRepresentation'/> </br>
			</xsl:when>
			<xsl:otherwise>
				<!-- <br> Other Format code = <xsl:value-of select='$nodeRepresentation'/> </br> -->
			</xsl:otherwise>
		</xsl:choose>
	
</xsl:template>
</xsl:stylesheet> 
