
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

<table border="0" cellpadding="0" cellspacing="5" width="100%">
        <tbody><tr> 
            <td rowspan="2" width="256"><a href="http://www.medcommons.net"><img src="contact.php_files/logo_sm.gif" height="50" width="256"></a></td>
          <td align="right" valign="bottom" width="100%"><p><span id='username'></span><span id="logoutlink" style="display:none">|| <a href="loggedout.jsp">logout</a></span></p></td>
        </tr>
        <tr>
          <td align="right" valign="bottom"><p>
              <%=tabWriter.tab("index.jsp","home")%> || <%=tabWriter.tab("register.jsp","register")%> || 
              <%=tabWriter.tab("download.jsp","download")%> || <%=tabWriter.tab("contact.jsp","contact")%> || 
              <%=tabWriter.tab("my_account.jsp","my account")%>
              </p></td>
        </tr>
  </tbody>
</table>
