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
      <link href="main.css" rel="stylesheet" type="text/css"/>    
      <style type="text/css">
        #loadingDiv { 
          display: none; 
        }
        body {
          margin: 5px;
          margin-left: 30px;
          margin-right: 60px;
          background-color: white;
        }
      </style>
      <script language="JavaScript">
        function showViewerHelp() {
          open('help/medhelp/index.htm', 'mchelp','scrollbars=1,width=570,height=635'); 
        }
        window.parent.setTools( [ 
            ["Help","showViewerHelp();"],
            ["Print","window.print();"]
            ]);
      </script>
    </head>
    <body style="margin-top: 10px;">    
     <div id="errorDiv">
      <p class="headline">No Selected CCR</p>
      <p>No CCR is currently selected to view.  You may have discarded or deleted the current CCR.</p>
      <p>Please choose a CCR from your desktop.</p>
     </div>
    </body>
</html>


