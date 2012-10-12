<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page isELIgnored="false" %> 
  <c:set var="xStaggerOffset" value="20"/>
  <c:set var="maxThumbs" value="8"/>
  <c:set var="xOffset" value="0"/>
  <c:set var="yOffset" value="0"/>
  <c:set var="display" value="none"/>
  <c:set var="showContent" value="true"/>

  <tiles:importAttribute name="xOffset" ignore="true"/>
  <tiles:importAttribute name="yOffset" ignore="true"/>
  <tiles:importAttribute name="maxThumbs" ignore="true"/>
  <tiles:importAttribute name="showContent" ignore="true"/>
  <tiles:useAttribute name="index"/>
  <tiles:useAttribute name="ccr" id="ccr"/>
  <tiles:useAttribute name="idPrefix"/>
  <tiles:useAttribute name="numSeries"/>
  <tiles:importAttribute name="display"/>

  <c:set var="ccrDom" value="${ccr.JDOMDocument}" scope="request"/>
  <c:set var='top' value='${yOffset + (index * -20) }'/>
  <c:set var='left' value='${xOffset + (index * xStaggerOffset)}'/>
  <c:set var="zIndex" value="${ 60 - $index * 3}"/>
  <c:set var="creationDate"><dt:format pattern="MM/dd/yyyy">${ccr.createTimeMs}</dt:format></c:set>

  <div id="${idPrefix}ccrRecord-${index}" style="display: ${display}; position: absolute; top: ${top}; left: ${left}; z-index: ${zIndex};">      
    
    <div id="${idPrefix}folder-${index}" class="folder">

    <div class="dividerLine"  id="${idPrefix}dividerLine-${index}">&nbsp;</div>

      <div id="${idPrefix}folderTab-${index}" class="folderTab" style="z-index: ${zIndex + 1}; " 
        onclick="toggleFolder( '${idPrefix}',${index} );">
        <img id="${idPrefix}foldertabbutton-${index}" class="folderTabButton" src="images/minusbutton.png"/>
        &#160;&#160;<span id="${idPrefix}creationDate-${index}">${creationDate}</span>
      </div>

      <table class="ccrCover" id='${idPrefix}ccrCover-${index}' style="display: ${display};" cellpadding="3" cellspacing="0">
        <tr><td class="leftnav">PHYSICIAN</td><td class="ltrtxt" width="280px" ><span id='${idPrefix}physician-${index}'><%=System.getProperty("user.name")%></span></td></tr>
        <tr><td class="leftnav">&nbsp;</td>
            <td>
              <table class="notificationsHeader" id="${idPrefix}notificationsHeader-${index}" cellspacing="0" width="100%">
                <tr><td width="45%">Notifications</td><td>MedCommons</td></tr>
              </table>
            </td>
        </tr>
        <tr><td class="leftnav" >TO</td>
          <td class="ltrtxt" rowspan="3" id="${idPrefix}notificationTable-${index}">
              <table class="notificationsTable" cellspacing="0" width="100%">
                <tr>
                  <td width="45%" style="border-right:none; font-size: 60%;">
                    <mc:xvalue bean="ccrDom" path="toEmail"/>&nbsp;
                   </td>
                  <td style="border-bottom: none;">
                  <%--
                    <span style="float: left;">Acct#</span><span style="float: right; word-wrap: normal;">
                        <mc:xnode bean="ccrDom" path="patientMedCommonsId" name="patientMedCommonsId"/>
                        <c:choose>
                        <c:when test='${empty patientMedCommonsId.textTrim}'>#9999 9999 9999 9999</c:when>
                        <c:otherwise><mc:medcommonsId>${patientMedCommonsId.textTrim}</mc:medcommonsId></c:otherwise>
                      </c:choose>
                    </span>
                    --%>
                   <span style="float: left;">Tracking#</span>
                   <span style="float: right;">
                      <c:choose>
                        <c:when test='${empty ccr.trackingNumber}'>(New)</c:when>
                        <c:otherwise><mc:tracknum>${ccr.trackingNumber}</mc:tracknum></c:otherwise>
                      </c:choose>
                   </span>
                  </td>
                </tr>
                <c:set var="sourceEmail"><mc:xvalue bean="ccrDom" path="sourceEmail" /></c:set>
                <tr><td style="border-top: none; border-bottom: none; border-right: none; font-size: 9px;">
                  <c:choose><c:when test='${empty sourceEmail}'>&nbsp;</c:when><c:otherwise>${sourceEmail}</c:otherwise></c:choose>
                  </td>
                  <td style="border-top: none; border-bottom: none;">&nbsp;</td>
                </tr>
                <tr>
                <c:set var="patientEmail"><mc:xvalue bean="ccrDom" path="patientEmail" /></c:set>
                  <td style="border-right: none; font-size: 9px;">
                    <c:choose><c:when test='${empty patientEmail}'>&nbsp;</c:when><c:otherwise>${patientEmail}</c:otherwise></c:choose>
                  </td>
                  <td style="border-top: none;">&nbsp;</td>
                </tr>
              </table>
          </td>
        </tr>
        <tr><td class="leftnav">FROM</td></tr>
        <tr><td class="leftnav">PATIENT EMAIL</td></tr>
        <tr><td class="leftnav">&nbsp</td><td class="ltrtxt">&nbsp</td></tr>
        <tr><td class="leftnav">PURPOSE</td><td class="ltrtxt"><mc:xvalue bean="ccrDom" path="ccrPurpose"/></td></tr>
        <tr><td class="leftnav" height="400">COMMENT
          <span class="leftnav" valign="top" id="${idPrefix}bodySectionCell-${index}" style='padding-top: 30px'>
            <c:if test="${showContent}">
              <c:set var="sections">Insurance,Medications,Advance Directives,Functional Status,Support,Vital Signs,Immunizations,Procedures,Problems,Encounters,Family History,Plan Of Care,Social History,Alerts,Health Care Providers</c:set> 
              <c:forTokens var="section" items="${sections}" delims=",">
                <c:set var='bodyCount'><mc:xvalue bean="ccrDom" path="count(//x:Body/*[local-name()=translate($section,' ','')])"/></c:set>
                <div <c:choose><c:when test="${bodyCount == '0.0' }">class="missing"</c:when>
                    <c:otherwise>
                      class="clickable" 
                      onclick="document.location.href='initViewer.do';"
                    </c:otherwise>
                  </c:choose>
                  >
                  ${fn:toUpperCase(section)}
                 </div>
              </c:forTokens>
            </c:if>
            </span>
        </td>
            <td class="ltrtxt">            
            <c:set var="purposeText"><mc:xvalue bean="ccrDom" path="purposeText"/></c:set>
            <textarea rows="16" readonly="true" style="padding-bottom: 5px; border-style: none; border-width: 0;  width: 280px; overflow: hidden; font-size: 10px; font-family: arial;"
             id="${idPrefix}ccrPurpose-${index}"><c:if test="${showContent}">${fn:substring(purposeText,0,610)}<c:if test='${fn:length(purposeText) > 610}'> (more ...)</c:if></c:if></textarea>
            </td>
        </tr>
      </table>

      <tiles:insert page="thumbnailContainer.jsp">
        <tiles:put name="maxThumbs" value="${maxThumbs}"/>
        <tiles:put name="index" value="${index}"/>
        <tiles:put name="idPrefix" value="${idPrefix}"/>
        <tiles:put name="enclosures" value="${fn:length(ccr.seriesList)-1} Enclosure(s)"/>
      </tiles:insert>

      <div id="${idPrefix}moreButton-${index}" class="moreButton"><img src="images/more.gif" onclick="showWado(${index},0);"/></div>
    </div> <!-- folder -->
  </div> <!-- ccrRecord -->

