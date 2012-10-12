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
var currentSeries = -1;

var fast = false;//true;

var maxRows ;
var maxColumns;
var imagesSkipped = 0;
var centralURL = "http://medcommons.net:8080";
var centralRouterURL = "http://medcommons.net:9080";




 // Flag to prevent multiple image requests from being made
 // while an image load is in progress.
 // Calling applications should invoke isImageLoaded() before
 // invoking setImage() to load next image.
var imageLoaded = false; 

// This is the number of the image that is currently loading
// only valid if imageLoaded = true
var imageLoading = -1;

function Patient(PatientName, PatientID){
	this.PatientName =PatientName;
	this.PatientID = PatientID;
	this.StudiesArray = new Array();
};
	
	
function Study(StudyDescription, StudyInstanceUID, StudyDate, StudyTime ){
	this.StudyDescription = StudyDescription;
	this.StudyInstanceUID = StudyInstanceUID;
	this.StudyDate = StudyDate;
	this.StudyTime = StudyTime;
	this.SeriesArray=new Array();
};

function Series(SeriesDescription, SeriesInstanceUID, Modality, SeriesNumber){
	this.SeriesDescription=SeriesDescription;
	this.SeriesInstanceUID=SeriesInstanceUID;
	this.Modality=Modality;
	this.SeriesNumber = parseInt(SeriesNumber);
	this.InstanceArray = new Array();
}

function Instance(SOPInstanceUID, InstanceNumber, FileReferenceID, window, level){
	var windowSpecified = true;
	this.InstanceNumber= parseInt(InstanceNumber);
	this.SOPInstanceUID = SOPInstanceUID;
	this.FileReferenceID=FileReferenceID;
	// DEMO HACK only.
	// In real world if the window/level values aren't set
	// the server should make some based on 
	// image attributes.
	if ((window == null) || (window == 0)){
		this.defaultWindow = 500;
		windowSpecified = false;
		}
	else{
		this.defaultWindow = window;
		}
		
	
	if (
		(windowSpecified == false) &&
		((level==null) || (level == 0)) 
		){
			this.defaultLevel = 200;
			}
	else{
		this.defaultLevel = level;
	}
	}
	
// Constants used in ToolState object.
var toolZoom = "Zoom";
var toolWL     = "WindowLevel";
var toolWLSelect = "WindowLevelSelect";
var patientOnlyAnnotation = "&annotation=patient";
var allAnnotation = "&annotation=patient,technique";


// Creates object used by event handlers to determine
// which actions are invoked.
function ToolState( ){
	this.activeTool = toolZoom;
	
	// currentRegion contains the region of the image to be displayed (zoomed). 
	// It is initially null; it is changed to null whenever a series boundary is crossed.
	this.currentRegion = null;
	
	
	// currentAnnotation contains the WADO URL arguments for overlay arguments.
	// There are two states: one draws only patient demographics; the other paints 
	// demographics plus image and study information. These states correspond to
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
	//currentToolState.currentWindowLevel = null;
	if (currentToolState.activeTool == toolWL)
		setZoomActive();
	else
		setWindowLevelActive();
	displayCurrentImage();
	
}
/**
 * WADO.js
 * To do: coordinates aren't quite right (window vs. image). 
 * 
 **/
 function beginDrag(element, event){
 	if (!event) event= window.event; // IE
 	imageLoaded = true;
 	calculateImageDimensions();
 	// If the drag starts on the far right border then
 	// it's going to be a image scrolling drag within
 	// a series.
 	var x = event.clientX;
 	if (x > scrollDragLimitX)
 		beginScrollDrag(element, event)	
 		
 	else if (currentToolState.activeTool == toolZoom)
 		beginZoomDrag(element, event);
 		
 	else if (currentToolState.activeTool == toolWLSelect)
 		selectWindowLevel(element, event);
 		
 	else if (currentToolState.activeTool == toolWL)
 		beginWindowLevelDragTemp(element, event);
 	else
 		window.status="ignored activeTool is " + currentToolState.activeTool;
 }
function beginScrollDrag(elementToZoom, event) {

    startX = event.clientX;
    startY = event.clientY;
	var nImages = numberOfImageInSeries(currentSeries);
	var nPixelsPerImage = imageHeight/nImages;
	var selectedImage;
	

    // Add event handlers (removed in upHandler)
   if (document.addEventListener) { // DOM Level 2 Event Model
    	document.addEventListener("mousemove", trackScrollHandler, true);
    	document.addEventListener("mouseup", upHandler, true);
    }
    else if (document.attachEvent){
    	document.attachEvent("onmousemove", trackScrollHandler);
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
    function trackScrollHandler(event) {

	if (!event) event= window.event; // IE
	endX = event.clientX;
	endY = event.clientY;
	selectedImage = Math.round(endY / nPixelsPerImage);
	 // We've handled this event.  Don't let anybody else see it.
    if (event.stopPropagation)
		event.stopPropagation(); // DOM Level 2
   	else
   		event.cancelBubble = true; // IE
   		
   	if (event.preventDefault)
   		event.preventDefault(); // DOM Level 2
   	else
   		event.returnValue = false; // IE
   		
	window.status="Scroll to image " + selectedImage + " out of " + nImages;
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
		document.removeEventListener("mousemove", trackScrollHandler, true);
	} 
	// IE Bug: handlers not yet removed.
	else if (document.detachEvent){
		window.status="Detach";
		document.detachEvent("onmouseup", upHandler);
		document.detachEvent("onmousemove", trackScrollHandler);
	}
	// And don't let the event propagate any further
	 if (event.stopPropagation)
		event.stopPropagation(); // DOM Level 2
   	else
   		event.cancelBubble = true; // IE
	
	displaySelectedImage(selectedImage);
	//alert("selected image is " + Math.round( selectedImage));
    }
    
    } // End scroll drag.
    
function beginZoomDrag(elementToZoom, event) {

    startX = event.clientX;
    startY = event.clientY;


    calculateImageDimensions();

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
	
	handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight);
	updateDisplayedThumbnail();
	setThumbnailRectangleOnSeries();
    }
    
    } // End zoom drag.
 
function beginWindowLevelDragTemp(elementToZoom, event) {

    startX = event.clientX;
    startY = event.clientY;

 	calculateImageDimensions();

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
		generateWindowLevelGrid();
	handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight);
    }
    
 }   // End window level drag
 
 function beginWindowLevelDrag(element, event){
 	generateWindowLevelGrid();
 	displayCurrentImage();
 }
 
// Broken.
// Appears that when arrow leaves rectagle
// durning mouse move that it no longer gets events.
function beginZoomRectangleDrag(elementToDrag, event) {
	//alert("beginZoomrectanglegdrag");
    // Compute the distance between the upper-left corner of the element
    // and the mouse click. The moveHandler function below needs these values.
    startX = event.clientX - parseInt(elementToDrag.style.left);
    startY = event.clientY - parseInt(elementToDrag.style.top);
    //alert("event.clientX = " + event.clientX + ", " +  elementToDrag.style.left +
   // 	", deltaX = " + deltaX);

    // Register the event handlers that will respond to the mousemove events
    // and the mouseup event that follow this mousedown event.  
    if (document.addEventListener) {  // DOM Level 2 Event Model
	// Register capturing event handlers
        document.addEventListener("mousemove", moveHandler, true);
		document.addEventListener("mouseup", upHandler, true);
    }
    else if (document.attachEvent) {  // IE 5+ Event Model
		// In the IE Event model, we can't capture events, so these handlers
		// are triggered when only if the event bubbles up to them.
		// This assumes that there aren't any intervening elements that
		// handle the events and stop them from bubbling.
		document.attachEvent("onmousemove", moveHandler);
		document.attachEvent("onmouseup", upHandler);
    }


    // We've handled this event.  Don't let anybody else see it.  
    if (event.stopPropagation) event.stopPropagation();   // DOM Level 2
    else event.cancelBubble = true;                       // IE

    // Now prevent any default action.
    if (event.preventDefault) event.preventDefault();     // DOM Level 2
    else event.returnValue = false;                       // IE

    /**
     * This is the handler that captures mousemove events when an element
     * is being dragged.  It is responsible for moving the element.
     **/
    function moveHandler(e) {
    	//alert("about to move");
	if (!e) e = window.event;  // IE event model
	
	endX = event.clientX;
	endY = event.clientY;
	//var element = document.getElementById("zoomMap");
        // Move the element to the current mouse position, adjusted as
	// necessary by the offset of the initial mouse click.
	//elementToDrag.style.left = (e.clientX - deltaX) + "px";
	//elementToDrag.style.top = (e.clientY - deltaY) + "px";
	//elementToDrag.style.left = (e.clientX -5) + "px";
	//elementToDrag.style.top = (e.clientY -5) + "px";
moveThumbnailRect(endX , endY);
	//window.status="X=" + e.clientX + ", " +  deltaX + ", Y=" + e.clientY +", " + deltaY;
	//window.status="moving...";
	// And don't let anyone else see this event.
	if (e.stopPropagation) e.stopPropagation();       // DOM Level 2
	else e.cancelBubble = true;                       // IE
    }

    /**
     * This is the handler that captures the final mouseup event that
     * occurs at the end of a drag.
     **/
    function upHandler(e) {
	if (!e) e = window.event;  // IE event model

	// Unregister the capturing event handlers.
	if (document.removeEventListener) {    // DOM Event Model
	    document.removeEventListener("mouseup", upHandler, true);
	    document.removeEventListener("mousemove", moveHandler, true);
	}
	else if (document.detachEvent) {       // IE 5+ Event Model
	    document.detachEvent("onmouseup", upHandler);
	    document.detachEvent("onmousemove", moveHandler);
	}


	// And don't let the event propagate any further.
	if (e.stopPropagation) e.stopPropagation();       // DOM Level 2
	else e.cancelBubble = true;                       // IE

	updateZoomRectangle(e.clientX, e.clientY);
	//alert("uphandler " + e.clientX);
    }
}
// Displays thumbnail of current image instead
// of default 'middle' image for series.
function updateDisplayedThumbnail(){
		//alert("thumbReference=" + thumbReference);
		//var thumbImage = series.InstanceArray[thumbImage];
		var seriesName = "series" + currentSeries;
		var thumbImage= p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[currentImage];
		//alert("thumbImage=" + thumbImage);
		var thumbURL = "/router/WADO?studyUID=";
		thumbURL+=p.StudiesArray[0].StudyInstanceUID;
		thumbURL+="&rows=140&columns=140&fname=";
		thumbURL+=thumbImage.FileReferenceID;
		thumbURL+="&windowCenter=";
		thumbURL+=thumbImage.defaultLevel;
		thumbURL+="&windowWidth=";
		thumbURL+=thumbImage.defaultWindow;
		
		//$$
		//alert(thumbURL);
		document.images[seriesName].src =thumbURL;
}
 function calculateImageDimensions(){
  	imageWidth =document.image.width;
 	imageHeight = document.image.height;
 	scrollDragLimitX = imageWidth - 32;
 }
 function selectWindowLevel(element, event){
 	calculateImageDimensions();
  	var clickX = event.clientX/(imageWidth/3);
    var clickY = event.clientY/(imageHeight/3);
    var tileRow = Math.floor(clickX) ;
    var tileCol = Math.floor(clickY);
    var index = ( tileCol * 3) + tileRow;
    
     var window = currentToolState.currentWindowArray[index];
    var level = currentToolState.currentLevelArray[index];
    
//    alert("window level: row:" + tileRow + ", col:" + tileCol + ", index:" + index +
//    ", clickX:" + clickX + ", clickY=" + clickY + ", imageWidth = " + imageWidth +
//   ", imageHeight = " + imageHeight +
//   ", window =" + window + ", level=" + level);
//   
//   var temp = "window level array";
//   for (var i=0;i<9;i++){
//   	temp +="\n " + i + " w: " + currentToolState.currentWindowArray[i] + ", l:" + 
//   		currentToolState.currentLevelArray[i];
//   }
//   alert(temp);
    setWindowLevel(window, level);
 	// set state back to window/level.
 	setWindowLevelActive();
 	currentToolState.currentRegion = null;
 	displayCurrentImage();
 }
function displaySelectedSeries(n){
	if ((FormType != null) &&
		(FormType == OrderFormType)){
		// Then this was clicked on the order form; launch the WADO viewer.
		urlParameters+="&selectedSeries=";
		urlParameters+=n;
		showWADO(urlParameters);//
	}
	else{
		// Elese show the selected series.
		selectSeries(n);
    currentThumb = n+1;
		displayCurrentImage();
	}
	//alert("DisplaySelectedSeries");
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

var cacheImage = null;

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
  if (this.imageNum >= numberOfImageInSeries(this.series)) {
		var nSeries = numberOfSeries();
		this.series++;
		resetSeriesChange();
		if (this.series>=nSeries) {
			this.series = 0;
			this.imageNum = 0;
			}
		else{
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
		this.series--;    
		resetSeriesChange();
		var nSeries = numberOfSeries();
		if (this.series < 0){
			this.series = nSeries -1;
			resetSeriesChange();
		}
		this.imageNum = numberOfImageInSeries(this.series)-1;
	}
}

function ImageSpecCache() {
  var imageURL = getImageUrl(this.series, this.imageNum);
  cacheImage = new Image();
  cacheImage.src = imageURL;
  //alert("Caching image " + imageURL);
}

/*************************************************/


function displayPreviousImage(){
	currentImage--;
	if (currentImage < 0){
		currentSeries--;    
		resetSeriesChange();
		var nSeries = numberOfSeries();
		if (currentSeries < 0){
			currentSeries = nSeries -1;
			resetSeriesChange();
		}
		currentImage = numberOfImageInSeries(currentSeries)-1;
	}

	displayCurrentImage();
}

function displayNextImage(){
	currentImage++;
	if (currentImage < 0) {
		currentImage=0;
		}
	else if (currentImage >= numberOfImageInSeries(currentSeries)){
		var nSeries = numberOfSeries();
		currentSeries++;
		resetSeriesChange();
		if (currentSeries>=nSeries){
			currentSeries = 0;
			currentImage = 0;
      currentThumb = 1;
			}
		else{
			currentImage = 0;
		}	
  }
	displayCurrentImage();
}

function handleMousewheel(n){
	//alert ("handleMousewheel " + n);
	
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
function selectSeries(n){
	var previousSeries = currentSeries;
	resetSeriesChange();
	currentSeries = n;
	//eval("document.series" + n + ".border = 5;");
	currentImage = 0;
  updateActiveImage();
}

function updateActiveImage() {
  var nSeries = numberOfSeries();

  // alert('image ' + currentSeries + ' active');
  for(i=0; i<nSeries; i++) {
     var nImages = numberOfImageInSeries(i);
     if(i==currentSeries) {
      var imageLabel =   "Image " + (currentImage + 1) + "/" + nImages;
      replaceText(document.getElementById("thumbLabel" + currentSeries),imageLabel);	
     }
     else {
        replaceText(document.getElementById("thumbLabel" + i), nImages + " Images" );	
     }
  }
    
  for(thumbIndex=0; thumbIndex<4; thumbIndex++) {     
    thumbCellName = "thumbCell" + thumbIndex;	
    if(document.getElementById(thumbCellName) != null) {
      if(currentThumb == thumbIndex) {
        document.getElementById(thumbCellName).className="SelectedImage";
      }
      else {
        document.getElementById(thumbCellName).className="NotSelectedImage";
      }
    }
  }
}

function Initialize(){
	p.StudiesArray[0].SeriesArray.sort(function (a,b){
		return (a.SeriesNumber - b.SeriesNumber);
	});
	for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
		p.StudiesArray[0].SeriesArray[i].InstanceArray.sort(function(a,b){
			return(a.InstanceNumber - b.InstanceNumber);
		});
	}
	//displayStudyArrays();
	
	var maxSeries = numberOfSeries();
	if (maxSeries > 3)
		maxSeries = 3;
	
	for (i=0;i<maxSeries;i++){
		var seriesName = "series" + i;
		var seriesDescription = p.StudiesArray[0].SeriesArray[i].SeriesDescription;
		
		//alert("seriesName = " + seriesName);
	// In future set series titles..
		// Grab middle image
		var nImages = numberOfImageInSeries(i);
		var thumbReference = Math.round((nImages-1)/2);
		//alert("thumbReference=" + thumbReference);
		//var thumbImage = series.InstanceArray[thumbImage];
		var thumbImage= p.StudiesArray[0].SeriesArray[i].InstanceArray[thumbReference];
		//alert("thumbImage=" + thumbImage);
		var thumbURL = "/router/WADO?studyUID=";
		thumbURL+=p.StudiesArray[0].StudyInstanceUID;
		thumbURL+="&rows=140&columns=140&fname=";
		thumbURL+=thumbImage.FileReferenceID;
		thumbURL+="&windowCenter=";
		thumbURL+=thumbImage.defaultLevel;
		thumbURL+="&windowWidth=";
		thumbURL+=thumbImage.defaultWindow;
		
		//$$
		//alert(thumbURL);
		document.images[seriesName].src =thumbURL;
		replaceText(document.getElementById("thumbDescription" + i),
			seriesDescription);
	}
	
	if (currentSeries == -1)
		selectSeries(0);
	else
		selectSeries(currentSeries); // Was set in URL.
	currentImage = 0;
	setZoomActive();
	displayCurrentImage();
}	

function displayStudyArrays(){
	
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

var currentImage = -1;
var currentSeries = -1;
var currentThumb = -1;

function displayCurrentImage(){
		
		if (isImageLoaded() == false){
			imagesSkipped++;
      //document.image.style.display='none';
			window.status="Skipping image " + imagesSkipped;
			return;
		}
    imageLoading = currentImage;
		
	  var nImages = numberOfImageInSeries(currentSeries);
		window.status="Series:" + currentSeries + ", image:" + currentImage + " / " + nImages;
    updateActiveImage();
    
    var imageURL = this.getImageUrl(currentSeries, currentImage);
    if(imageURL == null)
      return;

	 	setImage(imageURL);        
    updateActiveImage();
}

/**
 * Returns a URL for the given series and image
 */
function getImageUrl(urlSeries,urlImage) {
	 	var image= p.StudiesArray[0].SeriesArray[urlSeries].InstanceArray[urlImage];
	 	if (image ==null){
	 		alert("Series " + urlSeries + " image " + urlImage + " is null");
	 		return null;
	 		}
	 	if (image.FileReferenceID ==null){
	 		alert("Series " + urlSeries + " image " + urlImage + " FileReferenceID is null");
	 		return null;
	 		}

	 	var imageURL = "/router/WADO?studyUID=";
	 	imageURL+= p.StudiesArray[0].StudyInstanceUID; 
	 	imageURL+= "&fname=";
	 	imageURL+= image.FileReferenceID;
	 	imageURL+= "&patientName=";
	 	imageURL+= escape(p.PatientName);
	 	imageURL+=currentToolState.currentAnnotation;
	 	if (currentToolState.showImagesFullSize == false){
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
			currentToolState.currentWindow = image.defaultWindow;
			currentToolState.currentLevel = image.defaultLevel;
		}
		else
			imageURL+=currentToolState.currentWindowLevel;
    return imageURL;
}

function handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight){
	//alert("zoom region:" + startX + "," + startY + "," + endX + "," + endY + ",w=" + imageWidth	+ " h=" + imageHeight);
		var x1 = (1.0 * startX)/(1.0 * imageWidth);
		var y1 = (1.0 * startY)/(1.0 * imageHeight);
		var x2 = (1.0 *  endX)/(1.0 * imageWidth);
		var y2 = (1.0 * endY)/ (1.0 * imageHeight);
		
		
		if (x1 > x2){
			var temp;
			temp = x1; x1=x2; x2=temp;
		}
		if (y1 > y2){
			var temp;
			temp = y1; y1=y2; y2=temp;
		}
	
		// Set min zoom size (10:1)
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

	  displayCurrentImage();
}
 
 
 // Image Loading Routers
 
 
 // Invoked each time that an image has been loaded to set
 // the imageLoaded flag to 'true'.
 function imageLoadCompleted(){
  window.status = "Image Loaded.";
 	imageLoaded = true;
  if((imageLoading != currentImage) && (imageLoading != -1)) {
   var difference = currentImage - imageLoading;
   displayCurrentImage();   
   window.status = "Caught up " + difference + " images";
  }
  else {
    // Since user is up-to-date with all key presses etc.
    // start caching the next image in advance
    cacheNextImage();
    cachePrevImage();
  }
 }


function cacheNextImage() {
   
   if(currentSeries >= 0) {
     // We have now caught up loading images - lets cache the next one
     // so that there is an instant response when the user next
     // clicks
     var imageSpec = new ImageSpec(currentSeries,currentImage);
     imageSpec.next();
     imageSpec.cache();
  }
}
 
function cachePrevImage() {
   
   if(currentSeries >= 0) {
     // We have now caught up loading images - lets cache the next one
     // so that there is an instant response when the user next
     // clicks
     var imageSpec = new ImageSpec(currentSeries,currentImage);
     imageSpec.prev();
     imageSpec.cache();
     //window.status = "Prev image cached.";
  }
}

function imageLoadAborted(){
	imageLoaded = true;
	//alert("image load aborted");
}
function imageLoadError(){
	imageLoaded = true;
	//alert("image load error");
}
 // Returns true if the image has completely loaded, false otherwise.
 function isImageLoaded(){
  
 	if(imageLoaded)
    return true;

  if(image.complete)
    return true;

  return false;
 }
 
 // Sets the image src attribute and sets the imageLoaded flag
 // to false.
 function setImage(url){
 	if (document.image) {
 		document.image.src=url;
  }

 	imageLoaded = false;
 }



// Returns direction of mousewheel. 1 is forward, -1 is backward, 0 is nothing.
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
	currentToolState.currentAnnotation=allAnnotation;
	currentToolState.currentWindowLevel = null;
	imageLoaded = true;
	moveThumbnailRect(-100, -100);
  currentThumb = currentSeries + 1;
  //alert('currentThumb=' + currentSeries);
}

function resetViewer(){
	resetSeriesChange();
	fast= false;
	displayCurrentImage();
	
}
// *** WARNING - These need to be derived dynamically - 
// if the screen dimentions change all overlays will be
// wrong.
	var seriesThumbnailY = 744;
	var seriesThumbnailX = 140 + 8;
	var seriesXSize = 140;
	var seriesBorderSize = 5;
	var seriesYSize = 140;
	
function updateZoomRectangle(thumbX, thumbY){
	var offsetX = thumbX - (seriesThumbnailX + (seriesXSize +seriesBorderSize)*
		currentSeries );
	var offsetY = thumbY - seriesThumbnailY;

	var regionX1 = offsetX/(seriesXSize * 1.0);
	var regionY1 = offsetY/(seriesYSize * 1.0);
		
	if (regionX1 > 1)
		alert("RegionX1=" + regionX1);
	if (regionY1 > 1)
		alert("RegionY1=" + regionY1);
	var regionDeltaX =  regionX1 - currentToolState.regionX1  ;
	var regionDeltaY =  regionY1 - currentToolState.regionY1  ;
	var x1 = regionX1 * imageWidth;
	// Just modified. 
	var x2 = (currentToolState.regionX2 + regionDeltaX) * imageWidth;
	var y1 = regionY1 * imageHeight;
	var y2 = (currentToolState.regionY2 + regionDeltaY) * imageHeight;
	
	handleZoomRegion(x1,y1,x2,y2, imageWidth, imageHeight);
	 setThumbnailRectangleOnSeries();
	//displayCurrentImage();
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
	
	var thumbY = seriesThumbnailY + offsetY;
	var thumbX = seriesThumbnailX + (seriesXSize +seriesBorderSize)*
		currentSeries + offsetX;
	setThumbnailRectSize(thumbW, thumbH);
	moveThumbnailRect(thumbX, thumbY);	
	// update thumbnail to be current image
}
// Treats up/down arrows the same as mousewheel events.
// Works in Mozilla and IE.
function handleKeyPress(event) {
	if (!event) event= window.event; // IE
    var keyCode =
	    document.layers ? event.which :
	    document.all ? event.keyCode :
	    document.getElementById ? event.keyCode : 0;

    if (keyCode == 38) {
      window.status = "-1";
      handleMousewheel(-1);
    }
    else if (keyCode == 40) {
      window.status = "1";
        handleMousewheel(1);
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
}

function toggleOverlay(){
	if (currentToolState.currentAnnotation==allAnnotation)
		currentToolState.currentAnnotation = patientOnlyAnnotation;
	else
		currentToolState.currentAnnotation=allAnnotation;
		
	displayCurrentImage();	
}
function toggleFullSize(){
	currentToolState.showImagesFullSize = !currentToolState.showImagesFullSize;
	displayCurrentImage();
	
}
// Example URL
//http://medcommons.net:9080/router/OrderForm.jsp?guid=b0b78b3e828a90078ec4923d13c7fd60&name=Bill%20Donner&tracking=1234&address=123%20Lucky%20St&state=MT&city=Butte&zip=83132&cardnumber=6112574478132115&amount=50.00&tax=4.00&charge=54.00&expiration=12/07
function showOrder(passthruURLs){
		var newURL ="/router/OrderForm.jsp";
		newURL += passthruURLs;
		location.replace(newURL);
    currentThumb=0;
}
function showWADO(passthruURLs){
		var newURL ="/router/WADOViewer.jsp";
		newURL += passthruURLs;
		location.replace(newURL);
}

// Handler for the menu on the bottom of the page.
function menuSelect(selection){
	var value = parseInt(selection);
	//alert ("selection=" + selection + ", value=" + value);
	if (selection=="ABOUT") {
		alert('MedCommons Demo System \r\nVersion ' + version + '\r\nBuild Date  ' + buildDate + '\r\n\r\nCopyright © 2004 MedCommons, Inc.\r\n52 Marshall St.\r\nWatertown MA 02472\r\nUSA');
  }
	else if (selection=="HELP") {
   //NewWindow('manual.php.htm', 'Help', 620, 350, 'yes');
   help = window.open('manual.php.htm','Help','scrollbars=1,width=720,height=550,resizable=1');
  }
	else if (selection=="ORDER0") {
    showOrder(urlParameters);
  }
	else if (selection=="ORDER")
		showOrder(urlParameters + '&ordertype=new');
	else if ((FormType != null) && (FormType == WADOFormType))
		displaySelectedSeries(value);
	else if ((FormType != null) && (FormType == OrderFormType)){
		if (value != NaN){
			urlParameters+="&selectedSeries=";
			urlParameters+=value;
		}
		showWADO(urlParameters);
	}
	
		
	//alert("Menu selection=" + selection);
	
}

function displayHIPAALog(trackingNumber){
	//location.replace("/router/HipaaStaticLogPage.html");
  window.open("/router/my_account.jsp","mcwindow");
	// location.replace("/router/my_account.php.htm");
}

// Moves thumbnail rectangle on screen to specified
// absolute coordinates.
function moveThumbnailRect(x, y){
	var element = document.getElementById("zoomMap");
	//alert(element.style.left);
	if (element != null){
		element.style.left = x + "px";
		element.style.top  = y + "px";
	}

}
// Sets size of thumbnail rect
function setThumbnailRectSize(width, height){
	var element = document.zoomRectangle;
	element.width = width;
	element.height = height;

}

function replaceText(n, newText) {
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
	var obj = document.getElementById(name).style;
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
	//history.go(-1);
	//location.replace("/router/initSelection.do");
	//history.go("initSelection");
  window.close();
}
