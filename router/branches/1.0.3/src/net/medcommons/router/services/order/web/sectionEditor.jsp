<%@ page language="java"%>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page isELIgnored="false" %> 
<%--
  CCR Section Editor

  Renders a div containing a dhtml table in which a CCR section can be edited.
  
  See Javascript in ccreditor.js

  @param - initialize : javascript to load the initial rows into the table using addEditorRow
  @param - sectionTitle : title to display in editor heading
  @param - sectionPrefix : prefix to use when creating ids and request parameters
  @param - sectionName : section name as per XML
  @author Simon Sadedin, MedCommons Inc.
--%>
<stripes:layout-definition>
  <script type="text/javascript">
    function ${sectionPrefix}Init() {
      <stripes:layout-component name="initialize"/>

      <%-- search for any comments that are attached to sections of this type --%>
      <mc:xnode bean='notificationForm' path='/x:ContinuityOfCareRecord/x:Body/x:${sectionName}/*' forceList="true" name='sectionEntries'/>
      <c:forEach items="${sectionEntries}" var="entry" varStatus="status">
        <c:set var="commentId"><mc:xvalue bean="entry" path="x:CommentID"/></c:set>
        var commentTd = $('${sectionPrefix}[${status.index}].comment');
        commentTd.emptyField=false;
        replaceChildNodes(commentTd,'<mc:xvalue bean="notificationForm" path="commentFromId" escape='false' params="commentId"/>');
      </c:forEach>
      
      if(!window.editorTitleRounded) {
        roundClass('DIV','editorTitle',{corners:'top'});
        window.editorTitleRounded=true;
      }
      $('${sectionPrefix}Table').initialized = true;
      $('${sectionPrefix}Editor').dirty=false;
    }
  </script>
  <div id="${sectionPrefix}Editor" class="editorOuter">
    <div class="editorTitle"><div style="padding: 0px 2px;">${sectionTitle}</div></div>
    <div id="${sectionPrefix}EditorInner" class="ccrEditForm">
        <div id="${sectionPrefix}Box" class="editorBody">
          <p style="margin:0px;padding:5px 2px 10px 2px;">Select an entry to edit or click the "plus" sign to create a new one:</p>
          <div style="border-style: none; border-width: 1px;" >
            <table id="${sectionPrefix}Table" cellpadding="0" cellspacing="2" class="editorTable" border="0" onmouseover="initTable(this);">
              <thead>
              <tr>
                <stripes:layout-component name="tableHead"/>
                <th id='comment'>Comment</th>
                <th title="Add Row"><a href="javascript:addEditorRow();" title="Add Row"><img src="images/plus.gif"  border="0"/></a></th>
              </tr>
              </thead>
              <tbody>
              </tbody>
              </table>
            </div> <%-- border div --%>
          <hr/>
          <input type="button" id="save${sectionName}" value="Save" onclick="editorSave('${sectionName}');"/>&nbsp;
          <input type="button" value="Cancel" onclick="hideEditor();"/>
          </div> <%-- box --%>
    </div> <%-- editorInner --%>
  </div>
</stripes:layout-definition>
