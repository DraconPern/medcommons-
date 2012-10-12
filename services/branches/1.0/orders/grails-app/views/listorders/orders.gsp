<%@ page defaultCodec="html" %> 
<html>
    <head>
        <title>All Active Orders</title>
        <meta name="layout" content="timc" />
    </head>
    <body>
        <h1 style="margin-left:20px;">All Active Orders</h1>
        <table>
          <tr>
            <th>Seq No.</th>
          <% def props = DicomOrder.displayProperties.grep{it!='destination'} %>
          <% props.each { %>
              <th><g:message code='dicomOrder.${it}'/></th>
          <%}%>
          </tr>

          <g:each var='o' in='${orders}'>
          <tr>
            <td>${o.id}</td>
          
            <% props.each { %>
              <td><%if(it=~/Date/) {%>
                  <g:formatDate format="yyyy-MM-dd HH:mm" date="${o[it]}"/>
                <%} else if(it=='dateCreated') { %>
                  <g:formatDate format="yyyy-MM-dd HH:mm" date="${o[it]}"/>
                <%} else if(it == 'callersOrderReference') { %>
                  <a href='orderstatus?callers_order_reference=${o.callersOrderReference.encodeAsURL()}'>${o.callersOrderReference}</a>
                <%} else { %>
                  <%if(it=='ddlStatus') {%>
                    <%=o[it].toString().replaceAll("DDL_ORDER_","")%>
                  <%} else { %>
                    <%=o[it]!=null?o[it].encodeAsHTML():''%>
                  <%}%>
                <%}%>
                </td>
            <% } %>
          </tr>
          </g:each>
        </table>
    </body>
</html>

