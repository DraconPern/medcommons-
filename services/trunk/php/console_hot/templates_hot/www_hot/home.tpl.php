{% extends "www/base.html" %}

{% block title %}<?if(isset($title)):?><?=htmlentities($title)?><?else:?>{{ title }}<?endif;?>{% endblock %}

{% block head %}

<?
require_once "urls.inc.php";
global $Secure_Url;
?>

    <link rel="stylesheet" href="<?=$httpUrl?>/acct_all.css.php" type="text/css"/>
    <? include "acctjs.php" ?>
    <script type="text/javascript">
        function calculateHeight() {
          if($('featureboxes')) { // prevents errors on load
            var h = $('featureboxes').scrollHeight + 12;
            return h;
          }
          else
            return 0;
        }
    </script>
    <!--[if lt IE 7]>
      <script type="text/javascript">
        function calculateHeight() {
          if (document.body.scrollHeight > document.body.offsetHeight){ // all but Explorer Mac
            h = document.body.scrollHeight;
          } 
          else { // works in Explorer 6 Strict, Mozilla (not FF) and Safari
            h = document.body.offsetHeight;
          }
          return h;
        }
      </script>
    <![endif]-->
    <script type="text/javascript">
      var auth = '<?=$info->auth;?>';

      function init() {

        <? // Set up auto-sync'ing of iframe heights ?>
        addHeightSync();

        <?// Initialize context manager.  Do it AFTER other stuff has a chance to load to help performance?>
        <?if($info->practice):?>
          window.setTimeout(function(){
            setAccountFocus('<?=$info->accid?>', '<?=$info->practice->accid?>',
              '<?=htmlentities($info->practice->practicename,ENT_QUOTES)?>', '<?=$info->auth?>', 
              '<?=$gwUrlParts['host']?>','<?=$gwUrlParts['port']?>', 
              '<?=$gwUrlParts['scheme']?>','/gateway/services/CXP2', '<?=$Secure_Url?>');
          },1500);
        <?endif;?>
      }
    </script>

{% endblock head %}

{% block stamp %}
  {% include 'www/stamp.html' %}
{% endblock stamp %}

{% block body%}onload="init();"{% endblock %}

{% block main %}
    <?=$content?>
{% endblock %}
