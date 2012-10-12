<%@ page language="java"%>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ page isELIgnored="false" %> 
<html>
  <head>
    <title>Attributions</title>
    <link href="main.css" rel="stylesheet" type="text/css"/>
    <script language="JavaScript" src="utils.js"></script>
    <style type="text/css">
      body {
        margin: 20px;
      }

      #attribution {
        position: relative;
        border-style: solid;
        border-width: 2px;
        border-color: #527463;
        padding: 20px;
        padding-bottom: 40px;
      }
    </style>
    <script language="javascript">
      function forward() {
        document.location.href='/router/${forward.path}';
      }
    </script>
  </head>
  <body>
    <div id="attribution">
      <c:forEach items='${ccr.attributions}' var="entry">
        <p>${entry.value}</p>
      </c:forEach>
      <button style="position: absolute; right: 80px; width: 95; margin-top: 10px;" onclick="forward();">Continue &gt;&gt;</button>
    </div>
  </body>  
  <!--
  ${ccr.clearAttributions} 
  -->
</html>
