DICOM Order Complete
------------------------

The following DICOM Order has completed processing  at 

https://${settings.acDomain}

<%if(group) {%>
Group:        $group.name
<%}%>
HealthURL:    https://${settings.acDomain}/${order.mcid}

Order Ref#:   $order.callersOrderReference

Status:       ${order.ddlStatus}

Comments:     <%=order.comments?:'No Comments'%>

For more information, see full details at:

  ${config.grails.serverURL}/${ctx}/orderstatus?callers_order_reference=<%=esc(order.callersOrderReference)%>

