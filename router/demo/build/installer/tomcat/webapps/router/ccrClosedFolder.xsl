<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

<xsl:param name="index" select="0"/>
  <xsl:param name="xOffset" select=" 20 "/>
  <xsl:param name="top" select=" $index * -20 "/>
  <xsl:param name="left" select=" $index * $xOffset "/>
  <!-- <xsl:param name="tabPosition" select=" $index * ( 140 - $xOffset + 10 ) "/> -->
  <xsl:param name="zIndex" select=" 60 - $index * 3 "/>
  <xsl:param name="numSeries" select="0"/>
  <xsl:param name="maxThumbs" select="8"/>
  <xsl:param name="creationDate" select="0"/>
  <xsl:param name="patientMedcommonsId" select="'1111222233334444'"/>
  <xsl:param name="trackingNumber" select="'000000000000'"/>

  <xsl:template match="text()|@*">
  </xsl:template>

  <xsl:template match="/ContinuityOfCareRecord">
    <div id="ccrRecord-{$index}" style="position: absolute; top: {$top}; left: {$left}; z-index: {$zIndex};">      
      
      <div id="folder-{$index}" class="folder">

        <div id="folderTab-{$index}" class="folderTab" style="z-index: {$zIndex + 1}; " 
          onclick="toggleFolder( {$index} );">
          <img id="foldertabbutton-{$index}" class="folderTabButton" src="images/minusbutton.png"/>
          &#160;&#160;<xsl:value-of select="$creationDate"/>
        </div>

        <!-- patient card -->
        <xsl:for-each select="/ContinuityOfCareRecord/Patient">
          <div id="patientCard-{$index}" class="contactCardBox patientCard">
            <div style="position: relative;">
              <span class="cardLabel">CCR Patient</span>
              <div class="cardCheckBox"><input type="checkbox" onclick="handleCheckBox('patient', {$index});" name="patientNotify"/>
                  Send Notification Email <xsl:value-of select="EMail/Value[../Status='Active']"/></div>
              <xsl:apply-templates> 
                <xsl:with-param name="cardType" select="'Patient'"/>
                <xsl:with-param name="medcommonsId" select="$patientMedcommonsId"/>
              </xsl:apply-templates> 
            </div>
          </div>
        </xsl:for-each>

        

        <!-- track#, status, creation date, patient name -->
        <div id="ccrInfo-{$index}" class="ccrInfo">
          <table class="ccrInfoDetails">
            <tr><td>MedCommons Tracking</td><td class="ccrDetail"><xsl:value-of select="concat(substring($trackingNumber,1,4),' ',substring($trackingNumber,5,4),' ',substring($trackingNumber,9,4))"/></td></tr>
            <tr><td>MedCommons Status</td><td class="ccrDetail">No Errors</td></tr>
            <tr><td>CCR Creation Date</td><td class="ccrDetail"><xsl:value-of select="DateTime/ExactDateTime"/></td></tr>
            <tr><td>CCR Patient Name</td><td class="ccrDetail"><xsl:value-of select="Patient/Person/Name/CurrentName/Given"/>&#160;<xsl:value-of select="Patient/Person/Name/CurrentName/Family"/></td></tr>
          </table>
        </div>

        <div id="ccrPurpose-{$index}" class="ccrPurpose">
          <xsl:value-of select="Purpose/Description/Code/Value"/><br/>
          <xsl:value-of select="Purpose/Description/Text"/>&#160;
        </div>

        <!-- source card -->
        <xsl:for-each select="/ContinuityOfCareRecord/Source/Actor[count(Person) != '0']">
          <div id="sourceCard-{$index}" class="contactCardBox sourceCard">
            <div style="position: relative;">
              <span class="cardLabel">CCR Source&#160;</span>
              <div class="cardCheckBox">
                <input type="checkbox" name="patientNotify" onclick="handleCheckBox('source', {$index});"/>
                  Send Email on Receipt 
                  <xsl:value-of select="EMail/Value[../Status='Active']"/></div>
              <xsl:apply-templates> 
                <xsl:with-param name="cardType" select="'Source'"/>
              </xsl:apply-templates> 
            </div>
          </div>
        </xsl:for-each>

        <!-- to card -->
        <xsl:for-each select="/ContinuityOfCareRecord/To/Actor[count(Person) != '0']">
          <div id="toCard-{$index}" class="toCard contactCardBox">
            <div style="position: relative;">
              <span class="cardLabel">CCR To</span>
              <div id="ccrToCheckBox" class="cardCheckBox">
                <input type="checkbox" name="patientNotify" onclick="handleCheckBox('to', {$index});" />
                  Send Notification Email <xsl:value-of select="EMail/Value[../Status='Active']"/></div>
              <xsl:apply-templates/> 
            </div>
          </div>
        </xsl:for-each>
        
        <div id="bodySectionContainer-{$index}" class="bodySectionContainer">
          <p class="bodySectionLabel">CCR Sections</p>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Insurance</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Medications</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Advance Directives</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Functional Status</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Support</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Vital Signs</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Immunizations</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Procedures</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Problems</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Encounters</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Family History</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Plan Of Care</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Social History</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Alerts</xsl:with-param></xsl:call-template>
          <xsl:call-template name="BodySection"><xsl:with-param name="bodySectionName">Health Care Providers</xsl:with-param></xsl:call-template>
        </div>

        <div id="thumbnailContainer-{$index}" class="thumbnailContainer">
            <xsl:call-template name="thumbnails">
              <!--<xsl:with-param name="thumbnailIndex" select="$numSeries - 1"/>-->
            </xsl:call-template>
        </div>

        <div id="moreButton-{$index}" class="moreButton"><img src="images/more.gif" onclick="showWado({$index},0);"/></div>
          
      </div> <!-- folder -->
    </div> <!-- ccrRecord -->
  </xsl:template>
  
  <xsl:template name="BodySection">
    <xsl:param name="bodySectionName"></xsl:param>    
    <xsl:param name="bodySectionElement"><xsl:value-of select="translate($bodySectionName,' ','')"/></xsl:param>    

    <span class="bodySectionBox">
      <img src="images/bodysectiontop.gif"/>
      <div class="bodySectionMiddle" style="">
        <div style="width: 80px; text-align: center">
          <xsl:attribute name="class">
          <xsl:choose>
            <xsl:when test="count(/ContinuityOfCareRecord/Body/*[local-name()=$bodySectionElement]) != 0">
              presentBodySection
            </xsl:when>
            <xsl:otherwise>notPresentBodySection</xsl:otherwise>
          </xsl:choose>
        </xsl:attribute>
        <xsl:choose>
          <xsl:when test="count(/ContinuityOfCareRecord/Body/*[local-name()=$bodySectionElement]) != 0">
            <a href="javascript:showBodySection({$index},'{$bodySectionElement}');"><xsl:value-of select="$bodySectionName"/></a>
          </xsl:when>
          <xsl:otherwise><xsl:value-of select="$bodySectionName"/></xsl:otherwise>
        </xsl:choose>
        </div>
      </div>
      <img src="images/bodysectionbottom.gif"/>
    </span>
  </xsl:template>

  <xsl:template match="Person">
    <xsl:param name="cardType">Default</xsl:param>    
    <xsl:param name="medcommonsId">1111111111111111</xsl:param>    
    <div class="contactCard">
        <span class="contactDetails contactDetails{$cardType}">
          <span class="contactName"><xsl:value-of select="Name/CurrentName/Given"/>&#160;<xsl:value-of select="Name/CurrentName/Family"/>&#160;<xsl:value-of select="Name/CurrentName/Title"/></span>
          <img class="contactPicture" src="images/adrian.jpg"/>
          <span class="medcommonsId">MedCommons ID<sup>#</sup>&#160;<xsl:call-template name="mcId"><xsl:with-param name="mcId" select="$medcommonsId"/></xsl:call-template></span>
          <span class="contactTitle"><br/>TODO</span>
          <span class="contactOrg"><br/>Medcommons Inc.</span>
          <span class="contactAddress"><br/><br/><xsl:value-of select="../Address/Line1"/>
            <br/><xsl:value-of select="../Address/City"/>,<xsl:value-of select="../Address/State"/>&#160;<xsl:value-of select="../Address/PostalCode"/></span>
          <span class="phoneSection">
            <xsl:for-each select="../Telephone">
              <span class="contactWorkNo"><br/>
                <xsl:choose>
                  <xsl:when test="Type='Office Phone'">Office</xsl:when>
                  <xsl:otherwise><xsl:value-of select="Type"/></xsl:otherwise>
                </xsl:choose>:<span class="phoneNo">&#160;<xsl:value-of select="Value"/></span></span>
            </xsl:for-each>
            &#160;
          </span> <!-- phone section -->
          <span class="contactEmail"><xsl:value-of select="../EMail/Value[../Status='Active']"/>&#160;</span>
        </span> <!-- contact details -->
    </div> <!-- end contactCard -->
  </xsl:template>

  <xsl:template name="thumbnails">
    <xsl:param name="thumbnailIndex">0</xsl:param>    
    <!-- render the thumbnail -->
    <div class="thumbnailBox" id="thumbnailBox{$index}-{$thumbnailIndex}" onclick="showWado({$index},{$thumbnailIndex});">
      <img id="thumbnailImage{$index}-{$thumbnailIndex}" src="images/transparentblank.gif"/>
    </div>
    <!-- recurse -->
    <xsl:if test="$thumbnailIndex &lt; $numSeries - 1"> 
      <xsl:if test="$thumbnailIndex &lt; $maxThumbs - 1"> 
        <xsl:call-template name="thumbnails">
          <xsl:with-param name="thumbnailIndex" select="$thumbnailIndex + 1"/> 
        </xsl:call-template>
      </xsl:if>
    </xsl:if>
  </xsl:template>

  <xsl:template name="mcId">
    <xsl:param name="mcId">0000000000000000</xsl:param>    
    <xsl:value-of select="concat(substring($mcId,1,4),' ',substring($mcId,5,4),' ',substring($mcId,9,4),' ',substring($mcId,13,4))"/>
  </xsl:template>

</xsl:stylesheet>


