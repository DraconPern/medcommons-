<?

/**
 * This version of the Inbox page uses YUI widgets to display the inbox and upload form
 */
$template->extend("inbox.tpl.php");
?>
<?section("membersblock")?>
<style type='text/css'>
span.h4Sized {
 color: #3b5269 !important;
}
#container  {
    display: none;
}

</style>
<h2>Members</h2>
<?if(count($members)):?>
<div id='container' class='yui-skin-sam'>
<ol>
<?foreach ($members as $patient):?>
    <?
        $photourl = $patient ->photoUrl;
        if($photourl=='') 
            $photourl = gpath("Secure_Url").'/images/unknown-user.png';
        $name = trim("$patient->PatientGivenName $patient->PatientFamilyName");
        $when = strftime('%m/%d/%y %H:%M:%S', $patient->CreationDateTime);
        $date = substr($when,0,8);
        $time = substr($when,9,5);
    ?>
    <li><a href='<?=gpath('Secure_Url')."/{$patient->PatientIdentifier}"?>'><img src='<?=htmlentities($photourl, ENT_QUOTES)?>'/></a>
        <div class='stime'><?=$date?> </div>
        <?=htmlentities($name)?> </li>
<?endforeach?>    
</ol>
</div>
<style type='text/css'>
/* Always be sure to give your carousel items a width and a height */
.yui-carousel-element li {
    width: 100px;
    height: 105px;
}

.yui-carousel-element .yui-carousel-item-selected {
    border:0; /* Override selected item's dashed border so it feels more like a photo album */
}
</style>

<?/*
<script type="text/javascript" src="/yui/2.8.0r4/yahoo-dom-event/yahoo-dom-event.js"></script> 
<script type="text/javascript" src="/yui/2.8.0r4/element/element-min.js"></script> 
<script type="text/javascript" src="/yui/2.8.0r4/carousel/carousel-min.js"></script> 
<script type="text/javascript" src="/yui/2.8.0r4/animation/animation-min.js"></script> 
*/?>
<link type="text/css" rel="stylesheet" href="/zip/yui/2.8.0r4/carousel/assets/skins/sam/carousel.css">

<script type="text/javascript" src="/zip/yui/2.8.0r4/yahoo-dom-event/yahoo-dom-event.js,yui/2.8.0r4/element/element-min.js,yui/2.8.0r4/carousel/carousel-min.js,yui/2.8.0r4/animation/animation-min.js"></script>

<script type='text/javascript'>
/* Needs to be consistent with CSS above*/
var thumbWidth = 100;
var numMembers = <?=count($members)?>;
YAHOO.util.Event.onDOMReady(function (ev) {
    var carousel = new YAHOO.widget.Carousel("container", {
        // specify number of columns and number of rows
        numVisible: [Math.min(numMembers,Math.round((YAHOO.util.Dom.getViewportWidth() - 100) / thumbWidth)),2],
        numItems: numMembers
    });
    carousel.set("animation", { speed: 0.5 });
    carousel.render(); // get ready for rendering the widget
    carousel.show();   // display the widget
    $('container').style.display = 'block';
});
</script>
<?else:?>
<p>No members</p>
<?endif?>
<?end_section("membersblock")?>