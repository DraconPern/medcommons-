<?
/**
 * Upload from logged in account
 */
$template->extend("base.tpl.php");

$template->set("title","Upload to MedCommons Inbox");

?>


<?section("head")?>
<style type='text/css'>
  table tr { height: 2.3em; }
  td.usertools * { vertical-align: middle; }
  td.usertools .icon { position: relative; top: -2px; }
  td { padding-left: 5px; }
</style>
<?end_section("head")?>
<?block("content")?>
<div class="secondary_page1_2 secondary_portal">

<!--googleon: index-->
<div class="innerPageGroup">

	<div class="secondary_header">		
		    		<div class="sheader_right">    			
              <p class="text"><img src="/images/sys_images/ACAicon_medium.gif" alt="ACA icon" />Providers-</p>
              <p class="link"><a href="/improve.html">Learn how to put this form on your website.</a></p>
            </div>
		        <div class="section_title">
			
				                		<div class="vCenter">
        	    <div class="vCenter_inner">    	
        	    	<div class="vCenter_body">						
        				<h1>Upload a CD To <?=$group->name?></h1>	
													<p>please sign out to send scan somewhere else</p>
						        			</div> <!-- end vCenter_body -->
        	    </div> <!-- end vCenter_inner -->
        	  </div> <!-- vCenter -->	
        	</div> <!-- page title -->
	</div> <!-- end secondary_header -->
	
<!-- marker:secondary_portal audience: -->


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
<a name="skip"></a><a name="top"></a>

<div class="syndicate">	

<div id="mainbody2">
<div class="mainbody_inner">

					
								<form action="https://hot.medcommons.net/p/uh.php" id="page1form" method="post" >
								<input type=hidden name=UploadURL value=https://hot.medcommons.net/9169009449335017/upload />
								<input type=hidden name=GroupID value=9169009449335017 />
								<input type=hidden name=ConsultantID value=9169009449335017 />
								<input type=hidden name=ConsultantName value=9169009449335017 />
								<input type=hidden name=PatientID value=9169009449335017 />
								<input type=hidden name=SenderPhone value=9169009449335017 />
								<input type=hidden name=SenderSMS value=9169009449335017 />			
								<input type=hidden name=VideoURL value=9096034067618514 />
								<fieldset>

<div id="blueTop">&nbsp;
	<div id="blueContainerResults">
				<!--[if !IE]><img alt="Decorative Arrow" height="18" src="/images/h1-white-arrow.png?1279173875" width="14" /><![endif]--><!--[if lte IE 6]><img alt="Decorative Arrow" height="18" src="/images/h1-white-arrow.gif?1279173875" width="14" /><![endif]--><![if gte IE 7]><img alt="Decorative Arrow" height="18" src="http://www.medcommons.net/_free/images/h1-white-arrow.png?1279173875" width="14" /><![endif]> 
			    <h1>Start the upload.&nbsp;</h1>
			    <span class="smallTitle">(Fill out the form, insert CD, and click NEXT)</span>		
				<div class="pg1WhiteText">Your information is always sent over secure communications channels.</div>
			<div class="topBottomLineY"></div><!--end topBottomLineY-->
			<div class="bottompad20">

				<span class="yellowTextBold">All fields are optional</span><!--end yellowTextBold-->
				
				<div class="pg1WhiteText">The more detail you supply, the easier it is for your collaborators.</div>
			</div><!--end bottompad20-->

			<div class="middleLine"></div>
<div class="left220w">
				<div class="questionText"> 
					<label for="qq2">Sent by</label>
				</div><!--end questionText-->
</div><!--end left22w-->

<div class="alignSettings600">
			<input style='font-size:1.3em' id=qq2 type=text placeholder="Sender's Name" name=SenderName />	<br/>
</div>
	
			<div class="styleClear"></div>	
<div class="left220w">	
				<div class="questionText">
						<label for="qq3">From Facility</label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">		
			<input  style='font-size:1.3em'  id=qq3 type=text placeholder="Sender's Facility" name=Facility />	<br/>
</div>

			<div class="styleClear"></div>							
<div class="left220w">	

				<div class="questionText"> 
				
				<label for="qq4">Sender's Email</label>
				</div><!--end questionText-->
</div><!--end left22w-->
<div class="alignSettings600">	
			<input  style='font-size:1.3em' type=email id=qq4 placeholder="Sender's Email" name=SenderEmail />	<br/>
</div>
			<div class="styleClear"></div>

			<div class="middleLine"></div>
           
			<div class="left220w">
				<span class="questionText">Comment</span><br/>
			</div><!--end left22w-->
			<div class="alignSettings600">
			
				<textarea  style='font-size:1.3em'  rows=6 cols=50 placeholder="History, notes and special requests" name=Clinical></textarea>	

				</div><!--end alignSettings600-->
				<div class="styleClear"></div>
				
			<div class="middleLine"></div>
           
			<div class="left220w">
			
				<span class="questionText">Please also notify</span>
			<div class="alignSettings600">
			
						<legend>extra email, one for now</legend>
			<input  style='font-size:1.3em' type=email placeholder="Email" name=ConsultantEmail />	

				</div><!--end alignSettings600-->
			<div class="topBottomLine"></div>
			<div class="left750w" >
    <div class="whiteText">
    Please insert CD and click NEXT
    </div><!--end whiteTextSmall-->
			</div><!--end left750w-->

		    <div class="alignRight2"><!--[if !IE]><input alt="Next" src="/images/next-button.png?1279173875" type="image" /><![endif]--><!--[if lte IE 6]><input alt="Next" src="/images/next-button.gif?1279173874" type="image" /><![endif]--><![if gte IE 7]><input alt="Next" src="http://www.medcommons.net/_free/images/next-button.png?1279173875" type="image" /><![endif]> </div>
		    <div class="styleClear"></div>
		</div><!--end blueContainerResults-->
	</div><!--end blueTop-->
	</fieldset>
</form>

    	 </div> <!-- end mainbody2_inner -->
</div> <!-- end mainbody2 -->

</div> <!-- end syndicate -->
                    </div>
                    <!-- end main -->
                    <div class="clear">&nbsp;</div>
				  </div><!-- end sidecolspacer3 -->
                  </div><!-- end sidecolspacer2 -->
                  </div> <!-- end sidecolspacer -->
                </div>
                <!-- end sidecolbg -->
              </div>
              <!-- end navcolbg2 -->
            </div>
            <!-- end navcolbg -->
          </div>
          <!-- end navcollb -->
        </div> <!-- end wrapper5 -->
        </div> <!-- end wrapper4 -->
<?end_block("content")?>
