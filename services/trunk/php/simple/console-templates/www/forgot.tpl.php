{% extends "www/base.html" %}

{% block head %}

{% endblock head %}

{% block main %}

<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">
  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/sys_images/ACAicon_medium.gif" alt="" />Healthcare Providers:</p>
              <p class="link"><a href="http://www.healthcare.gov/improve.html">Get referrals and keep track of consults</a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Password Recovery</h1> 
                          <p>
We can send password recovery instructions to
the email address we have on file.</p>
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



<form method='post' action='forgot.php' id='password' name='password'>
<fieldset  >
   <div id="blueTop">&nbsp;
	<div id="blueContainerResults">
	
			<div class="bottompad20">
				<span class="yellowTextBold">All fields are required</span><!--end yellowTextBold-->
			</div><!--end bottompad20-->
		
<div class="left220w">
				<div class="questionText"> 
					<label for="mcid">Email<small></small></label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">

		    <input type="text" style='font-size:1.3em'  value='<?= $mcid ?>' id='mcid' name="<?=$name?>" class="infield"/>
</div>
			<div class="styleClear"></div>


				<div class="topBottomLine"></div>
			<div class="left750w" >
			<span class="bodyLink"><a class="bodyLink" target="popUp" href="/acct/forgot.php?next=/acct/home.php">
				<span class="yellowTextBold">You will receive an email with instructions to reset your password.</a></span></span>
		    <div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]-->
		    <!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]>
		    <input alt="Next" src="http://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" /><![endif]> </div>
		    <div class="styleClear"></div> 
			</div> <!-- left750w -->
		</div><!--end blueContainerResults-->
	</div><!--end blueTop-->
	</fieldset>
	</form><script type="text/javascript">
document.password.mcid.focus();
</script>

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
</div>
</div>
{% endblock main %}
