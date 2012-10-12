/* Timeline Javascript File for Healthcare.gov
US Department of Health and Human Services */
var cssTest;

$(document).ready(function() {
    if (document.getElementById("timeline-fullcontent") == null) {
        return;
    }
    
    var totalWidth = 980;
    var timelineWidth;
    var eventWidth = 18;
    var eventTotal = 0;
    var yearBarWidth = 2;
    var currLeft = 0;
    var currYear = 0;
    var currliID = 0;
    var yearCount = 0;

    //check if CSS is enabled or not if not, don't run timeline javascripts
    cssTest = $('#timeline-outer').css('background-image');

    if (cssTest != 'none') {
        totalWidth = document.getElementById("timeline-fullcontent").offsetWidth;
        timelineWidth = document.getElementById("timeline-outer").offsetWidth;

        eventTotal = $('#eventslist li').size();
        $('#timeline-content').css("width", totalWidth * $('#eventslist li').size());

        if (eventTotal > 0)
            eventWidth = (timelineWidth - 5) / eventTotal;

        $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-left");

        $('#eventslist li').each(function(index) {
            $(this).css("left", currLeft);
            $(this).css("width", eventWidth);

            currliID = $(this).attr('id');
            var nextYear = currliID.split("_")[1];

            if (nextYear > currYear) {
                $(this).css("border-left", "solid 2px #a87529");
                currYear = nextYear;

                //alert(index * eventWidth);
                var currHTML = $('#timeline-years').html();
                currHTML += "<div class=\"timeline-oneyear\" style=\"left: " + index * eventWidth + "px \">"
                    + currYear
                    + "</div>";
                $('#timeline-years').html(currHTML);

                var barLeft = (index * eventWidth) - (yearCount * yearBarWidth);
                if (($.browser.msie) && ($.browser.version == 8) && (yearCount > 0))
                    barLeft--;

                currHTML = "";
                currHTML = $('#timeline-years-bar').html();
                currHTML += "<div class=\"timeline-onebar\" style=\"left: " + barLeft + "px; width: 0px; \"></div>";
                $('#timeline-years-bar').html(currHTML);

                yearCount++;
            }
            $(this).attr("id", currliID.split("_")[0]);

            $(this).focusin(function() {
                var objTop = $(this).offset().top;
                var objLeft = $(this).offset().left;

                var idxYear = $(this).text().indexOf('--');
                if (idxYear > 0)
                    $('#event-popup #event-popup-middle #event-popup-text').text($(this).text().substring(0, idxYear));
                else
                    $('#event-popup #event-popup-middle #event-popup-text').text($(this).text());
                var eventPopHeight = $('#event-popup').height();
                var eventPopWidth = $('#event-popup').css('width').replace("px", "");

                var popOffset = ((totalWidth - timelineWidth) / 2) - 30;
                $('#event-popup').css('margin-left', ((index * eventWidth) + popOffset + (eventWidth / 2)) + 'px');
                $('#event-popup').css('top', (50 - eventPopHeight) + 'px');

                var badBrowser = (navigator.appName == "Microsoft Internet Explorer" && navigator.platform == "Win32");
                if (badBrowser) {
                    $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-left-gif");
                    $('#event-popup #event-popup-middle').addClass("event-popup-middle-left-gif");
                    $('#event-popup #event-popup-top').addClass("event-popup-top-left-gif");
                    if ($(this).css('left').replace("px", "") >= (timelineWidth / 2)) {
                        $('#event-popup #event-popup-bottom').removeClass("event-popup-bottom-left-gif");
                        $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-right-gif");

                        $('#event-popup #event-popup-middle').removeClass("event-popup-middle-left-gif");
                        $('#event-popup #event-popup-middle').addClass("event-popup-middle-right-gif");

                        $('#event-popup #event-popup-top').removeClass("event-popup-top-left-gif");
                        $('#event-popup #event-popup-top').addClass("event-popup-top-right-gif");

                        $('#event-popup').css('margin-left', ((index * eventWidth) + popOffset + (eventWidth / 2) - 185) + 'px');
                    }
                } else {
                    $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-left");
                    $('#event-popup #event-popup-middle').addClass("event-popup-middle-left");
                    $('#event-popup #event-popup-top').addClass("event-popup-top-left");
                    if ($(this).css('left').replace("px", "") >= (timelineWidth / 2)) {
                        $('#event-popup #event-popup-bottom').removeClass("event-popup-bottom-left");
                        $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-right");

                        $('#event-popup #event-popup-middle').removeClass("event-popup-middle-left");
                        $('#event-popup #event-popup-middle').addClass("event-popup-middle-right");

                        $('#event-popup #event-popup-top').removeClass("event-popup-top-left");
                        $('#event-popup #event-popup-top').addClass("event-popup-top-right");

                        $('#event-popup').css('margin-left', ((index * eventWidth) + popOffset + (eventWidth / 2) - 185) + 'px');
                    }
                }

                $('#event-popup').stop(true, true).animate({ opacity: "show" }, "fast");

                //$(this).css('background-image', 'url(./images/timeline-blue-hover-bg2.gif)');
                $(this).addClass("timeline-event-hover");
            });

            $(this).focusout(function() {
                $('#event-popup').animate({ opacity: "hide" }, "slow");
                //$(this).css('background-image', '');
                $(this).removeClass("timeline-event-hover");
            });

            $(this).hover(function() {
                var objTop = $(this).offset().top;
                var objLeft = $(this).offset().left;

                var idxYear = $(this).text().indexOf('--');
                if (idxYear > 0)
                    $('#event-popup #event-popup-middle #event-popup-text').text($(this).text().substring(0, idxYear));
                else
                    $('#event-popup #event-popup-middle #event-popup-text').text($(this).text());
                var eventPopHeight = $('#event-popup').height();
                var eventPopWidth = $('#event-popup').css('width').replace("px", "");

                var popOffset = ((totalWidth - timelineWidth) / 2) - 30;
                $('#event-popup').css('margin-left', ((index * eventWidth) + popOffset + (eventWidth / 2)) + 'px');
                $('#event-popup').css('top', (50 - eventPopHeight) + 'px');

                var badBrowser = (navigator.appName == "Microsoft Internet Explorer" && navigator.platform == "Win32");
                if (badBrowser) {
                    $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-left-gif");
                    $('#event-popup #event-popup-middle').addClass("event-popup-middle-left-gif");
                    $('#event-popup #event-popup-top').addClass("event-popup-top-left-gif");
                    if ($(this).css('left').replace("px", "") >= (timelineWidth / 2)) {
                        $('#event-popup #event-popup-bottom').removeClass("event-popup-bottom-left-gif");
                        $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-right-gif");

                        $('#event-popup #event-popup-middle').removeClass("event-popup-middle-left-gif");
                        $('#event-popup #event-popup-middle').addClass("event-popup-middle-right-gif");

                        $('#event-popup #event-popup-top').removeClass("event-popup-top-left-gif");
                        $('#event-popup #event-popup-top').addClass("event-popup-top-right-gif");

                        $('#event-popup').css('margin-left', ((index * eventWidth) + popOffset + (eventWidth / 2) - 185) + 'px');
                    }
                } else {
                    $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-left");
                    $('#event-popup #event-popup-middle').addClass("event-popup-middle-left");
                    $('#event-popup #event-popup-top').addClass("event-popup-top-left");
                    if ($(this).css('left').replace("px", "") >= (timelineWidth / 2)) {
                        $('#event-popup #event-popup-bottom').removeClass("event-popup-bottom-left");
                        $('#event-popup #event-popup-bottom').addClass("event-popup-bottom-right");

                        $('#event-popup #event-popup-middle').removeClass("event-popup-middle-left");
                        $('#event-popup #event-popup-middle').addClass("event-popup-middle-right");

                        $('#event-popup #event-popup-top').removeClass("event-popup-top-left");
                        $('#event-popup #event-popup-top').addClass("event-popup-top-right");

                        $('#event-popup').css('margin-left', ((index * eventWidth) + popOffset + (eventWidth / 2) - 185) + 'px');
                    }
                }

                $('#event-popup').stop(true, true).animate({ opacity: "show" }, "fast");
            }, function() {
                $('#event-popup').animate({ opacity: "hide" }, "slow");
                $(this).css('background-image', '');

                //$('#event-popup').animate({ opacity: "hide", top: (objTop - divHeight - 5) }, "slow");
            });
            currLeft += eventWidth;
        });

        /*eventWidth = eventWidth - (yearCount * yearBarWidth);
        $('#eventslist li').each(function(index) {
        var currLeft = $(this).css("left").replace("px", "");
        $(this).css("left", currLeft - (yearCount * yearBarWidth));
        $(this).css("width", eventWidth);
        });*/

        //To make it work with JQUERY < 1.4
        var divCount = 0;
        var maxDivHeight = 0;
        $('#timeline-content div').each(function(index) {
            if (divCount == 0) {
                $(this).css('margin-left', '45px');
                divCount = 1;
            }
            if ($(this).hasClass('section')) {
                if ($(this).height() > maxDivHeight)
                    maxDivHeight = $(this).height();

                /*var sec = $(this);
                sec.find("a").focus(function() {
                alert('focus');
                ScrollSection(sec.attr('id'), 'scroller', 'event1-pane');
                });*/

                $(this).html("<div class=\"section-top\"></div><div class=\"section-middle\">" + $(this).html() + "</div><div class=\"section-bottom\"></div>");
            }
        });
        if( ! jQuery.browser.safari)        {
				
				
        $('#timeline-content div').each(function(index) {
            if ($(this).hasClass('section-middle')) {
            	  
                $(this).css('height', maxDivHeight + 'px');
            }
        });
			}

        //Works with JQUERY 1.4+
        $('#timeline-content div').each(function(index) {
            if ($(this).hasClass('section'))
                $(this).focusin(function() {
                    //alert($(this).attr('id'));
                    ScrollSection($(this).attr('id'), 'scroller', 'event1-pane');
                });
        });

        if (window.location.hash) {
            ScrollSection(window.location.hash.replace("#", ""), 'scroller', 'event1-pane');
        }

        sectionTab = currentSection.split("-")[0] + tabTag;
        UpdateMarker();
    }
});

function UpdateMarker() {
    var markW = $('#timeline-marker').css("width").replace("px", "");
    $('#timeline-marker').css("margin-left", (document.getElementById(sectionTab).offsetLeft + (document.getElementById(sectionTab).offsetWidth/2) - (markW/2))  + "px");
}