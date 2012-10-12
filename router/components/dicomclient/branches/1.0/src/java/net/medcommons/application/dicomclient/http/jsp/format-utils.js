dojo.require("dojo.i18n.number");

var locale='en-us';
var decimalFlags = new Object();
decimalFlags.places = 1;
decimalFlags.round = true;
var integerFlags = new Object();
integerFlags.places = 0;
integerFlags.round = true;
    
function formatTotalBytes(totalBytes){
   	var mBytes = totalBytes /(1024.0 * 1024.0);
   		
   	return(dojo.i18n.number.format(mBytes, decimalFlags));
}
function formatPercentDone(bytesTransferred, totalBytes){
    var percent;
	if (totalBytes > 0){
	    percent = (bytesTransferred/totalBytes) * 100.0;
	    if (percent > 100.0){
	    	percent=100.0;
	    }
    }
    else{
    	percent = 0.0;
    }
    return(dojo.i18n.number.format(percent, decimalFlags));
}


function formatElapsedTime(elapsedTime){
    var formattedTime;
    var totalSeconds = elapsedTime / 1000;
    var seconds = totalSeconds % 60;
    var minutes = totalSeconds/60;
    var formattedSeconds = dojo.i18n.number.format(seconds, integerFlags) + " seconds";
    if (minutes < 1.0)
    	formattedTime = formattedSeconds;
	else{
		var formattedMinutes = dojo.i18n.number.format(minutes, integerFlags) + " minutes";
		formattedTime = formattedMinutes + ": " + formattedSeconds;
	}
	return(formattedTime);
}