<?

$template->extend("base.tpl.php");
$template->set("title","MedCommons Free Registration");
?>

<?block("head")?>

<?end_block("head")?>

<?block("content")?>

<div class="secondary_page1_1 secondary_portal ">

<div class ="innerPageGroup">
  <div class="secondary_header">    
            <div class="sheader_right">         
              <p class="text"><img src="../images/more.gif" alt="" />Healthcare Providers:</p>
              <p class="link"><a href="http://www.healthcare.gov/improve.html">You might want multiple groups</a></p>
            </div>
            <div class="section_title">
      
                            <div class="vCenter">
              <div class="vCenter_inner">     
                <div class="vCenter_body">

                <h1>Create Your Inbox</h1> 
                          <p>Keep track of health records and radiology for yourself, your family or your patients.</p>
                          </div> <!-- end vCenter_body -->
              </div> <!-- end vCenter_inner -->
            </div> <!-- vCenter --> 
          </div> <!-- page title -->
  </div> <!-- end secondary_header -->

  

<div id=wrapper>
								
<form name='registrationForm' method='post' action='groupsetup.php'>

        <input type=hidden value=basic id='portalType' name='portalType'>
        <input type=hidden id='inviteEmails' name='inviteEmails' class='infield' value='<?=field("inviteEmails")?>'/>
<fieldset>


            	
<div id="blueTop">&nbsp;</div>
	<div id="blueContainerResults">
					
	<?if(isset($errors)):?>
    <p class='formError'>One or more errors was found in your form.  Please correct them and try submitting again.</p>

<?endif;?>
   <h1>Let&#8217;s get started.&nbsp;</h1>
			    <span class="smallTitle">(Just two quick steps)</span>		
				<div class="pg1WhiteText">You'll receive an email requesting confirmation of your identity and then you are free to use MedCommons.</div>
			<div class="middleLine"></div>
			
					<div class="bottompad20">
				<span >Pick a name for your Inbox that others will see as your secure health records dropbox.
				<a target='_new' href="/privacy.html"><span class="yellowTextBold">Privacy Policy</span></a></span>		
					</div><!--end bottompad20-->	
				  <?if(isset($errors->practiceName)):?><div class='formError'><?=error_msg('practiceName')?></div><?endif;?>
<div class="left220w">
				<div class="questionText"> 
					<label for="x1">Inbox Name</label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">
    <input id=x1 type='text'  size=47 name='practiceName' class='infield' placeholder="User, Practice, or Group Name" value='<?=field('practiceName')?>'/>
    
</div>
			
			<div class="styleClear"></div>

			<div class="middleLine"></div>
			
<div class="bottompad20">
				<span class="yellowTextBold">Enter contact details so we can notify you of incoming radiology</span>		
				<?if(isset($errors->ln)||isset($errors->fn)):?> <div class='formError'><?=error_msg('fn')?><?=error_msg('ln')?></div><?endif;?>
        <?if(isset($errors->email)):?><div class='formError'><?=error_msg('email')?></div><?endif;?>
</div><!--end bottompad20-->

<div class="left220w">
				<div class="questionText"> 
					<label for="fn">First Last</label>
				</div><!--end questionText-->
				
 
</div><!--end left22w-->

<div class="alignSettings600"  id='personalDetails'>
       <input class='infield'  size=20 type='text' name='fn' id='fn' placeholder="First Name" value='<?=field('fn')?>' />&nbsp;&nbsp;&nbsp;
  
       <input class='infield'  size=20 type='text' name='ln' id='ln' placeholder="Last Name" value='<?=field('ln')?>' />
     
</div>
	
					<div class="styleClear"></div>
					
<div class="left220w">
				<div class="questionText"> 
					<label for="email">Email</label>
				</div><!--end questionText-->
</div>
    
<div id='p_email' class="alignSettings600">
        <span id='p_email'>
        
      <input size=47  class='infield'  type='email' name='email' id='email' placeholder="Email" value='<?=field('email')?>' /></span>
  
</div>
			
					<br/>
					
					<div class="styleClear"></div>

			<div class="middleLine"></div>
			
					<div class="bottompad20">
				<span class="yellowTextBold">Please enter a password (6-15 characters, a-z,0-9)</span>		
</div><!--end bottompad20-->
			
				  <?if(isset($errors->pw1)):?><div class='formError'><?=error_msg('pw1')?></div><?endif;?>			
<div class="left220w">
				<div class="questionText"> 
					<label for="pw1">Password</label>
				</div><!--end questionText-->
</div>	

<div id='p_pw1' id='p_email'class="alignSettings600">
			     <input class='infield'  size=22 type='password' name='pw1' id='pw1' placeholder="Password"  /> 
   
</div>   <br/>

				  <?if(isset($errors->pw2)):?><div class='formError'><?=error_msg('pw2')?></div><?endif;?>	
<div class="left220w">
        		<div class="questionText"> 
					<label for="pw2">Repeat Password</label>
				</div><!--end questionText-->
</div>

<div id 'p_pw2' class="alignSettings600">
			     <input class='infield' size=22 type='password' name='pw2' id='pw2'  placeholder="Repeat Password"/>  
    
 </div>     
       
			<div class="styleClear"></div>

					
				<div class="topBottomLine"></div>
			<div class="left750w" >
    <div class="whiteText">
			
				  <?if(isset($errors->termsOfUse)):?><div class='formError'><?=error_msg('termsOfUse')?></div><?endif;?>				
								
								      <label>
	        <input class='infield'  type='checkbox' name='termsOfUse' id='termsOfUse' <?if(field('termsOfUse')):?>checked='true'<?endif;?> />
	        I have read and understand the
	        <a target='_new' href="/termsofuse.php"><span class="yellowTextBold">Terms Of Use</span></a>

	      </label>
	

	      
    </div><!--end whiteTextSmall-->
			</div><!--end left750w-->

		    <div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]-->
		    <!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]>
		    <input alt="Next" src="http://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" name='submitButton' value='Submit' /><![endif]> </div>
		    <div class="styleClear"></div>
	
	   



	</div><!--end blueContainerResults-->

	</fieldset>
</form>

<!-- wrapper and outer divs ended in footer -->



<?end_block("content")?>
