<?xml version="1.0"?>
<xsl:stylesheet 
	version="1.0"
	xmlns:xsl='http://www.w3.org/1999/XSL/Transform'
	xmlns:SOAP-ENV='http://schemas.xmlsoap.org/soap/envelope/'
	xmlns='urn:oasis:names:tc:ebxml-regrep:registry:xsd:2.1'
	xmlns='urn:oasis:names:tc:ebxml-regrep:query:xsd:2.1' >
<xsl:output method="html"/>

<!-- Simple query processor -->

 <xsl:template match="/">
	<html>
		<head>
		<title> Query results </title>
		</head>
		<body>
		<h1> Query results </h1>
		<ul>
			<xsl:apply-templates select="RegistryResponse"/>
		</ul>
   <xsl:copy-of select="*" />
 </xsl:template>
<xsl:template match="RegistryResponse">
	<li>
		<xsl:value-of select="@status"/>j
	</li>
</xsl:template>

</xsl:stylesheet> 
