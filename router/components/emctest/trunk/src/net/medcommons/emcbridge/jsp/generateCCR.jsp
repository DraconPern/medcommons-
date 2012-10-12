<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld" %>
<html>
  <head>
    <title>EMCBridge Test</title>
    
  </head>
  <body>

<h1> Very basic page for testing emcbridge software </h1>
    <stripes:errors/>
    <stripes:form 
        action="net/medcommons/emcbridge/GenerateCCR.action" 
        id="generateCCR">
        Please enter the following information:
	<table>
<tr><td>Docbase: <stripes:text  name="docBase"/> (example: EMCOSA)</td></tr>
<tr><td>userName: <stripes:text  name="userName"/> (example: Administrator)</td></tr>
<tr><td>documentId: <stripes:text  name="documentId"/> (example: 090004d28000198b)</td></tr>
<tr><td>passWord: <stripes:text  name="passWord"/> (example: healthcare)</td></tr>
<stripes:submit name="generateCCR" value="view"/>  
</table>
     
       

    </stripes:form> 
  </body