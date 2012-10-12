{% extends "www/base.html" %}

{% block title %}{{ CommonName }} <?if(isset($title)):?> - <?=$title?><?endif;?>{% endblock title %}

{% block head %}
<?$site = "{{ Site }}";?>

<?if(isset($head)):?>
<?=$head?>
<?endif;?>
<!-- ******** remote and server ips: <?= $_SERVER['REMOTE_ADDR'].'<>'.$_SERVER['SERVER_ADDR'] ?> -->
<!-- ******** server name: <?= $_SERVER['SERVER_NAME'] ?> -->
{% endblock head %}

{% block main %}
<?=$content?>
{% endblock main %}
{% block endjs %}
<?section("endjs")?>{{ block.super }}<?end_section("endjs")?>
{% endblock endjs %}
