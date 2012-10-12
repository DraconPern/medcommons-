( &$message,	 $mcid,
               	 $template,
               	 $recipient,
               	 $subjectline,
               	 $a,$b,$c,$d,$e,$f,$g
			)		 
				 { 
				    $trackingnum = $a; // must be first if present
				 	$homepageurl = $GLOBALS['Homepage_Url'];
		
				 	$homepagehtml= "<a href=$homepageurl>$homepageurl</a>";
				 	$trackingurl = $GLOBALS['Tracking_Url'];
				 	$prettytracking = pretty_tracking($trackingnum);
				 	$merge = merge_tracking_mcid($trackingnum,$mcid);
				 	$trackinghtml = "<a href=$trackingurl?a=$merge>$prettytracking</a>";
				 	$mcidhtml = pretty_mcid($mcid);
			 	
				 	$message = <<<XXX

		