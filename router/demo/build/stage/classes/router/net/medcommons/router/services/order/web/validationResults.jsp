<%@ page language="java"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ page isELIgnored="false" %> 
<html>
  <head>
    <style type="text/css">
      * {
        font-family: arial;
      }
    </style>
  <body>
    <c:choose>
      <c:when test='${empty validationErrors}'>
        <h2>No validation errors</h2>
      </c:when>
      <c:otherwise>
        <h2>Validation Errors Found:</h2>
        <table width="80%" border="1">
        <tr>
          <td>
          <pre>
          ${validationErrors}
          </pre>
          </td>
          </tr>
        </table>
        <iframe height="70%" width="100%" src="downloadCcr.do?ccrIndex=${ccrIndex}&inline=true"/>
      </c:otherwise>
    </c:choose>
  </body>
</html>
