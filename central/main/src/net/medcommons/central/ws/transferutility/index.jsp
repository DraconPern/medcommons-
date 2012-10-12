<%@page language="java" %>
<%@taglib uri="/WEB-INF/struts-html.tld" prefix="html" %>

<html>
	<head>
		<title>MedCommons Network - Transfer Utility</title>
		<%@ include file="css.jsp" %>
	</head>
	<body>
		<img src="logo.jpg" alt="MedCommons"/>
		<h2>Transfer Utility</h2>
		
		<p>
		  [ Home ]
		  [ <a href="ActiveTransfers.jsp">Active Transfers</a> ]
		  [ <a href="Performance.jsp">Performance History</a> ]
		</p>
		
		<hr align="left"/>
		
		<html:form action="/transferutility/InvokeTransfer">
			<p><strong>Transfer Folder Tree Between Medical Routers</strong></p>
			<table>
			  <tr>
			    <td class="inputLabel">Folder GUID:</td>
			    <td><html:text property="folderGuid" /></td>
			  </tr>
			  <tr>
			    <td class="inputLabel">Source Router GUID:</td>
			    <td><html:text property="sourceDeviceGuid"/></td>
			  </tr>
			  <tr>
			    <td class="inputLabel">Destination Router GUID:</td>
			    <td><html:text property="destinationDeviceGuid"/></td>
			  </tr>
			  <tr>
			    <td colspan="2"><html:submit value="Initiate Transfer"/></td>
			  </tr>
			</table>			
		</html:form>
		
		<hr align="left"/>
	</body>
</html>