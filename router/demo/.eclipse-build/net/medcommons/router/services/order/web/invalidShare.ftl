<#include "basicPage.ftl">
<#assign mc=JspTaglibs["/WEB-INF/mc.tld"]>
<#assign s=JspTaglibs["http://stripes.sourceforge.net/stripes.tld"]>
<@basic_layout title="Invalid Link">
  <style type="text/css">
  form input {
    margin: 0px 5px 0px 20px;
  }
  </style>
  <h3 class="headline">Invalid Link</h3>
  <p>The link you used could not be verified or has expired.  Please check you
  have correctly entered it and try again.</p>
</@basic_layout>
