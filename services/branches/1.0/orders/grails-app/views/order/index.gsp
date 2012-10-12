<html>
    <head>
        <title>MedCommons - New DICOM Order</title>
        <meta name="layout" content="main" />
    </head>
    <body>
        <h1 style="margin-left:20px;">MedCommons - New DICOM Order</h1>
        <table>
          <%
            (order.displayProperties - ['ddlStatus']).each { %>
              <tr><th><g:message code='dicomOrder.${it}'/></th><td><%=order[it].encodeAsHTML()%><td></tr>
          <%} %>
        </table>

    </body>
</html>
