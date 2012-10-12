<html>
    <head>
        <title>New Order</title>
        <meta name="layout" content="timc" />
        <style type='text/css'>
          body {
            margin: 10px;
          }
          p {
            margin: 0.5em 0em;
          }
          pre.error {
            border: 1px solid #444;
            padding: 20px;
            margin: 20px 0px;
          }
        </style>
    </head>
    <body>
      <h1>Error</h1>
      <p>A problem occurred in processing the submitted order.</p>

      <div class="errors">
        <ul>
        <%if(!order.hasErrors()) {%>
        <li class='error'><%=message.encodeAsHTML()%></li>
        <%}%>

        <g:hasErrors bean="${order}">
            <%order.errors.allErrors.each { f -> %>
              <li>The value '${Camel.fromCamel(f.field).replaceAll("_"," ")}' was incorrect or not supplied.</li>
            <%}%>
        </g:hasErrors>
        </ul>
      </div>

    </body>
</html>
