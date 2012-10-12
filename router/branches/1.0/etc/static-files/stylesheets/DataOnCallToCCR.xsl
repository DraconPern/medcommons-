<?xml version="1.0" encoding="UTF-8"?>
<!--
Translates FAX output from DataOnCall to CCR format.
Need to describe mapping here.
-->
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xs="http://www.w3.org/2001/XMLSchema" xmlns:fn="http://www.w3.org/2005/xpath-functions" xmlns:xdt="http://www.w3.org/2005/xpath-datatypes">
	<xsl:output method="xml" version="1.0" encoding="UTF-8" indent="yes"/>
	<xsl:template match="/">
		<ContinuityOfCareRecord xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns="urn:astm-org:CCR">
			<CCRDocumentObjectID>XYZ</CCRDocumentObjectID>
			<Language>
				<Text>English</Text>
			</Language>
			<Version>V1.0</Version>
			<DateTime>
				<ExactDateTime>UNKNOWN</ExactDateTime>
			</DateTime>
			<Patient>
				<ActorID>AA0005</ActorID>
			</Patient>
			<From>
				<ActorLink>
					<ActorID>AA0003</ActorID>
					<ActorRole>
						<Text>FAX </Text>
					</ActorRole>
				</ActorLink>
			</From>
			<To>
				<ActorLink>
					<ActorID>AA0004</ActorID>
					<ActorRole>
						<Text>FAX </Text>
					</ActorRole>
				</ActorLink>
			</To>
			<Purpose>
				<Description>
					<Text>FAX Transmission <xsl:value-of select="UniFaxPostRequest/FaxControl/MCFID"/>/<xsl:value-of select="UniFaxPostRequest/FaxControl/FaxName"/></Text>
				</Description>
			</Purpose>
			<Body>
			</Body>
			<Actors>
				<!-- The MedCommons actor that imports this data -->
				<Actor>
					<ActorObjectID>AA0001</ActorObjectID>
					<InformationSystem>
						<Name>MedCommons Notification</Name>
						<Type>Repository</Type>
						<Version>V1.0</Version>
					</InformationSystem>
					<Source>
						<Actor>
							<ActorID>AA0001</ActorID>
						</Actor>
					</Source>
				</Actor>
				<Actor>
					<ActorObjectID>AA0002</ActorObjectID>
					<InformationSystem>
						<Name>DataOnCall</Name>
						<Version>V1.0</Version>
					</InformationSystem>
					<Source>
						<Actor>
							<ActorID>AA0002</ActorID>
						</Actor>
					</Source>
				</Actor>
				<!-- Source of FAX -->
				<Actor>
					<ActorObjectID>AA0003</ActorObjectID>
						<InformationSystem>
							<Name>FAX Source
							</Name>
						
					</InformationSystem>
					<Telephone>
						<Value>
						
							<xsl:call-template name="format-telephone">
								<xsl:with-param name="telephone-number">
									<xsl:value-of select="UniFaxPostRequest/FaxControl/ANI"/>
								</xsl:with-param>
							</xsl:call-template>
						</Value>
						<Type>
							<Text>Fax</Text>
						</Type>
					</Telephone>
					<Source>
						<Actor>
							<ActorID>AA0002</ActorID>
						</Actor>
					</Source>
				</Actor>
				<!-- Destination of FAX -->
				<Actor>
					<ActorObjectID>AA0004</ActorObjectID>
						<InformationSystem>
							<Name>FAX Destination
							</Name>
						
					</InformationSystem>
					<Telephone>
						<Value>
							
							<xsl:call-template name="format-telephone">
								<xsl:with-param name="telephone-number">
									<xsl:value-of select="UniFaxPostRequest/FaxControl/AccountID"/>
								</xsl:with-param>
							</xsl:call-template>
						</Value>
						<Type>
							<Text>Fax</Text>
						</Type>
					</Telephone>
					<Source>
						<Actor>
							<ActorID>AA0002</ActorID>
						</Actor>
					</Source>
				</Actor>
				<Actor>
					<ActorObjectID>AA0005</ActorObjectID>
					<IDs>
						<Type>
							<Text>MedCommons Account Id</Text>
						</Type>
						<ID></ID>
						<Source>
							<Actor>
								<ActorID>AA0002</ActorID>
							</Actor>
						</Source>
					</IDs>
					<IDs>
						<Type>
							<Text>DataOnCall AccountID</Text>
						</Type>
						<ID>
							<xsl:value-of select="UniFaxPostRequest/FaxControl/AccountID"/>
						</ID>
						<Source>
							<Actor>
								<ActorID>AA0002</ActorID>
							</Actor>
						</Source>
					</IDs>
					<Source>
							<Actor>
								<ActorID>AA0002</ActorID>
							</Actor>
						</Source>
				</Actor>
			</Actors>
		</ContinuityOfCareRecord>
	</xsl:template>
	<xsl:template name="format-telephone">
		<xsl:param name="telephone-number"/>
				(<xsl:value-of select="substring($telephone-number, 1, 3)"/>)
				<xsl:value-of select="substring($telephone-number, 4, 3)"/>
				-
				<xsl:value-of select="substring($telephone-number, 7, 4)"/>
	</xsl:template>
</xsl:stylesheet>
