<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<xsl:stylesheet version="1.0" cda:dummy-for-xmlns="" crs:dummy-for-xmlns="" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:sch="http://www.ascc.net/xml/schematron" xmlns:cda="urn:hl7-org:v3" xmlns:crs="urn:hl7-org:crs">
<xsl:output method="xml" omit-xml-declaration="no" standalone="yes" encoding="UTF-8" indent="yes" />
<xsl:template match="*|@*" mode="schematron-get-full-path">
<xsl:apply-templates select="parent::*" mode="schematron-get-full-path" />
<xsl:text>/</xsl:text>
<xsl:if test="count(. | ../@*) = count(../@*)">@</xsl:if>
<xsl:value-of select="name()" />
<xsl:text>[</xsl:text>
<xsl:value-of select="1+count(preceding-sibling::*[name()=name(current())])" />
<xsl:text>]</xsl:text>
</xsl:template>
<xsl:template match="/">
<schematron-output title="Schematron schema for validating conformance to IMPL_CDAR2_LEVEL1REF_US_I1_2005MAY" schemaVersion="" phase="#ALL">
<ns uri="urn:hl7-org:v3" prefix="cda" /><ns uri="urn:hl7-org:crs" prefix="crs" /><active-pattern name="ClinicalDocument"><text>This schema applies to CDA Release 2.0 documents.</text><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M3" /><active-pattern name="ClinicalDocument_General_Constraints"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M4" /><active-pattern name="Telephone_Numbers"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M5" /><active-pattern name="ClinicalDocument_typeId"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M6" /><active-pattern name="ClinicalDocument_id"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M7" /><active-pattern name="ClinicalDocument_code"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M8" /><active-pattern name="ClinicalDocument_effectiveTime"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M9" /><active-pattern name="ClinicalDocument_languageCode"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M10" /><active-pattern name="ClinicalDocument_setId"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M11" /><active-pattern name="ClinicalDocument_copyTime"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M12" /><active-pattern name="recordTarget"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M13" /><active-pattern name="author"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M14" /><active-pattern name="dataEnterer"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M15" /><active-pattern name="informant"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M16" /><active-pattern name="custodian"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M17" /><active-pattern name="informationRecipient"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M18" /><active-pattern name="legalAuthenticator"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M19" /><active-pattern name="authenticator"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M20" /><active-pattern name="participant"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M21" /><active-pattern name="documentationOf"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M22" /><active-pattern name="componentOf"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M23" /><active-pattern name="Body"><xsl:apply-templates /></active-pattern><xsl:apply-templates select="/" mode="M24" />
</schematron-output>
</xsl:template>
<xsl:template match="/*" priority="3999" mode="M3">
<fired-rule id="cda-root" context="/*" role="" />
<xsl:choose>
<xsl:when test="self::cda:ClinicalDocument" />
<xsl:otherwise>
<failed-assert id="" test="self::cda:ClinicalDocument" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-1">The root of a Care Record Summary must be a <emph>ClinicalDocument</emph> element from the <emph>urn:hl7-org:v3</emph> namespace.</diagnostic>
<text>The root of a Care Record Summary must be a <emph>ClinicalDocument</emph> element from the <emph>urn:hl7-org-v3</emph> namespace.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M3" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M3" />
<xsl:template match="cda:addr" priority="4000" mode="M4">
<fired-rule id="general-addr" context="cda:addr" role="" />
<xsl:choose>
<xsl:when test="not(@nullFlavor) or (@nullFlavor and normalize-space(.) = &quot;&quot;)" />
<xsl:otherwise>
<failed-assert id="null-or-no-content" test="not(@nullFlavor) or (@nullFlavor and normalize-space(.) = &quot;&quot;)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<text>When the <emph>addr</emph> element is null, it should not have content.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(@nullFlavor) and (string-length(normalize-space(.)) &gt; 0)" />
<xsl:otherwise>
<failed-assert id="empty-implies-null" test="not(@nullFlavor) and (string-length(normalize-space(.)) &gt; 0)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<text>When the <emph>addr</emph> element is empty, it must have a value for <emph>nullFlavor</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M4" />
</xsl:template>
<xsl:template match="cda:assignedPerson" priority="3999" mode="M4">
<fired-rule id="general-person" context="cda:assignedPerson" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-38">All <emph>dataEnterer</emph> elements have an <emph>assignedEntity/assignedPerson/name</emph> element.</diagnostic>
<diagnostic id="L1-57">The <emph>name</emph> element of the <emph>assignedPerson</emph> must be present.</diagnostic>
<text>The <emph>name</emph> of an <emph>assignedPerson</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M4" />
</xsl:template>
<xsl:template match="cda:representedOrganization" priority="3998" mode="M4">
<fired-rule id="general-org" context="cda:representedOrganization" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<text>The <emph>name</emph> of a <emph>representedOrganization</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M4" />
</xsl:template>
<xsl:template match="cda:authenticator | cda:author | cda:dataEnterer | cda:legalAuthenticator" priority="3997" mode="M4">
<fired-rule id="general-time-req" context="cda:authenticator | cda:author | cda:dataEnterer | cda:legalAuthenticator" role="" />
<xsl:choose>
<xsl:when test="(not(contains(translate(cda:time/@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) and string-length(cda:time/@value) &gt; 7) or &#xA;                string-length(substring-before(translate(cda:time/@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) &gt; 7" />
<xsl:otherwise>
<failed-assert id="" test="(not(contains(translate(cda:time/@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) and string-length(cda:time/@value) &gt; 7) or string-length(substring-before(translate(cda:time/@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) &gt; 7" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-31">The <emph>author/time</emph> element must be precise at least to the day.</diagnostic>
<diagnostic id="L1-36">All <emph>dataEnterer/time</emph> elements must be precise at least to the day.</diagnostic>
<text>The <emph>time</emph> element must be precise at least to the day.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="contains(translate(cda:time/@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)" />
<xsl:otherwise>
<failed-assert id="" test="contains(translate(cda:time/@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-32">The <emph>author/time</emph> element must have a time zone.</diagnostic>
<diagnostic id="L1-37">All <emph>dataEnterer/time</emph> elements must have a time zone.</diagnostic>
<text>The <emph>time</emph> element must have a time zone.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M4" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M4" />
<xsl:template match="cda:telecom" priority="4000" mode="M5">
<fired-rule id="telcom-null-or-valued" context="cda:telecom" role="" />
<xsl:choose>
<xsl:when test="@value or @nullFlavor" />
<xsl:otherwise>
<failed-assert id="" test="@value or @nullFlavor" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<text>A telecom element must have a value or a flavor of null.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(substring(@value,1,4) = &quot;tel:&quot;) or&#xA;                string-length(&#xA;                concat(&#xA;                translate(substring(@value,5,1),&quot;+0123456789()-.&quot;,&quot;&quot;),&#xA;                translate(substring(@value,6),&quot;0123456789()-.&quot;,&quot;&quot;)  &#xA;                )&#xA;                ) = 0" />
<xsl:otherwise>
<failed-assert id="telcom-regex" test="not(substring(@value,1,4) = &quot;tel:&quot;) or string-length( concat( translate(substring(@value,5,1),&quot;+0123456789()-.&quot;,&quot;&quot;), translate(substring(@value,6),&quot;0123456789()-.&quot;,&quot;&quot;) ) ) = 0" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-2">Telephone numbers must match the regular expression pattern <emph>tel:\+?[-0-9().]+</emph> </diagnostic>
<text>Telephone numbers must match the regular expression pattern tel:\+?[-0-9().]+</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(substring(@value,1,4) = &quot;tel:&quot;) or&#xA;                string-length(&#xA;                concat(&#xA;                translate(substring(@value,5,1),&quot;+()-.&quot;,&quot;&quot;),&#xA;                translate(substring(@value,6),&quot;()-.&quot;,&quot;&quot;)    &#xA;                )&#xA;                ) &gt; 0" />
<xsl:otherwise>
<failed-assert id="telcom-has-digit" test="not(substring(@value,1,4) = &quot;tel:&quot;) or string-length( concat( translate(substring(@value,5,1),&quot;+()-.&quot;,&quot;&quot;), translate(substring(@value,6),&quot;()-.&quot;,&quot;&quot;) ) ) &gt; 0" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-3">At least one dialing digit must be present in the phone number after visual separators are removed.</diagnostic>
<text>At least one dialing digit must be present in the phone number after visual separators are removed.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M5" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M5" />
<xsl:template match="/cda:ClinicalDocument/cda:typeId" priority="4000" mode="M6">
<fired-rule id="cda-typeid" context="/cda:ClinicalDocument/cda:typeId" role="" />
<xsl:choose>
<xsl:when test="@extension = &quot;POCD_HD000040&quot;" />
<xsl:otherwise>
<failed-assert id="typeId-extension" test="@extension = &quot;POCD_HD000040&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-4">The <emph>extension</emph> attribute of the <emph>typeId</emph> element must be <emph>POCD_HD000040</emph> .</diagnostic>
<text>The <emph>extension</emph> attribute of the <emph>typeId</emph> element must be <emph>POCD_HD000040</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M6" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M6" />
<xsl:template match="/cda:ClinicalDocument/cda:id[contains(@root, '-')]" priority="4000" mode="M7">
<fired-rule id="cda-id-uuid" context="/cda:ClinicalDocument/cda:id[contains(@root, '-')]" role="" />
<xsl:choose>
<xsl:when test="string-length(@root) = 37" />
<xsl:otherwise>
<failed-assert id="" test="string-length(@root) = 37" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>A properly formatted UUID has only 37 characters.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="translate(substring(@root, 1, 8),'ABCDEFabcdef0123456789','') = ''" />
<xsl:otherwise>
<failed-assert id="" test="translate(substring(@root, 1, 8),'ABCDEFabcdef0123456789','') = ''" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>The first four data bytes of the UUID should be represented using hexidecimal digits ([A-Fa-f0-9]).</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="translate(substring(@root, 10, 4),'ABCDEFabcdef0123456789','') = ''" />
<xsl:otherwise>
<failed-assert id="" test="translate(substring(@root, 10, 4),'ABCDEFabcdef0123456789','') = ''" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>The fifth and sixth data bytes of the UUID should be represented using hexidecimal digits ([A-Fa-f0-9]).</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="translate(substring(@root, 15, 4),'ABCDEFabcdef0123456789','') = ''" />
<xsl:otherwise>
<failed-assert id="" test="translate(substring(@root, 15, 4),'ABCDEFabcdef0123456789','') = ''" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>The seventh and eighth data bytes of the UUID should be represented using hexidecimal digits ([A-Fa-f0-9]).</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="translate(substring(@root, 20, 4),'ABCDEFabcdef0123456789','') = ''" />
<xsl:otherwise>
<failed-assert id="" test="translate(substring(@root, 20, 4),'ABCDEFabcdef0123456789','') = ''" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>The ninth and tenth data bytes of the UUID should be represented using hexidecimal digits ([A-Fa-f0-9]).</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="translate(substring(@root, 25, 12),'ABCDEFabcdef0123456789','') = ''" />
<xsl:otherwise>
<failed-assert id="" test="translate(substring(@root, 25, 12),'ABCDEFabcdef0123456789','') = ''" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>The eleventh through sixteenth data bytes of the UUID should be represented using hexidecimal digits ([A-Fa-f0-9]).</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="substring(@root, 9, 1) = '-'" />
<xsl:otherwise>
<failed-assert id="" test="substring(@root, 9, 1) = '-'" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>A hyphen should separate the first four data bytes from the remainder of the UUID.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="substring(@root, 14, 1) = '-'" />
<xsl:otherwise>
<failed-assert id="" test="substring(@root, 14, 1) = '-'" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>A hyphen should separate the fifth and sixth data byte from the remainder of the UUID.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="substring(@root, 19, 1) = '-'" />
<xsl:otherwise>
<failed-assert id="" test="substring(@root, 19, 1) = '-'" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>A hyphen should separate the seventh and eighth data byte from the remainder of the UUID.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="substring(@root, 24, 1) = '-'" />
<xsl:otherwise>
<failed-assert id="" test="substring(@root, 24, 1) = '-'" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-6">UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph> , where each X is a character from the set [A-Fa-f0-9].</diagnostic>
<text>A hyphen should separate the ninth and tenth data byte from the remainder of the UUID.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M7" />
</xsl:template>
<xsl:template match="/cda:ClinicalDocument/cda:id[contains(@root, '.')]" priority="3999" mode="M7">
<fired-rule id="cda-id-oid" context="/cda:ClinicalDocument/cda:id[contains(@root, '.')]" role="" />
<xsl:choose>
<xsl:when test="translate(@root, '0123456789.', '') = ''" />
<xsl:otherwise>
<failed-assert id="" test="translate(@root, '0123456789.', '') = ''" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-7">OIDs must be represented in dotted decimal notation, where each decimal number is either 0, or starts with a non-zero digit. More formally, an OID must be in the form ([0-2])(.([1-9][0-9]*|0))+.</diagnostic>
<text>Characters that are not in the set 0-9 or . are not present in a valid OID.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(substring(@root, 1, 1) = '.') and not(substring(@root, string-length(@root), 1) = '.')" />
<xsl:otherwise>
<failed-assert id="" test="not(substring(@root, 1, 1) = '.') and not(substring(@root, string-length(@root), 1) = '.')" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-7">OIDs must be represented in dotted decimal notation, where each decimal number is either 0, or starts with a non-zero digit. More formally, an OID must be in the form ([0-2])(.([1-9][0-9]*|0))+.</diagnostic>
<text>The first and last characters of an OID must be a digit.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(contains(@root,'..'))" />
<xsl:otherwise>
<failed-assert id="" test="not(contains(@root,'..'))" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<diagnostic id="L1-7">OIDs must be represented in dotted decimal notation, where each decimal number is either 0, or starts with a non-zero digit. More formally, an OID must be in the form ([0-2])(.([1-9][0-9]*|0))+.</diagnostic>
<text>A properly formatted OID should not contain two . characters without any intervening digits</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="string-length(@root) &lt; 65" />
<xsl:otherwise>
<failed-assert id="" test="string-length(@root) &lt; 65" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-8">OIDs must be no more than 64 characters in length.</diagnostic>
<text>An OID must be shorter than 65 characters.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M7" />
</xsl:template>
<xsl:template match="cda:id[not(contains(@root,&quot;.&quot;) or contains(@root,&quot;-&quot;))]" priority="3998" mode="M7">
<fired-rule id="cda-id" context="cda:id[not(contains(@root,&quot;.&quot;) or contains(@root,&quot;-&quot;))]" role="" />
<xsl:choose>
<xsl:when test="false()" />
<xsl:otherwise>
<failed-assert id="" test="false()" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-5">The root attribute of the id element must be a syntactically correct UUID or OID.</diagnostic>
<text>The root attribute of the id element must be a syntactically correct UUID or OID.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M7" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M7" />
<xsl:template match="/cda:ClinicalDocument/cda:code" priority="4000" mode="M8">
<fired-rule id="clinical-document-code" context="/cda:ClinicalDocument/cda:code" role="" />
<xsl:choose>
<xsl:when test="document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()/@code]" />
<xsl:otherwise>
<failed-assert id="" test="document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()/@code]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-9">The value of <emph>/ClinicalDocument/code/@code</emph> must come from the appropriate LOINC code subset.</diagnostic>
<text>The value of <emph>/ClinicalDocument/code/@code</emph> must come from the appropriate LOINC subset.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="@codeSystem = &quot;2.16.840.1.113883.6.1&quot;" />
<xsl:otherwise>
<failed-assert id="" test="@codeSystem = &quot;2.16.840.1.113883.6.1&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-10">The value of <emph>/ClinicalDocument/code/@codeSysem</emph> is the OID for LOINC.</diagnostic>
<text>The value of <emph>/ClinicalDocument/code/@codeSystem</emph> must be 2.16.840.1.113883.6.1</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="count(@codeSystemName) = 0 or @codeSystemName=&quot;LOINC&quot;" />
<xsl:otherwise>
<failed-assert id="" test="count(@codeSystemName) = 0 or @codeSystemName=&quot;LOINC&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-11">The value of <emph>/ClinicalDocument/code/@codeSystemName</emph> , if present is <emph>LOINC</emph> .</diagnostic>
<text>The value of <emph>/ClinicalDocument/code/@codeSystemName</emph> must be <emph>LOINC</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="true()" />
<xsl:otherwise>
<failed-assert id="" test="true()" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-12">If pre-coordinated document type codes are used, the role code and function code for the author must not conflict with the document type code.</diagnostic>
<text>TBD: If pre-coordinated document type codes are used, the role code and function code for the author must not conflict with the document type code.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M8" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M8" />
<xsl:template match="/cda:ClinicalDocument/cda:effectiveTime" priority="4000" mode="M9">
<fired-rule id="cda-effectiveTime" context="/cda:ClinicalDocument/cda:effectiveTime" role="" />
<xsl:choose>
<xsl:when test="(not(contains(translate(@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) and string-length(@value) &gt; 7) or &#xA;                string-length(substring-before(translate(@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) &gt; 7" />
<xsl:otherwise>
<failed-assert id="" test="(not(contains(translate(@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) and string-length(@value) &gt; 7) or string-length(substring-before(translate(@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)) &gt; 7" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-13">The <emph>effectiveTime</emph> element must be precise at least to the day.</diagnostic>
<text>The <emph>effectiveTime</emph> element must be precise at least to the day.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="contains(translate(@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)" />
<xsl:otherwise>
<failed-assert id="" test="contains(translate(@value,&quot;+-&quot;,&quot;Z&quot;),&quot;Z&quot;)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-14">The <emph>effectiveTime</emph> element must have a time zone.</diagnostic>
<text>The <emph>effectiveTime</emph> element must have a time zone.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M9" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M9" />
<xsl:template match="/cda:ClinicalDocument" priority="4000" mode="M10">
<fired-rule id="cda-languageCode" context="/cda:ClinicalDocument" role="" />
<xsl:choose>
<xsl:when test="cda:languageCode" />
<xsl:otherwise>
<failed-assert id="" test="cda:languageCode" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-15">The <emph>languageCode</emph> element must be present.</diagnostic>
<text>The <emph>languageCode</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M10" />
</xsl:template>
<xsl:template match="/cda:ClinicalDocument/cda:languageCode" priority="3999" mode="M10">
<fired-rule id="cda-languageCode-format" context="/cda:ClinicalDocument/cda:languageCode" role="" />
<xsl:choose>
<xsl:when test="(string-length(@code) = 5 and substring(@code,3,1) = &quot;-&quot;) or string-length(@code) = 2" />
<xsl:otherwise>
<failed-assert id="" test="(string-length(@code) = 5 and substring(@code,3,1) = &quot;-&quot;) or string-length(@code) = 2" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-16">The language code must be in the form <emph>nn</emph> , or <emph>nn-CC</emph> .</diagnostic>
<text>The language code must be in the form <emph>nn</emph> , or <emph>nn-CC</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="substring(@code,1,2) = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;ISO639-1&quot;]/code/@value" />
<xsl:otherwise>
<failed-assert id="" test="substring(@code,1,2) = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;ISO639-1&quot;]/code/@value" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-17">The <emph>nn</emph> portion must be a legal ISO-639-1 language code in lower case.</diagnostic>
<text>The language must be a legal ISO-639-1 language code in lower case.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="string-length(@code) = 2 or substring(@code,4,2) = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;ISO3166-1&quot;]/code/@value" />
<xsl:otherwise>
<failed-assert id="" test="string-length(@code) = 2 or substring(@code,4,2) = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;ISO3166-1&quot;]/code/@value" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-18">The <emph>CC</emph> portion, if present must be an ISO-3166 country code in upper case.</diagnostic>
<text>The country code portion, if present must be an ISO-3166 country code in upper case.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M10" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M10" />
<xsl:template match="/cda:ClinicalDocument/cda:setId" priority="4000" mode="M11">
<fired-rule id="" context="/cda:ClinicalDocument/cda:setId" role="" />
<xsl:choose>
<xsl:when test="/cda:ClinicalDocument/cda:versionNumber" />
<xsl:otherwise>
<failed-assert id="" test="/cda:ClinicalDocument/cda:versionNumber" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-19">When <emph>setId</emph> is present, then <emph>versionNumber</emph> must be present.</diagnostic>
<text>When <emph>setId</emph> is present, then <emph>versionNumber</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="@root != ../cda:id/@root or @extension != ../cda:id/@extension" />
<xsl:otherwise>
<failed-assert id="" test="@root != ../cda:id/@root or @extension != ../cda:id/@extension" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-20">Either there is no <emph>setId</emph> , or then <emph>extension</emph> and/or <emph>root</emph> of <emph>setId</emph> and <emph>id</emph> are different.</diagnostic>
<text>Either there is no <emph>setId</emph> , or the <emph>extension</emph> and/or <emph>root</emph> of <emph>setId</emph> and <emph>id</emph> are different.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M11" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M11" />
<xsl:template match="/cda:ClinicalDocument/cda:copyTime" priority="4000" mode="M12">
<fired-rule id="cda-copyTime" context="/cda:ClinicalDocument/cda:copyTime" role="" />
<xsl:choose>
<xsl:when test="false()" />
<xsl:otherwise>
<failed-assert id="" test="false()" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-21">No <emph>copyTime</emph> element is present in the <emph>ClinicalDocument</emph> .</diagnostic>
<text>No <emph>copyTime</emph> element is present in the <emph>ClinicalDocument</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M12" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M12" />
<xsl:template match="cda:recordTarget" priority="4000" mode="M13">
<fired-rule id="cda-recordTarget" context="cda:recordTarget" role="" />
<xsl:choose>
<xsl:when test="count(cda:patientRole) &gt; 0" />
<xsl:otherwise>
<failed-assert id="" test="count(cda:patientRole) &gt; 0" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-22">At least one <emph>recordTarget/patientRole</emph> element exists.</diagnostic>
<text>At least one <emph>recordTarget/patientRole</emph> element exists.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:patientRole/cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:patientRole/cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-23">The <emph>recordTarget/patientRole</emph> element has an <emph>addr</emph> element.</diagnostic>
<text>The <emph>recordTarget/patientRole</emph> element has an <emph>addr</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:patientRole/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:patientRole/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-24">The <emph>recordTarget/patientRole</emph> element has a <emph>telecom</emph> element that represents a contact phone number.</diagnostic>
<text>The <emph>recordTarget/patientRole</emph> element has a <emph>telecom</emph> element that represents a contact phone number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:patientRole/cda:patient/cda:birthTime" />
<xsl:otherwise>
<failed-assert id="" test="cda:patientRole/cda:patient/cda:birthTime" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-25">A <emph>patient/birthTime</emph> element is present.</diagnostic>
<text>A <emph>patient/birthTime</emph> element is present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:patientRole/cda:patient/cda:administrativeGenderCode" />
<xsl:otherwise>
<failed-assert id="" test="cda:patientRole/cda:patient/cda:administrativeGenderCode" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-26">A <emph>patient/administrativeGenderCode</emph> element is present.</diagnostic>
<text>A <emph>patient/administrativeGenderCode</emph> element is present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M13" />
</xsl:template>
<xsl:template match="cda:recordTarget/cda:patientRole/cda:providerOrganization" priority="3999" mode="M13">
<fired-rule id="cda-providerOrg" context="cda:recordTarget/cda:patientRole/cda:providerOrganization" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-27">All <emph>providerOrganization</emph> elements have a <emph>name</emph> element.</diagnostic>
<text>All <emph>providerOrganization</emph> elements have a <emph>name</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-28">All <emph>providerOrganization</emph> elements have an <emph>addr </emph> element.</diagnostic>
<text>All <emph>providerOrganization</emph> elements have an <emph>addr </emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-29">All <emph>providerOrganization</emph> elements have a <emph>telecom </emph> element that is a telephone contact number.</diagnostic>
<text>All <emph>providerOrganization</emph> elements have a <emph>telecom </emph> element that is a telephone contact number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M13" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M13" />
<xsl:template match="cda:author" priority="4000" mode="M14">
<fired-rule id="cda-author" context="cda:author" role="" />
<xsl:choose>
<xsl:when test="cda:time" />
<xsl:otherwise>
<failed-assert id="" test="cda:time" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-30">The <emph>author/time</emph> element must be present.</diagnostic>
<text>The <emph>author/time</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M14" />
</xsl:template>
<xsl:template match="cda:assignedAuthor" priority="3999" mode="M14">
<fired-rule id="cda-assignedAuthor" context="cda:assignedAuthor" role="" />
<xsl:choose>
<xsl:when test="cda:id" />
<xsl:otherwise>
<failed-assert id="" test="cda:id" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-33">The <emph>assignedAuthor/id</emph> element must be present.</diagnostic>
<text>The <emph>assignedAuthor/id</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-34">All <emph>assignedAuthors</emph> have at least one <emph>telecom</emph> element that contains a contact phone number.</diagnostic>
<text>All <emph>assignedAuthors</emph> have at least one <emph>telecom</emph> element that contains a contact phone number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M14" />
</xsl:template>
<xsl:template match="cda:assignedAuthoringDevice" priority="3998" mode="M14">
<fired-rule id="cda-assignedAuthoringDevice" context="cda:assignedAuthoringDevice" role="" />
<xsl:choose>
<xsl:when test="cda:softwareName" />
<xsl:otherwise>
<failed-assert id="" test="cda:softwareName" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-35">When <emph>assignedAuthoringDevice</emph> is present, the <emph>softwareName</emph> element must be present.</diagnostic>
<text>When <emph>assignedAuthoringDevice</emph> is present, the <emph>softwareName</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="string-length(normalize-space(cda:softwareName)) &gt; 0" />
<xsl:otherwise>
<failed-assert id="" test="string-length(normalize-space(cda:softwareName)) &gt; 0" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-35">When <emph>assignedAuthoringDevice</emph> is present, the <emph>softwareName</emph> element must be present.</diagnostic>
<text>When <emph>assignedAuthoringDevice/softwareName</emph> must have a value.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M14" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M14" />
<xsl:template match="cda:dataEnterer" priority="4000" mode="M15">
<fired-rule id="cda-dataEnterer" context="cda:dataEnterer" role="" />
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:assignedPerson/cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:assignedPerson/cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-38">All <emph>dataEnterer</emph> elements have an <emph>assignedEntity/assignedPerson/name</emph> element.</diagnostic>
<text>All <emph>dataEnterer</emph> elements have an <emph>assignedEntity/assignedPerson/name</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M15" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M15" />
<xsl:template match="cda:informant" priority="4000" mode="M16">
<fired-rule id="cda-informant" context="cda:informant" role="" />
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:assignedPerson/cda:name | cda:relatedEntity/cda:relatedPerson/cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:assignedPerson/cda:name | cda:relatedEntity/cda:relatedPerson/cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-39">An <emph>informant</emph> must have either an <emph>assignedEntity/assignedPerson/name</emph> element, or a <emph>relatedEntity/relatedPerson/name</emph> element.</diagnostic>
<text>An <emph>informant</emph> must have either an <emph>assignedEntity/assignedPerson/name</emph> element, or a <emph>relatedEntity/relatedPerson/name</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(descendant::crs:asPatientRelationship)" />
<xsl:otherwise>
<failed-assert id="" test="not(descendant::crs:asPatientRelationship)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-40">An <emph>informant</emph> should not have any <emph>assignedEntity/assignedPerson/crs:asPatientRelationship </emph> elements, or <emph>relatedEntity/relatedPerson/ crs:asPatientRelationship</emph> elements.</diagnostic>
<text>An <emph>informant</emph> should not have any <emph>assignedEntity/assignedPerson/crs:asPatientRelationship </emph> elements, or <emph>relatedEntity/relatedPerson/ crs:asPatientRelationship</emph> elements.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M16" />
</xsl:template>
<xsl:template match="cda:informant/cda:relatedEntity[@classCode = &quot;PRS&quot;]" priority="3999" mode="M16">
<fired-rule id="cda-relatedEntity-PRS" context="cda:informant/cda:relatedEntity[@classCode = &quot;PRS&quot;]" role="" />
<xsl:choose>
<xsl:when test="cda:code/@codeSystem = &quot;2.16.840.1.113883.5.111&quot; and cda:code/@code = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;PersonalRelationshipRoleType&quot;]/code/@value" />
<xsl:otherwise>
<failed-assert id="" test="cda:code/@codeSystem = &quot;2.16.840.1.113883.5.111&quot; and cda:code/@code = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;PersonalRelationshipRoleType&quot;]/code/@value" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-41">When <emph>relatedEntity/@classCode</emph> is <emph>PRS</emph> , values in <emph>relatedEntity/code</emph> shall come from the</diagnostic>
<text>When <emph>relatedEntity/@classCode</emph> is <emph>PRS</emph> , values in <emph>relatedEntity/code</emph> shall come from the <emph>PersonalRelationshipRoleType</emph> vocabulary.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M16" />
</xsl:template>
<xsl:template match="cda:informant/cda:relatedEntity[@classCode = &quot;CON&quot;]" priority="3998" mode="M16">
<fired-rule id="cda-relatedEntity-CON" context="cda:informant/cda:relatedEntity[@classCode = &quot;CON&quot;]" role="" />
<xsl:choose>
<xsl:when test="not(cda:code)" />
<xsl:otherwise>
<failed-assert id="" test="not(cda:code)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-42">When <emph>relatedEntity/@classCode</emph> is <emph>CON</emph> , <emph>relatedEntity/code</emph> shall not be present.</diagnostic>
<text>When <emph>relatedEntity/@classCode</emph> is <emph>CON</emph> , <emph>relatedEntity/code</emph> shall not be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M16" />
</xsl:template>
<xsl:template match="cda:informant/cda:relatedEntity[@classCode = &quot;PROV&quot;]/cda:code" priority="3997" mode="M16">
<fired-rule id="cda-relatedEntity-PROV" context="cda:informant/cda:relatedEntity[@classCode = &quot;PROV&quot;]/cda:code" role="" />
<xsl:choose>
<xsl:when test="@codeSystem = &quot;2.16.840.1.113883.6.96&quot; and @code = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;HealthcareProfessionals&quot;]/code/@value" />
<xsl:otherwise>
<failed-assert id="" test="@codeSystem = &quot;2.16.840.1.113883.6.96&quot; and @code = document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;HealthcareProfessionals&quot;]/code/@value" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-43">When <emph>relatedEntity/@classCode</emph> is <emph>PROV</emph> , and <emph>relatedEntity/code</emph> is present, the value shall come from SNOMED CT.</diagnostic>
<text>When <emph>relatedEntity/@classCode</emph> is <emph>PROV</emph> , <emph>relatedEntity/code</emph> shall be descended from the <emph>healthcare professional</emph> concept (223366009) of SNOMED CT.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M16" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M16" />
<xsl:template match="cda:custodian" priority="4000" mode="M17">
<fired-rule id="cda-custodian" context="cda:custodian" role="" />
<xsl:choose>
<xsl:when test="cda:assignedCustodian/cda:representedCustodianOrganization/cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedCustodian/cda:representedCustodianOrganization/cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-44">A <emph>custodian/assignedCustodian/representedCustodianOrganization/name</emph> element must be present.</diagnostic>
<text>A <emph>custodian/assignedCustodian/representedCustodianOrganization/name</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedCustodian/cda:representedCustodianOrganization/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedCustodian/cda:representedCustodianOrganization/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-45">A <emph>custodian/assignedCustodian/representedCustodianOrganization/telecom</emph> element must be present that contains a telephone contact.</diagnostic>
<text>A <emph>custodian/assignedCustodian/representedCustodianOrganization/telecom</emph> element must be present that contains a telephone contact.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedCustodian/cda:representedCustodianOrganization/cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedCustodian/cda:representedCustodianOrganization/cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-46">A <emph>custodian/assignedCustodian/representedCustodianOrganization/addr</emph> element must be present.</diagnostic>
<text>A <emph>custodian/assignedCustodian/representedCustodianOrganization/addr</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M17" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M17" />
<xsl:template match="cda:intendedRecipient/cda:informationRecipient" priority="4000" mode="M18">
<fired-rule id="cda-intendedRecipient" context="cda:intendedRecipient/cda:informationRecipient" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-48">All <emph>informationRecipient</emph> elements have a <emph>name</emph> element.</diagnostic>
<text>All <emph>informationRecipient</emph> elements have a <emph>name</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M18" />
</xsl:template>
<xsl:template match="cda:receivedOrganization" priority="3999" mode="M18">
<fired-rule id="cda-receivedOrganization" context="cda:receivedOrganization" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-49">All <emph>receivedOrganization</emph> elements have a <emph>name</emph> element.</diagnostic>
<text>All <emph>receivedOrganization</emph> elements have a <emph>name</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M18" />
</xsl:template>
<xsl:template match="cda:ClinicalDocument/cda:informationRecipient" priority="3998" mode="M18">
<fired-rule id="cda-informationRecipient" context="cda:ClinicalDocument/cda:informationRecipient" role="" />
<xsl:choose>
<xsl:when test="cda:intendedRecipient/cda:informationRecipient | cda:intendedRecipient/cda:receivedOrganization" />
<xsl:otherwise>
<failed-assert id="" test="cda:intendedRecipient/cda:informationRecipient | cda:intendedRecipient/cda:receivedOrganization" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-47">At least one of <emph>intendedRecipient/informationRecipient</emph> or <emph>intendedRecipient/recievedOrganization</emph> must be present.</diagnostic>
<text>At least one of <emph>intendedRecipient/informationRecipient</emph> or <emph>intendedRecipient/receivedOrganization</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:intendedRecipient/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:intendedRecipient/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-50">All <emph>intendedRecipient</emph> elements have at least one <emph>telecom</emph> element that contains a contact phone number.</diagnostic>
<text>All <emph>intendedRecipient</emph> elements have at least one <emph>telecom</emph> element that contains a contact phone number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:intendedRecipient/cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:intendedRecipient/cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-51">All <emph>intendedRecipient</emph> elements have an <emph>addr</emph> element.</diagnostic>
<text>All <emph>intendedRecipient</emph> elements have an <emph>addr</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M18" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M18" />
<xsl:template match="cda:legalAuthenticator" priority="4000" mode="M19">
<fired-rule id="cda-legalAuthenticator" context="cda:legalAuthenticator" role="" />
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-52">All <emph>legalAuthenticator/assignedEntity</emph> elements have at least one <emph>telecom</emph> element that contains a contact phone number.</diagnostic>
<text>All <emph>legalAuthenticator/assignedEntity</emph> elements have at least one <emph>telecom</emph> element that contains a contact phone number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-53">All <emph>legalAuthenticator/assignedEntity</emph> elements have an <emph>addr</emph> element.</diagnostic>
<text>All <emph>legalAuthenticator/assignedEntity</emph> elements have an <emph>addr</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:assignedPerson" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:assignedPerson" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-54">The <emph>legalAuthenticator/assignedEntity/assignedPerson</emph> element must be present.</diagnostic>
<text>The <emph>legalAuthenticator/assignedEntity/assignedPerson</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M19" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M19" />
<xsl:template match="cda:authenticator" priority="4000" mode="M20">
<fired-rule id="cda-authenticator" context="cda:authenticator" role="" />
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-55">The <emph>assignedEntity/addr</emph> element of the <emph>authenticator</emph> element must be present.</diagnostic>
<text>The <emph>assignedEntity/addr</emph> element of the <emph>authenticator</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-56">An <emph>assignedEntity/telecom</emph> element must be present whose value contains a contact phone number.</diagnostic>
<text>An <emph>assignedEntity/telecom</emph> element must be present whose value contains a contact phone number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedEntity/cda:assignedPerson/cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedEntity/cda:assignedPerson/cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-57">The <emph>name</emph> element of the <emph>assignedPerson</emph> must be present.</diagnostic>
<text>The <emph>name</emph> element of the <emph>assignedPerson</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M20" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M20" />
<xsl:template match="cda:participant/cda:associatedEntity" priority="4000" mode="M21">
<fired-rule id="cda-associatedEntity" context="cda:participant/cda:associatedEntity" role="" />
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-58">The <emph>associatedEntity/addr</emph> element must be present.</diagnostic>
<text>The <emph>associatedEntity/addr </emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-59">A <emph>associatedEntity/telecom</emph> element must be present that contains a contact phone number for the participant.</diagnostic>
<text>A <emph>associatedEntity/telecom</emph> element must be present that contains a contact phone number for the participant.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(../@typeCode = &quot;IND&quot;) or &#xA;                @classCode = &quot;PRS&quot; or @classCode = &quot;NOK&quot; or @classCode = &quot;ECON&quot; or @classCode = &quot;GUAR&quot;" />
<xsl:otherwise>
<failed-assert id="" test="not(../@typeCode = &quot;IND&quot;) or @classCode = &quot;PRS&quot; or @classCode = &quot;NOK&quot; or @classCode = &quot;ECON&quot; or @classCode = &quot;GUAR&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-63">When <emph>participant/@typeCode</emph> is <emph>IND</emph> , <emph>associatedEntity/@classCode</emph> must be <emph>PRS, NOK, ECON or GUAR.</emph> </diagnostic>
<text>When <emph>participant/@typeCode</emph> is <emph>IND</emph> , <emph>associatedEntity/@classCode</emph> must be <emph>PRS, NOK, ECON or GUAR.</emph> </text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(../@typeCode = &quot;IND&quot;) or &#xA;                @classCode = &quot;GUAR&quot; or &#xA;                count(cda:code/@code[.=document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;PersonalRelationshipRoleType&quot;]/code/@value]) = 1" />
<xsl:otherwise>
<failed-assert id="" test="not(../@typeCode = &quot;IND&quot;) or @classCode = &quot;GUAR&quot; or count(cda:code/@code[.=document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;PersonalRelationshipRoleType&quot;]/code/@value]) = 1" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-64">When <emph>associatedEntity/@classCode</emph> is <emph>PRS, NOK or ECON</emph> then <emph>associatedEntity/code</emph> must be present having a value drawn from the</diagnostic>
<text>When <emph>associatedEntity/@classCode</emph> is <emph>PRS, NOK or ECON</emph> then <emph>associatedEntity/code</emph> must be present having a value drawn from the <emph>PersonalRelationshipRoleType</emph> domain.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(../@typeCode=&quot;HLD&quot;) or @classCode = &quot;POLHOLD&quot;" />
<xsl:otherwise>
<failed-assert id="" test="not(../@typeCode=&quot;HLD&quot;) or @classCode = &quot;POLHOLD&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-65">When <emph>participant/@typeCode</emph> is <emph>HLD</emph> , <emph>associatedEntity/@classCode</emph> must be <emph>POLHOLD</emph> .</diagnostic>
<text>When <emph>participant/@typeCode</emph> is <emph>HLD</emph> , <emph>associatedEntity/@classCode</emph> must be <emph>POLHOLD</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(../@typeCode=&quot;HLD&quot;) or cda:scopingOrganization" />
<xsl:otherwise>
<failed-assert id="" test="not(../@typeCode=&quot;HLD&quot;) or cda:scopingOrganization" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-66">When <emph>participant/@typeCode</emph> is <emph>HLD</emph> , <emph>associatedEntity/scopingOrganization </emph> must be present.</diagnostic>
<text>When <emph>participant/@typeCode</emph> is <emph>HLD</emph> , <emph>associatedEntity/scopingOrganization</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M21" />
</xsl:template>
<xsl:template match="cda:scopingOrganization" priority="3999" mode="M21">
<fired-rule id="cda-scopingOrg" context="cda:scopingOrganization" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-60">The <emph>name</emph> element must be present in a <emph>scopingOrganization</emph> element.</diagnostic>
<text>The <emph>name</emph> element must be present in a <emph>scopingOrganization</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-61">The <emph>addr</emph> element must be present in a <emph>scopingOrganization</emph> element.</diagnostic>
<text>The <emph>addr</emph> element must be present in a <emph>scopingOrganization</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-62">A <emph>telecom</emph> element must be present that contains a contact phone number in a <emph>scopingOrganization</emph> element.</diagnostic>
<text>A <emph>telecom</emph> element must be present that contains a contact phone number in a <emph>scopingOrganization</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M21" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M21" />
<xsl:template match="cda:ClinicalDocument" priority="4000" mode="M22">
<fired-rule id="cda-documentationOf" context="cda:ClinicalDocument" role="" />
<xsl:choose>
<xsl:when test="count(cda:documentationOf) = 1" />
<xsl:otherwise>
<failed-assert id="" test="count(cda:documentationOf) = 1" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-67">Only one <emph>ClinicalDocument/documentationOf</emph> element must be present.</diagnostic>
<text>Only one <emph>ClinicalDocument/documentationOf</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M22" />
</xsl:template>
<xsl:template match="cda:serviceEvent" priority="3999" mode="M22">
<fired-rule id="cda-serviceEvent" context="cda:serviceEvent" role="" />
<xsl:choose>
<xsl:when test="cda:effectiveTime" />
<xsl:otherwise>
<failed-assert id="" test="cda:effectiveTime" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-68">The <emph>effectiveTime</emph> element of the <emph>serviceEvent</emph> element must be present.</diagnostic>
<text>The <emph>effectiveTime</emph> element of the <emph>serviceEvent</emph> element must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M22" />
</xsl:template>
<xsl:template match="cda:serviceEvent/cda:effectiveTime" priority="3998" mode="M22">
<fired-rule id="cda-effectiveTime" context="cda:serviceEvent/cda:effectiveTime" role="" />
<xsl:choose>
<xsl:when test="count(cda:low) = 1" />
<xsl:otherwise>
<failed-assert id="" test="count(cda:low) = 1" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-69">The <emph>effectiveTime</emph> element must contain only one <emph>low</emph> element. The <emph>low</emph> element is unrestricted with respect to precision or time zone.</diagnostic>
<text>The <emph>effectiveTime</emph> element must contain only one <emph>low</emph> element. The <emph>low</emph> element is unrestricted with respect to precision or time zone.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="count(cda:high) = 1" />
<xsl:otherwise>
<failed-assert id="" test="count(cda:high) = 1" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-70">The <emph>effectiveTime</emph> element must contain only one <emph>high</emph> element. The <emph>high</emph> element is unrestricted with respect to precision or time zone.</diagnostic>
<text>The <emph>effectiveTime</emph> element must contain only one <emph>high</emph> element. The <emph>high</emph> element is unrestricted with respect to precision or time zone.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M22" />
</xsl:template>
<xsl:template match="cda:performer/cda:assignedEntity" priority="3997" mode="M22">
<fired-rule id="cda-performer" context="cda:performer/cda:assignedEntity" role="" />
<xsl:choose>
<xsl:when test="count(cda:code) = 1 and cda:code/@codeSystem=&quot;2.16.840.1.113883.6.96&quot;" />
<xsl:otherwise>
<failed-assert id="" test="count(cda:code) = 1 and cda:code/@codeSystem=&quot;2.16.840.1.113883.6.96&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-71">The <emph>performer/assigneEntity/code</emph> if present must have a value drawn from the SNOMED CT <emph>healthcare professional </emph> subtype hierarchy.</diagnostic>
<text>The <emph>performer/assigneEntity/code</emph> if present must have a value drawn from the SNOMED CT <emph>healthcare professional </emph> subtype hierarchy.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedPerson | cda:representedOrganization" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedPerson | cda:representedOrganization" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-72">Every <emph>performer/assignedEntity</emph> element has at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> .</diagnostic>
<text>Every <emph>performer/assignedEntity</emph> element has at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-73">All <emph>performer/assignedEntity</emph> elements have an <emph>addr </emph> element.</diagnostic>
<text>All <emph>performer/assignedEntity</emph> elements have an <emph>addr</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-74">All <emph>performer/assignedEntity</emph> elements have a <emph>telecom </emph> element that is a telephone contact number.</diagnostic>
<text>All <emph>performer/assignedEntity</emph> elements have a <emph>telecom </emph> element that is a telephone contact number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M22" />
</xsl:template>
<xsl:template match="cda:performer/cda:assignedEntity/cda:assignedPerson" priority="3996" mode="M22">
<fired-rule id="cda-performer-person" context="cda:performer/cda:assignedEntity/cda:assignedPerson" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-75">All <emph>assignedPerson</emph> elements of <emph>performer/assignedEntity</emph> must have a <emph>name</emph> element.</diagnostic>
<text>All <emph>assignedPerson</emph> elements of <emph>performer/assignedEntity</emph> must have a <emph>name</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M22" />
</xsl:template>
<xsl:template match="cda:performer/cda:assignedEntity/cda:representedOrganization" priority="3995" mode="M22">
<fired-rule id="cda-performer-org" context="cda:performer/cda:assignedEntity/cda:representedOrganization" role="" />
<xsl:choose>
<xsl:when test="cda:name" />
<xsl:otherwise>
<failed-assert id="" test="cda:name" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-76">The <emph>name</emph> element must be present in a <emph>performer/assignedEntity/representedOrganization</emph> element.</diagnostic>
<text>The <emph>name</emph> element must be present in a <emph>performer/assignedEntity/representedOrganization</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-77">The <emph>addr</emph> element must be present in a <emph>performer/assignedEntity/representedOrganization</emph> element.</diagnostic>
<text>The <emph>addr</emph> element must be present in a <emph>performer/assignedEntity/representedOrganization</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-78">A <emph>telecom</emph> element must be present that contains a contact phone number in a <emph>performer/assignedEntity/representedOrganization</emph> element.</diagnostic>
<text>A <emph>telecom</emph> element must be present that contains a contact phone number in a <emph>performer/assignedEntity/representedOrganization</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M22" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M22" />
<xsl:template match="/cda:ClinicalDocument/cda:code" priority="4000" mode="M23">
<fired-rule id="cda-componentOf" context="/cda:ClinicalDocument/cda:code" role="" />
<xsl:choose>
<xsl:when test="/cda:ClinicalDocument/cda:componentOf or &#xA;                not(document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()]/@displayName = &quot;DISCHARGE SUMMARIZATION NOTE&quot; or document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()/@code]/@displayName = &quot;TRANSFER SUMMARIZATION NOTE&quot;)" />
<xsl:otherwise>
<failed-assert id="" test="/cda:ClinicalDocument/cda:componentOf or not(document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()]/@displayName = &quot;DISCHARGE SUMMARIZATION NOTE&quot; or document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()/@code]/@displayName = &quot;TRANSFER SUMMARIZATION NOTE&quot;)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-79">If the Care Record Summary is a Discharge or Transfer Summarization, then the <emph>componentOf</emph> element is required.</diagnostic>
<text>If the Care Record Summary is a Discharge or Transfer Summarization, then the <emph>componentOf</emph> element is required.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M23" />
</xsl:template>
<xsl:template match="cda:encompassingEncounter" priority="3999" mode="M23">
<fired-rule id="cda-encompassingEncounter" context="cda:encompassingEncounter" role="" />
<xsl:choose>
<xsl:when test="cda:id" />
<xsl:otherwise>
<failed-assert id="" test="cda:id" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-80">The <emph>encompassingEncounter</emph> element must have an <emph>id</emph> element.</diagnostic>
<text>The <emph>encompassingEncounter</emph> element must have an <emph>id</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:effectiveTime" />
<xsl:otherwise>
<failed-assert id="" test="cda:effectiveTime" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-81">The <emph>encompassingEncounter</emph> element must have an <emph>effectiveTime</emph> element.</diagnostic>
<text>The <emph>encompassingEncounter</emph> element must have an <emph>effectiveTime</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:dischargeDispositionCode or not(document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()/ancestor::cda:ClinicalDocument/cda:code/@code]/@displayName = &quot;DISCHARGE SUMMARIZATION NOTE&quot;)" />
<xsl:otherwise>
<failed-assert id="" test="cda:dischargeDispositionCode or not(document(&quot;voc.xml&quot;)/systems/system[@codeSystemName=&quot;LOINC&quot;]/code[@value = current()/ancestor::cda:ClinicalDocument/cda:code/@code]/@displayName = &quot;DISCHARGE SUMMARIZATION NOTE&quot;)" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-82">If <emph>ClinicalDocument/code/@value</emph> represents a Discharge Summarization Node, then <emph>dischargeDispositionCode</emph> must be present.</diagnostic>
<text>If <emph>ClinicalDocument/code/@value</emph> represents a Discharge Summarization Note, then <emph>dischargeDispositionCode</emph> must be present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M23" />
</xsl:template>
<xsl:template match="cda:encounterParticipant/cda:assignedEntity" priority="3998" mode="M23">
<fired-rule id="cda-encounterParticipant" context="cda:encounterParticipant/cda:assignedEntity" role="" />
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-83">The <emph>encounterParticipant/assignedEntity</emph> element must have an <emph>addr</emph> element.</diagnostic>
<text>The <emph>encounterParticipant/assignedEntity</emph> element must have an <emph>addr</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-84">The <emph>encounterParticipant/assignedEntity</emph> element must have a <emph>telecom</emph> element that represents a telephone contact number.</diagnostic>
<text>The <emph>encounterParticipant/assignedEntity</emph> element must have a <emph>telecom</emph> element that represents a telephone contact number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedPerson | cda:representedOrganization" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedPerson | cda:representedOrganization" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-85">The <emph>encounterParticipant/assignedEntity</emph> element must have at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.</diagnostic>
<text>The <emph>encounterParticipant/assignedEntity</emph> element must have at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M23" />
</xsl:template>
<xsl:template match="cda:responsibleParty/cda:assignedEntity" priority="3997" mode="M23">
<fired-rule id="cda-responsibleParty" context="cda:responsibleParty/cda:assignedEntity" role="" />
<xsl:choose>
<xsl:when test="cda:addr" />
<xsl:otherwise>
<failed-assert id="" test="cda:addr" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-86">The <emph>responsibleParty/assignedEntity</emph> element must have an <emph>addr</emph> element.</diagnostic>
<text>The <emph>responsibleParty/assignedEntity</emph> element must have an <emph>addr</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" />
<xsl:otherwise>
<failed-assert id="" test="cda:telecom[boolean(@nullFlavor) or substring-before(@value,&quot;:&quot;) = &quot;tel&quot;]" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-87">The <emph>responsibleParty/assignedEntity</emph> element must have a <emph>telecom</emph> element that represents a telephone contact number.</diagnostic>
<text>The <emph>responsibleParty/assignedEntity</emph> element must have a <emph>telecom</emph> element that represents a telephone contact number.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:assignedPerson | cda:representedOrganization" />
<xsl:otherwise>
<failed-assert id="" test="cda:assignedPerson | cda:representedOrganization" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L1-88">The <emph>responsibleParty/assignedEntity</emph> element must have at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.</diagnostic>
<text>The <emph>responsibleParty/assignedEntity</emph> element must have at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M23" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M23" />
<xsl:template match="cda:section" priority="4000" mode="M24">
<fired-rule id="sectionRequirements" context="cda:section" role="" />
<xsl:choose>
<xsl:when test="cda:code" />
<xsl:otherwise>
<failed-assert id="" test="cda:code" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-1">A <emph>cda:section</emph> element must have a <emph>cda:code</emph> element.</diagnostic>
<text>A <emph>cda:section</emph> element must have a <emph>cda:code</emph> element.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="cda:text | cda:component" />
<xsl:otherwise>
<failed-assert id="" test="cda:text | cda:component" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-2">A <emph>cda:section</emph> must contain at least one <emph>cda:text </emph> element or one or more <emph>cda:component</emph> elements.</diagnostic>
<text>A <emph>cda:section</emph> must contain at least one <emph>cda:text </emph> element or one or more <emph>cda:component</emph> elements.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="string-length(string(cda:text)) &gt; 0" />
<xsl:otherwise>
<failed-assert id="" test="string-length(string(cda:text)) &gt; 0" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-3">All <emph>cda:text</emph> or <emph>cda:component</emph> elements must contain content.</diagnostic>
<text>All <emph>cda:text</emph> or <emph>cda:component</emph> elements must contain content.</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M24" />
</xsl:template>
<xsl:template match="/cda:ClinicalDocument/cda:component/cda:structuredBody" priority="3999" mode="M24">
<fired-rule id="RequiredSections" context="/cda:ClinicalDocument/cda:component/cda:structuredBody" role="" />
<xsl:choose>
<xsl:when test="/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot; or descendant-or-self::cda:section/cda:code/@code=&quot;11535-2&quot;" />
<xsl:otherwise>
<failed-assert id="" test="/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot; or descendant-or-self::cda:section/cda:code/@code=&quot;11535-2&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-4">A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>11535-2</emph> .</diagnostic>
<text>A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>11535-2</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot;) or descendant-or-self::cda:section/cda:code/@code=&quot;11450-4&quot;" />
<xsl:otherwise>
<failed-assert id="" test="not(/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot;) or descendant-or-self::cda:section/cda:code/@code=&quot;11450-4&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-5">A Summary of Episode note that is not a discharge or transfer summary must include a <emph>section</emph> element whose code is <emph>11450-4</emph> .</diagnostic>
<text>A Summary of Episode note that is not a discharge or transfer summary must include a <emph>section</emph> element whose code is <emph>11450-4</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="descendant-or-self::cda:section/cda:code/@code=&quot;10155-0&quot;" />
<xsl:otherwise>
<failed-assert id="" test="descendant-or-self::cda:section/cda:code/@code=&quot;10155-0&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-6">A <emph>section</emph> must be present with a <emph>code</emph> value of <emph>10155-0</emph> .</diagnostic>
<text>A <emph>section</emph> must be present with a <emph>code</emph> value of <emph>10155-0</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot; or descendant-or-self::cda:section/cda:code/@code=&quot;10183-2&quot;" />
<xsl:otherwise>
<failed-assert id="" test="/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot; or descendant-or-self::cda:section/cda:code/@code=&quot;10183-2&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-7">A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>10183-2</emph> .</diagnostic>
<text>A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>10183-2</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="not(/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot;) or descendant-or-self::cda:section/cda:code/@code=&quot;10160-0&quot;" />
<xsl:otherwise>
<failed-assert id="" test="not(/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot;) or descendant-or-self::cda:section/cda:code/@code=&quot;10160-0&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-8">A Summary of Episode note that is not also a discharge or transfer summary must include a <emph>section</emph> element whose code is <emph>10160-0</emph> .</diagnostic>
<text>A Summary of Episode note that is not also a discharge or transfer summary must include a <emph>section</emph> element whose code is <emph>10160-0</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:choose>
<xsl:when test="/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot; or descendant-or-self::cda:section/cda:code/@code=&quot;8648-8&quot;" />
<xsl:otherwise>
<failed-assert id="" test="/cda:ClinicalDocument/cda:code/@code = &quot;34133-9&quot; or descendant-or-self::cda:section/cda:code/@code=&quot;8648-8&quot;" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-9">A level 2 conforming Care Record Summary that is a discharge or transfer summary shall contain a <emph>section</emph> with the <emph>code</emph> value of <emph>8648-8</emph> .</diagnostic>
<text>A level 2 conforming Care Record Summary that is a discharge or transfer summary shall contain a <emph>section</emph> with the <emph>code</emph> value of <emph>8648-8</emph> .</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M24" />
</xsl:template>
<xsl:template match="cda:section/cda:code[@code=&quot;X-RFVCC&quot;]" priority="3998" mode="M24">
<fired-rule id="RequiredSections" context="cda:section/cda:code[@code=&quot;X-RFVCC&quot;]" role="" />
<xsl:choose>
<xsl:when test="count(//cda:section/cda:code[@code=&quot;29299-5&quot; or @code=&quot;10154-3&quot;])=0" />
<xsl:otherwise>
<failed-assert id="" test="count(//cda:section/cda:code[@code=&quot;29299-5&quot; or @code=&quot;10154-3&quot;])=0" role="">
<xsl:attribute name="location"><xsl:apply-templates select="." mode="schematron-get-full-path" /></xsl:attribute>
<diagnostic id="L2-9">A level 2 conforming Care Record Summary that is a discharge or transfer summary shall contain a <emph>section</emph> with the <emph>code</emph> value of <emph>8648-8</emph> .</diagnostic>
<text>A level 2 conforming Care Record Summary that contains a <emph>section</emph> with a <emph>code</emph> value of <emph>X-RFVCC</emph> (REASON FOR VISIT/CHIEF COMPLAINT) shall not contain sections with a code value of <emph>29299-5</emph> (REASON FOR VISIT) or <emph>10154-3</emph> (CHIEF COMPLAINT).</text>
</failed-assert>
</xsl:otherwise>
</xsl:choose>
<xsl:apply-templates mode="M24" />
</xsl:template>
<xsl:template match="text()" priority="-1" mode="M24" />
<xsl:template match="text()" priority="-1" />
</xsl:stylesheet>
