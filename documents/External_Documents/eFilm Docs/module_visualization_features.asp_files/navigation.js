    /*****************************************************************************
Linked js file with all the menucreation code inside
******************************************************************************/
oM=new makeCM("oM");

//Menu properties
oM.pxBetween=0
//oM.fromLeft=210
oM.fromTop=0
oM.rows=1
oM.menuPlacement="center"
oM.offlineRoot="file:///C|/active/merge-efilm/web/"
if(document.domain == "beta1.ascedia.com")
{
	oM.onlineRoot="/merge-efilm/web/"
}
else
{
	oM.onlineRoot="/"
}
oM.resizeCheck=1
oM.wait=500
oM.fillImg="images/spacer.gif";
oM.zIndex=400

//Background bar properties
oM.useBar=0
oM.barWidth="100%"
oM.barHeight=0
oM.barClass="clBar"
oM.barX=0
oM.barY=0
oM.barBorderX=1
oM.barBorderY=1
oM.barBorderClass="clLevel0border"

//Level properties - ALL properties have to be spesified in level 0
oM.level[0]=new cm_makeLevel() //Add this for each new level
oM.level[0].width="12%"
oM.level[0].height=0
oM.level[0].regClass="clLevel0"
oM.level[0].overClass="clLevel0over"
oM.level[0].borderX=0
oM.level[0].borderY=0
oM.level[0].borderClass="clLevel0border"
oM.level[0].offsetX=0
oM.level[0].offsetY=0
oM.level[0].rows=0
oM.level[0].arrow=0
oM.level[0].arrowWidth=0
oM.level[0].arrowHeight=0
oM.level[0].align="bottom"
oM.level[0].filter="progid:DXImageTransform.Microsoft.Fade(duration=0.6); alpha(opacity=85, style=0)" //VALUE: 0 || "filter specs"

//EXAMPLE SUB LEVEL[1] PROPERTIES - You have to specify the properties you want different from LEVEL[0] - If you want all items to look the same just remove this
oM.level[1]=new cm_makeLevel() //Add this for each new level (adding one to the number)
oM.level[1].width=oM.level[0].width
oM.level[1].height=20
oM.level[1].regClass="clLevel1"
oM.level[1].overClass="clLevel1over"
oM.level[1].borderX=1
oM.level[1].borderY=1
oM.level[1].align="right"
oM.level[1].offsetX=-20
oM.level[1].offsetY=+10
oM.level[1].borderClass="clLevel1border"

//EXAMPLE SUB LEVEL[2] PROPERTIES - You have to spesify the properties you want different from LEVEL[1] OR LEVEL[0] - If you want all items to look the same just remove this
oM.level[2]=new cm_makeLevel() //Add this for each new level (adding one to the number)
oM.level[2].width=151
oM.level[2].height=20
oM.level[2].borderX=1
oM.level[2].borderY=1
oM.level[2].offsetX=0
oM.level[2].offsetY=0
oM.level[2].regClass="clLevel2"
oM.level[2].overClass="clLevel2over"
oM.level[2].borderClass="clLevel2border"

/******************************************
Menu item creation:
myCoolMenu.makeMenu(name, parent_name, text, link, target, width, height, regImage, overImage, regClass, overClass, align, rows, nolink, onclick, onmouseover, onmouseout) 
*************************************/

// HELP CENTER
oM.makeMenu('top0','','','','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub01','top0','Phone Support','helpcenter/index.asp','',125,0)
	oM.makeMenu('sub02','top0','Online Support','helpcenter/onlinesupport.asp','',125,0)
			oM.makeMenu('sub0201','sub02','RIS Logic Client Support','helpcenter/rislogiccsclient.asp','',180,0)
			oM.makeMenu('sub0202','sub02','eFilm Workstation Forum','forum/displayForum.asp?forum=Workstation','',180,0)
  			oM.makeMenu('sub0203','sub02','Help Files','helpcenter/onlinesupport.asp','',180,0)
			oM.makeMenu('sub0204','sub02','Version Logs','helpcenter/onlinesupport.asp','',180,0)
			oM.makeMenu('sub0205','sub02','DICOM Conformance Statements','helpcenter/onlinesupport.asp','',180,0)
			oM.makeMenu('sub0206','sub02','Support Downloads','helpcenter/onlinesupport.asp','',180,0)
			oM.makeMenu('sub0207','sub02','Software Patches','helpcenter/onlinesupport.asp','',180,0)			
			
	oM.makeMenu('sub03','top0','Industry Resources','helpcenter/industryresources.asp','',125,0)
				oM.makeMenu('sub031','sub03','DICOM','helpcenter/industryresources_dicom.asp','',125,0)
				oM.makeMenu('sub032','sub03','HL7','helpcenter/industryresources_hl7.asp','',125,0)
				oM.makeMenu('sub033','sub03','IHE','helpcenter/industryresources_ihe.asp','',125,0)

// COMPANY INFORMATION
oM.makeMenu('top1','','','#void','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub11','top1','Company Profile','companyinformation/index.asp','',136,0)
	oM.makeMenu('sub12','top1','Management Team','companyinformation/managementteam.asp','',136,0)
	oM.makeMenu('sub13','top1','Board of Directors','companyinformation/boardofdirectors.asp','',136,0)
	oM.makeMenu('sub14','top1','Press Room','companyinformation/pressroom.asp','',136,0)
	oM.makeMenu('sub15','top1','Careers','companyinformation/careers.asp','',136,0)
	oM.makeMenu('sub16','top1','Office Locations','companyinformation/officelocations.asp','',136,0)


// INVESTOR RELATIONS
oM.makeMenu('top2','','','#void','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub21','top2','Analyst Coverage','investor/analysts.asp','',150,0)
	oM.makeMenu('sub22','top2','Board of Directors','companyinformation/boardofdirectors.asp','',150,0)
	oM.makeMenu('sub23','top2','Earnings Call Calendar','investor/confcalllist.asp','',150,0)
	oM.makeMenu('sub24','top2','Information Request','investor/informationrequest.asp','',150,0)
	oM.makeMenu('sub25','top2','MRGE Reports','','',150,0)
		oM.makeMenu('sub250','sub25','Annual','investor/annual_reports.asp','',100,20)
		oM.makeMenu('sub251','sub25','Quarterly','investor/quarterly_reports.asp','',100,20)
		oM.makeMenu('sub252','sub25','Presentations','investor/presentations.asp','',100,20)
	oM.makeMenu('sub26','top2','NASDAQ','','',150,0)
		oM.makeMenu('sub260','sub26','MRGE Real Time Filings','http://www.nasdaq.com/asp/quotes_sec.asp?mode=&kind=&symbol=mrge&FormType=&mkttype=&pathname=&page=filings&selected=MRGE','_blank',150,20)
		oM.makeMenu('sub261','sub26','MRGE Holdings/Insiders','http://www.nasdaq.com/asp/Holdings.asp?mode=&kind=&symbol=mrge&FormType=&mkttype=&pathname=&page=holdingssummary&selected=MRGE','_blank',150,20)
		oM.makeMenu('sub262','sub26','Stock Quote','investor/index.asp','',150,20)
	oM.makeMenu('sub27','top2','Press Releases','companyinformation/pressroom.asp','',150,0)
	oM.makeMenu('sub28','top2','Section 16 Filings','http://www.sec.gov/cgi-bin/browse-edgar?company=&CIK=0000944765&filenum=&State=&SIC=&owner=include&action=getcompany','_blank',150,0)
/*	oM.makeMenu('sub21','top2','Stock Quote','investor/index.asp','',200,0)
	oM.makeMenu('sub22','top2','SEC Filings','investor/documents.asp','',200,0)
	oM.makeMenu('sub23','top2','Press Releases','companyinformation/pressroom.asp','',200,0)
	oM.makeMenu('sub24','top2','Analysts Coverage and Research','investor/analysts.asp','',200,0)
	oM.makeMenu('sub25','top2','Earnings Call Calendar','investor/confcalllist.asp','',200,0)
	oM.makeMenu('sub26','top2','Information Request','investor/informationrequest.asp','',200,0)*/			


// ESTORE
oM.makeMenu('top3','','','estore/index.asp','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub31','top3','My Account','estore/myaccount.asp','',125,0)
	oM.makeMenu('sub32','top3','Free Trial Software','estore/software.asp','',125,0)
	oM.makeMenu('sub33','top3','Purchase Software','estore/software.asp','',125,0)
	oM.makeMenu('sub34','top3','Request Quote','estore/requestquote.asp','',125,0)
	oM.makeMenu('sub35','top3','Class Registration','estore/classes.asp','',125,0)

// CONTACT US
oM.makeMenu('top4','','','#void','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub41','top4','Information Inquiry','contactus/index.asp','',150,0)
	oM.makeMenu('sub42','top4','Locate Us','contactus/locateus.asp','',150,0)
	oM.makeMenu('sub43','top4','Phone Us','contactus/phone.asp','',150,0)
	oM.makeMenu('sub44','top4','Worldwide Locations','contactus/worldmap.asp','',150,0)
				oM.makeMenu('sub4400','sub44','Canada','contactus/worldmap_canada.asp','',120,20)
				oM.makeMenu('sub4401','sub44','Denmark','contactus/worldmap_denmark.asp','',120,20)
				oM.makeMenu('sub4402','sub44','Finland','contactus/worldmap_finland.asp','',120,20)
				oM.makeMenu('sub4403','sub44','France','contactus/worldmap_france.asp','',120,20)
				oM.makeMenu('sub4404','sub44','Germany','contactus/worldmap_germany.asp','',120,20)
				oM.makeMenu('sub4405','sub44','Greece','contactus/worldmap_greece.asp','',120,20)
				oM.makeMenu('sub4406','sub44','Ireland','contactus/worldmap_ireland.asp','',120,20)
				oM.makeMenu('sub4407','sub44','Italy','contactus/worldmap_italy.asp','',120,20)
				oM.makeMenu('sub4408','sub44','Japan','contactus/worldmap_japan.asp','',120,20)
				oM.makeMenu('sub4409','sub44','Netherlands','contactus/worldmap_netherlands.asp','',120,20)
				oM.makeMenu('sub4410','sub44','Norway','contactus/worldmap_norway.asp','',120,20)
				oM.makeMenu('sub4411','sub44','Portugal','contactus/worldmap_portugal.asp','',120,20)
				oM.makeMenu('sub4412','sub44','South Africa','contactus/worldmap_southafrica.asp','',120,20)
				oM.makeMenu('sub4413','sub44','Spain','contactus/worldmap_spain.asp','',120,20)
				oM.makeMenu('sub4414','sub44','Sweden','contactus/worldmap_sweden.asp','',120,20)
				oM.makeMenu('sub4415','sub44','Turkey','contactus/worldmap_turkey.asp','',120,20)
				oM.makeMenu('sub4416','sub44','United Kindgom','contactus/worldmap_uk.asp','',120,20)
				oM.makeMenu('sub4417','sub44','United States','contactus/worldmap_us.asp','',120,20)
	oM.makeMenu('sub45','top4','Newsletters','contactus/notes.asp','',150,0)	
	
// TOP CUSTOMERS	
oM.makeMenu('top5','','','#void','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub50','top5','Customer Stories','customers/index.asp','',150,0)
	oM.makeMenu('sub51','top5','Customer Testimonials','customers/testimonials.asp','',150,0)

// PARTNERS
oM.makeMenu('top6','','','#void','',1,40,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sub61','top6','OEM/VAR','partners/index.asp','',180,0)
//	oM.makeMenu('sub62','top6','Technology Partners','partners/rislogicpartners.asp','',180,0)
//	oM.makeMenu('sub63','top6','VAR Partner Resource Page','partners/varpartners.asp','',180,0)
	
// FUSION INTEGRATED WORKFLOW
oM.makeMenu('side0','','','#void','',1,1,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sidesub00','side0','Workflow Overview','products/workflowoverview/index.asp','',125,20)

// PACS, TELETELERADIOLOGY, AND WEB SOLUTIONS 
oM.makeMenu('side1','','','#void','',1,1,'','','clLevelblank','clLevelblank')
//	oM.makeMenu('sidesub10','side1','RIS Logic&#8482; CS','products/pacsteleradiologyweb/rislogiccs.asp','',180,20)

	oM.makeMenu('sidesub11','side1','FUSION RIS/PACS','products/pacsteleradiologyweb/fusion_rispacs.asp','',180,20)
//		oM.makeMenu('sidesub110','sidesub11','Base Software','products/pacsteleradiologyweb/fusion_rispacs.asp','',280,20)
//		oM.makeMenu('sidesub111','sidesub11','RIS Logic Module','products/pacsteleradiologyweb/rislogicmodule.asp','',280,20)
//		oM.makeMenu('sidesub112','sidesub11','HIS/RIS Module','products/pacsteleradiologyweb/module_his_ris.asp','',280,20)
//		oM.makeMenu('sidesub113','sidesub11','Patient Registration and Order Entry Module','products/pacsteleradiologyweb/patientregistration.asp','',280,20)			
//		oM.makeMenu('sidesub114','sidesub11','Archiving Module','products/pacsteleradiologyweb/module_archiving.asp','',280,20)
//		oM.makeMenu('sidesub115','sidesub11','Image Streaming and Web Distribution Module','products/pacsteleradiologyweb/module_imageandweb.asp','',280,20)
//		oM.makeMenu('sidesub116','sidesub11','Visualization Module','products/pacsteleradiologyweb/module_visualization.asp','',280,20)
//		oM.makeMenu('sidesub117','sidesub11','Radiologist Workspace Module','products/pacsteleradiologyweb/module_workspace.asp','',280,20)			

	oM.makeMenu('sidesub12','side1','FUSION RIS','products/pacsteleradiologyweb/fusion_ris.asp','',180,20)
		oM.makeMenu('sidesub120','sidesub12','RIS Logic Module','products/pacsteleradiologyweb/fusion_ris.asp','',130,20)

	oM.makeMenu('sidesub13','side1','FUSION PACS','products/pacsteleradiologyweb/fusion_pacs.asp','',180,20)
		oM.makeMenu('sidesub130','sidesub13','Base Software','products/pacsteleradiologyweb/fusion_pacs.asp','',280,20)
		oM.makeMenu('sidesub131','sidesub13','HIS/RIS Module','products/pacsteleradiologyweb/module_his_ris.asp','',280,20)
		oM.makeMenu('sidesub132','sidesub13','Patient Registration and Order Entry Module','products/pacsteleradiologyweb/patientregistration.asp','',280,20)			
		oM.makeMenu('sidesub133','sidesub13','Archiving Module','products/pacsteleradiologyweb/module_archiving.asp','',280,20)
		oM.makeMenu('sidesub134','sidesub13','Image Streaming and Web Distribution Module','products/pacsteleradiologyweb/module_imageandweb.asp','',280,20)
		oM.makeMenu('sidesub135','sidesub13','Visualization Module','products/pacsteleradiologyweb/module_visualization.asp','',280,20)
		oM.makeMenu('sidesub136','sidesub13','Radiologist Workspace Module','products/pacsteleradiologyweb/module_workspace.asp','',280,20)			
//
	oM.makeMenu('sidesub14','side1','eFilm Workstation&#8482; Software','products/efilmworkstation.asp','',180,20)
		oM.makeMenu('sidesub140','sidesub14','Product Info','products/efilmworkstation.asp','',180,20)
		oM.makeMenu('sidesub141','sidesub14','Trial Download','estore/licenseagreement.asp?ID=1','',180,20)
		oM.makeMenu('sidesub142','sidesub14','Purchase','estore/software.asp','',180,20)
		oM.makeMenu('sidesub143','sidesub14','Academic Licensing','products/pacsteleradiologyweb/module_visualization_academic.asp','',180,20)
		oM.makeMenu('sidesub144','sidesub14','Add-ons','estore/software.asp','',180,20)
			oM.makeMenu('sidesub1440','sidesub144','eFilm Ortho','products/pacsteleradiologyweb/module_visualization_efilmortho.asp','',120,20)
			oM.makeMenu('sidesub1441','sidesub144','eFilm Scan','products/pacsteleradiologyweb/module_visualization_efilmscan.asp','',120,20)
			oM.makeMenu('sidesub1442','sidesub144','HIS/RIS SDK 0.5','products/dicomhisrisconnectivity/index.asp','',120,20)
			oM.makeMenu('sidesub1443','sidesub144','eFilm Image SDK','products/pacsteleradiologyweb/module_visualization_efilmimage.asp','',120,20)
		oM.makeMenu('sidesub145','sidesub14','Tech Specs','products/pacsteleradiologyweb/module_visualization_techspecs.asp','',180,20)
		oM.makeMenu('sidesub146','sidesub14','DICOM Conformance (PDF)','pdf/dcs_efilmworkstation_1-9.pdf','',180,20)
		oM.makeMenu('sidesub147','sidesub14','Online Support','helpcenter/onlinesupport.asp','',180,20)
/*		oM.makeMenu('sidesub144','sidesub14','Discussion','forum/displayForum.asp?forum=Workstation','',180,20)																
		oM.makeMenu('sidesub145','sidesub14','Help File (CHM)','downloads/help_efilmworkstation.chm','',180,20)
		oM.makeMenu('sidesub147','sidesub14','Datasheet (PDF)','pdf/data_efilmworkstation.pdf','',180,20)
		oM.makeMenu('sidesub148','sidesub14','Version Log (PDF)','pdf/versionlog_efilmworkstation.pdf','',180,20)
		oM.makeMenu('sidesub150','sidesub14','Known Issues','products/pacsteleradiologyweb/module_visualization_knownissues.asp','',180,20)
*/
/*oM.makeMenu('side1','','','#void','',1,1,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sidesub10','side1','FUSION Server&#8482; Base Software','products/pacsteleradiologyweb/fusionserver.asp','',280,20)
	oM.makeMenu('sidesub11','side1','HIS/RIS Module','products/pacsteleradiologyweb/module_his_ris.asp','',280,20)
					oM.makeMenu('sidesub110','sidesub11','HIS/RIS SDK 0.5','products/dicomhisrisconnectivity/index.asp','',150,20)
	oM.makeMenu('sidesub12','side1','Archiving Module','products/pacsteleradiologyweb/module_archiving.asp','',280,20)
	oM.makeMenu('sidesub13','side1','Image Serving and Web Distribution Module','products/pacsteleradiologyweb/module_imageandweb.asp','',280,20)
	oM.makeMenu('sidesub14','side1','Visualization Module (eFilm Workstation&#8482;) ','products/pacsteleradiologyweb/module_visualization.asp','',280,20)
				oM.makeMenu('sidesub140','sidesub14','Download','estore/licenseagreement.asp?ID=1','',180,20)
				oM.makeMenu('sidesub141','sidesub14','Purchase','estore/software.asp','',180,20)
				oM.makeMenu('sidesub142','sidesub14','Features','products/pacsteleradiologyweb/module_visualization_features.asp','',180,20)
				oM.makeMenu('sidesub143','sidesub14','Tech Specs','products/pacsteleradiologyweb/module_visualization_techspecs.asp','',180,20)
				oM.makeMenu('sidesub144','sidesub14','Discussion','forum/displayForum.asp?forum=Workstation','',180,20)																
				oM.makeMenu('sidesub145','sidesub14','Help File (CHM)','downloads/help_efilmworkstation.chm','',180,20)
				oM.makeMenu('sidesub146','sidesub14','DICOM Conformance (PDF)','pdf/dcs_efilmworkstation.pdf','',180,20)
				oM.makeMenu('sidesub147','sidesub14','Datasheet (PDF)','pdf/data_efilmworkstation.pdf','',180,20)
				oM.makeMenu('sidesub148','sidesub14','Version Log (PDF)','pdf/versionlog_efilmworkstation.pdf','',180,20)
				oM.makeMenu('sidesub149','sidesub14','Support Downloads','helpcenter/onlinesupport.asp','',180,20)
				oM.makeMenu('sidesub150','sidesub14','Known Issues','products/pacsteleradiologyweb/module_visualization_knownissues.asp','',180,20)
				oM.makeMenu('sidesub151','sidesub14','eFilm Scan','products/pacsteleradiologyweb/module_visualization_efilmscan.asp','',180,20)
				oM.makeMenu('sidesub152','sidesub14','eFilm Image SDK','products/pacsteleradiologyweb/module_visualization_efilmimage.asp','',180,20)													
	oM.makeMenu('sidesub15','side1','Radiologist Workspace Module','products/pacsteleradiologyweb/module_workspace.asp','',280,20)			
	oM.makeMenu('sidesub16','side1','Patient Registration and Order Entry Module','products/pacsteleradiologyweb/module_workspace.asp','',280,20)			
*/
// DIOCOM HIS/RIS
oM.makeMenu('side2','','','#void','',1,1,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sidesub20','side2','DICOM Conversion','products/dicomhisrisconnectivity/index.asp','',180,20)
			oM.makeMenu('sidesub200','sidesub20','Basic','products/dicomhisrisconnectivity/index.asp','',150,20)
			oM.makeMenu('sidesub201','sidesub20','Extended','products/dicomhisrisconnectivity/extendedconversion.asp','',150,20)
	oM.makeMenu('sidesub22','side2','HL7 Brokering','products/dicomhisrisconnectivity/hl7brokering.asp','',180,20)
	oM.makeMenu('sidesub23','side2','Print Serving','products/dicomhisrisconnectivity/printserving.asp','',180,20)
	oM.makeMenu('sidesub24','side2','Film Scanning','products/dicomhisrisconnectivity/filmscanning.asp','',180,20)
	oM.makeMenu('sidesub25','side2','Conformance Statements','products/dicomhisrisconnectivity/conformancestatements.asp','',180,20)

// DEVELOPMENT TOOLS	
oM.makeMenu('side3','','','#void','',1,1,'','','clLevelblank','clLevelblank')
	oM.makeMenu('sidesub30','side3','Toolkits','products/developmenttools/toolkits.asp','',150,20)
	oM.makeMenu('sidesub31','side3','Technical Platforms','products/developmenttools/technicalplatforms.asp','',150,20)	
	oM.makeMenu('sidesub32','side3','Image Streaming','products/developmenttools/imagestreaming.asp','',150,20)
	oM.makeMenu('sidesub33','side3','Protocol Monitor','products/developmenttools/protocolmonitors.asp','',150,20)		

// PROFESSIONAL SERVICES
oM.makeMenu('side4','','','#void','',1,1,'','','clLevelblank','clLevelblank')
//	oM.makeMenu('sidesub40','side4','DICOM Consulting','products/professionalservices/workflowconsulting/consulting.asp','',190,20)
	oM.makeMenu('sidesub40','side4','Project Management','products/professionalservices/workflowconsulting/projectmanagement.asp','',190,20)
	oM.makeMenu('sidesub41','side4','Training / Workshops','products/professionalservices/training/workshopindividuals.asp','',190,20)
		oM.makeMenu('sidesub410','sidesub41','Workshop Suite Courses','products/professionalservices/training/workshopsuites.asp','',160,20)
		oM.makeMenu('sidesub411','sidesub41','Individual Workshop Courses','products/professionalservices/training/workshopindividuals.asp','',160,20)
		oM.makeMenu('sidesub412','sidesub41','Instructors','products/professionalservices/training/instructors.asp','',160,20)
	//oM.makeMenu('sidesub43','side4','DICOM Testing and Design','products/professionalservices/dicomtesting/testinganddesign.asp','',190,20)					

	

//var avail="190+((cmpage.x2-235)/7)";
//oM.menuPlacement=new Array(192,avail+"-11",avail+"*2-8",avail+"*3-12",avail+"*4-7",avail+"*5-9",avail+"*6+5")
//oM.menuPlacement=new Array(220,300,390,510,610)
oM.construct();
//Extra test code to customize the CoolMenus.

//Hiding items
var xx,yy
oM.m["top0"].b.hideIt();
oM.m["top1"].b.hideIt();
oM.m["top2"].b.hideIt();
oM.m["top3"].b.hideIt();
oM.m["top4"].b.hideIt();
oM.m["top5"].b.hideIt();
oM.m["top6"].b.hideIt();
oM.m["side0"].b.hideIt();
oM.m["side1"].b.hideIt();
oM.m["side2"].b.hideIt();
oM.m["side3"].b.hideIt();
oM.m["side4"].b.hideIt();

function getCoords(e){
xx=(bw.ns4 || bw.ns6)?e.pageX:event.x||event.clientX
yy=(bw.ns4 || bw.ns6)?e.pageY:event.y||event.clientY
}

//Capturing onmousemove event
if(bw.ns4) {document.captureEvents(Event.MOUSEMOVE)
document.onmousemove=getCoords;}