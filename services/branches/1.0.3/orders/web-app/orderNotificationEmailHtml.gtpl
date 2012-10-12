<html>
  <body>
    <table border='0'>
        <tr>
          <th>Group:</th><td><%=esc(group.name)%></td>
        </tr>
        <tr>
          <th>Order Reference:</th><td><%=esc(order.callersOrderReference)%></td>
        </tr>
        <tr>
            <th>Status:</th>  
            <td>
              <a href='${config.grails.serverURL}/${ctx}/orderstatus?callers_order_reference=<%=esc(order.callersOrderReference)%>'>
              ${order.ddlStatus}</a>
            </td>
        </tr>
        <tr>
            <th>HealthURL:</th>  
            <td>
                <%if(order.mcid) {%>
                    <a href='${config.serverURL}/$order.mcid'>https://${settings.acDomain}/$order.mcid</a>.
                <%} else {%>
                    No patient HealthURL was ceated for this order
                <%}%>
            </td>
        </tr>
        <tr>
          <th>Comments:</th>
          <td>
            <%=esc(order.comments?:'No Comments')%>
          </td>
        </tr>
        </table>
  </body>
</html>