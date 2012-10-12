<html>
  <body>
    <b>DICOM Order Delayed</b>
    <hr/>
    <p>The DICOM Order has been received at <a href='https://${settings.acDomain}'>https://${settings.acDomain}</a>.
       but did not complete successfully within the configured timeout period.</p>
    <br/>
    <b>Error:</b>  <%=esc(order.errorCode)%>
    <br/>
    <b>HealthURL:</b>  <a href='https://${settings.acDomain}/$order.mcid'>https://${settings.acDomain}/$order.mcid</a>.</p>
    <br/>
    <b>Order Reference:</b>  <%=esc(order.callersOrderReference)%>
    <br/>
    <b>Comments:</b>        <%=esc(order.comments)%>
    <br/>
  </body>
</html>