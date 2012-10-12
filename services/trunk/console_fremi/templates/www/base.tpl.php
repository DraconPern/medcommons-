{% extends "www/base.html" %}

{% block title %}{{ CommonName }} <?if(isset($title)):?> - <?=$title?><?endif;?>{% endblock title %}

{% block head %}
<?$site = "{{ Site }}";?>

<?if(isset($head)):?>
<?=$head?>
<?endif;?>
<link rel='openid.server' href='http://{{ Domain }}/openid/server.php' />
<!-- ********remote and server ips: <?= $_SERVER['REMOTE_ADDR'].'<>'.$_SERVER['SERVER_ADDR'] ?> -->
<!-- ********server name: <?= $_SERVER['SERVER_NAME'] ?> -->
{% endblock head %}

{% block stamp %}
<?section("stamp")?>
  {% include 'www/stamp.html' %}
<?end_section("stamp")?>
{% endblock stamp %}

{% block main %}
<div id = 'content'>
<?=$content?>
</div>
{% endblock main %}

{% block footer %}
<?section("footer")?> {{ block.super }} <?end_section("footer")?>
{% endblock footer %}

{% block endjs %}
<?section("endjs")?>{{ block.super }}<?end_section("endjs")?>
{% endblock endjs %}
