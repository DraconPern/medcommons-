var currentSection = "event1-pane"; // The default loaded section on the page
var tabTag = "-button";
var paneTag = "-pane";

// Scroll the page manually to the position of element "link", passed to us.

function ScrollSection(link, scrollArea, offset)
{
    //alert(link + ' ' + scrollArea + ' ' + offset);
	// Store the last section, and update the current section

	if (currentSection == link) {
		return;
	}
	lastSection = currentSection;
	currentSection = link;
	
	// Change the section highlight.
	// Extract the root section name, and use that to change the background image to 'top', revealing the alt. state

	sectionTab = currentSection.split("-")[0] + tabTag;
	document.getElementById(sectionTab).className = "timeline-event-active";

	UpdateMarker();

    
    if (lastSection) {
	    lastTab = lastSection.split("-")[0] + tabTag;
	    document.getElementById(lastTab).className = "timeline-event-inactive";
	}
    
	// Get the element we want to scroll, get the position of the element to scroll to
	
	theScroll = document.getElementById(scrollArea);
	position = findElementPos(document.getElementById(link));

	// Get the position of the offset div -- the div at the far left.
	// This is the amount we compensate for when scrolling
	
	if (offset != "") {
		offsetPos = findElementPos(document.getElementById(offset));
		position[0] = position[0] - offsetPos[0];
	}

	scrollStart(theScroll, theScroll.scrollLeft, position[0], "horiz");

    // if no css follow anchor link, else don't
	if (cssTest == 'none')
	    return true;
	else
	    return false;
}

// Scroll the page using the arrows

function ScrollArrow(direction, toolbar, scrollArea, offset) {

	toolbarElem = document.getElementById(toolbar);
	toolbarNames = new Array();
    
	// Find all the <li> elements in the toolbar, and extract their id's into an array.
    
	if (toolbarElem.hasChildNodes())
	{
		var children = toolbarElem.childNodes;
		for (var i = 0; i < children.length; i++) 
		{
		    if (toolbarElem.childNodes[i].tagName == "LI") {
		        toolbarNames.push(toolbarElem.childNodes[i].id.split("-")[0]);
			}
		}
	}

	// Now iterate through our array of tab names, find matches, and determine where to go.

	for (var i = 0; i < toolbarNames.length; i++) {
		if (toolbarNames[i] == currentSection.split("-")[0]) {
			if (direction == "left") {
				if (i - 1 < 0) {
					gotoTab = toolbarNames[toolbarNames.length - 1];
				} else {
					gotoTab = toolbarNames[i - 1];
				}
			} else {
				if ((i + 1) > (toolbarNames.length - 1)) {
					gotoTab = toolbarNames[0];
				} else {
					gotoTab = toolbarNames[i + 1];
				}
			}
		}
	}
	
	// Go to the section name!
	
	ScrollSection(gotoTab+paneTag, scrollArea, offset);

}

//
// Animated Scroll Functions
// Scrolls are synchronous -- only one at a time.
//

var scrollanim = {time:0, begin:0, change:0.0, duration:0.0, element:null, timer:null};

function scrollStart(elem, start, end, direction)
{
    //alert("scrollStart from " + start + " to " + end + " in direction " + direction);
	//console.log("scrollStart from "+start+" to "+end+" in direction "+direction);

	if (scrollanim.timer != null) {
		clearInterval(scrollanim.timer);
		scrollanim.timer = null;
	}
	scrollanim.time = 0;
	scrollanim.begin = start;
	scrollanim.change = end - start;
	scrollanim.duration = 25;
	scrollanim.element = elem;
	
	if (direction == "horiz") {
		scrollanim.timer = setInterval("scrollHorizAnim();", 15);
	}
	else {
		scrollanim.timer = setInterval("scrollVertAnim();", 15);
	}
}

function scrollVertAnim()
{
	if (scrollanim.time > scrollanim.duration) {
		clearInterval(scrollanim.timer);
		scrollanim.timer = null;
	}
	else {
		move = sineInOut(scrollanim.time, scrollanim.begin, scrollanim.change, scrollanim.duration);
		scrollanim.element.scrollTop = move; 
		scrollanim.time++;
	}
}

function scrollHorizAnim()
{
	if (scrollanim.time > scrollanim.duration) {
		clearInterval(scrollanim.timer);
		scrollanim.timer = null;
	}
	else {
		move = sineInOut(scrollanim.time, scrollanim.begin, scrollanim.change, scrollanim.duration);
		scrollanim.element.scrollLeft = move;
		scrollanim.time++;
	}
}

//
// MOVE: Animate the move of an element.
//
// Move is also synchronous. One at a time, please.
//

var moveanim = {time:0, beginX:0, changeX:0.0, beginY:0, changeY:0, duration:0.0, element:null, timer:null};

function moveStart(elem, startX, endX, startY, endY, duration)
{
	if (moveanim.timer != null) {
		clearInterval(moveanim.timer);
		moveanim.timer = null;
	}
	moveanim.time = 0;
	moveanim.beginX = startX;
	moveanim.changeX = endX - startX;
	moveanim.beginY = startY;
	moveanim.changeY = endY - startY;
	moveanim.duration = duration;
	moveanim.element = elem;

	moveanim.timer = setInterval("moveAnimDo();", 15);
}

function moveAnimDo()
{
	if (moveanim.time > moveanim.duration) {
		clearInterval(moveanim.timer);
		moveanim.timer = null;
	}
	else {
		moveX = cubicOut(moveanim.time, moveanim.beginX, moveanim.changeX, moveanim.duration);
		moveY = cubicOut(moveanim.time, moveanim.beginY, moveanim.changeY, moveanim.duration);
		moveanim.element.style.left = moveX + "px";
		moveanim.element.style.top = moveY + "px";
		moveanim.time++;
	}
}


//console.log("Initialized");


// Utility: Math functions for animation calucations - From http://www.robertpenner.com/easing/
//
// t = time, b = begin, c = change, d = duration
// time = current frame, begin is fixed, change is basically finish - begin, duration is fixed (frames),

function linear(t, b, c, d) {
    return c * t / d + b;
}

function sineInOut(t, b, c, d) {
    return -c / 2 * (Math.cos(Math.PI * t / d) - 1) + b;
}

function cubicIn(t, b, c, d) {
    return c * (t /= d) * t * t + b;
}

function cubicOut(t, b, c, d) {
    return c * ((t = t / d - 1) * t * t + 1) + b;
}

function cubicInOut(t, b, c, d) {
    if ((t /= d / 2) < 1) return c / 2 * t * t * t + b;
    return c / 2 * ((t -= 2) * t * t + 2) + b;
}

function bounceOut(t, b, c, d) {
    if ((t /= d) < (1 / 2.75)) {
        return c * (7.5625 * t * t) + b;
    } else if (t < (2 / 2.75)) {
        return c * (7.5625 * (t -= (1.5 / 2.75)) * t + .75) + b;
    } else if (t < (2.5 / 2.75)) {
        return c * (7.5625 * (t -= (2.25 / 2.75)) * t + .9375) + b;
    } else {
        return c * (7.5625 * (t -= (2.625 / 2.75)) * t + .984375) + b;
    }
}


// Utility: Get the size of the window, and set myWidth and myHeight
// Credit to quirksmode.org

function getSize() {

    // Window Size

    if (self.innerHeight) { // Everyone but IE
        myWidth = window.innerWidth;
        myHeight = window.innerHeight;
        myScroll = window.pageYOffset;
    } else if (document.documentElement && document.documentElement.clientHeight) { // IE6 Strict
        myWidth = document.documentElement.clientWidth;
        myHeight = document.documentElement.clientHeight;
        myScroll = document.documentElement.scrollTop;
    } else if (document.body) { // Other IE, such as IE7
        myWidth = document.body.clientWidth;
        myHeight = document.body.clientHeight;
        myScroll = document.body.scrollTop;
    }

    // Page size w/offscreen areas

    if (window.innerHeight && window.scrollMaxY) {
        myScrollWidth = document.body.scrollWidth;
        myScrollHeight = window.innerHeight + window.scrollMaxY;
    } else if (document.body.scrollHeight > document.body.offsetHeight) { // All but Explorer Mac
        myScrollWidth = document.body.scrollWidth;
        myScrollHeight = document.body.scrollHeight;
    } else { // Explorer Mac...would also work in Explorer 6 Strict, Mozilla and Safari
        myScrollWidth = document.body.offsetWidth;
        myScrollHeight = document.body.offsetHeight;
    }
}

// Utility: Get Shift Key Status
// IE events don't seem to get passed through the function, so grab it from the window.

function getShift(evt) {
    var shift = false;
    if (!evt && window.event) {
        shift = window.event.shiftKey;
    } else if (evt) {
        shift = evt.shiftKey;
        if (shift) evt.stopPropagation(); // Prevents Firefox from doing shifty things
    }
    return shift;
}

// Utility: Find the Y position of an element on a page. Return Y and X as an array

function findElementPos(elemFind) {
    var elemX = 0;
    var elemY = 0;
    do {
        elemX += elemFind.offsetLeft;
        elemY += elemFind.offsetTop;
    } while (elemFind = elemFind.offsetParent)

    return Array(elemX, elemY);
}