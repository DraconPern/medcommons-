<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ page isELIgnored="false" %> 
<% response.setHeader("Cache-Control","no-cache"); // HTTP 1.1 response.setHeader("Pragma","no-cache"); // HTTP 1.0 %>
<html>
  <body>
    <form name="results">
        <input type="hidden" name="status" value="${deleteStatus}"/>
        <input type="hidden" name="error" value="${deleteError}"/>
    </form>
    <div id="referencesTable">
        <jsp:include page="referenceTable.jsp"/>
    </div>
  </body>
</html>
