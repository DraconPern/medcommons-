<?php

global $_rollovers;
$_rollovers=array(
array('sprite-1C-UnifiedViewer' , 'Unified Viewer',
		"http://fb01.medcommons.net/img/1C - Unified Viewer.png"
		, "1C - Unified Viewer" ,
		"http://fb01.medcommons.net/img/1B---Unified-Viewer.gif"
		, "1B - Unified Viewer"  , "upleft" ),

		array('sprite-2C-Dashboard',
		"Patient's Information Request"  , 
		"http://fb02.medcommons.net/img/5C - Request.png"
		, "5C - Request." ,
		"http://fb02.medcommons.net/img/5B---Request.gif"
		, "5B - Request"  , "upmiddle" ),
		array('sprite-3C-FaxIn' , 'Automated Fax In'  ,
		"http://fb01.medcommons.net/img/3C - Fax In.png"  , "3C - Fax In" , "http://fb01.medcommons.net/img/3B---Fax-In.gif"
		, "3B - Fax In"  , "upright" ),
		array('sprite-4C-Radiology' , 'Radiology Imaging'
		,
		"http://fb02.medcommons.net/img/4C - Radiology.png"
		, "4C - Radiology" ,
		"http://fb02.medcommons.net/img/4B---Radiology_M.jpg"
		, "4B - Radiology"  , "midleft" ),
		array('sprite-5C-Request' , 'Sharing Options'  ,
		"http://fb01.medcommons.net/img/2B - Dashboard.png"
		, "2C - Dashboard" ,
		"http://fb01.medcommons.net/img/2B---Sharing-Dashboard.gif"
		, "2B - Sharing Dashboard"  , "midmiddle" ),
		array('sprite-6C-Voucher',
		'Information Service Voucher'  , 
		"http://fb02.medcommons.net/img/6C - Voucher.png",
		 "6C - Voucher" ,
		"http://fb02.medcommons.net/img/6B---Voucher.gif",
		 "6B - Voucher"  , "midright" ),
		array('sprite-7C-Facebook' , 'Care Teams'  ,
		"http://fb01.medcommons.net/img/7C - Facebook.png"
		, "7C - Facebook" ,
		"http://fb01.medcommons.net/img/7B---Facebook.gif"
		, "7B - Facebook"  , "downleft" ),

		array('sprite-8C-AWS' , 'Amazon Payments to Doctor'
		,
		"http://fb02.medcommons.net/img/9C - Amazon Payments.png"
		, "9C - Amazon Payments" ,
		"http://fb02.medcommons.net/img/9B---Amazon-Payments.gif"
		, "9B - Amazon Payments"  , "downmiddle" ),
		array('sprite-9C-AmazonPayments',
		'Health Records in the Cloud'  , 
		"http://fb01.medcommons.net/img/8C - AWS.png"  , "8C - AWS" ,
		"http://fb01.medcommons.net/img/8B---AWS.gif"  , "8B - AWS"
		, "downright" )
		);
		function getrollover($j)
		{
			global $_rollovers;
			$x = $_rollovers[$j];
			return array ($x[1],$x[4],$x[2]);

		}
		function tabletop ()
		{
			return
				"
			<style type='text/css'>
			.rollovers img {border:none; width:40px; }
			</style>
			<table class=rollovers ><tbody>";
		}
		function tablebottom()
		{
			return
				"
			</tbody></table>
			";
		}
		function anchorimg($k){
			list ($label,$largeimage,$smallimage) = getrollover($k);
			// one cell
			$urle = urlencode($largeimage);
			return  "<a title='$label'  href='http://www.medcommons.net/rollview.php?a=$urle' ><img alt='$label' src='$smallimage' /></a>";
		}
		function threebythree()
		{
			/* layout a little table */

			$buf = tabletop();
			for ($i=0; $i<3; $i++)
			{
				$buf .= "
					<tr>";
				for ($j=0; $j<3; $j++ )
				$buf .="<td>".anchorimg($j+3*$i)."</td>";
				$buf .= '</tr>
				';
			}
			$buf .= tablebottom();
			return $buf;
		}
		function twobyfour()
		{
			/* leaves out last */

			$buf = tabletop();
			for ($i=0; $i<4; $i++)
			{
				$buf .= "
					<tr>";
				for ($j=0; $j<2; $j++ )
				$buf .="<td>".anchorimg($j+2*$i)."</td>";
				$buf .= '</tr>
				';
			}
			$buf .= tablebottom();
			return $buf;
		}

		function onebynine()
		{
			/* leaves out last */

			$buf = tabletop();
			for ($i=0; $i<1; $i++)
			{
				$buf .= "
					<tr>";
				for ($j=0; $j<9; $j++ )
				$buf .="<td>".anchorimg($j+1*$i)."</td>";
				$buf .= '</tr>
				';
			}
			$buf .= tablebottom();
			return $buf;
		}


		echo threebythree();
		echo '<hr/>';
			echo twobyfour();
		echo '<hr/>';
			echo onebynine();
		echo '<hr/>';




		?>