function updateLikes(id,l,d) {
	var like=Number(l);
	var dislike=Number(d);

	 var total=like+dislike;
	
	 if(total > 0) {
	 	$("[id^='p_like_"+id+"']").html(""+Math.round(like/total*100));
	 	 $("[id^='p_dislike_"+id+"']").html(""+Math.round(dislike/total*100)); 
	 	} 	
	 $("[id^='c_like_"+id+"']").html(""+like); 
	 $("[id^='c_dislike_"+id+"']").html(""+dislike); 
	 $("[id^='c_total_"+id+"']").html(""+total); 
	
}
$(document).ready(function() {	

 $("[id^='ci_like']").each(function(){
	 var id=$(this).attr('id').replace(/ci_([^_]*)_(.*)/,'$2');
	 
  $.get("/PSODeliveryCounters/services/count/like_"+id + "?var" + Math.floor(Math.random()*11),
		 function(data) {
				var like=data;
				$.get("/PSODeliveryCounters/services/count/dislike_"+id+"?var" + Math.floor(Math.random()*11),
						 function(data) {
						var dislike=data;
				    updateLikes(id,like,dislike);
				});
	});

	 		
			if($.cookie("cl_"+id)=="true") {
				$("div#feedbackstatus").html("<h3>Thanks for the feedback.</h3><p><label for='feedbackcomment'>Stay in touch with <a href=\"javascript:window.open('http://service.govdelivery.com/service/multi_subscribe.html?code=USHHSHC','Popup','width=780,height=440,toolbar=no,scrollbars=yes,resizable=yes'); void('');\" onClick=\"window.status='Sign Up'; return true\" onMouseOver=\"window.status='Sign Up'; return true\" onMouseOut=\"window.status=''; return true\">e-mail updates</a></label></p>")
				//$("div#feedbackstatus").html("<p>Thank you for your participation to make the site even better.</p>");
				//$(this).hide();
				//$("[id='ci_dislike_"+id+"']").hide();
			}
			
			
			/******** page rating effects ***********/
		$("[id^='ci_like']").each(function(){
			$(this).mouseover(function() {
				$(this).children("img").attr("src",rxs_navbase + "/images/sys_images/comment-yes-checked.gif");
			}).mouseout(function (){
				$(this).children("img").attr("src",rxs_navbase + "/images/sys_images/comment-yes-unchecked.gif");
			});
		});
		
		$("[id^='ci_dislike']").each(function(){
			$(this).mouseover(function() {
				$(this).children("img").attr("src",rxs_navbase + "/images/sys_images/comment-no-checked.gif");
			}).mouseout(function (){
				$(this).children("img").attr("src",rxs_navbase + "/images/sys_images/comment-no-unchecked.gif");
			});
		});
			
  	
});

 $("[id^='ci_']").click(function () { 
		 var counter=$(this).attr('id').replace(/ci_([^_]*).*/,'$1');
		 var id=$(this).attr('id').replace(/ci_([^_]*)_(.*)/,'$2');
		 $.post("/PSODeliveryCounters/services/count/"+counter+"_"+id + "?var" + Math.floor(Math.random()*11),
				 function(data) {
			$.cookie("cl_"+id, "true")
			
			if (counter=="like") {
					like = data;
					dislike = $("[id^='c_dislike_"+id+"']").html();
					
					$("div#feedbackstatus").html("<h3>Great!</h3><p><label for='feedbackcomment'>Could we make it even better?</label></p>")
			} else {
					like = $("[id^='c_like_"+id+"']").html();
					dislike=data;
					$("#evaluate_option").attr("value", "No");
					
 

					$("div#feedbackstatus").html("<h3>We're listening</h3><p><label for='feedbackcomment'>How can we make it better?</label></p>")
			}
			
			$("[id='ci_dislike_"+id+"']").hide();
			$("[id='ci_like_"+id+"']").hide();
			
			//alert(data);
			$("div#feedbackblock").slideDown("slow");				
			if (data > 0) {
				//alert('working');
				//$("div#feedbackblock").slideDown("slow");
				//updateLikes(id,like,dislike);				
			}
			});
			return false;

 });  	
 
 var options = { 
 				url: "/healthcare_ugc/feedback.do",
				dataType: 'text',
        beforeSubmit:  showRequest,  // pre-submit callback 
        success:       showResponse  // post-submit callback 
    }; 
 
    // bind to the form's submit event 
    $('#feedback_form').submit(function() {         
        $(this).ajaxSubmit(options); 
        return false; 
    }); 
	
	
});

// pre-submit callback 
function showRequest(formData, jqForm, options) {     
    var queryString = $.param(formData); 
    //alert(queryString); 
    /*
    if(formElement.body.value == ""){
    	alert("Comment is required to submit")
    	return false;
    } 
    */
    return true; 
} 
 
// post-submit callback 
function showResponse(responseText, statusText, xhr, $form)  {     
		if(statusText == "success"){			
			  
  

			
			
			$("div#feedbackstatus").html("<h3>Thanks for the feedback.</h3><p><label for='feedbackcomment'>Stay in touch with <a href=\"javascript:window.open('http://service.govdelivery.com/service/multi_subscribe.html?code=USHHSHC','Popup','width=780,height=440,toolbar=no,scrollbars=yes,resizable=yes'); void('');\" onClick=\"window.status='Sign Up'; return true\" onMouseOver=\"window.status='Sign Up'; return true\" onMouseOut=\"window.status=''; return true\">e-mail updates</a></label></p>")
			$("div#feedbackblock").hide();
		}  
} 