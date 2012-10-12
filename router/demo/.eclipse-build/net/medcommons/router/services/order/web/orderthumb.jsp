<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page isELIgnored="false" %> 

<%--

  MedCommons WADO Viewer JSP

  Outputs HTML for a MedCommons WADO Viewer Order Thumbnail

--%>
<tiles:useAttribute id="index" name="thumbIndex"/>
<div id="thumb<%=index%>" class="thumbCellBox">
  <% int thumbIndex = Integer.parseInt(index.toString()); %>

    <div id='<%="thumbCell"+(thumbIndex)%>' 
         class="NotSelectedImage" 
         style="cursor: hand;" 
         onclick="displaySelectedThumbnail(<%=thumbIndex %>);" >&nbsp;
   </div>
   <div id='<%="thumbLabel"+(thumbIndex)%>' class="ThumbLabel"></div>
   <div id='<%="thumbDescription"+(thumbIndex)%>' class="ThumbDescription"></div>
   <%--<div class="ThumbTime">00:00</div>--%>
</div>

<%-- 
  This div is not displayed but is used as a template by other thumbnails to create
  new CCR thumbnails.  It should be copied /inside/ a thumbCell div for a thumbnail.
--%>
    <div id='ccrThumbCellTemplate' style="display: none;">
        <div class="CCRTitle">
          <span style="position: relative;  left: 5px; top: 2px; text-align: left; width=100%;">
            CCR
            &nbsp;&nbsp;<span style="font-size: 11px; position: relative; top: -1px; ">##MCTRACK##</span>
          </span> 
        </div>
        <div class="CCRThumbLabels">
          <div id="ccrThumbName" style="position: relative; top: 4px; width: 140px;">Name: 
            <div class="CCRThumbContents" style="position: relative; top: -10px; left: 40px; width: 100px;">
              ##NAME##</div>
        </div>
        <div id="ccrThumbSex" style="position: relative; top: 0px;">Age: 
          <span class="CCRThumbContents" style="position: relative; left: 0px;">##SEX##</span>
        </div>
        <div id="ccrThumbDOB" style="position: relative; top: -13px; left: 50px;">DOB: 
          <span class="CCRThumbContents">##DOB##</span>
        </div>
        <div id="ccrThumbCCRDate" style="position: relative; top: -4px;" >CCR Date: 
          <span class="CCRThumbContents" style="position: relative; left: -1px;">##CCRDATE##</span>
        </div>
<%--        <div id="ccrFrom" style="position: relative; top: 4px">From: 
          <span class="CCRThumbContents" style="position: relative; top: 6px; left: 10px;">
            <br/>##CCRFROM##</span>
        </div>
        --%>
      <%--  <div id="ccrThumbTrackNum" style="position: relative; top: 4px">MedCommons Track#: 
          <span class="CCRThumbContents" style="position: relative; top: 8px; left: 60px;">
            <br/>##MCTRACK##</span>
        </div>
        --%>
      </div>
   </div>
