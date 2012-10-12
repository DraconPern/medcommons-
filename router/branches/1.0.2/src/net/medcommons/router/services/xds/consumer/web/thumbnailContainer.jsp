<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %> 

<tiles:useAttribute name="idPrefix"/>
<tiles:useAttribute name="index"/>
<tiles:useAttribute name="maxThumbs"/>
<tiles:useAttribute name="enclosures"/>

<div id="${idPrefix}thumbnailContainer-${index}" class="thumbnailContainer">
  <p class="ltrtxt grayback  borderTop" id="${idPrefix}thumbnailLabel-${index}" style="display:none; padding: 6px;"><b>${enclosures}:&nbsp;</b></p>
  <c:forEach begin="0" end="${maxThumbs - 1}" varStatus="status">
      <%-- <div class="thumbnailBox" id="${idPrefix}thumbnailBox${index}-${status.index}" onclick="showWado(${index},${status.index},'${idPrefix}');"> --%>
      <div class="thumbnailBox" id="${idPrefix}thumbnailBox${index}-${status.index}" onclick="onThumbClick(this);">
        <img id="${idPrefix}thumbnailImage${index}-${status.index}" src="images/transparentblank.gif" width="45" height="45"
          onload="imageLoaded();" onerror="imageLoaded();" onabort="imageLoaded();"/>
      </div>
  </c:forEach>
  <%-- A thumbnail saying "more...".  Only used if there are too many thumbnails to fit --%>
  <div class="moreThumbnailBox" id="${idPrefix}moreThumbnailBox${index}" onclick="showWado(${index},4,'${idPrefix}');">
    <img id="${idPrefix}moreThumbnailImage${index}" src="images/morethumbnail.gif"/>
  </div>
</div>
