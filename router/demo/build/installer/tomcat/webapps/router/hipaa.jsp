<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>

<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page import="net.medcommons.*" %>
<%@ page isELIgnored="false" %> 

<div id="hipaa">
  <div id="hipaaHeader">
  Security and HIPAA Log - Recent Account Activity
    &nbsp;&nbsp;&nbsp;
    <strong>Patient: 
    <c:if test='${fn:length(ccrs) > 0}'>
      <mc:xvalue bean="currentCcr" property="JDOMDocument" path="patientName"/>
    </c:if>
    </strong>
  </div>

  <% log("Fetching hipaa log..."); long startMs=System.currentTimeMillis();%>

  <%--

  <c:set var="hipaaId" value="${medcommonsId}"/>
  <c:if test='${!empty originalPatientMedCommonsId}'>
    <c:set var="hipaaId" value="${originalPatientMedCommonsId}"/>
  </c:if>
  --%>

  <%-- count so that we can limit results to 2 even though we draw from multiple sources --%>
  <% int count=0; %>
  <table class="hipaaLogTable">
    <tr><th width="125">CCR Date/Time</th><th width="170">MedCommons Tracking #</th><th width="190">Sender</th><th width="230">Date/Time of last Status Change</th><th width="80">Status</th></tr>
    <c:choose>
      <c:when test='${currentCcr.demo}'>
        <tr>
          <td><dt:format pattern="MM/dd/yyyy HH:mm:ss">${currentCcr.createTimeMs}</dt:format></td>
          <td><mc:tracknum>${currentCcr.trackingNumber}</mc:tracknum></td>
          <td>gateway001.test.medcommons.net</td>
          <td><dt:format pattern="MM/dd/yyyy HH:mm:ss"><%=(System.currentTimeMillis()-120000)%></dt:format></td>
          <td>Demo</td>
        </tr>
      </c:when>
      <c:otherwise>
          <c:forTokens var="hipaaIdName" items="medcommonsId originalPatientMedCommonsId" delims=" ">
            <logic:present name="${hipaaIdName}">
              <c:set var="hipaaId"><bean:write name="${hipaaIdName}"/></c:set>
              <c:url value='<%=net.medcommons.router.configuration.Configuration.getProperty("LogServiceUrl")%>' var="hipaaUrl">
                <c:param name="t1" value="hipaa"/>
                <c:param name="t2" value="2"/>
                <c:param name="a2" value="${hipaaId}"/>
                <c:param name="q" value="2"/>
              </c:url>
              <bean:define id="hipaaUrl" name="hipaaUrl"/>
              <% log( hipaaUrl.toString() ); %>
              <c:import var="hipaaLog" url="${hipaaUrl}"/>

              <% log("Got hipaa log in " + (System.currentTimeMillis() - startMs)); %>
              <bean:define id="hipaaLog" name="hipaaLog"/>
              <c:catch var="e">
                <x:parse var="hipaaLogXml" xml="${hipaaLog}"/>
              </c:catch>
              <c:if test="${e!=null}">
                <% log("Error parsing HIPAA log: " + hipaaLog.toString()); %>
              </c:if>
              <c:if test="${e==null}">        
                <x:forEach select="$hipaaLogXml/logservice/log/entry">
                  <% 
                    count++; 
                    if(count <=2) {
                  %>
                  <c:set var="logEntryTime"><x:out select="substring(@time, 7, 2)"/>-<x:out select="substring(@time, 5, 2)"/>-<x:out select="substring(@time, 1, 4)"/> <x:out select="substring(@time, 9, 2)"/>:<x:out select="substring(@time, 11, 2)"/>:<x:out select="substring(@time, 13, 2)"/></c:set>
                  <tr>
                    <td>
                      <c:set var="trackNum"><x:out select="tracking"/></c:set>
                      <c:forEach items="${ccrs}" var="ccr">
                        <c:if test='${ccr.trackingNumber == trackNum}'><dt:format pattern="MM/dd/yyyy HH:mm:ss">${ccr.createTimeMs}</dt:format></c:if>
                      </c:forEach>
                    </td>
                    <td><mc:tracknum>${trackNum}</mc:tracknum></td>
                    <td><x:out select="s4"/></td>
                    <td>${logEntryTime}</td>
                    <td><x:out select="s1"/></td>
                  </tr>
                  <% } %>
                </x:forEach>
             </c:if>
          </logic:present>
        </c:forTokens>
      </c:otherwise>
    </c:choose>
  </table>

  <span class="hipaaMessage"><%-- medcommonsId=${medCommonsId}/OrigId=${originalPatientMedCommonsId}  --%>
      - Additional detail <%--(2 <x:out select="count($hipaaLogXml/logservice/log/entry)"/> total)--%> 
      is listed in the <a href="javascript:parent.niy();" onfocus='this.blur();' class="hipaaLink" style='text-decoration:underline;'><u>HIPAA Log</u></a></span>
</div>

