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

                <h1>Change User Name</h1> 
                          <p>
You can freely change your user name at any time </p>
                          </div> <!-- end vCenter_body -->
              </div> <!-- end vCenter_inner -->
            </div> <!-- vCenter --> 
          </div> <!-- page title -->
  </div> <!-- end secondary_header -->

  

<!-- marker:secondary_portal -->


<div id="wrapper">

          
          
          <!-- Begin Page Body -->


<form method='post' action='edituser.php'>
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
				<span class="yellowTextBold"><?= $first_name ?> <?= $last_name ?></span><!--end yellowTextBold-->
			</div><!--end bottompad20-->
		
<div class="left220w">
				<div class="questionText"> 
					<label for="first_name">First Name<small></small></label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">

		    <input type="text" style='font-size:1.3em'  name='first_name' value='<?= $first_name ?>'  id='first_name'  class="infield"/>
</div>
			<div class="styleClear"></div>
			
<div class="left220w">
				<div class="questionText"> 
					<label for="last_name">Last Name<small></small></label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">

		    <input type="text" style='font-size:1.3em'  name='last_name' value='<?= $last_name ?>'  id='last_name'  class="infield"/>
</div>
			<div class="styleClear"></div>

				<div class="topBottomLine"></div>
			<div class="left750w" >
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

{% endblock main %}
