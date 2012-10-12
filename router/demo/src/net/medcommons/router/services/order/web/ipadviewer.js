/* Copyright 2010 MedCommons Inc.   All Rights Reserved. */

/**
 * Functions supporting the viewer inside the ipad
 */


// Hack - the iPad uses a scrollable touch based field, so it doesn't need to page the
// thumbnails.  Rather than program around this, we just make the page be infinitely big.
thumbnailPageSize = 100000;

/**
 * We keep track of some aspects of the state of external iPad components
 * so we can optimize and not send unnecessary calls.
 */
var ipad_state = {
  thumbs: { zoomed : false }  
};

// Prevent the default set of tools from showing
function showTools() {
    log("Tool palette is overridden by custom iPad implementation");
}

disconnectAll(toolPalette, 'hideTools');
var yuiLogger;
connect(toolPalette,'hideTools', function() {
    // enable_ipad_logging();
});

function enable_ipad_logging() {
    prependChildNodes('ViewerArea', DIV({id:'yuiLogger',style:'position: absolute; z-index: 100000;'}));
    yuiLogger = new YAHOO.widget.LogReader('yuiLogger');
}


function call_ipad(service, args) {
    
    var url = compute_ipad_url(service, args);
    
    // Example:
    //    mc://scrubber?op=add&lowlim=1&highlim=10&action=js_sliderevent&param=123456
    // YAHOO.util.Get.script(url);
    // loadScript(url);
    
    var iframe = document.createElement("iframe");
    iframe.setAttribute("style", "display: none");
    iframe.setAttribute("src", url);
    document.body.appendChild(iframe);    
}


function compute_ipad_url(service, args) {
    if(args.url) {
        args.url = accountsBaseURL + 'router/'+args.url;
        args.url += args.url.indexOf('?') >= 0 ?  '&' : '?';
        args.url += 'auth='+auth;
    }
    
    // Because the iPad tries to send us this value in single
    // quotes we have to escape them on the way in or it will 
    // fail 
    if(typeof args.jsprm == 'string') 
        args.jsprm = encode_ipad_prm(args.jsprm);
    
    return "mc://viewer?comp="+service+"&"+queryString(args);
}

/**
 * Overridden for bpad
 */
function encode_ipad_prm(jsprm) {
    return jsprm.replace(/\'/g, "\\'");
}

CCRThumbnail.prototype.updateThumbnail = function(thumbIndex, thumb) {
    
    // Properties that we have available from thumb in args:
    // 
    //    tn: tn, 
    //      ccrCreateDate: ccrCreateDate, 
    //      fromActor: fromActor,
    //      dateOfBirth: formattedDob,
    //      patientName: patientName,
    //      ageSex : ageSex
    //
    // In the iPad we have:
    //
    //      url, title, subtitle, jsevt, jsprm
    //
    call_ipad('thumb', {
        op: 'mod',
        title: thumb.patientName,
        subtitle: formatLocalDateTime(thumb.ccrCreateDate),
        id: thumbIndex
    });
};

/**
 * When a series is selected we highlight it.
 */
connect(events, 'newSeriesSelected', function(series) {
    var i;
    for(i=0; i<thumbnails.length; ++i) {
        if(thumbnails[i].series == series)
            break;
    }
    if(i>=thumbnails.length)
        return;
    
    call_ipad('thumb', {
        op: 'mod',
        id: i,
        sel: 'true'
    });
    
});

connect(events, 'afterCurrentThumbChange', function(thumb) {
    if(series.mimeType != 'application/dicom') 
        return;
    
    for(var i=0; i<toolPalette.length; ++i) { 
        if(toolPalette[i].name == 'wl') {
            onToolClick(i);
            break;
        }
    }
});


SeriesThumbnail.prototype.updateThumbnail = function(thumbIndex, thumb) {
        
        // The iPad doesn't like HTML tags - so we make a simplified
        // version of the labels
        var nImages = numberOfImageInSeries(this.seriesNumber);
        if(this.seriesNumber==currentSeriesIndex) {
            thumb.label = "Image " + (currentImage + 1) + " / " + nImages;
            if(this.series.instances[currentImage].caching)
                thumb.label += "\nBuffering ...";
            else
            if(animation && animation.timer) { // running
                thumb.label += "\n" + animation.fps + " fps"
                      + "\n" +this.series.instances[currentImage].numFrames + " frames" ;
            }
            else
            if(animation && !animation.timer) { // paused
                thumb.label += "\nFrame " + (animation.currentFrame+1) + " / "
                      + this.series.instances[currentImage].numFrames;
            }        
        }
        
        call_ipad('thumb', {
            op: 'mod',
            title: thumb.label,
            subtitle: thumb.desc,
            url: thumb.image,
            id: thumbIndex
        });
};

update(DocumentThumbnail.prototype, {
    updateThumbnail: function(thumbIndex, thumb) {
        call_ipad('thumb', {
            op: 'mod',
            title: thumb.label,
            subtitle: thumb.desc,
            url: thumb.image,
            id: thumbIndex
        });
    }
});

update(BlankThumbnail.prototype, {
    updateThumbnail: function(thumbIndex, thumb) {
    }
});

function onScrubChange(pos) {

    timedMessage('ViewerArea', 'scrub:  ' +  pos);
    
    var thumbnail = thumbnails[currentThumb];
    if(thumbnail.series && (thumbnail.series.mimeType == 'application/dicom')) {
      currentImage=pos;
      displayCurrentImage();
    }
}

var toolImages = {};

function onToolClick(index) {
    var tool = toolPalette[index];
    
    // Hack: the default tools code expects to be able to modify styles on the image
    // for the tool.  Rather than refactor that, we make a dummy image for it to 
    // do its changes to (causing them to have no effect).
    var img = toolImages[tool.name];
    if(!toolImages[tool.name]) {
        img = toolImages[tool.name]= IMG();
        img.index = index;
    }
    
    img.events = [];
    signal(toolPalette, tool.name, img);
    timedMessage('ViewerArea', 'Tool Selected: ' + tool.name);
}

connect(toolPalette, 'exclusiveToolSelect', function(img) {
    forEach(['wl', 'pan', 'magnify'], function(t) {
        if(img == toolImages[t]) {
            call_ipad('tool', {
               op: 'mod', 
               id: img.index,
               img: t+'_active'
            });
            img.ipadActive = true;
        }
        else
        if(toolImages[t] && toolImages[t].ipadActive) {
            call_ipad('tool', {
               op: 'mod', 
               id: toolImages[t].index,
               img: t
            });
            toolImages[t].ipadActive = false;
        }
        else {
            log("tool " + t + " : " + toolImages[t]);
        }
    });
});


function onSwitchEpisodes(id) {
    var e = filter(function(e) { return e.profileId == id; }, episodes)[0];
    
    if(!e) {
        alert("Unknown episode " + id);
        return;
    }
    
    var guid = null;
    if(e.guid) {
        guid = e.guid;
    }
    else {
        var doc = accountDocuments[e.documentType];
        guid = doc.guid;
    }
    
    window.location.href=window.location.href.replace(new RegExp(ccr.patientId+".*$"),ccr.patientId+'/'+guid);
}


//=============== General Init ===============

var episodes;

connect(events, 'startInitialize', function() {
    
    // enable_ipad_logging();
    
    call_ipad('thumbs', { op: 'mod', vis: 'show'});
    call_ipad('scrub', { op: 'mod', vis: 'show' , curval: 0, minval: 0, maxval: 100, jsevt: 'onScrubChange', jsprm: '' });
    call_ipad('button',  { op: 'mod', vis: 'hide' });
    
    var ccrCreateDate = toAmericanDate(new Date(ccr.createDateTime));
    
    call_ipad('head',  { 
        op: 'mod', id: 'title', title: p.PatientName
    });
    
    call_ipad('head',  { 
        op: 'mod', id: 'subtitle', title: ccrCreateDate 
    });
    
    call_ipad('head',  { 
        op: 'mod', id: 'documents', title: 'Documents', jsevt: 'onDocumentsButton' 
    });
    
     setTimeout(function() {
        for(var i=0; i<toolPalette.length; ++i) {
            var img = toolPalette[i].img.replace("images/tool_", "")
                                        .replace(".png","");
            call_ipad('tool', {op: 'add',  id: i, img:  img, jsevt: 'onToolClick', jsprm: i});
        }
        call_ipad('tools',  { op: 'mod', vis: 'show' });
    }, 1000);
    
    // We add all the thumbnails here but with blank images
    // The real thumbnail images will get added via the 'update' 
    // that happens when displayThumbnail() is called on each
    // thumbnail by the core WADO.js code
    for(var i=0; i<thumbnails.length; ++i) {
        call_ipad('thumb', {
            op: 'add',
            label: '*',
            jsevt: 'onThumbnailClick',
            jsprm: i,
            url:   i == 0 ? 'images/caution.png' : 'images/blank.png',
            id : i
        });
        thumbnails[i].updateLabels(i);
    } 
    
    setTimeout(function() {
        execJSONRequest('QueryPatientCCRs.action',{ccrIndex:ccrIndex}, function(result) {
    
          if(!result || (result.status!="ok")) {
            alert("An error occurred while retrieving other CCRs for this patient:\r\n\r\n"+result.message);
            return;
          }
          
          episodes = addDocumentInfo(result.profiles);
          forEach(episodes, function(e) {
            call_ipad('episodes', {
               op: 'add', 
               id: e.label,
               title: e.label,
               jsevt: 'onSwitchEpisodes',
               jsprm: "'"+e.profileId+"'"
            });
          });
        });
    },1000);
            
});

connect(events, 'startInitialize', function() {
    setTimeout(function() {
        for(var i=0; i<thumbnails.length; ++i) {
            thumbnails[i].displayThumbnail(i);
        }
    }, 2000);
});

connect(events, 'activeThumbnailChanged', function(thumbnail) {
    if(thumbnail.series && (thumbnail.series.mimeType == 'application/dicom')) {
        call_ipad('scrub', { op: 'mod', curval: currentImage});
    }
});

connect(events, 'afterCurrentThumbChange', function(thumbnail) {
    log('setting scrubber visibility');
    // If the highlighted thumbnail is a DICOM image
    // set the scrubber to the appropriate range for the
    // number of images
    if(thumbnail.series && (thumbnail.series.mimeType == 'application/dicom')) {
        call_ipad('scrub', { op: 'mod', vis: 'show', curval: 0, minval: 0, maxval: thumbnail.series.instances.length-1, jsevt: 'onScrubChange'});
    }
    else {
        // hide scrubber
        call_ipad('scrub', { op: 'mod', vis: 'hide' });
    }
});

// =============== Documents Button ===============

function onDocumentsButton() {
    call_ipad('thumbs', { op: 'mod',  zoom: 'max' });
    call_ipad('scrub', { op: 'mod',  vis: 'hide' });
    call_ipad('main', { op: 'mod',  vis: 'hide' });
    call_ipad('tools', { op: 'mod',  vis: 'hide' });
    ipad_state.thumbs.zoomed = true;
}

// =============== Clicking on Thumbnails ===============

function onThumbnailClick(i) {
    if(ipad_state.thumbs.zoomed) {
        call_ipad('thumbs', { op: 'mod',  zoom: 'norm' });
        // call_ipad('scrub', { op: 'mod',  vis: 'show' });
        call_ipad('main', { op: 'mod',  vis: 'show' });
        call_ipad('tools', { op: 'mod',  vis: 'show' });
        ipad_state.thumbs.zoomed = true;
    }
    
    // HACK - only needed because CCR Thumbnail 
    // generates an error inside handleThumbClick()
    // for some reason - seemin
    
    if(i == 0)
        call_ipad('thumb', {
            op: 'mod',
            id: i,
            sel: 'true'
        });
    
    log("ipad click " + i);
    handleThumbClick(i);   
}

/**
 * Event handler - called by iPad in respone to events
 * 
 * @param action
 * @param param
 */
function doit(action, param) {
    eval(action + '(' + param + ')');
}