<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<!--
 Copyright 2007 MedCommons Inc.   All Rights Reserved.
-->
<html>
  <head>
    <link rel="stylesheet" href="base.css"/>
  </head>
  <body>
    <h1>Welcome to MedCommons</h1>
    <p>This gateway has not been configured yet.  In order for this gateway to run it requires a key from
    the MedCommons instance or Appliance providing services for the gateway.
    This key will be provided to you by the Administrator of
    the appliance or hosted services to which you want to connect.</p>
    <p>If you are seeing this message
    on an appliance that you control yourself then you should check in the Console of the appliance
    to find the appropriate key to enter below.</p>

	<#if actionBean?exists >
        <p style='color: red;'>The key you entered did not appear to be valid.  Please try again!</p>
	<#else>
        <p>Please enter the MedCommons Key assigned to this Gateway:</p>
    </#if>
    <form name="keyForm" action="NodeKey.action" method="post">
      <input type="hidden" name="_sourcePage" value="keyConfig.ftl"/>
      <input type="text" name="key" size="50"/>
      <input type="submit" name="saveKey" value="Save Key"/>
    </form>
  </body>
</html>
