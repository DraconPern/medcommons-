<#macro basic_layout title head='' onload=''>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2007 MedCommons Inc.   All Rights Reserved.
-->
<html>
  <head>
    <link rel="stylesheet" href="${baseUrl}base.css"/>
    <title>${title}</title>
    ${head}
    <script type='text/javascript'>
      if(parent.hidePatientHeader)
        parent.hidePatientHeader();
    </script>
  </head>
  <body class='yui-skin-sam' onload='${onload}'>
    <#nested>
  </body>
</html>
</#macro>
