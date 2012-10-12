
/************************* Constants ********************************/

/**
 * The difference in vertical position b/w open and closed
 * folders. 
 */
var openClosedHeightOffset = 200;

/**
 * Common mime-types
 */
var CCR_MIME_TYPE = "application/x-ccr+xml";

/**
 * Mime-type to image translations
 */
var mimeTypeImages = new Array();
mimeTypeImages["application/pdf"]='images/pdfthumb-45x45.gif';
mimeTypeImages["image/jpg"]='images/picturethumbnail-45x45.gif';
mimeTypeImages["image/pjpeg"]='images/picturethumbnail-45x45.gif';
mimeTypeImages["image/jpeg"]='images/picturethumbnail-45x45.gif';
mimeTypeImages["image/gif"]='images/picturethumbnail-45x45.gif';
mimeTypeImages["text/x-cdar1+xml"]='images/cdathumbnail-45x45.gif';
mimeTypeImages["application/x-hl7"]='images/hl7thumb-45x45.gif';
mimeTypeImages[CCR_MIME_TYPE]='images/ccrthumb-45x45.gif';


/**
 * Colors for unvalidated folder
 */
var unvalidColor1='#ffffff';
var unvalidColor2='#c1d1c4';
var unvalidHighlighColor='#ee9999';          

/**
 * Folders
 */
var folders = new Array();

/**
 * Folder Class
 */
function Folder(ccrs,prefix) {
  if(ccrs.length) {
    this.ccrs = ccrs;
  }
  else {
    this.ccrs.push(ccrs);
  }
  if(prefix)
    this.prefix = prefix;
}

/**
 * Folder.open() method - performs operations common to all folder types for opening a folder.
 */
Folder.prototype.open = function() {
  // Make sure the background image is showing on the open folder
  document.getElementById('open-ccrRecord-0').style.display='block';
  document.getElementById('open-thumbnailLabel-0').style.display='block';
  document.getElementById('open-thumbnailLabel-0').innerHTML=this.getElement('thumbnailLabel').innerHTML;

}

/**
 * Folder.close() method
 */
Folder.prototype.close = function() {

  this.folderDiv.style.backgroundImage="url('images/closedfolder.gif')";
  this.folderDiv.style.left=(230 + this.index*16) + 'px';
  this.folderDiv.style.width='330px';
  this.getElement('ccrRecord').style.top=(openClosedHeightOffset - this.index*20) + 'px';

  // Hide unwanted stuff
  if(this.bodySectionContainerDiv)
    this.bodySectionContainerDiv.style.display='none';
  if(this.ccrInfoDiv)
    this.ccrInfoDiv.style.display='none';

  // Move the thumbnails
  this.thumbnailContainerDiv.style.left=(this.folderDiv.style.width).replace(/px/,'')-20;
  this.thumbnailContainerDiv.style.top='25px';
  this.thumbnailContainerDiv.style.width='75px';

  // Hide all thumbnails >= 4
  for(var i=0; i<this.thumbBoxes.length; i++) {
    this.thumbBoxes[i].style.display=(i<4) ? 'block' : 'none';
  }

  // If more than 4 thumbs, show more image
  if(this.moreThumbnailBoxDiv) {
    log("folder " + this.index + " showing more thumb " + (this.thumbBoxes.length>4) ? 'block' : 'none');
    this.moreThumbnailBoxDiv.style.display=(this.thumbBoxes.length>4) ? 'block' : 'none';
  }

  // No drop shadows
  for(var i=0; i<this.thumbBoxes.length; i++) {
    this.thumbBoxes[i].style.backgroundImage='none';
  }
  this.folderTabDiv.style.left=(this.folderDiv.style.width).replace(/px/,'')-105;
  this.folderTabDiv.style.top='6px';
  this.folderTabDiv.style.fontSize='11px'; 
  this.getElement('folder').style.display='block'; 

  this.getElement('ccrRecord').style.zIndex=(60 - this.index*3);
  this.getElement('foldertabbutton').src = 'images/plusbutton.png';
  this.getElement('moreButton').style.display='none';
  var creationDate = this.getElement('creationDate');
  if(creationDate)
    creationDate.style.color='#616161';
}

Folder.prototype.getElement = function(elId) {
  if(this.prefix) {
    log(this.prefix+'-'+elId+'-'+this.displayIndex);
    return document.getElementById(this.prefix+'-'+elId+'-'+this.displayIndex);
  }
  else
    return document.getElementById(elId +'-'+this.displayIndex);
}

Folder.prototype.ccrs = new Array();
Folder.prototype.thumbBoxes = new Array();
Folder.prototype.prefix = '';

function onThumbClick(thumbnailBoxDiv) {
  var series = thumbnailBoxDiv.series;
  log("onThumbClick series=" + series);
  var folderPrefix = series.ccr.folder.prefix;
  if(thumbnailBoxDiv.prefix == 'open') {
    var openCcr = getOpenCcr();
    if(openCcr) {
      showCcrSeries(openCcr, openCcr.index, series.index);
    }
    else 
    if((pendingCcrs.length > 0) && (pendingCcrs[0].open)) {
      // showCcrSeries(series.ccr, series.ccr.index, series.index,true);
      highlightPendingSeries(series);
    }
  }
  else {
    showCcrSeries(series.ccr, series.ccr.index, series.index);
  }
}

function findPendingSeries(globalSeriesIndex) {
  var count = 0;
  for(ccrIndex=0; ccrIndex < pendingCcrs.length; ++ccrIndex,++thumbBoxIndex) {
    for(seriesIndex=0; seriesIndex<pendingCcrs[ccrIndex].series.length; ++seriesIndex) {
      if(count++ == globalSeriesIndex)
        return pendingCcrs[ccrIndex].series[seriesIndex];
    }
  }
}

function showCcrSeries(ccr, ccrIndex, seriesIndex, pending) {
  log("Showing viewer for folder " + ccrIndex + " series " + seriesIndex);
  var wadoUrl = 
    "/router/initViewer.do?guid=" + ccr.orderGuid + "&initialSeriesGuid=" + ccr.series[seriesIndex].guid;

   wadoUrl += ("&ccrIndex=" + ccrIndex);

  if(pending) {
   wadoUrl += ("&pending=true");
  }

  document.location.href=wadoUrl;
}

/**
 * Toggles the state of the given folder between open and closed.
 * If another folder is open and the requested folder is opened,
 * that other folder is closed.
 */
function toggleFolder(idPrefix, folderIndex) {

  log("toggling folder " + idPrefix + "," + folderIndex);

  // Is it the desktop folder?
  if(idPrefix == 'open-') {
    for(i=0; i<numFolders; i++) {
      if(ccrs[i].open) {
        ccrs[i].open = false;
        log("closeFolder " + i);
        closeFolder(i);        
      }
    }
    log("Hiding main folder");
    document.getElementById('open-ccrRecord-0').style.display='none';
  }
  else {
    var ccr = ccrs[folderIndex];
    if(ccr.open) {
      ccr.open = false;
      log("closing folder " + folderIndex);
      closeFolder(folderIndex);    
      document.getElementById('open-ccrRecord-0').style.display='none';
    }
    else {
      // Only 1 folder allowed open at a time.
      // first close all the folders so only the new one is open
      for(i=0; i<numFolders; i++) {
        if(ccrs[i].open && (i!=folderIndex)) {
          ccrs[i].open = false;
          closeFolder(i);
        }
      }
      ccr.open = true;
      openFolder(folderIndex);
      document.getElementById('open-ccrRecord-0').style.display='block';
    }
  }

  //updateFolderState();
}

function raiseFolder(folderIndex) {
  for(i=0; i<numFolders; ++i) {
    if(i != folderIndex) {
      log("Lowering folder " + i);
      document.getElementById('ccrRecord-'+i).style.zIndex= ''+(20 - i);
    }
    else {
      log("Raising folder " + i);
      document.getElementById('ccrRecord-'+folderIndex).style.zIndex='1000';
    }
  }
}

/**
 * Iterates over all the folders and ensures that the image for the
 * folder is consistent with the folder's open/closed status.
 */
function updateFolderState() {

  for(var ccrIndex=0; ccrIndex<ccrs.length; ccrIndex++) {
    var ccr = ccrs[ccrIndex];
    if(ccr.open) {
      openFolder(ccrIndex);
    }
    else {
      closeFolder(ccrIndex);
    }
  }
}

/**
 * Iterates over all the folders and ensures that the image for the
 * folder is consistent with the folder's open/closed status.
 */
function closeAllFolders() {
  for(var ccrIndex=0; ccrIndex<ccrs.length; ccrIndex++) {
    var ccr = ccrs[ccrIndex];
    ccr.open = false;
    closeFolder(ccrIndex);
  }
  closePendingFolder();
}

/**
 * Sets the style such that this folder appears closed
 * on the patient desktop.
 */
function closeFolder(folderIndex) {  
  // Set the background image
  log("closing folder " + folderIndex);
  ccrs[folderIndex].folder.close();
}

function setClosedThumbnailPosition(containerDiv) {
}

function openFolder(folderIndex) {
  var ccr = ccrs[folderIndex];

  if(isPendingFolderOpen())
    closePendingFolder();

  // Copy the date 
  document.getElementById('open-creationDate-0').innerHTML=ccr.creationDate;
   
  // Show table for patient info
  getRule('xds.css','.leftnav').style.display = 'block';
  document.getElementById('open-dividerLine-0').style.display = 'block';
  document.getElementById('open-notificationsHeader-0').style.display = 'block';
  document.getElementById('open-physician-0').style.display='block';

  // Move the thumbnail container
  var thumbnailContainerDiv = document.getElementById('open-thumbnailContainer-0');
  thumbnailContainerDiv.style.left=140;
  thumbnailContainerDiv.style.top=400;

  // Copy the patient details 
  loadContent('notificationTable',folderIndex);

  // Copy the Source details
  //loadContent('contactCard-Source',folderIndex);

  // Note: we set the value of the textarea rather than using
  // innerHTML because IE seems to lose the line breaks.
  // Also, FF has bugs with word wrapping if the whole text area 
  // element is replaced.  Setting value directly works everywhere.
  document.getElementById('open-ccrPurpose-0').value = document.getElementById('ccrPurpose-'+folderIndex).value;
  document.getElementById('open-bodySectionCell-0').style.display='block';
  loadContent('bodySectionCell', folderIndex);

  // Copy the track #
  // loadContent('dateTime',folderIndex);
  // loadContent('patientName',folderIndex);

  // Show all thumbnails & drop shadows
  for(var i=0; i<60; i++) {
    var thumbBox = document.getElementById('open-thumbnailBox0-'+i);

    // Note: we limit to 8 thumbnails for the normal folders.
    if((i<8) && (i<ccrs[folderIndex].folder.thumbBoxes.length)) {
      thumbBox.style.backgroundColor="#ffffff";
      thumbBox.style.display='block';
      thumbBox.prefix = 'open';
      thumbBox.series = ccrs[folderIndex].series[i+1];
      var thumbImg=document.getElementById('open-thumbnailImage0-'+i);
      thumbImg.src = ccrs[folderIndex].folder.thumbBoxes[i].thumbImg.src;
    }
    else {
      thumbBox.ccr = null;
      thumbBox.style.display='none';
    }
  }


  // If there are series that did not fit on the page, show the "more" button
  // note we subtract 1 because the first thumbnail is not displayed
  // due to it being the CCR for the folder itself
  var state='none';
  if(ccr.series.length - 1 > ccr.folder.thumbBoxes.length) {
    state='block';
  }

  document.getElementById('foldertabbutton-'+folderIndex).src = 'images/minusbutton.png';
  document.getElementById('creationDate-'+folderIndex).style.color=highlightTextColors[currentHlColor];
  document.getElementById('open-moreButton-0').style.display=state;
  ccr.folder.folderDiv.style.backgroundImage="url('" + highlightImages[currentHlImage] + "')";

  ccr.folder.open();
  ccr.open = true;
  document.ccrForm.ccrIndex.value = folderIndex;
}

function togglePendingFolder() {
  log("togglePendingFolder");
  if(isPendingFolderOpen()) {
    closeAllFolders();
  }
  else {
    closeAllFolders();
    openPendingFolder();
  }
}

function highlightPendingSeries(series) {
  var thumbBoxIndex = 0;
  var bgcolor=unvalidColor1;
  for(ccrIndex=0; ccrIndex < pendingCcrs.length; ++ccrIndex,++thumbBoxIndex) {
    pendingCcrs[ccrIndex].open=true;
    for(seriesIndex=0; seriesIndex<pendingCcrs[ccrIndex].series.length; ++seriesIndex,++thumbBoxIndex) {      
      var thumbBox = document.getElementById('open-thumbnailBox0-'+thumbBoxIndex);
      if(!thumbBox) {
        break;
      }
      if(thumbBox.series) {
        if(thumbBox.series.ccr == series.ccr) {
          thumbBox.style.backgroundColor=unvalidHighlighColor;          
        }
        else {
          thumbBox.style.backgroundColor=bgcolor;
        }
      }
    }    
    // Alternate colors for each series
    if(bgcolor==unvalidColor1) 
      bgcolor=unvalidColor2;
    else
      bgcolor=unvalidColor1;
  }
  series.ccr.selected=true;
  window.parent.setTools(toolsWithCreateCcr);
}

function openPendingFolder() {
  log("Opening pending folder");
  closeAllFolders();

  // Pending folder is always folder 0
  var pendingFolder = folders[0];

  // Hide table for patient info
  getRule('xds.css','.leftnav').style.display = 'none';
  getRule('xds.css','.ltrtxt').style.display = 'none';
  document.getElementById('open-notificationsHeader-0').style.display = 'none';
  document.getElementById('open-dividerLine-0').style.display = 'none';
  document.getElementById('open-thumbnailLabel-0').style.borderTop='none';
  document.getElementById('open-physician-0').style.display='none';

  // Copy the date 
  document.getElementById('open-creationDate-0').innerHTML='Unvalidated';
   
  // Copy the patient details 
  document.getElementById('open-notificationTable-0').innerHTML='';

  log("Setting ccr purpose blank");
  document.getElementById('open-ccrPurpose-0').value = '';
  el('open-ccrPurpose-0').style.display = 'none';

  // Show all thumbnails & drop shadows
  var ccrIndex = 0;
  var seriesIndex = 0;
  var thumbBox;
  var thumbBoxIndex = 0;
  var someHidden = false;

  // Move the thumbnail container
  var thumbnailContainerDiv = document.getElementById('open-thumbnailContainer-0');
  thumbnailContainerDiv.style.left=40;
  thumbnailContainerDiv.style.top=60;

  var bgcolor=unvalidColor1;
  var rowSize = 4;
  for(ccrIndex=0; ccrIndex < pendingCcrs.length; ++ccrIndex,++thumbBoxIndex) {
    pendingCcrs[ccrIndex].open=true;
    for(seriesIndex=0; seriesIndex<pendingCcrs[ccrIndex].series.length; ++seriesIndex,++thumbBoxIndex) {      
      var thumbBox = document.getElementById('open-thumbnailBox0-'+thumbBoxIndex);
      if(!thumbBox) { // Run out of space to display!
        someHidden = true;
        log('Ran out of thumb boxes at open-thumbnailBox0-'+thumbBoxIndex);
        break;
      }

      /*
       Experimental - add divider line between series
      if(seriesIndex<rowSize) {
        thumbBox.style.marginTop='10';
        thumbBox.style.borderTop='solid';
        thumbBox.style.borderWidth='1px';
        thumbBox.style.borderColor='black';
      }*/

      thumbBox.series = pendingCcrs[ccrIndex].series[seriesIndex];
      thumbBox.folder = pendingFolder;
      thumbBox.prefix = 'open';

      thumbBox.style.backgroundColor=bgcolor;
      thumbBox.style.display='block';
      var thumbImg=document.getElementById('open-thumbnailImage0-'+thumbBoxIndex);
      log("initializing thumbnail " + thumbBoxIndex + " for pending ccr " + ccrIndex + " series " + seriesIndex);
      initializeThumbImage(pendingCcrs[ccrIndex].series[seriesIndex],thumbImg);
    }    
    
    // Alternate colors for each series
    if(bgcolor==unvalidColor1) 
      bgcolor=unvalidColor2;
    else
      bgcolor=unvalidColor1;
  }
  
  // Hide any remaining thumb boxes
  while(thumbBox = document.getElementById('open-thumbnailBox0-'+(thumbBoxIndex++))) {
    thumbBox.style.display = 'none';
    thumbBox.ccr = null;
    thumbBox.folder = null;
    thumbBoxIndex++;
  }

  // If there are series that did not fit on the page, show the "more" button
  var state='none';
  if(someHidden) {
    state='block';
  }

  document.getElementById('pending-foldertabbutton-0').src = 'images/minusbutton.png';
  document.getElementById('pending-creationDate-0').style.color=highlightTextColors[currentHlColor];
  document.getElementById('open-moreButton-0').style.display=state;
  // ccr.folderDiv.style.backgroundImage="url('" + highlightImages[currentHlImage] + "')";

  document.ccrForm.ccrIndex.value = -1;
  document.getElementById('open-bodySectionCell-0').style.display='none';
  document.getElementById('open-ccrRecord-0').style.display='block';
  pendingFolderDiv.style.backgroundImage="url('images/stripefolderhl.gif')";
  document.getElementById('pending-creationDate-0').style.color=highlightTextColors[currentHlColor];
  document.getElementById('pending-creationDate-0').style.fontSize='12px';

  pendingFolder.open();

  log("Opened pending folder");
}

function isPendingFolderOpen() {
  return (pendingCcrs.length > 0) && (pendingCcrs[0].open);
}

function closePendingFolder() {
  log("closing pending folder");
  if(isPendingFolderOpen()) {
    folders[0].close();

    for(ccrIndex=0; ccrIndex < pendingCcrs.length; ++ccrIndex) {
      pendingCcrs[ccrIndex].open=false;
    }
    document.getElementById('open-ccrRecord-0').style.display='none';
    pendingFolderDiv.style.backgroundImage="url('images/stripefolder.gif')";
    document.getElementById('pending-creationDate-0').style.color='#616161';
    document.getElementById('pending-creationDate-0').style.fontSize='11px';
    document.getElementById('pending-foldertabbutton-0').src = 'images/plusbutton.png';
    //pendingFolderDiv.style.fontSize='10px'; 
  }
  else {
    log("pending folder not open");
  }
}

/**
 * Loads the content of a closed folder into the desktop open folder
 */
function loadContent(elementId,folderIndex) {
  // log('loading element ' + elementId + ' to main folder from source element ' + (elementId+'-'+folderIndex) );
  document.getElementById('open-'+elementId+'-0').innerHTML=
    document.getElementById(elementId+'-'+folderIndex).innerHTML;
}

/**
 * Sets the style such that this folder appears open
 * on the patient desktop. Note this is different to 'opening'
 * a folder which just copies details from the folder to the
 * primary display folder.
 */
function maximiseFolder(folderIndex) {
  var ccr = ccrs[folderIndex];
  ccr.folderDiv.style.backgroundImage="url('images/folder.gif')";
  ccr.folderDiv.style.left='0px';
  ccr.folderDiv.style.width='435px';
  document.getElementById('ccrRecord-'+folderIndex).style.top='0px';
  document.getElementById('ccrRecord-'+folderIndex).style.left='0px';

  // Show the folder contents
  showHideFolderContents(folderIndex, 'block');

  // Move the thumbnails
  var thumbnailContainerDiv = document.getElementById('thumbnailContainer-'+folderIndex);
  thumbnailContainerDiv.style.left='280px';
  thumbnailContainerDiv.style.top='360px';
  thumbnailContainerDiv.style.width='150px';

  // Show all thumbnails & drop shadows
  for(var i=0; i<ccrs[folderIndex].folder.thumbBoxes.length; i++) {
    ccrs[folderIndex].folder.thumbBoxes[i].style.display='block';
    ccrs[folderIndex].folder.thumbBoxes[i].style.backgroundImage="url('images/thumbnaildropshadow.gif')";
  }
  ccr.folderTabDiv.style.left='302px';
  ccr.folderTabDiv.style.fontSize='12px';
  ccr.folderTabDiv.style.top='7px';
  document.getElementById('foldertabbutton-'+folderIndex).src = 'images/minusbutton.png';
  document.getElementById('ccrRecord-'+folderIndex).style.zIndex='1000';

  // If there are series that did not fit on the page, show the "more" button
  var state='none';
  if(ccr.series.length > ccr.folder.thumbBoxes.length) {
    state='block';
  }
  document.getElementById('moreButton-'+folderIndex).style.display=state;
}

/**
 * Either shows or hides the contents of the requested folder
 * based on the state passed in.  The state should be one of:
 * - none
 * - block
 */
function showHideFolderContents(folderIndex, state,prefix) {
 /* if(document.getElementById('patientCard-'+folderIndex))
    document.getElementById('patientCard-'+folderIndex).style.display=state;

  if(document.getElementById('sourceCard-'+folderIndex))
    document.getElementById('sourceCard-'+folderIndex).style.display=state;

  if(document.getElementById('toCard-'+folderIndex))
    document.getElementById('toCard-'+folderIndex).style.display=state;

  if(document.getElementById('ccrPurpose-'+folderIndex))
    document.getElementById('ccrPurpose-'+folderIndex).style.display=state;
*/

  var namePrefix = '';
  if(prefix) {
    namePrefix = prefix + "-";
  }

  if(document.getElementById(namePrefix + 'bodySectionContainer-'+folderIndex))
    document.getElementById(namePrefix  +'bodySectionContainer-'+folderIndex).style.display=state;

  if(document.getElementById(namePrefix  +'ccrInfo-'+folderIndex))
    document.getElementById(namePrefix +'ccrInfo-'+folderIndex).style.display=state;

}

/**
 * Iterates over all folders and ensures they are visible.
 */
function showAllFolders()
{
  for(i=0; i<numFolders; ++i) {
    log("showing folder " + i);
    document.getElementById('ccrRecord-'+i).style.display='block';
  }
}

var imageWaitCount = 0;
var allFoldersShown = false;
function imageLoaded() {
  if(initialized) {
    imageWaitCount--;
    //log("Image wait count reduced to " + imageWaitCount);
    if(imageWaitCount <= 0) {
      if(!allFoldersShown) {
        allFoldersShown=true;
        showAllFolders();
      }
    }
  }
}

/**
 * Initializes all the folders and thumbnails
 */
var initialized = false;
var hasFolders = true;
var pendingFolderDiv;

var normalTools = [ 
        ["Reply CCR",'parent.showTabById("tab4");'],
        // ["Add Document",'showAddDocument();'],
        ["Print","print();"]
      ];

var toolsWithCreateCcr = normalTools.concat( [  [ "Validate CCR", "createCCR();" ] ]);

function initialize() {
  log("initializing");

  window.parent.setTools(normalTools);

  if((ccrs.length==0) && (pendingCcrs.length == 0)) {
    hasFolders=false;
  }

  var openFolderTable = document.getElementById("open-ccrCover-0");
  if(openFolderTable != null) {
    var tableHeight = openFolderTable.offsetHeight;
    log("table height is " + tableHeight);
    document.getElementById("open-dividerLine-0").style.height = tableHeight;
  }

  imageWaitCount=0;

  pendingFolderDiv = document.getElementById('pending-folder-0');
  log("pendingFolderDiv = " + pendingFolderDiv);

  if(pendingCcrs.length > 0) {
    var thumbBoxIndex=0;
    var folder = new Folder(pendingCcrs,"pending");    
    folder.folderTabDiv = document.getElementById("pending-folderTab-0");
    folder.folderDiv = document.getElementById('pending-folder-0');    
    folder.thumbnailContainerDiv = document.getElementById("pending-thumbnailContainer-0");
    folder.bodySectionContainerDiv = document.getElementById("pending-bodySectionContainer-0");
    folder.ccrInfoDiv = document.getElementById("pending-ccrInfo-0");
    folder.moreThumbnailBoxDiv = document.getElementById("pending-moreThumbnailBox0");
    for(ccrIndex=0; ccrIndex < pendingCcrs.length; ++ccrIndex,++thumbBoxIndex) {
      pendingCcrs[ccrIndex].open=true;
      pendingCcrs[ccrIndex].folder = folder;
      for(seriesIndex=0; seriesIndex<pendingCcrs[ccrIndex].series.length; ++seriesIndex,++thumbBoxIndex) {
        var thumbBox = document.getElementById('pending-thumbnailBox0-'+thumbBoxIndex);
        if(!thumbBox) { // Run out of space to display!
          break;
        }

        thumbBox.style.display='block';
        var thumbImg=document.getElementById('pending-thumbnailImage0-'+thumbBoxIndex);
        log("initializing thumbnail for pending ccr " + ccrIndex + " series " + seriesIndex);
        initializeThumbImage(pendingCcrs[ccrIndex].series[seriesIndex],thumbImg);
        thumbBox.series = pendingCcrs[ccrIndex].series[seriesIndex];
        folder.thumbBoxes.push(thumbBox);
      }    
    }
    folder.displayIndex = 0;
    folder.index = folders.push(folder)-1;    
  }

  for(var ccrIndex=0; ccrIndex<ccrs.length; ccrIndex++) {
    var ccr = ccrs[ccrIndex];
    var folder = new Folder(ccr);
    ccr.folder = folder;
    folder.folderTabDiv = document.getElementById("folderTab-"+ccrIndex);
    folder.folderDiv = document.getElementById('folder-'+ccrIndex);    
    folder.thumbnailContainerDiv = document.getElementById("thumbnailContainer-"+ccrIndex);
    folder.bodySectionContainerDiv = document.getElementById("bodySectionContainer-"+ccrIndex);
    folder.ccrInfoDiv = document.getElementById("ccrInfo-"+ccrIndex);
    folder.moreThumbnailBoxDiv = document.getElementById("moreThumbnailBox"+ccrIndex);
    folder.thumbBoxes=new Array();
    // We start from 1 because the folder CCR is first and should not be displayed as thumbnail
    for(var seriesIndex=1; seriesIndex<ccrs[ccrIndex].series.length; seriesIndex++) {
      //log('initializing image ' + ccrIndex + '-' + seriesIndex);
      var series = ccr.series[seriesIndex];
      //log("Series guid = " + series.guid);
      var thumbIndex = seriesIndex-1; 
      var thumbImg=document.getElementById('thumbnailImage'+ccrIndex+'-'+thumbIndex);
      if(thumbImg != null) {
        log("initializing thumbbox " + ccrIndex + '-' + thumbIndex + " for ccr " + ccr + " with thumbboxes " + ccr.folder.thumbBoxes);
        folder.thumbBoxes[thumbIndex]=document.getElementById('thumbnailBox'+ccrIndex+'-'+thumbIndex);
        folder.thumbBoxes[thumbIndex].thumbImg = thumbImg;
        folder.thumbBoxes[thumbIndex].series = series;
        imageWaitCount++;
        initializeThumbImage(series,thumbImg);
      }      
    }
    folder.displayIndex = ccrIndex;    
    folder.index = folders.push(folder)-1;    
  }

  if(hasFolders) {
    document.getElementById('open-ccrRecord-0').style.zIndex=1000;
    log("Closing all folders");
    closeAllFolders();
    if(pendingFolderDiv) {
      openPendingFolder();
    }
    else {
      openFolder(initialOpenFolderIndex);
    }
  }
  initialized = true;

  //updateFolderState();
  //document.getElementById('allFolders').style.display='block';
}

function initializeThumbImage(series, thumbImg) {
  if(series.mimeType=='application/dicom') {
    var imgUrl='/router/wadoImage.do?&imageQuality=90&windowWidth=400&windowCenter=40';
    imgUrl+='&fname='+series.firstInstanceFile+'&mcGUID='+series.guid;
    imgUrl+='&maxRows='+thumbImageSize;
    imgUrl+='&maxColumns='+thumbImageSize;
    thumbImg.src = imgUrl;
    thumbImg.alt=series.description + ' - ' + series.numImages + ' images';
  }
  else {
    thumbImg.src=mimeTypeImages[series.mimeType];
    if(series.mimeType==CCR_MIME_TYPE) {
      thumbImg.alt=series.description + " - " + series.ccr.creationTime + " " + series.ccr.creationDate;
    }
    else {
      thumbImg.alt=series.firstInstanceFile + ' (' + series.description + ')';
    }
  }
}

function handleCheckBox(type, ccrIndex) {
 document.location.href='https://secure.medcommons.net/HIPAA_Restriction_Request.php?mcid=8336367483102661';
}

function showBodySection(ccrIndex, sectionName) {
  showWado(ccrIndex, 0);
}

function handleKey(event) {
  if (!event) event= window.event; // IE

  var keyCode =
    document.layers ? event.which :
    document.all ? event.keyCode :
    document.getElementById ? event.keyCode : 0;

  if ( (keyCode == 106) || (keyCode == 74) ) {
    currentHlColor++;
    if(currentHlColor >= highlightTextColors.length) {
      currentHlColor=0;
    }
    updateFolderState();
  }
  if((keyCode == 107) || (keyCode == 75)) {
    currentHlImage++;
    if(currentHlImage >= highlightImages.length) {
      currentHlImage=0;
    }
    updateFolderState();
  }

  if((keyCode == 87) || (keyCode == 119)) {
    if(document.getElementById('open-ccrCover-0').style.backgroundColor=='#ffffff') {
      document.getElementById('open-ccrCover-0').style.backgroundColor='transparent';
    }
    else
      document.getElementById('open-ccrCover-0').style.backgroundColor='#ffffff';
  }

  var adj = document.getElementById('contactslist');
  if(!adj) {
    return;
  }

  var bgColor=adj.style.backgroundColor;
  if(bgColor==null || bgColor=='') {
    bgColor='#6988bb';
  }

  var r = hex2Int(bgColor.charAt(1))*16 + hex2Int(bgColor.charAt(2));
  var g = hex2Int(bgColor.charAt(3))*16 + hex2Int(bgColor.charAt(4));
  var b = hex2Int(bgColor.charAt(5))*16 + hex2Int(bgColor.charAt(6));
  log(bgColor + ' r='+r + ' g=' + g + ' b=' + b + ' key='+keyCode);
  if(keyCode == 82) {
    r+=2;
    if(r>255)
      r=0;
    var newColor='#' + int2Hex(r)+int2Hex(g)+int2Hex(b);
    log('new color is ' + newColor);
    adj.style.backgroundColor=newColor;
  }
  else
  if(keyCode == 71) {
    g+=2;
    if(g>255)
      g=0;
    var newColor='#' + int2Hex(r)+int2Hex(g)+int2Hex(b);
    log('new color is ' + newColor);
    adj.style.backgroundColor=newColor;
  }
  else
  if(keyCode == 66) {
    b+=2;
    if(b>255)
      b=0;
    var newColor='#' + int2Hex(r)+int2Hex(g)+int2Hex(b);
    log('new color is ' + newColor);
    adj.style.backgroundColor=newColor;
  }
}


function toggleContacts() {

  alert('Contacts list is unavailable in current Demo');
  return;
  var contactsListDiv = document.getElementById('contactslist');
  var folderAreaDiv = document.getElementById('folderArea');
  if(contactsListDiv.style.display=='block') {
    contactsListDiv.style.display='none';
    folderAreaDiv.style.position='absolute';
    folderAreaDiv.style.left='27px';
  }
  else {
    contactsListDiv.style.display='block';
    folderAreaDiv.style.position='absolute';
    var newLeft = contactsListDiv.offsetWidth+28;
    folderAreaDiv.style.left=newLeft;
  }
}

function getOpenCcr() {
  for(var ccrIndex=0; ccrIndex<ccrs.length; ccrIndex++) {
    var ccr = ccrs[ccrIndex];
    if(ccr.open) {
      return ccr;
    }
  }
  return null;
}

/**
 * Invokes the viewer for the currently highlighted pending/unvalidated CCR
 */
function createCCR() {
  // Find which CCR is open in the pending folder
  for(ccrIndex=0; ccrIndex < pendingCcrs.length; ++ccrIndex) {
    var ccr = pendingCcrs[ccrIndex];
    if(ccr.selected) {
      showCcrSeries(ccr, ccr.index, 0,true);
      return;
    }
  }
}

/**
 * Shows a frame to allow user to add a document to the order/series for the current CCR
 */
function showAddDocument() {
  var ccr = getOpenCcr();
  if(ccr == null) {
    alert('Please open a CCR to add the document to and try again');
    return;
  }

  window.open('about:blank','addDocument','scrollbars=1,width=720,height=550,resizable=1');
  document.ccrForm.action = 'updateCcr.do?forward=showAddDocument';
  document.ccrForm.target = 'addDocument';
  document.ccrForm.submit();
}

function refreshPage() {
  window.location.href='showPatientFolders.do';
}

function onSwitchTab(url) {
  document.ccrForm.action = url;
  document.ccrForm.submit();
}

    /*
      removed from openPendingFolder() ... 

    // Place new CCR on next row  
    var backTrack=false;
    for(; thumbBoxIndex % rowSize > 0; thumbBoxIndex++)  {
      var thumbBox = document.getElementById('open-thumbnailBox0-'+thumbBoxIndex);
      if(!thumbBox) { // Run out of space to display!
        break;
      }
      thumbBox.ccr = pendingCcrs[ccrIndex];
      thumbBox.folder = pendingFolder;

      thumbBox.style.display='block';
      thumbBox.style.backgroundImage='none';
      var thumbImg=document.getElementById('open-thumbnailImage0-'+thumbBoxIndex);
      thumbImg.src='images/transparentblank.gif';
      thumbImg.title='';
      backTrack=true;
      log("Thumb " + thumbBoxIndex + " is blank");
    }

    if(backTrack)
      thumbBoxIndex--;
    */
      

