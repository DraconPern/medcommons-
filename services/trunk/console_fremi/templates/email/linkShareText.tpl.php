{% extends "email/hipaa.text" %}
{% block content %}
A MedCommons member has shared a HealthURL with you.

Sender comments:

<?=$comments?>

You can access the shared content via the following link:

 {{ link }}

{% endblock content %}
