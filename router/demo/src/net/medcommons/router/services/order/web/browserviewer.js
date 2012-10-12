/* Copyright 2010 MedCommons Inc.   All Rights Reserved. */
/**
 * Functions supporting the viewer in a normal browser
 * 
 * The following features are added to the core viewer by this file:
 * 
 *     - support for thumbnail strip on left
 *     - support for floating toolbar docked on RHS
 */
CCRThumbnail.prototype.updateThumbnail = function(thumbIndex, thumb) {
    
  var thumbCellDiv = $('thumbCell'+thumbIndex);
  replaceChildNodes(thumbCellDiv, 
      DIV({'class':"CCRTitle"},
        SPAN({ style:"position: relative;  left: 5px; top: 2px; text-align: left; width:100%;"},
          'CCR ',
          SPAN({ style:"font-size: 11px; position: relative; top: -1px; "}, thumb.tn)
        ) 
      ),
      DIV({ 'class':"CCRThumbLabels"},
              IMG({src: 'images/ccr.png', style:'margin-top: 20px'})
      )
  );
                  
  // Set the watermark image
  if(document.images['thumbImage'+thumbIndex]!=null) {
    document.images['thumbImage'+thumbIndex].src='images/ccrthumb.gif';
    replaceChildNodes($("thumbDescription" + thumbIndex),'1 page');
    replaceChildNodes($("thumbLabel" +thumbIndex),'CCR');  
  }

  // Hide labels
  hide('thumbLabel'+thumbIndex);
  hide('thumbDescription'+thumbIndex);

  // Give correct style / background color
  var cellDiv =  $('thumbCell'+thumbIndex);
  cellDiv.style.backgroundColor='#345';
  cellDiv.style.height='140px';
  cellDiv.style.cursor='pointer';

  // Set the onclick handler - need to use some hackery to make the callback
  // use the correct thumbIndex.
  var clickHandler = function() { displaySelectedThumbnail(thumbIndex); };
  clickHandler.thumbIndex = thumbIndex;
  $('thumbCell'+thumbIndex).onclick=clickHandler;

  // Display annotation to show that validation required
  if(this.series.paymentRequired) {
    show("thumbAnnotation"+thumbIndex);
    $("thumbAnnotation"+thumbIndex).src='images/payment_required.png';
  }
  else
  if(this.series.validationRequired) {
    show("thumbAnnotation"+thumbIndex);
    $("thumbAnnotation"+thumbIndex).src='images/validation_required.png';
  }
};

/**
 * SeriesThumbnails
 * 
 * The WADO viewer 
 */
SeriesThumbnail.prototype.updateThumbnail = function(thumbIndex, thumb) {
    
  var nImages = numberOfImageInSeries(this.seriesNumber);
  var label;
  if(this.seriesNumber==currentSeriesIndex) {
      
     label = "Image <div>" + (currentImage + 1) + " / " + nImages+"</div>";
     if(this.series.instances[currentImage].caching)
         label += "<br/><span class='buffermsg'>Buffering ...</span>";
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
    
  var thumbCellDiv = $('thumbCell'+thumbIndex);
  
  if(thumb.image) {
      log("Displaying thumb as url " + thumb.image);
      if(!$('thumbImage'+thumbIndex)) {
          thumbCellDiv.innerHTML=
              '<IMG height=140 src="'+thumb.image+'" width=140 border=0 name="thumbImage'+thumbIndex+'" id="thumbImage'+thumbIndex+'">';
      }
      else  {
          $('thumbImage'+thumbIndex).src = thumb.image;
      }
  }
  
  if(thumb.desc)
      replaceChildNodes("thumbDescription" + thumbIndex, thumb.desc);
  
  if($("thumbLabel" + thumbIndex)) {
      $("thumbLabel" + thumbIndex).innerHTML = label;
  }
};


update(DocumentThumbnail.prototype, {
    updateThumbnail: function(thumbIndex, thumb) {
      var thumbURL = thumb.image;
      var thumbDescription = thumb.desc;
      var spacer = '';
      if(findValue(imageMimeTypes,this.series.mimeType)>=0) {
        spacer='<br/>';
      }
    
      var newHtml ='<div class="NotSelectedImage thumbCell" onclick="displaySelectedThumbnail('+thumbIndex +');" style="cursor: hand;">'
              + '<div class="CCRTitle"><span style="position: relative;  left: 5px; text-align: left; width=100%;">'
              + '<img border="0" style="position: relative; top: 3px;" src="images/record.gif"/>&nbsp;&nbsp;Document</span></div>'
              +'<div class="CCRThumbLabels" style="height: 110px; position: relative; top: 4px; "><div id="ccrThumbName" style="width: 140px;">' 
              + '&nbsp;' + trunc(thumbDescription, 25) + '</div>'
              + spacer 
              +'<div class="CCRThumbLabels" style="text-align: center;"><img src="'+thumbURL+'"/></div>';
      newHtml += '</div>';
      
      $('thumbCell'+thumbIndex).innerHTML=newHtml;
      
      replaceChildNodes($("thumbDescription" + thumbIndex),'');
      replaceChildNodes($("thumbLabel" +thumbIndex),'');  
      // Display annotation to show that validation required
      if(this.series.paymentRequired) {
        $("thumbAnnotation"+thumbIndex).style.display='block';
        $("thumbAnnotation"+thumbIndex).src='images/payment_required.png';
      }
      /*
      else
      if(this.series.validationRequired) {
        $("thumbAnnotation"+thumbIndex).style.display='block';
        $("thumbAnnotation"+thumbIndex).src='images/validation_required.png';
      }
      */
      else {
        $("thumbAnnotation"+thumbIndex).style.display='none';
      } 
    }
});


update(BlankThumbnail.prototype, {
    updateThumbnail: function(thumbIndex, thumb) {
      var thumbImageName='thumbImage'+thumbIndex;
      var thumbCellDiv = $('thumbCell'+thumbIndex);    
      if(thumbCellDiv != null) {
        thumbCellDiv.innerHTML='<IMG height=140 src="blank.png" width=140 border=0 name="thumbImage'+thumbIndex+'" id="'+thumbImageName+'"></A>';
        replaceChildNodes($("thumbDescription" + thumbIndex),'');
        replaceChildNodes($("thumbLabel" +thumbIndex),'');  
      }
    }
});

/**
 * Create the actual thumbnail DIVs
 */
connect(events, "startInitialize", function() {
    prependChildNodes('ViewerArea',DIV({id:'thumbOuter'}));
    for(var i=3; i>=0; --i) {
        prependChildNodes('thumbOuter', makeThumbCell(i, function(cell, ann) {
            connect(cell,'onclick', function() {displaySelectedThumbnail(i); });
            connect(ann, 'onclick', function() { validateSelectedSeries(i); });
        }));
        if(!thumbnails[i])
            thumbnails[i] = new BlankThumbnail();
    }
    
    if(mobile) {
        var btnOffset = 50;
        $('thumbOuter').style.top = btnOffset+'px';
        $('pager').style.top = (getElementPosition('pager').y+btnOffset)+'px';
        var inbox,menus;
        prependChildNodes('ViewerArea', DIV({id:'mobilebtns'}, inbox=BUTTON({'class':'back'},'Inbox'), menus=BUTTON('Menus')));
        connect(inbox,'onclick',function() {
           window.top.location.href=accountsBaseURL+'/acct/'; 
        });
        connect(menus,'onclick',function() {
           window.top.location.href='/router/ccrs/'+ccr.patientId+'?fmt=full';
        });        
    }
});

connect(events, 'initialized', function() {
  if(hideSeries()) {
    hide( 'thumb1','thumb2','thumb3','mclogo','tools','pager','footer');
    $('thumb0').style.left='0px';
    $('thumb0').style.backgroundColor='black';
    $('mainImage').style.left='200px';
  }

  if(hideDicomButtons) {
    hide('mclogo', 'tools','footer');
    $('thumb0').style.left='0px';
    $('thumb1').style.left='0px';
    $('thumb2').style.left='0px';
    $('thumb3').style.left='0px';
    $('mainImage').style.left='200px';
  }

  initializeThumbnails();
  updatePagerLinks();   
});

function initializeThumbnails() {
  for(var i=0; i<4; ++i) {
      if($('thumbCell'+i)) {
        disconnectAll('thumbCell'+i);
        connect($('thumbCell'+i), 'onclick', partial(handleThumbClick,i));
      }
  }

  // Always show thumbnail 0
  thumbnails[0].displayThumbnail(0);

  // Remaining thumbnails determined by current page
  for(i=1;i<4;i++) {
    var thumbnailNumber=currentThumbnailPage*thumbnailPageSize+i;
    thumbnails[thumbnailNumber].displayThumbnail(i);
  }

  if($('pagernumbers') != null) {
    $('pagernumbers').innerHTML=(currentThumbnailPage +1) + " of " + numberOfPages();
  }
  
  // The last page may have some unused thumbnail boxes - so we have to 
  // fill them out with Blank Thumbnails. 
  var pageCount = Math.round((thumbnails.length -1) / 3);
  for(var i=thumbnails.length; i<pageCount*3; ++i) {
      if(typeof thumbnails[i] == 'undefined')
          thumbnails[i] = new BlankThumbnail();
  }
  log("thumbs after init: " + thumbnails.length);
}

connect(events, 'newSeriesSelected', initializeThumbnails);

// ================ Scrubber Support =====================

// connect(events, 'afterCurrentThumbChange', addRemoveScrubber);

addLoadEvent(function() {
    // Bind events to all the dicom thumbnails to add tools to the viewer
    forEach(filter(function(t){return (t && t.series && (t.series.mimeType == "application/dicom"));}, thumbnails), function(t) { 
        connect(t, "displayed", partial(addRemoveScrubber,t));
    });
});


connect(events, 'afterCurrentThumbChange', updatePagerLinks);

var scrubber;
var scrubberXRange;

function addRemoveScrubber(thumbnail) {
  log("addRemoveScrubber");
  if(thumbnail.series.mimeType == 'application/dicom') {
    if(!$('scrubber')) {
      appendChildNodes('ViewerArea', 
          DIV({id: 'scrubber', 'class':'yui-skin-mc'},
            DIV({id:'sliderbg'}, DIV({id:'sliderthumb'}, IMG({src:'yui-2.8.2r1/slider/assets/thumb-n.gif'})))
          )
      );
      $('scrubber').style.width = imageWidth+'px';
      scrubberXRange = imageWidth-10;
      scrubber = YAHOO.widget.Slider.getHorizSlider("sliderbg", "sliderthumb", 0, scrubberXRange);
      scrubber.subscribe('change', onScrubberMove);
      scrubber.subscribe('slideEnd', onScrubberEnd);
    }
    else {
        scrubber.setValue(0, false, false, false);
        show('scrubber');
    }
  }
  else { // Remove scrubber
    hide('scrubber');
  }
}

var messageTimer;
var scrubberPreview;
var scrubberImg;
var previewWidth=40;

function onScrubberMove(x) {
    log('scrubber: ' + x);
    
    var len = thumbnails[currentThumb].series.instances.length;
    var pos = Math.round((x / scrubberXRange) * len);
    if(pos >= len)
        pos = len - 1;
        
    if(!$('messageDiv')) 
        appendChildNodes('ViewerArea', DIV({ id: 'messageDiv', style: 'position: absolute; top: 0px;  left: 200px;  color: white; background-color: blue; z-index: 1000000'}));
        
    if(messageTimer)
        clearTimeout(messageTimer);
    
    messageTimer = setTimeout(function() { removeElement('messageDiv'); }, 3000);
    
    replaceChildNodes('messageDiv', SPAN('Image ' +  (pos+1)));
    
    var thumbnail = thumbnails[currentThumb];
    if(thumbnail.series && (thumbnail.series.mimeType == 'application/dicom')) {
      // currentImage=pos;
      // displayCurrentImage();
    }
    
    if(!$('scrubberPreview')) {
        var img;
        appendChildNodes('scrubber',
                DIV({id:'scrubberPreviewNum'}, ''),
                DIV({id:'scrubberPreviewBox', 'class': 'loading'},
                    img=IMG({id:'scrubberPreview', src:'images/ajax-loader.gif'})));
        scrubberImg = IMG({});
        scrubberImg.onload = scrubberImg.onabort = scrubberImg.onerror = function() {
             img.src=scrubberImg.src;   
             img.style.height = (previewWidth * thumbnail.series.instances.length)+'px';
             img.style.width = previewWidth +'px';
             if($('scrubberPreviewBox'))
                 removeElementClass('scrubberPreviewBox', 'loading');
        };
        scrubberImg.src=getImageUrl(currentSeriesIndex, 0, {maxRows:previewWidth, maxColumns:previewWidth,thumbStrip:true});
    }
    show('scrubberPreviewBox');
    show('scrubberPreviewNum');
    $('scrubberPreviewBox').style.left=(x-15)+'px';
    $('scrubberPreview').style.top=-(pos*previewWidth)+'px';
    
    $('scrubberPreviewNum').innerHTML = pos > 0 ? ""+(pos+1) : '';
    $('scrubberPreviewNum').style.left = x+'px';
}

function onScrubberEnd() {
    var x = scrubber.getValue();
    var len = thumbnails[currentThumb].series.instances.length;
    var pos = Math.round((x / scrubberXRange) * len);
    if(pos >= len)
        pos = len - 1;
 
    var thumbnail = thumbnails[currentThumb];
    if(thumbnail.series && (thumbnail.series.mimeType == 'application/dicom')) {
       currentImage=pos;
       displayCurrentImage();
    }
    setTimeout(function() {
        hide('scrubberPreviewBox', 'scrubberPreviewNum');
    },2000);
 }

connect(events, 'onCurrentImageChange', function() {
   if(!scrubberXRange)
       return;
   
   var pos = scrubberXRange * (currentImage / thumbnails[currentThumb].series.instances.length);
   scrubber.setValue(pos,true,true,true /* do not send events */);
    if($('scrubberPreviewBox'))
       removeElement('scrubberPreviewBox');
});

connect(events, 'onCurrentThumbChange', function() {
    if($('scrubber'))
        scrubber.setValue(0,true,true,true);
    if($('scrubberPreviewBox'))
       removeElement('scrubberPreviewBox');
});

connect(events, 'afterCurrentThumbChange', function() {
    if($('scrubberPreviewBox'))
       removeElement('scrubberPreviewBox');
});
// ================ End Scrubber Support =====================



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

  for( ; thumbnailIndex < p.studies[0].series.length+2; ++thumbnailIndex) {
    var seriesLink = $('pagerLink'+thumbnailIndex);
    if(seriesLink != null) {
      seriesLink.style.display='none';
    }
  }
}

/**
 * Updates the thumbnail images to ensure the correct one is highlighted
 * and the labels for each thumbnail are correct.
 */
function updateActiveThumbnail() {
  // log("updateActiveImage: " + stacktrace());
  var nSeries = numberOfSeries();
  for(thumbIndex=0; thumbIndex<4; thumbIndex++) {     
    var thumbArrayIndex = 
      thumbIndex == 0 ? 0 : currentThumbnailPage*thumbnailPageSize + thumbIndex;

    thumbCellName = "thumbCell" + thumbIndex;  
    
    if(!thumbnails[thumbArrayIndex])
        thumbnails[thumbArrayIndex] = new BlankThumbnail();
    
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
  
  // If the series needs validation, display that option in the menu
  var series = thumbnails[currentThumb].series;
  if(series.validationRequired)
    log("validation required for series " + currentSeriesIndex);
}

connect(events, 'activeThumbnailChanged', updateActiveThumbnail);


