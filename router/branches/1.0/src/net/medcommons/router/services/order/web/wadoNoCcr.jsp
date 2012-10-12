<%@ page language="java"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<%@ include file="/taglibs.inc.jsp" %>
<!--
 Copyright 2004 MedCommons Inc.   All Rights Reserved.
-->
   <head>
      <title>MedCommons WADO Viewer - Error</title>
      <mc:base/>
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
    </head>
    <body style="margin-top: 10px;">    
     <div id="errorDiv">
      <p class="headline">No Selected CCR</p>
      <p>No CCR is currently selected to view.  You may have discarded or deleted the current CCR.</p>
      <p>Please choose a CCR from your desktop.</p>
     </div>
    </body>
</html>


