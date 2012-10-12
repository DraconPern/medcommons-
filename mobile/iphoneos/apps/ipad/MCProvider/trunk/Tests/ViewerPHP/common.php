<?php
    //
    // Common test harness code
    //
    // jgp - 25 Aug 2010
    //

    function out ($s)
    {
        global $buf;

        $buf .= $s;
    }

    function jsout ($s)
    {
        global $jsbuf;

        $jsbuf .= $s;
    }

    function h ($s)
    {
        out ("
             </ul>
             <h2>$s</h2>
             <ul>
             ");
    }

//    function emitauto ($s)
//    {
//        jsout ("
//               + '<iframe src=$s alt=missingiframe>noframesupport</iframe>'
//               ");
//    }
	function emitauto ($s)
    {
        jsout ("invoke('$s');
               ");
    }
    function emitnoauto ($s)
    {
        // do not include these in the generated javascript
        out ("
             <li><a href='$s'>$s</a></li>
             ");
    }

    function emit ($s)
    {
        emitauto ($s);
        emitnoauto ($s);
    }

    function addMenuComponent ($id, $title, $jsevt, $jsprm)
    {
        $title = urlencode ($title);

        emit ("mc://viewer?op=add&comp=menu&id=$id&title=$title&jsevt=$jsevt&jsprm=$jsprm");
    }
	function addEpisodeComponent ($id, $title, $doclist,$jsevt, $jsprm)
    {
        $title = urlencode ($title);
		
        emit ("mc://viewer?op=add&comp=episodes&id=$id&title=$title&doclist=$doclist&jsevt=$jsevt&jsprm=$jsprm");
    }

    function addThumbComponent ($id, $url, $title, $subtitle, $jsevt, $jsprmignored)
    {
        $title = urlencode ($title);
        $subtitle = urlencode ($subtitle);
		$jsprm =urlencode( 'viewer.php?url='.$url);

        emit ("mc://viewer?op=add&comp=thumb&id=$id&url=$url&title=$title&subtitle=$subtitle&jsevt=$jsevt&jsprm=$jsprm");
    }
	function addOverlayComponent ($id, $url, $title, $subtitle, $jsevt, $jsprmignored)
    {
        $title = urlencode ($title);
       // $subtitle = urlencode ($subtitle);
		//$jsprm =urlencode( 'viewer.php?url='.$url);
		
        emit ("mc://viewer?op=add&comp=overlay&id=$id&url=$url&title=$title&jsevt=$jsevt&jsprm=$jsprm");
    }

    function addToolComponent ($id, $img, $jsevt, $jsprm)
    {
        emit ("mc://viewer?op=add&comp=tool&id=$id&img=$img&jsevt=$jsevt&jsprm=$jsprm");
    }

    function modifyHeaderComponent ($id, $title, $jsevt, $jsprm)
    {
        $title = urlencode ($title);

        emit ("mc://viewer?op=mod&comp=head&id=$id&title=$title&jsevt=$jsevt&jsprm=$jsprm");
    }

    function modifyScrubberComponent ($curval, $minval, $maxval, $jsevt, $jsprm)
    {
        emit ("mc://viewer?op=mod&comp=scrub&curval=$curval&minval=$minval&maxval=$maxval&jsevt=$jsevt&jsprm=$jsprm");
    }

    function modifyToolComponent ($id, $img)
    {
        emitnoauto ("mc://viewer?op=mod&comp=tool&id=$id&img=$img");
    }

    function selectThumbComponent ($id, $sel)
    {
        emitnoauto ("mc://viewer?op=mod&comp=thumb&id=$id&sel=$sel");
    }

    function setMainComponentVisibility ($vis)
    {
        emitauto ("mc://viewer?op=mod&comp=main&vis=$vis");
    }
    
    function setMainComponentZoom ($zoom)
    {
        emitauto ("mc://viewer?op=mod&comp=main&zoom=$zoom");
    }
    
    function setScrubberComponentVisibility ($vis)
    {
        emitauto ("mc://viewer?op=mod&comp=scrub&vis=$vis");
    }

    function setThumbsComponentVisibility ($vis)
    {
        emitauto ("mc://viewer?op=mod&comp=thumbs&vis=$vis");
    }

    function setThumbsComponentZoom ($zoom)
    {
        emitauto ("mc://viewer?op=mod&comp=thumbs&zoom=$zoom");
    }
    
    function setToolsComponentVisibility ($vis)
    {
        emitauto ("mc://viewer?op=mod&comp=tools&vis=$vis");
    }
	
	function dummyCall ($vis)
    {
        emitauto ("mc://viewer?op=mod&comp=dummy&vis=$vis"); // this is not really implemented, it should be thrown away by iphad
    }

    //////////////////////////// MAIN /////////////////////////////

    ?>
