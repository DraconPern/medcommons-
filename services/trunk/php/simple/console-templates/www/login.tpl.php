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
              <p class="text"><img src="../images/sys_images/ACAicon_medium.gif" alt="" />What's Inside?</p>
              <p class="link">   
              <a href="https://healthurl.medcommons.net/router/currentccr?a=1013062431111407&aa=1117658438174637&g=3642665a324059089d0b989286eb9a9ced046e54&t=&m=&c=&auth=c3faf3705e0e98c0984d1f86b96a789f3dba2638&at=c3faf3705e0e98c0984d1f86b96a789f3dba2638" 
              
			title="View All HealthURL Components"><span>View a Sample DICOM Health Record<span></a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Sign On To MedCommons Free</h1> 
                          <p>You must sign on to view any scans sent to you</p>
                          </div> <!-- end vCenter_body -->
              </div> <!-- end vCenter_inner -->
            </div> <!-- vCenter --> 
          </div> <!-- page title -->
  </div> <!-- end secondary_header -->

  

<!-- marker:secondary_portal -->


<div id="wrappers">
<div id="wrapper">
<div id="wrapper2">
<div id="wrapper3">
<div id="wrapper4">
<div id="wrapper5">
<div id="navcollb">
<div id="navcolbg_no">
<div id="navcolbg2">
<div id="sidecolbg_no">
<div id="sidecolspacer">  
<div id="sidecolspacer2">
<div id="sidecolspacer3">



                          
<div id="main">                     
<div class="syndicate"> 
<div id="mainbody2">

   <!-- <div class="mainbody_inner">--><!--ctac commented out-->
   
<div class="mainbody_inner">
<div class="mainbody_content">
          
          <!-- Begin Page Body -->

		
		
		           	
					
		<form action="/acct/login.php" id="page1form" method="post" >
<fieldset  >
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
		    <input type="text" style='font-size:1.3em' value="<?=$openid_url?>" id=x3 name="<?=$name?>" class="infield"/>
</div>
			<div class="styleClear"></div>

<div class="left220w">
				<div class="questionText"> 
					<label for="password">Password</label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">

		    <input style='font-size:1.3em' type="password" id="password" name="password"/>
</div>
	
				<div class="topBottomLine"></div>
			<div class="left750w" >
			<span class="bodyLink"><a class="bodyLink" target="popUp" href="/acct/forgot.php?next=/acct/home.php">
				<span class="yellowTextBold">Problems?</a></span></span>
		    <div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]-->
		    <!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]>
		    <input alt="Next" src="http://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" /><![endif]> </div>
		    <div class="styleClear"></div> 
			</div> <!-- left750w -->
		</div><!--end blueContainerResults-->
	</div><!--end blueTop-->
	</fieldset>
</form>
	
<!-- End Page Body -->
        </div> <!-- end mainbody_content -->
      </div> <!-- end mainbody_inner2 -->
   <!-- </div>--> <!-- end mainbody_inner ctac commented out-->
</div> <!-- end mainbody -->


</div> <!-- end syndicate -->
</div>
<!-- end main -->
<div class="clear">&nbsp;</div>

</div><!-- end sidecolspacer3 -->
</div><!-- end sidecolspacer2 -->
</div> <!-- end sidecolspacer -->
</div><!-- end sidecolbg -->
</div><!-- end navcolbg2 -->
</div><!-- end navcolbg -->
</div><!-- end navcollb -->
</div> <!-- end wrapper5 -->
</div> <!-- end wrapper4 -->


<?
end_block("content");
?>
