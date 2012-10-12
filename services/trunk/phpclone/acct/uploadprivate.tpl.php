<?
/**
 * Upload from logged in account
 */
$template->extend("base.tpl.php");

$template->set("title","Upload to MedCommons Inbox");

?>


<?section("head")?>
<style type='text/css'>
table tr {
	height: 2.3em;
}

td.usertools * {
	vertical-align: middle;
}

td.usertools .icon {
	position: relative;
	top: -2px;
}

td {
	padding-left: 5px;
}
</style>
<?end_section("head")?>
<?block("content")?>
<div class="secondary_page1_1 secondary_portal"><!--googleon: index-->
<div class="innerPageGroup">

<div class="secondary_header ">
<div class="sheader_right">
<p class="text"><img src="/images/more.gif" alt="" />Download</p>
<p class="link"><a href="/samples.html">Samples to Upload.</a></p>
</div>
<div class="section_title">

<div class="vCenter">
<div class="vCenter_inner">
<div class="vCenter_body">
<h1>Upload a CD To <?=$group->name?></h1>
<p>please sign out to send scan somewhere else</p>
</div>
<!-- end vCenter_body --></div>
<!-- end vCenter_inner --></div>
<!-- vCenter --></div>
<!-- page title --></div>
<!-- end secondary_header --> <!-- marker:secondary_portal audience: -->


<div id="wrapper">
<form action="https://hot.medcommons.net/p/uh.php" id="page1form"
	method="post">
<fieldset>

<div id="blueTop">&nbsp;</div>
<div id="blueContainerResults"><input type=hidden name=UploadURL
	value="https://hot.medcommons.net/<?=$group->accid?>/upload" /> <input
	type=hidden name=GroupID value=<?=$group->accid?> /> <input type=hidden
	name=ConsultantID value=<?=$group->accid?> /> <input type=hidden
	name=ConsultantName value=<?=$group->accid?> /> <input type=hidden
	name=PatientID value=<?=$group->accid?> /> <input type=hidden
	name=SenderPhone value=<?=$group->accid?> /> <input type=hidden
	name=SenderSMS value=<?=$group->accid?> /> <input type=hidden
	name=VideoURL value=<?=$group->accid?> />
				<input type=hidden name=next value='/acct/index.php' />



<h1>Start the upload.&nbsp;</h1>
<span class="smallTitle">(Fill out the form, insert CD, and click NEXT)</span>
<div class="pg1WhiteText">Your information is always sent over secure
communications channels.</div>
<div class="topBottomLineY"></div>
<!--end topBottomLineY-->
<div class="bottompad20"><span class="yellowTextBold">All fields are
optional</span><!--end yellowTextBold-->

<div class="pg1WhiteText">The more detail you supply, the easier it is
for your collaborators.</div>
</div>
<!--end bottompad20-->

<div class="middleLine"></div>
<div class="left220w">
<div class="questionText"><label for="qq2">Sent by</label></div>
<!--end questionText--></div>
<!--end left22w-->

<div class="alignSettings600"><input class=infield id=qq2 type=text
	placeholder="Sender's Name" name=SenderName /> <br />
</div>

<div class="styleClear"></div>
<div class="left220w">
<div class="questionText"><label for="qq3">From Facility</label></div>
<!--end questionText--></div>
<!--end left22w-->
<div class="alignSettings600"><input class=infield id=qq3 type=text
	placeholder="Sender's Facility" name=Facility /> <br />
</div>

<div class="styleClear"></div>
<div class="left220w">

<div class="questionText"><label for="qq4">Sender's Email</label></div>
<!--end questionText--></div>
<!--end left22w-->
<div class="alignSettings600"><input class=infield type=email id=qq4
	placeholder="Sender's Email" name=SenderEmail /> <br />
</div>
<div class="styleClear"></div>

<div class="middleLine"></div>

<div class="left220w"><span class="questionText">Comment</span><br />
</div>
<!--end left22w-->
<div class="alignSettings600"><textarea class=infield rows=6 cols=50
	placeholder="History, notes and special requests" name=Clinical></textarea>

</div>
<!--end alignSettings600-->
<div class="styleClear"></div>

<div class="middleLine"></div>

<div class="left220w"><span class="questionText">Please also notify</span>
</div>
<!--end left22w-->
<div class="alignSettings600"><input class=infield type=email
	placeholder="Email" name=ConsultantEmail /></div>
<!--end alignSettings600-->
<div class="styleClear"></div>
<div class="topBottomLine"></div>
<div class="left750w">
<div class="whiteText">Please insert CD and click NEXT</div>
<!--end whiteTextSmall--></div>
<!--end left750w-->

<div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]--><!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]><input
	alt="Next"
	src="https://www.medcommons.net/_free/images/next-button.png?1279173875"
	type="image" /><![endif]></div>
<div class="styleClear"></div>

</div>

<!-- blue top --></fieldset>
</form>

<!-- wrapper and outer divs ended in footer --> <?end_block("content")?>

<?section("endjs")?> <?end_section("endjs")?>