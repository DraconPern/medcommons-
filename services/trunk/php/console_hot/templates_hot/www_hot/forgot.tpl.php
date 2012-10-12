{% extends "www/base.html" %}

{% block head %}

{% endblock head %}

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



<div id="wrapper">

          
          
          
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

		    <input type="text" style='font-size:1.3em'  value='<?= $mcid ?>' id='mcid' name="mcid" class="infield"/>
</div>
			<div class="styleClear"></div>


				<div class="topBottomLine"></div>
			<div class="left750w" >
			<span class="bodyLink"><a class="bodyLink" target="popUp" href="/acct/forgot.php?next=/acct/home.php">
				<span class="yellowTextBold">You will receive an email with instructions to reset your password.</span></a></span>
		    <div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]-->
		    <!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]>
		    <input alt="Next" src="https://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" /><![endif]> </div>
		    <div class="styleClear"></div> 
			</div> <!-- left750w -->
		</div><!--end blueContainerResults-->
	</div><!--end blueTop-->
	</fieldset>
	</form><script type="text/javascript">
document.password.mcid.focus();
</script>


</div>
{% endblock main %}
