<?
$template->extend("base.tpl.php");
$template->set("title", "Welcome to MedCommons - Login");

// The name of username field depends on whether we're doing an openid login
$name = (isset($password) && $password) ? 'mcid' : 'openid_url';

if(!isset($openid_url)) {
  $openid_url = "";
}

block("head");
?>
<?
end_block("head");



block("content");
?>

<?if(isset($prompt)):?>
	<?=$prompt?>
<?endif;?>


<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">
  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="/images/more.gif" alt="" ><br/>view sample</p>
              <p class="link">   
              <a href="https://healthurl.medcommons.net/router/currentccr?a=1013062431111407&amp;aa=1117658438174637&amp;g=3642665a324059089d0b989286eb9a9ced046e54&amp;t=&amp;m=&amp;c=&amp;auth=c3faf3705e0e98c0984d1f86b96a789f3dba2638&amp;at=c3faf3705e0e98c0984d1f86b96a789f3dba2638" 
              
			title="View All HealthURL Components"><span>DICOM-enabled Health Record</span></a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Sign On To MedCommons</h1> 
                          <p>You must sign on to view any scans sent to you</p>
                          </div> <!-- end vCenter_body -->
              </div> <!-- end vCenter_inner -->
            </div> <!-- vCenter --> 
          </div> <!-- page title -->
  </div> <!-- end secondary_header -->

  

<!-- marker:secondary_portal -->

<div id="wrapper">

       
          <!-- Begin Page Body --> 	
					
<form action="/acct/login.php" id="page1form" method="post" >


   <div id="blueTop">&nbsp;
	<div id="blueContainerResults">
	
		<?if(isset($error)): ?>
		  <div class='formError'>
		    <?= $error ?>
		  </div>
		<?endif?>
   <h1>Please sign on.&nbsp;</h1>
			    <span class="smallTitle"></span>		
				<div class="pg1WhiteText"></div>
			<div class="middleLine"></div>
			<div class="bottompad20">
				<span class="yellowTextBold">All fields are required</span><!--end yellowTextBold-->
			</div><!--end bottompad20-->
		
<div class="left220w">
				<div class="questionText"> 
					<label for="x3">Email<small></small></label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">
		    <input type="text" style='font-size:1.3em' value="<?=$openid_url?>" id=x3 name="<?=$name?>" class="infield">
</div>
			<div class="styleClear"></div>

<div class="left220w">
				<div class="questionText"> 
					<label for="password">Password</label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">

		    <input style='font-size:1.3em' type="password" id="password" name="password">
</div>
	
				<div class="topBottomLine"></div>
			<div class="left750w" >
			<span class="bodyLink"><a class="bodyLink" href="/acct/forgot.php?next=/acct/home.php">
				<span class="yellowTextBold">Problems?</span></a></span>
		    <div class="alignRight2">
		    <input alt="Next" src="https://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" > </div>
		    <div class="styleClear"></div> 
			</div> <!-- left750w -->
		</div><!--end blueContainerResults-->
	</div><!--end blueTop-->

</form>
	
<!-- End Page Body -->

<!-- wrapper and outer divs ended in footer -->



<?
end_block("content");
?>
