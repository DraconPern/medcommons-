<html>
    <head>
        <title>Detail for Order ${order.callersOrderReference}</title>
        <meta name="layout" content="timc" />
        <style type='text/css'>
            button#show {
                margin: 1em 0;
            }
        </style>
    </head>
    <body>
        <h1>Details for Order ${order.id}</h1>
        
        <table id='summary'>
            <tr><th>Patient ID</th><td>${order.patientId}</td></tr>
            <tr><th>State</th><td><g:message code="orderState.${order.ddlStatus}.label"/> - <g:message code="orderState.${order.ddlStatus}.desc"/></td></tr>
            <tr><th>Created</th><td><g:formatDate format="MM/dd/yyyy hh:mm:ss" date="${order.dateCreated}"/></td></tr>
            <tr><th>Health Record</th><td><a href='${cfg.appliance}${order.mcid}' target='ccr'>${cfg.appliance}${order.mcid}</a></td></tr>
        </table>
        
        <button id='show'>Show Full Order Details</button>
        
        <table id='details' class='hidden'>
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
            <tr>
                <th>Notifications</th>
                <td><a href='/${ctx}/dicomOrderNotification/list?id=${order.id}'>Notifications</a></td>
            </tr>
        </table>
        <h1>Order History</h1>
        <table>
        <tr><th>Date / Time</th><th>Status</th><th>Description</th><th>Ip Address</th><th>User</th></tr>
        <% history.each { h -> %>
          <tr><td><g:formatDate format="MM/dd/yyyy hh:mm:ss" date="${h.dateCreated}"/></td><td><g:message code="orderState.${h.ddlStatus}.label"/></td><td>${h.description}</td><td>${h.remoteIp} <g:if test='${h.geoIp && h.geoIp != "Reserved"}'> / ${h.geoIp}</g:if></td><td>${h.remoteUser}</td></tr>
        <%}%>
        </table>

    <script type='text/javascript'>
        window.onload =  function() {
        	Y.one('#show').on('click', function() {
        		Y.one('#details').removeClass('hidden');
        	});
        };
    </script>
    </body>
</html>
