<html>
    <head>
        <title>Detail for Order ${order.callersOrderReference}</title>
        <meta name="layout" content="timc" />
    </head>
    <body>
        <h1>Detail for Order ${order.callersOrderReference}</h1>
        <table>
          <tr>
            <th>Seq No.</th>
            <td>${order.id}</td>
          </tr>
          <%
            order.displayProperties.each { p ->  %>
          <tr>
              <th><g:message code='dicomOrder.${p}'/></th>
              <td>
                <g:if test='${p=="ddlStatus"}'>
                  ${order.ddlStatus}
                   <g:if test='${order.ddlStatus in ["DDL_ORDER_COMPLETE","DDL_ORDER_ERROR"]}'>
                    &nbsp;&nbsp;&nbsp;<a href='orderstatus/reset?id=${order.id}'>Reset for Download</a>
                    </g:if>
                </g:if>
                <g:else>
                  ${order[p]}
                </g:else>
              </td>
            </tr>
          <% } %>
            <tr>
                <th>HealthURL</th>
                <td><a href='${cfg.appliance}${order.mcid}' target='ccr'>${cfg.appliance}${order.mcid}</a></td>
            </tr>
        </table>
        <h1>Order History</h1>
        <table>
        <tr><th>Status</th><th>Description</th><th>Date / Time</th><th>Ip Address</th><th>User</th></tr>
        <% history.each { h -> %>
          <tr><td>${h.ddlStatus}</td><td>${h.description}</td><td>${h.dateCreated}</td><td>${h.remoteIp} <g:if test='${h.geoIp && h.geoIp != "Reserved"}'> / ${h.geoIp}</g:if></td><td>${h.remoteUser}</td></tr>
        <%}%>
        </table>

    </body>
</html>