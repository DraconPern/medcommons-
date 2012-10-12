<#include "basicPage.ftl">
<@basic_layout title="Verification Successful">
    <p>Verifying your OpenID Succeeded.</p>
    <script type="text/javascript">
      window.opener.location='access?load&g='+'${actionBean.guid}';
      window.close();
    </script>
</@basic_layout>
