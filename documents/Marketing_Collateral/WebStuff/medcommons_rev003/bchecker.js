// JavaScript Document
browsername=navigator.appName;
if (browsername.indexOf("Netscape")!=-1) {browsername="NS"}
else
{if (browsername.indexOf("Microsoft")!=-1) {browsername="IE"}
else {browsername="NA"}};
browserversion="0";
if (navigator.appVersion.indexOf("2.")!=-1) {browserversion="2"};
if (navigator.appVersion.indexOf("3.")!=-1) {browserversion="3"};
if (navigator.appVersion.indexOf("4.")!=-1) {browserversion="4"};
if (navigator.appVersion.indexOf("5.")!=-1) {browserversion="5"};
if (navigator.appVersion.indexOf("6.")!=-1) {browserversion="6"}; 
if ((browsername=="NS" && browserversion<5) || (browsername=="IE" && browserversion<6) || (browsername=="NA")) {
	alert("please note: This browser is not supported by MedCommons. MedCommons supports Netscape Mozilla 5.0 and above and MSIE 6.0 and above.");
}
var cookieEnabled=(navigator.cookieEnabled)? true : false
if (typeof navigator.cookieEnabled=="undefined" && !cookieEnabled){ 
document.cookie="testcookie"
cookieEnabled=(document.cookie.indexOf("testcookie")!=-1)? true : false
}
if (cookieEnabled==false) {
	alert("please note: Cookies are disabled in your browser. You must enable cookies to properly access and use MedCommons.");
}