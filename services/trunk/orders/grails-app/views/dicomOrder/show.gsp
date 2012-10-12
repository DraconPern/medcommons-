

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Show DicomOrder</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">DicomOrder List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New DicomOrder</g:link></span>
        </div>
        <div class="body">
            <h1>Show DicomOrder</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="dialog">
                <table>
                    <tbody>

                    
                        <tr class="prop">
                            <td valign="top" class="name">Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'id')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Callers Order Reference:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'callersOrderReference')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Protocol Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'protocolId')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Patient Id:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'patientId')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Modality:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'modality')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Comments:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'comments')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Destination:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'destination')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">MedCommons ID:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'mcid')}</td>
                            
                        </tr>
                        <tr class="prop">
                            <td valign="top" class="name">Ddl Status:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'ddlStatus')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Status Callback:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'statusCallback')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Baseline:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'baseline')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Date Created:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'dateCreated')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Due Date Time:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'dueDateTime')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Email:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'email')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Last Updated:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'lastUpdated')}</td>
                            
                        </tr>
                    
                        <tr class="prop">
                            <td valign="top" class="name">Scan Date Time:</td>
                            
                            <td valign="top" class="value">${fieldValue(bean:dicomOrderInstance, field:'scanDateTime')}</td>
                            
                        </tr>
                    
                    </tbody>
                </table>
            </div>
            <div class="buttons">
                <g:form>
                    <input type="hidden" name="id" value="${dicomOrderInstance?.id}" />
                    <span class="button"><g:actionSubmit class="edit" value="Edit" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </g:form>
            </div>
        </div>
    </body>
</html>
