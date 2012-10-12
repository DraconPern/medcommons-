<?xml version="1.0" encoding="UTF-8" ?>

<!--
    Document   : validateF.xsl
    Created on : October 5, 2004, 8:35 AM
    Author     : bill
    Description:
        Purpose of transformation follows.
-->

<!--
Issues
1) Association objects must have hard coded uuids.


-->

<!-- Test ideas

make sure all required fields are not just ""

contents of folder can be in same SS or not - complicates other tests
xxx required attributes present
xxx all docs for a folder are same patientid
xxx associations linking submission set to doc must have special slot (by value, by ref)
SS can include existing docs (in registry) - complicates other tests
all by value docs in ss must be same patientid
XDSDocumentEntryStub
folders are in ss
coding - need reference tables
association types, limited to structural plus 3 documented in xds
xxx slots - single or mulitple values
check repository submission request differently from registry submission request
slot encodings
R2 attributes
no extra attributes
xxx folder atts
xxx ss atts
no nesting of folders
-->

<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
    <xsl:output method="text" indent="yes" omit-xml-declaration="yes"/>

<xsl:param name="inputType"/>  <!-- values are P or PR -->
    
<xsl:variable name="debug" select="false()"/>

<!-- definition of XDSDocumentEntry.classCode -->
<xsl:variable name="classCodes" select="document('classCodes.xml')"/>

<xsl:variable name="confidentialityCodes" select="document('confidentialityCodes.xml')"/>
<xsl:variable name="healthCareFacilityTypeCodes" select="document('healthCareFacilityTypeCodes.xml')"/>
<xsl:variable name="practiceSettingCodes" select="document('practiceSettingCodes.xml')"/>
<xsl:variable name="contentTypeCodes" select="document('contentTypeCodes.xml')"/>
<xsl:variable name="formatCodes" select="document('formatCodes.xml')"/>
<xsl:variable name="externalIdentifiers" select="document('externalIdentifiers.xml')"/>


<xsl:template match="/">
    <xsl:variable name="results">
        <!-- Structural constraints -->
        <xsl:call-template name="submit-wrapper"/>
        <xsl:call-template name="eo-are-xds-docs"/>
        <xsl:call-template name="package-are-folder-or-ss"/>
        <xsl:call-template name="valid-mime-types"/>
        <xsl:call-template name="eo-in-ss"/>
        <xsl:call-template name="ss-assoc-type"/>
        
        <!-- Metadata specific constraints -->
        <xsl:call-template name="doc-required-atts"/>
        <xsl:call-template name="folder-required-atts"/>
        <xsl:call-template name="ss-required-atts"/>
        <xsl:call-template name="external-classification-structure"/>
        <!-- verify docs have patient id before this call -->
        <xsl:call-template name="folder-docs-1-patient"/>
        <xsl:call-template name="verify-doc-classCode"/>
        <xsl:call-template name="verify-doc-confidentialityCode"/>
        <xsl:call-template name="verify-doc-healthCareFacilityTypeCode"/>
        <xsl:call-template name="verify-doc-formatCode"/>
        <xsl:call-template name="verify-doc-practiceSettingCode"/>
        <xsl:call-template name="verify-doc-authorDepartment"/>
        <xsl:call-template name="verify-doc-contentTypeCode"/>
        <xsl:call-template name="verify-externalIdentifiers"/>
    </xsl:variable>
    <xsl:value-of select="$results"/>
    <xsl:choose>
        <xsl:when test="$debug">
End of debug results
        </xsl:when>
        <xsl:otherwise>
            <xsl:if test="string-length($results)!=0">
Validation failed
            </xsl:if>
        </xsl:otherwise>
    </xsl:choose>
</xsl:template>

<xsl:template name="verify-externalIdentifiers">
    <xsl:if test="$debug">
Test: verify-externalIdentifiers
    </xsl:if>
    <xsl:for-each select="//ExternalIdentifier">
        <xsl:variable name="ei" select="."/>
        <xsl:variable name="scheme" select="@identificationScheme"/>
        <xsl:variable name="name" select="./Name/LocalizedString/@value"/>
        <xsl:if test="count($scheme) = 0">
ERROR: in ExternalIdentifier, has no identificationScheme
        </xsl:if>
        <xsl:if test="count($name) = 0">
ERROR: in Externalidentifier (identificationScheme=<xsl:value-of select="$scheme"/>), no name present        
        </xsl:if>
        <xsl:variable name="code" select="$externalIdentifiers//code[@value=$scheme]"/>
        <xsl:variable name="codename" select="$code/@name"/>
        <xsl:if test="count($codename)=0">
ERROR: in Externalidentifier (identificationScheme=<xsl:value-of select="$scheme"/>), name (<xsl:value-of select="$name"/>) does not match expected name (<xsl:value-of select="$codename"/>)        
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-authorDepartment">
    <xsl:if test="$debug">
Test: verify-doc-authorDepartment
    </xsl:if>
    <!-- doc.practiveSettingCode -->
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eo" select="."/>
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="$eo/Slot[@name='authorDepartment']">
            <xsl:variable name="slot" select="."/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:variable name="codes" select="$practiceSettingCodes//code[@value=$slotValue]"/>
            <xsl:if test="count($codes) = 0">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the authorDepartment <xsl:value-of select="$slotValue"/> is not valid            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-practiceSettingCode">
    <xsl:if test="$debug">
Test: verify-doc-practiceSettingCode
    </xsl:if>
    <!-- doc.practiveSettingCode -->
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="//Classification[@classificationScheme='urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead'][@classifiedObject=$eoId]">
            <xsl:variable name="class" select="."/>
            <xsl:variable name="classCode" select="./@nodeRepresentation"/>
            <xsl:variable name="codeElement" select="$practiceSettingCodes//code[@value=$classCode]"/>
            <xsl:if test="count($codeElement) = 0">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the practiceSettingCode <xsl:value-of select="$classCode"/> is not valid            
            </xsl:if>
            <xsl:variable name="codeName" select="$codeElement/@name"/>
            <xsl:if test="$codeName != $class/Name/LocalizedString/@value">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the practiceSettingCode <xsl:value-of select="$classCode"/> has the name <xsl:value-of select="$class/Name/LocalizedString/@value"/>, it should be <xsl:value-of select="$codeName"/>            
            </xsl:if>
            <xsl:variable name="slot" select="$class/Slot[@name='codingScheme']"/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:if test="$slotValue != $practiceSettingCodes/codes/@name">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the practiceSettingCode <xsl:value-of select="$classCode"/> has the coding scheme name <xsl:value-of select="$slotValue"/>, it should be <xsl:value-of select="$practiceSettingCodes/codes/@name"/>            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-healthCareFacilityTypeCode">
    <xsl:if test="$debug">
Test: verify-doc-healthCareFacilityTypeCode
    </xsl:if>
    <!-- doc.healthCareFacilityTypeCode -->
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="//Classification[@classificationScheme='urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1'][@classifiedObject=$eoId]">
            <xsl:variable name="class" select="."/>
            <xsl:variable name="classCode" select="./@nodeRepresentation"/>
            <xsl:variable name="codeElement" select="$healthCareFacilityTypeCodes//code[@value=$classCode]"/>
            <xsl:if test="count($codeElement) = 0">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the healthCareFacilityTypeCode <xsl:value-of select="$classCode"/> is not valid            
            </xsl:if>
            <xsl:variable name="codeName" select="$codeElement/@name"/>
            <xsl:if test="$codeName != $class/Name/LocalizedString/@value">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the healthCareFacilityTypeCode <xsl:value-of select="$classCode"/> has the name <xsl:value-of select="$class/Name/LocalizedString/@value"/>, it should be <xsl:value-of select="$codeName"/>            
            </xsl:if>
            <xsl:variable name="slot" select="$class/Slot[@name='codingScheme']"/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:if test="$slotValue != $healthCareFacilityTypeCodes/codes/@name">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the healthCareFacilityTypeCode <xsl:value-of select="$classCode"/> has the coding scheme name <xsl:value-of select="$slotValue"/>, it should be <xsl:value-of select="$healthCareFacilityTypeCodes/codes/@name"/>            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-formatCode">
    <xsl:if test="$debug">
Test: verify-doc-formatCode
    </xsl:if>
    <!-- doc.formatCode -->
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="//Classification[@classificationScheme='urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d'][@classifiedObject=$eoId]">
            <xsl:variable name="class" select="."/>
            <xsl:variable name="classCode" select="./@nodeRepresentation"/>
            <xsl:variable name="codeElement" select="$formatCodes//code[@value=$classCode]"/>
            <xsl:if test="count($codeElement) = 0">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the formatCode <xsl:value-of select="$classCode"/> is not valid            
            </xsl:if>
            <xsl:variable name="codeName" select="$codeElement/@name"/>
            <xsl:if test="$codeName != $class/Name/LocalizedString/@value">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the formatCode <xsl:value-of select="$classCode"/> has the name <xsl:value-of select="$class/Name/LocalizedString/@value"/>, it should be <xsl:value-of select="$codeName"/>            
            </xsl:if>
            <xsl:variable name="slot" select="$class/Slot[@name='codingScheme']"/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:if test="$slotValue != $formatCodes/codes/@name">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the formatCode <xsl:value-of select="$classCode"/> has the coding scheme name <xsl:value-of select="$slotValue"/>, it should be <xsl:value-of select="$formatCodes/codes/@name"/>            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-confidentialityCode">
    <xsl:if test="$debug">
Test: verify-doc-confidentialityCode
    </xsl:if>
    <!-- doc.confidentialityCode -->
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="//Classification[@classificationScheme='urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f'][@classifiedObject=$eoId]">
            <xsl:variable name="class" select="."/>
            <xsl:variable name="classCode" select="./@nodeRepresentation"/>
            <xsl:variable name="codeElement" select="$confidentialityCodes//code[@value=$classCode]"/>
            <xsl:if test="count($codeElement) = 0">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the confidentialityCode <xsl:value-of select="$classCode"/> is not valid            
            </xsl:if>
            <xsl:variable name="codeName" select="$codeElement/@name"/>
            <xsl:if test="$codeName != $class/Name/LocalizedString/@value">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the confidentialityCode <xsl:value-of select="$classCode"/> has the name <xsl:value-of select="$class/Name/LocalizedString/@value"/>, it should be <xsl:value-of select="$codeName"/>            
            </xsl:if>
            <xsl:variable name="slot" select="$class/Slot[@name='codingScheme']"/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:if test="$slotValue != $confidentialityCodes/codes/@name">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the confidentialityCode <xsl:value-of select="$classCode"/> has the coding scheme name <xsl:value-of select="$slotValue"/>, it should be <xsl:value-of select="$confidentialityCodes/codes/@name"/>            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-contentTypeCode">
    <xsl:if test="$debug">
Test: verify-doc-contentTypeCode
    </xsl:if>
    <!-- doc.classCode -->
    <xsl:for-each select="//RegistryPackage">
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="//Classification[@classificationScheme='urn:uuid:aa543740-bdda-424e-8c96-df4873be8500'][@classifiedObject=$eoId]">
            <xsl:variable name="class" select="."/>
            <xsl:variable name="classCode" select="./@nodeRepresentation"/>
            <xsl:variable name="codeElement" select="$contentTypeCodes//code[@value=$classCode]"/>
            <xsl:if test="count($codeElement) = 0">
ERROR: In XDSSubmissionSet, <xsl:value-of select="$eoId"/>, the contentTypeCode <xsl:value-of select="$classCode"/> is not valid            
            </xsl:if>
            <xsl:variable name="codeName" select="$codeElement/@name"/>
            <xsl:if test="$codeName != $class/Name/LocalizedString/@value">
ERROR: In XDSSubmissionSet, <xsl:value-of select="$eoId"/>, the contentTypeCode <xsl:value-of select="$classCode"/> has the name <xsl:value-of select="$class/Name/LocalizedString/@value"/>, it should be <xsl:value-of select="$codeName"/>            
            </xsl:if>
            <xsl:variable name="slot" select="$class/Slot[@name='codingScheme']"/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:if test="$slotValue != $contentTypeCodes/codes/@name">
ERROR: In XDSSubmissionSet, <xsl:value-of select="$eoId"/>, the contentTypeCode <xsl:value-of select="$classCode"/> has the coding scheme name <xsl:value-of select="$slotValue"/>, it should be <xsl:value-of select="$contentTypeCodes/codes/@name"/>            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="verify-doc-classCode">
    <xsl:if test="$debug">
Test: verify-doc-classCode
    </xsl:if>
    <!-- doc.classCode -->
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eoId" select="@id"/>
        <xsl:for-each select="//Classification[@classificationScheme='urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a'][@classifiedObject=$eoId]">
            <xsl:variable name="class" select="."/>
            <xsl:variable name="classCode" select="./@nodeRepresentation"/>
            <xsl:variable name="codeElement" select="$classCodes//code[@value=$classCode]"/>
            <xsl:if test="count($codeElement) = 0">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the classCode <xsl:value-of select="$classCode"/> is not valid            
            </xsl:if>
            <xsl:variable name="codeName" select="$codeElement/@name"/>
            <xsl:if test="$codeName != $class/Name/LocalizedString/@value">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the classCode <xsl:value-of select="$classCode"/> has the name <xsl:value-of select="$class/Name/LocalizedString/@value"/>, it should be <xsl:value-of select="$codeName"/>            
            </xsl:if>
            <xsl:variable name="slot" select="$class/Slot[@name='codingScheme']"/>
            <xsl:variable name="slotValue" select="$slot/ValueList/Value"/>
            <xsl:if test="$slotValue != $classCodes/codes/@name">
ERROR: In XDSDocumentEntry, <xsl:value-of select="$eoId"/>, the classCode <xsl:value-of select="$classCode"/> has the coding scheme name <xsl:value-of select="$slotValue"/>, it should be <xsl:value-of select="$classCodes/codes/@name"/>            
            </xsl:if>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="ss-assoc-type">
    <xsl:if test="$debug">
Test: ss-assoc-type
    </xsl:if>
    <xsl:for-each select="//RegistryPackage">
        <xsl:variable name="ss" select="."/>
        <xsl:variable name="isSS">
            <xsl:call-template name="is-ss">
                <xsl:with-param name="packageId" select="$ss/@id"/>
            </xsl:call-template>
        </xsl:variable>
        <xsl:if test="$isSS = 'true'">
            <xsl:if test="$debug">
            Found Submission Set <xsl:value-of select="$ss/@id"/>
            </xsl:if>
            <xsl:variable name="assocs" select="//Association[@associationType='HasMember'][@sourceObject=$ss/@id]"/>
            <xsl:if test="count($assocs) = 0">
ERROR: No members found for Submission Set
            </xsl:if>
            <xsl:for-each select="$assocs">
                <xsl:variable name="assoc" select="."/>
                <xsl:variable name="slots" select="$assoc/Slot"/>
                <xsl:variable name="childId" select="$assoc/@targetObject"/>
                <xsl:if test="//ExtrinsicObject[@id=$childId]">
                <xsl:if test="$debug">
                    XDSDocument <xsl:value-of select="$childId"/>
                </xsl:if>
                    <xsl:if test="count($slots) = 0">
ERROR: Association binding XDSDocument <xsl:value-of select="$assoc/@targetObject"/> to Submission Set must have SubmissionSetStatus slot             
                    </xsl:if>
                    <xsl:if test="count($slots) > 1">
ERROR: Association binding XDSDocument <xsl:value-of select="$assoc/@targetObject"/> to Submission Set must have only one slot (count = <xsl:value-of select="count($slots)"/>)           
                    </xsl:if>
                    <xsl:if test="count($slots) = 1">
                        <xsl:choose>
                        <xsl:when test="$slots/@name != 'SubmissionSetStatus'">
ERROR: Association binding XDSDocument <xsl:value-of select="$assoc/@targetObject"/> to Submission Set must have SubmissionSetStatus slot             
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:variable name="sssValue" select="$slots/ValueList/Value"/>
                            <xsl:if test="count($sssValue) = 0">
ERROR: Association binding XDSDocument <xsl:value-of select="$assoc/@targetObject"/> to Submission Set must have SubmissionSetStatus slot with valid value          
                            </xsl:if>
                            <xsl:if test="count($sssValue) > 1">
ERROR: Association binding XDSDocument <xsl:value-of select="$assoc/@targetObject"/> to Submission Set must have SubmissionSetStatus slot with single value          
                            </xsl:if>
                            <xsl:if test="count($sssValue) = 1">
                                <xsl:choose>
                                    <xsl:when test="$sssValue = 'Original'">
                                    </xsl:when>
                                    <xsl:when test="$sssValue = 'Reference'">
                                    </xsl:when>
                                    <xsl:otherwise>
ERROR: Association binding XDSDocument <xsl:value-of select="$assoc/@targetObject"/> to Submission Set must have SubmissionSetStatus slot with value=Original or value=Reference          
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:if>
                        </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:if>
            </xsl:for-each>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="is-ss">
    <xsl:param name="packageId"/>
    <xsl:variable name="classes" select="//Classification[@classifiedObject=$packageId][@classificationNode='urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd']"/>
    <xsl:value-of select="count($classes) > 0"/>
</xsl:template>
    
<xsl:template name="validate-slot">
    <xsl:param name="doc"/>
    <xsl:param name="name"/>
    <xsl:param name="multipleOk"/>
    <xsl:variable name="values" select="$doc/Slot[@name=$name]"/>
    <xsl:if test="count($values) = 0">
ERROR: Slot <xsl:value-of select="$name"/> missing on document <xsl:value-of select="$doc/@id"/>        
    </xsl:if>
    <xsl:if test="count($values) > 1">
ERROR: Duplicate <xsl:value-of select="$name"/> Slot on document <xsl:value-of select="$doc/@id"/>        
    </xsl:if>
    <xsl:variable name="valuesa" select="$doc/Slot[@name=$name]/ValueList/Value"/>
    <xsl:if test="count($valuesa) = 0">
ERROR: Value for slot <xsl:value-of select="$name"/> missing on document <xsl:value-of select="$doc/@id"/>        
    </xsl:if>
    <xsl:if test="$multipleOk = false()">
        <xsl:if test="count($valuesa) > 1">
    ERROR: Multiple values for <xsl:value-of select="$name"/> Slot on document <xsl:value-of select="$doc/@id"/>        
        </xsl:if>
    </xsl:if>
</xsl:template>

<xsl:template name="validate-external-classification">
    <xsl:param name="doc"/>
    <xsl:param name="classificationUuid"/>
    <xsl:param name="name"/>
    <xsl:param name="type"/>
    <xsl:variable name="classCodes" select="//Classification[@classifiedObject=$doc/@id][@classificationScheme=$classificationUuid]"/>
    <xsl:if test="count($classCodes) = 0">
ERROR: Classification <xsl:value-of select="$name"/> (<xsl:value-of select="$classificationUuid"/>) missing on <xsl:value-of select="$type"/>,  <xsl:value-of select="$doc/@id"/>        
    </xsl:if>
    <xsl:if test="count($classCodes) > 1">
ERROR: Multiple classifications of type <xsl:value-of select="$name"/> (<xsl:value-of select="$classificationUuid"/>) on <xsl:value-of select="$type"/>,  <xsl:value-of select="$doc/@id"/> count = <xsl:value-of select="count($classCodes)"/>        
    </xsl:if>
</xsl:template>

<xsl:template name="validate-external-id">
    <xsl:param name="doc"/>
    <xsl:param name="identificationScheme"/>
    <xsl:param name="name"/>
    <xsl:variable name="eids" select="$doc/ExternalIdentifier[@identificationScheme=$identificationScheme]"/>
    <xsl:if test="count($eids) = 0">
ERROR: On Document <xsl:value-of select="$doc/@id"/> Missing External Identifier for <xsl:value-of select="$name"/> (<xsl:value-of select="$identificationScheme"/>)    
    </xsl:if>
    <xsl:if test="count($eids) > 1">
ERROR: Multiple External Identifier for <xsl:value-of select="$name"/> (<xsl:value-of select="$identificationScheme"/>)    
    </xsl:if>
    <xsl:for-each select="$eids">
        <xsl:variable name="ename" select="./Name/LocalizedString/@value"/>
        <xsl:variable name="value" select="@value"/>
        <xsl:if test="$ename != $name">
ERROR: Name wrong on External Identifier for <xsl:value-of select="$name"/> (<xsl:value-of select="$identificationScheme"/>)
        </xsl:if>
        <xsl:if test="$value = ''">
ERROR: No value found for External Identifier <xsl:value-of select="$name"/>        
        </xsl:if>
    </xsl:for-each>
</xsl:template>


<xsl:template name="doc-required-atts">
    <xsl:if test="$debug">
Test: doc-required-atts
    </xsl:if>
    <xsl:for-each select="//ExtrinsicObject[@objectType='urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1']">
        <xsl:variable name="doc" select="."/>
        <xsl:if test="$debug">
        Document <xsl:value-of select="$doc/@id"/>
        </xsl:if>
        
        <!-- external classifications -->
        <xsl:call-template name="validate-external-classification">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="classificationUuid">urn:uuid:41a5887f-8865-4c09-adf7-e362475b143a</xsl:with-param>
            <xsl:with-param name="name">classCode</xsl:with-param>
            <xsl:with-param name="type">document</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="validate-external-classification">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="classificationUuid">urn:uuid:f4f85eac-e6cb-4883-b524-f2705394840f</xsl:with-param>
            <xsl:with-param name="name">confidentialityCode</xsl:with-param>
            <xsl:with-param name="type">document</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="validate-external-classification">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="classificationUuid">urn:uuid:a09d5840-386c-46f2-b5ad-9c3699a4309d</xsl:with-param>
            <xsl:with-param name="name">formatCode</xsl:with-param>
            <xsl:with-param name="type">document</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="validate-external-classification">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="classificationUuid">urn:uuid:f33fb8ac-18af-42cc-ae0e-ed0b0bdb91e1</xsl:with-param>
            <xsl:with-param name="name">healthCareFacilityTypeCode</xsl:with-param>
            <xsl:with-param name="type">document</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="validate-external-classification">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="classificationUuid">urn:uuid:cccf5598-8b07-4b77-a05e-ae952c785ead</xsl:with-param>
            <xsl:with-param name="name">practiceSettingCode</xsl:with-param>
            <xsl:with-param name="type">document</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="validate-external-classification">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="classificationUuid">urn:uuid:f0306f51-975f-434e-a61c-c59651d33983</xsl:with-param>
            <xsl:with-param name="name">typeCode</xsl:with-param>
            <xsl:with-param name="type">document</xsl:with-param>
        </xsl:call-template>
        <!-- slots -->
        <xsl:call-template name="validate-slot">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="name">creationTime</xsl:with-param>
            <xsl:with-param name="multipleOk" select="false()"/>
        </xsl:call-template>
        <xsl:call-template name="validate-slot">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="name">languageCode</xsl:with-param>
            <xsl:with-param name="multipleOk" select="false()"/>
        </xsl:call-template>
        <xsl:call-template name="validate-slot">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="name">sourcePatientId</xsl:with-param>
            <xsl:with-param name="multipleOk" select="false()"/>
        </xsl:call-template>
        <xsl:call-template name="validate-slot">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="name">sourcePatientInfo</xsl:with-param>
            <xsl:with-param name="multipleOk" select="true()"/>
        </xsl:call-template>
        <xsl:if test="$inputType='R'">
            <xsl:call-template name="validate-slot">
                <xsl:with-param name="doc" select="$doc"/>
                <xsl:with-param name="name">size</xsl:with-param>
                <xsl:with-param name="multipleOk" select="false()"/>
            </xsl:call-template>
            <xsl:call-template name="validate-slot">
                <xsl:with-param name="doc" select="$doc"/>
                <xsl:with-param name="name">hash</xsl:with-param>
                <xsl:with-param name="multipleOk" select="false()"/>
            </xsl:call-template>
        </xsl:if>
        <!-- external id -->
        <xsl:call-template name="validate-external-id">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="identificationScheme">urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427</xsl:with-param>
            <xsl:with-param name="name">XdsDocumentEntry.patientId</xsl:with-param>
        </xsl:call-template>
        <xsl:call-template name="validate-external-id">
            <xsl:with-param name="doc" select="$doc"/>
            <xsl:with-param name="identificationScheme">urn:uuid:2e82c1f6-a085-4c72-9da3-8640a32e42ab</xsl:with-param>
            <xsl:with-param name="name">XdsDocumentEntry.uniqueId</xsl:with-param>
        </xsl:call-template>
        <!-- external link -->
        <xsl:if test="$inputType = 'R'">
            <xsl:variable name="elAss" select="//Association[@associationType='ExternallyLinks'][@targetObject=$doc/@id]"/>
            <xsl:if test="count($elAss) = 0">
ERROR: No 'ExternallyLinks' Association found for ExtrinsicObject <xsl:value-of select="$doc/@id"/>
            </xsl:if>
            <xsl:if test="count($elAss) > 1">
ERROR: Multiple ExternalLink objects associated with ExtrinsicObject <xsl:value-of select="$doc/@id"/>
            </xsl:if>
            <xsl:variable name="elId" select="$elAss/@sourceObject"/>
            <xsl:variable name="el" select="//ExternalLink[@id = $elId]"/>
            <xsl:if test="count($el) = 0">
ERROR: No ExternalLink object with id = <xsl:value-of select="$elId"/>        
            </xsl:if>
            <xsl:variable name="uri" select="//ExternalLink[@id = $elId][@externalURI]"/>
            <xsl:if test="count($uri) = 0">
ERROR: No externalURI attribute in ExternalLink object for ExtrinsicObject <xsl:value-of select="$doc/@id"/>     
            </xsl:if>
            <xsl:if test="string-length($uri/@externalURI) = 0">
ERROR: URI missing from ExternalLink (id = <xsl:value-of select="$elId"/>)   
            </xsl:if>
            <xsl:variable name="name" select="$el/Name/LocalizedString/@value"/>
            <xsl:if test="string-length($name) = 0">
ERROR: No name in External Link (id = <xsl:value-of select="$elId"/>)        
            </xsl:if>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="folder-required-atts">
    <xsl:if test="$debug">
Test: folder-required-atts
    </xsl:if>
    <xsl:for-each select="//RegistryPackage">
        <xsl:variable name="package" select="."/>
        <xsl:if test="//Classification[@classifiedObject = $package/@id][@classificationNode = 'urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2']">
            <xsl:variable name="folder" select="$package"/>    
            <xsl:if test="$debug">
                Folder <xsl:value-of select="$folder/@id"/>
                </xsl:if>
            <!-- external classifications -->
            <xsl:call-template name="validate-external-classification">
                <xsl:with-param name="doc" select="$folder"/>
                <xsl:with-param name="classificationUuid">urn:uuid:1ba97051-7806-41a8-a48b-8fce7af683c5</xsl:with-param>
                <xsl:with-param name="name">codeList</xsl:with-param>
            <xsl:with-param name="type">folder</xsl:with-param>
            </xsl:call-template>
            <!-- slots -->
            <!-- external id -->
            <xsl:call-template name="validate-external-id">
                <xsl:with-param name="doc" select="$folder"/>
                <xsl:with-param name="identificationScheme">urn:uuid:f64ffdf0-4b97-4e06-b79f-a52b38ec2f8a</xsl:with-param>
                <xsl:with-param name="name">XDSFolder.patientId</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="validate-external-id">
                <xsl:with-param name="doc" select="$folder"/>
                <xsl:with-param name="identificationScheme">urn:uuid:75df8f67-9973-4fbe-a900-df66cefecc5a</xsl:with-param>
                <xsl:with-param name="name">XDSFolder.uniqueId</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="ss-required-atts">
    <xsl:if test="$debug">
Test: ss-required-atts
    </xsl:if>
    <xsl:for-each select="//RegistryPackage">
        <xsl:variable name="package" select="."/>
        <xsl:if test="//Classification[@classifiedObject = $package/@id][@classificationNode = 'urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd']">
            <xsl:variable name="ss" select="$package"/>    
            <xsl:if test="$debug">
                Submission Set <xsl:value-of select="$ss/@id"/>
                </xsl:if>
            <!-- external classifications -->
            <xsl:call-template name="validate-external-classification">
                <xsl:with-param name="doc" select="$ss"/>
                <xsl:with-param name="classificationUuid">urn:uuid:aa543740-bdda-424e-8c96-df4873be8500</xsl:with-param>
                <xsl:with-param name="name">contentTypeCode</xsl:with-param>
                <xsl:with-param name="type">submission set</xsl:with-param>
            </xsl:call-template>
            <!-- slots -->
            <xsl:call-template name="validate-slot">
                <xsl:with-param name="doc" select="$ss"/>
                <xsl:with-param name="name">submissionTime</xsl:with-param>
                <xsl:with-param name="multipleOk" select="false()"/>
            </xsl:call-template>
            <!-- external id -->
            <xsl:call-template name="validate-external-id">
                <xsl:with-param name="doc" select="$ss"/>
                <xsl:with-param name="identificationScheme">urn:uuid:554ac39e-e3fe-47fe-b233-965d2a147832</xsl:with-param>
                <xsl:with-param name="name">XDSSubmissionSet.sourceId</xsl:with-param>
            </xsl:call-template>
            <xsl:call-template name="validate-external-id">
                <xsl:with-param name="doc" select="$ss"/>
                <xsl:with-param name="identificationScheme">urn:uuid:96fdda7c-d067-4183-912e-bf5ee74998a8</xsl:with-param>
                <xsl:with-param name="name">XDSSubmissionSet.uniqueId</xsl:with-param>
            </xsl:call-template>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="folder-docs-1-patient">
    <xsl:if test="$debug">
Test: folder-docs-1-patient
    </xsl:if>
    <xsl:variable name="packages" select="//RegistryPackage"/>
    <xsl:if test="$debug">
    Found <xsl:value-of select="count($packages)"/> RegistryPackages
    </xsl:if>
    <xsl:for-each select="$packages">
        <xsl:variable name="packageId" select="@id"/>
        <xsl:if test="$debug">
        RegistryPackage <xsl:value-of select="$packageId"/>
        </xsl:if>
        <xsl:variable name="isFolder" select="boolean(count(//Classification[@classifiedObject=$packageId][@classificationNode='urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2']))"/>
        <xsl:if test="$isFolder">
            <xsl:variable name="folderId" select="$packageId"/>
            <xsl:if test="$debug">
            is a Folder
            </xsl:if>
            <xsl:variable name="asss" select="//Association[@associationType='HasMember'][@sourceObject=$folderId]"/>
            <xsl:if test="$debug">
            has <xsl:value-of select="count($asss)"/> documents
            </xsl:if>
            <xsl:variable name="ass1" select="$asss[1]"/>
            <xsl:variable name="doc1Id" select="$ass1/@targetObject"/>
            <xsl:if test="$debug">
            first document has id <xsl:value-of select="$doc1Id"/>
            </xsl:if>
            <xsl:variable name="eia" select="//ExternalIdentifier[../@id = $doc1Id][@identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427']"/>
            <xsl:variable name="ei1" select="$eia[1]"/>
            <xsl:variable name="patientId1" select="$ei1/@value"/>
            <xsl:if test="$debug">  
            patientId is <xsl:value-of select="$patientId1"/>
            </xsl:if>
            <xsl:if test="$debug">  <!-- guaranteed to be single by previous test -->
            Now checking entire contents of folder...
            </xsl:if>
            <!-- we have a folder and a sample patientid, now verify that all docs in folder hold same patientId -->
            <xsl:for-each select="$asss">
                <xsl:variable name="ass" select="."/>
                <xsl:variable name="doc" select="//ExtrinsicObject[@id=$ass/@targetObject]"/>
                <xsl:for-each select="$doc/ExternalIdentifier[@identificationScheme='urn:uuid:58a6f841-87b3-4a3e-92fd-a8ffeff98427']">
                    <xsl:variable name="eib" select="."/>
                    <xsl:if test="$debug">
                    patientId is <xsl:value-of select="$eib/@value"/>
                    </xsl:if>
                    <xsl:if test="$eib/@value != $patientId1">
ERROR: patientId <xsl:value-of select="$eib/@value"/> does not match others in folder <xsl:value-of select="$folderId"/>                    
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="external-classification-structure">
    <xsl:if test="$debug">
Test: external-classification-structure
    </xsl:if>
    <xsl:for-each select="//Classification[@nodeRepresentation]">
        <xsl:if test="$debug">
    Classification of <xsl:value-of select="@classifiedObject"/> by <xsl:value-of select="@classificationScheme"/> as <xsl:value-of select="@nodeRepresentation"/>
        </xsl:if>
        <xsl:variable name="hasName" select="./Name"/>
        <xsl:if test="count($hasName) != 1">
ERROR: No (or multiple) Name for Classification of <xsl:value-of select="@classifiedObject"/> by <xsl:value-of select="@classificationScheme"/> as <xsl:value-of select="@nodeRepresentation"/>
        </xsl:if>
        <xsl:variable name="name" select="./Name/LocalizedString/@value"/>
        <xsl:if test="string-length($name) = 0">
ERROR: Name has no value for Classification of <xsl:value-of select="@classifiedObject"/> by <xsl:value-of select="@classificationScheme"/> as <xsl:value-of select="@nodeRepresentation"/>
        </xsl:if>
        <xsl:variable name="codingSlot" select="./Slot"/>
        <xsl:if test="count($codingSlot) != 1">
ERROR: No (or mulitple) Slot for codingScheme on Classification of <xsl:value-of select="@classifiedObject"/> by <xsl:value-of select="@classificationScheme"/> as <xsl:value-of select="@nodeRepresentation"/>
        </xsl:if>
        <xsl:variable name="codingSlot1" select="$codingSlot[1]"/>
        <xsl:if test="$codingSlot1/@name != 'codingScheme'">
ERROR: No Slot for codingScheme (<xsl:value-of select="$codingSlot1/@name"/>) on Classification of <xsl:value-of select="@classifiedObject"/> by <xsl:value-of select="@classificationScheme"/> as <xsl:value-of select="@nodeRepresentation"/>
        </xsl:if>
        <xsl:variable name="codingName" select="$codingSlot1/ValueList/Value"/>
        <xsl:if test="string-length($codingName) = 0">
ERROR: codingScheme has no value on Classification of <xsl:value-of select="@classifiedObject"/> by <xsl:value-of select="@classificationScheme"/> as <xsl:value-of select="@nodeRepresentation"/>
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="submit-wrapper">
    <xsl:if test="$debug">
Test: submit-wrapper
    </xsl:if>
    <xsl:variable name="submit" select="/SubmitObjectsRequest"/>
    <xsl:if test="count($submit) != 1">
ERROR: No SubmitObjectsRequest found in submission    
    </xsl:if>
    <xsl:variable name="leaf" select="/SubmitObjectsRequest/LeafRegistryObjectList"/>
    <xsl:if test="count($leaf) != 1">
ERROR: No LeafRegistryObjectList found in submission    
    </xsl:if>
</xsl:template>

<xsl:template name="valid-mime-types">
    <xsl:if test="$debug">
Test: valid-mime-types
    </xsl:if>
    <xsl:variable name="mimeTypes" select="document('mimeTypes.xml')"/>
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="mimeType" select="@mimeType"/>
        <xsl:if test="not($mimeTypes//mimeType[@name=$mimeType])">
ERROR: MIME Type <xsl:value-of select="$mimeType"/> for ExtrinsicObject <xsl:value-of select="@id"/> is not acceptable in XDS.
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="package-are-folder-or-ss">
    <xsl:if test="$debug">
Test: package-are-folder-or-ss
    </xsl:if>
    <xsl:for-each select="//RegistryPackage">
        <xsl:variable name="packageId" select="@id"/>
        <xsl:variable name="ss" select="//Classification[@classificationNode='urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd'][@classifiedObject=$packageId]"/>
        <xsl:variable name="folder" select="//Classification[@classificationNode='urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2'][@classifiedObject=$packageId]"/>
        <xsl:if test="count($ss) + count($folder) != 1">
ERROR: Package <xsl:value-of select="$packageId"/> is not a Submission Set or Folder            
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="eo-are-xds-docs">
    <xsl:if test="$debug">
Test: eo-are-xds-docs
    </xsl:if>
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:if test="@objectType != 'urn:uuid:7edca82f-054d-47f2-a032-9b2a5b5186c1'">
ERROR: ExtrinsicObject <xsl:value-of select="@id"/> does not have object type of XDSDocumentEntry        
        </xsl:if>
    </xsl:for-each>
</xsl:template>

<xsl:template name="eo-in-ss">
    <xsl:variable name="result">
        <xsl:call-template name="eo-in-ss-2"/>
    </xsl:variable>
    <xsl:choose>
        <xsl:when test="contains($result,'ERROR')">
            <xsl:value-of select="$result"/>
        </xsl:when>
        <xsl:when test="$debug">
            <xsl:value-of select="$result"/>
        </xsl:when>
        <xsl:when test="contains($result,'SSFound')">
        </xsl:when>
    </xsl:choose>
    <xsl:if test="contains($result,'SSFound') = false()">
ERROR: No submission set found
    </xsl:if>
</xsl:template>

<xsl:template name="eo-in-ss-2">
    <xsl:if test="$debug">
Test: eo-in-ss
    </xsl:if>
    <xsl:for-each select="//ExtrinsicObject">
        <xsl:variable name="eoid" select="@id"/>
        <xsl:if test="$debug">
        ExtrinsicObject id = <xsl:value-of select="$eoid"/>
        </xsl:if>
        <xsl:variable name="packageAssocs" select="//Association[@targetObject=$eoid][@associationType='HasMember']"/>
        <xsl:if test="$debug">
            is contained in <xsl:value-of select="count($packageAssocs)"/> packages
        </xsl:if>
        <xsl:if test="count($packageAssocs)=0">
ERROR: ExtrinsicObject <xsl:value-of select="$eoid"/> is not contained in any packages. It must be part of a Submission Set.
        </xsl:if>
        <xsl:for-each select="$packageAssocs">
            <xsl:variable name="packageId" select="@sourceObject"/>
            <xsl:if test="$debug">
                Package <xsl:value-of select="$packageId"/>
            </xsl:if>
            <xsl:variable name="package" select="//RegistryPackage[@id=$packageId]"/>
            <xsl:if test="count($package)!=1">
ERROR: Package <xsl:value-of select="$packageId"/> referenced but missing.
            </xsl:if>
            <xsl:for-each select="$package">
                <xsl:variable name="classification" select="//Classification[@classificationNode='urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd'][@classifiedObject=$packageId]"/>
                <xsl:if test="$debug">
                    Is classified as Submission Set? <xsl:value-of select="count($classification)"/>
                </xsl:if>
                <xsl:if test="count($classification)=1">
                    SSFound
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:for-each>
</xsl:template>

<xsl:template name="in-folder-test">
    <xsl:call-template name="is-in-folder">
        <xsl:with-param name="objectId">theDocument</xsl:with-param>
        <xsl:with-param name="folderId">SubmissionSet</xsl:with-param>
    </xsl:call-template>
</xsl:template>

<xsl:template name="is-in-folder">
    <xsl:param name="objectId"/>
    <xsl:param name="folderId"/>
    
    <xsl:variable name="assn" select="//Association[@associationType='HasMember'][@targetObject=$objectId][@sourceObject=$folderId]"/>
    <xsl:value-of select="count($assn) = 1"/>
</xsl:template>

<xsl:template name="classified-as-test">
    <xsl:call-template name="is-classified-as">
        <xsl:with-param name="objectId">Folder</xsl:with-param>
        <xsl:with-param name="classificationId">urn:uuid:a54d6aa5-d40d-43f9-88c5-b4633d873bdd</xsl:with-param>
    </xsl:call-template>
</xsl:template>

<xsl:template name="is-classified-as">
    <xsl:param name="objectId"/>
    <xsl:param name="classificationId"/>
    
    <xsl:variable name="class" select="//Classification[@classifiedObject=$objectId][@classificationNode=$classificationId]"/>
    <xsl:value-of select="count($class) = 1"/>
</xsl:template>

<xsl:template name="is-folder">
    <xsl:param name="packageId"/>
    <xsl:call-template name="is-classified-as">
        <xsl:with-param name="objectId" select="$packageId"/>
        <xsl:with-param name="classificationId">urn:uuid:d9d542f3-6cc4-48b6-8870-ea235fbc94c2</xsl:with-param>
    </xsl:call-template>
</xsl:template>


</xsl:stylesheet>
