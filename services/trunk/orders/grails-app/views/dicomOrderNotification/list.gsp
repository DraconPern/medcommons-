

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="timc" />
        <g:set var="entityName" value="${message(code: 'dicomOrderNotification.label', default: 'DicomOrderNotification')}" />
        <title><g:message code="default.list.label" args="[entityName]" /> for Order ${dicomOrder.id}</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/listorders')}">Orders List</a></span>
        </div>
        <div class="body">
            <h1><g:message code="default.list.label" args="[entityName]" /> for Order ${dicomOrder.id}</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                            <g:sortableColumn property="id" title="${message(code: 'dicomOrderNotification.id.label', default: 'Id')}" />
                            
                            <g:sortableColumn property="recipient" title="${message(code: 'dicomOrderNotification.recipient.label', default: 'Recipient(s)')}" />
                        
                            <g:sortableColumn property="subject" title="${message(code: 'dicomOrderNotification.subject.label', default: 'Subject')}" />
                        
                            <g:sortableColumn property="status" title="${message(code: 'dicomOrderNotification.status.label', default: 'Status')}" />
                        
                            <g:sortableColumn property="sentDateTime" title="${message(code: 'dicomOrderNotification.sentDateTime.label', default: 'Sent Date Time')}" />
                        
                            <g:sortableColumn property="error" title="${message(code: 'dicomOrderNotification.error.label', default: 'Error')}" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${dicomOrderNotificationInstanceList}" status="i" var="dicomOrderNotificationInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${dicomOrderNotificationInstance.id}">${fieldValue(bean: dicomOrderNotificationInstance, field: "id")}</g:link></td>
                            
                            <td>
                                <%dicomOrderNotificationInstance.recipient.split(",").each { %>
                                    ${it}
                                    <br/>
                                <% } %>
                            </td>
                        
                            <td>${fieldValue(bean: dicomOrderNotificationInstance, field: "subject")}</td>
                        
                            <td>${fieldValue(bean: dicomOrderNotificationInstance, field: "status")}</td>
                        
                            <td><g:formatDate date="${dicomOrderNotificationInstance.sentDateTime}" /></td>
                        
                            <td>${fieldValue(bean: dicomOrderNotificationInstance, field: "error")}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
