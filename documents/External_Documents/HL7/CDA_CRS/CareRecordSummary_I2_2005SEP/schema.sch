<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<!DOCTYPE schema [
<!-- 
Replace baseURI below with a reference to the published Implementation Guide HTML.
-->
<!ENTITY baseURI "">
]>
<schema xmlns="http://www.ascc.net/xml/schematron" xmlns:cda="urn:hl7-org:v3" xmlns:crs='urn:hl7-org:crs'>
    
    <title>Schematron schema for validating conformance to IMPL_CDAR2_LEVEL1REF_US_I1_2005MAY</title>
    <ns prefix="cda" uri="urn:hl7-org:v3" />
    <ns prefix="crs" uri="urn:hl7-org:crs" />
    <pattern name='ClinicalDocument' see='&baseURI;#ClinicalDocument'>
        <p>This schema applies to CDA Release 2.0 documents.</p>
        <rule id='cda-root' context='/*'>
            <assert diagnostics="L1-1" test='self::cda:ClinicalDocument'>
                The root of a Care Record Summary must be a <emph>ClinicalDocument</emph> element from 
                the <emph>urn:hl7-org-v3</emph> namespace.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_General_Constraints' see='&baseURI;#ClinicalDocument_General_Constraints'>
        <rule id='general-addr' context='cda:addr'>
            <assert id='null-or-no-content' test='not(@nullFlavor) or (@nullFlavor and normalize-space(.) = "")'>
                When the <emph>addr</emph> element is null, it should not have content.
            </assert>
            <assert id='empty-implies-null' test='not(@nullFlavor) and (string-length(normalize-space(.)) &gt; 0)'>
                When the <emph>addr</emph> element is empty, it must have a value for 
                <emph>nullFlavor</emph>.
            </assert>
        </rule>
        <rule id='general-person' context='cda:assignedPerson'>
            <assert diagnostics="L1-38 L1-57" test='cda:name'>
                The <emph>name</emph> of an <emph>assignedPerson</emph> must be present.
            </assert>
        </rule>
        <rule id='general-org' context='cda:representedOrganization'>
            <assert test='cda:name'>
                The <emph>name</emph> of a <emph>representedOrganization</emph> must be present.
            </assert>
        </rule>
        <rule id='general-time-req' context='cda:authenticator | cda:author | cda:dataEnterer | cda:legalAuthenticator'>
            <assert diagnostics='L1-31 L1-36' 
                test='(not(contains(translate(cda:time/@value,"+-","Z"),"Z")) and string-length(cda:time/@value) &gt; 7) or 
                string-length(substring-before(translate(cda:time/@value,"+-","Z"),"Z")) &gt; 7'
                >
                The <emph>time</emph> element must be precise at least to the day.
            </assert>
            <assert diagnostics='L1-32 L1-37' test='contains(translate(cda:time/@value,"+-","Z"),"Z")'>
                The <emph>time</emph> element must have a time zone.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='Telephone_Numbers' see='&baseURI;#Telephone_Numbers'>
        <rule id='telcom-null-or-valued' context='cda:telecom'>
            <assert test='@value or @nullFlavor'>
                A telecom element must have a value or a flavor of null.
            </assert>
            <assert id='telcom-regex' diagnostics='L1-2' 
                test='not(substring(@value,1,4) = "tel:") or
                string-length(
                concat(
                translate(substring(@value,5,1),"+0123456789()-.",""),
                translate(substring(@value,6),"0123456789()-.","")  
                )
                ) = 0'
                >
                Telephone numbers must match the regular expression pattern tel:\+?[-0-9().]+
            </assert>
            <assert id='telcom-has-digit' diagnostics='L1-3' 
                test='not(substring(@value,1,4) = "tel:") or
                string-length(
                concat(
                translate(substring(@value,5,1),"+()-.",""),
                translate(substring(@value,6),"()-.","")    
                )
                ) &gt; 0'
                >
                At least one dialing digit must be present in the phone number after visual separators 
                are removed.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_typeId' see='&baseURI;#ClinicalDocument_typeId'>
        <rule id='cda-typeid' context="/cda:ClinicalDocument/cda:typeId">
            <assert id='typeId-extension' diagnostics='L1-4' test='@extension = "POCD_HD000040"'>
                The <emph>extension</emph> attribute of the <emph>typeId</emph> element must be <emph>POCD_HD000040</emph>.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_id' see='&baseURI;#ClinicalDocument_id'>
        <rule id='cda-id-uuid' context="/cda:ClinicalDocument/cda:id[contains(@root, '-')]">
            <assert diagnostics='L1-5 L1-6' test="string-length(@root) = 37" >
                A properly formatted UUID has only 37 characters.
            </assert>
            <assert diagnostics='L1-5 L1-6' 
                test="translate(substring(@root, 1, 8),'ABCDEFabcdef0123456789','') = ''">
                The first four data bytes of the UUID should be represented using hexidecimal 
                digits ([A-Fa-f0-9]).
            </assert>
            <assert diagnostics='L1-5 L1-6' 
                test="translate(substring(@root, 10, 4),'ABCDEFabcdef0123456789','') = ''" >
                The fifth and sixth data bytes of the UUID should be represented using hexidecimal 
                digits ([A-Fa-f0-9]).
            </assert>
            <assert diagnostics='L1-5 L1-6' 
                test="translate(substring(@root, 15, 4),'ABCDEFabcdef0123456789','') = ''">
                The seventh and eighth data bytes of the UUID should be represented using hexidecimal 
                digits ([A-Fa-f0-9]).
            </assert>
            <assert diagnostics='L1-5 L1-6' 
                test="translate(substring(@root, 20, 4),'ABCDEFabcdef0123456789','') = ''">
                The ninth and tenth data bytes of the UUID should be represented using hexidecimal 
                digits ([A-Fa-f0-9]).
            </assert>
            <assert diagnostics='L1-5 L1-6' 
                test="translate(substring(@root, 25, 12),'ABCDEFabcdef0123456789','') = ''">
                The eleventh through sixteenth data bytes of the UUID should be represented using 
                hexidecimal digits ([A-Fa-f0-9]).
            </assert>
            <assert diagnostics='L1-5 L1-6' test="substring(@root, 9, 1) = '-'">
                A hyphen should separate the first four data bytes from the remainder of the UUID.
            </assert>
            <assert diagnostics='L1-5 L1-6' test="substring(@root, 14, 1) = '-'">
                A hyphen should separate the fifth and sixth data byte from the remainder of the UUID.
            </assert>
            <assert diagnostics='L1-5 L1-6' test="substring(@root, 19, 1) = '-'">
                A hyphen should separate the seventh and eighth data byte from the remainder of the UUID.
            </assert>
            <assert diagnostics='L1-5 L1-6' test="substring(@root, 24, 1) = '-'">
                A hyphen should separate the ninth and tenth data byte from the remainder of the UUID.
            </assert>
        </rule>
        <rule id='cda-id-oid' context="/cda:ClinicalDocument/cda:id[contains(@root, '.')]" >
            <assert test="translate(@root, '0123456789.', '') = ''" diagnostics='L1-5 L1-7'>
                Characters that are not in the set 0-9 or . are not present in a valid OID.
            </assert>
            <assert diagnostics='L1-5 L1-7' 
                test="not(substring(@root, 1, 1) = '.') and not(substring(@root, string-length(@root), 1) = '.')" >
                The first and last characters of an OID must be a digit.
            </assert>
            <assert diagnostics='L1-5 L1-7' test="not(contains(@root,'..'))">
                A properly formatted OID should not contain two . characters without any 
                intervening digits
            </assert>
            <assert diagnostics='L1-8' test="string-length(@root) &lt; 65">
                An OID must be shorter than 65 characters.
            </assert>
        </rule>
        <rule id='cda-id' context='cda:id[not(contains(@root,".") or contains(@root,"-"))]'>
            <assert diagnostics='L1-5' test='false()'>
                The root attribute of the id element must be a syntactically correct UUID or OID.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_code' see='&baseURI;#ClinicalDocument_code'>
        <rule id='clinical-document-code' context='/cda:ClinicalDocument/cda:code'>
            <assert diagnostics='L1-9'
                test='document("voc.xml")/systems/system[@codeSystemName="LOINC"]/code[@value = current()/@code]'>
                The value of <emph>/ClinicalDocument/code/@code</emph> must come from the 
                appropriate LOINC subset.
            </assert>
            <assert test='@codeSystem = "2.16.840.1.113883.6.1"' diagnostics='L1-10'>
                The value of <emph>/ClinicalDocument/code/@codeSystem</emph> must be 2.16.840.1.113883.6.1
            </assert>
            <assert test='count(@codeSystemName) = 0 or @codeSystemName="LOINC"' diagnostics='L1-11'>
                The value of <emph>/ClinicalDocument/code/@codeSystemName</emph> must be 
                <emph>LOINC</emph>.
            </assert>
            <assert diagnostics="L1-12" test='true()'>
                TBD: If pre-coordinated document type codes are used, the role code and function code for the 
                author must not conflict with the document type code.
            </assert>
        </rule>
    </pattern>  
    
    <pattern name='ClinicalDocument_effectiveTime' see='&baseURI;#ClinicalDocument_effectiveTime'>
        <rule id='cda-effectiveTime' context='/cda:ClinicalDocument/cda:effectiveTime'>
            <assert diagnostics='L1-13' 
                test='(not(contains(translate(@value,"+-","Z"),"Z")) and string-length(@value) &gt; 7) or 
                string-length(substring-before(translate(@value,"+-","Z"),"Z")) &gt; 7'
                >
                The <emph>effectiveTime</emph> element must be precise at least to the day.
            </assert>
            <assert diagnostics="L1-14" test='contains(translate(@value,"+-","Z"),"Z")'>
                The <emph>effectiveTime</emph> element must have a time zone.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_languageCode' see='&baseURI;#ClinicalDocument_languageCode'>
        <rule id='cda-languageCode' context='/cda:ClinicalDocument'>
            <assert diagnostics='L1-15' test='cda:languageCode'>
                The <emph>languageCode</emph> element must be present.
            </assert>
        </rule>
        <rule id='cda-languageCode-format' context='/cda:ClinicalDocument/cda:languageCode'>
            <assert diagnostics='L1-16'
                test='(string-length(@code) = 5 and substring(@code,3,1) = "-") or string-length(@code) = 2' >
                The language code must be in the form <emph>nn</emph>, or <emph>nn-CC</emph>.
            </assert>
            <assert diagnostics='L1-17'
                test='substring(@code,1,2) = document("voc.xml")/systems/system[@codeSystemName="ISO639-1"]/code/@value' >
                The language must be a legal ISO-639-1 language code in lower case.
            </assert>
            <assert diagnostics='L1-18'
                test='string-length(@code) = 2 or substring(@code,4,2) = document("voc.xml")/systems/system[@codeSystemName="ISO3166-1"]/code/@value' >
                The country code portion, if present must be an ISO-3166 country code in upper case.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_setId' see='&baseURI;#ClinicalDocument_setId'>
        <rule context='/cda:ClinicalDocument/cda:setId'>
            <assert diagnostics="L1-19" test='/cda:ClinicalDocument/cda:versionNumber'>
                When <emph>setId</emph> is present, then <emph>versionNumber</emph> must be present.
            </assert>
            <assert diagnostics="L1-20" test='@root != ../cda:id/@root or @extension != ../cda:id/@extension' >
                Either there is no <emph>setId</emph>, or the <emph>extension</emph> and/or 
                <emph>root</emph> of <emph>setId</emph> and <emph>id</emph> are different.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='ClinicalDocument_copyTime' see='&baseURI;#ClinicalDocument_copyTime'>
        <rule id='cda-copyTime' context='/cda:ClinicalDocument/cda:copyTime'>
            <assert test='false()' diagnostics='L1-21'>
                No <emph>copyTime</emph> element is present in the <emph>ClinicalDocument</emph>.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='recordTarget' see='&baseURI;#recordTarget'>
        <rule id='cda-recordTarget' context='cda:recordTarget'>
            <assert diagnostics="L1-22" test='count(cda:patientRole) &gt; 0'>
                At least one <emph>recordTarget/patientRole</emph> element exists.
            </assert>
            <assert diagnostics="L1-23" test='cda:patientRole/cda:addr'>
                The <emph>recordTarget/patientRole</emph> element has an <emph>addr</emph> element.
            </assert>
            <assert diagnostics="L1-24" 
                test='cda:patientRole/cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                The <emph>recordTarget/patientRole</emph> element has a <emph>telecom</emph> element that represents a contact phone number.
            </assert>
            <assert diagnostics="L1-25" test='cda:patientRole/cda:patient/cda:birthTime'>
                A <emph>patient/birthTime</emph> element is present.
            </assert>
            <assert diagnostics="L1-26" test='cda:patientRole/cda:patient/cda:administrativeGenderCode'>
                A <emph>patient/administrativeGenderCode</emph> element is present.
            </assert>
        </rule>
        <rule id='cda-providerOrg' context='cda:recordTarget/cda:patientRole/cda:providerOrganization'>
            <assert diagnostics="L1-27" test='cda:name'>
                All <emph>providerOrganization</emph> elements have a <emph>name</emph> element.
            </assert>
            <assert diagnostics="L1-28" test='cda:addr'>
                All <emph>providerOrganization</emph> elements have an <emph>addr </emph>element.
            </assert>
            <assert diagnostics="L1-29" test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                All <emph>providerOrganization</emph> elements have a <emph>telecom </emph>element 
                that is a telephone contact number.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='author' see='&baseURI;#author'>
        <rule id='cda-author' context='cda:author'>
            <assert diagnostics="L1-30" test='cda:time'>
                The <emph>author/time</emph> element must be present.
            </assert>
        </rule>
        <rule id='cda-assignedAuthor' context='cda:assignedAuthor'>
            <assert diagnostics="L1-33" test='cda:id'>
                The <emph>assignedAuthor/id</emph> element must be present.
            </assert>
            <assert diagnostics="L1-34" test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                All <emph>assignedAuthors</emph> have at least one <emph>telecom</emph> element that 
                contains a contact phone number.
            </assert>
        </rule>
        <rule id='cda-assignedAuthoringDevice' context='cda:assignedAuthoringDevice'>
            <assert diagnostics="L1-35" test='cda:softwareName'>
                When <emph>assignedAuthoringDevice</emph> is present, the <emph>softwareName</emph> 
                element must be present.
            </assert>
            <assert diagnostics="L1-35" test='string-length(normalize-space(cda:softwareName)) &gt; 0'>
                When <emph>assignedAuthoringDevice/softwareName</emph> must have a value.
            </assert>
        </rule>
    </pattern>
    <pattern name='dataEnterer' see='&baseURI;#dataEnterer'>
        <rule id='cda-dataEnterer' context='cda:dataEnterer'>
            <assert diagnostics="L1-38" test='cda:assignedEntity/cda:assignedPerson/cda:name'>
                All <emph>dataEnterer</emph> elements have an <emph>assignedEntity/assignedPerson/name</emph> element.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='informant' see='&baseURI;#informant'>
        <rule id='cda-informant' context='cda:informant'>
            <assert diagnostics="L1-39" 
                test='cda:assignedEntity/cda:assignedPerson/cda:name | cda:relatedEntity/cda:relatedPerson/cda:name'>
                An <emph>informant</emph> must have either an <emph>assignedEntity/assignedPerson/name</emph> 
                element, or a <emph>relatedEntity/relatedPerson/name</emph> element.
            </assert>
            <assert test='not(descendant::crs:asPatientRelationship)' diagnostics='L1-40'>
                An <emph>informant</emph> should not have any <emph>assignedEntity/assignedPerson/crs:asPatientRelationship </emph>elements, or <emph>relatedEntity/relatedPerson/ crs:asPatientRelationship</emph> elements.
            </assert>
        </rule>
        <rule id='cda-relatedEntity-PRS' context='cda:informant/cda:relatedEntity[@classCode = "PRS"]'>
            <assert diagnostics="L1-41" 
                test='cda:code/@codeSystem = "2.16.840.1.113883.5.111" and cda:code/@code = document("voc.xml")/systems/system[@codeSystemName="PersonalRelationshipRoleType"]/code/@value'>
                When <emph>relatedEntity/@classCode</emph> is <emph>PRS</emph>, values in 
                <emph>relatedEntity/code</emph> shall come from the 
                <emph>PersonalRelationshipRoleType</emph> vocabulary.
            </assert>
        </rule>
        <rule id='cda-relatedEntity-CON' context='cda:informant/cda:relatedEntity[@classCode = "CON"]'>
            <assert diagnostics="L1-42" test='not(cda:code)'>
                When <emph>relatedEntity/@classCode</emph> is <emph>CON</emph>, 
                <emph>relatedEntity/code</emph> shall not be present.
            </assert>
        </rule>
        <rule id='cda-relatedEntity-PROV' context='cda:informant/cda:relatedEntity[@classCode = "PROV"]/cda:code'>
            <assert diagnostics="L1-43" 
                test='@codeSystem = "2.16.840.1.113883.6.96" and @code = document("voc.xml")/systems/system[@codeSystemName="HealthcareProfessionals"]/code/@value'>
                When <emph>relatedEntity/@classCode</emph> is <emph>PROV</emph>, 
                <emph>relatedEntity/code</emph> shall be descended from the 
                <emph>healthcare professional</emph> concept (223366009) of SNOMED CT.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='custodian' see='&baseURI;#custodian'>
        <rule id='cda-custodian' context='cda:custodian'>
            <assert diagnostics="L1-44" 
                test='cda:assignedCustodian/cda:representedCustodianOrganization/cda:name'>
                A <emph>custodian/assignedCustodian/representedCustodianOrganization/name</emph> element 
                must be present.
            </assert>
            <assert diagnostics="L1-45" 
                test='cda:assignedCustodian/cda:representedCustodianOrganization/cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                A <emph>custodian/assignedCustodian/representedCustodianOrganization/telecom</emph> 
                element must be present that contains a telephone contact.
            </assert>
            <assert diagnostics="L1-46" 
                test='cda:assignedCustodian/cda:representedCustodianOrganization/cda:addr'>
                A <emph>custodian/assignedCustodian/representedCustodianOrganization/addr</emph> element 
                must be present.
            </assert>
        </rule>
    </pattern>
    <pattern name='informationRecipient' see='&baseURI;#informationRecipient'>
        <rule id='cda-intendedRecipient' context='cda:intendedRecipient/cda:informationRecipient'>
            <assert diagnostics="L1-48" test='cda:name'>
                All <emph>informationRecipient</emph> elements have a <emph>name</emph> element.
            </assert>
        </rule>
        <rule id='cda-receivedOrganization' context='cda:receivedOrganization'>
            <assert diagnostics="L1-49" test='cda:name'>
                All <emph>receivedOrganization</emph> elements have a <emph>name</emph> element.
            </assert>
        </rule>
        <rule id='cda-informationRecipient' context='cda:ClinicalDocument/cda:informationRecipient'>
            <assert diagnostics="L1-47" 
                test='cda:intendedRecipient/cda:informationRecipient | cda:intendedRecipient/cda:receivedOrganization'>
                At least one of <emph>intendedRecipient/informationRecipient</emph> or 
                <emph>intendedRecipient/receivedOrganization</emph> 
                must be present.
            </assert>
            <assert diagnostics="L1-50" 
                test='cda:intendedRecipient/cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                All <emph>intendedRecipient</emph> elements have at least one <emph>telecom</emph> 
                element that contains a contact phone number.
            </assert>
            <assert diagnostics="L1-51" test='cda:intendedRecipient/cda:addr'>
                All <emph>intendedRecipient</emph> elements have an <emph>addr</emph> element.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='legalAuthenticator' see='&baseURI;#legalAuthenticator'>
        <rule id='cda-legalAuthenticator' context='cda:legalAuthenticator'>
            <assert diagnostics="L1-52"
                test='cda:assignedEntity/cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                All <emph>legalAuthenticator/assignedEntity</emph> elements have at least one 
                <emph>telecom</emph> element 
                that contains a contact phone number.
            </assert>
            <assert diagnostics="L1-53" test='cda:assignedEntity/cda:addr'>
                All <emph>legalAuthenticator/assignedEntity</emph> elements have an 
                <emph>addr</emph> element.
            </assert>
            <assert diagnostics='L1-54' test='cda:assignedEntity/cda:assignedPerson'>
                The <emph>legalAuthenticator/assignedEntity/assignedPerson</emph> element 
                must be present.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='authenticator' see='&baseURI;#authenticator'>
        <rule id='cda-authenticator' context='cda:authenticator'>
            <assert diagnostics="L1-55" test='cda:assignedEntity/cda:addr'>
                The <emph>assignedEntity/addr</emph> element of the <emph>authenticator</emph> element 
                must be present.
            </assert>
            <assert diagnostics="L1-56" 
                test='cda:assignedEntity/cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                An <emph>assignedEntity/telecom</emph> element must be present whose value contains a 
                contact phone number.
            </assert>
            <assert diagnostics='L1-57' test='cda:assignedEntity/cda:assignedPerson/cda:name'>
                The <emph>name</emph> element of the <emph>assignedPerson</emph> must be present.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='participant' see='&baseURI;#participant'>
        <rule id='cda-associatedEntity' context='cda:participant/cda:associatedEntity'>
            <assert diagnostics="L1-58" test='cda:addr'>
                The <emph>associatedEntity/addr </emph>element must be present.
            </assert>
            <assert diagnostics="L1-59" 
                test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                A <emph>associatedEntity/telecom</emph> element must be present that contains a 
                contact phone number 
                for the participant.
            </assert>
            <assert diagnostics="L1-63" 
                test='not(../@typeCode = "IND") or 
                @classCode = "PRS" or @classCode = "NOK" or @classCode = "ECON" or @classCode = "GUAR"'
                >
                When <emph>participant/@typeCode</emph> is <emph>IND</emph>, <emph>associatedEntity/@classCode</emph> 
                must be <emph>PRS, NOK, ECON or GUAR.</emph>
            </assert>
            <assert diagnostics="L1-64" 
                test='not(../@typeCode = "IND") or 
                @classCode = "GUAR" or 
                count(cda:code/@code[.=document("voc.xml")/systems/system[@codeSystemName="PersonalRelationshipRoleType"]/code/@value]) = 1'
                >
                When <emph>associatedEntity/@classCode</emph> is <emph>PRS, NOK or ECON</emph> then <emph>associatedEntity/code</emph> 
                must be present having a value drawn from the <emph>PersonalRelationshipRoleType</emph> domain.
            </assert>
            <assert diagnostics="L1-65" test='not(../@typeCode="HLD") or @classCode = "POLHOLD"'>
                When <emph>participant/@typeCode</emph> is <emph>HLD</emph>, 
                <emph>associatedEntity/@classCode</emph> must be 
                <emph>POLHOLD</emph>.
            </assert>
            <assert diagnostics="L1-66" test='not(../@typeCode="HLD") or cda:scopingOrganization'>
                When <emph>participant/@typeCode</emph> is <emph>HLD</emph>, 
                <emph>associatedEntity/scopingOrganization</emph> must be present.
            </assert>
        </rule>     
        <rule id='cda-scopingOrg' context='cda:scopingOrganization'>
            <assert diagnostics='L1-60' test='cda:name'>
                The <emph>name</emph> element must be present in a <emph>scopingOrganization</emph>
                element.
            </assert>
            <assert diagnostics='L1-61' test='cda:addr'>
                The <emph>addr</emph> element must be present in a <emph>scopingOrganization</emph>
                element.
            </assert>
            <assert diagnostics='L1-62' test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                A <emph>telecom</emph> element must be present that contains a contact phone number 
                in a <emph>scopingOrganization</emph> element.
            </assert>
        </rule>
    </pattern>
    
    
    <pattern name='documentationOf' see='&baseURI;#documentationOf'>
        <rule id='cda-documentationOf' context='cda:ClinicalDocument'>
            <assert diagnostics="L1-67" test='count(cda:documentationOf) = 1'>
                Only one <emph>ClinicalDocument/documentationOf</emph> element must be present.
            </assert>
        </rule>
        <rule id='cda-serviceEvent' context='cda:serviceEvent'>
            <assert diagnostics="L1-68" test='cda:effectiveTime'>
                The <emph>effectiveTime</emph> element of the <emph>serviceEvent</emph> element must 
                be present.
            </assert>
        </rule>
        <rule id='cda-effectiveTime' context='cda:serviceEvent/cda:effectiveTime'>
            <assert diagnostics="L1-69" test='count(cda:low) = 1'>
                The <emph>effectiveTime</emph> element must contain only one <emph>low</emph> element.
                The <emph>low</emph> element is unrestricted with respect to precision or time zone.
            </assert>
            <assert diagnostics="L1-70" test='count(cda:high) = 1'>
                The <emph>effectiveTime</emph> element must contain only one <emph>high</emph> element.
                The <emph>high</emph> element is unrestricted with respect to precision or time zone.
            </assert>
        </rule>
        <rule id='cda-performer' context='cda:performer/cda:assignedEntity'>
            <assert diagnostics="L1-71" test='count(cda:code) = 1 and cda:code/@codeSystem="2.16.840.1.113883.6.96"'>
                The <emph>performer/assigneEntity/code</emph> if present must have a value 
                drawn from the SNOMED CT <emph>healthcare professional </emph>subtype hierarchy.
            </assert>
            <assert diagnostics="L1-72" test='cda:assignedPerson | cda:representedOrganization'>
                Every <emph>performer/assignedEntity</emph> element has at least one <emph>assignedPerson</emph>
                or <emph>representedOrganization</emph>.            
            </assert>
            <assert diagnostics="L1-73" test='cda:addr'>
                All <emph>performer/assignedEntity</emph> elements have an 
                <emph>addr</emph> element.
            </assert>
            <assert diagnostics="L1-74" test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                All <emph>performer/assignedEntity</emph> elements have a 
                <emph>telecom </emph>element that is a telephone contact number.
            </assert>
        </rule>
        <rule id='cda-performer-person' context='cda:performer/cda:assignedEntity/cda:assignedPerson'>
            <assert diagnostics="L1-75" test='cda:name'>
                All <emph>assignedPerson</emph> elements of 
                <emph>performer/assignedEntity</emph> must have a <emph>name</emph> element.
            </assert>
        </rule>
        <rule id='cda-performer-org' context='cda:performer/cda:assignedEntity/cda:representedOrganization'>
            <assert diagnostics="L1-76" test='cda:name'>
                The <emph>name</emph> element must be present in a 
                <emph>performer/assignedEntity/representedOrganization</emph> element.
            </assert>
            <assert diagnostics="L1-77" test='cda:addr'>
                The <emph>addr</emph> element must be present in a 
                <emph>performer/assignedEntity/representedOrganization</emph> element.
            </assert>
            <assert diagnostics="L1-78" test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                A <emph>telecom</emph> element must be present that contains a contact phone number in a 
                <emph>performer/assignedEntity/representedOrganization</emph> element.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='componentOf' see='&baseURI;componentOf'>
        <rule id='cda-componentOf' context='/cda:ClinicalDocument/cda:code'>
            <assert diagnostics='L1-79' 
                test='/cda:ClinicalDocument/cda:componentOf or 
                not(document("voc.xml")/systems/system[@codeSystemName="LOINC"]/code[@value = current()]/@displayName = "DISCHARGE SUMMARIZATION NOTE" or document("voc.xml")/systems/system[@codeSystemName="LOINC"]/code[@value = current()/@code]/@displayName = "TRANSFER SUMMARIZATION NOTE")'>
                If the Care Record Summary is a Discharge or Transfer Summarization, 
                then the <emph>componentOf</emph> element is required.
            </assert>
        </rule>
        <rule id='cda-encompassingEncounter' context='cda:encompassingEncounter'>
            <assert diagnostics="L1-80" test='cda:id'>
                The <emph>encompassingEncounter</emph> element must have an <emph>id</emph> element.
            </assert>
            <assert diagnostics="L1-81" test='cda:effectiveTime'>
                The <emph>encompassingEncounter</emph> element must have an <emph>effectiveTime</emph> 
                element.
            </assert>
            <assert diagnostics="L1-82" 
                test='cda:dischargeDispositionCode or not(document("voc.xml")/systems/system[@codeSystemName="LOINC"]/code[@value = current()/ancestor::cda:ClinicalDocument/cda:code/@code]/@displayName = "DISCHARGE SUMMARIZATION NOTE")'>
                If <emph>ClinicalDocument/code/@value</emph> represents a Discharge Summarization Note,
                then <emph>dischargeDispositionCode</emph> must be present.
            </assert>
        </rule>
        <rule id='cda-encounterParticipant' context='cda:encounterParticipant/cda:assignedEntity'>
            <assert diagnostics="L1-83" test='cda:addr'>
                The <emph>encounterParticipant/assignedEntity</emph> element must have an 
                <emph>addr</emph> element.
            </assert>
            <assert diagnostics="L1-84" test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                The <emph>encounterParticipant/assignedEntity</emph> element must have a 
                <emph>telecom</emph> element that represents a telephone contact number.
            </assert>
            <assert diagnostics="L1-85" test='cda:assignedPerson | cda:representedOrganization'>
                The <emph>encounterParticipant/assignedEntity</emph> element must have at least one 
                <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.
            </assert>
        </rule>
        <rule id='cda-responsibleParty' context='cda:responsibleParty/cda:assignedEntity'>
            <assert diagnostics="L1-86" test='cda:addr'>
                The <emph>responsibleParty/assignedEntity</emph> element must have an <emph>addr</emph>
                element.
            </assert>
            <assert diagnostics="L1-87" test='cda:telecom[boolean(@nullFlavor) or substring-before(@value,":") = "tel"]'>
                The <emph>responsibleParty/assignedEntity</emph> element must have a <emph>telecom</emph>
                element that represents a telephone contact number.
            </assert>
            <assert diagnostics="L1-88" test='cda:assignedPerson | cda:representedOrganization'>
                The <emph>responsibleParty/assignedEntity</emph> element must have at least one 
                <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.
            </assert>
        </rule>
    </pattern>
    
    <pattern name='Body' see='&baseURI;Body'>
        <rule id='sectionRequirements' context='cda:section'>
            <assert diagnostics='L2-1' test='cda:code'>
                A <emph>cda:section</emph> element must have a <emph>cda:code</emph> element.
            </assert>
            <assert diagnostics="L2-2" test='cda:text | cda:component'>
                A <emph>cda:section</emph> must contain at least one <emph>cda:text </emph>element or one or more <emph>cda:component</emph> elements. 
            </assert>
            <assert diagnostics="L2-3" test='string-length(string(cda:text)) &gt; 0'>
                All <emph>cda:text</emph> or <emph>cda:component</emph> elements must contain content.
            </assert>
        </rule>
        
        <rule id='RequiredSections' context='/cda:ClinicalDocument/cda:component/cda:structuredBody'>
            <assert diagnostics="L2-4" 
                test='/cda:ClinicalDocument/cda:code/@code = "34133-9" or descendant-or-self::cda:section/cda:code/@code="11535-2"'>
                A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>11535-2</emph>.
            </assert>
            <assert diagnostics="L2-5" 
                test='not(/cda:ClinicalDocument/cda:code/@code = "34133-9") or descendant-or-self::cda:section/cda:code/@code="11450-4"'>
                A Summary of Episode note that is not a discharge or transfer summary must include a <emph>section</emph> 
                element whose code is <emph>11450-4</emph>.
            </assert>
            <assert diagnostics="L2-6" test='descendant-or-self::cda:section/cda:code/@code="10155-0"'>
                A <emph>section</emph> must be present with a <emph>code</emph> value of <emph>10155-0</emph>.
            </assert>
            <assert diagnostics="L2-7" 
                test='/cda:ClinicalDocument/cda:code/@code = "34133-9" or descendant-or-self::cda:section/cda:code/@code="10183-2"'>
                A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>10183-2</emph>.
            </assert>
            <assert diagnostics="L2-8" 
                test='not(/cda:ClinicalDocument/cda:code/@code = "34133-9") or descendant-or-self::cda:section/cda:code/@code="10160-0"'>
                A Summary of Episode note that is not also a discharge or transfer summary must include a <emph>section</emph> 
                element whose code is <emph>10160-0</emph>.
            </assert>
            <assert diagnostics="L2-9" 
                test='/cda:ClinicalDocument/cda:code/@code = "34133-9" or descendant-or-self::cda:section/cda:code/@code="8648-8"'>
                A level 2 conforming Care Record Summary that is a discharge or transfer summary shall contain a 
                <emph>section</emph> with the <emph>code</emph> value of <emph>8648-8</emph>.
            </assert>
            
        </rule>
        <rule id='RequiredSections' context='cda:section/cda:code[@code="X-RFVCC"]'>
            <assert diagnostics="L2-9" 
                test='count(//cda:section/cda:code[@code="29299-5" or @code="10154-3"])=0'>
                A level 2 conforming Care Record Summary that contains a <emph>section</emph> with a <emph>code</emph> 
                value of <emph>X-RFVCC</emph> (REASON FOR VISIT/CHIEF COMPLAINT) shall not contain sections with a code
                value of <emph>29299-5</emph> (REASON FOR VISIT) or <emph>10154-3</emph> (CHIEF COMPLAINT).
            </assert>
        </rule>     
    </pattern>
    <diagnostics>
        <diagnostic id="L1-1">
            The root of a Care Record Summary must be a <emph>ClinicalDocument</emph> element from the <emph>urn:hl7-org:v3</emph> namespace.
        </diagnostic>
        <diagnostic id="L1-2">
            Telephone numbers must match the regular expression pattern<emph>tel:\+?[-0-9().]+</emph>
        </diagnostic>
        <diagnostic id="L1-3">
            At least one dialing digit must be present in the phone number after visual separators are removed.
        </diagnostic>
        <diagnostic id="L1-4">
            The <emph>extension</emph> attribute of the <emph>typeId</emph> element must be <emph>POCD_HD000040</emph>.
        </diagnostic>
        <diagnostic id="L1-5">
            The root attribute of the id element must be a syntactically correct UUID or OID.
        </diagnostic>
        <diagnostic id="L1-6">
            UUIDs must be represented in the form <emph>XXXXXXXX-XXXX-XXXX-XXXX-XXXXXXXXXXXX</emph>, where each X is a character from the set [A-Fa-f0-9].
        </diagnostic>
        <diagnostic id="L1-7">
            OIDs must be represented in dotted decimal notation, where each decimal number is either 0, or starts with a non-zero digit.  More formally, an OID must be in the form ([0-2])(.([1-9][0-9]*|0))+.
        </diagnostic>
        <diagnostic id="L1-8">
            OIDs must be no more than 64 characters in length.
        </diagnostic>
        <diagnostic id="L1-9">
            The value of <emph>/ClinicalDocument/code/@code</emph> must come from the appropriate LOINC code subset.
        </diagnostic>
        <diagnostic id="L1-10">
            The value of <emph>/ClinicalDocument/code/@codeSysem</emph> is the OID for LOINC.
        </diagnostic>
        <diagnostic id="L1-11">
            The value of <emph>/ClinicalDocument/code/@codeSystemName</emph>, if present is <emph>LOINC</emph>.
        </diagnostic>
        <diagnostic id="L1-12">
            If pre-coordinated document type codes are used, the role code and function code for the author must not conflict with the document type code.
        </diagnostic>
        <diagnostic id="L1-13">
            The <emph>effectiveTime</emph> element must be precise at least to the day.
        </diagnostic>
        <diagnostic id="L1-14">
            The <emph>effectiveTime</emph> element must have a time zone.
        </diagnostic>
        <diagnostic id="L1-15">
            The <emph>languageCode</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-16">
            The language code must be in the form <emph>nn</emph>, or <emph>nn-CC</emph>.
        </diagnostic>
        <diagnostic id="L1-17">
            The <emph>nn</emph> portion must be a legal ISO-639-1 language code in lower case.
        </diagnostic>
        <diagnostic id="L1-18">
            The <emph>CC</emph> portion, if present must be an ISO-3166 country code in upper case.
        </diagnostic>
        <diagnostic id="L1-19">
            When <emph>setId</emph> is present, then <emph>versionNumber</emph> must be present.
        </diagnostic>
        <diagnostic id="L1-20">
            Either there is no <emph>setId</emph>, or then <emph>extension</emph> and/or <emph>root</emph> of <emph>setId</emph> and <emph>id</emph> are different.
        </diagnostic>
        <diagnostic id="L1-21">
            No <emph>copyTime</emph> element is present in the <emph>ClinicalDocument</emph>.
        </diagnostic>
        <diagnostic id="L1-22">
            At least one <emph>recordTarget/patientRole</emph> element exists.
        </diagnostic>
        <diagnostic id="L1-23">
            The <emph>recordTarget/patientRole</emph> element has an <emph>addr</emph> element.
        </diagnostic>
        <diagnostic id="L1-24">
            The <emph>recordTarget/patientRole</emph> element has a <emph>telecom</emph> element that represents a contact phone number.
        </diagnostic>
        <diagnostic id="L1-25">
            A <emph>patient/birthTime</emph> element is present. 
        </diagnostic>
        <diagnostic id="L1-26">
            A <emph>patient/administrativeGenderCode</emph> element is present.
        </diagnostic>
        <diagnostic id="L1-27">
            All <emph>providerOrganization</emph> elements have a <emph>name</emph> element.
        </diagnostic>
        <diagnostic id="L1-28">
            All <emph>providerOrganization</emph> elements have an <emph>addr </emph>element.
        </diagnostic>
        <diagnostic id="L1-29">
            All <emph>providerOrganization</emph> elements have a <emph>telecom </emph>element that is a telephone contact number.
        </diagnostic>
        <diagnostic id="L1-30">
            The <emph>author/time</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-31">
            The <emph>author/time</emph> element must be precise at least to the day.
        </diagnostic>
        <diagnostic id="L1-32">
            The <emph>author/time</emph> element must have a time zone.
        </diagnostic>
        <diagnostic id="L1-33">
            The <emph>assignedAuthor/id</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-34">
            All <emph>assignedAuthors</emph> have at least one <emph>telecom</emph> element that contains a contact phone number.
        </diagnostic>
        <diagnostic id="L1-35">
            When <emph>assignedAuthoringDevice</emph> is present, the <emph>softwareName</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-36">
            All <emph>dataEnterer/time</emph> elements must be precise at least to the day.
        </diagnostic>
        <diagnostic id="L1-37">
            All <emph>dataEnterer/time</emph> elements must have a time zone.
        </diagnostic>
        <diagnostic id="L1-38">
            All <emph>dataEnterer</emph> elements have an <emph>assignedEntity/assignedPerson/name</emph> element.
        </diagnostic>
        <diagnostic id="L1-39">
            An <emph>informant</emph> must have either an <emph>assignedEntity/assignedPerson/name</emph> element, or a <emph>relatedEntity/relatedPerson/name</emph> element.
        </diagnostic>
        <diagnostic id="L1-40">
            An <emph>informant</emph> should not have any <emph>assignedEntity/assignedPerson/crs:asPatientRelationship </emph>elements, or <emph>relatedEntity/relatedPerson/ crs:asPatientRelationship</emph> elements.
        </diagnostic>
        <diagnostic id="L1-41">
            When <emph>relatedEntity/@classCode</emph> is <emph>PRS</emph>, values in <emph>relatedEntity/code</emph> shall come from the 
        </diagnostic>
        <diagnostic id="L1-42">
            When <emph>relatedEntity/@classCode</emph> is <emph>CON</emph>, <emph>relatedEntity/code</emph> shall not be present.
        </diagnostic>
        <diagnostic id="L1-43">
            When <emph>relatedEntity/@classCode</emph> is <emph>PROV</emph>, and <emph>relatedEntity/code</emph> is present, the value shall come from SNOMED CT.
        </diagnostic>
        <diagnostic id="L1-44">
            A <emph>custodian/assignedCustodian/representedCustodianOrganization/name</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-45">
            A <emph>custodian/assignedCustodian/representedCustodianOrganization/telecom</emph> element must be present that contains a telephone contact.
        </diagnostic>
        <diagnostic id="L1-46">
            A <emph>custodian/assignedCustodian/representedCustodianOrganization/addr</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-47">
            At least one of <emph>intendedRecipient/informationRecipient</emph> or <emph>intendedRecipient/recievedOrganization</emph> must be present.
        </diagnostic>
        <diagnostic id="L1-48">
            All <emph>informationRecipient</emph> elements have a <emph>name</emph> element.
        </diagnostic>
        <diagnostic id="L1-49">
            All <emph>receivedOrganization</emph> elements have a <emph>name</emph> element.
        </diagnostic>
        <diagnostic id="L1-50">
            All <emph>intendedRecipient</emph> elements have at least one <emph>telecom</emph> element that contains a contact phone number.
        </diagnostic>
        <diagnostic id="L1-51">
            All <emph>intendedRecipient</emph> elements have an <emph>addr</emph> element.
        </diagnostic>
        <diagnostic id="L1-52">
            All <emph>legalAuthenticator/assignedEntity</emph> elements have at least one <emph>telecom</emph> element that contains a contact phone number.
        </diagnostic>
        <diagnostic id="L1-53">
            All <emph>legalAuthenticator/assignedEntity</emph> elements have an <emph>addr</emph> element.
        </diagnostic>
        <diagnostic id="L1-54">
            The <emph>legalAuthenticator/assignedEntity/assignedPerson</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-55">
            The <emph>assignedEntity/addr</emph> element of the <emph>authenticator</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-56">
            An <emph>assignedEntity/telecom</emph> element must be present whose value contains a contact phone number.
        </diagnostic>
        <diagnostic id="L1-57">
            The <emph>name</emph> element of the <emph>assignedPerson</emph> must be present.
        </diagnostic>
        <diagnostic id="L1-58">
            The <emph>associatedEntity/addr</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-59">
            A <emph>associatedEntity/telecom</emph> element must be present that contains a contact phone number for the participant.
        </diagnostic>
        <diagnostic id="L1-60">
            The <emph>name</emph> element must be present in a <emph>scopingOrganization</emph> element.
        </diagnostic>
        <diagnostic id="L1-61">
            The <emph>addr</emph> element must be present in a <emph>scopingOrganization</emph> element.
        </diagnostic>
        <diagnostic id="L1-62">
            A <emph>telecom</emph> element must be present that contains a contact phone number in a <emph>scopingOrganization</emph> element.
        </diagnostic>
        <diagnostic id="L1-63">
            When <emph>participant/@typeCode</emph> is <emph>IND</emph>, <emph>associatedEntity/@classCode</emph> must be <emph>PRS, NOK, ECON or GUAR.</emph>
        </diagnostic>
        <diagnostic id="L1-64">
            When <emph>associatedEntity/@classCode</emph> is <emph>PRS, NOK or ECON</emph> then <emph>associatedEntity/code</emph> must be present having a value drawn from the 
        </diagnostic>
        <diagnostic id="L1-65">
            When <emph>participant/@typeCode</emph> is <emph>HLD</emph>, <emph>associatedEntity/@classCode</emph> must be <emph>POLHOLD</emph>.
        </diagnostic>
        <diagnostic id="L1-66">
            When <emph>participant/@typeCode</emph> is <emph>HLD</emph>, <emph>associatedEntity/scopingOrganization </emph>must be present.
        </diagnostic>
        <diagnostic id="L1-67">
            Only one <emph>ClinicalDocument/documentationOf</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-68">
            The <emph>effectiveTime</emph> element of the <emph>serviceEvent</emph> element must be present.
        </diagnostic>
        <diagnostic id="L1-69">
            The <emph>effectiveTime</emph> element must contain only one <emph>low</emph> element.   The <emph>low</emph> element is unrestricted with respect to precision or time zone.
        </diagnostic>
        <diagnostic id="L1-70">
            The <emph>effectiveTime</emph> element must contain only one <emph>high</emph> element.  The <emph>high</emph> element is unrestricted with respect to precision or time zone.
        </diagnostic>
        <diagnostic id="L1-71">
            The <emph>performer/assigneEntity/code</emph> if present must have a value drawn from the SNOMED CT <emph>healthcare professional </emph>subtype hierarchy. 
        </diagnostic>
        <diagnostic id="L1-72">
            Every <emph>performer/assignedEntity</emph> element has at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph>.
        </diagnostic>
        <diagnostic id="L1-73">
            All <emph>performer/assignedEntity</emph> elements have an <emph>addr </emph>element.
        </diagnostic>
        <diagnostic id="L1-74">
            All <emph>performer/assignedEntity</emph> elements have a <emph>telecom </emph>element that is a telephone contact number.
        </diagnostic>
        <diagnostic id="L1-75">
            All <emph>assignedPerson</emph> elements of <emph>performer/assignedEntity</emph> must have a <emph>name</emph> element.
        </diagnostic>
        <diagnostic id="L1-76">
            The <emph>name</emph> element must be present in a <emph>performer/assignedEntity/representedOrganization</emph> element.
        </diagnostic>
        <diagnostic id="L1-77">
            The <emph>addr</emph> element must be present in a <emph>performer/assignedEntity/representedOrganization</emph> element.
        </diagnostic>
        <diagnostic id="L1-78">
            A <emph>telecom</emph> element must be present that contains a contact phone number in a <emph>performer/assignedEntity/representedOrganization</emph> element.
        </diagnostic>
        <diagnostic id="L1-79">
            If the Care Record Summary is a Discharge or Transfer Summarization, then the <emph>componentOf</emph> element is required.
        </diagnostic>
        <diagnostic id="L1-80">
            The <emph>encompassingEncounter</emph> element must have an <emph>id</emph> element.
        </diagnostic>
        <diagnostic id="L1-81">
            The <emph>encompassingEncounter</emph> element must have an <emph>effectiveTime</emph> element.
        </diagnostic>
        <diagnostic id="L1-82">
            If <emph>ClinicalDocument/code/@value</emph> represents a Discharge Summarization Node, then <emph>dischargeDispositionCode</emph> must be present.
        </diagnostic>
        <diagnostic id="L1-83">
            The <emph>encounterParticipant/assignedEntity</emph> element must have an <emph>addr</emph> element.
        </diagnostic>
        <diagnostic id="L1-84">
            The <emph>encounterParticipant/assignedEntity</emph> element must have a <emph>telecom</emph> element that represents a telephone contact number.
        </diagnostic>
        <diagnostic id="L1-85">
            The <emph>encounterParticipant/assignedEntity</emph> element must have at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.
        </diagnostic>
        <diagnostic id="L1-86">
            The <emph>responsibleParty/assignedEntity</emph> element must have an <emph>addr</emph> element.
        </diagnostic>
        <diagnostic id="L1-87">
            The <emph>responsibleParty/assignedEntity</emph> element must have a <emph>telecom</emph> element that represents a telephone contact number.
        </diagnostic>
        <diagnostic id="L1-88">
            The <emph>responsibleParty/assignedEntity</emph> element must have at least one <emph>assignedPerson</emph> or <emph>representedOrganization</emph> element present.
        </diagnostic>
        <diagnostic id="L2-1">
            A <emph>cda:section</emph> element must have a <emph>cda:code</emph> element.
        </diagnostic>
        <diagnostic id="L2-2">
            A <emph>cda:section</emph> must contain at least one <emph>cda:text </emph>element or one or more <emph>cda:component</emph> elements. 
        </diagnostic>
        <diagnostic id="L2-3">
            All <emph>cda:text</emph> or <emph>cda:component</emph> elements must contain content.
        </diagnostic>
        <diagnostic id="L2-4">
            A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>11535-2</emph>.
        </diagnostic>
        <diagnostic id="L2-5">
            A Summary of Episode note that is not a discharge or transfer summary must include a <emph>section</emph> element whose code is <emph>11450-4</emph>.
        </diagnostic>
        <diagnostic id="L2-6">
            A <emph>section</emph> must be present with a <emph>code</emph> value of <emph>10155-0</emph>.
        </diagnostic>
        <diagnostic id="L2-7">
            A Discharge or Transfer summary must include a <emph>section</emph> element whose code is <emph>10183-2</emph>.
        </diagnostic>
        <diagnostic id="L2-8">
            A Summary of Episode note that is not also a discharge or transfer summary must include a <emph>section</emph> element whose code is <emph>10160-0</emph>.
        </diagnostic>
        <diagnostic id="L2-9">
            A level 2 conforming Care Record Summary that is a discharge or transfer summary shall contain a <emph>section</emph> with the <emph>code</emph> value of <emph>8648-8</emph>.
        </diagnostic>
        <diagnostic id="L2-10">
            The section type code for the section describing the reason for visit in a level 2 conforming Care Record Summary shall be either <emph>X-RFVCC</emph> (REASON FOR VISIT/CHIEF COMPLAINT), or <emph>29299-5</emph> (REASON FOR VISIT).
        </diagnostic>
        <diagnostic id="L2-11">
            The section type code for the section describing the patient's chief complaint in a level 2 conforming Care Record Summary shall be either <emph>X-RFVCC</emph> (REASON FOR VISIT/CHIEF COMPLAINT), or <emph>10154-3</emph> (CHIEF COMPLAINT).
        </diagnostic>
        <diagnostic id="L2-12">
            A level 2 conforming Care Record Summary that contains a <emph>section</emph> with a <emph>code</emph> value of <emph>X-RFVCC</emph> (REASON FOR VISIT/CHIEF COMPLAINT) shall not contain sections with a code value of <emph>29299-5</emph> (REASON FOR VISIT) or <emph>10154-3</emph> (CHIEF COMPLAINT).
        </diagnostic>
        <diagnostic id="L2-13">
            The <emph>code</emph> for the <emph>section</emph> describing the Reason for Referral in a level 2 conforming Care Record Summary shall be <emph>X-RFR (REASON FOR REFERRAL)</emph>.
        </diagnostic>
        <diagnostic id="L2-14">
            The <emph>code</emph> for the <emph>section</emph> describing the patient Advance Directives in a level 2 conforming Care Record Summary shall be <emph>X-ADVDIR</emph> (ADVANCE DIRECTIVES).
        </diagnostic>
        <diagnostic id="L2-15">
            The LOINC section type <emph>code</emph> for the section describing the History of Present Illness in a level 2 conforming Care Record Summary shall be <emph>10164-2</emph> (HISTORY OF PRESENT ILLNESS).
        </diagnostic>
        <diagnostic id="L2-16">
            The LOINC section type <emph>code</emph> for the section describing the patient's functional status in a level 2 conforming Care Record Summary shall be <emph>10158-4</emph> (HISTORY OF FUNCTIONAL STATUS).
        </diagnostic>
        <diagnostic id="L2-17">
            The LOINC section type <emph>code</emph> for the section providing family history of the patient in a level 2 conforming Care Record Summary shall be <emph>10157-6</emph> (HISTORY OF FAMILY MEMBER DISEASES).
        </diagnostic>
        <diagnostic id="L2-18">
            The LOINC section type <emph>code</emph> for the <emph>section</emph> providing immunization history in a level 2 conforming Care Record Summary shall be <emph>11369-6</emph> (HISTORY OF IMMUNIZATION).
        </diagnostic>
        <diagnostic id="L2-19">
            The LOINC section type <emph>code</emph> for the section describing the patient's past surgical history in a level 2 conforming Care Record Summary shall be <emph>10167-5</emph> (PAST SURGICAL HISTORY).
        </diagnostic>
        <diagnostic id="L2-20">
            The LOINC section <emph>code</emph> used for the section describing prior outpatient visits in a level 2 conforming Care Record Summary shall be <emph>11346-4</emph> (HISTORY OF OUTPATIENT VISITS).
        </diagnostic>
        <diagnostic id="L2-21">
            The LOINC section <emph>code</emph> used for the section describing prior hospitalizations in a level 2 conforming Care Record Summary shall be <emph>11336-5</emph> (HISTORY OF HOSPITALIZATIONS).
        </diagnostic>
        <diagnostic id="L2-22">
            The LOINC section <emph>code</emph> used for the section providing the patient vital signs in a level 2 conforming Care Record Summary shall be <emph>8716-3</emph> (VITAL SIGNS, PHYSICAL FINDINGS).
        </diagnostic>
        <diagnostic id="L2-23">
            The LOINC section <emph>code</emph> used for the section describing results or referring to other reports in a level 2 conforming Care Record Summary shall be <emph>11493-4 </emph>(HOSPITAL DISCHARGE STUDIES SUMMARY), or <emph>X-SS</emph> (STUDIES SUMMARY)
        </diagnostic>
        <diagnostic id="L2-24">
            The LOINC section <emph>code</emph> used for the section describing the plan of care for the patient in a level 2 conforming Care Record Summary shall be <emph>18775-6 </emph>(TREATMENT PLAN)
        </diagnostic>
    </diagnostics>
</schema>