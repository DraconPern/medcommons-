
function grow()
{
    document.getElementById('growShrinkStuff').innerHTML  =  '<iframe style=display:none src=mc://viewer?op=mod&comp=scrub&vis=hide>noframe</iframe>'+
    '<iframe style=display:none src=mc://viewer?op=mod&comp=main&zoom=max>noframe</iframe>';
}
function shrink()
{
    document.getElementById('growShrinkStuff').innerHTML  = '<iframe style=display:none src=mc://viewer?op=mod&comp=scrub&vis=show>noframe</iframe>'+
    '<iframe style=display:none src=mc://viewer?op=mod&comp=main&vis=hide>noframe</iframe>';
}
function invoke(s)
{

    document.getElementById('iframeStuff').innerHTML  = '<iframe style=display:none src='+s+' >noframe</iframe>';
}
function doit (event,param)



{
	
	
	if (event != 'scrubber_pressed_event')
		alert ('native code calling doit ('+event+','+param+')');
//	
//	if ((event == 'episode_chosen_event'))
//    {
//        
//        window.location.href = param;
//        return;
//        
//    }
//    if ((event == 'button_pressed_event') && (param == 'episodes_button'))
//    {
//        
//        window.location.href = 'switcher.php';
//        return;
//        
//    }
//    else
//        if ((event == 'button_pressed_event') && (param == 'documents_button'))
//        {
//            
//            window.location.href = 'nativethumbs.php';
//            return;
//            
//        }
//        else
//            if (event == 'thumb_pressed_event')
//            {
//                
//                window.location.href = param;
//                return;
//                
//            }
//			else
//				if (event == 'ccr_pressed_event')
//				{
//					
//					window.location.href = 'ccrviewer.php';
//					return;
//					
//				}
//
//	
}