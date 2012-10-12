<?xml version='1.0'?>
<!--    
    CCR Styleseet for HIMMS 2005 Demonstration
    
    This stylesheet is intended to display CCRs for human consumption on a computer screen, or for printing to paper.
    
    Portions of this stylesheet are based on similar work done by Bob Dolin for CDA Level 1 [specifically, the 
    styles used, and the date formatting code].

    Change History:
    12/03/2004  Steve Moore	Removed double - in comments; caused problems for XMLSpy
    12/02/2004  Initial Version created - Just the basics for display of CCRs using the CCR Schema for the HIMSS IHE Demo.
    
    Keith W. Boone - Dictaphone Corporation
-->
<!DOCTYPE xsl:stylesheet [
<!ENTITY CCR-Stylesheet
    '-//IHE//XSL IHE V0.9 CCR Stylesheet: 2004-12-02//EN'>
]>
<xsl:stylesheet version='1.0' xmlns:xsl='http://www.w3.org/1999/XSL/Transform' xmlns:ccr="urn:astm-org:CCR">
    
    <xsl:output method='html' indent='yes' version='4.01' encoding='UTF-8' doctype-public='-//W3C//DTD HTML 4.01//EN'/>
    
    <xsl:variable name='docType'>Continuity of Care Record</xsl:variable>
    <xsl:variable name='orgName' select='/ContinuityOfCareRecord/Source/Actor/Organization/Name'/>
    <xsl:variable name='title'>
        <xsl:value-of select='$orgName'/>
        <xsl:text> </xsl:text>
        <xsl:value-of select='$docType'/>
    </xsl:variable>
    
    <xsl:template match='/'>
        <xsl:apply-templates select='ccr:ContinuityOfCareRecord'/>
    </xsl:template>
    
    <xsl:template match='ccr:CCRDataObjectID|ccr:ProcedureType|ccr:ProductType|ccr:ProductName|ccr:BrandName'/>
    
    <xsl:template match='/ccr:ContinuityOfCareRecord'>
        <html>
            <head>
                <meta name='Generator' content='&CCR-Stylesheet;'/>
                <xsl:comment>
                    do NOT edit this HTML directly, it was generated
                    via an XSLT transformation from the original CCR
                    document.
                </xsl:comment>
                <style>
                    <xsl:comment>
                        body {background-color: white; color: black; }
                        p { margin: 10px 0 10px 0.25in; }
                        div.caption { font-weight: bold; text-decoration: underline; }
                        span.coded { font-weight: bold; text-decoration: underline; }
                        span.coding { font-size: 50%; border: 1px solid black;}
                        span.caption { font-weight: bold; }
                        div.title { font-size: 18pt; font-weight: bold }
                        div.demographics { text-align: left; vertical-align: top; }
                        TABLE { border: 0px solid black; border-collapse: collapse; }
                        TR { border: 0px solid black }
                        TD { vertical-align: text-top; border: 0px solid black; }
                    </xsl:comment>
                </style>
                <title>
                    <xsl:value-of select='$title'/>
                </title>
            </head>
            <body>
                <h1 class='title'><xsl:value-of select='$title'/></h1>
                <table>
                    <tr>
                        <td><xsl:apply-templates select='ccr:DateTime/ccr:DateTimeType'/>:</td>
                        <td>
                            <xsl:call-template name='date'>
                                <xsl:with-param name='date' select='ccr:DateTime/ccr:ExactDateTime'/>
                            </xsl:call-template>
                        </td>
                    </tr>
                </table>
                <h2>From</h2>
                <div class='demographics'>
                    <table>
                        <xsl:for-each select='ccr:Source/ccr:Actor[./ccr:Person]'>
                            <tr>
                                <td><xsl:value-of select='./ccr:Roles/ccr:Role'/>:</td><td><table><xsl:apply-templates select='.'/></table></td>
                            </tr>
                        </xsl:for-each>
                        <xsl:for-each select='ccr:Source/ccr:Actor[./ccr:Institution]'>
                            <tr>
                                <td>Organization:</td><td><xsl:apply-templates select='ccr:Source/ccr:Actor/ccr:Institution'/></td>
                            </tr>
                        </xsl:for-each>
                        <tr>
                            <td><xsl:value-of select='ccr:Source/ccr:Actor/ccr:InformationSystem/ccr:Type'/>:</td>
                            <td><xsl:value-of select='ccr:Source/ccr:Actor/ccr:InformationSystem/ccr:Name'/>
                                <xsl:text> </xsl:text>
                                <xsl:value-of select='ccr:Source/ccr:Actor/ccr:InformationSystem/ccr:Version'/>
                            </td>
                        </tr>
                    </table>
                </div>
                <h2>To</h2>
                <xsl:for-each select='ccr:To'>
                    <div class='demographics'>
                        <table>
                            <xsl:for-each select='ccr:To/ccr:Person'>
                                <tr>
                                    <td><xsl:value-of select='ccr:Role'/>:</td><td><table><xsl:apply-templates select='.'/></table></td>
                                </tr>
                            </xsl:for-each>
                            <xsl:for-each select='ccr:To/ccr:Institution'>
                                <tr>
                                    <td>Organization:</td><td><xsl:apply-templates select='ccr:To/ccr:Institution'/></td>
                                </tr>
                            </xsl:for-each>
                            <tr>
                                <td><xsl:value-of select='ccr:Source/ccr:Actor/ccr:InformationSystem/ccr:Type'/>:</td>
                                <td><xsl:value-of select='ccr:Source/ccr:Actor/ccr:InformationSystem/ccr:Name'/>
                                    <xsl:text> </xsl:text>
                                    <xsl:value-of select='ccr:Source/ccr:Actor/ccr:InformationSystem/ccr:Version'/>
                                </td>
                            </tr>
                        </table>
                    </div>
                </xsl:for-each>
                <h2>Patient</h2>
                <div class='demographics'>
                    <xsl:apply-templates select='ccr:Patient'/>
                </div>
                <br/>
                <xsl:apply-templates select='ccr:Body'/>
                <xsl:if test='ccr:Reference'>
                    <h2>References:</h2>
                    <ul>
                        <xsl:for-each select='ccr:Reference'>
                            <li><a href='{ccr:Location}'><xsl:value-of select='ccr:Text'/></a></li>
                        </xsl:for-each>
                    </ul>
                </xsl:if>
            </body>
        </html>
    </xsl:template>
    
    <xsl:template match='ccr:Body'>
        <xsl:apply-templates/>
    </xsl:template>
    
    <xsl:template match="ccr:Insurance">
        <h2>Insurance</h2>
        <ul><xsl:apply-templates/></ul>
    </xsl:template>
    <xsl:template match="ccr:Insurer">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:AdvanceDirectives">
        <h2>Advanced Directives</h2>
        <ul><xsl:apply-templates/></ul>
    </xsl:template>
    <xsl:template match="ccr:AdvanceDirective">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Support">
        <h2>Support</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:SupportProvider">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:FunctionalStatus">
        <h2>Functional Status</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Function">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Problems">
        <h2>Problems</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Problem">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:FamilyHistory">
        <h2>Family History</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:History">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:SocialHistory">
        <h2>Social History</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:RiskFactor">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Alerts">
        <h2>Allergies, Alerts and Adverse Reactions</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Alert">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Medications">
        <h2>Medications</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Medication">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Immunizations">
        <h2>Immunizations</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Immunization">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:DoseStrength">
        <div>Dose Strength: <xsl:apply-templates/></div>
    </xsl:template>
    <xsl:template match="ccr:Form">
        <div>Form: <xsl:apply-templates/></div>
    </xsl:template>
    <xsl:template match="ccr:Concentration">
        <div>Concentration: <xsl:apply-templates/></div>
    </xsl:template>
    <xsl:template match="ccr:Quantity">
        <div>Quantity: <xsl:apply-templates/></div>
    </xsl:template>
    
    <xsl:template match="ccr:Route">
        <div>Route: <xsl:apply-templates/></div>
    </xsl:template>
    
    <xsl:template match="ccr:VitalSigns">
        <h2>Vital Signs</h2>
        <dl><xsl:apply-templates/></dl>
    </xsl:template>
    
    <xsl:template match="ccr:Results">
        <h2>Lab and Diagnostic Results</h2>
        <dl><xsl:apply-templates/></dl>
    </xsl:template>
    <xsl:template match="ccr:Result">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Procedures">
        <h2>Procedures</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Procedure">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:Encounters">
        <h2>Encounters</h2>
        <ul><xsl:apply-templates/></ul>
    </xsl:template>
    
    <xsl:template match="ccr:Encounter">
        <li><xsl:apply-templates select='ccr:EncounterType'/>: <xsl:apply-templates select='ccr:Description'/>
            <br/>
            <xsl:apply-templates select='ccr:DateTimeType'/>: <xsl:call-template name='date'><xsl:with-param name='date' select='ccr:ExactDateTime'/></xsl:call-template>
        </li>
    </xsl:template>
    
    <xsl:template match="ccr:PlanOfCare">
        <h2>Plan of Care</h2>
        <ol><xsl:apply-templates/></ol>
    </xsl:template>
    <xsl:template match="ccr:Plan">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match="ccr:HealthCareProviders">
        <h2>Health Care Providers</h2>
        <ul><xsl:apply-templates/></ul>
    </xsl:template>
    <xsl:template match="ccr:Provider">
        <li><xsl:apply-templates/></li>
    </xsl:template>
    
    <xsl:template match='ccr:Patient|ccr:Actor[./ccr:Person]|ccr:Provider'>
        <table>
            <col style='text-align: right'/>
            <col style='text-align: left'/>
            <tbody>
                <xsl:apply-templates select='ccr:Person/ccr:Name'/>
                <xsl:if test='ccr:Person/ccr:DateOfBirth'>
                    <tr><td>Date of Birth:</td>
                        <td><xsl:call-template name='date'>
                                <xsl:with-param name='date' select='ccr:Person/ccr:DateOfBirth/ccr:ExactDateTime'/>
                                <xsl:with-param name='dateOnly' select='1'/>
                            </xsl:call-template>
                        </td>
                    </tr>
                </xsl:if>
                <xsl:if test='ccr:Specialty'>
                    <tr><td>Specialty:</td>
                        <td><table><xsl:apply-templates select='ccr:Specialty'/></table></td>
                    </tr>
                </xsl:if>
                <xsl:if test='ccr:ExternalIdentifiers'>
                    <xsl:for-each select='ccr:ExternalIdentifiers/ccr:ExternalID'>
                        <tr><td><xsl:if test='position()=1'>ID:</xsl:if></td><td><xsl:apply-templates select='ccr:ID'/></td></tr>
                    </xsl:for-each>
                </xsl:if>
                <xsl:apply-templates select='ccr:Address'/>
                <xsl:apply-templates select='ccr:Telephone'/>
                <xsl:if test='ccr:EMail'><tr><td>E-Mail:</td>
                        <td><table><xsl:apply-templates select='ccr:EMail'/></table></td>
                    </tr></xsl:if>
            </tbody>
        </table>
    </xsl:template>
    
    <xsl:template match='ccr:ID'>
        <xsl:value-of select='substring-before(.,"^")'/> [<xsl:value-of select='substring-before(substring-after(.,"&amp;"),"&amp;")'/>]
    </xsl:template>
    
    <xsl:template match='ccr:Name'>
        <xsl:choose>
            <xsl:when test='../self::ccr:Person'>
                <tr><td>Name:</td><td><xsl:apply-templates select='ccr:CurrentName'/></td><td><xsl:if test='string(ccr:BirthName)'>[Current]</xsl:if></td></tr>
                <xsl:if test='string(ccr:BirthName)'><tr><td> </td><td><xsl:apply-templates select='ccr:BirthName'/></td><td>[Birth]</td></tr></xsl:if>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='.'/>
            </xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    
    <xsl:template match='ccr:BirthName'>
        <xsl:call-template name='CCRName'/>
    </xsl:template>
    <xsl:template match='ccr:CurrentName'>
        <xsl:call-template name='CCRName'/>
    </xsl:template>
    
    <xsl:template name='CCRName'>
        <xml><xsl:copy-of select='.'/></xml>
        <xsl:value-of select='ccr:Family'/>
        <xsl:if test='ccr:Suffix'><xsl:text> </xsl:text><xsl:value-of select='ccr:Suffix'/></xsl:if>
        <xsl:text>, </xsl:text><xsl:if test='ccr:Prefix'><xsl:value-of select='ccr:Prefix'/><xsl:text> </xsl:text></xsl:if>
        <xsl:value-of select='ccr:Given'/>
        <xsl:if test='ccr:Middle'> <xsl:value-of select='ccr:Middle'/></xsl:if>
    </xsl:template>
    
    <xsl:template match='ccr:Address'>
        <xsl:if test='string(ccr:Line1) or string(ccr:Line2) or string(ccr:City) or string(ccr:State) or string(ccr:PostalCode)'>
            <tr><td>Address:</td>
                <td><table>
                        <tr><td><xsl:value-of select='ccr:Line1'/></td></tr>
                        <xsl:if test='ccr:Line2'><tr><td><xsl:value-of select='ccr:Line2'/></td></tr></xsl:if>
                        <tr><td><xsl:apply-templates select='ccr:City'/>, <xsl:value-of select='ccr:State'/>  <xsl:value-of select='ccr:PostalCode'/></td></tr>
                        <xsl:if test='ccr:Country'><tr><td><xsl:value-of select='ccr:Country'/></td></tr></xsl:if>
                    </table>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    <xsl:template match='ccr:Telephone'>
        <xsl:if test='string(ccr:Value)'>
            <tr>
                <td>Telephone:</td>
                <td>
                    <table>
                        <tr><td><xsl:value-of select='ccr:Type'/>:</td><td><xsl:value-of select='ccr:Value'/></td></tr>
                    </table>
                </td>
            </tr>
        </xsl:if>
    </xsl:template>
    
    <xsl:template match='ccr:Code'>
        <xsl:value-of select='ccr:Value'/>
        <xsl:if test='ccr:CodingSystem'>
            <xsl:text> </xsl:text>[<xsl:value-of select='ccr:CodingSystem'/> <xsl:value-of select='ccr:Version'/>]
        </xsl:if>
    </xsl:template>
    
    <xsl:template match='ccr:Description'>
        <xsl:choose>
            <xsl:when test='ccr:*'>
                <div>
                    <span>
                        <xsl:if test='ccr:Code'>
                            <xsl:attribute name='title'><xsl:apply-templates select='ccr:Code'/></xsl:attribute>
                        </xsl:if>
                        <xsl:value-of select='ccr:Text'/>
                    </span>
                    <xsl:if test='ccr:Attribute/ccr:Value'>
                        <div>
                            <dl>
                                <xsl:for-each select='ccr:Attribute'>
                                    <dt><xsl:apply-templates select='ccr:Code'/></dt>
                                    <dd>
                                        <xsl:attribute name='title'><xsl:apply-templates select='ccr:Code'/></xsl:attribute>
                                        <xsl:value-of select='ccr:Value'/>
                                    </dd>
                                </xsl:for-each>
                            </dl>
                        </div>
                    </xsl:if>
                </div>
                <xsl:if test='not(../ccr:ProductName = ccr:Text)'>
                    <div>
                        <xsl:value-of select='../ccr:ProductName'/>
                        <xsl:if test='../ccr:BrandName'>
                            <xsl:text> (</xsl:text><xsl:value-of select='../ccr:BrandName'/>)
                        </xsl:if>
                    </div>
                </xsl:if>
            </xsl:when>
            <xsl:otherwise><xsl:value-of select='.'/></xsl:otherwise>
        </xsl:choose>
    </xsl:template>
    <!--
    outputs a date in Month Day, Year form
    e.g., 19991207 ==> December 07, 1999
    -->
    <xsl:template name='date'>
        <xsl:param name='date'/>
        <xsl:param name='dateOnly' select='0'/>
        <xsl:variable name='d2' select='translate($date,":-T","")'/>
        <xsl:variable name='month' select='substring ($d2, 5, 2)'/>
        <xsl:choose>
            <xsl:when test='$month=01'>
                <xsl:text>January </xsl:text>
            </xsl:when>
            <xsl:when test='$month=02'>
                <xsl:text>February </xsl:text>
            </xsl:when>
            <xsl:when test='$month=03'>
                <xsl:text>March </xsl:text>
            </xsl:when>
            <xsl:when test='$month=04'>
                <xsl:text>April </xsl:text>
            </xsl:when>
            <xsl:when test='$month=05'>
                <xsl:text>May </xsl:text>
            </xsl:when>
            <xsl:when test='$month=06'>
                <xsl:text>June </xsl:text>
            </xsl:when>
            <xsl:when test='$month=07'>
                <xsl:text>July </xsl:text>
            </xsl:when>
            <xsl:when test='$month=08'>
                <xsl:text>August </xsl:text>
            </xsl:when>
            <xsl:when test='$month=09'>
                <xsl:text>September </xsl:text>
            </xsl:when>
            <xsl:when test='$month=10'>
                <xsl:text>October </xsl:text>
            </xsl:when>
            <xsl:when test='$month=11'>
                <xsl:text>November </xsl:text>
            </xsl:when>
            <xsl:when test='$month=12'>
                <xsl:text>December </xsl:text>
            </xsl:when>
        </xsl:choose>
        <xsl:choose>
            <xsl:when test='substring ($d2, 7, 1)="0"'>
                <xsl:value-of select='substring ($d2, 8, 1)'/><xsl:text>,
                </xsl:text>
            </xsl:when>
            <xsl:otherwise>
                <xsl:value-of select='substring ($d2, 7, 2)'/><xsl:text>,
                </xsl:text>
            </xsl:otherwise>
        </xsl:choose>
        <!-- Deal with time part -->
        <xsl:value-of select='substring ($d2, 1, 4)'/>
        <xsl:variable name='hour' select='substring($d2,9,2)'/>
        <xsl:variable name='minute' select='substring($d2,11,2)'/>
        <xsl:variable name='second' select='substring($d2,13,2)'/>
        <xsl:variable name='ampm'>
            <xsl:if test='$hour &lt; 12'>AM</xsl:if>
            <xsl:if test='not($hour &lt; 12)'>PM</xsl:if>
        </xsl:variable>
        <xsl:variable name='hh'>
            <xsl:choose>
                <xsl:when test='$hour= 0 or $hour = 12'>12</xsl:when>
                <xsl:when test='$ampm="PM"'><xsl:value-of select='$hour - 12'/></xsl:when>
                <xsl:otherwise><xsl:value-of select='$hour + 0'/></xsl:otherwise>
            </xsl:choose>
        </xsl:variable>
        <xsl:if test='$hour and not($dateOnly)'>
            <xsl:text> at </xsl:text>
            <xsl:value-of select='concat($hh,":", $minute)'/>
            <xsl:if test='$second'>:<xsl:value-of select='$second'/></xsl:if>
            <xsl:value-of select='$ampm'/>
        </xsl:if>
    </xsl:template>
</xsl:stylesheet>