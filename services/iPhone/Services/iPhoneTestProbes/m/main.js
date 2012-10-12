

//
// This template also demonstrates how to make use of client-side database storage to store content that can be used, whether the application is online or offline.
// The database has one trivial table: a simple key-value table. You can imagine yourself having different tables with different columns, if you are familiar with relational database concepts.
//
// On devices that doesn't have the local database feature, the settings will not be remembered.
//
var ctx = new Object  ; // email and password are stashed here

var database = null;                            // The client-side database
var DB_tableName = "MCPropertiesTable";         // database name
var originalSettings = {};                      // in case there is no client side database


//
// Function: updateColorChip()
// Update the settings UI so that the correct color chip is selected
//
// color: the new color
//
function updateColorChip(color)
{
    var checkMark = document.getElementById('checkMark');
    if (checkMark) {
        var colorChip = document.getElementById(color+'ColorChip');
        if (colorChip) {
            setTimeout(function() {
                checkMark.style.left = document.defaultView.getComputedStyle(colorChip, null).getPropertyValue('left');
            }, 0);
            document.getElementById('message').style.color = color;
        }
    }
}

//
// Function: updateSelectValue(selectElement, value)
// Update the settings UI so that the right popup value is selected
//
// selectElement: the element with the popup
// value: the new value
//
function updateSelectValue(selectElement, value)
{
    var options = selectElement.options;
    var i = 0;
    for (; i < options.length; i++) {
        if (options.item(i).value == value) break;
    }
    if (i < options.length) {
        selectElement.selectedIndex = i;
    }
}

//
// Function: getFontSettingsFromElement(element)
// Get font family and size of an element
//
function getFontSettingsFromElement(element)
{
    var computedStyle = document.defaultView.getComputedStyle(element, null);
    var returnValue = {};
    
    returnValue.fontFamily = computedStyle.getPropertyValue("font-family");
    // Simplistic matching of font names like 'Marker Felt'
    try {
        if (returnValue.fontFamily.charAt(0) == "'") {
            returnValue.fontFamily = returnValue.fontFamily.substring(1, returnValue.fontFamily.length-1);
        }
    }
    catch (e) {}
    
    returnValue.fontSize = computedStyle.getPropertyValue("font-size");
    
    return returnValue;
}
var mc = {
    log :  function (m) { 
    //debug.log (m); 
    console.log (m); 
    } };

//
// Function: load()
// Called by HTML body element's onload event when the web application is ready to start
//
function load()
{
    var element = document.getElementById('message');
    if (element) {
        originalSettings.message = element.value;
        originalSettings.color = 'black'; // We only have a limited set of color chips, so use 'black' here.
        var fontSettings = getFontSettingsFromElement(element);
        originalSettings.fontFamily = fontSettings.fontFamily;
        originalSettings.fontSize = fontSettings.fontSize;
        originalSettings.email = '';
        originalSettings.password = '';
        element.value = '';
    }
    
    dashcode.setupParts();
    
    initDB();
    if (!database) {
        element.value = originalSettings.message;
        console.log ("No sqlite database reverting to original settings");
    }
    

}
function ctxToString ()
{

buf = "email: "+ctx.email+"password: "+ctx.password+"accid: "+ctx.accid+"practiceid: "+
            ctx.practiceid + "practicename: "+ctx.practicename+"";
            
return buf();
}
function initCtx()
{
    ctx.email = null;
    ctx.password=null;
    ctx.accid = null;
    ctx.practiceid=null;
    ctx.practicename=null;
    ctx.providerid=null;
    ctx.providername=null;
    ctx.patientid=null;
    ctx.patientname=null;
    ctx.patientappliance=null;
}


var url;
var appliance;
function mc_rest(s) { 
 rets = appliance+'/probe/'+s; 
 console.log ("connection point:  "+ rets);
 return rets;
 } //phonegap needs the whole deal
function customSetup (whichappliance){
    appliance = whichappliance; // remember globally
    url =  appliance + '/probe/ws/pushdata.php';  

    load(); // original initialization
    initCtx();  // clear context areas    
    document.getElementById('logo').src=appliance+ '/probe/custom/logo60by60.png';    
    document.getElementById('password').type = 'password';
    reloadDBandFlip();

    //flipToFront(event);  
    return true;    
}

function addNewContactButtonClicked(event)
{
    // Insert Code Here
    addContactGui() ;
}

function registerChosenContactButtonClicked(event)
{
    // Insert Code Here
    mcChooseContactRegister();
}


function inviteChosenContactButtonClicked(event)
{
    // Insert Code Here
   mcChooseContactInvite();

}


function buttonJaneHClicked(event)
{
    document.location = "https://tenth.medcommons.net/router/currentccr?a=1013062431111407&aa=1117658438174637&g=a938178669bd5c7add9faa076d581313469038fb&t=&m=&c=&auth=8e80860c75ca33f30801da7e7ebac3a02ab1ca34&at=8e80860c75ca33f30801da7e7ebac3a02ab1ca34";
    
}


function buttonJanePClicked(event)
{
    document.location = '../uiMedCommons.html';
}



function gotoRegister(event)
{
    document.location = "/acct/register.php";
}
function makeNewPatient(event)
{
   
    loc  = "/router/tracking.jsp?tracking=new&accid="+ctx.accid+"&auth=" +ctx.auth; 
    mc.log ("going to make new patient at "+loc);
    document.location = loc;
}


function gotoHelp(event)
{
    
document.location = "mc_mobile_help.html";
}


function gotoShootButtonClicked(event)
{
//this doesnt work
var suffix = ''; 
suffix = suffix + '&email='+  ctx.email ;
suffix = suffix + '&accid='+  ctx.email ;
suffix = suffix + '&practiceid='+  ctx.email ;
suffix = suffix + '&providerid='+  ctx.email ;
suffix = suffix + '&patientid='+  ctx.email ;

document.location = '../dashcodeMCU.php?a'+suffix;

}


function callhome(event)
{
document.location ="tel://1234565668686";
}
