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
function FindWindowSize(){
if ((screen.width) > (screen.height)) {
	return 'l';
}
else {
	return 'p';
}
}

function NewWindow(mypage,myname,w,h,scroll){
LeftPosition = (screen.width) ? (screen.width-w)/2 : 0;
TopPosition = (screen.height) ? (screen.height-h)/2 : 0;
settings =
'height='+h+',width='+w+',top='+TopPosition+',left='+LeftPosition+',scrollbars='+scroll+',resizable'
win = window.open(mypage,myname,settings)
if(!win==""){win.window.focus();}
}

function SendOrderForm() {
	
	location.href="mailto:" + document.forms["form1"].copyto.value + "?subject=MEDCOMMONS Report Tracking Number " + document.forms["form1"].tracking.value + "&from=" + document.forms["form1"].name.value + "&body=Please check the following URL for image: " + window.document.URL;
	
}

function DropDownAction(mySel) {

	myVal = mySel[mySel.selectedIndex].value
	
if (myVal==1) {
	
	alert("Version Number");
}
else if (myVal==2) {
	
	NewWindow('http://www.autocyt.com/mc/manual.php', 'Help', 620, 350, 'yes')
	
}

}

	