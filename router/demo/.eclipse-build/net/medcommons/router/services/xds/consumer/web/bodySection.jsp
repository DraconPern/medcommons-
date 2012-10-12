<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page isELIgnored="false" %> 
<tiles:useAttribute name="section"/>
<tiles:useAttribute name="index"/>
<tiles:useAttribute name="idPrefix"/>
<c:set var='bodySectionElement'>${fn:replace(section,' ','')}</c:set>
<c:set var='bodyCount'><mc:xvalue bean="ccrDom" path="bodySectionCount"/></c:set>
<c:set var='bodyClass'>
    <c:choose>
      <c:when test="${bodyCount == '0.0' }">notPresentBodySection</c:when>
      <c:otherwise>presentBodySection</c:otherwise>
    </c:choose>
</c:set>
<c:set var='bodyContent'>
    <c:choose>
      <c:when test="${bodyCount != '0.0' }">
        <a href="javascript:showBodySection(${index},'${bodySectionElement}');"><tiles:get name="section"/></a>
      </c:when>
      <c:otherwise>${section}</c:otherwise>
    </c:choose>
</c:set>
<span class="bodySectionBox"><img src="images/bodysectiontop.gif"/
  ><div class="bodySectionMiddle" style="">
  <div style="width: 80px; text-align: center" id="${idPrefix}bodySectionMiddle-${bodySectionElement}-${index}" class="${bodyClass}">${bodyContent}</div>
  </div><img src="images/bodysectionbottom.gif"/></span>

