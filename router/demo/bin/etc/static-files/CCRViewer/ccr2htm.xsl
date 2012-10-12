<?xml version="1.0" encoding="utf-8"?>
<!-- 
Simple HTML representation of the ASTM Continuity of Care Record. This representation
does not present all the potential data storable in the CCR.  Instead it gives a
clinical representation of the CCR instance.

  Author:   	Steven E. Waldren, MD 
  				    American Academy of Family Physicians
				    swaldren@aafp.org

  Coauthors:	Ken Miller      Simon Sadedin
				        Solventus      Medcommons

  Date: 	2005-02-01
  Version: 	1.2

 -->
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:a="urn:astm-org:CCR" xmlns:date="http://exslt.org/dates-and-times" exclude-result-prefixes="a date">
	<!-- From the http://www.exslt.org library and used for date processing -->
	<xsl:import href="./lib/date.format-date.template.xsl"/>
	<!-- Import sub templates -->
	<xsl:import href="./templates/code.xsl"/>
	<xsl:import href="./templates/datetime.xsl"/>
	<xsl:import href="./templates/actor.xsl"/>
	<xsl:import href="./templates/footer.xsl"/>
	<xsl:import href="./templates/problemDescription.xsl"/>
	<xsl:import href="./templates/directions.xsl"/>
	<xsl:import href="./templates/defaultCSS.xsl"/>
	
	<xsl:output method="html" encoding="UTF-8"/>
	<!-- XSL Parameters -->
	<!-- This param can be used to define diference CCS style sheets
			If not passed, default.css will be used -->
	<xsl:param name="stylesheet"/>
	<xsl:template match="/">
		<html xmlns:date="http://exslt.org/dates-and-times">
			<head>
				<!-- Load in the CSS file -->
				<xsl:choose>
					<xsl:when test="$stylesheet!=''">
						<link href="{$stylesheet}" rel="stylesheet" type="text/css"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:call-template name="defaultCCS"/> <!-- call to ./templates/defaultCCS.xsl-->
					</xsl:otherwise>
				</xsl:choose>
				
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
										<tr id="ccrheaderrow">
											<td/>
											<td>
												<h1>Continuity of Care Record
													<br/>
												</h1>
												<table id="ccrheader" cellSpacing="3" cellPadding="1" width="75%" bgColor="#ffffcc">
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
                                <em><xsl:value-of select="a:ContinuityOfCareRecord/a:Purpose/a:Description/a:Text"/></em>
                                <xsl:if test="string-length(normalize-space(a:ContinuityOfCareRecord/a:Purpose/a:Description/a:Text))!=0">
                                  <br/>
                                </xsl:if>

                                <xsl:for-each select="a:ContinuityOfCareRecord/a:Comments/a:Comment[a:Type/a:Text='MedCommons User Comment']/a:Description/a:Text[string-length(.)!=0]">
                                  <span class="commentHeading">Subjective</span>
                                  <br/>
                                  <xsl:value-of select="."/>
                                </xsl:for-each>
                                <xsl:for-each select="a:ContinuityOfCareRecord/a:Comments/a:Comment[a:Type/a:Text='MedCommons Objective']/a:Description/a:Text[string-length(.)!=0]">                                  
                                  <br/>
                                  <span class="commentHeading">Objective</span>
                                  <br/>
                                  <xsl:value-of select="."/>
                                </xsl:for-each>
                                <xsl:for-each select="a:ContinuityOfCareRecord/a:Comments/a:Comment[a:Type/a:Text='MedCommons Assessment']/a:Description/a:Text[string-length(.)!=0]">
                                  <br/>
                                  <span class="commentHeading">Assessment</span>
                                  <br/>
                                  <xsl:value-of select="."/>
                                </xsl:for-each>
                                <xsl:for-each select="a:ContinuityOfCareRecord/a:Comments/a:Comment[a:Type/a:Text='MedCommons Plan']/a:Description/a:Text[string-length(.)!=0]">
                                  <br/>
                                  <span class="commentHeading">Plan</span>
                                  <br/>
                                  <xsl:value-of select="."/>
                                </xsl:for-each>
															</td>
														</tr>
													</tbody>
												</table>
												<br/>
											</td>
										</tr>
										<tr id="demographicsrow">
											<td/>
											<td>
												<span class="header">Patient Demographics</span>
												<br/>
												<table id="demographics" class="list" width="100%">
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
										<span id="ccrcontent">
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Alerts">
												<tr id="alertsrow">
													<td/>
													<td>
														<span class="header">Alerts</span>
														<br/>
														<table id="alerts" class="list" width="100%">
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
																			<xsl:value-of select="a:Reaction/a:Description/a:Text"/>
																			<xsl:if test="a:Reaction/a:Severity/a:Text">-
																	<xsl:value-of select="a:Reaction/a:Severity/a:Text"/>
																			</xsl:if>
																		</td>
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:AdvanceDirectives">
												<tr id="advancedirectivesrow">
													<td/>
													<td>
														<span class="header">Advance Directives</span>
														<br/>
														<table id="advancedirectives" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Support">
												<tr id="supportprovidersrow">
													<td/>
													<td>
														<span id="supportproviders" class="header">Support Providers</span>
														<br/>
                              <table class="list" id="support" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:FunctionalStatus">
												<tr id="functionalstatusrow">
													<td/>
													<td>
														<span class="header">Functional Status</span>
														<br/>
														<table id="functionalstatus" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Problems">
												<tr id="problemsrow">
													<td/>
													<td>
														<span class="header">Problems</span>
														<br/>
														<table id="problems" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Procedures">
												<tr id="proceduresrow">
													<td/>
													<td>
														<span class="header">Procedures</span>
														<br/>
														<table id="procedures" class="list" width="100%">
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
																					<xsl:if test="a:Actor/a:ActorRole/a:Text"><![CDATA[ ]]>-<![CDATA[ ]]><xsl:value-of select="a:ActorRole/a:Text"/>)</xsl:if>
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Medications">
												<tr id="medicationsrow">
													<td/>
													<td>
														<span id="medications" class="header">Medications</span>
														<br/>
														<table class="list" width="100%">
															<tbody>
																<tr>
																<th>Medication</th>
																<th>Date</th>
																<th>Status</th>
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
																			<xsl:if test="a:Product/a:BrandName"><![CDATA[ ]]>(<xsl:value-of select="a:Product/a:BrandName/a:Text"/>)
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
																		<xsl:value-of select="a:Status/a:Text"/>
																	</td>
																	<td>
																		<xsl:value-of select="a:Product/a:Form/a:Text"/>
																	</td>
																	<td>
																		<xsl:for-each select="a:Product/a:Strength">
																			<xsl:if test="position() &gt; 1">
																			/
																		</xsl:if>
																			<xsl:value-of select="a:Value"/><![CDATA[ ]]><xsl:value-of select="a:Units/a:Unit"/>
																		</xsl:for-each>
																	</td>
																	<td>
																		<xsl:value-of select="a:Quantity/a:Value"/><![CDATA[ ]]><xsl:value-of select="a:Quantity/a:Units/a:Unit"/>
																	</td>
																	<td>
																		<table class="internal" width="100%" border="1">
																			<tbody>
																				<xsl:apply-templates select="a:Directions"/> <!-- call to /templates/directions.xsl -->
																			</tbody>
																		</table>
																	</td>
																	<td>
																		<xsl:for-each select="a:Indications/a:Indication">
																			<xsl:call-template name="problemDescription">
																				<xsl:with-param name="objID" select="a:InternalCCRLink/a:LinkID"/>
																			</xsl:call-template>
																			<br/>
																		</xsl:for-each>
																	</td>
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
																	<td>
																		<a>
																			<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																			<xsl:call-template name="actorName">
																				<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																			</xsl:call-template>
																		</a>
																	</td>
																</tr>
															</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Immunizations">
												<tr id="immunizationsrow">
													<td/>
													<td>
														<span class="header">Immunizations</span>
														<br/>
														<table id="immunizations" class="list" width="100%">
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
																				<xsl:if test="a:Product/a:Form"><![CDATA[ ]]>(<xsl:value-of select="a:Product/a:Form/a:Text"/>)
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:VitalSigns">
												<tr id="vitalsignsrow">
													<td/>
													<td>
														<span class="header">Vital Signs</span>
														<br/>
														<table id="vitalsigns" class="list" width="100%">
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
																								<xsl:value-of select="a:TestResult/a:Value"/><![CDATA[ ]]><xsl:value-of select="a:TestResult/a:Units/a:Unit"/>
																							</td>
																							<td width="33%">
																								<xsl:value-of select="a:Flag/a:Text"/>
																							</td>
																						</tr>
																					</xsl:for-each>
																				</tbody>
																			</table>
																		</td>
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Encounters">
												<tr id="encountersrow">
													<td/>
													<td>
														<span class="header">Encounters</span>
														<br/>
														<table id="encounters" class="list" width="100%">
															<tbody>
																<tr>
																	<th>Type</th>
																	<th>Date</th>
																	<th>Location</th>
																	<th>Status</th>
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
																			<xsl:call-template name="actorName">
																				<xsl:with-param name="objID" select="a:Actor/a:ActorID"/>
																			</xsl:call-template>
																			<br/>
																		</xsl:for-each>
																		</td>
																		<td>
																		<xsl:value-of select="a:Status/a:Text"/>
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
																		<td>
																		<xsl:for-each select="a:Indications/a:Indication">
																			<xsl:call-template name="problemDescription">
																				<xsl:with-param name="objID" select="a:InternalCCRLink/a:LinkID"/>
																			</xsl:call-template>
																			<br/>
																		</xsl:for-each>
																	</td>
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:SocialHistory">
												<tr id="socialhistoryrow">
													<td/>
													<td>
														<span class="header">Social History</span>
														<br/>
														<table id="socialhistory" class="list" width="100%">
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
																				<span>
																					<xsl:value-of select="a:Description/a:Text" disable-output-escaping="yes"/>
																				</span>
																			</strong>
																		</td>
																		<td>
																			<xsl:value-of select="a:Status/a:Text"/>
																		</td>
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:FamilyHistory">
												<tr id="familyhistoryrow">
													<td/>
													<td>
														<span class="header">Family History</span>
														<br/>
														<table id="familyhistory" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Results/a:Result[a:Test/a:TestResult/a:Value!='']">
												<tr id="resultsrow">
													<td/>
													<td>
														<span class="header">Results (Discrete)</span>
														<br/>
														<table id="results" class="list" width="100%">
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
																										<xsl:value-of select="a:TestResult/a:Value"/><![CDATA[ ]]><xsl:value-of select="a:TestResult/a:Units/a:Unit"/>
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
																										<xsl:value-of select="a:TestResult/a:Value"/><![CDATA[ ]]><xsl:value-of select="a:TestResult/a:Units/a:Unit"/>
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
												</xsl:if>
												<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Results/a:Result[a:Test/a:TestResult/a:Description/a:Text!='']">
												<tr id="resultsreportrow">
													<td/>
													<td>
														<span class="header">Results (Report)</span>
														<br/>
														<table id="resultsreport" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:Payers">
												<tr id="payersrow">
													<td/>
													<td>
														<span class="header">Insurance</span>
														<br/>
														<table id="payers" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:PaymentProvider/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Subscriber/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare">
												<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare/a:Plan[a:Type/a:Text='Treatment Recommendation']">
													<tr id="planofcarerow">
														<td/>
														<td>
															<span class="header">Plan Of Care Recommendations</span>
															<br/>
															<table id="planofcare" class="list" width="100%">
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
																				<xsl:value-of select="a:Description/a:Text"/>
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
																			<td>
																				<a>
																					<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																					<xsl:call-template name="actorName">
																						<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																					</xsl:call-template>
																				</a>
																			</td>
																		</tr>
																	</xsl:for-each>
																</tbody>
															</table>
														</td>
													</tr>
												</xsl:if>
												<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:PlanOfCare/a:Plan[a:Type/a:Text='Order']">
													<tr id="planofcareordersrow">
														<td/>
														<td>
															<span class="header">Plan Of Care Orders</span>
															<br/>
															<table id="planofcareorders" class="list" width="100%">
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
																				<span>Every </span>
																				<xsl:apply-templates select="a:OrderRequest/a:Procedures/a:Procedure/a:Interval/a:Value"/><![CDATA[ ]]><xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Interval/a:Units/a:Unit"/>
																				<span> for </span>
																				<xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Duration/a:Value"/><![CDATA[ ]]><xsl:value-of select="a:OrderRequest/a:Procedures/a:Procedure/a:Duration/a:Units/a:Unit"/>
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
																			<td>
																				<a>
																					<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																					<xsl:call-template name="actorName">
																						<xsl:with-param name="objID" select="a:Source/a:Actor/a:ActorID"/>
																					</xsl:call-template>
																				</a>
																			</td>
																		</tr>
																	</xsl:for-each>
																</tbody>
															</table>
														</td>
													</tr>
												</xsl:if>
											</xsl:if>
											<xsl:if test="a:ContinuityOfCareRecord/a:Body/a:HealthCareProviders">
												<tr id="healthcareprovidersrow">
													<td/>
													<td>
														<span class="header">Health Care Providers</span>
														<br/>
														<table id="healthcareproviders" class="list" width="100%">
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
																		<td>
																			<a>
																				<xsl:attribute name="href">#
																	<xsl:value-of select="a:Source/a:Actor/a:ActorID"/></xsl:attribute>
																				<xsl:call-template name="actorName">
																					<xsl:with-param name="objID" select="a:ActorID"/>
																				</xsl:call-template>
																			</a>
																		</td>
																	</tr>
																</xsl:for-each>
															</tbody>
														</table>
													</td>
												</tr>
											</xsl:if>
										</span>
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
				<br/>
				<span id="actors">
					<span class="header">Actors</span>
					<xsl:if test="a:ContinuityOfCareRecord/a:Actors/a:Actor[a:Person]">
						<span id="people">
							<h4>People</h4>
							<table id="actorstable" class="list" width="100%">
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
												<td>
													<a>
														<xsl:attribute name="name"><xsl:value-of select="a:ActorObjectID"/></xsl:attribute>
														<xsl:call-template name="actorName">
															<xsl:with-param name="objID" select="a:ActorObjectID"/>
														</xsl:call-template>
													</a>
												</td>
												<td>
													<xsl:value-of select="a:Specialty/a:Text"/>
												</td>
												<td>
													<xsl:value-of select="a:Relation/a:Text"/>
												</td>
												<td>
													<table class="internal" width="100%">
														<tbody>
															<xsl:for-each select="a:IDs/a:ID">
																<tr>
																	<td width="50%">
																		<xsl:value-of select="a:Type/a:Text"/>
																	</td>
																	<td width="50%">
																		<xsl:value-of select="."/>
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
													<xsl:for-each select="a:EMail">
														<br/>
														<xsl:value-of select="a:Value"/>
													</xsl:for-each>
												</td>
											</tr>
										</xsl:if>
									</xsl:for-each>
								</tbody>
							</table>
						</span>
					</xsl:if>
					<xsl:if test="a:ContinuityOfCareRecord/a:Actors/a:Actor[a:Organization]">
						<span id="organizations">
							<h4>Organizations</h4>
							<table id="organizationstable" class="list" width="100%">
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
												<td>
													<a>
														<xsl:attribute name="name"><xsl:value-of select="a:ActorObjectID"/></xsl:attribute>
														<xsl:value-of select="a:Organization/a:Name"/>
													</a>
												</td>
												<td>
													<xsl:value-of select="a:Specialty/a:Text"/>
												</td>
												<td>
													<xsl:value-of select="a:Relation/a:Text"/>
												</td>
												<td>
													<table class="internal" width="100%">
														<tbody>
															<xsl:for-each select="a:IDs/a:ID">
																<tr>
																	<td width="50%">
																		<xsl:value-of select="a:Type/a:Text"/>
																	</td>
																	<td width="50%">
																		<xsl:value-of select="."/>
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
													<xsl:for-each select="a:EMail">
														<br/>
														<xsl:value-of select="a:Value"/>
													</xsl:for-each>
												</td>
											</tr>
										</xsl:if>
									</xsl:for-each>
								</tbody>
							</table>
						</span>
					</xsl:if>
					<xsl:if test="a:ContinuityOfCareRecord/a:Actors/a:Actor[a:InformationSystem]">
						<span id="informationsystems">
							<h4>Information Systems</h4>
							<table id="informationsystemstable" class="list" width="100%">
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
												<td>
													<a>
														<xsl:attribute name="name"><xsl:value-of select="a:ActorObjectID"/></xsl:attribute>
														<xsl:value-of select="a:InformationSystem/a:Name"/>
													</a>
												</td>
												<td>
													<xsl:value-of select="a:InformationSystem/a:Type"/>
												</td>
												<td>
													<xsl:value-of select="a:InformationSystem/a:Version"/>
												</td>
												<td>
													<table class="internal" width="100%">
														<tbody>
															<xsl:for-each select="a:IDs/a:ID">
																<tr>
																	<td width="50%">
																		<xsl:value-of select="a:Type/a:Text"/>
																	</td>
																	<td width="50%">
																		<xsl:value-of select="."/>
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
													<xsl:for-each select="a:EMail">
														<br/>
														<xsl:value-of select="a:Value"/>
													</xsl:for-each>
												</td>
											</tr>
										</xsl:if>
									</xsl:for-each>
								</tbody>
							</table>
						</span>
					</xsl:if>
				</span>
				<xsl:call-template name="footer"/>
			</body>
		</html>
	</xsl:template>
</xsl:stylesheet>
