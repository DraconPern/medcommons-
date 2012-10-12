<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
   <head>
      <title>MedCommons WADO Viewer - Error</title>
      <link href="main.css" rel="stylesheet" type="text/css">    
      <style type="text/css">
        #loadingDiv { display=none; }
      </style>
    </head>
    <body onLoad="window.focus();" onBeforeUnload="document.getElementById('errorDiv').style.display='none'; document.getElementById('loadingDiv').style.display='block';">    
       <table width="100%"><tr><td bgcolor="#dff2f7" width="100%">
        <tiles:insert page="header.jsp">
          <tiles:put name="hideLinks" value="true"/>
        </tiles:insert>
        </td></tr>
      </table>
     <div id="errorDiv">
      <h2 class="headline">An error occurred processing your request.</h2>
      <p><html:errors/></p>
     </div>
     <div id="loadingDiv">
      <h2>Loading.... Please Wait</h2>
     </div>
    </body>
</html>


