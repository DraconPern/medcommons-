<%@ page language="java"%>
<!-- Copyright 2010 MedCommons Inc.   All Rights Reserved. -->
<%@ include file="taglibs.inc.jsp" %> 
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.modules.utils.*" %>
<%--
  MedCommons WADO Viewer JSP  -  Browser Edition

  This is the general version of the viewer, targeted at modern browsers, 
  including mobile.
--%>
<s:layout-render name="/wado.jsp">
    <s:layout-component name="css">
        <src>yui-2.8.2r1/slider/assets/skins/sam/slider.css</src>  
        <src>browserviewer.css</src>  
    </s:layout-component>
    <s:layout-component name="scripts">
       <c:if test='${framed}'>
           <src>viewerframed.js</src>  
       </c:if>
       <src>browserviewer.js</src>  
       <src>yui-2.8.2r1/dragdrop/dragdrop.js</src>
       <src>yui-2.8.2r1/slider/slider.js</src> 
    </s:layout-component>

    <s:layout-component name="postBodyHTML">
      <c:if test='${empty ccr}'><c:set var="ccr" value="${actionBean.ccr}" scope="request"/></c:if>
      <div id="pager">
        <div id='pagerLinks' style='font-size: 12px; color: #cccccc; font-weight: bold;'>
        <div style='font-size: 11px; color: #cccccc; font-weight: bold;'>Reference:&nbsp;&nbsp;</div>
          <table border='0' cellpadding='0' cellspacing='0' id='pagerTable'><tr>
          <% int thumbnailIndex=0; %>
          <c:forEach items="${ccr.seriesList}" var="series" varStatus="s">
            <c:if test="${s.index > 0}">
              <td id='pagerCell${s.index}'>
              <a title='Series ${s.index} - <c:out value='${series.seriesDescription}'/> - ${fn:length(series.instances)} images'
                 id='pagerLink${s.index}' 
                 class='pagerLink' 
                 href='javascript:displaySelectedSeries(${s.index});' 
                 onfocus='this.blur();'>${s.index}</a>
              <% thumbnailIndex++; %>
              </td>
              <c:if test='${s.index%9 == 0}'></tr><tr></c:if>
            </c:if>
            </c:forEach>
          </tr></table>
        </div>      
      </div>
    </s:layout-component>
</s:layout-render>

