


//***************************************************//
//         Text sizing version .9                    //
//***************************************************//

//var sizesArray = new Array("60%", "70%", "80%", "90%", "100%", "110%", "120%", "130%", "140%");
var sizesArray = new Array("70%", "80%", "90%", "100%", "110%", "120%", "130%");

var sizePointer = 3;
var ruleCounter;
// ruleCounter is used for Mozilla due to the necessity to write the new CSS rule in the last position so that it is applied.


if (getCookie("sizePref") != "") {
	sizePointer = Number(getCookie("sizePref"))
	// now apply the font
	if (document.styleSheets[0].cssRules) {
		ruleCounter = document.styleSheets[0].cssRules.length
	// Mozilla
	document.styleSheets[0].insertRule("#navcolbg {font-size: " + sizesArray[sizePointer] + ";}", ruleCounter)
		//ruleCounter = ruleCounter + 1
	}
	else {
	//IE
	document.styleSheets[0].addRule("#navcolbg", "{font-size: " + sizesArray[sizePointer] + ";}")
	}
}

var ie=false;
if (navigator.appName.indexOf("Microsoft") != -1){
	ie=true;
}
	


function getCookie(c_name)
{
if (document.cookie.length>0)
  {
  c_start=document.cookie.indexOf(c_name + "=")
  if (c_start!=-1)
    { 
    c_start=c_start + c_name.length+1 
    c_end=document.cookie.indexOf(";",c_start)
    if (c_end==-1) c_end=document.cookie.length
    return unescape(document.cookie.substring(c_start,c_end))
    } 
  }
return "";
}

function largerFont () {
	setEqualHeight("default");	
	if (document.styleSheets[0].cssRules) {
	// Mozilla
		if (document.styleSheets[0].cssRules[0]) {
			if (sizePointer != 8) {
				ruleCounter = document.styleSheets[0].cssRules.length;
				sizePointer = sizePointer + 1;
				document.styleSheets[0].insertRule("#wrapper5 {font-size: " + sizesArray[sizePointer] + ";}", ruleCounter)
				//document.write('<style>* {font-size: ' + sizesArray[sizePointer] + ';}</style>');
				document.cookie = 'sizePref='+ sizePointer + '; path=/; domain=.healthcare.gov';
			}
		}	
	}
	
	else if (document.styleSheets[0].rules) {
	// IE
	
		if (sizePointer < 8) {
			
			sizePointer = sizePointer + 1;
			document.cookie = 'sizePref='+ sizePointer + '; path=/; domain=.healthcare.gov';			
			document.styleSheets[0].addRule("#wrapper5", "font-size: " + sizesArray[sizePointer])
			}
		}		
	setEqualHeight();
}

function smallerFont () {	
	setEqualHeight("default");
	if (document.styleSheets[0].cssRules) {
		
	// Mozilla
		if (document.styleSheets[0].cssRules[0]) {	
			if (sizePointer != 0) {
				sizePointer = sizePointer - 1;
				ruleCounter = document.styleSheets[0].cssRules.length;
				document.styleSheets[0].insertRule("#wrapper5 {font-size: " + sizesArray[sizePointer] + ";}", ruleCounter)
				document.cookie = 'sizePref='+ sizePointer + '; path=/; domain=.healthcare.gov';
			}
		}	
	}
	
	else if (document.styleSheets[0].rules) {
	// IE		
		if (sizePointer > 0) {
			sizePointer = sizePointer - 1;
			document.cookie = 'sizePref='+ sizePointer + '; path=/; domain=.flu.gov';
			document.styleSheets[0].addRule("#wrapper5", "font-size: " + sizesArray[sizePointer])
		}
	}
	setEqualHeight();
}

function emailupdate(){
	window.open('http://service.govdelivery.com/service/multi_subscribe.html?code=USHHSHC','Popup','width=780,height=440,toolbar=no,scrollbars=yes,resizable=yes'); void(''); 	
}


var cur_spotlight = 1;
$(document).ready(function () {	
	/********* Control Hover **************/
	
	var tmp;
	
	/* font size buttons */
	$("p#control a#largerfont").hover(		
		function(){			
				tmp = $(this).children("img").attr("src");				
				$(this).children("img").attr("src",getpath(tmp) + "font-larger-hover.gif");
			},
			function(){				
				$(this).children("img").attr("src",tmp);
			}
	 );
	 
	 $("p#control a#smallerfont").hover(
		
		function(){			
				tmp = $(this).children("img").attr("src");				
				$(this).children("img").attr("src",getpath(tmp) + "font-smaller-hover.gif");
			},
			function(){				
				$(this).children("img").attr("src",tmp);
			}
	 );
	/********* Expanding box ********/
	$("div.expandbox div.ebControl a").each( function(i){
		$(this).attr("href","#eb" + i )
	});
	
	$("div.expandbox div.ebContent").each( function(i){
		$(this).prepend('<a name="eb' + i + '"></a>');
	});
	
	$("div.expandbox div.ebControl a").click(function (){
		var obj = $(this).parents(".expandbox").children(".ebContent");
		
		if( obj.is(":hidden")){
			obj.slideDown("slow");
			$(this).text("HIDE MORE");
			$(this).removeClass("show");
			$(this).addClass("hide");
			
		}
		else{
			obj.slideUp("slow");
			$(this).text("SHOW MORE");
			$(this).removeClass("hide");
			$(this).addClass("show");
		}
		return false;
	});
	
	/**** Expand Code *****/
	$("div.expandbox_code div.ebControl_code a").each( function(i){
		$(this).attr("href","#eb" + i )
	});
	
	$("div.expandbox_code div.ebContent_code").each( function(i){
		$(this).prepend('<a name="eb' + i + '"></a>');
	});
	
	$("div.expandbox_code div.ebControl_code a").click(function (){
		var obj = $(this).parents(".expandbox_code").children(".ebContent_code");
		
		if( obj.is(":hidden")){
			obj.slideDown("slow");
			$(this).text("Hide code");
			$(this).removeClass("show");
			$(this).addClass("hide");
			
		}
		else{
			obj.slideUp("slow");
			$(this).text("Show code");
			$(this).removeClass("hide");
			$(this).addClass("show");
		}
		return false;
	});

	
	
	
	/******* Slide Board *******/
	$("div.slideBoard div.sbSlides div.sbSlide").each( function(i){
		$(this).prepend('<a name="sbSlide' + i + '"></a>');
	});
	$("div.slideBoard div.ctrlRight a").attr("href","#sbSlide" + 2 );
	
	$("div.slideBoard div.sbSlides div.sbSlide:first").show();
	var sbSize = $("div.slideBoard div.sbSlides div.sbSlide").size();
	var sbIndx = 1;
	
	
	if( sbIndx == 1){
		$("div.slideBoard div.ctrlLeft").hide();
	}
	
	
	$("div.slideBoard div.ctrlRight a").click(function (){
		$sbObj = $("div.slideBoard div.sbSlides div.sbSlide:visible");// .css("border", "1px solid red");				
		if( sbIndx < sbSize){		
			if( sbIndx == 1){
					$("div.slideBoard div.ctrlLeft").show();
			}
			$(this).attr("href", "#sbSlide" + (sbIndx+2));
			$("div.slideBoard div.ctrlLeft a").attr("href", "#sbSlide" + sbIndx);
			$sbObj.hide();			
			$sbObj.next().show();
			sbIndx++;
			if( sbIndx == sbSize){
					$("div.slideBoard div.ctrlRight").hide();
			}
		}
		return false;		
	});
	
	//Slideboard
		
	$("div.slideBoard div.ctrlLeft a").click(function (){
		$sbObj = $("div.slideBoard div.sbSlides div.sbSlide:visible");// .css("border", "1px solid red");				
		if( sbIndx > 1 ){			
			if( sbIndx == sbSize){
					$("div.slideBoard div.ctrlRight").show();
			}
			$(this).attr("href", "#sbSlide" + (sbIndx-2));
			$("div.slideBoard div.ctrlRight a").attr("href", "#sbSlide" + (sbIndx));
			$sbObj.hide();
			$sbObj.prev().show();
			sbIndx--;
			if( sbIndx == 1){
					$("div.slideBoard div.ctrlLeft").hide();
			}
		}
		return false;		
	});
	
	
	
	//$("div.vote div.vote_form form").clearForm();
	
	$("div.vote div.vote_form form input:radio").click( function() {
		
		if( $(this).attr("value") == "No" && $(this).is(":checked") ){
			$(this).parents("div.vote").children(".vote_feedback").slideDown();
		}		
		else{
			$(this).parents("div.vote").children(".vote_feedback").slideUp();
		}
	});
	
	
	
	
	// Home billboard
	
	$("ul.audience_list li:first").addClass("selected");	
	
	var img_src = $("ul.audience_list li.selected img").attr("src");
	var img_alt = $("ul.audience_list li.selected img").attr("alt");	
	
	$("div.audience div#audience_img").prepend('<img src="' + img_src + '" alt="' + img_alt + '" />')
	
	
	
	
	$("ul.audience_list").everyTime(5000, "nextTimer", 	function(){	  	
		var cur = $("ul.audience_list li.selected");
		var nxt = cur.next();
		if( nxt.length ){
			//cur.hide("slow");
			cur.removeClass("selected");
			//nxt.hide();
			nxt.addClass("selected");			
			//nxt.fadeIn(3000);
		}
		
		else{
			cur.removeClass("selected");
			nxt = $("ul.audience_list li:first");
			nxt.addClass("selected");
		}	

		
		$("div.audience div#audience_img").append('<img src="' + nxt.children("img").attr('src') + '" alt="' + nxt.children("img").attr('alt') + '" style="display:none" />')
		
		
		$("div.audience div#audience_img img:first").fadeTo("medium", .3, function(){			
			$("div.audience div#audience_img img:last").fadeIn(600,function(){
				$("div.audience div#audience_img img:first").remove();
				//$(obj).remove();
			});
						
		});
		
		
	});
	
	$("ul.audience_list li").mouseover(function(){
		$("ul.audience_list").stopTime();				
		
		$("ul.audience_list li.selected").removeClass("selected");
		$(this).addClass("selected");
		
		var tmp_obj = $("div.audience div#audience_img img:last");
		$tobj = this;
		tmp_obj.hide(10, function(){
			tmp_obj.attr("src", $($tobj).children("img").attr('src') );	
			tmp_obj.attr("alt", $($tobj).children("img").attr('alt') );	
			tmp_obj.fadeIn(200);
		});		
	});
	
	$("ul.audience_list li a").focus(function(){				
		var tobj = $(this).parent();
		
		$("ul.audience_list").stopTime();				
		
		$("ul.audience_list li.selected").removeClass("selected");		
		tobj.addClass("selected");	
		
		var tmp_obj = $("div.audience div#audience_img img:last");
		tmp_obj.hide(10, function(){			
			tmp_obj.attr("src", tobj.children("img").attr('src') );	
			tmp_obj.attr("alt", tobj.children("img").attr('alt') );	
			tmp_obj.fadeIn(200);			
			
		});
		
	});
	
	
	/* Information for you popup */
	
	if( ! $("div.information #topsubnav").length  ){
		$("div#topnav ul li.popup").hover(
				function(){								
					$(this).find(".pmenu").show();	
					$(this).addClass("hover");
					//alert(jQuery.isEmptyObject( $("div.information #topsubnav") ));
					//alert($("div.information #topsubnav").isEmptyObject() );			
					//alert('ok');
				},
				function(){				
					$(this).removeClass("hover");
					$(this).find(".pmenu").hide();
				}
			);
		}
		
		$("div#topnav ul li.popup a").focus(function(){
			$("div#topnav ul li.popup").stopTime();			
			$(this).parents(".smenu").children(".pmenu").show();			
			//$("p#debug_display").append($(this).html());
		});
		
		$("div#topnav ul li.popup a").blur(function(){
			 var tmp = $(this).parents(".smenu").children(".pmenu");
			$("div#topnav ul li.popup").everyTime(5, "popupTimer", 	function(){
				tmp.hide();
			});
			
			//$(this).parents(".smenu").children(".pmenu").hide();
			
		});
		
		$("div#topnav ul li.popup a.popupa").click(function(){
			return false;
		});
		
		// Back to portal
		if( $.cookie('returnurl') ){
			
			$("div.default_audience").replaceWith("<div class='default_audience'><h3>Go back to your coverage options</h3><p>Review the results from your options search.</p>" + 
			"<p class='returnimg'><a href='" + $.cookie('returnurl') + "'><img alt='Return to my options' src='" +
			rxs_navbase + "/images/sys_images/retrn-btn.gif'></a></p>" +
			"<p class='returnlink'>or <a href='http://finder.healthcare.gov'>Start Over</a></p></div>");
		}
		//setEqualHeight("default");
		
		setEqualHeight();
		
		//$("dov.var6")
		
		
	
	//$("p#debug_display").html( nxt.css("background-image") );
	/*
	function nextAudience(){		
		var cur = $("ul.audience_list li.selected");
		var nxt = cur.next();
		
		if(nxt.html()){
			cur.removeClass("selected");
			nxt.addClass("selected");
		}
		else{
			cur.removeClass("selected");
			$("ul.audience_list li:first).addClass("selected");
		}
	}
	*/
	
	/**********************************************************************/
	/*
	$("div.syndicate a").each(function(){
		
		if($(this).attr("name").length){						
			$(this).addClass("anchor");
		}
		
	});
	*/
	
	
	
});


$(window).load(function(){
		setEqualHeight("default");
		setEqualHeight();
	});


function setEqualHeight(){	
	if( ! $("div#timeline-fullcontent").length){
		if(arguments.length == 1){		
			equalHeightReset($("div.var6"));		
			equalHeightReset($("div.two_columns"));	
		}
		else{
			equalHeight($("div.var6"));
			equalHeight($("div.two_columns"));	
		}
	}
}

function equalHeight(group) {
	var tallest = 0;
	group.each(function() {
		var thisHeight = $(this).height();
		if(thisHeight > tallest) {
			tallest = thisHeight;
		}
	});
	group.height(tallest);
}


function equalHeightReset(group){		
	group.height("auto");
}

function removeExt(str){
	var indx = str.lastIndexOf(".");
	if(indx != -1){
		
		str = str.substring(0,indx);
		return str;
	}
	else{
		return str;
	}
}

function removeExt(str,str2){
	var indx = str.lastIndexOf(str2);
	if(indx == -1)
		indx = str.lastIndexOf(".")
	if(indx != -1){
		
		str = str.substring(0,indx);
		return str;
	}
	else{
		return str;
	}
}
function getExt(str){
	var ext="";
	var indx = str.lastIndexOf(".");
	if(indx != -1){
		ext=str.substring(indx);
	}
	return ext;
}
function getpath(str){
	var p="";
	var indx = str.lastIndexOf("/");
	if(indx != -1){
		p=str.substring(0,indx+1);
	}
	return p;

}