<%@ page language="java" contentType="text/xml"%><?xml version="1.0" encoding="UTF-8"?>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ page isELIgnored="false" %> 
<%--
  MedCommons Activity RSS

  This page renders events affecting a user's account as an RSS feed.

  @author Simon Sadedin, MedCommons Inc.
--%>
  <c:set var='accountServer'><mc:config property='AccountServer'/></c:set>
  <c:set var='EnableModRewriteURLs'><mc:config property="EnableModRewriteURLs" default="true"/></c:set>
  <c:set var='now'><%=System.currentTimeMillis()%></c:set>
  <rss version='2.0'>
    <channel>
    <title><c:out value="${actionBean.accountSettings.firstName}"/> <c:out value="${actionBean.accountSettings.lastName}"/></title>
    <link>http://www.medcommons.net</link>
    <description>MedCommons Personal healthURL Feed</description>
    <category>healthURL, health URL</category>
    <generator>Medcommons Gateway</generator>
    <webMaster>cmo@medcommons.net</webMaster>
    <language>en-us</language>
    <copyright>Copyright 2007 MedCommons, Inc</copyright>
    <image>
      <title>MedCommons Home Page</title>
      <url>http://www.medcommons.net/images/mclogo.gif</url>
      <link>http://www.medcommons.net/index.html</link>
    </image>
    <c:forEach items="${events}" var="event" varStatus="s">
      <item>
      <title>
        <c:choose>
          <c:when test='${event.timeStampMs - now > 31536000000}'><dt:format pattern="dd MMM, yyyy ">${event.timeStampMs}</dt:format></c:when>
          <c:otherwise><dt:format pattern="dd MMM, K:mm a">${event.timeStampMs}</dt:format></c:otherwise>
        </c:choose>
          - <c:out value="${event.description}"/>     </title>
        <c:if test='${not empty event.trackingNumber}'>
        <link>
          <c:choose>
            <c:when test='${EnableModRewriteURLs == "true"}'>${accountServer}/../../${event.trackingNumber}</c:when>
            <c:otherwise><mc:config property="CommonsServer"/>/../gwredir.php?tracking=${event.trackingNumber}</c:otherwise>
          </c:choose>
        </link>
        </c:if>
        <description><c:out value="${event.description}"/> at 
        <dt:format pattern="MM/dd/yyyy K:mm a">${event.timeStampMs}</dt:format> by user 
        <c:choose>
          <c:when test='${event.sourceAccountId=="000000000000000"}'>Anonymous User</c:when>
          <c:otherwise>
            <mc:translate account='${event.sourceAccountId}'/>
          </c:otherwise>
        </c:choose>
        </description>
        <author><mc:medcommonsId>${event.sourceAccountId}</mc:medcommonsId></author>
        <pubDate><dt:format pattern="E, dd MMM yyyy K:mm Z">${event.timeStampMs}</dt:format></pubDate>
      </item>
    </c:forEach>
  </channel>
</rss>
