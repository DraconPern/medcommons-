<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:a="urn:astm-org:CCR"  xmlns:fo="http://www.w3.org/1999/XSL/Format">
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
</xsl:stylesheet>
