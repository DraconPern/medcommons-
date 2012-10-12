

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>Edit DicomOrder</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="list" action="list">DicomOrder List</g:link></span>
            <span class="menuButton"><g:link class="create" action="create">New DicomOrder</g:link></span>
        </div>
        <div class="body">
            <h1>Edit DicomOrder</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <g:hasErrors bean="${dicomOrderInstance}">
            <div class="errors">
                <g:renderErrors bean="${dicomOrderInstance}" as="list" />
            </div>
            </g:hasErrors>
            <g:form method="post" >
                <input type="hidden" name="id" value="${dicomOrderInstance?.id}" />
                <input type="hidden" name="version" value="${dicomOrderInstance?.version}" />
                <div class="dialog">
                    <table>
                        <tbody>
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="callersOrderReference">Callers Order Reference:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'callersOrderReference','errors')}">
                                    <input type="text" id="callersOrderReference" name="callersOrderReference" value="${fieldValue(bean:dicomOrderInstance,field:'callersOrderReference')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="protocolId">Protocol Id:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'protocolId','errors')}">
                                    <input type="text" id="protocolId" name="protocolId" value="${fieldValue(bean:dicomOrderInstance,field:'protocolId')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="patientId">Patient Id:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'patientId','errors')}">
                                    <input type="text" id="patientId" name="patientId" value="${fieldValue(bean:dicomOrderInstance,field:'patientId')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="modality">Modality:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'modality','errors')}">
                                    <input type="text" id="modality" name="modality" value="${fieldValue(bean:dicomOrderInstance,field:'modality')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="comments">Comments:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'comments','errors')}">
                                    <input type="text" id="comments" name="comments" value="${fieldValue(bean:dicomOrderInstance,field:'comments')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="destination">Destination:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'destination','errors')}">
                                    <input type="text" id="destination" name="destination" value="${fieldValue(bean:dicomOrderInstance,field:'destination')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="ddlStatus">Ddl Status:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'ddlStatus','errors')}">
                                    <input type="text" id="ddlStatus" name="ddlStatus" value="${fieldValue(bean:dicomOrderInstance,field:'ddlStatus')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="statusCallback">Status Callback:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'statusCallback','errors')}">
                                    <input type="text" id="statusCallback" name="statusCallback" value="${fieldValue(bean:dicomOrderInstance,field:'statusCallback')}"/>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="baseline">Baseline:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'baseline','errors')}">
                                    <g:checkBox name="baseline" value="${dicomOrderInstance?.baseline}" ></g:checkBox>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dateCreated">Date Created:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'dateCreated','errors')}">
                                    <g:datePicker name="dateCreated" value="${dicomOrderInstance?.dateCreated}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="dueDateTime">Due Date Time:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'dueDateTime','errors')}">
                                    <g:datePicker name="dueDateTime" value="${dicomOrderInstance?.dueDateTime}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="email">Email:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'email','errors')}">
                                    <g:checkBox name="email" value="${dicomOrderInstance?.email}" ></g:checkBox>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="lastUpdated">Last Updated:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'lastUpdated','errors')}">
                                    <g:datePicker name="lastUpdated" value="${dicomOrderInstance?.lastUpdated}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                            <tr class="prop">
                                <td valign="top" class="name">
                                    <label for="scanDateTime">Scan Date Time:</label>
                                </td>
                                <td valign="top" class="value ${hasErrors(bean:dicomOrderInstance,field:'scanDateTime','errors')}">
                                    <g:datePicker name="scanDateTime" value="${dicomOrderInstance?.scanDateTime}" ></g:datePicker>
                                </td>
                            </tr> 
                        
                        </tbody>
                    </table>
                </div>
                <div class="buttons">
                    <span class="button"><g:actionSubmit class="save" value="Update" /></span>
                    <span class="button"><g:actionSubmit class="delete" onclick="return confirm('Are you sure?');" value="Delete" /></span>
                </div>
            </g:form>
        </div>
    </body>
</html>
