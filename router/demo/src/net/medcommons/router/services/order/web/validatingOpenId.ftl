<#include "basicPage.ftl">
<@basic_layout title="Verifying your OpenID ...">
  <p>Please wait while your OpenID is verified ...</p>
  <script type="text/javascript">
    window.onload=function() {
      window.focus();
      window.opener.document.openIdForm.submit();
    }
  </script>
</@basic_layout>
