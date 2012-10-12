/**
 * Copyright 2004 MedCommons Inc.   All Rights Reserved.
 */
var ThumbOff = new Image();
var ThumbOn = new Image();

ThumbOff.src = "images/thumbnail1_off.gif";
ThumbOn.src = "images/thumbnail1_on.gif";

function ThumbClick(numImage) {
	
	if (document.images["thumb" + numImage].src==ThumbOff.src) {
		
		document.images["thumb" + numImage].src = ThumbOn.src
		
	}
	else {
		
		document.images["thumb" + numImage].src = ThumbOff.src
		
	}
}
	