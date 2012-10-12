<?
    require_once "template.inc.php";
    require_once "utils.inc.php";
    
    if(!isset($template)) {
        echo template("nojs.tpl.php")->fetch();
        exit;
    }
    
    $template->set("title","Javascript Support Required")
             ->extend("base.tpl.php");
?>

<?section("content")?>
	<h2>Javascript Disabled</h2>
	<p>We were unable to detect Javascript enabled in your browser.   This web site requires a browser supporting Javascript and for the
	Javascript support to be enabled.   If your browser supports Javascript, please check in your settings or with your system administrator to see 
	if Javascript is enabled.   If your browser does not support Javascript you will need upgrade to or install a new browser supporting 
	Javascript to use our service.</p>
	<p>To try the page again, <a href='<?=htmlentities(req('referrer','/'),ENT_QUOTES)?>'>Click Here</a></p>
<?end_section("content")?>