/**
 * Support for tools in the viewer, compatible with mobile browsers.
 * <p>
 * This operates as an extension to WADO.js.  First include WADO.js, then this
 * file which then disconnects some events and connects others to enable
 * the mobile / touch actions.
 * 
 * Copyright MedCommons 2010
 */


/**
 * Events to map when displayed on a device with a mouse
 */
var browserTouchEvents = {
    ontouchstart : 'onmousedown',
    ontouchmove : 'onmousemove',
    ontouchend : 'onmouseup',
    pos : function(evt) {
        return {clientX: evt.mouse().client.x, clientY: evt.mouse().client.y};
    }
};


/**
 * Events to map when displayed on a device with a touch screen
 */
var mobileTouchEvents = {
    ontouchstart : 'ontouchstart',
    ontouchmove : 'ontouchmove',
    ontouchend : 'ontouchend',
    pos : function(evt) {
        return evt.event().changedTouches[0];
    }
};


/**
 * Tool Definitions
 * <p>
 * Each tool is converted to an image in the tool palette in the 
 * order listed below.   The image gets an id 'tool<name>' where name is
 * the name defined below.
 */
var toolPalette = [
    {name: 'hideTools', img: 'images/tool_arrow.png'},
    {name: 'magnify', img: 'images/tool_magnify.png'},
    {name: 'pan', img: 'images/tool_pan.png'},
    {name: 'overlay', img: 'images/tool_overlay.png'},
    {name: 'wl', img: 'images/tool_wl.png'},
    {name: 'wlpreset', img: 'images/tool_wlpreset.png'},
    {name: 'prevSeries', img: 'images/tool_prev_series.png'},
    {name: 'nextSeries', img: 'images/tool_next_series.png'},
    {name: 'prevImage', img: 'images/tool_prev_image.png'},
    {name: 'nextImage', img: 'images/tool_next_image.png'},   
    {name: 'pause', img: 'images/tool_pause.png', hidden: true},
    {name: 'play', img: 'images/tool_play.png', hidden: true},
    {name: 'halfspeed', img: 'images/tool_halfspeed.png', hidden: true},
    {name: 'reset', img: 'images/tool_reset.png'}   
];

/**
 * Active set of event mappings
 */
var touchEvents = null;

addLoadEvent(function() {
    // Bind events to all the dicom thumbnails to add tools to the viewer
    forEach(filter(function(t){return (t && t.series && (t.series.mimeType == "application/dicom"));}, thumbnails), function(t) { 
        connect(t, "displayed", function() { 
            showTools();
        });
    });
    
    // For now, we enable touch events only for WebKit mobile browsers
    touchEvents = 
        isMobileBrowser() ?  mobileTouchEvents : browserTouchEvents;
    
    connect(events, 'resetSeries', resetTools);
    
});

var toolTimeout = null;
var dtapTimer = 0;

function showTools() {
    
    log("Showing tools");
    
    if($('toolPalette')) {
       $('toolPalette').style.display = 'block';
    }
    else {
        var div = DIV({id:'toolPalette','class':'toolPalette'});
        forEach(toolPalette, function(t) {
            var img = IMG({'class': 'toolImage', id: 'tool'+t.name, src: t.img});
            if(t.hidden)
                addElementClass(img,'hidden');
            appendChildNodes(div, img);     
            connect(img, touchEvents.ontouchstart, function(evt) {
                
                // double tapping on a mobile browser causes zoom-in which
                // is really annoying if you just happen to hit a tool 
                // too fast
                var now = new Date().getTime();
                if(mobile && (now-dtapTimer < 500))  {
                    evt.preventDefault();
                }
                dtapTimer = now;
                    
                resetToolTimeout();
                signal(toolPalette,t.name, img);
            }); 
            img.events = [];
        });
        // appendChildNodes($('mainImage'), div);
        appendChildNodes(document.body, div); 
        
        // Window / Level is the default tool, so signal it 
        // just as if it had been clicked - not on mobile 
        // because it makes panning around really hard
        if(!mobile && (p.studies[0].series[currentSeriesIndex].instances[currentImage].numFrames<=1)) {
            signal(toolPalette, 'wl', $('toolwl')); 
        }
    }
    resetToolTimeout(); 
}

var autoHide = false;
function resetToolTimeout() {
    if(toolTimeout)
        clearTimeout(toolTimeout);
    
    if(autoHide)
        toolTimeout = setTimeout(partial(fade,'toolPalette'), 4000);
    
}

function resetTools() {
    forEach($$('#toolPalette img'), function(img) {
        deactivateTool(img);
    });
    if($('toolPalette'))
        removeElement('toolPalette');
    
}

connect(toolPalette, 'prevImage', function() {
    displayPreviousImage();
});
connect(toolPalette, 'nextImage', function() {
    displayNextImage();
});
connect(toolPalette, 'prevSeries', function() {
    // we want to wrap around - so try every thumbnail
    // in order of distance from current
    for(var i = 1; i<thumbnails.length; ++i) {
        var index = (currentThumb-i)%thumbnails.length;
        if(index<0)
            index = thumbnails.length + index;
        var t = thumbnails[index];
        if(t && t.series && (t.series.mimeType == 'application/dicom')) {
            thumbnails[index].display(); 
            return;
        }
    }    
});
connect(toolPalette, 'nextSeries', function() {
    // we want to wrap around - so try every thumbnail
    // in order of distance from current
    log('next series tool selected');
    for(var i = 1; i<thumbnails.length; ++i) {
        var index = (currentThumb+i)%thumbnails.length;
        var t = thumbnails[index]; 
        if(t && t.series && (t.series.mimeType == 'application/dicom')) {
            thumbnails[index].display(); 
            return;
        }
    }
});

function deactivateTool(img) {
    img.active = false;
    removeElementClass(img, 'activeTool');
    if(img.event) {
        disconnect(img.event);
        img.event = null;
    }
    if(img.events) {
        forEach(img.events, function(e) {
            disconnect(e);
        });
        img.events = [];
    }
};

var magnifyEvent;
connect(toolPalette, 'magnify', function(img) {
    if(!img.active) {
        addElementClass('wadoImageArea', 'zoomable');
        addElementClass(img, 'activeTool');
        img.event = connect('wadoImageArea', touchEvents.ontouchstart, function(evt) {
            log("Started touch for magnify"); 
            var imagePos = elementPosition('wadoImageArea');
            var pos = touchEvents.pos(evt);
            var clickX = pos.clientX;
            var clickY = pos.clientY;
            
            log("Zooming around point " + clickX + "," + clickY);
            var imageArea = elementDimensions('wadoImage');
            
            // 4 x magnification - so, we want the zoom rectangle
            // we set to be 1/4 in each dimension
            var zoomRadius = { w:  Math.round(imageArea.w/8), h: Math.round(imageArea.h/8) };
            log("Zoom radius = " + zoomRadius.w); 
            
            handleZoomRegion(clickX-zoomRadius.w, clickY-zoomRadius.h, clickX+zoomRadius.w, clickY+zoomRadius.h, imageArea.w, imageArea.h);
            setThumbnailRectangleOnSeries();
            deactivateTool(img);
            removeElementClass('wadoImageArea', 'zoomable');
            signal(toolPalette, 'pan', $('toolpan'));
        });
        img.active = true;
        signal(toolPalette,'exclusiveToolSelect',img);
    }
    else {
        deactivateTool(img);
        removeElementClass('wadoImageArea', 'zoomable');
    }
    connect(toolPalette, 'exclusiveToolSelect', function(toolImg) {
        if(toolImg != img && img.active) {
            deactivateTool(img);
            removeElementClass('wadoImageArea', 'zoomable');
        }
    });
});


// --------------------------- Pan Function --------------------------- 

connect(toolPalette, 'pan', function(img) {
    if(!img.active) {
        
        // If not zoomed, do not let them even
        // activate it
        if(!currentToolState.currentRegion) 
            return;
        
        
        addElementClass(img, 'activeTool');
        var div;
        var start;
        var imgStart;
        addElementClass('wadoImageArea','movable');
        img.event = connect('wadoImageArea', touchEvents.ontouchstart, function(evt) { 
            
            evt.preventDefault();
            
            if(!currentToolState.currentRegion) 
               return;
            
            start = {x: touchEvents.pos(evt).clientX, y: touchEvents.pos(evt).clientY};
            
            timedMessage('wadoImageArea', 'Touched ' + start.x + "," + start.y);
            appendChildNodes('wadoImageArea', div);
            imgStart = elementPosition('wadoImage');
            log("Mouse down for pan operation: " + start);
        });
        
        img.events.push(connect('wadoImageArea', touchEvents.ontouchmove, function(evt) {
            evt.preventDefault();
            if(!start)
                return;
            var pos = {x: touchEvents.pos(evt).clientX, y: touchEvents.pos(evt).clientY};
            timedMessage('wadoImageArea', "Moved " + pos.x + "," + pos.y);
            var offsetPos = elementPosition('wadoImageArea');
            setElementPosition('wadoImage',{x:imgStart.x+(pos.x-start.x-offsetPos.x), y: imgStart.y+(pos.y-start.y)});
            
            var delta = calculatePanDelta(start, pos);
            var cts = currentToolState;
            var thumbPos = {
                regionX1: cts.regionX1 - delta.x,
                regionY1: cts.regionY1- delta.y,
                regionX2: cts.regionX2 - delta.x,
                regionY2: cts.regionY2 - delta.y            };
            
            setThumbnailRectangleOnSeries(thumbPos);
        })); 
        
        connect('wadoImageArea', touchEvents.ontouchend, function(evt) {
            evt.preventDefault();
            
            if(!start) {
                log("got mouse up without mouse down");
                return;
            }
            
            var delta = calculatePanDelta(start, {x:touchEvents.pos(evt).clientX, y: touchEvents.pos(evt).clientY});
            
            var cts = currentToolState;
            cts.regionX1 -= delta.x;
            cts.regionY1 -= delta.y;
            cts.regionX2 -= delta.x;
            cts.regionY2 -= delta.y;
            if(cts.regionX1<0)
                cts.regionX1 = 0;
            if(cts.regionY1<0)
                cts.regionY1 = 0;
            if(cts.regionX1>1.0)
                cts.regionX1 = 1.0;
            if(cts.regionY1>1.0)
                cts.regionY1 = 1.0;
            
            var regionParams = "&region=" +
                [cts.regionX1,cts.regionY1,cts.regionX2, cts.regionY2].join(",");
                
            cts.currentRegion = regionParams;
            start = null;
            displayCurrentImage();
            setThumbnailRectangleOnSeries();
            imageCache.reset();
        });
        
        img.active = true;
        signal(toolPalette,'exclusiveToolSelect',img);
    }
    else {
        deactivateTool(img);
        removeElementClass('wadoImageArea', 'movable');
    }
    connect(toolPalette, 'exclusiveToolSelect', function(toolImg) {
        if(toolImg != img && img.active) {
            removeElementClass('wadoImageArea', 'movable');
            deactivateTool(img);
        }
    });
});

// --------------------------- Cine Controls --------------------------- 

connect(events, 'beginAnimation', function(anim) {
    log('cine starting - showing cine controls');
    removeElementClass('toolpause', 'hidden');
    var stop = function() {
        addElementClass('toolpause', 'hidden');
        forEach(disc, disconnect);
    };
    
    var disc = [
        connect(events, 'onCurrentImageChange', stop),
        connect(events, 'onCurrentThumbChange', stop)
    ];
});

connect(toolPalette, 'pause', function() {
    if(animation && animation.timer) {
    	animation.pause();
    }
    addElementClass('toolpause', 'hidden');
    removeElementClass('toolhalfspeed', 'hidden');
    thumbnails[currentThumb].updateLabels(currentThumb);
});
connect(toolPalette, 'halfspeed', function() {
    if(animation) {
	    animation.start(ANIMATION_FRAME_RATES[3]);
    }
    thumbnails[currentThumb].updateLabels(currentThumb);
    addElementClass('toolhalfspeed', 'hidden');
    removeElementClass('toolplay', 'hidden');
});
connect(toolPalette, 'play', function() {
    if(animation) {
	    animation.start(ANIMATION_FRAME_RATES[5]);
    }
    thumbnails[currentThumb].updateLabels(currentThumb);
    addElementClass('toolplay', 'hidden');
    removeElementClass('toolpause', 'hidden');
});

function calculatePanDelta(start, pos) {
    var tl = {x: currentToolState.regionX1, y: currentToolState.regionY1 };
    var br = {x: currentToolState.regionX2, y: currentToolState.regionY2 };
            
    // The new region is offset from the old region by the amount of the drag, scaled 
    // up by the magnification factor
    var xmag = 1/(br.x - tl.x);
    var ymag = 1/(br.y - tl.y);
            
    // Move the positions over by the amount scaled by the zoom factor
    var xdelta = ((pos.x - start.x)/imageWidth)/xmag;
    var ydelta = ((pos.y - start.y)/imageHeight)/ymag;
    
    return { x: xdelta, y: ydelta };}

connect(toolPalette, 'overlay', function() {
    toggleOverlay();
});

function nop() {
}


// --------------------------- Window / Level Function --------------------------- 

/**
 * 
 * When active, clicking and dragging mouse adjusts window width (horizontal)
 * and level (vertical).   The viewer area is treated as a drag surface
 * and the distance dragged is used to interpolate to one of the values
 * defined in the levelValues and windowValues arrays.   Note that these
 * are logarithmic. 
 */

/**
 * Range of level values considered
 */var levelValues = [  -4096, -2560, -2048, -1536, -1024, -896, -768, -512, -256, 128, 0, 128, 256, 512, 768, 896, 1024, 1536, 2048, 2560, 3072, 4096  ];

/**
 * Window width values 
 */
var windowValues = [  1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 3000, 6000, 12000 ];

function findRange(val,lst) {
    for(var i=0;i<lst.length-1; ++i) {
        if(lst[i]<=val && lst[i+1]>val) { 
            return i;
        }
    }
    return lst.length-1;
}

function listRange(i, lst) {
    if(i<0)
        i=0;
    if(i>lst.length-1)
        i=lst.length-1;
    return i;
}

var wl = {
  size : 100,      
  origin : {x: 0, y: 0 },
  tiles : { },
  reset: function() {
      for(i in wl.tiles) {
          wl.tiles[i].remove();
      }
  },
  tile:function(x,y) {
      return wl.tiles[x+','+y];
  }
};

function Tile(x,y, w,l) {
    log("New tile for w="+w);
    this.x = x;
    this.y = y;
    this.w = w;
    this.l = l;
    this.pos = { x: wl.origin.x + x*wl.size, y: wl.origin.y + y*wl.size };
    this.div = DIV({'class':'wlTile'},'x:'+this.x+',y:'+this.y, IMG({src:'images/ajax-loader.gif', style: 'position: absolute; top: 44%; left: 44%;'}));
    setElementDimensions(this.div, {w: wl.size-2, h: wl.size-2});
    setElementPosition(this.div, this.pos);
    appendChildNodes('wadoImageArea', this.div);
    wl.tiles[x+','+y] = this;
    var gridUrl = getImageUrl(thumbnails[currentThumb].series.index,currentImage, {
        maxRows: wl.size-4,
        maxCols: wl.size-4, 
        imageQuality: 50,
        windowCenter: l,
        windowWidth: w 
    });
    log("tile image url = " + gridUrl);
    this.img = new Image();
    var img = this.img;
    img.style.position = 'absolute';
    img.style.top = '0px';
    img.style.left = '0px';
    var d = this.div;
    this.img.onload = this.img.onabort = this.img.onerror = function() {
        appendChildNodes(d,img);
    };
    this.img.src = gridUrl;
}

Tile.prototype.highlight = function() {
    addElementClass(this.img, 'highlighted');   
};
Tile.prototype.unhighlight = function() {
    removeElementClass(this.img, 'highlighted');   
}

Tile.prototype.remove = function() {
    var d = this.div;
    fade(this.div, {afterFinish: function() {removeElement(d);}});
    delete wl.tiles[this.x+','+this.y];
};

Tile.prototype.img = null;
Tile.prototype.div = null;

var invalidMask=[];
connect(toolPalette, 'wl', function(img) {

    if(!img.active) { 
        
        addElementClass(img, 'activeTool');
        
        var dragged = false;
        var shown = false;
        var div;
        var start;
        var dragRegionSize = imageWidth-100;
        var currentTile = null;
        
        addElementClass('wadoImageArea','crosshairs');
        appendChildNodes('wadoImageArea', wl.div);
        img.event = connect('wadoImageArea', touchEvents.ontouchstart, function(evt) { 
            evt.preventDefault();
            
            start = {x: touchEvents.pos(evt).clientX, y: touchEvents.pos(evt).clientY};
            timedMessage('wadoImageArea','Touched ' + start.x + "," + start.y);
            /*
            div = DIV({id:'touchInfo',
                       style:'position: absolute; top: 4px; left: 0px; z-index: 100000; color: white; background-color: blue;'}, 
                        'Touched ' + start.x + "," + start.y);
            appendChildNodes('wadoImageArea', div);
            */
            
            wl.reset();
            wl.origin = { x: start.x - wl.size/2 - getElementPosition('wadoImageArea').x, y: start.y - wl.size/2 };
            currentTile = wl.tiles['0,0']=new Tile(0,0,currentToolState.currentWindowLevel.w, currentToolState.currentWindowLevel.l);
            
            log("Mouse down for wl operation: " + start);
        });
        
        img.events.push(connect('wadoImageArea', touchEvents.ontouchmove, function(evt) {
            evt.preventDefault();
            if(!start)
                return;
            var pos = {x: touchEvents.pos(evt).clientX, y: touchEvents.pos(evt).clientY};
            
            var gridPos = {x: Math.round((pos.x - start.x)/wl.size), y: Math.round((pos.y - start.y)/wl.size)};
            var newWL = computeWL(gridPos);
            log("Grid position = " + gridPos.x + "," + gridPos.y);
            var tile = wl.tile(gridPos.x,gridPos.y);
            if(!tile) {
                tile = new Tile(gridPos.x,gridPos.y,newWL.w, newWL.l);
            }
            tile.highlight();
            
            if(currentTile != tile) {
                currentTile.unhighlight();
                currentTile = tile;
            }
            
            timedMessage('wadoImageArea', "W/L=("+newWL.w + "," + newWL.l+")");
        })); 
        
        connect('wadoImageArea', touchEvents.ontouchend, function(evt) {
            evt.preventDefault();
            
            if(!start) {
                log("got mouse up without mouse down");
                return;
            }
            
            var pos = {x: touchEvents.pos(evt).clientX, y: touchEvents.pos(evt).clientY};
            var gridPos = {x: Math.round((pos.x - start.x)/wl.size), y: Math.round((pos.y - start.y)/wl.size)};
            var tile = wl.tile(gridPos.x, gridPos.y);
            var newWL = currentToolState.currentWindowLevel = {w:tile.w, l:tile.l};
            
            var wIndex=findValue(windowValues, newWL.w);
            var lIndex=findValue(levelValues, newWL.l);
            timedMessage('wadoImageArea', "W/L=("+newWL.w + "," + newWL.l+")");
            
            start = null;
            
            displayCurrentImage();
            wl.reset();
        });
        
        img.active = true;
        signal(toolPalette,'exclusiveToolSelect',img);
    }
    else {
        deactivateTool(img);
        removeElementClass('wadoImageArea', 'crosshairs');
    }
    
    connect(toolPalette, 'exclusiveToolSelect', function(toolImg) {
        if(toolImg != img && img.active) {
            deactivateTool(img);
        }
    });
    
});


/**
 * Compute window and level values for a specified grid position
 * @return object of form {w: window, l: level}
 */
function computeWL(delta) {
    var wl = clone(currentToolState.currentWindowLevel);
    var win = findRange(wl.w, windowValues);
    var lev = findRange(wl.l, levelValues);
    log("old win = " + wl.w + " index = " + win);
    
    var newIndexes = {
        w: listRange(win+delta.x,windowValues),
        l: listRange(lev+delta.y, levelValues)
    };
    
    wl.w = windowValues[newIndexes.w];
    wl.l = levelValues[newIndexes.l];
    return wl;
}

// --------------------------- Window / Level Preset Function --------------------------- 

var defaultPresets = [
    {name:'CT Abdomen', level: 40, window: 350},
    {name:'CT Bone', level: 300, window: 1500},
    {name:'CT Brain', level: 50, window: 100},
    {name:'CT Lung', level: -500, window: 1400}
];

/**
 * Displays a menu of presets that are defined in the DICOM for the current
 * image.
 */
connect(toolPalette, 'wlpreset', function(img) {
    
    try {
        var m=$('basicmenu');
        if(m) 
            removeElement('basicmenu');
        
        signal(toolPalette,'exclusiveToolSelect',img);
        
        var menu = new YAHOO.widget.Menu("basicmenu", { fixedcenter: true });
        var series = thumbnails[currentThumb].series;
        var allPresets = [].concat(series.presets, defaultPresets);
            
        var items = map(function(preset) {
            // NOTE: the url property is necessary or weird bugs occur when
            // the menu is called up multiple times
            return { text: preset.name, clicktohide: true, url: 'javascript:nop()', onclick: { fn: function() {
                timedMessage('ViewerArea', 'w='+preset.window+',l='+preset.level);
                setWindowLevel(preset.window,preset.level);
                displayCurrentImage(); 
                menu.destroy();
                return false; 
            }}}; 
        }, allPresets);
        
        var resetMenuOption = {text: 'Reset to Default', url: 'javascript:nop()', onclick: {fn: function() {
            currentToolState.currentWindowLevel = null;
            displayCurrentImage(); 
            menu.destroy();
            return false;
        }}};
        
        menu.addItems([items, [  resetMenuOption ]]);
        menu.render($('wadoImageArea'));
        
        m=$('basicmenu');
        m.style.visibility='visible';
        m.style.top='340px';
        m.style.zIndex=11000;
        m.style.width= '22em';
        m.style.left= 'auto';
        
        if($('toolPalette')) {
            var toolsPos = elementPosition('toolPalette');
            m.style.right = elementDimensions('toolPalette').w + 'px';
        }
        else {
            m.style.right= '10px';
        }

        $$('#basicmenu a')[0].focus();
    }
    catch(e) {
        dumpProperties('failed to create menu', e);
    }
});

// --------------------------- Hide Tools Function --------------------------- 
var toolsVisible = true;
connect(toolPalette, 'hideTools', function(me) {
    if(toolsVisible) {
        forEach($$('#toolPalette img'), function(img) {
            if(img != me)
                addElementClass(img, 'invisible');
        });
    }
    else {
        forEach($$('#toolPalette img'), function(img) {
            removeElementClass(img, 'invisible');
        });
    }
    toolsVisible=!toolsVisible;
});


// --------------------------- Reset Function --------------------------- 
connect(toolPalette, 'reset', function(me) {
    
    var series = p.studies[0].series[currentSeriesIndex];
    series.lastImageIndex = 0;
    selectSeries(series);
    thumbnails[currentThumb].display();
});

// Disable zoom drag
noZoomDrag = true;
