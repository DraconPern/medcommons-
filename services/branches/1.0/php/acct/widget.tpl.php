<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" style="border-style: none;">
  <head>
      <?if(isset($title)):?><title><?=$title?></title><?endif;?>
      <link rel="alternate" type="application/rss+xml" title="rss" href="https://www.medcommons.net/acct/rss.php"/>
      <link rel="shortcut icon" href="images/favicon.gif" type="image/gif"/>
      <? include "acctjs.php" ?>
      <link rel="stylesheet" type="text/css" href="acct_all.css"/>
      <link rel="stylesheet" type="text/css" href="../css/medCommonsStyles.css"/>
      <style type='text/css'>
        #main h2 {
            font-size: 120%;
            font-weight: bold;
            margin-bottom: 0px;
        }
        #worklistButtons {
            top: 5px;
        }
      </style>
  </head>
  <body style="background: white;">
    <div id="main">
    <?=$content;?>
    </div>
    <script type="text/javascript">
       addLoadEvent(addHeightMonitor);
    </script>
  </body>
</html>
