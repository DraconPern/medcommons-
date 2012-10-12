<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@page import="static net.medcommons.modules.utils.Str.*"%>
<%--

  MedCommons WADO Viewer JSP

  Outputs HTML for a MedCommons WADO Viewer Thumbnail

--%>
  
<tiles:useAttribute name="seriesList" classname="java.util.List"/>
  <tiles:useAttribute name="seriesIndex"/>
  <tiles:useAttribute name="thumbIndex"/>
  <% int index = Integer.parseInt(seriesIndex.toString()); %>
  <div id="thumb<%=thumbIndex %>" class="thumbCellBox">
  <% if(index<seriesList.size()) {
    MCSeries series = (MCSeries)seriesList.get(index);
    int thumbnailReference = series.getInstances().size()/2;
    MCInstance thumbnailImage = (MCInstance) series.getInstance(thumbnailReference);
    if (thumbnailImage == null)
    	throw new NullPointerException("Null MCInstance in series for " 
    		+ thumbnailReference);
    String thumbnailName = "thumbImage" +thumbIndex;
    String thumbDescription = "thumbDescription"+thumbIndex;
    String thumbLabel = "thumbLabel" + thumbIndex;
    String thumbTime = "thumbTime" + thumbIndex;
    StringBuilder buff = new StringBuilder("/router/WADO?studyUID=");
    buff.append(thumbnailImage.StudyInstanceUID)
	    .append("&rows=140&columns=140&fname=")
	    .append(thumbnailImage.ReferencedFileID)
	    .append("&windowCenter=");
    if(blank(thumbnailImage.level)) {
        buff.append(200);
    }
    else
      buff.append(thumbnailImage.level);
      
    buff.append("&windowWidth=");	
    if((thumbnailImage.window == null) || (thumbnailImage.window.equals(""))){
        buff.append(500);
    }
    else
      buff.append(thumbnailImage.window);
    String thumbnailURL = buff.toString();
    %>
     
    <div id='<%="thumbCell"+(index+1)%>' class="NotSelectedImage" style="background-color: black;"
        >
      <IMG height=140 src="blank.png" width=140 border=0 name="<%=thumbnailName%>" id="<%=thumbnailName%>">
   </div>
   <div id="<%=thumbLabel%>" class="ThumbLabel"> images</div>
   <div id="<%=thumbDescription%>" class="ThumbDescription"> series </div>
   <div class="ThumbTime" id="<%=thumbTime%>">&nbsp;</div>
   <div class="thumbAnnotation" onclick='validateSelectedSeries(<%=(index+1)%>);'><img id="thumbAnnotation<%=(index+1)%>" src="images/transparentblank.gif"/></div>
   <% 
   } // index<seriesList.size()
   else { %>
      <script language="JavaScript">
        thumbnails[<%=thumbIndex%>]=new BlankThumbnail();
      </script>
      <TABLE cellSpacing=0 cellPadding=0 width=140 border=0>
        <TBODY>
        <TR>
          <TD colSpan=2>
          <IMG 
            height=140 src="blank.png" 
            width=140 border=0>

            </TD>
         </TR>
        <TR>
          <TD width=90>
            <P class=ThumbLabel></P></TD>
          <TD width=50>
            <P class=ImageCount></P></TD></TR>
        <TR>
          <TD colSpan=2><IMG height=5 
            src="bformspacer.gif" width=1></TD></TR>
        <TR>
          <TD colSpan=2>
            <P class=ThumbLabel> </P>
            </TD>
          </TR>
        </TBODY>
       </TABLE>
  <% } %>
  </div>
