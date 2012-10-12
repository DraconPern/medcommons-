

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
        <meta name="layout" content="main" />
        <title>DicomOrder List</title>
    </head>
    <body>
        <div class="nav">
            <span class="menuButton"><a class="home" href="${createLinkTo(dir:'')}">Home</a></span>
            <span class="menuButton"><g:link class="create" action="create">New DicomOrder</g:link></span>
        </div>
        <div class="body">
            <h1>DicomOrder List</h1>
            <g:if test="${flash.message}">
            <div class="message">${flash.message}</div>
            </g:if>
            <div class="list">
                <table>
                    <thead>
                        <tr>
                        
                   	        <g:sortableColumn property="id" title="Id" />
                        
                   	        <g:sortableColumn property="callersOrderReference" title="Callers Order Reference" />
                        
                   	        <g:sortableColumn property="protocolId" title="Protocol Id" />
                        
                   	        <g:sortableColumn property="patientId" title="Patient Id" />
                        
                   	        <g:sortableColumn property="modality" title="Modality" />
                        
                   	        <g:sortableColumn property="ddlStatus" title="Status" />

                   	        <g:sortableColumn property="mcid" title="MedCommons ID" />
                        
                        </tr>
                    </thead>
                    <tbody>
                    <g:each in="${dicomOrderInstanceList}" status="i" var="dicomOrderInstance">
                        <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
                        
                            <td><g:link action="show" id="${dicomOrderInstance.id}">${fieldValue(bean:dicomOrderInstance, field:'id')}</g:link></td>
                        
                            <td><a href='../orderstatus?callers_order_reference=${URLEncoder.encode(dicomOrderInstance.callersOrderReference)}'>${fieldValue(bean:dicomOrderInstance, field:'callersOrderReference')}</a></td>
                        
                            <td>${fieldValue(bean:dicomOrderInstance, field:'protocolId')}</td>
                        
                            <td>${fieldValue(bean:dicomOrderInstance, field:'patientId')}</td>
                        
                            <td>${fieldValue(bean:dicomOrderInstance, field:'modality')}</td>
                        
                            <td>${fieldValue(bean:dicomOrderInstance, field:'ddlStatus')}</td>

                            <td>${fieldValue(bean:dicomOrderInstance, field:'mcid')}</td>
                        
                        </tr>
                    </g:each>
                    </tbody>
                </table>
            </div>
            <div class="paginateButtons">
                <g:paginate total="${dicomOrderInstanceTotal}" />
            </div>
        </div>
    </body>
</html>
