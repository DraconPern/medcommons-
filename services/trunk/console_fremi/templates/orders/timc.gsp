{% extends "www/base.html" %} 
{% block title %}<g:layoutTitle default="MedCommons Orders" />{% endblock %}
{% block head %}
    <link rel="stylesheet" href="${createLinkTo(dir:'css',file:'main.css')}" />
    <% String serverPort = (request.getServerPort()==80||request.getServerPort()==443)?"":":"+request.getServerPort();
    def baseUrl=request.getScheme() + "://" +request.getServerName()+serverPort + "/";
    if(request.ctx != "")
      baseUrl += request.ctx + "/"; %>
    <base href='${baseUrl}'/>
    <g:layoutHead />
    <g:javascript library="application" />				
{% endblock %}
{% block logo %}
<a href="{{ HomePage }}" title="{{ Alt }}">
<script type='text/javascript'>
if(getCookie('mc') && (get_mc_attribute('s')&64))	{
   document.write('<img  border="0" src="{{Site}}/acct/logo.php"  id="logoImg"/>');
}
else
   document.write('<img  border="0" src="{% if not isLogoAbsolute %}{{Site}}/{% endif %}{{ Logo }}"  id="logoImg"/>');
</script>
</a>
{% endblock logo %} 

{% block main %} 
    <g:layoutBody />		
{% endblock main %}
