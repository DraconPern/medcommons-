{% extends "www/base.html" %}

{% block main %}



<div class="secondary_page1_1 secondary_portal">

<!--googleon: index-->

<div class="innerPageGroup">
  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/more.gif" alt="" />Healthcare Providers:</p>
              <p class="link"><a href="http://www.healthcare.gov/improve.html">Get referrals and keep track of consults</a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Change Email Address</h1> 
                          <p>

Your email address will not be changed immediately.</p>
                          </div> <!-- end vCenter_body -->
              </div> <!-- end vCenter_inner -->
            </div> <!-- vCenter --> 
          </div> <!-- page title -->
  </div> <!-- end secondary_header -->

  

<!-- marker:secondary_portal -->



<div id="wrapper">

	<form method='post' action='setemail.php'>
<fieldset  >
   <div id="blueTop">&nbsp;
	<div id="blueContainerResults">
	
	<?php
if (isset($error)) {
  echo "<div class='formError'>";
  echo $error;
  echo "</div>";
} ?>
			<div class="bottompad20">
				<span class="yellowTextBold">Current email is <?= $email ?></span><!--end yellowTextBold-->
			</div><!--end bottompad20-->
		
<div class="left220w">
				<div class="questionText"> 
					<label for="mcid">New Email<small></small></label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">

		    <input type="text" style='font-size:1.3em'  value='<?= $email ?>'  id='mcid' name="email" class="infield"/>
</div>
			<div class="styleClear"></div>


				<div class="topBottomLine"></div>
			<div class="left750w" >
			<span class="bodyLink"><a class="bodyLink" target="popUp" href="/acct/forgot.php?next=/acct/home.php">
				<span class="yellowTextBold">We will send out a confirmation email.
Click on the link contained in that email
to complete the change.</a></span></span>
		    <div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]-->
		    <!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]>
		    <input alt="Next" src="http://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" name="submitButton" /><![endif]> </div>
		    <div class="styleClear"></div> 
			</div> <!-- left750w -->
			</form>
		</div><!--end blueContainerResults-->
	</div><!--end blueTop-->
	</fieldset>
	</form><script type="text/javascript">
document.password.mcid.focus();
</script>
</div>
{% endblock main %}
