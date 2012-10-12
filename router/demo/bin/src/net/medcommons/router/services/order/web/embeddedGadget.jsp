<%@ include file="/taglibs.inc.jsp" %>
<s:layout-definition>
  <%--
    Minimal Layout that displays an Embedded Gadget

    Designed to be embedded in another page that has already declared 
    head, body, base javascript and css files.

    @param - title of gadget

    @author Simon Sadedin, MedCommons Inc.
  --%>
  <s:layout-component name="head"/>
  <c:if test='${!hideTitle}'>
    <div style='float: left; width: ${fn:length(title)*0.8}em; margin: 0px; padding: 0px;' class='gadgetTitleBox gadgetBorderColor'><h3 class="gadgetTitle" ><c:out value="${title}"/></h3></div>
    <img style='float:left;' src='images/heading_r_br.png'/>
    <div class='gadgetSeparator' style="">&nbsp;</div>
    <br style='clear:both;'/>
  </c:if>
  <div class="panePadding"><s:layout-component name="body"/></div>
  <div style='height: 5px;'>&nbsp;</div>
</s:layout-definition>
