/**
 * Copyright 2004-2010 MedCommons Inc.   All Rights Reserved.
 */
/********************************************************************************************
 * MedCommons HealthURL Viewer
 * 
 * This file contain the core of the MedCommons Viewer.   This code manages the display 
 * of DICOM images from a CCR, including a set of "thumbnails" associated with the 
 * DICOM and other attachments referenced in the CCR.   This code
 * doesn't directly display the UI for the thumbnails or other features surrounding the 
 * core viewer such as tools, menus, etc.   Rather it emits events which allow pages
 * to embed the core and customize it by adding or subtracting elements to the UI and
 * responding to activity taking place in the viewer.
 */
var p = null;
var PatientName ="";
var PatientID="";

var currentImage = -1;
var currentSeriesIndex = -1;
var currentThumb = -1;

var months = ['Jan','Feb','Mar','Apr','May','Jun','Jul','Aug','Sep','Oct','Nov','Dec'];

// If showing a multiframe image in animated 
// mode then this animation will be set to an 
// Animation object (see below).
var animation = null;

var fast = false;//true;

var maxRows ;
var maxColumns;
var imagesSkipped = 0;
// var rand = new Date().getTime();
var rand = 1; // new Date().getTime();

/**
 * This object serves as target for MochiKit events that can be connected to
 * by other modules.  Supported events:
 * 
 *     beforeInit              -  sent before anything else when viewer starts
 *                                even the DOM is not available here
 *     startInitialize         -  sent when DOM available but before viewer 
 *                                itself is initialized
 *                                
 *     onCurrentThumbChange    -  sent before switching selected thumbnail
 *     afterCurrentThumbChange -  sent after switching selected thumbnail,
 *                                after new content is placed in viewer
 *     newSeriesSelected       -  sent when a new series has become active in the viewer
 *                                but before content is displayed in the viewer
 *     activeThumbnailChanged  -  sent at any time when the currently active thumbnail
 *                                might have been modified
 *                                
 *     onCurrentImageChange    -  after the current image to be displayed is modified
 *                                but before that image is actually presented in the
 *                                viewer
 *                                (only when DICOM is in the main viewer area)
 *                                
 *     resetSeries             -  sent after switching selected dicom series, 
 *                                but not necessarily before content is rendered 
 *                                in viewer body
 *                                
 *     initialized             -  after the viewer is completely initialized and ready to display
 */ 
var events = {};

var enableNoAttachmentsMode = true;

var noZoomDrag = false;

/**
 * Stores state and other information for each series present in the 
 * viewer.  These are 'virtual' in the sense that there may be more 
 * thumbnails than fit.  In the classic interface each thumbnail is mapped 
 * into one of the 4 physical positions for display.
 */
var thumbnails = [];

var mimeTypes = {
  "application/pdf": { image: 'images/pdfthumb2.gif', ext: 'pdf' },
  "image/jpg": { image: 'images/picturethumbnail.gif', ext: 'jpg' },
  "image/pjpeg": { image: 'images/picturethumbnail.gif', ext: 'jpg' },
  "image/jpeg": { image: 'images/picturethumbnail.gif', ext: 'jpg' },
  "image/png": { image: 'images/picturethumbnail.gif', ext: 'png' },
  "image/gif": { image: 'images/picturethumbnail.gif', ext: 'gif' },
  "text/x-cdar1+xml": { image: 'images/cdathumb.gif', ext: 'xml' }, 
  "application/x-hl7": { image: 'images/hl7thumb.gif', ext: '' },
  "application/x-ccr+xml": { image: 'images/ccrthumb.gif', ext: 'xml' },
  "video/quicktime": { image: 'images/quicktime.png', ext: 'mov' },
  "text/plain": { image: 'images/unknownthumb.gif', ext: 'txt' },
  "text/x-blue-button": { image: 'images/Blue_Button_Large.png', ext: 'txt' },
  "URL": { image: 'images/text-html.png', ext: 'html' }
};

/**
 * Mime types that we recognize as images.  These can be rendered as thumbnails
 * directly to the thumbnail in scaled form.
 */
var imageMimeTypes = [ "image/jpg", "image/pjpeg", "image/jpeg", "image/gif", "image/png"];

/**
 * Map of keyboard numbers to frame rates
 */
var ANIMATION_FRAME_RATES = {
        1 : 2,
        2 : 5, 
        3 : 10,
        4 : 15,
        5 : 20
};

var DEFAULT_FRAME_RATE = 15;

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
    if(!framed) {
        alert('Simtrak not supported in this view');
        return;
    }
    
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

var tools = [];

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
  this.studies = new Array();
};
  
  
/* ---------------------------Study Object--------------------------------------- */
function Study(StudyDescription, StudyInstanceUID, StudyDate, StudyTime ){
  this.StudyDescription = StudyDescription;
  this.StudyInstanceUID = StudyInstanceUID;
  this.StudyDate = StudyDate;
  this.StudyTime = StudyTime;
  this.series=new Array();
};

/* ---------------------------Series Object-------------------------------------- */
function Series(SeriesDescription, mcGUID, SeriesInstanceUID, Modality, SeriesNumber){
  this.SeriesDescription=SeriesDescription;
  this.mcGUID = mcGUID;
  this.SeriesInstanceUID=SeriesInstanceUID;
  this.Modality=Modality;
  this.SeriesNumber = parseInt(SeriesNumber);
  this.instances = [];
}

/* ---------------------------Instance Object------------------------------------ */
function Instance(SOPInstanceUID, InstanceNumber, FileReferenceID, window, level, numFrames){
  this.InstanceNumber= parseInt(InstanceNumber);
  this.SOPInstanceUID = SOPInstanceUID;
  this.FileReferenceID=FileReferenceID;
  this.numFrames = numFrames;
  // DEMO HACK only.
  // In real world if the window/level values aren't set
  // the server should make some based on 
  // image attributes.
  this.defaultWindowLevel = 
      {w: window?window:500, l: level?level:200};
}
  
/* ---------------------------Series Thumbnail Object---------------------------- */
/**
 * A type of thumbnail representing a series
 */
function SeriesThumbnail(seriesNumber) {
  this.seriesNumber = seriesNumber;
}

SeriesThumbnail.prototype.enableImageCache = true;

/**
 * Show the series in the main image area.  Note 
 * that this won't change other state in the viewer. 
 * To cause the viewer to properly switch to a different
 * thumbnail use displaySelectedThumbnail(n). 
 */
SeriesThumbnail.prototype.display = function() {
    
  // Ensure black background for main image
  // This was a hack for HIMMS because CDA stylesheets don't set the body color to white,
  // so it is set to white in that case and back to black here.
  $('mainImage').style.backgroundColor='black';
  displaySelectedSeries(findIdentical(thumbnails,this));
  signal(this, "displayed");
};

SeriesThumbnail.prototype.updateLabels = function(thumbIndex) {
  var nImages = numberOfImageInSeries(this.seriesNumber);
  var label;
  if(this.seriesNumber==currentSeriesIndex) {
      
     label = "Image " + (currentImage + 1) + " / " + nImages;
     if(this.series.instances[currentImage].caching)
         label += "Buffering ...";
     else
     if(animation && animation.timer) { // running
         label += "<br/>" + animation.fps + " fps"
               + "<br/>" +this.series.instances[currentImage].numFrames + " frames" ;
     }
     else
     if(animation && !animation.timer) { // paused
         label += "<br/>Frame<div>" + (animation.currentFrame+1) + " / "
               + this.series.instances[currentImage].numFrames + "</div>";
     }
  }
  else {
      label = nImages + " Images";  
  }
    
  this.updateThumbnail(thumbIndex, {label: label, desc: this.series.SeriesDescription});
};

SeriesThumbnail.prototype.displayThumbnail = function(thumbIndex) {

    log("Displaying series thumb in cell " + thumbIndex);

  var nImages = this.series.instances.length;
    var seriesName = "series" + this.seriesNumber;
    // In future set series titles..
    // Grab middle image
    var thumbReference = Math.round((nImages-1)/2);
    if(currentImage >= 0 && currentThumb == thumbIndex) {
      thumbReference = currentImage;
    }

    var thumbImage= this.series.instances[thumbReference];

    if(thumbImage == null) {
      alert("DEBUG ERROR:  thumbnail image not found for series " 
          + getSeriesIndex(this.series) + "\r\nCalled from " + stacktrace() 
          + "\r\ninstances.length="+this.series.instances.length
          + "\r\nthumbReference="+thumbReference);
      return;
    }

    var thumbURL = "wado/"+this.series.storageId + "?studyUID="
        +p.studies[0].StudyInstanceUID
        +"&mcGUID="
        +this.series.mcGUID
        +"&rows=140&columns=140&fname="
        +thumbImage.FileReferenceID
        +"&ccrIndex="+ccrIndex;
    
    var wl = thumbImage.defaultWindowLevel;
    thumbURL+="&"+queryString({windowWidth: wl.w, windowCenter:wl.l});
    
    this.updateThumbnail(thumbIndex, {image: thumbURL, desc: this.series.SeriesDescription});
    
    this.updateLabels(thumbIndex);
};

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
};


/**
 * Makes a standard thumbnail cell with the given index
 */
function makeThumbCell(i, callback) {
    log("thumbs = " + thumbnails.length);
    var div = DIV({id: 'thumb'+i, 'class':'thumbCellBox'},
            cell = DIV({id:'thumbCell'+i,
                 'class': 'NotSelectedImage thumbCell' 
            }),
            DIV({id:'thumbLabel'+i, 'class':'ThumbLabel'}),
            DIV({id:'thumbDescription'+i, 'class':'ThumbDescription'}),
            DIV({id:'thumbTime'+i, 'class':'ThumbTime'}),
            ann = DIV({'class':'thumbAnnotation'}, IMG({id:'thumbAnnotation'+i, src:"images/transparentblank.gif"}))
    );    
    if(callback) 
        callback(cell, ann);
    return div;
}

/* -------------------------- SimTrak Data ------------------------------ */

function SimTrakThumbnail(ccr) {
  this.ccr = ccr;
  this.ccrThumb = new CCRThumbnail(ccr);
}

SimTrakThumbnail.prototype.display = function(section) {

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
function CCRThumbnail(ccr,series) {
  this.ccr = ccr;
  this.series = series;
}

CCRThumbnail.prototype.display = function() {

  if($('ccrflag'))
    return;

  log("Displaying CCR with FileReferenceID=" + this.series.instances[0].FileReferenceID + ":" + stacktrace());
  $('mainImage').innerHTML=
    "<span id='ccrflag' style='display: none;'></span>" +
    "<iframe src='DisplayCCR.action?ccrIndex="+ccrIndex+"&guid="
        +this.series.mcGUID 
        +"' name='orderWindow' id='orderWindow' width='"+getAvailableContentWidth()
        +"' height='"+getAvailableContentHeight()
        +"' style='border-style: none; background-color: white;'"
        +" onload='signal(window,\"ccrloaded\");' bgcolor='white'/>";  // NB: for some reason IE is not receiving the signal 
};

CCRThumbnail.prototype.updateLabels = function(thumbIndex) {
  // noop for now
};

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
  
  var fromActor = null;
  if(this.ccr.getFromActor) {
    fromActor = this.ccr.getFromActor();
  }
  
  var tn = this.ccr.trackingNumber.substr(0,4) + ' ' 
           + this.ccr.trackingNumber.substr(4,4) + ' ' + this.ccr.trackingNumber.substr(8,4);

  var patientName = this.ccr.patient.givenName + " " + this.ccr.patient.familyName;
  
  this.updateThumbnail(thumbIndex, { 
      tn: tn, 
      ccrCreateDate: ccrCreateDate, 
      fromActor: fromActor,
      dateOfBirth: formattedDob,
      patientName: patientName,
      ageSex : ageSex
  });
};

/* --------------------------PDF Thumbnail Object------------------------------ */

/**
 * A type of thumbnail representing an Document
 */
function DocumentThumbnail(theSeries) {
  this.series=theSeries;
}

DocumentThumbnail.prototype.display = function() {
  // log("Displaying thumbnail for series " + series.mimeType  + " from " + stacktrace());
  
  var documentUrl = '';
  var bgcolor;
  var mimeType = this.series.mimeType;
  if((mimeType=='text/x-cdar1+xml') || (mimeType=='application/x-ccr+xml')) {
    this.series.instances[0].FileReferenceID;
    bgcolor='white';
  }
  else
  if((mimeType=='application/pdf') && ((this.series.instances[0].FileReferenceID.indexOf("http://") != -1))) {
    documentUrl = this.series.instances[0].FileReferenceID;
  }
  else 
  if((mimeType=='application/x-hl7') && ((this.series.instances[0].FileReferenceID.indexOf("http://") != -1))) {
    documentUrl = this.series.instances[0].FileReferenceID;
    bgcolor='white';
  }
  else {
    var typeInfo = mimeTypes[mimeType];
    if(!typeInfo) {
        alert("This type of document is not supported for display in this viewer");
        return;
    }
    documentUrl="/router/document/"+this.series.mcGUID+"."+typeInfo.ext+"?ccrIndex="+ccrIndex+"&guid="+this.series.mcGUID;
    log("Showing Document with url " + documentUrl);
  }
 
  if(mimeType=='text/plain' || mimeType=='text/x-blue-button') 
     bgcolor='white';
  
  log("Available width is " + getAvailableContentWidth());
  $('mainImage').innerHTML =
    "<iframe src='"+documentUrl + 
          "' name='orderWindow' id='orderWindow' width='"+getAvailableContentWidth() +
          "' height='"+getAvailableContentHeight()+
          "' style='border-style: none;" + (bgcolor?' background-color: '+bgcolor:'')+"'/>";  
};

DocumentThumbnail.prototype.updateLabels = function(thumbIndex) {
  // noop for now
};

DocumentThumbnail.prototype.displayThumbnail = function(thumbIndex) {

  var thumbCellDiv = $('thumbCell'+thumbIndex);
  
  log("checking for mime type " + this.series.mimeType);
  var type = mimeTypes[this.series.mimeType];
  var thumbImageSrc = type ? type.image : null;
  if(thumbImageSrc == null) {
    thumbImageSrc = 'images/unknownthumb.gif'; 
  }

  // HACK:  for non-local items (CDA, CCR, PDF) display the description, everything else use the file name
  // BUG: Some PDFS are local.
  var thumbDescription;
  if(this.series.instances[0].FileReferenceID == this.series.SeriesDescription)
      thumbDescription = this.series.instances[0].FileReferenceID;
  else
      thumbDescription = this.series.instances[0].FileReferenceID + ' ('+ this.series.SeriesDescription +')';
  
  if(
  (this.series.mimeType=='text/x-cdar1+xml') || 
  (this.series.mimeType=='application/x-ccr+xml') || 
  (this.series.mimeType=='application/pdf') ||
  (this.series.mimeType=='text/plain') ||
  (this.series.mimeType=='text/x-blue-button') ||
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
  
  if(findValue(imageMimeTypes,this.series.mimeType)>=0) {
    thumbImageSrc = 'ImageThumb.action?ccrIndex='
      +ccrIndex
      +'&seriesIndex='+this.series.index
      +'&height=75&width=75';
    thumbDescription = this.series.SeriesDescription;
  }

  this.updateThumbnail(thumbIndex, { image: thumbImageSrc, desc: thumbDescription });
};


/* --------------------------WebReference Thumbnail Object------------------------------ */

/**
 * A type of thumbnail representing an external document on the web.
 */
function WebReferenceThumbnail(theSeries) {
  this.series=theSeries;
}

WebReferenceThumbnail.prototype.display = function() {

  var documentUrl=this.series.instances[0].FileReferenceID;
  log("Showing Web Reference with url " + documentUrl);
  $('mainImage').innerHTML=
    "<iframe src='"+documentUrl + "' name='orderWindow' width='"+getAvailableContentWidth()+"' height='"+getAvailableContentHeight()+"' style='border-style: none;'/>";  
  // window.open(documentUrl,'reference');
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
  var thumbImageSrc = mimeTypes[this.series.mimeType].image;
  if(thumbImageSrc == null) {
    thumbImageSrc = 'images/unknownthumb.gif'; 
  }

  var thumbDescription = this.series.instances[0].FileReferenceID + ' (Web Document)';
  var newHtml ='<div class="NotSelectedImage thumbCell" onclick="displaySelectedThumbnail('+thumbIndex +');" style="cursor: hand;">'
          + '<div class="CCRTitle"><span style="position: relative;  left: 5px; text-align: left; width=100%;">'
          + '<img border="0" style="position: relative; top: 3px;" src="images/record.gif"/>&nbsp;&nbsp;Document</span></div>'
          +'<div class="CCRThumbLabels" style="height: 110px; position: relative; top: 4px; "><div id="ccrThumbName" style="width: 140px;">' 
          + '&nbsp;' + thumbDescription + '</div>'
          +'<div class="CCRThumbLabels"><img class="webReferenceThumbnail" src="'+thumbImageSrc+'"/></div>';
  newHtml += '</div>';
  thumbCellDiv.innerHTML=newHtml;
  replaceChildNodes($("thumbDescription" + thumbIndex),'');
  replaceChildNodes($("thumbLabel" +thumbIndex),'');  

  if(this.series.validationRequired) {
    el("thumbAnnotation"+thumbIndex).style.display='block';
    el("thumbAnnotation"+thumbIndex).src='images/validation_required.png';
  }
  else {
    el("thumbAnnotation"+thumbIndex).style.display='none';
  }
};


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
  this.updateThumbnail(thumbIndex, {  image: 'images/blank.png', label: '', desc: ''});
};

BlankThumbnail.prototype.displayThumbnail = function(thumbIndex) {
  this.updateThumbnail(thumbIndex, {  image: 'images/blank.png', label: '', desc: ''});
};

/* ------------------------------Image Caching Support-------------------------- */

var enableCacheDisplay = true;

function ImageCache(cacheSize) {
  this.cacheSize=cacheSize;
  this.currentCacheOffset=0;
  this.cacheNext=ImageCacheCacheNext;
  this.reset=ImageCacheReset;
  this.forwardSpec = new ImageSpec(currentSeriesIndex,currentImage);
  this.backwardSpec = new ImageSpec(currentSeriesIndex,currentImage);
}

function ImageCacheCacheNext() {

  log("ImageCacheCacheNext 1");
   
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
        var w = Math.round(((this.currentCacheOffset/this.cacheSize) * (findPosX(cachedPercent)-findPosX(cacheDisplay)-3)));
        if(w<0)
            w = 0;
        cacheDisplay.style.width= w+'px';
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
    log("Caching image " + this.currentCacheOffset 
        + "(" + this.backwardSpec.series + "," +  this.backwardSpec.imageNum + ") " 
        + getImageUrl(this.backwardSpec.series, this.backwardSpec.imageNum));

    setCookie("priority","low"); // ensure that caching gets done at low priority
    this.backwardImage.src = getImageUrl(this.backwardSpec.series, this.backwardSpec.imageNum);
    var fwdImg = new Image();
    fwdImg.onload = fwdImg.onerror = fwdImg.onabort = function() { imageCache.cacheNext(); }
    fwdImg.src = getImageUrl(this.forwardSpec.series, this.forwardSpec.imageNum);
    this.forwardImage = fwdImg;
  }
  else {
    window.setTimeout(ImageCacheHideCachedPane,3000);
    log("Caching finished");
  }
}

function ImageCacheReset() {
  //log("Resetting image cache to (" + currentSeriesIndex + "," + currentImage + "):  " + stacktrace());
  this.currentCacheOffset=0;
  this.forwardSpec = new ImageSpec(currentSeriesIndex,currentImage);
  this.backwardSpec = new ImageSpec(currentSeriesIndex,currentImage);
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
  
  // currentWindowLevel contains w and l properties that are the
  // windowWidth and windowCenter values used to display 
  // an image within the series. If null, the default window/level for the instance
  // is used.
  // Value is set to null when a series is changed.
  this.currentWindowLevel = null;
  
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
    var image= p.studies[0].series[currentSeriesIndex].instances[currentImage];
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
   
   // Zoom drag does not work on devices like iPad
   if(noZoomDrag) 
       return;
   
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

    log('begin drag at ' + startX + ',' + startY);

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
      update(dragRect.style, {
	      zIndex : '30',
	      top : Math.min(startY,endY)+'px',
	      left : Math.min(startX,endX)+'px',
	      width : Math.abs(endX-startX)+'px',
	      height : Math.abs(endY-startY)+'px',
	      borderStyle : 'dotted'
      });

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
  return (((thumbnailIndex-1) % thumbnailPageSize) + 1);
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
    var thumbImage= p.studies[0].series[currentSeriesIndex].instances[currentImage];
    var thumbURL = "wado/"+p.studies[0].series[currentSeriesIndex].storageId+"?studyUID=";
    thumbURL+=p.studies[0].StudyInstanceUID
            + "&rows=140&columns=140&fname="
            + thumbImage.FileReferenceID
            + "&mcGUID="
            + p.studies[0].series[currentSeriesIndex].mcGUID
            + "&ccrIndex="
            + ccrIndex
            + "&windowCenter="
            + thumbImage.defaultLevel
            + "&windowWidth="
            + thumbImage.defaultWindow;
            
    if($(thumbImageName)!=null) {
      $(thumbImageName).src =thumbURL;
    }
}

function calculateImageDimensions(){
   imageWidth = $('wadoImage').width;
  
  // Note we have to adjust for the position of the image on the page since it may
  // be defined by the user via css
   scrollDragLimitX = imageWidth - 32 + $('mainImage').offsetLeft;
 }

/**
 * Displays a thumbnail in the main viewer based
 * on the index of the visual cell in which that thumbnail
 * is rendered. 
 */ 
function displaySelectedThumbnail(thumbCellIndex) {
  var index = getThumbArrayIndex(thumbCellIndex);
  
  log("Showing thumbnail " + index + " in main viewing area");
  
  signal(events,'onCurrentThumbChange');
  currentThumb=index;

  var series = thumbnails[index].series;
  if(series && series.paymentRequired) {
    paymentRequiredDlg(series.billingEvent, partial(doPayment, series, partial(displaySelectedThumbnail,thumbCellIndex)));

        /*
    if(confirm('Payment is required to access this content.\n\nClick OK to pay for this content using your account, or Cancel to return.')) {
      doPayment(series, partial(displaySelectedThumbnail,thumbCellIndex));
    }
    */
    return;
  }

  signal(events,'resetSeries');
  
  if(index == 0) {
    thumbnails[0].display();
  }
  else {
    // log("displaying thumbnail " + index + ':' + stacktrace());
    thumbnails[index].display();  
  }
  
  adjustContentAreaSize();
  signal(events,'activeThumbnailChanged', thumbnails[index]);
  signal(events, 'afterCurrentThumbChange', thumbnails[index]);
}

/**
 * Displays the series for the given thumbnail, also selecting
 * that thumbnail.  The given index is the index into the 
 * thumbnails array, not the visual thumbnail box on the screen.
 */
function displaySelectedSeries(thumbnailIndex) {
  // Else show the selected series.    
  log("displaySelectedSeries");
  selectSeries(thumbnails[thumbnailIndex].series);
  displayCurrentImage();
  series = thumbnails[thumbnailIndex].series;
  signal(events, 'afterCurrentThumbChange', thumbnails[thumbnailIndex]);
}

function displaySelectedImage(n){
  currentImage = n;
  if (currentImage< 0)
    currentImage = 0;
  else if (currentImage >= numberOfImageInSeries(currentSeriesIndex)){
    currentImage = numberOfImageInSeries(currentSeriesIndex) - 1;
  }
  displayCurrentImage();
}


function numberOfSeries(){
  return(p.studies[0].series.length);
}

function numberOfImageInSeries(seriesNumber){
  return(p.studies[0].series[seriesNumber].instances.length);
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
    currentImage = numberOfImageInSeries(currentSeriesIndex)-1;
  }
  signal(events,'onCurrentImageChange');
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
  if(currentImage >= numberOfImageInSeries(currentSeriesIndex)) {
    currentImage = 0;
  }
  signal(events,'onCurrentImageChange');
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
 * Causes the requested thumbnail representing the specified series 
 * to be highlighted.  Does not display the series in main image.
 * See displayCurrentImage() or use thumbnails[n].display() to 
 * actually place the image into the image area, or to cause the 
 * whole viewer to switch images use displaySelectedThumbnail(n).
 * 
 * @param    series    the series object to be displayed
 */
function selectSeries(series) {

  var previousSeries = currentSeriesIndex;
  currentSeriesIndex = getSeriesIndex(series);
  currentImage = 0;
  if(series.lastImageIndex)
    currentImage = series.lastImageIndex;

  currentThumb=thumbnailIndexFromSeries(series);

  resetSeriesChange();

  var image= p.studies[0].series[currentSeriesIndex].instances[currentImage];
  if(image != null) 
    currentToolState.currentWindowLevel = image.defaultWindowLevel;

  // Make sure that we are showing the correct page of thumbnails
  currentThumbnailPage = Math.floor( (currentThumb-1) / thumbnailPageSize);
  if(currentThumbnailPage<0)
    currentThumbnailPage = 0;
  
  log("currentThumbnailPage = " + (currentThumb)+ " / " + thumbnailPageSize + " = " + currentThumbnailPage);
  
  signal(events, 'activeThumbnailChanged', thumbnails[currentThumb]);
  signal(events, 'newSeriesSelected', series);
}


function onSwitchTab(url) {
  if(framed && parent.menu.getItem(3)) {
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
  for(i=0; i<p.studies[0].series.length;++i) {
    if(p.studies[0].series[i] == series)
      return i;
  }
  log("WARN: no series index found for series " + series);
  return -1;
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
  var availableHeight =
      framed ? (window.parent.document.body.offsetHeight - window.parent.$('tabs').scrollHeight - 10)
             : document.body.offsetHeight;
  if(availableHeight > 750)
    mainImageHeight = 750;
  else
    mainImageHeight = availableHeight;

  var oldSize = elementDimensions('mainImage');
  log("Adjusting size:  ("+oldSize.w+","+ oldSize.h + ") to (?,"+mainImageHeight+")");
  
  if(oldSize.h == mainImageHeight)
      return;

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

var EDIT_AS_NEW_TOOL = false;
var DISCARD_SELECTION_TOOL = false;

/**
 * Initializes the whole WADO viewer. Call this method once when the
 * page has loaded to initialize the main view.
 */
function Initialize() {
    
  signal(events, "startInitialize");
    
  // Check for too small window and shrink as necessary
  window.onParentSize = function() { window.onParentSize =  adjustMainImageSize; };
    
  showStartupMessage();

  // Mozilla scroll wheel support
  if(document.addEventListener) { // DOM Level 2 Event Model
    window.addEventListener("DOMMouseScroll", MozillaScroll, false);
  }

  log("currentThumb="+currentThumb);
  
  
  for(var i=0; i<thumbnails.length; ++i) {
    if(thumbnails[i]==null) {
      thumbnails[i] = new BlankThumbnail();
    }
  }
  
  if(initialSeriesIndex > 0) {
      log('selecting series ' + initialSeriesIndex);
      currentSeriesIndex = initialSeriesIndex;
      var s = p.studies[0].series[currentSeriesIndex];
      selectSeries(s); // Was set in URL.
      setZoomActive();
      displayCurrentImage();
      forEach(filter(function(t){return t.series==s}, thumbnails), function(t) { signal(t,'displayed');});
  }
  else
  if(currentThumb == 0) {
    thumbnails[0].displayThumbnail(0);
    signal(events, 'activeThumbnailChanged', thumbnails[currentThumb]);
    displaySelectedSeries(0);
    signal(events,'resetSeries');
  }
  else
  if(currentSeriesIndex >= 0) {
    log('selecting series ' + currentSeriesIndex);
    selectSeries(p.studies[0].series[currentSeriesIndex]); // Was set in URL.
    setZoomActive();
    displayCurrentImage();
  }

  currentImage = 0;

  if(enableSimTrak) 
    addSimTrakTools();

  // POPS transfers show the quick reply button
  if(enableQuickReply) {
    visibility(parent.$('topcenterbuttons'),true);
    connect(window,'onunload',function(){visibility(parent.$('topcenterbuttons'),false)});
  }

  // Update parent window query fragment to restore this view
  updateFragment();
  
  
  log('initialized');
  signal(events, 'initialized');
  
  visibility('ViewerArea',true);
  
  // HACK : Firefox seems to want window where 
  // all other browsers want body
  forEach(['onkeydown','onkeyup','onkeypress'], function(evt) {
	  if(YAHOO.env.ua.gecko > 0) {
		  connect(window, evt, handleKeyDown);
		  connect(window.parent, evt, handleKeyDown);
	  }
	  else {
		  connect(document.body, evt, handleKeyDown);
		  connect(parent.document.body, evt, handleKeyDown);
	  }
  });
  
  
  // We can have a whole chain of warnings to pester the user with
  var warnings = [];
  
  if((tipState&8) ^ 8)
      warnings.push(warnNoClinicalUse);
  
  // If CCR is incomplete, warn the user about that
  if(ccr.patient.status == 'INCOMPLETE') 
      warnings.push(warnIncompletePatient);
  
  if(initShare)
      warnings.push(function() {
          showSharingDialog({
              buttons: {
                  'Back to Inbox': function() { top.location.href='/acct/'; }
              }
          });
      });
      

  // Almost a Y Combinator!
  if(warnings.length > 0) {
      var f = warnings.shift();
      f(function() {
            if(warnings.length) {
                warnings.shift()(function() {
                  if(warnings.length)
                     warnings.shift()();
                });
            }
      });
  }
}  

function warnNoClinicalUse(next) {
    dialog('clinicalUseWarning','Clinical Use Warning',
           'This viewer is not FDA approved and should not be used for clinical purposes.'+
           "<p><input id=noClinWarn type=checkbox checked> Don't show this again",
           460, 
          { OK : function() {
                  
                  if($('noClinWarn').checked) {
                      execJSONRequest(accountsBaseURL+'/acct/disable_tip.php?tip=8');
                      setTimeout(function() {
                          execJSONRequest('FlushSettings.action');
                      },500);
                  }
                  
                  this.destroy();
                  
                  if(next)
                      next();
             }
          }, function(dlg) {
      dlg.cfg.setProperty("icon",YAHOO.widget.SimpleDialog.ICON_WARN);
    });
}


function handleThumbClick(i) {
  displaySelectedThumbnail(i);
}

/**
 * Returns the number of pages of thumbnails
 */
function numberOfPages() { 
 return Math.floor( (numberOfSeries()+1) / thumbnailPageSize + 1);
}

function showActivity() {
    var url ='CurrentCCRWidget.action?combined=true&noheader=true&cssClass=borderless';
    var tab = parent.addTab('Activity / Consents',url,'');    
    parent.showTab(tab);
}


/**
 * Displays debug information about the studies in the WADO viewer.
 */
function displayStudyArrays() {
  var msg =p.studies[0].StudyDescription + "\n";
  msg += "Series in study:";
  msg+= p.studies[0].series.length + ".";
  for (i=0;i<p.studies[0].series.length;i++){
    msg+="\n===";
    msg+=p.studies[0].series[i].SeriesDescription;
    msg+="\n images: " ;
    msg+=p.studies[0].series[i].instances.length;
    msg+= "\n";
    msg+=p.studies[0].series[i].SeriesInstanceUID;
  }
  alert (msg);
}

function restoreMainImageHtml() {
  $('mainImage').innerHTML= 
  '<div id="wadoImageArea" style="width:'+imageWidth+'px; height: ' + imageHeight + 'px;  overflow: hidden;"><img id="wadoImage" name="image" src="blank.png" '
  + ' onload="imageLoadCompleted();" onabort="imageLoadAborted();" onerror="imageLoadError();" '
  + ' onselect="return false;" /></div>';
  
  
  connect('wadoImageArea', 'onmousedown', function(evt) {
      if(evt.mouse().button.left) 
	      beginDrag(this,evt.event());
  });
  
  signal(events, 'newMainImage');
}

connect(events, 'buffering', function() {
    if(!$('bufferingMsg')) {
        var div;
	    appendChildNodes($('wadoImageArea'),
	            div = DIV({id:'bufferingMsg'},"Buffering ..."));
	    setOpacity(div, 0.5);
    }
});

connect(events, 'endBuffering', function() {
    if($('bufferingMsg'))
        removeElement('bufferingMsg');
});

	
/**
 * Displays the current image (indicated by currentSeriesIndex and currentImage) 
 * in the main image area.
 */
function displayCurrentImage() {
    
    var series = p.studies[0].series[currentSeriesIndex];
    if(series.mimeType == 'application/dicom') {
        
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
      
      var nImages = numberOfImageInSeries(currentSeriesIndex);
      log("Series:" + currentSeriesIndex + ", image:" + (currentImage+1) + " / " + nImages);
      
      
      var imageURL = getImageUrl(currentSeriesIndex, currentImage);
      if(imageURL == null)
        return;

      var dicomImg = series.instances[currentImage];
      series.lastImageIndex = currentImage;
      if(dicomImg != null) {
        currentToolState.currentWindow = dicomImg.defaultWindow;
        currentToolState.currentLevel = dicomImg.defaultLevel;
      }

      setImage(imageURL);        
      var numFrames = dicomImg.numFrames;
      if(numFrames > 1) {
          log("Multiframe image - buffering frames");
          
          signal(events, 'buffering');
          
          var images = [];
          var framesPerSprite = Math.floor(MAX_SPRITE_HEIGHT / imageHeight);
          for(var frameNumber = 0; frameNumber<numFrames; frameNumber+=framesPerSprite) {
			  var img = new Image();
			  img.pendingSrc = getImageUrl(currentSeriesIndex, currentImage, {frameNumber: frameNumber, imageQuality: 50, rows:imageHeight, columns:imageWidth}) + '&frameSprite=true';
			  images.push(img); 
          }
          dicomImg.cache = new ImageQueue(images);
          dicomImg.caching = true;
          thumbnails[currentThumb].updateLabels(currentThumb);
          animation = new Animation($('wadoImage'), dicomImg.cache.images, imageHeight, framesPerSprite, numFrames);
          
          callLater(0,partial(signal,events, 'beginAnimation', animation)); 
          
          var cancelled = false;
          var started = false;
          connect(dicomImg.cache, 'complete', function() {
	          
	          if(started) {
	              log("Multiframe image - more frames loaded");
	              return;
	          }
	          
              signal(events,'endBuffering');
	              
	          log("Multiframe image - beginning animation");
	          
              dicomImg.caching = false;
              document.image.src = dicomImg.cache.images[0].src;
              
              // All frames are coming back
              $('wadoImage').height = Math.min(MAX_SPRITE_HEIGHT-(MAX_SPRITE_HEIGHT%imageHeight), numFrames*imageHeight);
              $('wadoImage').width = imageWidth;
              
              if(animation) {
                  animation.stop();
              }
              $('ViewerArea').focus();
              
              
              animation.start(DEFAULT_FRAME_RATE);
	          thumbnails[currentThumb].updateLabels(currentThumb);
	          
	          var a, div;
	          appendChildNodes($('wadoImageArea'), div = DIV({id: 'animationHelp'}, 
	                  SPAN("1 - 5 select frame rate, Space pauses, Left and Right change frames"))
	          );
	          roundElement(div);
	          setOpacity(div,0.5);
	          
	          connect(div, 'onmousedown', function(evt) {
	              evt.preventDefault();
	              evt.stopPropagation();
	              fade(div, {afterFinish: function() {
	                  if(!div.removed && div.parentNode) {
    		              removeElement(div);
    		              div.removed = true;
	                  }
	              }});
	          });
	          
	          setTimeout(function() { 
	              fade(div, {afterFinish: function() {
	                  if(!div.removed && div.parentNode) {
    		              removeElement(div);
    		              div.removed = true;
	                  }
	              }});
	          }, 3000);
	          
	          started = true;
          });
          var stop = function() {
              log('Stopping animation');
              dicomImg.cache.cancel();
              disconnectAll(dicomImg.cache);
              dicomImg.caching = false;
              if(animation) {
                  animation.stop();
                  animation = null;
              }
              if($('wadoImage')) {
	              $('wadoImage').height = imageHeight;
	              $('wadoImage').removeAttribute('height');
              }
              restoreMainImageHtml();
          };
          connect(events, 'onCurrentThumbChange', stop);
          connect(events, 'onCurrentImageChange', stop);
          dicomImg.cache.start();
      }
      else {
          $('wadoImage').height = imageHeight;
          $('wadoImage').removeAttribute('height');
      } 
      
      signal(events, 'activeThumbnailChanged', thumbnails[currentThumb]);
  }
  else {
    thumbnails[currentThumb].display();
  }
  adjustContentAreaSize();
}

/**
 * Performs an animation using a set of frame buffers that are full of 
 * images tiled vertically.  Supports iteration over multiple frame 
 * buffers.
 * <p>
 * Note: if the 'timer' property of an animation object is non-null
 * then it means that the animation is running.
 * 
 * @param image             the target image which is to be displayed
 * @param images            array holding frame buffer images
 * @param frameHeight       height of each individual frame within each frame buffer
 * @param imagesPerSprite   how many images are in each frame buffer (or 'sprite')
 * @param numFrames         the total number of frames to be displayed
 */
function Animation(image, images, frameHeight, imagesPerSprite, numFrames) {
    this.image = image;
    this.images = images;
    this.height = frameHeight;
    this.numFrames = numFrames;
    this.currentFrame = 0;
    this.timer = null;
    this.imagesPerSprite = imagesPerSprite;
    this.fps = 15;
    this.buffering = false;
    this.bufferSize = 50;
    
    log("Images per sprite = " + imagesPerSprite);
}

Animation.prototype.start = function(fps) {
    if(typeof fps != 'undefined' && fps < 0)
        throw new Error("Bad value for fps: " + fps);
     
    if(typeof fps == 'undefined')
        fps = this.fps;
        
    this.fps = fps;
    if(this.timer)
        clearInterval(this.timer);
    
    this.timer = setInterval(bind('next',this), 1000 / this.fps);
};

Animation.prototype.next = function() {
    this.currentFrame++;
    if(this.currentFrame >= this.numFrames) {
        this.currentFrame = 0;
    }
    if(!this.show())
        this.back();
};


Animation.prototype.back = function() {
    this.currentFrame--;
    if(this.currentFrame < 0) {
        this.currentFrame = this.numFrames-1;
    }
};
Animation.prototype.prev = function() {
    this.back();
    this.show();
}; 


Animation.prototype.hasSufficientBuffer = function(spriteIndex) {
    // If current frame isn't loaded then obviously we don't
    // have enough buffer
    if(!this.images[spriteIndex].loaded)
        return false;
    
    // If we have loaded current frame and it is the last 
    // then we have sufficient buffer
    if(spriteIndex >= this.images.length -1)
        return true;
    
    // We have loaded the current frame, but it is not the last
    // Count how much head room we have
    var headRoom = 0;
    for(var i=spriteIndex+1; i<this.images.length;++i) {
        if(this.images[i].loaded)
            ++headRoom;
        else
            break;
    }
    
    // log("Sprite = " + spriteIndex + " headRoom = " + headRoom + " unloaded = " + (this.images.length - i));
    
    return (i==this.images.length) || ((headRoom+1)*this.imagesPerSprite >= this.bufferSize); 
};


/**
 * Show the current frame in the animation.   If the image for the
 * current frame is not loaded, enter buffering state and 
 * return false, else display the image and return true.
 */
Animation.prototype.show = function() {
    // Calculate which sprite to use
    var spriteIndex = Math.floor(this.currentFrame / this.imagesPerSprite);
    
    if(!this.hasSufficientBuffer(spriteIndex)) {
        if(!this.buffering) {
	        this.buffering = true;
	        log("Insufficient images loaded (" + spriteIndex + ") Buffering more images");
	        signal(events,'buffering');
        }
        return false; 
    } 
    
    if(this.buffering) {
        this.buffering = false;
        log("End buffering from Animation");
        signal(events,'endBuffering');
    }
    
    if(this.image.src != this.images[spriteIndex].src) {
        this.image.parentNode.replaceChild(this.images[spriteIndex], this.image);
        this.image = this.images[spriteIndex]; 
        this.image.style.position = 'relative';
        this.image.id = 'wadoImage';
        this.spriteHeight = (spriteIndex < this.images.length-1) ? 
	        (imageHeight*this.imagesPerSprite) : (imageHeight * (this.numFrames - this.currentFrame));
    	    
        if(this.spriteHeight)
            this.image.height  = this.spriteHeight;
    
	    // $('thumbDescription1').innerHTML = this.spriteHeight;
	    // this.image.setAttribute('height', height); 
    }
    
    
    var imageIndex = this.currentFrame % this.imagesPerSprite; 
	    
    // log('imge ' + imageIndex +  " / " + spriteIndex + ' off= ' + (-imageIndex * imageHeight) + ' hght = ' + this.image.height);
    // $('thumbDescription1').innerHTML=('imge ' + imageIndex +  " / " + spriteIndex + ' off= ' + (-imageIndex * imageHeight) + ' hght = ' + this.image.height);
    this.image.style.top = (-imageIndex * imageHeight)+"px";

    // log("image " + imageIndex + " in sprite " + spriteIndex);
    return true;
};
Animation.prototype.pause = function() {
    if(this.timer)
	    clearInterval(this.timer);
    this.timer = null;
};
Animation.prototype.stop = function() {
    this.pause(); 
    this.currentFrame = 0;
};

/**
 * Loads a set of images one by one.  
 * At the completion of each image load a 
 * 'complete' event is fired to notify 
 * the owner of the queue that a new image 
 * is ready
 */
function ImageQueue(images) {
    this.current = 0;
    this.cancelled = false;
    this.images = images;
    this.image = null;
}
ImageQueue.prototype.start = function() {
    this.current = 0;
    this.next();
}
ImageQueue.prototype.next = function() {
    if(this.cancelled)
        return;
    var q = this;
    this.image = this.images[this.current];
    this.image.onload =  function() {
      setTimeout(function() { 
		  signal(q, 'complete');
		  q.image.loaded = true;
		  ++q.current;
		  log('current = ' + q.current + ' / ' + q.images.length);
		  if(q.current < q.images.length)
		      q.next();
	  },1);
    };
    
    
    this.image.onerror = this.image.onabort =  function() {
        alert('Error loading image ' + this.image.src);
        this.image.onload();
    };
    
    this.image.src = this.image.pendingSrc;
    log('buffering sprite ' + this.current + ' - ' + this.image.src);
}
ImageQueue.prototype.cancel = function() {
  log("Image queue operation cancelled");
  this.cancelled = true;
}

/**
 * Returns a URL for the given series and image
 * <p>
 * Valid options include:
 * <li>frameNumber
 * <li>imageQuality
 * <li>windowCenter
 * <li>windowWidth
 * <li>maxRows
 * <li>maxCols
 * <p>
 * Where options are not specified they are defaulted to values
 * for the currently displayed main image.
 */
function getImageUrl(urlSeries,urlImage,options) {
    
    if(!urlSeries)
        urlSeries = currentSeriesIndex;
    
    if(!urlImage) 
        urlImage = currentImage;
    
    if(!options) 
        options = {};
     
     var image= p.studies[0].series[urlSeries].instances[urlImage];
     if(image==null) {
       alert("Series " + urlSeries + " image " + urlImage + " is null");
       return null;
     }
     
     if(image.FileReferenceID==null){
       alert("Series " + urlSeries + " image " + urlImage + " FileReferenceID is null");
       return null;
     }
     
     var defaults = {
             studyUID: p.studies[0].StudyInstanceUID,
             fname: image.FileReferenceID,
             patientName: p.PatientName,
             mcGUID: p.studies[0].series[currentSeriesIndex].mcGUID,
             imageQuality: 90,
             ccrIndex: ccrIndex
     };
     
     if(MAX_SPRITE_HEIGHT != DEFAULT_FRAME_RATE)
         defaults.maxSpriteHeight = MAX_SPRITE_HEIGHT;
     
     var wl = currentToolState.currentWindowLevel;
     if(!wl) { 
         wl = image.defaultWindowLevel;
     }
     update(defaults,{windowWidth: wl.w, windowCenter:wl.l});
     
     if(currentToolState.showImagesFullSize == false){
         defaults.maxRows = maxRows;
         defaults.maxColumns = maxColumns;
     }
     
     var imageURL = "wado/" + p.studies[0].series[currentSeriesIndex].storageId + "?"; 
     imageURL += queryString(merge(defaults,options));
     
     if(!options.annotation) {
         if(currentToolState.currentAnnotation==undefined){
           currentToolState.currentAnnotation = allAnnotation;
         }
         imageURL+=currentToolState.currentAnnotation;
     }
    
     if(fast==true)
       imageURL+="&interpolation=FAST";
     
     if(currentToolState.currentRegion != null) {
      imageURL+=currentToolState.currentRegion;
     }

    imageURL+="&rand="+rand;
    log("image url: " + imageURL);
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
    
  log("handleZoomRegion: " + startX + ", " + startY + "," + endX + "," + endY);

  startX = startX - $('mainImage').offsetLeft;
  startY = startY - $('mainImage').offsetTop;
  endX = endX - $('mainImage').offsetLeft;
  endY = endY - $('mainImage').offsetTop;

  //alert("zoom region:" + startX + "," + startY + "," + endX + "," + endY + ",w=" + imageWidth  + " h=" + imageHeight);
  log('End drag at Y='+endY);

  var x1 = (1.0 * startX)/(1.0 * imageWidth);
  var y1 = (1.0 * startY)/(1.0 * imageHeight);
  var x2 = (1.0 *  endX)/(1.0 * imageWidth);
  var y2 = (1.0 * endY)/ (1.0 * imageHeight);
  
  //alert("zoom region:" + x1 + "," + y1 + "," + x2 + "," + y2 + ",w=" + imageWidth  + " h=" + imageHeight);
  
  if (x1 > x2){
    var temp = x1; x1=x2; x2=temp;
  }
  if (y1 > y2){
    var temp = y1; y1=y2; y2=temp;
  }

  // Here we used to modify the zoom level if it was too great.
  // This, however, proved confusing and allowed rogue gestures
  // to do confusing things.  Instead, we now throw an exception 
  // which aborts the operation underway.

  log("Zoom X=" + (x2-x1) + " Y=" + (y2-y1));

  /*
  if(((x2-x1) < maximumZoom) || ((y2-y1) < maximumZoom)) {
    log("Zoom too large.");
    throw zoomTooLargeError;
  }
  */
  
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
  signal(events,'onCurrentImageChange');
  displayCurrentImage();
}
 
 // Image Loading Routers
 
 // Invoked each time that an image has been loaded to set
 // the imageLoaded flag to 'true'.
 function imageLoadCompleted(){
     
  // Bypass all this logic when we are showing an animation
  if(animation)
      return;
  
  imageLoaded = true;
  log("Image " + imageLoading + " loaded");
  signal(events, 'imageLoaded');
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
      if(!animation) {
	      log("Initiating caching for thumb index " + currentThumb + " mimeType=" +  thumbnails[currentThumb].series.mimeType );
	      window.setTimeout(cacheNextImage,300);
      }
    } 
    
    if(document.image && hasElementClass(document.image,'hidden')) 
        removeElementClass(document.image,'hidden');
    setElementPosition(document.image,{x:0, y:0});
  }
 }

/**
 * Called on timeout to start the caching process which caches
 * images forward and backward from the one currently being viewed.
 */
var synchronizeThumbnailTimerID=0;
function cacheNextImage() {
  log("cacheNextImage imageLoaded = " + imageLoaded + " enable cache = " +  thumbnails[currentThumb].enableImageCache);
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

// Right now ignore it
function handleDoubleClick(){
  // resetViewer();
}

// Sets the global window/level values.
function setWindowLevel(w, l){
  currentToolState.currentWindowLevel = {w: w, l: l};
}

function resetSeriesChange(){
  currentToolState.currentRegion = null;
  currentToolState.currentAnnotation=this.currentAnnotation;
  currentToolState.currentWindowLevel = null;
  currentToolState.zoomed = false;
  imageLoaded = true;
  moveThumbnailRect(-100, -100);
  log("Finding thumbnail for series " + currentSeriesIndex);
  currentThumb = thumbnailIndexFromSeries(p.studies[0].series[currentSeriesIndex]);
  this.imageCache.reset();

  /*
  if(numberOfImageInSeries(currentSeriesIndex) > 1) {
    window.setTimeout(hideSeriesDragIcon,3000);
  }
  */
  signal(events,'resetSeries');
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

  var img = $('wadoImage');
  var dim = elementDimensions('wadoImageArea');
  
  // Convert the offset to relative form
  var regionX1 = offsetX / seriesXSize * (dim.w / img.width);
  var regionY1 = offsetY / seriesYSize * (dim.h / img.height);
    
  var regionDeltaX =  regionX1 - currentToolState.regionX1;
  var regionDeltaY =  regionY1 - currentToolState.regionY1;
  
  // The user can actually drop outside of the image for non-rectangular images
  if (regionX1 > 1 || ((currentToolState.regionX2 + regionDeltaX) > 1)) {
      regionX1 = currentToolState.regionX1;
      regionDeltaX = 0;
  }
  
  if (regionY1 > 1 || ((currentToolState.regionY2 + regionDeltaY) > 1)) {
      regionY1 = currentToolState.regionY1;
      regionDeltaY = 0;
  }

  // Calculate the zoom rectangle in absolute pixel coordinates
  // on the original image
  var x1 = regionX1 * img.width + $('mainImage').offsetLeft;
  var x2 = (currentToolState.regionX2 + regionDeltaX) * img.width 
    + $('mainImage').offsetLeft;
  var y1 = regionY1 * img.height;
  var y2 = (currentToolState.regionY2 + regionDeltaY) * img.height;
  
  handleZoomRegion(x1,y1,x2,y2, img.width, img.height); 
  setThumbnailRectangleOnSeries();
}

function setThumbnailRectangleOnSeries(pos){
    
  if(!pos)
      pos = currentToolState;

  var dim = elementDimensions('wadoImageArea');
  var img = $('wadoImage');
  
  var offsetX = (seriesXSize * pos.regionX1) * (img.width / dim.w);
  var offsetY = (seriesYSize * pos.regionY1) * (img.height / dim.h);
  
  var thumbW = 
      (pos.regionX2 - pos.regionX1) * seriesXSize * (img.width / dim.w);
  var thumbH = 
      (pos.regionY2 - pos.regionY1) * seriesYSize * (img.height / dim.h);
    
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
  // log("KEY:  " + event.key().string + ' CODE: ' + event.key().code + ' EVENT: ' + event.type());
  if(disableKeyCapture)
    return;
  
  var handled = function() {
      event.stopPropagation();
      event.preventDefault();
  };
  
  if((event.key().code == 32) && animation) {
      handled();
      event.key().string = 'KEY_SPACEBAR'
          
  }
  
  var down = event.type() == 'keydown';
  
  
  switch(event.key().string) {
	  case 'KEY_ARROW_UP':
	      if(down)
		      handleMousewheel(-1);
	      handled();
	      break;
	      
	  case 'KEY_ARROW_DOWN':
	      if(down)
		      handleMousewheel(-1);
	      handleMousewheel(1);
	      handled();
	      break;
	      
	  case 'KEY_ARROW_LEFT':
	      if(down && animation && !animation.timer){
	          animation.prev();
	          thumbnails[currentThumb].updateLabels(currentThumb);
		      handled();
	      }
	      break;
	      
	  case 'KEY_ARROW_RIGHT':
	      if(down && animation && !animation.timer){
	          animation.next();
	          thumbnails[currentThumb].updateLabels(currentThumb);
		      handled();
	      }
	      break;
	      
	  case 'KEY_A':
	      imageLoaded=true;
	      signal(events, 'activeThumbnailChanged', thumbnails[currentThumb]);
	      break;
	      
	  case 'KEY_SPACEBAR':
	      log('toggling pause');
	      if(down && animation) {
		      if(animation.timer) {
		           log('pausing animation');
		           animation.pause();
		      }
		      else {
		           log('starting animation');
		           animation.start();
		      }
	          thumbnails[currentThumb].updateLabels(currentThumb);
	      }
	      break;
	      
	  default:
	      if(down && (event.key().code >= 49) && (event.key().code <= 53)) {
	          if(animation) {
	              animation.pause();
	              animation.start(ANIMATION_FRAME_RATES[event.key().code-48]);
                  thumbnails[currentThumb].updateLabels(currentThumb);
	          }
	      }
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

// Moves thumbnail rectangle on screen to specified
// absolute coordinates.
function moveThumbnailRect(x, y){
  var element = $("zoomMap");
  if (element != null){
    log("Moving thumbnail zoom rect to " + x + "," + y);
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

/* ----------------------------  Paging Functions ------------------------------- */

var currentThumbnailPage=0;
var thumbnailPageSize=3;

/**
 * Causes the thumbnail strip to page backward 1 page
 */
function gotoPreviousPage() {
  if(currentThumbnailPage>0) {
    currentThumbnailPage--;
    initializeThumbnails();
    signal(events, 'activeThumbnailChanged', thumbnails[currentThumb]);
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

function showNotificationForm() {
  window.parent.currentTab.mode = 'edit';
  document.location.href='updateCcr.do?ccrIndex='+window.parent.currentTab.ccrIndex+'&forward=transaction&mode=edit';
}

function showViewerHelp() {
  open(accountsBaseURL+'/FAQandHelp.pdf', 'mchelp','scrollbars=1,width=640,height=480'); 
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

  var seriesIndex = thumbnails[currentThumb].series.index;

  // Can't modify fixed CCRs - make them open a new one
  if(ccr.storageMode == 'FIXED') {
    alert("This CCR has already been saved and cannot be modified.\r\n\r\nPlease select 'Edit as New CCR' to create a New CCR before Confirming");
    return;
  }

  if(ccr.guid && ccr.guid != '') {
    execJSONRequest('newTrackingNumber.do?ccrIndex='+ccrIndex,null,function(res) {
        ccrIndex = res.ccrIndex;
        if(framed)
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
      setModified();
      window.location.href='viewEditCCR.do?mode=view&ccrIndex='+ccrIndex;
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
      {ccrIndex: ccrIndex, seriesIndex: seriesIndex, displayIndex: false});
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
  execJSONRequest("SetEmergencyCCR.action", {ccrIndex: ccrIndex}, function(result) {
      if(framed)
	      window.parent.replaceTab('Emergency CCR','viewEditCCR.do?ccrIndex='+(result.ccrIndex),'',window.parent.nextTab());
      alert('The current CCR has been set as your Emergency CCR');
  });
}

function createCCR() {
  document.location.href='validateCcr.do';
}

function editCCR() {
  showNotificationForm();
  return false;
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
  for(var i=0; i<p.studies[0].series.length;++i) {
    if(p.studies[0].series[i] == aSeries) {
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

  // Because the iPhone and similar mobile browsers don't
  // use space for scrollbars they have extra space
  var extra = 5;
  if(YAHOO.env.ua.webkit && YAHOO.env.ua.mobile) 
      extra = 0;
  var width = (window.document.body.offsetWidth - leftWidth) - extra;
  
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
  return ccrs[p.studies[0].series[0].mcGUID];
}

function getStorageId(){
  return(storageId); 
}

function sendDownloadReferencesCommand() {
    clearAuthorizationContext();
    downloadDocumentAttachments (storageId,ccrGuid, cxp2Protocol, cxp2Host, cxp2Port, cxp2Path, groupName,groupId, accId, auth);
}

function downloadDICOM() {
    
    if(!storageId || storageId == '') {
        alert('This CCR is not associated with a MedCommons Patient Account.\n\n'
             +'You can only download DICOM from CCRs that have a patient account associated with them');
        return;
    }
        
    if(ccrGuid == null){
        alert("This CCR must be saved before you can download DICOM.  Please Save the CCR and try again.");
        return;
    } 
    
    // Note hack to make refresh for new page load:  use cookie event src id.
    // Should get rid of this eventually when we can implement hash based 
    // file names
    YAHOO.util.Get.script("contextManager.js?rand="+parent.ce_src_id, {onSuccess: function() { 
        if(ddlRunning) {
            sendDownloadReferencesCommand();
            return;
        } 
        
        // DDL is not running
        connect(ddlEvents, 'ddlStarted', function() {
            sendDownloadReferencesCommand();
            disconnectAllTo(ddlEvents, arguments.callee);
        }); 
        
        pingDDL(showStartDDLHelp);
    }});     
}

function shareForm() {
    if(isMobileBrowser())
        window.top.location.href='/router/form?patientId='+ccr.patientId+'&next='+encodeURIComponent(window.top.location.href);
    else
       showSharingDialog({});
}

var thumbnailGridDisplayed=false;
function showThumbnailGrid() {
    var hidden = false;
    if(thumbnails[currentThumb].series.mimeType=="application/pdf") {
        hide('mainImage');
        hidden = true;
    }
    
    hide('thumbnailGrid');
    $('thumbnailGrid').style.left = -document.body.offsetWidth+'px';
    YUI({base: 'yui3/3.2.0/'}).use('node', 'transition', function(Y) {
      var base = 1000;
      log("creating grid with " + thumbnails.length + " thumbnails");
      $('thumbnailGrid').innerHTML = '';
      for(var i=1; i<thumbnails.length; ++i) {
          var div;
          var n = i;
         appendChildNodes('thumbnailGrid', div=makeThumbCell(base+i, function(cell, ann) {
             var k = parseInt(''+n);
             connect(cell, touchEvents.ontouchstart, function() {
                thumbnails[k].display();
                // Argh - different handling for IE because although opacity
                // works, it leaves behind un-anti-aliased text which looks horrible
                if(Y.UA.ie) {
                    var underlay = Y.one('#underlay');
                    underlay.on('transition:end', function() {
                       hide('underlay'); 
                       document.body.style.overflow='auto';
                    });
                    underlay.transition({opacity: 0});
                }
                else
                Y.one('#ViewerArea').transition({
                    duration: 0.4,
                    opacity: 1.0
                });
                
                Y.one('#thumbnailGrid').transition({
                    duration: 0.4,
                    left: '-'+document.body.offsetWidth+'px'
                }, function() { hide('thumbnailGrid');});    
                if(hidden) 
                    show('mainImage');
                thumbnailGridDisplayed=false;
             });
         }));  
         addElementClass(div,'gridThumb');
         thumbnails[i].displayThumbnail(base+i);
      }
      
      if(Y.UA.ie) {
          lightbox(DIV(),null,0.8);
          document.body.style.overflow='hidden';
      }
      else 
          Y.one('#ViewerArea').transition({
            duration: 0.4,
            opacity: 0.2
          });
      
      Y.one('#thumbnailGrid').transition({
          duration: 0.4,
          left: '0px'
      });    
  });    
    
  appear('thumbnailGrid');
  thumbnailGridDisplayed=true;
  return false;
}