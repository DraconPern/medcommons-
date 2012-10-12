<?xml version="1.0" encoding="UTF-8" ?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                 xmlns:SOAP-ENV="http://schemas.xmlsoap.org/soap/envelope/"
                 exclude-result-prefixes="SOAP-ENV"
                 version="1.0">
<xsl:template match="/">
   <xsl:apply-templates select="SOAP-ENV:Envelope/SOAP-ENV:Body/node()"/>
</xsl:template>

<xsl:template match="*"> <!--synthesize element with the input name-->
   <xsl:element name="{name(.)}" >
     <xsl:copy-of select="@*"/>
     <xsl:apply-templates/>
   </xsl:element>
</xsl:template>

</xsl:stylesheet>