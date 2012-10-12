{% extends "email/base.html" %}
{% block title %}{{ ApplianceName }} - Verifying Your Email{% endblock %}
{% block content %}
<p>
To complete your {{ ApplianceName }} registration, please use this link:
</p>
<p> <a href='<?php echo $url; ?>'><?=htmlentities($url)?></a></p>
{% endblock content %}
