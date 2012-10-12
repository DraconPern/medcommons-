<%@ page contentType="application/x-java-jnlp-file" %> 
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" 
  prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" 
  prefix="fn" %>
<?xml version="1.0" encoding="utf-8"?>
<!-- JNLP File for DICOM Data Liberator (DDL) -->
<%-- 


    	
http://localhost:8090/DDL/app/ddl.jsp?DICOMRemoteAETitle=Remote&DICOMRemoteDicomPort=3001&DICOMRemoteHost=radworkstation.hospital.edu&DICOMLocalAETitle=MCDICOM&DICOMLocalDicomPort=3002
http://stego.myhealthespace.com/DDL/app/ddl.jsp?DICOMRemoteAETitle=TimbuckToo&DICOMRemoteDicomPort=3333&DICOMRemoteHost=radworkstation.hospital.edu&DICOMLocalAETitle=DDLDICOM&DICOMLocalDicomPort=3444

--%>
<jnlp
  spec="1.5+"
	codebase="<%=request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + "/DDL/app" %>"
	href="<%= "ddl.jsp?DICOMRemoteAETitle=" +request.getParameter("DICOMRemoteAETitle") +
		"&DICOMRemoteDicomPort=" + request.getParameter("DICOMRemoteDicomPort") +
		"&DICOMRemoteHost=" + request.getParameter("DICOMRemoteHost") +
		"&DICOMLocalAETitle=" + request.getParameter("DICOMLocalAETitle") +
		"&DICOMLocalDicomPort=" + request.getParameter("DICOMLocalDicomPort") 
		  %>">
  <information>
    <title>DICOM Data Liberator</title>
    <vendor>MedCommons, Inc.</vendor>

    <homepage href="http://medcommons.net" />
    <description>DICOM CSTORE SCP/SCU to CXP-enabled appliances. </description>
    <description kind="short">DICOM Data Liberator </description>
    <icon  kind="splash" href="../Logo_300x300.gif"/>
    <icon  kind="shortcut" href="../Logo_300x300.gif"/>
    <icon  kind="default" href="../Logo_300x300.gif"/>
    <association mime-type="application/x-healthurl-download" extensions="download-hurl"/>
    <offline-allowed />

    <update check="always" policy="prompt-update"/>

    <shortcut online="true">
         <menu submenu="DICOM Data Liberator"/>
         <desktop/> 
    </shortcut>

  </information>
  <security>
    <all-permissions/>
  </security>
  <resources>

 <j2se version="1.5+" />

	<extension name="Activation" href="activation.jnlp"/> <!-- Hack for getting around jar signed by another entity -->
	<jar href="medcommons-dicomclient-application.jar" main="true"/>
    <jar href="log4j.jar" download="eager"/>
    <jar href="hibernate3.jar" download="eager" />

    <jar href="antlr.jar" download="eager"/>
    <jar href="asm-attrs.jar" download="eager"/>
    <jar href="asm.jar" download="eager"/>
    <jar href="ccrxmlbean.jar" download="eager"/>
    <jar href="cglib.jar" download="eager"/>
    <jar href="commons-codec.jar" download="eager"/>
    <jar href="commons-collections.jar" download="eager"/>
    <jar href="commons-httpclient.jar" download="eager"/>
    <jar href="commons-logging-api.jar" download="eager"/>
    <jar href="commons-logging.jar" download="eager"/>
    <jar href="cos.jar" download="eager"/>
    <jar href="dcm4che-core.jar" download="eager"/>
    <jar href="dcm4che-net.jar" download="eager"/>
    <jar href="dom4j.jar" download="eager"/>
    <jar href="ehcache.jar" download="eager"/>
    <jar href="hsqldb.jar" download="eager"/>
    <jar href="jdom.jar" download="eager"/>
    <jar href="jetty-util.jar" download="eager"/>
    <jar href="jetty.jar" download="eager"/>
    <jar href="jnlp.jar" download="eager"/>
    <jar href="jsp.jar" download="eager"/>
    <jar href="jsp-api.jar" download="eager"/>
    <jar href="jsr173_api.jar" download="eager"/>
 	<jar href="jta.jar" download="eager"/>
    <jar href="mail.jar" download="eager"/>
    <jar href="gateway-interfaces.jar" download="eager"/>
    <jar href="medcommons-crypto.jar" download="eager"/>
    <jar href="medcommons-cxp-client.jar" download="eager"/>
    <jar href="medcommons-transfer-application.jar" download="eager"/>
    <jar href="medcommons-utils.jar" download="eager"/>
    <jar href="saxon-dom.jar" download="eager"/>
    <jar href="saxon.jar" download="eager"/>
    <jar href="servlet-api.jar" download="eager"/>
    <jar href="slf4j-api.jar" download="eager" />
    <jar href="slf4j-log4j12.jar" download="eager"/>
    <jar href="stripes.jar" download="eager"/>
    <jar href="wsdl4j.jar" download="eager"/>
    <jar href="wstx-asl.jar" download="eager"/>
    <jar href="xbean.jar" download="eager"/>
    <jar href="xbean_xpath.jar" download="eager"/>
    <jar href="xercesImpl.jar" download="eager"/>
    <jar href="xfire-all.jar" download="eager"/>

 	<property name="defaultDICOMRemoteAETitle"
    	value="<%= request.getParameter("DICOMRemoteAETitle") %>"/>
    <property name="defaultDICOMRemoteDicomPort"
    	value="<%= request.getParameter("DICOMRemoteDicomPort") %>"/>
    <property name="defaultDICOMRemoteHost"
    	value="<%= request.getParameter("DICOMRemoteHost") %>"/>
    <property name="defaultDICOMLocalAETitle"
    	value="<%= request.getParameter("DICOMLocalAETitle") %>"/>
    <property name="defaultDICOMLocalDicomPort"
    	value="<%= request.getParameter("DICOMLocalDicomPort") %>"/>

	<property
        name="ddl.configuration"
        value="<%= request.getScheme() + "://" +request.getServerName() + "/DDL/app/DDL.properties"  %>"/>
	<property name="gatewayRoot"
	    value="<%= request.getScheme() + "://" +request.getServerName() %>" />


   

  </resources>

  <application-desc main-class="net.medcommons.application.dicomclient.DICOMClient" />


</jnlp>
