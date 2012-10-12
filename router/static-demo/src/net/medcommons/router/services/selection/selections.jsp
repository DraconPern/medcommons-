<%@ page language="java"%>

<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-nested" prefix="nested" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <title>MedCommons Demo</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <meta content="none" name="robots">
    <link rel="stylesheet" type="text/css" href="css/medcommons.css">
    <link href="main.css" rel="stylesheet" type="text/css">    
    <script type="text/javascript" src="cookies.js"></script>    
    <style type="text/css">
      
      BODY {
      PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; MARGIN: 0px; PADDING-TOP: 0px; BACKGROUND-COLOR: #ffffff
      }
      
      #headerPane
      {
      position: absolute;
      left: 30;
      top:  0.2in;
      height: 70;
      width:  718;
      background-color:  #dff2f7
      } 
      
      #selectionPane
      {
      position: relative;
      left: 30;
      top:  0.2in;
      height: 300;
      width:  718;
      }
      
      #selectionButtons
      {
      position: relative;
      left: 30;
      top:  0.2in;
      height: 200;
      width:  718;
      }
      
      .selHeader {
      FONT-WEIGHT: bold; FONT-SIZE: 15px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; BORDER-BOTTOM: solid black
      }
      .selTitle {
      FONT-WEIGHT: bold; FONT-SIZE: 18px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
      }
      .selRow {
      FONT-WEIGHT: normal; FONT-SIZE: 12px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
      }
      .selBottom {
      FONT-WEIGHT: bold; FONT-SIZE: 15px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; BORDER-TOP: solid black 3px
      }
      .instructionText {
      FONT-WEIGHT: normal; FONT-SIZE: 12px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; BORDER-TOP: solid black 3px
      }=
    </style>
    
    <script language="JavaScript">
      function checkSelected() {
        i=0;
        count=0;
        while( document.getElementById('studies'+i) != null ) {
          if(document.getElementById('studies'+i).checked) {
            count++;
          }
          i++;
        }
        
        if(count == 0) {
          alert('Please select one or more studies to view');
          return false;
        }        
        
        doWadoUrl('about:blank');
        document.studyForm.target='wadowindow';
        document.studyForm.submit();
        return false;
      }

      function uncheckOthers(checkboxToSelect) {
        i=0;        
        while( document.getElementById('studies'+i) != null ) {
          var cbox = document.getElementById('studies'+i);
          if(cbox.id != checkboxToSelect.id) {
            cbox.checked = false;
          }
          i++;
        }        
      }

      var orderButton = new Image(); orderButton.src = 'images/orderbutton.png';
      var orderButton_ro = new Image(); orderButton_ro.src = 'images/orderbutton_ro.png';
    </script>    
  </head>
  <body onload="loggedInAs();">
  <table width="100%">
  <tr><td bgcolor="#dff2f7" width="100%"><%@ include file="header.jsp" %></td></tr>
      </table>
    <html:form name="studyForm" type="net.medcommons.router.services.selection.action.StudyListForm" action="/viewStudies.do">
      <div id="selectionPane">
        <p class="selTitle">Selection Screen - MedCommons Central</p>
        <table width="100%">
          <tr>
            <td class="selHeader">&nbsp;</td>
            <td class="selHeader">Patient</td>
            <td class="selHeader">ID</td>
            <td class="selHeader">Date/Time</td>
            <td class="selHeader">Description</td>
            <td class="selHeader">Status</td>
          </tr>
          
          <logic:iterate id="study" indexId="index" name="studyForm" property="studies">
            <tr>
              <td class="selRow"><html:checkbox name="studyForm" onclick='uncheckOthers( this );' styleId='<%= "studies"+ index %>' property='<%= "studies["+ index +"].selected"%>' /></td>
              <td class="selRow"><bean:write name="study" property="study.patientName"/></td>
              <td class="selRow"><bean:write name="study" property="study.patientID"/></td>
              <td class="selRow"><a href="javascript:doWadoUrl('<bean:write name="study" property="wadoUrl"/>')"><dt:format pattern="MM/dd/yyyy hh:mm a"><bean:write name="study" property="date.time"/></dt:format></a>
               <%-- <bean:write name="study" property="study.studyTime"/> --%> </td>
              <td class="selRow"><bean:write name="study" property="study.studyDescription"/></td>
              <td class="selRow">New</td>
            </tr>
          </logic:iterate>
        </table>
      </div>
      
      <div id="selectionButtons">
        <table width="100%">
          <tr><td class="instructionText" colspan="2">&nbsp;</td></tr>
          <tr><td>
              <p>Select studies and then click the View button to view and/or order these studies. </p></td>
              <td valign="center">
              <a href="" border="0">
              <img alt="View" id="orderB" border="0" 
<%--                onmouseout="document.getElementById('orderB').src='images/orderbutton.png';"
                onmouseover="document.getElementById('orderB').src='images/orderbutton_ro.png';" --%>
                src="images/button_view.gif" onclick="return checkSelected();"/></a></td></tr>
          <!--spacer-->
          <tr><td colspan="2">&nbsp;</td></tr>
          <tr class="selTitle">
            <td align="left"> </td><td>&nbsp;</td></tr>
        </table>
      </div>
    </html:form>
  </body>
</html>
