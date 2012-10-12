<#include "basicPage.ftl">
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign s=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<@basic_layout title="Document Unavailable">
  <h3 class="headline">Document Unavailable</h3>
  <p>The document you are attempting to access is not available</p>
  <p>Possible reasons include:</p>
  <ul>
      <li>Consent for you to access the document has been revoked</li>
      <li>The account owning the document has expired or been deleted by its owner</li>
      <li>You used an incorrect link, Tracking Number or PIN</li> 
  </ul>
</@basic_layout>
