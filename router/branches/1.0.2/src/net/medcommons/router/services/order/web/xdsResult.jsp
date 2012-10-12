<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ page isELIgnored="false" %> 
<!--
 Copyright 2005 MedCommons Inc.   All Rights Reserved.
-->
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
<body>
  <h1>Success.</h1>
  <c:set var="xslt">
      <?xml version="1.0"?>
      <xsl:stylesheet xmlns:xsl='http://www.w3.org/1999/XSL/Transform' version="1.0" >
       <xsl:template match="/RegistryResponse">
          <xsl:variable name='status' select='@status'/>
          <xsl:choose>
            <xsl:when test=" $status = 'Success'">Your document was successfully transmitted.</xsl:when>
            <xsl:otherwise>There appeared to be a problem with your document submission.  The
            remote registry may be down or unable to accept your document.</xsl:otherwise>
          </xsl:choose>
          <xsl:apply-templates/>
       </xsl:template>
      </xsl:stylesheet>     
  </c:set>
  <x:transform xslt="${xslt}" xml="${registryResponseXml}">
  </x:transform>
</body>
</html>

