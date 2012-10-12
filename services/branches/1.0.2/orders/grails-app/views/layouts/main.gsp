<html>
    <head>
        <title><g:layoutTitle default="MedCommons TIMC Order Console" /></title>
        <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
        <link rel="shortcut icon" href="${createLinkTo(dir:'images',file:'favicon.gif')}" type="image/x-icon" />
        <% String serverPort = (request.getServerPort()==80||request.getServerPort()==443)?"":":"+request.getServerPort();
        def baseUrl=request.getScheme() + "://" +request.getServerName()+serverPort +request.getContextPath() + "/"; %>
        <base href='${baseUrl}'/>

        <g:layoutHead />
        <g:javascript library="application" />				
    </head>
    <body>
        <div id="spinner" class="spinner" style="display:none;">
            <img src="${createLinkTo(dir:'images',file:'spinner.gif')}" alt="Spinner" />
        </div>	
        <div class="logo"><img src="${createLinkTo(dir:'images',file:'mc_logo.png')}" alt="MedCommons TIMC Management Console" /></div>	
        <div class="topmenu"><a href='${baseUrl}'>Home</a></div>
        <g:layoutBody />		
    </body>	
</html>
