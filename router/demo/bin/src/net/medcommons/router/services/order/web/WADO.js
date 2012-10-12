/**
 * Copyright 2004-2006 MedCommons Inc.   All Rights Reserved.
 */
/********************************************************************************************
 * MedCommons WADO Viewer
 */

var p = null;
var PatientName ="";
var PatientID="";

var StudyInstanceUID="";
var StudyDate="";
var StudyTime="";
var StudyDescription="";


var SeriesDescription="";
var SeriesInstanceUID=""; 
var Modality="";
var SeriesNumber="";

var InstanceNumber="";
var SOPInstanceUID = "";
var FileReferenceID="";

var DirectoryRecordType="";

var currentImage = -1;
var currentSeries = 0;

var fast = false;//true;

var maxRows ;
var maxColumns;
var imagesSkipped = 0;

var enableNoAttachmentsMode = true;

// Array of thumbnails - tracks which thumbnails are currently
// showing in the viewer.
var thumbnails = new Array();

var mimeTypeImages = new Array();
mimeTypeImages["application/pdf"]='images/pdfthumb2.gif';
mimeTypeImages["image/jpg"]='images/picturethumbnail.gif';
mimeTypeImages["image/pjpeg"]='images/picturethumbnail.gif';
mimeTypeImages["image/jpeg"]='images/picturethumbnail.gif';
mimeTypeImages["image/gif"]='images/picturethumbnail.gif';
mimeTypeImages["image/gif"]='images/picturethumbnail.gif';
mimeTypeImages["text/x-cdar1+xml"]='images/cdathumb.gif';
mimeTypeImages["application/x-hl7"] = 'images/hl7thumb.gif';
mimeTypeImages["application/x-ccr+xml"]='images/ccrthumb.gif';
mimeTypeImages["video/quicktime"]='images/quicktime.png';

/**
 * Mime types that we recognize as images.  These can be rendered as thumbnails
 * directly to the thumbnail in scaled form.
 */
var imageMimeTypes = [ "image/jpg", "image/pjpeg", "image/jpeg", "image/gif", "image/png"];

var SIMTRAK_MENU = 3;

var symtrakTools = [
  [SIMTRAK_MENU,'Viewer',"displaySimTrak('Main')"]
];

var simtrakMapping = {
  'Main':'A',
  'Address':'B',
  'Personal':'C',
  'Medical':'D',
  'Notes':'E',
  'Injuries':'INJURY',
  'Weight':'WEIGHT'
};

function displaySimTrak(s) {
  var simtrak = new SimTrakThumbnail(ccr);
  this.thumbnails[0].simtrakThumbnail = simtrak;
  simtrak.display(simtrakMapping[s]);
}

function openSimtrakAdmin() {
  parent.location.href=simtrakAdminURL;
}

function addSimTrakTools() {
    var stMenu = parent.menu.getItem(SIMTRAK_MENU);
    if(!stMenu) 
      stMenu = parent.menu.addItem( { text: "Simtrak",  onclick: function() { return false;  } } );

    var oldSubmenu = stMenu.cfg.getProperty("submenu");
    while(oldSubmenu && oldSubmenu.getItems().length) {
          oldSubmenu.clearContent();
    }

    var submenu = [];
    forEach(symtrakTools, function(t) {
        log("Adding symtrak tool " + t[1]);
        submenu.push( { text: t[1], onclick: { fn: function() { displaySimTrak('Main'); } } });
    });
    submenu.push( { text: 'Admin', onclick: { fn: function() { openSimtrakAdmin(); } } });
    stMenu.cfg.setProperty("submenu", { id: 'symtrakMenu', itemdata: submenu });
    parent.menu.render();
}

var tools = [ 
                                  ["Edit and Send CCR","editCCR();"],
        EDIT_AS_NEW_TOOL =        ["Edit as New CCR", "editAsNew()" ],
                                  ["Download CCR","downloadCCR();"],
                                  ["Create Reply","createReplyCCR();"],
                                  ["Change DICOM Overlay","toggleOverlay();"],
                                  ["Print Document","printWindow();"],
                                  ["Help","showViewerHelp();"],
        CONFIRM_SELECTION_TOOL =  [CCR_ATTACHMENTS_MENU, "Confirm Selection","validateSelectedSeries();"],
        DISCARD_SELECTION_TOOL =  [CCR_ATTACHMENTS_MENU,"Discard Selection","discardSeries();"]
];


 // Flag to prevent multiple image requests from being made
 // while an image load is in progress.
 // Calling applications should invoke isImageLoaded() before
 // invoking setImage() to load next image.
var imageLoaded = false; 

// This is the number of the image that is currently loading
// only valid if imageLoaded = true
var imageLoading = -1;

/**
 * Error thrown if the user tries to zoom in too far.
 */
var zoomTooLargeError = new Error("Zoom value too large.");

/**
 * The largest zoom allowed in a single drag operation
 */
var maximumZoom = 0.25;

/**
 * The frame size for the main image - this is adjusted slightly down
 * for smaller screens.
 */
var mainImageHeight = 750;

/* ---------------------------Patient Object------------------------------------- */

function Patient(PatientName, PatientID){
  this.PatientName =PatientName;
  this.PatientID = PatientID;
  this.StudiesArray = new Array();
};
  
  
/* ---------------------------Study Object--------------------------------------- */
function Study(StudyDescription, StudyInstanceUID, StudyDate, StudyTime ){
  this.StudyDescription = StudyDescription;
  this.StudyInstanceUID = StudyInstanceUID;
  this.StudyDate = StudyDate;
  this.StudyTime = StudyTime;
  this.SeriesArray=new Array();
};

/* ---------------------------Series Object-------------------------------------- */
function Series(SeriesDescription, mcGUID, SeriesInstanceUID, Modality, SeriesNumber){
  this.SeriesDescription=SeriesDescription;
  this.mcGUID = mcGUID;
  this.SeriesInstanceUID=SeriesInstanceUID;
  this.Modality=Modality;
  this.SeriesNumber = parseInt(SeriesNumber);
  this.InstanceArray = new Array();
}

/* ---------------------------Instance Object------------------------------------ */
function Instance(SOPInstanceUID, InstanceNumber, FileReferenceID, window, level){
  var windowSpecified = true;
  this.InstanceNumber= parseInt(InstanceNumber);
  this.SOPInstanceUID = SOPInstanceUID;
  this.FileReferenceID=FileReferenceID;
  // DEMO HACK only.
  // In real world if the window/level values aren't set
  // the server should make some based on 
  // image attributes.
  if ((window == null) || (window == 0)) {
    this.defaultWindow = 500;
    windowSpecified = false;
  }
  else {
    this.defaultWindow = window;
  }
  
  if((windowSpecified == false) && ((level==null) || (level == 0))) {
      this.defaultLevel = 200;
  }
  else {
    this.defaultLevel = level;
  }
}
  
/* ---------------------------Series Thumbnail Object---------------------------- */
/**
 * A type of thumbnail representing a series
 */
function SeriesThumbnail(seriesNumber) {
  this.seriesNumber = seriesNumber;
}

SeriesThumbnail.prototype.enableImageCache = true;

SeriesThumbnail.prototype.display = function() {
  // Ensure black background for main image
  // This was a hack for HIMMS because CDA stylesheets don't set the body color to white,
  // so it is set to white in that case and back to black here.
  $('mainImage').style.backgroundColor='black';
  
  displaySelectedSeries(findIdentical(thumbnails,this));
}

SeriesThumbnail.prototype.updateLabels = function(thumbIndex) {
  var nImages = numberOfImageInSeries(this.seriesNumber);
  if(this.seriesNumber==currentSeries) {
    var imageLabel =   "Image " + (currentImage + 1) + "/" + nImages;
    replaceText($("thumbLabel" +thumbIndex),imageLabel);  
  }
  else {
    replaceText($("thumbLabel" + thumbIndex), nImages + " Images" );  
  }
}

SeriesThumbnail.prototype.displayThumbnail = function(thumbIndex) {

    log("Displaying series thumb in cell " + thumbIndex);

    var thumbCellDiv = $('thumbCell'+thumbIndex);
    thumbCellDiv.innerHTML='<A href="javascript:displaySelectedThumbnail('+thumbIndex +');" onfocus="this.blur();">'
          + '<IMG height=140 src="blank.png" width=140 border=0 name="thumbImage'+thumbIndex+'" id="<%=thumbnailName%>"></A>';
	var nImages = this.series.InstanceArray.length;
    var seriesName = "series" + this.seriesNumber;
    var seriesDescription = this.series.SeriesDescription;
    // In future set series titles..
    // Grab middle image
    var thumbReference = Math.round((nImages-1)/2);
    if(currentImage >= 0) {
      // log("series " + this.seriesNumber + " currentImage " + currentImage);
      thumbReference = currentImage;
    }

    var thumbImage= this.series.InstanceArray[thumbReference];

    if(thumbImage == null) {
      alert("DEBUG ERROR:  thumbnail image not found for series " 
          + getSeriesIndex(this.series) + "\r\nCalled from " + stacktrace() 
          + "\r\nInstanceArray.length="+this.series.InstanceArray.length
          + "\r\nthumbReference="+thumbReference);
      return;
    }

    var thumbURL = "wado/"+this.series.storageId + "?studyUID=";
    thumbURL+=p.StudiesArray[0].StudyInstanceUID;
    thumbURL+="&mcGUID=";
    thumbURL+=this.series.mcGUID;
    thumbURL+="&rows=140&columns=140&fname=";
    thumbURL+=thumbImage.FileReferenceID;
    thumbURL+="&windowCenter=";
    thumbURL+=thumbImage.defaultLevel;
    thumbURL+="&windowWidth=";
    thumbURL+=thumbImage.defaultWindow;
    if(document.images['thumbImage'+thumbIndex]!=null) {
      log("Displaying thumb as url " + thumbURL);
      document.images['thumbImage'+thumbIndex].src =thumbURL;
      replaceText($("thumbDescription" + thumbIndex),seriesDescription);
    }
    this.updateLabels(thumbIndex);
}

/**
 * Responds to Mozilla mouse wheel events
 */
SeriesThumbnail.prototype.onWheel = function(evt) {
  cancelEventBubble(evt);
  if(evt.detail < 0) {
    displayNextImage();
  }
  else {
    displayPreviousImage();
  }
}


/* -------------------------- SimTrak Data ------------------------------ */

function SimTrakThumbnail(ccr) {
  this.ccr = ccr;
  this.ccrThumb = new CCRThumbnail(ccr);
}

SimTrakThumbnail.prototype.display = function(section) {

  hideDragScrollPane();
  hideSeriesDragIcon();

  // If no section passed, show the CCR instead
  if(!section) {
    this.section = null;
    this.ccrThumb.display();
    return;
  }

  if(enableSimTrak) {
    this.section = section;
    var url = simtrakURL + "?" + queryString({accid:storageId, tab: 'tab_'+section});
    log("Displaying SimTrak in content pane using url " + url + " with width " + getAvailableContentWidth());
    $('mainImage').innerHTML=
      "<iframe src='"+url
          +"' name='orderWindow' id='orderWindow' width='"+getAvailableContentWidth()
          +"' height='"+getAvailableContentHeight()
          +"' style='border-style: none; background-color: white;'"
          +" bgcolor='white'/>";  // NB: for some reason IE is not receiving the signal 
  }
  else {
    this.ccrThumb.display(thumbIndex);
  }
}

SimTrakThumbnail.prototype.resize = function() {
  log("resizing simtrak thumb currently displaying section " + this.section);
  if(this.section) {
    this.display(this.section);
  }
}

SimTrakThumbnail.prototype.displayThumbnail = function(thumbIndex) {

  var cell = $('thumbCell'+thumbIndex);

  if(enableSimTrak) {
    cell.innerHTML = 
              '<div class="CCRTitle">'
      +         '<span style="position: relative;  left: 5px; top: 2px; text-align: left; width=100%;">'
      +           'CCR'
      +         '</span> '
      +       '</div>'
      + '<div class="textThumb">'
      + '<p><a href="javascript:simtrakCCR();">Show CCR</a></p>'
      + '<p><a href="javascript:simtrakMain();">Show Simtrak Data</a></p>'
      + '</div>';
  }
  else {
    this.ccrThumb.displayThumbnail(thumbIndex);
  }
}

SimTrakThumbnail.prototype.updateLabels = function(thumbIndex) {
}

function simtrakMain() {
  thumbnails[0].display(simtrakMapping['Injuries']);
}

function simtrakCCR() {
  thumbnails[0].ccrThumb.display();
}

/* --------------------------CCR Thumbnail Object------------------------------ */

/**
 * A type of thumbnail representing a CCR
 */
function CCRThumbnail(ccr) {
  this.ccr = ccr;
}

CCRThumbnail.prototype.display = function() {


  hideDragScrollPane();
  hideSeriesDragIcon();

  if($('ccrflag'))
    return;

  log("Displaying CCR with FileReferenceID=" + this.series.InstanceArray[0].FileReferenceID + ":" + stacktrace());
  $('mainImage').innerHTML=
    "<span id='ccrflag' style='display: none;'></span>" +
    "<iframe src='DisplayCCR.action?ccrIndex="+parent.currentTab.ccrIndex+"&guid="
        +this.series.mcGUID 
        +"' name='orderWindow' id='orderWindow' width='"+getAvailableContentWidth()
        +"' height='"+getAvailableContentHeight()
        +"' style='border-style: none; background-color: white;'"
        +" onload='signal(window,\"ccrloaded\");' bgcolor='white'/>";  // NB: for some reason IE is not receiving the signal 
}

CCRThumbnail.prototype.updateLabels = function(thumbIndex) {
  // noop for now
}

CCRThumbnail.prototype.displayThumbnail = function(thumbIndex) {
  log("Displaying CCR Thumb at index " + thumbIndex);

  // figure out the age/sex string
  var ageSex = '';
  if(this.ccr.patient.age != null) {
    ageSex = this.ccr.patient.age;
  }
  // dumpProperties("this.ccr.patient", this.ccr.patient);
  if(this.ccr.patient.gender == "Male")
    ageSex += "M";
  else
  if(this.ccr.patient.gender == "Female")
    ageSex += "F";
  else 
    ageSex += "?";
    
  
  var formattedDob = null;
  var dobRaw = this.ccr.patient.dateOfBirth;
    
  if ((dobRaw == '') || (dobRaw == null))
	    formattedDob = "Unknown";
  else {
	    var dobDate = new Date(this.ccr.patient.dateOfBirth);      
	    formattedDob = formatDateOfBirth(dobDate);
      if((dobDate.getMonth()==NaN) || (formattedDob == null) || (formattedDob.indexOf('NaN')>=0)) {
        formattedDob = dobRaw;
      }
  }
	    
  var ccrCreateDate = new Date(this.ccr.createDateTime);
  var ccrHtml = $('ccrThumbCellTemplate').innerHTML;
  ccrHtml = ccrHtml.replace(/##NAME##/, this.ccr.patient.givenName + " " + this.ccr.patient.familyName); 
  ccrHtml = ccrHtml.replace(/##SEX##/, ageSex); 
  ccrHtml = ccrHtml.replace(/##CCRDATE##/, formatLocalDateTime(ccrCreateDate)); 

  var tn = this.ccr.trackingNumber.substr(0,4) + ' ' 
           + this.ccr.trackingNumber.substr(4,4) + ' ' + this.ccr.trackingNumber.substr(8,4);

  ccrHtml = ccrHtml.replace(/##MCTRACK##/, tn); 
  ccrHtml = ccrHtml.replace(/##DOB##/, formattedDob); 
  var fromActor = null;
  if(this.ccr.getFromActor) {
    fromActor = this.ccr.getFromActor();
  }
  ccrHtml = ccrHtml.replace(/##CCRFROM##/, fromActor? fromActor.email : ''); 
  setHtml('thumbCell'+thumbIndex,ccrHtml);
 
  // Set the watermark image
  var thumbCellDiv = $('thumbCell'+thumbIndex);

  if(document.images['thumbImage'+thumbIndex]!=null) {
    document.images['thumbImage'+thumbIndex].src='images/ccrthumb.gif';
    replaceText($("thumbDescription" + thumbIndex),'1 page');
    replaceText($("thumbLabel" +thumbIndex),'CCR');  
  }

  // Hide labels
  hide('thumbLabel'+thumbIndex);
  hide('thumbDescription'+thumbIndex);

  // Set bg color
  el('thumbCell'+thumbIndex).style.backgroundColor='#345';

  // Adjust contents height
  el('thumbCell'+thumbIndex).style.height='140';
  el('thumbCell'+thumbIndex).style.cursor='pointer';

  // Set the onclick handler - need to use some hackery to make the callback
  // use the correct thumbIndex.
  var clickHandler = function() { displaySelectedThumbnail(thumbIndex); };
  clickHandler.thumbIndex = thumbIndex;
  el('thumbCell'+thumbIndex).onclick=clickHandler;

  // Display annotation to show that validation required
  if(this.series.paymentRequired) {
    $("thumbAnnotation"+thumbIndex).style.display='block';
    $("thumbAnnotation"+thumbIndex).src='images/payment_required.png';
  }
  else
  if(this.series.validationRequired) {
    $("thumbAnnotation"+thumbIndex).style.display='block';
    $("thumbAnnotation"+thumbIndex).src='images/validation_required.png';
  }
}

/* --------------------------PDF Thumbnail Object------------------------------ */

/**
 * A type of thumbnail representing an Document
 */
function DocumentThumbnail(theSeries) {
  this.series=theSeries;
}

DocumentThumbnail.prototype.display = function() {
  // log("Displaying thumbnail for series " + series.mimeType  + " from " + stacktrace());
  
  hideDragScrollPane();

  var documentUrl = '';
  if((this.series.mimeType=='text/x-cdar1+xml') || (this.series.mimeType=='application/x-ccr+xml')) {
    this.series.InstanceArray[0].FileReferenceID;
    $('mainImage').style.backgroundColor='white';
  }
  else
  if((this.series.mimeType=='application/pdf') && ((this.series.InstanceArray[0].FileReferenceID.indexOf("http://") != -1))) {
    documentUrl = this.series.InstanceArray[0].FileReferenceID;
  }
  else 
  if((this.series.mimeType=='application/x-hl7') && ((this.series.InstanceArray[0].FileReferenceID.indexOf("http://") != -1))) {
  	documentUrl = this.series.InstanceArray[0].FileReferenceID;
    $('mainImage').style.backgroundColor='white';
  }
  else {
    documentUrl="/router/displayDocument.do?ccrIndex="+parent.currentTab.ccrIndex+"&mcGuid="+this.series.mcGUID+"&sopUid="+this.series.InstanceArray[0].SOPInstanceUID;
    log("Showing Document with url " + documentUrl);
  }

  log("Available width is " + getAvailableContentWidth());
  $('mainImage').innerHTML =
    "<iframe src='"+documentUrl + "' name='orderWindow' id='orderWindow' width='"+getAvailableContentWidth()+"' height='"+getAvailableContentHeight()+"' style='border-style: none;'/>";  

  hideSeriesDragIcon();
}

DocumentThumbnail.prototype.updateLabels = function(thumbIndex) {
  // noop for now
}

DocumentThumbnail.prototype.displayThumbnail = function(thumbIndex) {

  var thumbCellDiv = $('thumbCell'+thumbIndex);
  //var thumbArrayIndex=getThumbArrayIndex(thumbIndex);
  var cellClass='NotSelectedImage';
  if(thumbnails[currentThumb]==this) {
    if(!hideSeries()) // don't highlight if only 1 series shown
      cellClass='SelectedImage';
  }
  var thumbImageSrc = mimeTypeImages[this.series.mimeType];
  if(thumbImageSrc == null) {
    thumbImageSrc = 'images/unknownthumb.gif'; 
  }

  // HACK:  for non-local items (CDA, CCR, PDF) display the description, everything else use the file name
  // BUG: Some PDFS are local.
  var thumbDescription = this.series.InstanceArray[0].FileReferenceID + ' ('+ this.series.SeriesDescription +')'
  if(
  (this.series.mimeType=='text/x-cdar1+xml') || 
  (this.series.mimeType=='application/x-ccr+xml') || 
  (this.series.mimeType=='application/pdf') ||
  (this.series.mimeType=='application/x-hl7') ||
  (this.series.mimeType=='text/html')
  )
  {
    thumbDescription = this.series.SeriesDescription;
  }

  // If PDF we may have the number of pages
  if(this.series.mimeType=='application/pdf') {
    if(this.series.billingEvent && (this.series.billingEvent.type == 'INBOUND_FAX')) {
      thumbDescription += "&nbsp; (" + this.series.billingEvent.quantity + " page"+ ((this.series.billingEvent.quantity > 1) ? "s" : "") + ")";
    }
  }

  var spacer = '';
  if(findValue(imageMimeTypes,this.series.mimeType)>=0) {
    thumbImageSrc = 'ImageThumb.action?ccrIndex='
      +window.parent.currentTab.ccrIndex
      +'&seriesIndex='+this.series.index
      +'&height=75&width=75';
    thumbDescription = this.series.SeriesDescription;
    spacer='<br/>';
  }

  var newHtml ='<div class="NotSelectedImage" onclick="displaySelectedThumbnail('+thumbIndex +');" style="cursor: hand;">'
          + '<div class="CCRTitle"><span style="position: relative;  left: 5px; text-align: left; width=100%;">'
          + '<img border="0" style="position: relative; top: 3px;" src="images/record.gif"/>&nbsp;&nbsp;Document</span></div>'
          +'<div class="CCRThumbLabels" style="height: 110px; position: relative; top: 4px; "><div id="ccrThumbName" style="width: 140px;">' 
          + '&nbsp;' + trunc(thumbDescription, 30) + '</div>'
          + spacer 
          +'<div class="CCRThumbLabels" style="text-align: center;"><img src="'+thumbImageSrc+'"/></div>';
  newHtml += '</div>';
  thumbCellDiv.innerHTML=newHtml;
  replaceText($("thumbDescription" + thumbIndex),'');
  replaceText($("thumbLabel" +thumbIndex),'');  
  // Display annotation to show that validation required
  if(this.series.paymentRequired) {
    $("thumbAnnotation"+thumbIndex).style.display='block';
    $("thumbAnnotation"+thumbIndex).src='images/payment_required.png';
  }
  else
  if(this.series.validationRequired) {
    $("thumbAnnotation"+thumbIndex).style.display='block';
    $("thumbAnnotation"+thumbIndex).src='images/validation_required.png';
  }
  else {
    $("thumbAnnotation"+thumbIndex).style.display='none';
  }
}


/* --------------------------WebReference Thumbnail Object------------------------------ */

/**
 * A type of thumbnail representing an external document on the web.
 */
function WebReferenceThumbnail(theSeries) {
  this.series=theSeries;
}

WebReferenceThumbnail.prototype.display = function() {

  var documentUrl=this.series.InstanceArray[0].FileReferenceID;
  log("Showing Web Reference with url " + documentUrl);
  /*$('mainImage').innerHTML=
    "<iframe src='"+documentUrl + "' name='orderWindow' width='"+getAvailableContentWidth()+"' height='"+getAvailableContentHeight()+"' style='border-style: none;'/>";  
  */
  window.open(documentUrl,'reference');
  $("dragScrollOuter").style.display='none';
  hideSeriesDragIcon();
}

WebReferenceThumbnail.prototype.updateLabels = function(thumbIndex) {
  // noop for now
}

WebReferenceThumbnail.prototype.displayThumbnail = function(thumbIndex) {
  var thumbCellDiv = $('thumbCell'+thumbIndex);
  //var thumbArrayIndex=getThumbArrayIndex(thumbIndex);
  var cellClass='NotSelectedImage';
  if(thumbnails[currentThumb]==this) {
    if(!hideSeries()) // don't highlight if only 1 series shown
      cellClass='SelectedImage';
  }
  var thumbImageSrc = mimeTypeImages[this.series.mimeType];
  if(thumbImageSrc == null) {
    thumbImageSrc = 'images/unknownthumb.gif'; 
  }

  var thumbDescription = this.series.InstanceArray[0].FileReferenceID + ' (Web Document)';
  var newHtml ='<div class="NotSelectedImage" onclick="displaySelectedThumbnail('+thumbIndex +');" style="cursor: hand;">'
          + '<div class="CCRTitle"><span style="position: relative;  left: 5px; text-align: left; width=100%;">'
          + '<img border="0" style="position: relative; top: 3px;" src="images/record.gif"/>&nbsp;&nbsp;Document</span></div>'
          +'<div class="CCRThumbLabels" style="height: 110px; position: relative; top: 4px; "><div id="ccrThumbName" style="width: 140px;">' 
          + '&nbsp;' + thumbDescription + '</div>'
          +'<div class="CCRThumbLabels"><img src="'+thumbImageSrc+'"/></div>';
  newHtml += '</div>';
  thumbCellDiv.innerHTML=newHtml;
  replaceText($("thumbDescription" + thumbIndex),'');
  replaceText($("thumbLabel" +thumbIndex),'');  

  if(this.series.validationRequired) {
    el("thumbAnnotation"+thumbIndex).style.display='block';
    el("thumbAnnotation"+thumbIndex).src='images/validation_required.png';
  }
  else {
    el("thumbAnnotation"+thumbIndex).style.display='none';
  }
}


/* --------------------------Blank Thumbnail Object------------------------------ */
/**
 * Blank thumbnail type
 */
function BlankThumbnail() {
}

BlankThumbnail.prototype.display = function() {
  // noop
}
BlankThumbnail.prototype.updateLabels = function(thumbIndex) {
  var thumbImageName='thumbImage'+thumbIndex;
  var thumbCellDiv = $('thumbCell'+thumbIndex);    
  if(thumbCellDiv != null) {
    thumbCellDiv.innerHTML='<IMG height=140 src="blank.png" width=140 border=0 name="thumbImage'+thumbIndex+'" id="'+thumbImageName+'"></A>';
    replaceText($("thumbDescription" + thumbIndex),'');
    replaceText($("thumbLabel" +thumbIndex),'');  
  }
}
BlankThumbnail.prototype.displayThumbnail = function(thumbIndex) {
  if(document.images['thumbImage'+thumbIndex]!=null) {
    document.images['thumbImage'+thumbIndex].src='blank.png';
    replaceText($("thumbDescription" + thumbIndex),'');
  }
}


/* --------------------------Caution Thumbnail------------------------------ */
function CautionThumbnail(ccr,series) {
  this.ccr = ccr;
  this.series = series;
  this.ccrThumbnail = new CCRThumbnail(ccr);
  this.ccrThumbnail.series = series;

  // First time we display the CCR.  Subsequent times we display the terms of use.
  this.displayed = false;
}

CautionThumbnail.prototype.display = function() {
  this.ccrThumbnail.display();
}

CautionThumbnail.prototype.updateLabels = function(thumbIndex) {
  var thumbImageName='thumbImage'+thumbIndex;
  var thumbCellDiv = $('thumbCell'+thumbIndex);    
  if(thumbCellDiv != null) {
    if(!cautionClosed) {
      thumbCellDiv.innerHTML='<div id="cautionThumb" class="textThumb">'
        +'<h3>CAUTION:</h3>'
        +'<p>Prototype software.</p>'
        +'<p>For beta use only.</p>'
        +'<a href="'+touUrl+'" target="tou">Terms of Use</a> &nbsp;&nbsp;'
        +'<a href="#" onclick="return closeCaution();">Close</a>'
        +'</div>';

      replaceText($("thumbDescription" + thumbIndex),'');
      replaceText($("thumbLabel" +thumbIndex),'');  
    }
    else {
      thumbCellDiv.innerHTML=
                '<div class="CCRTitle">'
        +         '<span style="position: relative;  left: 5px; top: 2px; text-align: left; width=100%;">'
        +           'CCR'
        +         '</span> '
        +       '</div>'
        + '<div class="textThumb">'
        + '<p><a href="javascript:thumbnails[0].ccrThumbnail.display();">Show CCR</a></p>'
        + '<p><a href="javascript:editCCR();">Edit CCR</a></p>'
        + '</div>';
    }
  }
}
CautionThumbnail.prototype.displayThumbnail = function() {
}
CautionThumbnail.prototype.resize = function(thumbIndex) {
  if(this.simtrakThumbnail)
    this.simtrakThumbnail.resize();
  else
    this.display();
}


var cautionClosed = false;

function closeCaution() {
  cautionClosed = true;

  if(enableSimTrak) {
    var ser = thumbnails[0].series;
    thumbnails[0] = new SimTrakThumbnail(ccr);
    thumbnails[0].series = ser;
    thumbnails[0].ccrThumb.series = ser;
  }

  thumbnails[0].displayThumbnail(0);
  return false;
}

/* ------------------------------Image Caching Support-------------------------- */

var enableCacheDisplay = true;

function ImageCache(cacheSize) {
  this.cacheSize=cacheSize;
  this.currentCacheOffset=0;
  this.cacheNext=ImageCacheCacheNext;
  this.reset=ImageCacheReset;
  this.forwardSpec = new ImageSpec(currentSeries,currentImage);
  this.backwardSpec = new ImageSpec(currentSeries,currentImage);
}

function ImageCacheCacheNext() {

  if(!isImageLoaded()) {
    //ImageCacheHideCachedPane();
    return;
  }

  if(this.currentCacheOffset<=this.cacheSize) {
    window.status="Cached " + (100*this.currentCacheOffset/this.cacheSize) + "%  - " 
      + getCookie("imageKBytesPerSecond") + " KBytes/sec";
    if(enableCacheDisplay) {
      var cacheDisplay=$("cachedAmount");
      var cachedPercent=$("cachedPercent");
      if((cacheDisplay != null) && (cachedPercent != null)){
        cacheDisplay.style.width=(this.currentCacheOffset/this.cacheSize) * (findPosX(cachedPercent)-findPosX(cacheDisplay)-3);
        cachedPercent.innerHTML=(100*this.currentCacheOffset/this.cacheSize) + "%";
        var cachedPane=$("cachedPane");
        if(cachedPane != null) {
          cachedPane.style.display='block';
        }
      }
      var speedDisplay=$("speedAmount");
      var speedPercent=$("speedPercent");
      if((speedDisplay != null) && (speedPercent != null)){
        var imageKBytesPerSecond = getCookie("imageKBytesPerSecond");
        if(imageKBytesPerSecond!=null) {
          var speedKBS=parseFloat(imageKBytesPerSecond);
          if(speedKBS>0) { // Don't display if zero - looks strange (note: log also blows up below).            
            var logSpeedKBS=Math.log(speedKBS)/2.0/2.3; // divide by 2.3 converts natural=>log10, divide/2 gives us log100
            var newWidth=(logSpeedKBS) * (findPosX(speedPercent)-findPosX(speedDisplay)-3);
            if(newWidth<0)
              newWidth=0;
            //log("Speed Update:  speedKBS="+speedKBS+" speedPercent="+speedPercent+" speedDisplay="+speedDisplay + " newWidth="+newWidth);
            speedDisplay.style.width=newWidth;
            speedPercent.innerHTML=getCookie("imageKBytesPerSecond") + "&nbsp;KB/s";
            var speedPane=$("speedPane");
            if(speedPane != null) {
              speedPane.style.display='block';
            }
          }
       }
      }
    }
    this.currentCacheOffset++;
    this.forwardSpec.next();
    this.backwardImage = new Image();
    this.backwardSpec.prev();
    //log("Caching image " + this.currentCacheOffset 
    //    + "(" + this.backwardSpec.series + "," +  this.backwardSpec.imageNum + ") " 
    //    + getImageUrl(this.backwardSpec.series, this.backwardSpec.imageNum));

    setCookie("priority","low"); // ensure that caching gets done at low priority
    this.backwardImage.src = getImageUrl(this.backwardSpec.series, this.backwardSpec.imageNum);
    this.forwardImage = new Image();
    this.forwardImage.onload = function () { imageCache.cacheNext(); }
    this.forwardImage.onerror = function () { imageCache.cacheNext(); }
    this.forwardImage.onabort = function () { imageCache.cacheNext(); }
    this.forwardImage.src = getImageUrl(this.forwardSpec.series, this.forwardSpec.imageNum);
  }
  else {
    window.setTimeout(ImageCacheHideCachedPane,3000);
    log("Caching finished");
  }
}

function ImageCacheReset() {
  //log("Resetting image cache to (" + currentSeries + "," + currentImage + "):  " + stacktrace());
  this.currentCacheOffset=0;
  this.forwardSpec = new ImageSpec(currentSeries,currentImage);
  this.backwardSpec = new ImageSpec(currentSeries,currentImage);
}

function ImageCacheHideCachedPane() {
  var cachedPane=$("cachedPane");
  if(cachedPane != null) {
    cachedPane.style.display='none';
    window.status='';
  }
  var speedPane=$("speedPane");
  if(speedPane != null) {
    speedPane.style.display='none';
  }
}

/**
 * The global image cache object
 */
var imageCache = new ImageCache(5);

/* ------------------------------ToolState Support------------------------------- */
// Constants used in ToolState object.
var toolZoom = "Zoom";
var toolWL     = "WindowLevel";
var toolWLSelect = "WindowLevelSelect";
var localizerAnnotation = "&annotation=localizers";
var allAnnotation = "&annotation=patient,localizers";
var noAnnotation1 = "&annotation=none1";
var noAnnotation2 = "&annotation=none2";

// Creates object used by event handlers to determine
// which actions are invoked.
function ToolState() {

  this.activeTool = toolZoom;
  
  // currentRegion contains the region of the image to be displayed (zoomed). 
  // It is initially null; it is changed to null whenever a series boundary is crossed.
  this.currentRegion = null;
  
  // currentAnnotation contains the WADO URL arguments for overlay arguments.
  // There are thee types: one draws only patient demographics; another paints 
  // demographics plus image and study information while the third shows no
  // overlay information at all. These states correspond to
  // the WADO patient and technique modifies to the annotation parameter.
  this.currentAnnotation = allAnnotation;
  
  // currentWindowLevel contains the value used for window/level of
  // an image within the series. If null, the default window/level for the instance
  // is used.
  // Value is set to null when a series is changed.
  this.currentWindowLevel = null;
  this.currentWindow = null;
  this.currentLevel = null;
  
  this.currentWindowArray = null;
  this.currentLevelArray = null;
  
  this.showImagesFullSize = false;
  this.zoomed = false;
  
  this.regionX1 = 0;
  this.regionY1 = 0;
  this.regionX2 = 0;
  this.regionY2 = 0;
};

var currentToolState = new ToolState();


var startX = 0;
var startY = 0;
var endX = 0;
var endY = 0;
var imageHeight = 0;
var imageWidth=0;
var imageOffsetX = 0;
var imageOffsetY = 0;
var scrollDragLimitX = 0;

function showErrorMessage(msg) {  
  $("errorPaneMessage").innerHTML=msg;
  $("errorPane").style.display='block';
  window.status=msg;
  window.setTimeout(hideErrorMessage, 2000);
}

function hideErrorMessage() {
  $("errorPane").style.display='none';
}

function setZoomActive(){
  currentToolState.activeTool = toolZoom;
  if(document.WL_Zoom_Toggle != null) {
    document.WL_Zoom_Toggle.src = "ZOOM.jpg";
  }
}
function setWindowLevelActive(){
  currentToolState.activeTool = toolWL;
  document.WL_Zoom_Toggle.src = "Window-Level.jpg";
  moveThumbnailRect(-100, -100);
}

function toggleZoomWL(){
  currentToolState.currentRegion = null;
  imageLoaded = true;

  if(currentToolState.currentWindow == null) {
    var image= p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[currentImage];
    if(image != null) {
      currentToolState.currentWindow = image.defaultWindow;
      currentToolState.currentLevel = image.defaultLevel;
    }
  }

  if (currentToolState.activeTool == toolWL)
    setZoomActive();
  else
    setWindowLevelActive();

  displayCurrentImage();
}

/**
 * Dragging functions
 */ 

/**
 * This is the general function that detects the drag and passes off 
 * to other functions to handle specific scenarios.
 * 
 * The end of a drag is detected in the 'upHandler' function (see below).
 */
 function beginDrag(element, event){
   if (!event) event= window.event; // IE
   imageLoaded = true;
   calculateImageDimensions();
   // If the drag starts on the far right border then
   // it's going to be a image scrolling drag within
   // a series.
   var x = event.clientX;
  window.status="currentToolState.activeTool="+currentToolState.activeTool;
  // Note: we put WLSelect before anything else so that when the user
  // is in WL select mode it cannot be interfered with by a scrolld drag.
  if (currentToolState.activeTool == toolWLSelect) {
     log("mousedown: selectWindowLevel");
     selectWindowLevel(element, event);
  }
  else if (currentToolState.activeTool == toolZoom) {
     log("mousedown: toolZoom");
     beginZoomDrag(element, event);
  }
  else if (currentToolState.activeTool == toolWL) {
     log("mousedown: beginWLDrag");
     beginZoomDrag(element, event);
  }
  else {
     log("mousedown: ignored");
     window.status="ignored activeTool is " + currentToolState.activeTool;
  }
 }
    
function beginZoomDrag(elementToZoom, event) {

    startX = event.clientX + document.body.scrollLeft;
    startY = event.clientY + document.body.scrollTop;

    window.status='begin drag at ' + startX + ',' + startY;

    calculateImageDimensions();

    var dragHandler = new DragHandler(event);
    dragHandler.handleMove = trackRegionHandler;
    dragHandler.handleUp = zdUpHandler;

    /**
     * This is the handler that captures mousemove events which
     * generate the endpoint of the region.
     **/
    function trackRegionHandler(event) {

      endX = event.clientX + document.body.scrollLeft;
      endY = event.clientY + document.body.scrollTop;

      var dragRect = $("mainImageZoomRect");
      dragRect.style.zIndex='30';
      dragRect.style.top=Math.min(startY,endY);
      dragRect.style.left=Math.min(startX,endX);
      dragRect.style.width=Math.abs(endX-startX);
      dragRect.style.height=Math.abs(endY-startY);
      dragRect.style.borderStyle='dotted';

      var zoomMagnitude = calculateZoomMagnitude(startX,startY,endX,endY);
      if(zoomMagnitude < maximumZoom) {
        dragRect.style.borderColor='red';
      }
      else {
        dragRect.style.borderColor='white';
      }
      // debug: useful, may be costly though
      //var actualStartX = startX - $('mainImage').offsetLeft;
      //var actualStartY = startY - $('mainImage').offsetTop;
      //var actualEndX = endX - $('mainImage').offsetLeft;
      //var actualEndY = endY - $('mainImage').offsetTop;
      //window.status="("+actualStartX+","+actualStartY+")-("+actualEndX + ","+actualEndY+")";
    }

    /**
     * This is the handler that captures the final mouseup event that
     * occurs at the end of a zoom drag.
     **/
    function zdUpHandler(event) {
      var dragRect = $("mainImageZoomRect");
      dragRect.style.borderStyle='none';
      dragRect.style.zIndex='0';
      
      try {
        
        if(currentToolState.zoomed) {
          throw zoomTooLargeError;
        }

        if(currentToolState.activeTool == toolWL) {
          generateWindowLevelGrid();
        }

        handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight);
        updateDisplayedThumbnail();
        setThumbnailRectangleOnSeries();
        currentToolState.zoomed = true;
      }
      catch(e) {
        if(e == zoomTooLargeError) {
          showErrorMessage("Zoom magnitude too great.  Please select a larger area.");
          if(currentToolState.activeTool == toolWLSelect) {
            currentToolState.activeTool = toolWL;
          }
        }
        else {
          throw e;
        }
      }
    }
} // End zoom drag.
 
function handleDragStart() {
  //alert('Drag started!');
  return false;
}
 
/**
 * Handles drag of zoom rectangle within thumbnails.
 * This handler is called directly fron onclick() handlers
 * on thumbnail elements themselves.
 */
function beginZoomRectangleDrag(elementToDrag, event) {
 
    if (!event) event = window.event;  // IE event model

    $('zoomRectangle').ondragstart=handleDragStart;

    // Compute the distance between the upper-left corner of the element
    // and the mouse click. The moveHandler function below needs these values.
    startX = event.clientX - parseInt(elementToDrag.style.left);
    startY = event.clientY - parseInt(elementToDrag.style.top);

    var dragHandler = new DragHandler(event);
    dragHandler.handleMove = zoomRectangleMoveHandler;
    dragHandler.handleUp = function(e) { updateZoomRectangle(e.clientX, e.clientY); }
}

/**
 * This is the handler that captures mousemove events when an element
 * is being dragged.  It is responsible for moving the element.
 **/
function zoomRectangleMoveHandler(e) {
  endX = e.clientX;
  endY = e.clientY;

  //var element = $("zoomMap");
  // Move the element to the current mouse position, adjusted as
  // necessary by the offset of the initial mouse click.
  //elementToDrag.style.left = (e.clientX - deltaX) + "px";
  //elementToDrag.style.top = (e.clientY - deltaY) + "px";
  //elementToDrag.style.left = (e.clientX -5) + "px";
  //elementToDrag.style.top = (e.clientY -5) + "px";
  moveThumbnailRect(endX , endY);
  window.status="X=" + e.clientX + ",  Y=" + e.clientY;
}

/**
 * Maps a virtual thumbnail index to the physical
 * thumbcell index (one of four) on the screeen where it
 * would appear.
 */
function getThumbCellIndex(thumbnailIndex) {
  return (((thumbnailIndex-1) % thumbnailPageSize) + 1)
}

/**
 * Converts a thumbCellIndex (0-3) to an index in the thumbnail array
 * based on the current thumbnail page.
 */
function getThumbArrayIndex(thumbCellIndex) {  
  if(thumbCellIndex==0)
    return 0;
  return currentThumbnailPage*thumbnailPageSize + thumbCellIndex;
}

// Displays thumbnail of current image instead
// of default 'middle' image for series.
function updateDisplayedThumbnail(){
    var thumbImageName = "thumbImage" + getThumbCellIndex(currentThumb);
    var thumbImage= p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[currentImage];
    var thumbURL = "wado/"+p.StudiesArray[0].SeriesArray[currentSeries].storageId+"?studyUID=";
    thumbURL+=p.StudiesArray[0].StudyInstanceUID;
    thumbURL+="&rows=140&columns=140&fname=";
    thumbURL+=thumbImage.FileReferenceID;
    thumbURL+="&mcGUID=";
    thumbURL+=p.StudiesArray[0].SeriesArray[currentSeries].mcGUID;
    thumbURL+="&windowCenter=";
    thumbURL+=thumbImage.defaultLevel;
    thumbURL+="&windowWidth=";
    thumbURL+=thumbImage.defaultWindow;
    
    // TODO:   ssadedin - somehow broke this, commented out until I can fix it
    if($(thumbImageName)!=null) {
      $(thumbImageName).src =thumbURL;
    }
}

function calculateImageDimensions(){
    imageWidth =document.image.width;
   imageHeight = document.image.height;
  
  // Note we have to adjust for the position of the image on the page since it may
  // be defined by the user via css
   scrollDragLimitX = imageWidth - 32 + $('mainImage').offsetLeft;
 }

 function selectWindowLevel(element, event){
     calculateImageDimensions();

    // Adjust for window position
    clickX = event.clientX - $('mainImage').offsetLeft;
    clickY = event.clientY - $('mainImage').offsetTop;

    // Adjust for window size
    clickX = clickX/(imageWidth/3);
    clickY = clickY/(imageHeight/3);

    var tileRow = Math.floor(clickX) ;
    var tileCol = Math.floor(clickY);
    var index = ( tileCol * 3) + tileRow;
    
    log("Window level index " + index + " selected.");
    
     var window = currentToolState.currentWindowArray[index];
    var level = currentToolState.currentLevelArray[index];
    
//    alert("window level: row:" + tileRow + ", col:" + tileCol + ", index:" + index +
//    ", clickX:" + clickX + ", clickY=" + clickY + ", imageWidth = " + imageWidth +
//  ", imageHeight = " + imageHeight +
//   ", window =" + window + ", level=" + level);
//   
//   var temp = "window level array";
//   for (var i=0;i<9;i++){
//     temp +="\n " + i + " w: " + currentToolState.currentWindowArray[i] + ", l:" + 
//       currentToolState.currentLevelArray[i];
//   }
//   alert(temp);

    log('window='+window + ', level='+level);
    setWindowLevel(window, level);

   // set state back to window/level.
   setWindowLevelActive();
   currentToolState.currentRegion = null;
   displayCurrentImage();
 }

/**
 * Displays a thumbnail in the main viewer based
 * on the index of the thumbnail in the thumbnail array
 * This works whether the thumbnail contains an order or a 
 * series.
 */ 
function displaySelectedThumbnail(thumbCellIndex) {
  var index = getThumbArrayIndex(thumbCellIndex);
  currentThumb=index;

  var series = thumbnails[index].series;
  if(series.paymentRequired) {
    paymentRequiredDlg(series.billingEvent, partial(doPayment, series, partial(displaySelectedThumbnail,thumbCellIndex)));

        /*
    if(confirm('Payment is required to access this content.\n\nClick OK to pay for this content using your account, or Cancel to return.')) {
      doPayment(series, partial(displaySelectedThumbnail,thumbCellIndex));
    }
    */
    return;
  }

  if(index == 0) {
    thumbnails[0].display();
    //currentThumbnailPage=0;
    //currentThumb=0;
  }
  else {
    //log("displaying thumbnail " + index + ':' + stacktrace());
    thumbnails[index].display();  
  }
  adjustContentAreaSize();
  updateActiveImage();
  updatePagerLinks();
  updateMenu();
}

/**
 * Displays the series for the given thumbnail, also selecting
 * that thumbnail.
 */
function displaySelectedSeries(thumbnailIndex) {
  // Else show the selected series.    
  selectSeries(thumbnails[thumbnailIndex].series);
  currentThumb = thumbnailIndex;
  displayCurrentImage();
  series = thumbnails[thumbnailIndex].series;
}

function displaySelectedImage(n){
  currentImage = n;
  if (currentImage< 0)
    currentImage = 0;
  else if (currentImage >= numberOfImageInSeries(currentSeries)){
    currentImage = numberOfImageInSeries(currentSeries) - 1;
  }
  displayCurrentImage();
}


function numberOfSeries(){
  return(p.StudiesArray[0].SeriesArray.length);
}

function numberOfImageInSeries(seriesNumber){
  return(p.StudiesArray[0].SeriesArray[seriesNumber].InstanceArray.length);
}

/*************************************************/

/**
 * ImageSpec defines an image - it just consists
 * of the series and image numbers.
 */
function ImageSpec() 
{
  this.series = 0;
  this.imageNum = 0;
  this.next = ImageSpecNextImage;
  this.prev = ImageSpecPrevImage;
  this.cache = ImageSpecCache;
}

/**
 * ImageSpec copied from another ImageSpec
 */
function ImageSpec(other) 
{
  this.series = other.series;
  this.imageNum = other.imageNum;
  this.next = ImageSpecNextImage;
  this.prev = ImageSpecPrevImage;
  this.cache = ImageSpecCache;
}

/**
 * ImageSpec from specific image/series
 */
function ImageSpec(initSeries, initImageNum) 
{
  this.series = initSeries;
  this.imageNum = initImageNum;
  this.next = ImageSpecNextImage;
  this.prev = ImageSpecPrevImage;
  this.cache = ImageSpecCache;
}


/**
 * Returns the next image spec from this spec
 */
function ImageSpecNextImage() 
{
  this.imageNum++;
  if (this.imageNum < 0) {
    this.imageNum=0;
  }
  else 
  if(this.imageNum >= numberOfImageInSeries(this.series)) {
    var nSeries = numberOfSeries();
    //this.series++;
    this.imageNum=0;
    if(this.series>=nSeries) {
      this.series = 0;
      this.imageNum = 0;
    }
    else {
      this.imageNum = 0;
    }  
  }
}

/**
 * Returns the previous image in a sequence from this spec
 */

function ImageSpecPrevImage() {
  this.imageNum--;
  if (this.imageNum < 0){
    /* this.series--;    
     var nSeries = numberOfSeries();
    if (this.series < 0){
      this.series = nSeries -1;
    }
    */
    this.imageNum = numberOfImageInSeries(this.series)-1;
  }
}

function ImageSpecCache() {
  var imageURL = getImageUrl(this.series, this.imageNum);
  var cacheImage = new Image();
  cacheImage.onload = function () { log("CACHE: Image loaded"); }
  log("Caching image " + imageURL);
  cacheImage.src = imageURL;
  return cacheImage;
}

/*************************************************/


function displayPreviousImage(){
  currentImage--;
  log("prev image " + currentImage);
  if(currentImage < 0){
    currentImage = numberOfImageInSeries(currentSeries)-1;
  }
  imageCache.reset();
  displayCurrentImage();
}

function displayNextImage(){
  currentImage++;
  log("next image " + currentImage);
  if (currentImage < 0) {
    currentImage=0;
  }
  else 
  if(currentImage >= numberOfImageInSeries(currentSeries)) {
    currentImage = 0;
  }
  imageCache.reset();
  displayCurrentImage();
}

// TODO: this is only called for IE
// should unify the IE mozilla handling
// by calling thumbnail[x].onWheel()
function handleMousewheel(n){
  if(thumbnails[currentThumb].series.mimeType != 'application/dicom')
    return;

  if (imagesSkipped > 0){
    //fast = true;
    imagesSkipped = 0;
  }
  
  if (n>0)
    displayNextImage();
  else if (n<0)
    displayPreviousImage();
  fast = false;
}

/**
 * Causes the requested series to be displayed in the main image area
 */
function selectSeries(series) {

  var previousSeries = currentSeries;
  currentSeries = getSeriesIndex(series);
  currentImage = 0;
  if(series.lastImageIndex)
    currentImage = series.lastImageIndex;

  currentThumb=thumbnailIndexFromSeries(series);

  resetSeriesChange();
  updateActiveImage();

  var image= p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[currentImage];
  if(image != null) {
    currentToolState.currentWindow = image.defaultWindow;
    currentToolState.currentLevel = image.defaultLevel;
  }


  // Make sure that we are showing the correct page of thumbnails
  currentThumbnailPage = Math.floor( (currentThumb-1) / thumbnailPageSize);
  if(currentThumbnailPage<0)
    currentThumbnailPage = 0;
  log("currentThumbnailPage = " + (currentThumb)+ " / " + thumbnailPageSize + " = " + currentThumbnailPage);
  initializeThumbnails();
  updateActiveImage();
  updatePagerLinks();
  updateMenu();
  if(series.presets.length > 0) {
    var wlMenu = parent.menu.getItem(3);
    if(!wlMenu) 
      wlMenu = parent.menu.addItem( { text: "Window / Level Presets",  onclick: function() { return false;  } } );

    var oldSubmenu = wlMenu.cfg.getProperty("submenu");
    while(oldSubmenu && oldSubmenu.getItems().length) {
          oldSubmenu.removeItem(0);
    }

    var submenu = [];
    forEach(series.presets, function(preset) {
        submenu.push( { text: preset.name, onclick: { fn: function() { setWindowLevel(preset.window,preset.level);  displayCurrentImage(); return false; }} } );
    });
    wlMenu.cfg.setProperty("submenu", { id: 'wlpresets'+(new Date()).getTime(), itemdata: submenu });
    parent.menu.render();
  }
  else {
    parent.menu.removeItem(3); // Remove W/L menu
  }

  log("Selecting menu item " + (currentSeries+1));
  $("selectionMenu").selectedIndex=currentSeries+1;
}

function updateMenu() {
  // Adjust menu for new series
  /*
  window.parent.disableTools(
			     function(n) { return n=='Change DICOM Overlay'; }, 
      (thumbnails[currentThumb].series.mimeType != 'application/dicom'));
      */
}

function onSwitchTab(url) {
  if(parent.menu.getItem(3)) {
    parent.menu.removeItem(3);
  }
  log('Switching tab to url ' + url);
  window.location = url;
  return true;
}

function thumbnailIndexFromSeries(series) {
  for(i=0; i< thumbnails.length;++i) {
    log("thumbnails["+i+"]="+thumbnails[i].series);
    if(thumbnails[i].series == series) {
      log("Found series at index " + i);
      return i;
    }
  }
  log("WARN: no thumbnail found for series " + series);
  return -1;
}

function getSeriesIndex(series) {
  for(i=0; i<p.StudiesArray[0].SeriesArray.length;++i) {
    if(p.StudiesArray[0].SeriesArray[i] == series)
      return i;
  }
  log("WARN: no series index found for series " + series);
  return -1;
}

function updatePagerLinks() {

  log('updatePagerLinks:  currentThumb=' + currentThumb);

  // If the fixed zero-position thumb is being displayed, unhighlight everything
  //var highlightedLinkIndex = ((currentThumb % 4) == 0) ? 0 : currentThumb;
  var highlightedLinkIndex = currentThumb;

  // Update the styles of the pager links
  for(thumbnailIndex=0; thumbnailIndex<thumbnails.length; thumbnailIndex++) {
    var seriesLink = $('pagerLink'+thumbnailIndex);
    //log("Pager link style " + thumbnailIndex + ": " + seriesLink);
    if(seriesLink != null) {        
      if(seriesLink.style.display=='none') {
        seriesLink.style.display='';
      }
      if(thumbnailIndex == highlightedLinkIndex) {
        seriesLink.className='SelectedPagerLink';
      }
      else {
        seriesLink.className='NotSelectedPagerLink';
      }
    }

    var seriesCell=$('pagerCell'+thumbnailIndex);
    if(seriesCell != null) {

      if(isThumbnailVisible(thumbnailIndex)) {
        seriesCell.style.borderBottom='solid 1px white';
      }
      else {
        seriesCell.style.borderBottom='none';
      }
    }
  }  

  for( ; thumbnailIndex < p.StudiesArray[0].SeriesArray.length+2; ++thumbnailIndex) {
    var seriesLink = $('pagerLink'+thumbnailIndex);
    if(seriesLink != null) {
      seriesLink.style.display='none';
    }
  }
}

/**
 * Returns true if the thumbnail for the given series is currently 
 * visible on the screen.
 */
function isThumbnailVisible(seriesIndex) {
  var startHighlightIndex=currentThumbnailPage*thumbnailPageSize + 1;
  var endHighlightIndex=(currentThumbnailPage+1)*thumbnailPageSize + 1;

  return (seriesIndex >= startHighlightIndex) && (seriesIndex <endHighlightIndex);
}

/**
 * Updates the thumbnail images to ensure the correct one is highlighted
 * and the labels for each thumbnail are correct.
 */
function updateActiveImage() {
  var nSeries = numberOfSeries();
  for(thumbIndex=0; thumbIndex<4; thumbIndex++) {     
    var thumbArrayIndex = 
      thumbIndex == 0 ? 0 : currentThumbnailPage*thumbnailPageSize + thumbIndex;

    thumbCellName = "thumbCell" + thumbIndex;  
    thumbnails[thumbArrayIndex].updateLabels(thumbIndex);

    if($(thumbCellName) != null) {
      if((currentThumb == thumbArrayIndex) && !hideSeries()) {
        log("highlighting thumbCell " + thumbIndex);
        $(thumbCellName).className="SelectedImage";
      }
      else {
        $(thumbCellName).className="NotSelectedImage";
      }
    }
    else {
      log("Image " + thumbCellName + " not found!");
    }
  }
  
  var selectionMenu = $("selectionMenu");
  if(selectionMenu)
    selectionMenu.selectedIndex=currentThumb+1;

  // If the series needs validation, display that option in the menu
  var series = thumbnails[currentThumb].series;
  if(series.validationRequired)
    log("validation required for series " + currentSeries);

  enableValidationTools(series.validationRequired);
}

function enableValidationTools(enable) {
  window.parent.enableTool(CONFIRM_SELECTION_TOOL, enable);
  window.parent.enableTool(DISCARD_SELECTION_TOOL, enable);
}

/**
 * Supports Mozilla/Firefox mouse wheel scrolling
 */
function MozillaScroll(evt) {
  if(thumbnails[currentThumb] && thumbnails[currentThumb].onWheel) {
    thumbnails[currentThumb].onWheel(evt);
  }

}

/**
 * Returns true if the additional empty series thumbnails
 * should be hidden.  This is a special mode for the case
 * where there is only 1 CCR in the viewer.
 */
function hideSeries() {
  return enableNoAttachmentsMode && (numberOfSeries() <= 1);
}

function adjustMainImageSize() {
  var availableHeight = window.parent.document.body.offsetHeight - window.parent.$('tabs').scrollHeight - 10;
  if(availableHeight > 750)
    mainImageHeight = 750
  else
    mainImageHeight = availableHeight;

  //var oldSize = elementDimensions('mainImage');
  //log("Adjusting size:  old height = " + oldSize.h + " new height = " + mainImageHeight);

  el('mainImage').style.height = mainImageHeight;
  if(thumbnails[currentThumb].resize)  {
    log("thumb has resize");
    thumbnails[currentThumb].resize();
  }
  else
    thumbnails[currentThumb].display();
  adjustContentAreaSize();
}

var hideDicomButtons = true;
var tou = null;

/**
 * Initializes the whole WADO viewer. Call this method once when the
 * page has loaded to initialize the main view.
 */
function Initialize() {

  /*if(showAttributions) {
    window.open('attributions.jsp','attributions','height=200,width=300,toolbar=no,menubar=no');
  }*/


  showStartupMessage();

  window.parent.addPatientHeader(getCcr());
  /*
  if(!window.parent.hasPatientHeader) {
    window.parent.addPatientHeader(getCcr());
  }
  */

  parent.currentTab.url = 'viewEditCCR.do';

  // Check for too small window and shrink as necessary
  window.onParentSize = function() { window.onParentSize =  adjustMainImageSize };
 
  if(hideSeries()) {
    hide( 'thumb1','thumb2','thumb3','mclogo','tools','pager','footer');
    el('thumb0').style.left=3;
    el('thumb0').style.backgroundColor='black';
    el('mainImage').style.left='200px';
  }

  if(hideDicomButtons) {
    hide('mclogo', 'tools','footer');
    el('thumb0').style.left=3;
    el('thumb1').style.left=3;
    el('thumb2').style.left=3;
    el('thumb3').style.left=3;
    el('mainImage').style.left='200px';
  }

  if((accId != '') && (accId != '0000000000000000') && (displayMode != "eccr")) {
    tools.push( ["Set as Emergency CCR", "setEmergencyCCR();"] );
  }

  addSectionMenus(tools);

  window.parent.setTools(tools);

  window.parent.enableTool(EDIT_AS_NEW_TOOL, ccr.storageMode == 'FIXED');

  // Mozilla scroll wheel support
  if (document.addEventListener) { // DOM Level 2 Event Model
    window.addEventListener("DOMMouseScroll", MozillaScroll, false);
  }

  initializeThumbnails();

  log("currentThumb="+currentThumb);
  
  if(currentThumb == 0) {
    thumbnails[0].displayThumbnail(0);
    updateActiveImage();
    displaySelectedSeries(0);
  }
  else
  if(currentSeries >= 0) {
    log('selecting series ' + currentSeries);
    selectSeries(p.StudiesArray[0].SeriesArray[currentSeries]); // Was set in URL.
    setZoomActive();
    displayCurrentImage();
  }

  currentImage = 0;
  updatePagerLinks();
  updateMenu();

  // If clicking in header, show CCR (thumbnail 0)
  withWindow(parent, function() {
      connect($('patientHeaderTable'),'onclick',function() {
        thumbnails[0].display(0);
      });
  });


  // If >= 4 series then show the pagers
  if(numberOfSeries() >= 4) 
    showPager();
  else 
    hidePager();

  var buttons =  [
		{ text: 'Edit',   action: 'editCCR()', tip: "Click to Edit this CCR"  }
  ];

  if (ccr.guid) {
  	var operatingSystem = clientOS();
  	if (operatingSystem == "Windows"){
  		var installed = isHealthBookInstalled();
  		var editURL = generateCCRExternaEditURL();
  		log ("edit url is " + editURL);
	   	if (installed == HEALTHBOOK_INSTALLED){
	   		buttons.push({ text: 'Edit With HealthBook',   
	   			action: 'editCCRExternally()', 
	   			tip: "Click to Edit this CCR with HealthBook" , 
	   			actionTarget: '_top',
	   			url: editURL  });
	  	}
	  	else if (installed == HEALTHBOOK_NOT_INSTALLED){
	  		buttons.push({ text: 'Install HealthBook',   
	  			action: 'installHealthBook()', 
	  			tip: "Click to install HealthBook editor" ,
	  			actionTarget: '_top'  });
	  		buttons.push({ text: 'Edit with HealthBook',   
	  			action: 'editCCRExternally()', 
	  			tip: "Click to Edit this CCR with HealthBook (if installed)",
	  			actionTarget: '_top',
	  			url: editURL  });
	  	}
	  	else{
	  		buttons.push({ text: 'Edit With HealthBook',   
	  			action: 'editCCRExternally()', 
	  			tip: "Click to Edit this CCR with HealthBook. \nIf HealthBook is not installed please download from http://www.medcommons.net/healthbook.php" ,
	  		  actionTarget: '_top',
	  		  url: editURL });
	  		//buttons.push({ text: 'Install HealthBook',   action: 'installHealthBook()', tip: "Click to install HealthBook editor" , actionTarget: '_top' } });
	  	}
  	}
  	// else - show nothing. We only have an editor for windows.
  }

  if(enableSimTrak)
    addSimTrakTools();
  
  parent.setButtons(buttons);

  if(editOnLoad =='true') {
    addReport();
  }

  if(cleanPatient) {
    cleanTabs();
  }
  fetchPatientCCRs();

  // POPS transfers show the quick reply button
  if(enableQuickReply) {
    visibility(parent.$('topcenterbuttons'),true);
    connect(window,'onunload',function(){visibility(parent.$('topcenterbuttons'),false)});
  }

  // Update parent window query fragment to restore this view
  updateFragment();
  
  // If CCR is incomplete, warn the user about that
  if(ccr.patient.status == 'INCOMPLETE') 
      warnIncompletePatient();
  
  visibility('ViewerArea',true);
}  

function initializeThumbnails() {

  // Always show thumbnail 0
  thumbnails[0].displayThumbnail(0);

  // Remaining thumbnails determined by current page
  for (i=1;i<4;i++) {
    var thumbnailNumber=currentThumbnailPage*thumbnailPageSize+i;

    if(thumbnails[thumbnailNumber]==null) {
      thumbnails[thumbnailNumber] = new BlankThumbnail();
    }
    thumbnails[thumbnailNumber].displayThumbnail(i);
  }

  if($('pagernumbers') != null) {
    $('pagernumbers').innerHTML=(currentThumbnailPage +1) + " of " + numberOfPages();
  }

  initializeMenuHighlight();
}

/**
 * Initializes the highlighting of items in the menu to ensure that
 * that the page of series being shown is highlighted in the menu
 */
function initializeMenuHighlight() {
  //var startHighlightIndex=currentThumbnailPage*thumbnailPageSize;
  //var endHighlightIndex=(currentThumbnailPage+1)*thumbnailPageSize;
  for(i=0;i<thumbnails.length;++i) {
    if($('thumbOption'+i)!=null) {
      if(isThumbnailVisible(i)) {
        $('thumbOption'+i).className='MenuHighlight';
      }
      else {
        $('thumbOption'+i).className='MenuItems';
      }
    }
  } 

  var addReportOption = $("addReportOption");
  if(disableAddReport) {
    addReportOption.className='MenuDisabled';
  }
}

/**
 * Returns the number of pages of thumbnails
 */
function numberOfPages() { 
 return Math.floor( (numberOfSeries()+1) / thumbnailPageSize + 1);
}


/**
 * Displays debug information about the studies in the WADO viewer.
 */
function displayStudyArrays() {
  var msg =p.StudiesArray[0].StudyDescription + "\n";
  msg += "Series in study:";
  msg+= p.StudiesArray[0].SeriesArray.length + ".";
  for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
    msg+="\n===";
    msg+=p.StudiesArray[0].SeriesArray[i].SeriesDescription;
    msg+="\n images: " ;
    msg+=p.StudiesArray[0].SeriesArray[i].InstanceArray.length;
    msg+= "\n";
    msg+=p.StudiesArray[0].SeriesArray[i].SeriesInstanceUID;
  }
  alert (msg);
}

function restoreMainImageHtml() {
  $('mainImage').innerHTML=
  '<div id="wadoImageArea" style="width:'+imageWidth+'px; height:'+imageHeight+'px;"><img id="wadoImage" name="image" src="blank.png" '
  + ' onload="imageLoadCompleted();" onabort="imageLoadAborted();" onerror="imageLoadError();" '
  + ' onmousedown="beginDrag(this,event);" onselect="return false;" /></div>';
  
  // HACK: IE has a strange issue with the z-index that causes the
  // drag scroll pain to render underneath the main image when it is
  // transparent (even thought it's z-index is higher).  To avoid this
  // we just force it 30px over
  if(YAHOO.env.ua.ie>0) {
    var img = $('mainImage');
    if(!img.shifted) {
      log("Shifting main image 30px right due to IE z-index bug");
      img.originalLeft = img.style.left;
      img.style.left=(20+parseInt(img.style.left,10))+'px';
      img.shifted = true;
    }
  }
}

function hideDragScrollPane() {
  $("dragScrollOuter").style.display='none';
  var img = $('mainImage');
  if(img.shifted) {
    log("unshifting main image 20px right due to IE z-index bug");
    img.style.left=(parseInt(img.style.left,10)-20)+'px';
    img.shifted = false;
  }
}

var currentImage = -1;
var currentSeries = -1;
var currentThumb = -1;

function displayCurrentImage() {

    var series = p.StudiesArray[0].SeriesArray[currentSeries];
    if(series.mimeType == 'application/dicom') {
      log("dicom");
      $("dragScrollOuter").style.display='block';
      // Make sure that the mainImage area has correct content
      if(!$('wadoImageArea')) {
        restoreMainImageHtml();
      }
      if (isImageLoaded() == false){
        log("not loaded");
        imagesSkipped++;
        //document.image.style.display='none';
        window.status="Skipping image " + imagesSkipped;
        return;
      }
      imageLoading = currentImage;
      
      var nImages = numberOfImageInSeries(currentSeries);
      window.status="Reference:" + currentSeries + ", image:" + (currentImage+1) + " / " + nImages;
      updateActiveImage();
      
      var imageURL = this.getImageUrl(currentSeries, currentImage);
      if(imageURL == null)
        return;

      var image= series.InstanceArray[currentImage];
      series.lastImageIndex = currentImage;
      if(image != null) {
        currentToolState.currentWindow = image.defaultWindow;
        currentToolState.currentLevel = image.defaultLevel;
      }

      setImage(imageURL);        
      updateActiveImage();
  }
  else {
    $("dragScrollOuter").style.display='none';
    // Hack - non-dicom series only allowed to show 1st image!!!
    // p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[0].display();
    thumbnails[currentThumb].display();
  }
  adjustContentAreaSize();
}

/**
 * Returns a URL for the given series and image
 */
function getImageUrl(urlSeries,urlImage) {
     var image= p.StudiesArray[0].SeriesArray[urlSeries].InstanceArray[urlImage];
     if(image==null) {
       alert("Series " + urlSeries + " image " + urlImage + " is null");
       return null;
     }
     if(image.FileReferenceID==null){
       alert("Series " + urlSeries + " image " + urlImage + " FileReferenceID is null");
       return null;
     }

     var imageURL = "wado/" + p.StudiesArray[0].SeriesArray[currentSeries].storageId + "?"; 
     imageURL += queryString( {
       studyUID: p.StudiesArray[0].StudyInstanceUID,
       fname: image.FileReferenceID,
       patientName: p.PatientName,
       mcGUID: p.StudiesArray[0].SeriesArray[currentSeries].mcGUID
     });
     
     if (currentToolState.currentAnnotation==undefined){
     	currentToolState.currentAnnotation = allAnnotation;
     }
     imageURL+=currentToolState.currentAnnotation;

     if(currentToolState.showImagesFullSize == false){
       imageURL+="&maxRows=";
       imageURL+=maxRows;
       imageURL+="&maxColumns=";
       imageURL+=maxColumns;
     }
     imageURL+="&imageQuality=";
     imageURL+=90;
     if(fast==true)
       imageURL+="&interpolation=FAST";
     if (currentToolState.currentRegion != null){
      imageURL+=currentToolState.currentRegion;
     }

    if (currentToolState.currentWindowLevel == null){
      imageURL+="&windowWidth=";
      imageURL+=image.defaultWindow;
      imageURL+="&windowCenter=";
      imageURL+=image.defaultLevel;
    
      // ssadedin - 10/22/04: commented out because it causes incorrect results since this
      // function is called via the caching code as well.
      // See related issue (#269).
      // currentToolState.currentWindow = image.defaultWindow;
      // currentToolState.currentLevel = image.defaultLevel;
    }
    else {
      imageURL+=currentToolState.currentWindowLevel;
    }
    return imageURL;
}

/**
 * Calculates the zoom magnitude based on the given coordinates
 */
function calculateZoomMagnitude(startX, startY, endX, endY) {
    startX = startX - $('mainImage').offsetLeft;
    startY = startY - $('mainImage').offsetTop;
    endX = endX - $('mainImage').offsetLeft;
    endY = endY - $('mainImage').offsetTop;

    //alert("zoom region:" + startX + "," + startY + "," + endX + "," + endY + ",w=" + imageWidth  + " h=" + imageHeight);
    window.status='End drag at Y='+endY; 

    var x1 = (1.0 * startX)/(1.0 * imageWidth);
    var y1 = (1.0 * startY)/(1.0 * imageHeight);
    var x2 = (1.0 *  endX)/(1.0 * imageWidth);
    var y2 = (1.0 * endY)/ (1.0 * imageHeight);
    
    //alert("zoom region:" + x1 + "," + y1 + "," + x2 + "," + y2 + ",w=" + imageWidth  + " h=" + imageHeight);
    
    if (x1 > x2){
      var temp;
      temp = x1; x1=x2; x2=temp;
    }
    if (y1 > y2){
      var temp;
      temp = y1; y1=y2; y2=temp;
    }
    return Math.min((x2-x1),(y2-y1));
}

function handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight){

  startX = startX - $('mainImage').offsetLeft;
  startY = startY - $('mainImage').offsetTop;
  endX = endX - $('mainImage').offsetLeft;
  endY = endY - $('mainImage').offsetTop;

  //alert("zoom region:" + startX + "," + startY + "," + endX + "," + endY + ",w=" + imageWidth  + " h=" + imageHeight);
  window.status='End drag at Y='+endY; 

  var x1 = (1.0 * startX)/(1.0 * imageWidth);
  var y1 = (1.0 * startY)/(1.0 * imageHeight);
  var x2 = (1.0 *  endX)/(1.0 * imageWidth);
  var y2 = (1.0 * endY)/ (1.0 * imageHeight);
  
  //alert("zoom region:" + x1 + "," + y1 + "," + x2 + "," + y2 + ",w=" + imageWidth  + " h=" + imageHeight);
  
  if (x1 > x2){
    var temp;
    temp = x1; x1=x2; x2=temp;
  }
  if (y1 > y2){
    var temp;
    temp = y1; y1=y2; y2=temp;
  }

  // Here we used to modify the zoom level if it was too great.
  // This, however, proved confusing and allowed rogue gestures
  // to do confusing things.  Instead, we now throw an exception 
  // which aborts the operation underway.

  log("Zoom X=" + (x2-x1) + " Y=" + (y2-y1));

  if(((x2-x1) < maximumZoom) || ((y2-y1) < maximumZoom)) {
    log("Zoom too large.");
    throw zoomTooLargeError;
  }
  
/*    // Set min zoom size (10:1)
    // If the zoom is greater than 10:1
    // in either dimension then expand
    // region from center unless region is
    // near edge of image.
    if ((x2-x1) < .1){
      if (x1 <.1)
        x2 = x1 + .1;
      else if (x2>.9)
        x1 = x2 - .1;
      else{
        var diff = .1 - (x2-x1);
        x1-=(diff/2.0);
        x2+=(diff/2.0);
      }
    }
      
    if ((y2-y1) < .1){
      if (y1 <.1)
        y2 = y1 + .1;
      else if (y2>.9)
        y1 = y2 - .1;
      else{
        var diff = .1 - (y2-y1);
        y1-=(diff/2.0);
        y2+=(diff/2.0);
      }
        
    }
*/
  // Shouldn't happen - but it appears that drag event can let loose outside of 
  // image region.
  if (x2 > 1) x2 = 1.0;
  if (y2 > 1) y2 = 1.0;
  if (x1 < 0) x1 = 0.0;
  if (y1 < 0) y1 = 0.0;
    
  currentToolState.currentRegion="&region=";
  currentToolState.currentRegion+=x1; currentToolState.currentRegion+=",";
  currentToolState.currentRegion+=y1; currentToolState.currentRegion+=",";
  currentToolState.currentRegion+=x2; currentToolState.currentRegion+=",";
  currentToolState.currentRegion+=y2; 
   
  currentToolState.regionX1 = x1;
  currentToolState.regionY1 = y1;
  currentToolState.regionX2 = x2;
  currentToolState.regionY2 = y2;

  imageCache.reset();
  displayCurrentImage();
}
 
 // Image Loading Routers
 
 // Invoked each time that an image has been loaded to set
 // the imageLoaded flag to 'true'.
 function imageLoadCompleted(){
  //window.status = "Image Loaded.";
   imageLoaded = true;
  log("Image " + imageLoading + " loaded");
  if((imageLoading != currentImage) && (imageLoading != -1)) {
    var difference = currentImage - imageLoading;
    displayCurrentImage();   
    window.status = "Caught up " + difference + " images";
  }
  else {
    // Main image is loaded - start a timer to begin caching
    // forward and backward. We do not start caching immediately
    // so as to limit performance impact if the user is scrolling
    // (both on server and client side).
    if(thumbnails[currentThumb].series.mimeType != "application/x-ccr+xml") {
      log("Initiating caching for thumb index " + currentThumb + " mimeType=" +  thumbnails[currentThumb].series.mimeType );
      window.setTimeout(cacheNextImage,300);
    }
  }
 }

/**
 * Called on timeout to start the caching process which caches
 * images forward and backward from the one currently being viewed.
 */
var synchronizeThumbnailTimerID=0;
function cacheNextImage() {
  if(imageLoaded && thumbnails[currentThumb].enableImageCache) {
    //log("initiating caching ...");
    imageCache.cacheNext(); 

    // Set a timeout to synchronize the thumbnail image
    // with the main image. We leave it a few seconds since
    // we only want it to happen if the user is sitting idle
    // on a particular image for a long time.
    if(synchronizeThumbnailTimerID!=0) {
      window.clearTimeout(synchronizeThumbnailTimerID);
    }
    synchronizeThumbnailTimerID = window.setTimeout(synchronizeThumbnail, 2000);
  }
  else {
    //log("not initiating caching: new image load started.");
    //log("Hiding cache pane");
    //ImageCacheHideCachedPane();
  }
}
 
function imageLoadAborted(){
  imageLoaded = true;
}

function imageLoadError(){
  imageLoaded = true;
}

/**
 * Returns true if the image has completely loaded, false otherwise.
 */
function isImageLoaded(){

  if(FormType == OrderFormType)
    return true;
 
   if(imageLoaded)
    return true;

  var wadoImage = $("wadoImage");
  if((wadoImage != null) && wadoImage.complete)
    return true;

  return false;
}
 
function synchronizeThumbnail() {
  //log("Synchronizing thumbnail to " + currentImage + " currentThumb="+currentThumb);
  thumbnails[currentThumb].displayThumbnail(getThumbCellIndex(currentThumb));
}

/**
 * Sets the image src attribute and sets the imageLoaded flag to false.
 */
function setImage(url){
  if (document.image) {
    log("Setting url=["+url+"]");
    setCookie("priority","high");  // main image always has high priority
    document.image.src=url;
 }
 imageLoaded = false;
}

/**
 * Returns direction of mousewheel. 1 is forward, -1 is backward, 0 is nothing.
 */
function mouseWheelClick(){   
  var clickDirection = 0;
        if (event.wheelDelta >= 120)
                clickDirection=-1;
        else if (event.wheelDelta <= -120)
                clickDirection=1;
   return clickDirection; 
}

// Right now just reset things.
function handleDoubleClick(){
  resetViewer();
}

// Sets the global window/level values.
function setWindowLevel(window, level){
  currentToolState.currentWindowLevel = "&windowWidth=";
  currentToolState.currentWindowLevel +=window;
  currentToolState.currentWindowLevel +="&windowCenter=";
  currentToolState.currentWindowLevel +=level;
  currentToolState.currentWindow = window;
  currentToolState.currentLevel = level;
}

function generateWindowLevelGrid(){
  var seedWindow = currentToolState.currentWindow;
  var seedLevel = currentToolState.currentLevel;
  
  generateWindowLevelGridArray(seedWindow, seedLevel);
  currentToolState.activeTool= toolWLSelect;
  
}
function generateWindowLevelGridArray(seedWindow, seedLevel){
  var windowArray = new Array(9);
  var levelArray = new Array(9);
  var window = parseInt(seedWindow);
  var level = parseInt(seedLevel);
  // Note that middle element (i==4)
  // is the seed window/level value.
  for (var i=0;i<9;i++){
    windowArray[i] = window - ((4-i) * 30);
    levelArray[i] = level - ((4-i) * 40);
  }
  currentToolState.currentWindowLevel = "&windowLevelGrid=";
  for (var i=0;i<9;i++){
    currentToolState.currentWindowLevel +=windowArray[i];
    currentToolState.currentWindowLevel +=",";
    currentToolState.currentWindowLevel +=levelArray[i];
    if (i !=8)
      currentToolState.currentWindowLevel+= ","
  }
  currentToolState.currentWindowArray = windowArray;
  currentToolState.currentLevelArray = levelArray;
  
  
}

function resetSeriesChange(){
  currentToolState.currentRegion = null;
  currentToolState.currentAnnotation=this.currentAnnotation;
  currentToolState.currentWindowLevel = null;
  currentToolState.zoomed = false;
  imageLoaded = true;
  moveThumbnailRect(-100, -100);
  log("Finding thumbnail for series " + currentSeries);
  currentThumb = thumbnailIndexFromSeries(p.StudiesArray[0].SeriesArray[currentSeries]);
  this.imageCache.reset();
  $("dragScrollLabel").style.display='block';
  $("dragScrollTotalImages").innerHTML=
    numberOfImageInSeries(currentSeries) + ' Images';

  if(numberOfImageInSeries(currentSeries) > 1) {
    window.setTimeout(hideSeriesDragIcon,3000);
  }
}

function hideSeriesDragIcon() {
 	$("dragScrollLabel").style.display='none';
  
}

function resetViewer(){
  resetSeriesChange();
  fast= false;
  displayCurrentImage();
  
}
// *** WARNING - These need to be derived dynamically - 
// if the screen dimentions change all overlays will be
// wrong.
  //var seriesThumbnailY = 744;
  //var seriesThumbnailX = 140 + 8;
  //var seriesBorderSize = 5;

  var seriesXSize = 140;
  var seriesYSize = 140;

/**
 * Updates the Zoom Rectangle position to reflect the given coordinates.
 * The coordinates (thumbX,thumbY) are specified relative to the top level 
 * window.  The function calculates the correct position of the zoom
 * rectangle within the appropriate thumbnail based on the currently
 * active thumbnail.
 */
function updateZoomRectangle(thumbX, thumbY){

  // Get the pixel offsets from the top left corner of the thumbnail image
  var thumbCellIndex = getThumbCellIndex(currentThumb);
  var offsetX = thumbX - findPosX($('thumb'+thumbCellIndex));
  var offsetY = thumbY - findPosY($('thumb'+thumbCellIndex));

  // Convert the offset to relative form
  var regionX1 = offsetX/(seriesXSize * 1.0);
  var regionY1 = offsetY/(seriesYSize * 1.0);
    
  if (regionX1 > 1)
    alert("RegionX1=" + regionX1);
  if (regionY1 > 1)
    alert("RegionY1=" + regionY1);

  var regionDeltaX =  regionX1 - currentToolState.regionX1;
  var regionDeltaY =  regionY1 - currentToolState.regionY1;
  var x1 = regionX1 * imageWidth + $('mainImage').offsetLeft;

  // Just modified. 
  var x2 = (currentToolState.regionX2 + regionDeltaX) * imageWidth 
    + $('mainImage').offsetLeft;
  var y1 = regionY1 * imageHeight;
  var y2 = (currentToolState.regionY2 + regionDeltaY) * imageHeight;
  
  handleZoomRegion(x1,y1,x2,y2, imageWidth, imageHeight);
  setThumbnailRectangleOnSeries();
}

function setThumbnailRectangleOnSeries(){

  var offsetY = (seriesYSize * currentToolState.regionY1);
  var offsetX = (seriesXSize * currentToolState.regionX1);
  
  var thumbW = (currentToolState.regionX2 - currentToolState.regionX1) *
    seriesXSize;
  var thumbH = (currentToolState.regionY2 - currentToolState.regionY1) *
    seriesYSize;
    
  if (thumbW < 0) thumbW = -thumbW;
  if (thumbH < 0) thumbH = -thumbH;

  var thumbCellName = "thumb" + getThumbCellIndex(currentThumb); // (((currentThumb-1) % thumbnailPageSize) + 1);
  var thumbY = findPosY($(thumbCellName)) + offsetY;
  var thumbX = findPosX($(thumbCellName)) + offsetX;

  setThumbnailRectSize(thumbW, thumbH);
  moveThumbnailRect(thumbX, thumbY);  
}

/**
 * This flag is used to disable the capture of keyboard events. It is needed
 * because the default handling captures things like arrow keys which 
 * are wanted captured in some scenarios (eg. WADO image) but are 
 * /not/ wanted captured in others (eg. user trying to select something in
 * a dropdown. To deal with this, the capturing can be toggled on and off
 * with this flag, depending what mode the viewer is in.
 */
var disableKeyCapture = false;

/**
 * Handles the 'onKeyPress' event.  The reason that this is required is that Mozilla
 * seems to have a bug(?) whereby the event cannot be canceled in the onKeyDown
 * event.  On the other hand, IE requires processing to be done in the onKeyDown,
 * so we handle both events, but only cancel in here.
 */
function handleKeyPress(event) {
  if(disableKeyCapture)
    return;

  if (!event) event= window.event; // IE
    var keyCode =
      document.layers ? event.which :
      document.all ? event.keyCode :
      $ ? event.keyCode : 0;

    if (keyCode == 38) {
      cancelEventBubble(event);
    }
    else 
    if (keyCode == 40) {
      cancelEventBubble(event);
    }
}

/**
 * Handles the 'onKeyDown' event.  Checks if one of the arrow keys has
 * been pressed and scrolls the view accordingly.
 */
function handleKeyDown(event) {
  if(disableKeyCapture)
    return;

  if (!event) event= window.event; // IE
    var keyCode =
      document.layers ? event.which :
      document.all ? event.keyCode :
      $ ? event.keyCode : 0;

    if (keyCode == 38) {
      window.status = "-1";
      handleMousewheel(-1);
      cancelEventBubble(event);
    }
    else if (keyCode == 40) {
      window.status = "1";
      handleMousewheel(1);
      if(event.stopPropagation)
         event.stopPropagation(); // DOM Level 2
      if(event.preventDefault)
         event.preventDefault(); // DOM Level 2
      cancelEventBubble(event);
    }
    else if (keyCode == 65) {
      window.status = "A";
      imageLoaded=true;
      updateActiveImage();
    }
} 
    
function captureMousewheel(){
  var n = mouseWheelClick();
  //alert("mousewheel n=" + n);
  if (n != 0 )
    handleMousewheel(n);
  return false;
}

/*
Annotation states:
localizerAnnotation
allAnnotation 
noAnnotation1 
noAnnotation2 
*/
function toggleOverlay(){
  //alert('Overlay toggle' + currentToolState.currentAnnotation);
  if (currentToolState.currentAnnotation==allAnnotation) {
    currentToolState.currentAnnotation = noAnnotation1;
  }
  else if (currentToolState.currentAnnotation==noAnnotation1) {
    currentToolState.currentAnnotation = localizerAnnotation;
  }
  else if (currentToolState.currentAnnotation==localizerAnnotation) {
    currentToolState.currentAnnotation = noAnnotation2;
  }
  else if (currentToolState.currentAnnotation==noAnnotation2) {
    currentToolState.currentAnnotation = allAnnotation;
  }
  else {
    currentToolState.currentAnnotation=allAnnotation;
  }
  //alert('new overlay setting ' + currentToolState.currentAnnotation);
    
  displayCurrentImage();  
}
function toggleFullSize(){
  currentToolState.showImagesFullSize = !currentToolState.showImagesFullSize;
  displayCurrentImage();
  
}

// Handler for the menu on the bottom of the page.
function menuSelect(selection){
  var value = parseInt(selection);


  if(selection == "")
    return;
  
  //alert ("selection=" + selection + ", value=" + value + " FormType=" + FormType);
  if (selection=="ABOUT") {
    alert('MedCommons Demo System \r\nVersion ' + version + '\r\nBuild Date  ' 
      + buildDate + '\r\n\r\nCopyright ? 2004 MedCommons, Inc.\r\n52 Marshall St.\r\nWatertown MA 02472\r\nUSA');
  }
  else if (selection=="HELP") {
    //help = window.open('manual.php.htm','Help','scrollbars=1,width=720,height=550,resizable=1');
    showViewerHelp();
  }
  else if (selection=="addReport") { // User chose to "Add Report"
    if(!disableAddReport) {
      addReport();
    }
  }
  else if ((FormType != null) && (FormType == WADOFormType)) {
    //alert("Showing series " + value);
    log("Showing series " + value);
    displaySelectedSeries(value);
  }
}

function displayHIPAALog(trackingNumber){
  //location.replace("/router/HipaaStaticLogPage.html");
  window.open("/router/my_account.jsp","mcwindow");
  // location.replace("/router/my_account.php.htm");
}

// Moves thumbnail rectangle on screen to specified
// absolute coordinates.
function moveThumbnailRect(x, y){
  var element = $("zoomMap");
  if (element != null){
    element.style.left = x + "px";
    element.style.top  = y + "px";
  }
}

// Sets size of thumbnail rect
function setThumbnailRectSize(width, height){
  var element = $("zoomRectangle");
  element.width = width;
  element.height = height;

}

function replaceText(n, newText) {
  if (n == null){
    // TODO
    // Apparently this routine is being invoked
    // on nodes that have not been defined or are offscreen
    // (> 3 series in a study). This throws an error and
    // prevents the WADO viewer from rendering the study.
    //alert("n is null, Ignoring " + newText);
    return;
  }
    if (n.nodeType == 3 /*Node.TEXT_NODE*/) {

        var newNode = document.createTextNode(newText);
        var parent = n.parentNode;
        parent.replaceChild(newNode, n);
    }
    else {
        // If the node was not a Text node, loop through its children,
        // and recursively call this function on each child.
        var kids = n.childNodes;
        for(var i = 0; i < kids.length; i++) replaceText(kids[i], newText);
    }
}

function getAbsolutePosition(name){
  var obj = $(name).style;
  //alert("obj: " + obj + ",obj.left=" + obj.left);
  
}

function showToolState(){
  var stateString = "Current Tool State ";
  stateString+="\n Active tool = ";
  stateString+=currentToolState.activeTool;
  
  stateString+= "\n ImageLoaded = ";
  stateString+= imageLoaded;
  
  stateString+="\n  current region= ";
  stateString+=currentToolState.currentRegion;
  
  stateString+="\n  currentAnnotation = ";
  stateString+=currentToolState.currentAnnotation;
  
  stateString+="\n  current window level = ";
  stateString+=currentToolState.currentWindowLevel;
  
  stateString+="\n  currentWindow = ";
  stateString+=currentToolState.currentWindow;
  
  stateString+="\n  currentLevel = ";
  stateString+=currentToolState.currentLevel;
  
  stateString+="\n showImagesFullSize = ";
  stateString+=currentToolState.showImagesFullSize;

  alert(stateString);
  
  getAbsolutePosition("series1");
}
  
  
function exitScreen(){
  window.close();
}


/* ----------------------------  Paging Functions ------------------------------- */

var currentThumbnailPage=0;
var thumbnailPageSize=3;

/**
 * Shows the pager buttons
 * Disabled for now:  we have disabled the pager buttons altogether.
 */
function showPager() {
  //$("pageuparrow").style.display='block';
  //$("pagedownarrow").style.display='block';
}

/*
 * Disabled for now:  we have disabled the pager buttons altogether.
 */
function hidePager() {
  //$("pagerlabel").style.display='none';
  //$("pageuparrow").style.display='none';
  //$("pagedownarrow").style.display='none';
  //$("pager").style.display='none';
}

/**
 * Causes the thumbnail strip to page backward 1 page
 */
function gotoPreviousPage() {
  if(currentThumbnailPage>0) {
    currentThumbnailPage--;
    initializeThumbnails();
    updateActiveImage();
    // TODO: really should hide/restore if the user is paging back and forward
    // for now just hide permanently if they change pages
    moveThumbnailRect(-100,-100);
  }
}

/**
 * Causes the thumbnail strip to page forward 1 page
 */
function gotoNextPage() {
  if(currentThumbnailPage+1 < numberOfPages()) {
    currentThumbnailPage++;
    initializeThumbnails();
    updateActiveImage();
    // TODO: really should hide/restore if the user is paging back and forward
    // for now just hide permanently if they change pages
    moveThumbnailRect(-100,-100);
  }
}

var scrollDragInProgress=false;

function rollOverDragScroll() {
  $("dragScrollLabel").style.display='none';
}

function rollOutDragScroll() {
  scrollDragInProgress=false;
}

var dragScrollBeginX = -1;
var dragScrollBeginY = -1;

function dragScrollPaneMouseDown(evt) {
  if(!evt) 
    evt= window.event; // IE

  var dragHandler = new DragHandler(evt);
  dragHandler.handleMove = updateDragScrollDisplay;
  dragHandler.handleUp = dragScrollPaneMouseUp;

  var dragScrollElement=$("dragScrollDisplay");
  dragScrollBeginX = evt.clientX + 25;
  dragScrollBeginY = evt.clientY;
  updateDragScrollDisplay(evt);
  dragScrollElement.style.display='block';
  $("dragScrollImageNum").style.display='block';
  log("dragScrollPaneMouseDown at "+ evt.clientY);
}

function updateDragScrollDisplay(evt) {
  var dragScrollOuter = $("dragScrollOuter");
  var dragScrollOuterX = findPosX(dragScrollOuter);
  var dragScrollOuterY = findPosY(dragScrollOuter);
  var startX = dragScrollBeginX - dragScrollOuterX -15;
  var startY = dragScrollBeginY - dragScrollOuterY;
  var endY = evt.clientY - dragScrollOuterY;
  var endX = startX-15;
  var dragScrollElement=$("dragScrollDisplay");

  dragScrollElement.style.zIndex='30';
  dragScrollElement.style.top=Math.min(startY,endY);
  dragScrollElement.style.left=Math.min(startX,endX);
  dragScrollElement.style.width=Math.abs(endX-startX);
  dragScrollElement.style.height=Math.abs(endY-startY);
  dragScrollElement.style.borderStyle='dotted';
  
  var dragScrollImageNum=$("dragScrollImageNum");
  dragScrollImageNum.innerHTML=''+(getNewImageFromDelta(evt)+1);
  dragScrollImageNum.style.left=Math.min(startX,endX);
  dragScrollImageNum.style.top=Math.min(startY,endY)-10;
}

function dragScrollPaneMouseUp(evt) {
  var newImageNum=getNewImageFromDelta(evt);
  scrollDragInProgress=false;
  var dragScrollElement=$("dragScrollDisplay");
  dragScrollElement.style.display='none';
  $("dragScrollImageNum").style.display='none';
  currentImage=newImageNum;
  displayCurrentImage();
  log("Scroll drag moved to " + newImageNum);
}

function getNewImageFromDelta(evt) {
  var ySize = $("dragScrollOuter").offsetHeight;
  var yPosDelta = evt.clientY - dragScrollBeginY;    
  var totalImages = numberOfImageInSeries(currentSeries); 
  var imageDelta =  Math.round((yPosDelta/ySize) * totalImages);
  var newImage = currentImage + imageDelta
  if(newImage < 0)
    newImage =0;
  if(newImage >= totalImages) {
    newImage = newImage % totalImages;
  }
  return newImage;
}


/* ----------------------------  XDS Functions ------------------------------------ */

function addReport() {
  // Show the add report form
  disableKeyCapture=true; // Turn off WADO key capturing
  $("xdsAddReportForm").style.display='block';
  $("selectionMenu").selectedIndex=0;
  $("addReportForm").codeType.focus();
  $("addReportForm").purposeText.value='';
}

/**
 * Called when the XDS document frame is loaded.
 */
var isSubmitting = false;
var disableAddReport=false;

function xdsLoaded() {
  if(isSubmitting) {  // Submit must be finished.
    $('xdsPleaseWait').style.display='none';
  }
  else {
    // Add display icons for the new series added
    log("Adding new CCR to series array");

    p.StudiesArray[0].SeriesArray.splice(
      0,0,new Series("MedCommons Document*", "new-cda", "new-cda", "", -1)); 
    p.StudiesArray[0].SeriesArray[0].InstanceArray[0]=
      new Instance( "new-cda-instance", "0", "showReport.jsp?stylesheet=cda2htm&source=newCda", 0, 0);
      p.StudiesArray[0].SeriesArray[0].mimeType="text/x-cdar1+xml";
    thumbnails.splice(1,0, new DocumentThumbnail(p.StudiesArray[0].SeriesArray[0])); 

    p.StudiesArray[0].SeriesArray.splice(
      0,0,new Series("Continuity of Care Record*", "new-ccr", "new-ccr", "", -2)); 
    p.StudiesArray[0].SeriesArray[0].InstanceArray[0]=
      new Instance( "new-ccr-instance", "0", "showReport.jsp?stylesheet=ccr2htm&source=newCcr", 0, 0);
    p.StudiesArray[0].SeriesArray[0].mimeType="application/x-ccr+xml";
    thumbnails.splice(1,0, new DocumentThumbnail(p.StudiesArray[0].SeriesArray[0]));

    // Show the additional pager links
    updatePagerLinks();


    initializeThumbnails();

    disableAddReport = true;
    initializeMenuHighlight();
    
    showXdsSubmitButton();    
  }
}

function hideXdsSubmitButton() {
  $("xdsSubmit").style.display='none';
}

function showXdsSubmitButton() {
  $("xdsSubmit").style.display='block';
}

function submitXdsDocument() {
  // Just submit the form
  $("attachSessionCcrDoc").value='newCcr';
  $("attachSessionCdaDoc").value='newCda';
  var xdsForm=$("xdsDocumentSubmissionForm");
  xdsForm.sourcePatientId.value=ccr.patientId;
  xdsForm.target='orderWindow';
  xdsForm.forward.value='xdsResult.jsp';

  log("Submitting XDS Form for patient " + ccr.patientId);
  var mainImageDiv = $('mainImage');  
  log("Displaying please wait...");
  $('xdsPleaseWait').style.display='block';
  isSubmitting=true;
  xdsForm.submit();
  hideXdsSubmitButton();
}

function showNotificationForm() {
  window.parent.currentTab.mode = 'edit';
  document.location.href='updateCcr.do?ccrIndex='+window.parent.currentTab.ccrIndex+'&forward=transaction&mode=edit';
}

function showFolderView() {
  document.location.href="/router/XdsFolders.jsp";
}

function showViewerHelp() {
  open('help/medhelp/index.htm', 'mchelp','scrollbars=1,width=570,height=635'); 
}

function printWindow() {
  orderWindow.focus();
  window.orderWindow.print();
}

/* ----------------------------  CCR Editing Functions ------------------------------- */

function validateSelectedSeries(index) {

  // If they are viewing a different thumbnail to the one that
  // they clicked the validation for then just display it
  // so they have the right one selected
  // The user will have to click again to actually validate it.
  if((typeof index != "undefined") && (index != currentThumb)) {
    displaySelectedThumbnail(index);
    return;
  }

  var ccrIndex = window.parent.currentTab.ccrIndex;
  var seriesIndex = thumbnails[currentThumb].series.index;

  // Can't modify fixed CCRs - make them open a new one
  if(ccr.storageMode == 'FIXED') {
    alert("This CCR has already been saved and cannot be modified.\r\n\r\nPlease select 'Edit as New CCR' to create a New CCR before Confirming");
    return;
  }

  if(ccr.guid && ccr.guid != '') {
    execJSONRequest('newTrackingNumber.do?ccrIndex='+ccrIndex,null,function(res) {
        window.parent.currentTab.ccrIndex = res.ccrIndex;
        setModified();
        ccr.guid = null;
        validateSelectedSeries();
    });
    return;
  }
  
  log("validating current series " + seriesIndex);
  execJSONRequest("ValidateSeries.action", queryString({ccrIndex: window.parent.currentTab.ccrIndex, seriesIndex: seriesIndex}), 
      function validateSelectedSeriesSuccess(obj) {
        try {
          if(obj.status == "ok") {
            setUnmodified();
            ccr.guid = obj.guid;
            thumbnails[currentThumb].series.validationRequired=false;
            el("thumbAnnotation"+getThumbCellIndex(currentThumb)).src='images/validation_success.png';
            enableValidationTools(false);
          }
          else {
            alert("A problem occurred confirming the attachment you selected:\n\n"+obj.error);
          }
        }
        catch(e) {
          alert(e);
        }
      }); 
}

function discardSeriesSuccess(req) {
  
  try {
    if(req.responseText.match(/value="ok"/)) {
      thumbnails[currentThumb].series.validationRequired=false;
      // el("thumbAnnotation"+getThumbCellIndex(currentThumb)).src='images/validation_discarded.png';
      enableValidationTools(false);
      setModified();
      window.location.href='viewEditCCR.do?mode=view&ccrIndex='+parent.currentTab.ccrIndex;
    }
    else {
      alert("An error occurred while discarding the series:\r\n\r\n" +
        req.responseText);
    }
  }
  catch(e) {
    alert(e.message);
  }
}

function discardSeries() {
  var seriesIndex = thumbnails[currentThumb].series.index;
  log("discarding current series " + seriesIndex);
  var deferred = doSimpleXMLHttpRequest("RemoveSeries.action", 
      {ccrIndex:  window.parent.currentTab.ccrIndex, seriesIndex: seriesIndex, displayIndex: false});
  deferred.addCallbacks(discardSeriesSuccess, genericErrorHandler);
}

function setEmergencyCCR() {

  if( (!ccr.guid) || (ccr.guid == '')) {
    alert('Before you can set this CCR as your Emergency CCR you must Save it.\r\n\r\n'
         +'To allow you to do this the display will change to Edit mode');
    editCCR();
    return;
  }
  
  var seriesIndex = thumbnails[currentThumb].series.index;
  log("setting current CCR as emergency ccr");
  var ccrIndex = window.parent.currentTab.ccrIndex; 
  execJSONRequest("SetEmergencyCCR.action", {ccrIndex: ccrIndex}, function(result) {
      window.parent.replaceTab('Emergency CCR','viewEditCCR.do?ccrIndex='+(result.ccrIndex),'',window.parent.nextTab());
      alert('The current CCR has been set as your Emergency CCR');
  });
}

function createCCR() {
  document.location.href='validateCcr.do';
}

function editCCR() {
  showNotificationForm();
}

function createReplyCCR() {
  var ccrIndex = window.parent.currentTab.ccrIndex; 
  var url='createReplyCCR.do?mode=edit&ccrIndex='+ccrIndex;
  var tab = window.parent.addTab("Reply CCR *",url,"Reply CCR",window.parent.currentTab);
  window.parent.showTab(tab)
}

function discardCCR() {
  document.location.href='discardCcr.do';
}

function downloadCCR() {
  var ccrIndex = window.parent.currentTab.ccrIndex; 
  document.location.href='DownloadCCR.action?ccrIndex='+ccrIndex;
}

function editAsNew() {
  if(!checkNewCCR()) 
    return;

  // Create a new tab
  var tab = window.parent.addTab('New CCR','viewEditCCR.do','',window.parent.nextTab());
  tab.ccr = { documentType: 'NEWCCR' };
  var url ='EditAsNew.action?mode=view&ccrIndex='+window.parent.currentTab.ccrIndex;
  window.parent.highlightTab(tab);
  document.location.href=url;
}

/* ----------------------------  Utility Functions ------------------------------- */

/**
 * returns the index of the given series in the series array
 */
function getSeriesIndex(aSeries) {
  for(var i=0; i<p.StudiesArray[0].SeriesArray.length;++i) {
    if(p.StudiesArray[0].SeriesArray[i] == aSeries) {
      return i;
    }
  }
  return -1;
}

/**
 * Sets the HTML content for the element identified by the given id
 */
function setHtml(id, content) {
  //log("setHtml(" + id  + "," + content + ")");
  $(id).innerHTML=content;
}

function getAvailableContentWidth() {
  // how much screen space there is
  var leftWidth = 250;
  if(hideSeries() || hideDicomButtons)
    leftWidth = 200;

  var width = (window.document.body.offsetWidth - leftWidth) - 5;
  return width;
}

function getAvailableContentHeight() {
  // var height =  window.document.body.offsetHeight - 30;
  return viewportSize().h - 10;
}

function adjustContentAreaSize() {
  if($('orderWindow')) {
    $('mainImage').style.height=$('orderWindow').height + 'px';
    $('mainImage').style.width=$('orderWindow').width + 'px';
  }
  else {
    $('mainImage').style.height='750px';
    $('mainImage').style.width='750px';
  }
}

function highlightSection(s) {

  // Make sure the CCR is visible in the order window
  if(!$('ccrflag')) {
    connect(window, 'ccrloaded',partial(highlightSection,s));
    displaySelectedSeries(0);
    return;
  }

  disconnectAll(window,'ccrloaded');

  var secId = removeSpaces(s.toLowerCase());
  if(orderWindow.document.getElementById(secId)) {
    window.orderWindow.setTimeout(
      "window.scrollTo(0,window.parent.findPosY(document.getElementById('"+secId+"'))-16)",0);
  }
}

function doPayment(series, callback) {
  execJSONRequest('Billing.action?charge',{ ccrIndex: parent.currentTab.ccrIndex, type: 'INBOUND_FAX', seriesIndex: series.index, count: series.billingEvent.quantity }, function(r) {
      if(r.status == 'ok') {
        series.paymentRequired = false;
        var i = 0;
        forEach(thumbnails, function(t) { if(t.series==series) t.displayThumbnail(i);  ++i;});
        callback();
      }
      else
      if(r.credit == 'insufficient') {
        window.result = r;
        noCreditDlg(series.billingEvent, r.counters, function() { /*alert('ok');*/ });
      }
      else {
        alert('A problem occurred while charging your account:\n\n'+r.error);
      }
  });
}

/**
 * Returns a CCR object with attributes reprenting the primary
 * CCR currently active (as opposed to other CCRs which might
 * be attached as references.)
 */
function getCcr() {
  return ccrs[p.StudiesArray[0].SeriesArray[0].mcGUID];
}

function getStorageId(){
	return(storageId);
}
