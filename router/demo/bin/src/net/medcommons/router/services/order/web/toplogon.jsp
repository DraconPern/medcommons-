<%@ include file="/taglibs.inc.jsp" %>
<html>
  <body onload='document.forms[0].submit();'>
    <form name='auto' action='tracking.jsp' target='_top' method='post'>
      <c:if test='${not empty expired}'>
        <input type='hidden' name='expired' value='true'/>
      </c:if>
    </form>
  </body>
</html>
