<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0">

<!-- ********************************************************************
     $Id: chunk-common.xsl,v 1.41 2004/10/23 11:51:10 nwalsh Exp $
     ********************************************************************

     This file is part of the XSL DocBook Stylesheet distribution.
     See ../README or http://nwalsh.com/docbook/xsl/ for copyright
     and other information.

     ******************************************************************** -->

<!-- ==================================================================== -->

 <xsl:template match="para">
    <b><xsl:apply-templates select="reqTitle"/></b>
    <i><xsl:apply-templates select="reqDescription"/></i>
</xsl:template>

    
</xsl:stylesheet>
