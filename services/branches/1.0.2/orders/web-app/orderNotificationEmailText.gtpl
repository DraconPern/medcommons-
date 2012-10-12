
HealthURL:  https://${settings.acDomain}/${order.mcid}

Order Reference:  $order.callersOrderReference

Scan Date:        $order.scanDateTime

Comments:        <%=order.comments?:''%>
