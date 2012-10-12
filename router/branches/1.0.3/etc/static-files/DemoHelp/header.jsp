<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<% 
  class TabWriter {
    private HttpServletRequest request;
    
    public TabWriter(HttpServletRequest request) {
      this.request = request;
    }
    
    public String tab(String page, String name) {
      if(!request.getServletPath().endsWith( page ) ) {
        return "<a href='" + page + "'>" + name + "</a>";
      }
      else {
        return "<span class='currentPage'>" + name + "</span>";
      }
    }
  };
  TabWriter tabWriter = new TabWriter(request);
%>

<%--
 Deprecated - the links may come back into use at a later date but for 
 now showing them will confuse

<tiles:importAttribute name="hideLinks" ignore="true"/>
--%>
<bean:define id="hideLinks" value="true"/>
  <table border="0" cellpadding="0" cellspacing="0" width="100%" bgcolor="#7F9991">
        <tr><td align="left" valign= "top" rowspan=3 height=50 width=246>
          <img src="images/MEDcommons_logo_246x50.gif" height="50" width="246"></td>
        </tr>
        <tr><td align="right" valign="bottom" class = "text">&nbsp;</td></tr>
        <tr>
          <td align="right" valign="bottom"><p>
          <logic:notPresent name="hideLinks">
              <%=tabWriter.tab("index.jsp","home")%> || <%=tabWriter.tab("register.jsp","register")%> || 
              <%=tabWriter.tab("download.jsp","download")%> || <%=tabWriter.tab("contact.jsp","contact")%> || 
              <%=tabWriter.tab("my_account.jsp","my account")%>
          </logic:notPresent>
          </p></td>
        </tr>
   </table>
