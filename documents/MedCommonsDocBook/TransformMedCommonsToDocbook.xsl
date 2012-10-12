<?xml version="1.0" encoding="UTF-8" ?>
<!-- java -jar lib/saxon.jar -t documents\TempMedCommons.xml TransformMedCommonsToDocbook.xsl > temp.xml -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
   <!-- Import the identity transformation. -->
   <xsl:import href="Identity.xsl"/>
   <!-- 
   | This will match any 'requirement element.
   +-->
   <xsl:template match="requirement">
      <xsl:variable name="id" select="@id"/>
      <!-- This will construct a "date" attribute having value of its content -->
      <informaltable frame="all" tocentry="1">
         <!--<title>Requirement <xsl:value-of select='$id'/></title> -->
         <tgroup cols="3" align="left" colsep="1" rowsep="1">
            <colspec colwidth="1.0in"/>
            <colspec colwidth="1.5in"/>
            <colspec colwidth="4.0in"/>
            <tbody valign="top">
               <row>
                  <entry>
                     <!-- Put in indexEntry , tochere -->
                     <anchor id="{$id}" role="requirement"/>
                     <indexterm>
                        <primary>
                           <xsl:value-of select="$id"/>
                        </primary>
                     </indexterm>
                     <emphasis>
                        <xsl:value-of select="$id"/>
                     </emphasis>
                  </entry>
                  <entry>
                     <xsl:apply-templates select="reqTitle"/>
                  </entry>
                  <entry>
                     <xsl:apply-templates select="reqDescription"/>
                  </entry>
               </row>
            </tbody>
         </tgroup>
      </informaltable>
   </xsl:template>
   <xsl:template match="reqDescription">
      <xsl:apply-templates/>
   </xsl:template>
   <xsl:template match="reqTitle">
      <xsl:value-of select="."/>
   </xsl:template>
   
   <xsl:template match="usecase">
      <xsl:variable name="id" select="@id"/>
      <!-- This will construct a "date" attribute having value of its content -->
      <sect2>
         <title> 
            <xsl:value-of select="$id"/> 
            &#160; <!-- space -->
            <xsl:apply-templates select="useTitle"/>
         </title>
         <xsl:apply-templates select="useDescription"/>
      </sect2>
      
   </xsl:template>
   <xsl:template match="useDescription">
      <xsl:apply-templates/>
   </xsl:template>
   <xsl:template match="useTitle">
      <xsl:value-of select="."/>
   </xsl:template>
   <!-- Matching for elements that may be embedded in useDescription or reqDescription -->
   <xsl:template match="orderedlist">
      <xsl:copy>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="para">
      <xsl:copy>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>
   <xsl:template match="itemizedlist">
      <xsl:copy>
         <xsl:apply-templates/>
      </xsl:copy>
   </xsl:template>
</xsl:stylesheet>
