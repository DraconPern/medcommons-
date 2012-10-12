<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ include file="header.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 TRANSITIONAL//EN">
<html>
  <head>


    <title>Configure DDL</title>
<script>
  djConfig = {
    isDebug: true,
    debugAtAllCosts: true
  };
</script>
<!-- SECTION 1 -->
<script type="text/javascript" src="dojo.js"></script>




<script  type="text/javascript" src="ddl.js"> /* Load basic DDL routines */ </script>
<script  type="text/javascript" src="format-utils.js"> /* Load dojo-based format routines */ </script>
<script  type="text/javascript" src="configure.js"> /* Load dojo-based format routines */ </script>




<style type="text/css" media="all">
@import "ddl.css";
</style>



</head>

<body>


<h2>Configuration</h2>

<table dojoType="filteringTable" id="dicomConfiguration"
    multiple="true" alternateRows="true"
    cellpadding="0" cellspacing="0" border="0" style="margin-bottom:24px;">
	<form  method="get" id="dicomConfigs" action="">
	  <thead>
    	<tr value=1>
		  <th field="name" dataType="String">Name</th>
		  <th field="value" dataType="html">Value</th>

		</tr>

	  </thead>
	</form>
</table>

<table dojoType="filteringTable" id="ddlConfiguration"
    multiple="true" alternateRows="true"
    cellpadding="0" cellspacing="0" border="0" style="margin-bottom:24px;">

  <thead>
    <tr value=1>
      <th field="name" dataType="String">Name</th>
      <th field="value" dataType="html">Value</th>

    </tr>

  </thead>
</table>


</html>
<%@ include file="footer.jsp" %>


