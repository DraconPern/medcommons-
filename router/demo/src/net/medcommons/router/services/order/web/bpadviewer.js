/* Copyright 2010 MedCommons Inc.   All Rights Reserved. */

/**
 * Functions supporting the viewer inside the ipad
 */
function call_ipad(service, args) {
    
    // This is ONLY for debug purposes -
    // display the URL that ipad would have used
    var url = compute_ipad_url(service,args);
    log("IPAD: " + url);
    
    if(service == 'thumb') {
        switch(args.op) {
            case 'add': 
                add_thumbs(args);
                break;
            case 'mod': 
                update_thumbs(args);
                break;
            default:
                alert('Unknown operation: ' + args.op + ' for service ' + service);
        }
    }
    else 
    if(service == 'scrub') {
        switch(args.op) {
            case 'mod':
                update_scrubber(args);
                break;
            default:
                alert('Unknown operation: ' + args.op + ' for service ' + service);
        }
    }
    else 
    if(service == 'tool') {
        switch(args.op) {
            case 'add':
                add_tool(args);
                break;
            case 'mod':
                update_tool(args);
                break;
            default:
                alert('Unknown operation: ' + args.op + ' for service ' + service);
        }
    }
    else 
    if(service == 'episodes') {
        switch(args.op) {
            case 'add':
                add_episode(args);
                break;
            default:
                alert('Unknown operation: ' + args.op + ' for service ' + service);
        }
    }
    
}

/**
 * Slightly different escaping of strings
 */
function encode_ipad_prm(jsprm) {
    return jsprm;
}


/**
 * Add an onclick handler that emulates the way the iPad
 * would signal the same event to the iPad viewer
 */
function connect_handler(e, args) {
    if(!args.jsevt)
        return;
    
    connect(e, 'onclick', function() { 
        dispatch_event(args);
    });
}

function dispatch_event(args) {
    if(args.jsprm) {
        eval(args.jsevt+"(" + args.jsprm + ")");
    }
    else 
        eval(args.jsevt+'()');
}


// =============== Thumbnail Service ===============
var viewerState = {
    thumbnailCount : 0       
};

function add_thumbs(args) {
    
    args = merge({ title: '', subtitle: '', url: "images/transparentblank.gif"}, args);
    
    // var i = viewerState.thumbnailCount;
    var i = args.id;
    appendChildNodes('lefttop', 
        DIV({id: 'thumb'+i, 'class':'thumbCellBox', style: 'top: ' + (i*165) + 'px'},
        cell = DIV({id:'thumbCell'+i,
             'class': 'NotSelectedImage thumbCell' 
        }),
        DIV({id:'thumbLabel'+i, 'class':'ThumbLabel'},args.title),
        DIV({id:'thumbDescription'+i, 'class':'ThumbDescription'}, args.subtitle),
        DIV({id:'thumbTime'+i, 'class':'ThumbTime'}),
        ann = DIV({'class':'thumbAnnotation'}, IMG({id:'thumbAnnotation'+i, src:'images/transparentblank.gif'}))
    ));
    
    if(args.jsevt) {
        connect(cell, 'onclick', function() { 
            if(args.jsprm) {
                eval(args.jsevt+'(' + args.jsprm + ')');
            }
            else 
                eval(args.jsevt+'()');
        });
    }
    
    viewerState.thumbnailCount++;
}

function update_thumbs(thumb) {
    
    /*
    if(thumb.id == 0)
        return;
        */
    
    var thumbIndex = thumb.id;
    var thumbCellDiv = $('thumbCell'+thumbIndex);
    
    if(thumb.url) {
        log("Displaying thumb as url " + thumb.url);
        if(!$('thumbImage'+thumbIndex)) {
            thumbCellDiv.innerHTML=
                '<IMG height=140 src="'+thumb.url+'" width=140 border=0 name="thumbImage'+thumbIndex+'" id="thumbImage'+thumbIndex+'">';
        }
        else  {
            $('thumbImage'+thumbIndex).src = thumb.url;
        }
    }
        
    if(thumb.subtitle)
        replaceChildNodes("thumbDescription" + thumbIndex,thumb.subtitle);
    
    if(thumb.title && $("thumbLabel" + thumbIndex)) {
        $("thumbLabel" + thumbIndex).innerHTML = thumb.title; 
    }
    
    if(thumb.jsevt) {
        disconnectAll(thumbCellDiv);
        connect(thumbCellDiv, 'onclick', function() { 
            if(thumb.jsprm) {
                eval(thumb.jsevt+'(' + args.jsprm + ')');
            }
            else 
                eval(thumb.jsevt+'()');
        });
    }
}

// =============== Scrubber Service ===============

var scrubber_callback = null;
var scrubber_param = null;
var scrubber_expanded = false;

function update_scrubber(args) {
   if(args.vis) {
       // TODO: this can fire before we are ready
       if(!layout2) {
           log("Skipping scrubber update because layout not ready: " + args.vis);
           return;
       }
       
       if(!scrubber) {
           log("Skipping scrubber update because scrubber not ready: " + args.vis);
           return;
       }
       
       log("scrubber vis = " + args.vis);
       switch(args.vis) {
           case 'show':
               if(!scrubber_expanded) {
                   layout2.getUnitById('footer').expand();
                   scrubber_expanded = true;
               }
               break;
       
           case 'hide':
               layout2.getUnitById('footer').collapse();
               scrubber_expanded = false;
               break;
               
           default:
               alert('Invalid argument for scrubber "vis" configuration: ' + vis);
       }
   }     
       
   if(typeof args.minval != 'undefined') {
        set_scrubber_int('min', args.minval);
   }
       
   if(typeof args.maxval != 'undefined') {
        set_scrubber_int('max', args.maxval);
   }
       
   if(typeof args.curval != 'undefined') {
        set_scrubber_int('value', args.curval);
   }
       
   if(typeof args.jsprm != 'undefined') {
        scrubber_param = args.jsprm;
   }
       
   if(typeof args.jsevt != 'undefined') {
        scrubber_callback = args.jsevt;
   }
}

function set_scrubber_int(att, val) {
    if(typeof val  == 'string') {
           if(isNaN(val = parseInt(val)))
                   throw "Invalid value for scrubber "+ att;
    }
    scrubber.set(att, val);
    log('scrubber: ' + att  + ' => ' + val);
}

// =============== Tool Service ===============

function add_tool(args) {
    var t;
    appendChildNodes('right', 
        t = DIV({'class':'tool', id: 'tool_'+args.id},
           IMG({src: 'images/tool_'+args.img+'.png'})
        )
    );
    connect_handler(t, args);
}

function update_tool(args) {
    var id = 'tool_'+args.id;
    if(!$(id)) {
        alert('Unknown tool: ' + args.id);
        return;
    }
    if(args.img) {
        var active = false;
        log("Updating image: " + args.img);
        if(args.img.match(/_active$/)) {
            args.img = args.img.replace(/_active$/,"");
            active = true;
            log("Image " + args.img + " is active");
        }
        
        Y.one('#'+id+' img').set('src','images/tool_'+args.img+'.png');
        if(active)
            Y.one('#'+id + ' img').addClass('activeTool');
        else
            Y.one('#'+id + ' img').removeClass('activeTool');
    }
}

// =============== Episodes Service ===============

function add_episode(args) {
    episodesButton.get("menu").push({text:args.title, onclick: { fn: function() { dispatch_event(args); }}});
}


// =============== General Init ===============

var scroller, scrubber, layout, layout2, episodesButton;

(function() {
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event;
 
    Event.onDOMReady(function() {
        addElementClass(document.body, 'yui-layout');
        layout = new YAHOO.widget.Layout({
            units: [
                { position: 'left',  body: 'lefttop', header: 'Thumbnails', width: 212,  gutter: '5', scroll: null, zIndex: 1,  animate: false },
                { position: 'center', body: 'center', gutter: '5 5' },
                {
                   position: 'right',  body: 'right', header: 'Tools', width: 65,  gutter: '5', scroll: null, zIndex: 1,  animate: false 
                }
            ]
        });
        
        layout.on('render', function() {
                var wrapper = layout.getUnitByPosition('center').get('wrap');
                layout2 = new YAHOO.widget.Layout(wrapper, {
                    parent: layout,
                    units: [
                        {
                          position: 'center', body: 'ViewerArea', gutter: '2px', scroll: true, 
                          header: '<div class="ccrHeader">'+htmlentities(p.PatientName) + 
                                  ' - Current CCR ' + formatDateOfBirth(new Date(ccr.createDateTime))+
                                  '<div id="episodesMenu"></div></div>'  
                        },
                        { position: 'bottom', header: 'Scrubber', body: 'footer', height: 70, gutter: '2px'}
                    ]
                });
                layout2.render();
                layout2.getUnitById('footer').collapse();
                
                var episodes = [
                ];                
                
                episodesButton = new YAHOO.widget.Button({
                      id: "episodesSplitButton",
                      type: "split",
                      label: "Episodes", 
                      name: "episodesSplitButton", 
                      menu: episodes, 
                      container: $("yui-gen3") 
                });
        });
 
        layout.render();
        
        YUI({
            base: 'yui3/3.2.0/', // the base path to the YUI install.  Usually not needed because the default is the same base path as the yui.js include file
            filter: 'raw' // apply a filter to load the raw or debug version of YUI files
        }).use('node', 'event', 'scrollview-base', 'slider', 'node-menunav', function(Y) {
            window.Y = Y;
            var scrollView = new Y.ScrollView({
                contentBox: '#lefttop',
                height: viewportSize().h - 65,
                flick : {
                    minDistance:0,
                    minVelocity:0
                }
            });
            scrollView.render(); 
            scrubber = new Y.Slider({length: elementDimensions('footer').w - 10});
            scrubber.render("#footer");
            scrubber.on('thumbMove', function(evt) {
                if(scrubber_callback) {
                    
                    if(scrubber_param)
                        eval(scrubber_callback+'('+scrubber.getValue()+',' + scrubber_param+')');
                    else
                        eval(scrubber_callback+'('+scrubber.getValue()+')');
                }
            });
        });
        
        YAHOO.util.Connect.asyncRequest('GET', 'DisplayCCR.action?ccrIndex=0&body=true', {
                        success: function(r) { 
                            /* $('ccr').innerHTML = r.responseText; 
                            if(isMobileBrowser()) {
                                document.ontouchmove = function(e) { e.preventDefault(); return false; };
                                scroller = new iScroll( document.getElementById('ccr') );
                                scroller.refresh(); 
                            }
                            */
                        },
                        failure: function(r) { alert('failed'); }
        } , "");
        setTimeout(function() { window.scrollTo(0, 1); }, 100);
    });
})();
