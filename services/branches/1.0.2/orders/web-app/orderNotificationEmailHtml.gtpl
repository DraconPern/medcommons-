<html>
  <body>
    <table border='0'>
    <tr>
        <th>HealthURL:</th>
        <td><a href='https://${settings.acDomain}/$order.mcid'>https://${settings.acDomain}/$order.mcid</a>.</td>
    </tr>
    <tr>
        <th>Order Reference:</th> <td> <%=esc(order.callersOrderReference)%></td>
    </tr>
    <tr>
        <th>Scan Date:</th><td>       <%=esc(order.scanDateTime.toString())%></td>
    </tr>
    <tr>
        <th>Comments:</th><td>        <%=esc(order.comments?:'')%></td>
    </tr>
    </table>
  </body>
</html>