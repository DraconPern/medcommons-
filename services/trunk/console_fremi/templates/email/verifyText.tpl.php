{% extends "email/base.text" %}
{% block content %}
To complete your {{ ApplianceName }} registration, please use this link:

    <?php echo $url; ?>

You may need to copy and paste this link into your preferred
web browser.
{% endblock content %}
