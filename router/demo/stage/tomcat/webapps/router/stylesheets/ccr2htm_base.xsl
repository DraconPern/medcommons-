<?xml version="1.0" encoding="utf-8"?>
<!-- 
Simple HTML representation of the ASTM Continuity of Care Record. This representation
does not present all the potential data storable in the CCR.  Instead it gives a
clinical representation of the CCR instance.

  Author:   	Steven E. Waldren, MD 
  				American Academy of Family Physicians
				swaldren@aafp.org

  Coauthors:	Ken Miller
				Solventus

  Date: 	2005-11-01
  Version: 	1.1

 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:a="urn:astm-org:CCR" xmlns:date="http://exslt.org/dates-and-times" exclude-result-prefixes="a date">

	<!-- From the http://www.exslt.org library and used for date processing -->
	<xsl:import href="date.format-date.template.xsl"/>
	
	<xsl:output method="html" encoding="UTF-8"/>
	<xsl:template match="/">
		<html xmlns:date="http://exslt.org/dates-and-times">
			<head>
				<style type="text/css">&lt;!--
strong.clinical {
	color: #3300FF;
}
p {margin-left: 20px}
span.header{
	font-weight: bold;
    font-size: 14pt;
    line-height: 16pt;
	padding-top: 10px;
}
td{
	vertical-align: top;
}
table.list {
	padding-bottom: 5px;
	border: thin solid #cccccc;
	border-style-internal: thin solid #cccccc;
	BORDER-COLLAPSE: collapse;
	BACKGROUND-COLOR: white;
}
table.list th {
	text-align: left;
	FONT-WEIGHT: bold;
	COLOR: white;
	BACKGROUND-COLOR: #006699;
}
table.list td {
	padding: 5;
	border: thin solid #cccccc;
}
table.internal {
	border: none
}
table.internal td {
    padding: 1;
    border: none;
}
table.internal tr.even{
	background-color: #CEFFFF;
}
--&gt;</style>
				<title>Continuity of Care Record</title>
			</head>
			<body>
				<table cellSpacing="1" cellPadding="1">
					<tbody>
						<tr>
							<td/>
						</tr>
						<tr>
							<td>
								<table cellSpacing="1" cellPadding="1">
									<tbody>
										<tr>
											<td/>
											<td>
												<h1>Continuity of Care Record
													<br/>
												</h1>
												<table cellSpacing="3" cellPadding="1" width="75%" bgColor="#ffffcc">
													<tbody>
														<tr>
															<td>
																<strong>Date Created:</strong>
															</td>
															<td>
																<xsl:call-template name="date:format-date">
																	<xsl:with-param name="date-time">
																		<xsl:value-of select="a:ContinuityOfCareRecord/a:DateTime/a:ExactDateTime"/>
																	</xsl:with-param>
																	<xsl:with-param name="pattern">EEE MMM dd, yyyy 'at' hh:mm aa zzz</xsl:with-param>
																</xsl:call-template>
															</td>
														</tr>
														<tr>
															<td>
																<strong>From:</strong>
															</td>
															<td>
																<xsl:for-each select="a:ContinuityOfCareRecord/a:From/a:ActorLink">
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:ActorID"/>
																	</xsl:call-template>
																	<xsl:if test="a:ActorRole/a:Text"><![CDATA[ ]]>(
																		<xsl:value-of select="a:ActorRole/a:Text"/>)</xsl:if>
																	<br/>
																</xsl:for-each>
															</td>
														</tr>
														<tr>
															<td>
																<strong>To:</strong>
															</td>
															<td>
																<xsl:for-each select="a:ContinuityOfCareRecord/a:To/a:ActorLink">
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:ActorID"/>
																	</xsl:call-template><![CDATA[ ]]>(
																	<xsl:value-of select="a:ActorRole/a:Text"/>)
																	<br/>
																</xsl:for-each>
															</td>
														</tr>
														<tr>
															<td>
																<strong>Purpose:</strong>
															</td>
															<td>
																<xsl:value-of select="a:ContinuityOfCareRecord/a:Purpose/a:Description/a:Text"/>
															</td>
														</tr>
													</tbody>
												</table>
												<br/>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Patient Demographics</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Name</th>
															<th>Date of Birth</th>
															<th>Gender</th>
															<th>Identification Numbers</th>
															<th>Address / Phone</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Patient">
															<xsl:variable name="objID" select="a:ActorID"/>
															<xsl:for-each select="/a:ContinuityOfCareRecord/a:Actors/a:Actor">
																<xsl:variable name="thisObjID" select="a:ActorObjectID"/>
																<xsl:if test="$objID = $thisObjID">
																	<tr>
																		<td>
																			<xsl:call-template name="actorName">
																				<xsl:with-param name="objID">
																					<xsl:value-of select="$thisObjID"/>
																				</xsl:with-param>
																			</xsl:call-template>
																			<br/>
																		</td>
																		<td>
																			<table class="internal">
																				<tbody>
																					<xsl:call-template name="dateTime">
																						<xsl:with-param name="dt" select="a:Person/a:DateOfBirth"/>
																					</xsl:call-template>
																				</tbody>
																			</table>
																		</td>
																		<td>
																			<xsl:value-of select="a:Person/a:Gender/a:Text"/>
																		</td>
																		<td>
																			<xsl:for-each select="a:IDs">
																				<xsl:value-of select="a:Type/a:Text"/>:
																				<xsl:value-of select="a:ID"/>
																				<br/>
																			</xsl:for-each>
																		</td>
																		<td>
																			<xsl:for-each select="a:Address">
																				<xsl:if test="a:Type">
																					<b>
																						<xsl:value-of select="a:Type/a:Text"/>:</b>
																					<br/>
																				</xsl:if>
																				<xsl:if test="a:Line1">
																					<xsl:value-of select="a:Line1"/>
																					<br/>
																				</xsl:if>
																				<xsl:if test="a:Line2">
																					<xsl:value-of select="a:Line2"/>
																					<br/>
																				</xsl:if>
																				<xsl:value-of select="a:City"/>,
																				<xsl:value-of select="a:State"/>
																				<xsl:value-of select="a:PostalCode"/>
																				<br/>
																			</xsl:for-each>
																			<xsl:for-each select="a:Telephone">
																				<br/>
																				<xsl:value-of select="a:Type/a:Text"/>:
																				<xsl:value-of select="a:Value"/>
																			</xsl:for-each>
																		</td>
																	</tr>
																</xsl:if>
															</xsl:for-each>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Alerts</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Code</th>
															<th>Description</th>
															<th>Reaction</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Alerts/a:Alert">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:apply-templates select="a:Description/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:value-of select="a:Reaction/a:Description/a:Text"/><xsl:if test="a:Reaction/a:Severity/a:Text">-
																	<xsl:value-of select="a:Reaction/a:Severity/a:Text"/></xsl:if>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Advance Directives</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Description</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:AdvanceDirectives/a:AdvanceDirective">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Support/a:SupportProvider">
										<tr>
											<td/>
											<td>
												<span class="header">Support Providers</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Role</th>
															<th>Name</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Support/a:SupportProvider">
															<tr>
																<td>
																	<xsl:value-of select="a:ActorRole/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										</xsl:if>
										<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:FunctionalStatus/a:Function">
										<tr>
											<td/>
											<td>
												<span class="header">Functional Status</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Code</th>
															<th>Description</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:FunctionalStatus/a:Function">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:apply-templates select="a:Description/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										</xsl:if>
										<tr>
											<td/>
											<td>
												<span class="header">Problems</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Code</th>
															<th>Description</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Problems/a:Problem">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:apply-templates select="a:Description/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Procedures</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Code</th>
															<th>Description</th>
															<th>Location</th>
															<th>Substance</th>
															<th>Method</th>
															<th>Position</th>
															<th>Site</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Procedures/a:Procedure">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:apply-templates select="a:Description/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:for-each select="a:Locations/a:Location">
																		<xsl:value-of select="a:Description/a:Text"/>
																		<xsl:if test="a:Actor">
																			(<xsl:call-template name="actorName">
																				<xsl:with-param name="objID" select="a:Actor/a:ActorID"/>
																			  </xsl:call-template>
																			  <xsl:if test="a:Actor/a:ActorRole/a:Text"><![CDATA[ ]]>-<![CDATA[ ]]>
																				<xsl:value-of select="a:ActorRole/a:Text"/>)</xsl:if>
																			  </xsl:if>)
																		<xsl:if test="position() != last()">
																			<br/>
																		</xsl:if>
																	</xsl:for-each>
																</td>
																<td>
																	<xsl:for-each select="a:Substance">
																		<xsl:value-of select="a:Text"/>
																	</xsl:for-each>
																</td>
																<td>
																	<xsl:value-of select="a:Method/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:Position/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:Site/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>	
										<tr>
											<td/>
											<td>
												<span class="header">Medications</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Medication</th>
															<th>Date</th>
															<th>Form</th>
															<th>Strength</th>
															<th>Quantity</th>
															<th>SIG</th>
															<th>Indications</th>
															<th>Instruction</th>
															<th>Refills</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Medications/a:Medication">
															<tr>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Product/a:ProductName/a:Text"/>
																		<xsl:if test="a:Product/a:BrandName">
																			<![CDATA[ ]]>(<xsl:value-of select="a:Product/a:BrandName/a:Text"/>)
																		</xsl:if>
																	</strong>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:value-of select="a:Product/a:Form/a:Text"/>
																</td>
																<td>
																	<xsl:for-each select="a:Product/a:Strength">
																		<xsl:if test="position() &gt; 1">
																			/
																		</xsl:if>
																		<xsl:value-of select="a:Value"/><![CDATA[ ]]>
																		<xsl:value-of select="a:Units/a:Unit"/>
																	</xsl:for-each>
																</td>
																<td>
																	<xsl:value-of select="a:Quantity/a:Value"/><![CDATA[ ]]>
																	<xsl:value-of select="a:Quantity/a:Units/a:Unit"/>
																</td>
																<td>
																	<table class="internal" width="100%" border="1">
																		<tbody>
																			<xsl:for-each select="a:Directions/a:Direction">
																				<xsl:choose>
																					<xsl:when test="position() mod 2=0">
																						<tr class="even">
																							<td>
																								<xsl:value-of select="a:Dose/a:Value"/><![CDATA[ ]]>
																								<xsl:value-of select="a:Dose/a:Units/a:Unit"/><![CDATA[ ]]>
																								<xsl:value-of select="a:Route/a:Text"/><![CDATA[ ]]>
																								<xsl:value-of select="a:Frequency/a:Value"/>
																								<xsl:if test="a:Duration">
																									<![CDATA[ ]]>(for <xsl:value-of select="a:Duration/a:Value"/><![CDATA[ ]]>
																									<xsl:value-of select="a:Duration/a:Units/a:Unit"/>)
																								</xsl:if>
																							</td>
																							<xsl:if test="a:MultipleDirectionModifier/a:ObjectAttribute">
																								<td>
																									<xsl:for-each select="a:MultipleDirectionModifier/a:ObjectAttribute">
																										<xsl:value-of select="a:Attribute"/><br/><xsl:value-of select="a:AttributeValue/a:Value"/>
																									</xsl:for-each>
																								</td>
																							</xsl:if>
																						</tr>
																					</xsl:when>
																					<xsl:otherwise>
																						<tr class="odd">
																							<td width="50%">
																								<xsl:value-of select="a:Dose/a:Value"/><![CDATA[ ]]>
																								<xsl:value-of select="a:Dose/a:Units/a:Unit"/><![CDATA[ ]]>
																								<xsl:value-of select="a:Route/a:Text"/><![CDATA[ ]]>
																								<xsl:value-of select="a:Frequency/a:Value"/>
																								<xsl:if test="a:Duration">
																									<![CDATA[ ]]>(for <xsl:value-of select="a:Duration/a:Value"/><![CDATA[ ]]>
																									<xsl:value-of select="a:Duration/a:Units/a:Unit"/>)
																								</xsl:if>
																							</td>
																							<xsl:if test="a:MultipleDirectionModifier/a:ObjectAttribute">
																								<td>
																									<xsl:for-each select="a:MultipleDirectionModifier/a:ObjectAttribute">
																										<xsl:value-of select="a:Attribute"/>:<xsl:value-of select="a:AttributeValue/a:Value"/>
																									</xsl:for-each>
																								</td>
																							</xsl:if>
																						</tr>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:for-each>
																		</tbody>
																	</table>
																</td>
																<td/>
																<td>
																	<xsl:for-each select="a:PatientInstructions/a:Instruction">
																		<xsl:value-of select="a:Text"/>
																		<br/>
																	</xsl:for-each>
																</td>
																<td>
																	<xsl:for-each select="a:Refills/a:Refill">
																		<xsl:value-of select="a:Number"/><![CDATA[ ]]></xsl:for-each>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Immunizations</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Code</th>
															<th>Vaccine</th>
															<th>Date</th>
															<th>Route</th>
															<th>Site</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Immunizations/a:Immunization">
															<tr>
																<td>
																	<xsl:apply-templates select="a:Product/a:ProductName/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Product/a:ProductName/a:Text"/>
																		<xsl:if test="a:Product/a:Form">
																			<![CDATA[ ]]>(<xsl:value-of select="a:Product/a:Form/a:Text"/>)
																		</xsl:if>
																	</strong>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:value-of select="a:Directions/a:Direction/a:Route/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:Directions/a:Direction/a:Site/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Vital Signs</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Vital Sign</th>
															<th>Date</th>
															<th>Result</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:VitalSigns/a:Result">
															<tr>
																<td>
																	<xsl:value-of select="a:Description/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																				<xsl:with-param name="fmt">MMM dd, yyyy ':' hh:mm aa zzz</xsl:with-param>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<table class="internal" width="100%">
																		<tbody>
																			<xsl:for-each select="a:Test">
																				<tr>
																					<td width="33%">
																						<strong class="clinical">
																							<xsl:value-of select="a:Description/a:Text"/>
																						</strong>
																					</td>
																					<td width="33%">
																						<xsl:value-of select="a:TestResult/a:Value"/><![CDATA[ ]]>
																						<xsl:value-of select="a:TestResult/a:Units/a:Unit"/>
																					</td>
																					<td width="33%">
																						<xsl:value-of select="a:Flag/a:Text"/>
																					</td>
																				</tr>
																			</xsl:for-each>
																		</tbody>
																	</table>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Encounters</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Location</th>
															<th>Practitioner</th>
															<th>Description</th>
															<th>Indications</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Encounters/a:Encounter">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:for-each select="a:Locations/a:Location">
																		<xsl:value-of select="a:Description/a:Text"/>
																		<br/>
																	</xsl:for-each>
																</td>
																<td>
																	<xsl:for-each select="a:Practitioners/a:Practitioner">
																		<xsl:call-template name="actorName">
																			<xsl:with-param name="objID" select="a:ActorID"/>
																		</xsl:call-template>
																		<br/>
																	</xsl:for-each>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td/>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Social History</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Code</th>
															<th>Description</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:SocialHistory/a:SocialHistoryElement">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:apply-templates select="a:Description/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Family History</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Code</th>
															<th>Description</th>
															<th>Relationship(s)</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:FamilyHistory/a:FamilyProblemHistory">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<xsl:apply-templates select="a:Problem/a:Description/a:Code"/>
																</td>
																<td>
																	<strong class="clinical">
																		<xsl:value-of select="a:Problem/a:Description/a:Text"/>
																	</strong>
																</td>
																<td>
																	<xsl:value-of select="a:FamilyMember/a:ActorRole/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Results (Discrete)</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Test</th>
															<th>Date</th>
															<th>Result</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Results/a:Result[a:Test/a:TestResult/a:Value!='']">
															<tr>
																<td>
																	<xsl:value-of select="a:Description/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																				<xsl:with-param name="fmt">MMM dd, yyyy ':' hh:mm aa zzz</xsl:with-param>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<table class="internal" width="100%">
																		<tbody>
																			<xsl:for-each select="a:Test">
																				<xsl:choose>
																					<xsl:when test="position() mod 2=0">
																						<tr class="even">
																				
																					<td width="33%">
																						<strong class="clinical">
																							<xsl:value-of select="a:Description/a:Text"/>
																						</strong>
																					</td>
																					<td width="33%">
																						<xsl:value-of select="a:TestResult/a:Value"/><![CDATA[ ]]>
																						<xsl:value-of select="a:TestResult/a:Units/a:Unit"/>
																					</td>
																					<td width="33%">
																						<xsl:value-of select="a:Flag/a:Text"/>
																					</td>
																				</tr>
																				</xsl:when>
																				<xsl:otherwise>
																					<tr class="odd">
																					<td width="33%">
																						<strong class="clinical">
																							<xsl:value-of select="a:Description/a:Text"/>
																						</strong>
																					</td>
																					<td width="33%">
																						<xsl:value-of select="a:TestResult/a:Value"/><![CDATA[ ]]>
																						<xsl:value-of select="a:TestResult/a:Units/a:Unit"/>
																					</td>
																					<td width="33%">
																						<xsl:value-of select="a:Flag/a:Text"/>
																					</td>
																				</tr>
																				</xsl:otherwise>
																				</xsl:choose>
																			</xsl:for-each>
																		</tbody>
																	</table>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Results (Report)</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Test</th>
															<th>Date</th>
															<th>Result</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Results/a:Result[a:Test/a:TestResult/a:Description/a:Text!='']">
															<tr>
																<td>
																	<xsl:value-of select="a:Description/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:DateTime"/>
																				<xsl:with-param name="fmt">MMM dd, yyyy ':' hh:mm aa zzz</xsl:with-param>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																	<table class="internal" width="100%">
																		<tbody>
																			<xsl:for-each select="a:Test">
																				<xsl:choose>
																					<xsl:when test="position() mod 2=0">
																						<tr class="even">
																					<td width="20%">
																						<strong class="clinical">
																							<xsl:value-of select="a:Description/a:Text"/>
																						</strong>
																					</td>
																					<td width="65%">
																						<span>
																							<xsl:value-of select="a:TestResult/a:Description/a:Text" disable-output-escaping="yes"/>
																						</span>
																					</td>
																					<td width="15%">
																						<xsl:value-of select="a:Flag/a:Text"/>
																					</td>
																				</tr>
																				</xsl:when>
																				<xsl:otherwise>
																					<tr class="odd">
																					<td width="20%">
																						<strong class="clinical">
																							<xsl:value-of select="a:Description/a:Text"/>
																						</strong>
																					</td>
																					<td width="65%">
																						<xsl:value-of select="a:TestResult/a:Description/a:Text" disable-output-escaping="yes"/>
																					</td>
																					<td width="15%">
																						<xsl:value-of select="a:Flag/a:Text"/>
																					</td>
																				</tr>
																				</xsl:otherwise>
																				</xsl:choose>
																			</xsl:for-each>
																		</tbody>
																	</table>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<tr>
											<td/>
											<td>
												<span class="header">Insurance</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Type</th>
															<th>Date</th>
															<th>Identification Numbers</th>
															<th>Payment Provider</th>
															<th>Subscriber</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:Payers/a:Payer">
															<tr>
																<td>
																	<xsl:value-of select="a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal" width="100%">
																		<tbody>
																			<xsl:for-each select="a:DateTime">
																				<xsl:call-template name="dateTime">
																					<xsl:with-param name="dt" select="."/>
																					<xsl:with-param name="fmt">MMM dd, yyyy ':' hh:mm aa zzz</xsl:with-param>
																				</xsl:call-template>
																			</xsl:for-each>
																		</tbody>
																	</table>
																</td>
																<td>
																	<table class="internal" width="100%" border="1">
																		<tbody>
																			<xsl:for-each select="a:IDs">
																				<xsl:choose>
																					<xsl:when test="position() mod 2=0">
																						<tr class="even">
																							<td width="50%">
																								<xsl:value-of select="a:Type/a:Text"/>:</td>
																							<td width="50%">
																								<xsl:value-of select="a:ID"/>
																							</td>
																						</tr>
																					</xsl:when>
																					<xsl:otherwise>
																						<tr class="odd">
																							<td width="50%">
																								<xsl:value-of select="a:Type/a:Text"/>:</td>
																							<td width="50%">
																								<xsl:value-of select="a:ID"/>
																							</td>
																						</tr>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:for-each>
																		</tbody>
																	</table>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:PaymentProvider/a:ActorID"/>
																	</xsl:call-template></a></td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Subscriber/a:ActorID"/>
																	</xsl:call-template></a></td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare/a:Plan[a:Type/a:Text='Treatment Recommendation']">
										<tr>
											<td/>
											<td>
												<span class="header">Plan Of Care Recommendations</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Description</th>
															<th>Recommendation</th>
															<th>Goal</th>
															<th>Status</th>
															<th>Source</th>															
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare/a:Plan[a:Type/a:Text='Treatment Recommendation']">
															<tr>
																<td>
																		<xsl:value-of select="a:Description/a:Text" />
																</td>
																<td>
																		<xsl:value-of select="a:OrderRequest/a:Description/a:Text" disable-output-escaping="yes"/>
																</td>
																<td>
																		<xsl:value-of select="a:OrderRequest/a:Goals/a:Goal/a:Description/a:Text" disable-output-escaping="yes"/>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>										
										</xsl:if>
										<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare/a:Plan[a:Type/a:Text='Order']">
										<tr>
											<td/>
											<td>
												<span class="header">Plan Of Care Orders</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Descripion</th>
															<th>Plan Status</th>
															<th>Type</th>
															<th>Date</th>															
															<th>Procedure</th>
															<th>Schedule</th>
															<th>Location</th>
															<th>Substance</th>
															<th>Method</th>
															<th>Position</th>
															<th>Site</th>
															<th>Status</th>
															<th>Source</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare/a:Plan[a:Type/a:Text='Order']">
															<tr>
																<td>
																		<xsl:apply-templates select="a:Description/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:Status/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Type/a:Text"/>
																</td>
																<td>
																	<table class="internal">
																		<tbody>
																			<xsl:call-template name="dateTime">
																				<xsl:with-param name="dt" select="a:OrderRequest/a:Procedures/a:Procedure/a:DateTime"/>
																			</xsl:call-template>
																		</tbody>
																	</table>
																</td>
																<td>
																		<xsl:apply-templates select="a:OrderRequest/a:Procedures/a:Procedure/a:Description/a:Text"/>											
																</td>
																<td>
																		<span>Every </span><xsl:apply-templates select="a:OrderRequest/a:Procedures/a:Procedure/a:Interval/a:Value"/><![CDATA[ ]]>
																		<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Interval/a:Units/a:Unit"/><span> for </span>
																		<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Duration/a:Value"/><![CDATA[ ]]>
																		<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Duration/a:Units/a:Unit"/>
																</td>
																<td>
																	<xsl:for-each select="a:OrderRequest/a:Procedures/a:Procedure/a:Locations">
																		<xsl:value-of select="a:Location/a:Description/a:Text"/>
																		<xsl:if test="position() != last()">
																			<br/>
																		</xsl:if>
																	</xsl:for-each>
																</td>
																<td>
																		<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Substance/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Method/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Position/a:Text"/>
																</td>
																<td>
																	<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Site/a:Text"/>
																</td>
																<td/>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>															
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>										
										</xsl:if>

										<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:HealthCareProviders/a:Provider">
										<tr>
											<td/>
											<td>
												<span class="header">Health Care Providers</span>
												<br/>
												<table class="list" width="100%">
													<tbody>
														<tr>
															<th>Role</th>
															<th>Name</th>
														</tr>
														<xsl:for-each select="a:ContinuityOfCareRecord/a:Body/a:HealthCareProviders/a:Provider">
															<tr>
																<td>
																	<xsl:value-of select="a:ActorRole/a:Text"/>
																</td>
																<td><a><xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																	<xsl:call-template name="actorName">
																		<xsl:with-param name="objID" select="a:ActorID"/>
																	</xsl:call-template></a></td>
															</tr>
														</xsl:for-each>
													</tbody>
												</table>
											</td>
										</tr>
										</xsl:if>
										
										<tr>
											<td/>
											<td/>
										</tr>
									</tbody>
								</table>
							</td>
						</tr>
						<tr>
							<td/>
						</tr>
					</tbody>
				</table>
				<span class="header">Actors</span>
				<h4>People</h4>
				<table class="list" width="100%">
					<tbody>
						<tr>
							<th>Name</th>
							<th>Specialty</th>
							<th>Relation</th>
							<th>Identification Numbers</th>
							<th>Phone</th>
							<th>Address/ E-mail</th>
						</tr>
						<xsl:for-each select="a:ContinuityOfCareRecord/a:Actors/a:Actor">
							<xsl:sort select="a:Person/a:Name/a:DisplayName|a:Person/a:Name/a:CurrentName/a:Family" data-type="text" order="ascending"/>
							<xsl:if test="a:Person">
								<tr>
									<td><a><xsl:attribute name="name">
										<xsl:value-of select="a:ActorObjectID"/></xsl:attribute>
										<xsl:call-template name="actorName">
											<xsl:with-param name="objID" select="a:ActorObjectID"/>
										</xsl:call-template></a></td>

									<td>
										<xsl:value-of select="a:Specialty/a:Text"/>
									</td>
									<td>
										<xsl:value-of select="a:Relation/a:Text"/>
									</td>
									<td>
										<table class="internal" width="100%">
											<tbody>
												<xsl:for-each select="IDs">
													<tr>
														<td width="50%">
															<xsl:value-of select="a:Type/a:Text"/>
														</td>
														<td width="50%">
															<xsl:value-of select="a:ID"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</td>
									<td>
										<table class="internal" width="100%">
											<tbody>
												<xsl:for-each select="a:Telephone">
													<tr>
														<td width="50%">
															<xsl:value-of select="a:Type/a:Text"/>
														</td>
														<td width="50%">
															<xsl:value-of select="a:Value"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</td>
									<td>
										<xsl:for-each select="a:Address">
											<xsl:if test="a:Type">
												<b>
													<xsl:value-of select="a:Type/a:Text"/>:</b>
												<br/>
											</xsl:if>
											<xsl:if test="a:Line1">
												<xsl:value-of select="a:Line1"/>
												<br/>
											</xsl:if>
											<xsl:if test="a:Line2">
												<xsl:value-of select="a:Line2"/>
												<br/>
											</xsl:if>
											<xsl:value-of select="a:City"/>,
											<xsl:value-of select="a:State"/>
											<xsl:value-of select="a:PostalCode"/>
											<br/>
										</xsl:for-each>
										<xsl:for-each select="a:Email">
											<br/>
											<xsl:value-of select="a:Value"/>
										</xsl:for-each>
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</tbody>
				</table>
				<h4>Organizations</h4>
				<table class="list" width="100%">
					<tbody>
						<tr>
							<th>Name</th>
							<th>Specialty</th>
							<th>Relation</th>
							<th>Identification Numbers</th>
							<th>Phone</th>
							<th>Address/ E-mail</th>
						</tr>
						<xsl:for-each select="a:ContinuityOfCareRecord/a:Actors/a:Actor">
							<xsl:sort select="a:Organization/a:Name" data-type="text" order="ascending"/>
							<xsl:if test="a:Organization">
								<tr>
									<td><a><xsl:attribute name="name">
										<xsl:value-of select="a:ActorObjectID"/></xsl:attribute>
										<xsl:value-of select="a:Organization/a:Name"/></a></td>

									<td>
										<xsl:value-of select="a:Specialty/a:Text"/>
									</td>
									<td>
										<xsl:value-of select="a:Relation/a:Text"/>
									</td>
									<td>
										<table class="internal" width="100%">
											<tbody>
												<xsl:for-each select="a:IDs">
													<tr>
														<td width="50%">
															<xsl:value-of select="a:Type/a:Text"/>
														</td>
														<td width="50%">
															<xsl:value-of select="a:ID"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</td>
									<td>
										<table class="internal" width="100%">
											<tbody>
												<xsl:for-each select="a:Telephone">
													<tr>
														<td width="50%">
															<xsl:value-of select="a:Type/a:Text"/>
														</td>
														<td width="50%">
															<xsl:value-of select="a:Value"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</td>
									<td>
										<xsl:for-each select="a:Address">
											<xsl:if test="a:Type">
												<b>
													<xsl:value-of select="a:Type/a:Text"/>:</b>
												<br/>
											</xsl:if>
											<xsl:if test="a:Line1">
												<xsl:value-of select="a:Line1"/>
												<br/>
											</xsl:if>
											<xsl:if test="a:Line2">
												<xsl:value-of select="a:Line2"/>
												<br/>
											</xsl:if>
											<xsl:value-of select="a:City"/>,
											<xsl:value-of select="a:State"/>
											<xsl:value-of select="a:PostalCode"/>
											<br/>
										</xsl:for-each>
										<xsl:for-each select="a:Email">
											<br/>
											<xsl:value-of select="a:Value"/>
										</xsl:for-each>
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</tbody>
				</table>
				<h4>Information Systems</h4>
				<table class="list" width="100%">
					<tbody>
						<tr>
							<th>Name</th>
							<th>Type</th>
							<th>Version</th>
							<th>Identification Numbers</th>
							<th>Phone</th>
							<th>Address/ E-mail</th>
						</tr>
						<xsl:for-each select="a:ContinuityOfCareRecord/a:Actors/a:Actor">
						    <xsl:sort select="a:InformationSystem/a:Name" data-type="text" order="ascending"/>							
							<xsl:if test="a:InformationSystem">
								<tr>
									<td><a><xsl:attribute name="name">
										<xsl:value-of select="a:ActorObjectID"/></xsl:attribute>
										<xsl:value-of select="a:InformationSystem/a:Name"/></a></td>

									<td>
										<xsl:value-of select="a:InformationSystem/a:Type"/>
									</td>
									<td>
										<xsl:value-of select="a:InformationSystem/a:Version"/>
									</td>
									<td>
										<table class="internal" width="100%">
											<tbody>
												<xsl:for-each select="a:IDs">
													<tr>
														<td width="50%">
															<xsl:value-of select="a:Type/a:Text"/>
														</td>
														<td width="50%">
															<xsl:value-of select="a:ID"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</td>
									<td>
										<table class="internal" width="100%">
											<tbody>
												<xsl:for-each select="a:Telephone">
													<tr>
														<td width="50%">
															<xsl:value-of select="a:Type/a:Text"/>
														</td>
														<td width="50%">
															<xsl:value-of select="a:Value"/>
														</td>
													</tr>
												</xsl:for-each>
											</tbody>
										</table>
									</td>
									<td>
										<xsl:for-each select="a:Address">
											<xsl:if test="Type">
												<b>
													<xsl:value-of select="a:Type/a:Text"/>:</b>
												<br/>
											</xsl:if>
											<xsl:if test="a:Line1">
												<xsl:value-of select="a:Line1"/>
												<br/>
											</xsl:if>
											<xsl:if test="a:Line2">
												<xsl:value-of select="a:Line2"/>
												<br/>
											</xsl:if>
											<xsl:value-of select="a:City"/>,
											<xsl:value-of select="a:State"/>
											<xsl:value-of select="a:PostalCode"/>
											<br/>
										</xsl:for-each>
										<xsl:for-each select="a:Email">
											<br/>
											<xsl:value-of select="a:Value"/>
										</xsl:for-each>
									</td>
								</tr>
							</xsl:if>
						</xsl:for-each>
					</tbody>
				</table>
			</body>
		</html>
	</xsl:template>

	<!-- Returns the name of the actor, if there is no name it returns the ActorObjectID that was passed in -->
	<xsl:template name="actorName">
		<xsl:param name="objID"/>
		<xsl:for-each select="/a:ContinuityOfCareRecord/a:Actors/a:Actor">
			<xsl:variable name="thisObjID" select="a:ActorObjectID"/>
			<xsl:if test="$objID = $thisObjID">
				<xsl:choose>
					<xsl:when test="a:Person">
						<xsl:choose>
							<xsl:when test="a:Person/a:Name/a:DisplayName">
								<xsl:value-of select="a:Person/a:Name/a:DisplayName"/>
							</xsl:when>
							<xsl:when test="a:Person/a:Name/a:CurrentName">
								<xsl:value-of select="a:Person/a:Name/a:CurrentName/a:Given"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:CurrentName/a:Middle"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:CurrentName/a:Family"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:CurrentName/a:Suffix"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:CurrentName/a:Title"/><![CDATA[ ]]>
								</xsl:when>
							<xsl:when test="a:Person/a:Name/a:BirthName">
								<xsl:value-of select="a:Person/a:Name/a:BirthName/a:Given"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:BirthName/a:Middle"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:BirthName/a:Family"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:BirthName/a:Suffix"/><![CDATA[ ]]>
								<xsl:value-of select="a:Person/a:Name/a:BirthName/a:Title"/><![CDATA[ ]]>
								</xsl:when>
							<xsl:when test="a:Person/a:Name/a:AdditionalName">
								<xsl:for-each select="a:Person/a:Name/a:AdditionalName">
									<xsl:value-of select="a:Given"/><![CDATA[ ]]>
									<xsl:value-of select="a:Middle"/><![CDATA[ ]]>
									<xsl:value-of select="a:Family"/><![CDATA[ ]]>
									<xsl:value-of select="a:Suffix"/><![CDATA[ ]]>
									<xsl:value-of select="a:Title"/><![CDATA[ ]]>
									<xsl:if test="position() != last()">
										<br/>
									</xsl:if>
								</xsl:for-each>
							</xsl:when>
						</xsl:choose>
					</xsl:when>
					<xsl:when test="a:Organization">
						<xsl:value-of select="a:Organization/a:Name"/>
					</xsl:when>
					<xsl:when test="a:InformationSystem">
						<xsl:value-of select="a:InformationSystem/a:Name"/><![CDATA[ ]]>
						<xsl:if test="a:InformationSystem/a:Version">
							<xsl:value-of select="a:InformationSystem/a:Version"/><![CDATA[ ]]></xsl:if>
						<xsl:if test="a:InformationSystem/a:Type">(<xsl:value-of select="a:Type"/>)</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$objID"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<!-- End actorname template -->

	<!-- Displays the DateTime.  If ExactDateTime is present, it will format according
		 to the 'fmt' variable. The default format is: Oct 31, 2005 -->
	<xsl:template name="dateTime" match="DateTime">
		<xsl:param name="dt" select="."/>
		<xsl:param name="fmt">MMM dd, yyyy</xsl:param>
		<tr>
			<xsl:if test="$dt/a:Type/a:Text">
				<td>
					<xsl:value-of select="$dt/a:Type/a:Text"/>:</td>
			</xsl:if>
			<xsl:choose>
				<xsl:when test="$dt/a:ExactDateTime">
					<td>								
					<xsl:call-template name="date:format-date">
							<xsl:with-param name="date-time">
								<xsl:value-of select="$dt/a:ExactDateTime"/>
								</xsl:with-param>
							<xsl:with-param name="pattern" select="$fmt"/>
						</xsl:call-template>
					</td>
				</xsl:when>
				<xsl:when test="$dt/a:Age">
					<td>
						<xsl:value-of select="$dt/a:Age/a:Value"/><![CDATA[ ]]>
						<xsl:value-of select="$dt/a:Age/a:Units/a:Unit"/>
					</td>
				</xsl:when>
				<xsl:when test="$dt/a:ApproximateDateTime">
					<td>
						<xsl:value-of select="$dt/a:ApproximateDateTime/a:Text"/>
					</td>
				</xsl:when>
				<xsl:when test="$dt/a:DateTimeRange">
					<td>
						<xsl:for-each select="$dt/a:DateTimeRange/a:BeginRange">
							<xsl:choose>
								<xsl:when test="$dt/a:ExactDateTime">
									<xsl:call-template name="date:format-date">
										<xsl:with-param name="date-time">
											<xsl:value-of select="$dt/a:ExactDateTime"/>
										</xsl:with-param>
										<xsl:with-param name="pattern" select="$fmt"/>
									</xsl:call-template>
								</xsl:when>
								<xsl:when test="$dt/a:Age">
									<xsl:value-of select="$dt/a:Age/a:Value"/><![CDATA[ ]]>
									<xsl:value-of select="$dt/a:Age/a:Units/a:Unit"/>
								</xsl:when>
								<xsl:when test="$dt/a:ApproximateDateTime">
									<xsl:value-of select="$dt/a:ApproximateDateTime/a:Text"/>
								</xsl:when>
								<xsl:otherwise/>
							</xsl:choose>
						</xsl:for-each><![CDATA[ ]]>
				-<![CDATA[ ]]>
						<xsl:for-each select="$dt/a:DateTimeRange/a:EndRange">
							<xsl:choose>
								<xsl:when test="$dt/a:ExactDateTime">
									<xsl:call-template name="date:format-date">
										<xsl:with-param name="date-time">
											<xsl:value-of select="$dt/a:ExactDateTime"/>
										</xsl:with-param>
										<xsl:with-param name="pattern" select="$fmt"/>
									</xsl:call-template>
								</xsl:when>
								<xsl:when test="$dt/a:Age">
									<xsl:value-of select="$dt/a:Age/a:Value"/><![CDATA[ ]]>
									<xsl:value-of select="$dt/a:Age/a:Units/a:Unit"/>
								</xsl:when>
								<xsl:when test="$dt/a:ApproximateDateTime">
									<xsl:value-of select="$dt/a:ApproximateDateTime/a:Text"/>
								</xsl:when>
								<xsl:otherwise/>
							</xsl:choose>
						</xsl:for-each>
					</td>
				</xsl:when>
				<xsl:otherwise/>
			</xsl:choose>
		</tr>
	</xsl:template>
	<!-- End DateTime template -->

	<xsl:template match="a:Code">
		<xsl:value-of select="a:Value"/>
		<xsl:if test="a:CodingSystem">
			<![CDATA[ ]]>(<xsl:value-of select="a:CodingSystem"/>)
		</xsl:if>
	</xsl:template>
</xsl:stylesheet>
