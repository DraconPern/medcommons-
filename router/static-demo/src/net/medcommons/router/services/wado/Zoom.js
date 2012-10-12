var startX = 0;
var startY = 0;
var endX = 0;
var endY = 0;
var imageHeight = 0;
var imageWidth=0;
var imageOffsetX = 0;
var imageOffsetY = 0;
/**
 * Zoom.js
 * To do: coordinates aren't quite right (window vs. image). 
 * 
 **/
function beginZoomDrag(elementToZoom, event) {

	if (!event) event= window.event; // IE
    startX = event.clientX;
    startY = event.clientY;

    imageHeight = document.image.height;
    imageWidth = document.image.width;
    

    // Add event handlers (removed in upHandler)
   if (document.addEventListener) { // DOM Level 2 Event Model
    	document.addEventListener("mousemove", trackRegionHandler, true);
    	document.addEventListener("mouseup", upHandler, true);
    }
    else if (document.attachEvent){
    	document.attachEvent("onmousemove", trackRegionHandler);
    	document.attachEvent("onmouseup", upHandler);
    }
  
    // We've handled this event.  Don't let anybody else see it.
    if (event.stopPropagation)
		event.stopPropagation(); // DOM Level 2
   	else
   		event.cancelBubble = true; // IE
   		
   	if (event.preventDefault)
   		event.preventDefault(); // DOM Level 2
   	else
   		event.returnValue = false; // IE

    /**
     * This is the handler that captures mousemove events which
     * generate the endpoint of the region.
     **/
    function trackRegionHandler(event) {

	if (!event) event= window.event; // IE
	endX = event.clientX;
	endY = event.clientY;
	 // We've handled this event.  Don't let anybody else see it.
    if (event.stopPropagation)
		event.stopPropagation(); // DOM Level 2
   	else
   		event.cancelBubble = true; // IE
   		
   	if (event.preventDefault)
   		event.preventDefault(); // DOM Level 2
   	else
   		event.returnValue = false; // IE
	window.status="X=" + endX + ", Y=" + endY;

	
    }

    /**
     * This is the handler that captures the final mouseup event that
     * occurs at the end of a drag.
     **/
    function upHandler(event) {
    if (!event) event= window.event; // IE
   // alert("upHandler");
	// Unregister the capturing event handlers
	if (document.removeEventListener){ // DOM Level 2
		document.removeEventListener("mouseup", upHandler, true);
		document.removeEventListener("mousemove", trackRegionHandler, true);
	} 
	// IE Bug: handlers not yet removed.
	else if (document.detachEvent){
		window.status="Detach";
		document.detachEvent("onmouseup", upHandler);
		document.detachEvent("onmousemove", trackRegionHandler);
	}
	// And don't let the event propagate any further
	 if (event.stopPropagation)
		event.stopPropagation(); // DOM Level 2
   	else
   		event.cancelBubble = true; // IE
	
	//alert("region:" + startX + "," + startY + "," + endX + "," + endY);
	
	parent.control.handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight);
    }
    
}
