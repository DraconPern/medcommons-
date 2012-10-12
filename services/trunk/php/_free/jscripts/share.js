$(document).ready(function () {	
	//$("ul.toolbox_body ul").toggle();
	
	$("ul.toolbox_body li.share").hover(
		function() {
			
			$(this).children("ul").show();		
		},
		function(){
			$(this).children("ul").hide();			
		}
	);
	
	var isInBookmark = false;
	$("ul.toolbox_body li.share a").bind("focus", function() {			
		tmp = $(this).parent();		
		tmp.children("ul").show();			
	});
	
	$("ul.toolbox_body li.share").each( function(){
		
		$(this).children("a:first").bind("blur", function() {				
			if(isInBookmark){
				$(this).parents("li.share").children("ul").hide();		
				isInBookmark = false;	
			}
		});
	});
		
	
	$("ul.toolbox_body li.share ul a ").bind("focus", function() {			
		isInBookmark = true;		
	});
	
	
	
	$("ul.toolbox_body li.share").each(function(){
		$(this).find("ul li a:last").bind("blur", function() {
			$(this).parents("li.share").children("ul").hide();	
			isInBookmark = false;			
		});
	});
	
	
});


//******************************************************************************************//
//* A function that opens the selected bookmark/share website and passes the URL and title
//* using the API for that site.
//******************************************************************************************//
function openBookmarkSite(sitename) {
	
	var title;
	var url;
	if (sitename.toLowerCase() == "favorites") {		
		title = document.title;
		url = location.href;		
		if (url.endsWith("#")) {
			url = url.substring(0, url.length - 1);
		}		
		
		if (window.sidebar) { // firefox
			window.sidebar.addPanel(title, url, "");
		} else if (document.all) { // IE
			external.AddFavorite(url, title);
		} else if (window.opera && window.print) { // opera
			var elem = document.createElement('a');
			elem.setAttribute('href', url);
			elem.setAttribute('title', title);
			elem.setAttribute('rel', 'sidebar');
			elem.click();
		}
	}
	else
	{
		title = escape(document.title);
		url = escape(location.href);
		if (url.endsWith("#")) {
			url = url.substring(0, url.length - 1);
		}		
		var bookmarkURL;
		if (sitename.toLowerCase() == "delicious") {
			bookmarkURL = "http://delicious.com/save?url=" + url + "&title=" + title;
		}
		else if (sitename.toLowerCase() == "digg") {
			bookmarkURL = "http://digg.com/submit?url=" + url + "&title=" + title + "&media=news";
		}	
		else if (sitename.toLowerCase() == "facebook") {
			bookmarkURL = "http://www.facebook.com/sharer.php?u=" + url + "&t=" + title;
		}	
		else if (sitename.toLowerCase() == "google") {
			bookmarkURL = "http://www.google.com/bookmarks/mark?op=add&bkmk=" + url + "&title=" + title;
		}
		else if (sitename.toLowerCase() == "yahoo-myweb") {
			bookmarkURL = "http://myweb.yahoo.com/myresults/bookmarklet?&ei=UTF-8&u=" + url + "&t=" + title;
		}
		else if (sitename.toLowerCase() == "technorati") {
			bookmarkURL = "http://technorati.com/faves?sub=favthis&add=" + url;
		}
		cdcShareWindowObjectReference = window.open(bookmarkURL, 
			"_blank",  "height=500,width=780,status=yes,toolbar=no,menubar=no,location=no,scrollbars=yes,resizable=yes");
	}
	window.location.reload(true);
	cancel(this);
}
