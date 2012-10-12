

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
        <meta name="layout" content="main" />
        <g:set var="entityName" value="${message(code: 'dicomOrderNotification.label', default: 'DicomOrderNotification')}" />
        <title><g:message code="default.show.label" args="[entityName]" /></title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLink(uri: '/listorders')}">Home</a></span>
        </div>
        <div class="body">
            <h1><g:message code="default.show.label" args="[entityName]" /></h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.id.label" default="Id" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: dicomOrderNotificationInstance, field: "id")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.error.label" default="Error" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: dicomOrderNotificationInstance, field: "error")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.order.label" default="Order" /></td>
                            
                            <td valign="top" class="value"><g:link controller="dicomOrder" action="show" id="${dicomOrderNotificationInstance?.order?.id}">${dicomOrderNotificationInstance?.order?.encodeAsHTML()}</g:link></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.status.label" default="Status" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: dicomOrderNotificationInstance, field: "status")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.subject.label" default="Subject" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: dicomOrderNotificationInstance, field: "subject")}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.sentDateTime.label" default="Sent Date Time" /></td>
                            
                            <td valign="top" class="value"><g:formatDate date="${dicomOrderNotificationInstance?.sentDateTime}" /></td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name"><g:message code="dicomOrderNotification.recipient.label" default="Recipient" /></td>
                            
                            <td valign="top" class="value">${fieldValue(bean: dicomOrderNotificationInstance, field: "recipient")}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
        </div>
    </body>
</html>
