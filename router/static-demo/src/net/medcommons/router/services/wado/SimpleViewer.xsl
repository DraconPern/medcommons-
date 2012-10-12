<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform">
	<xsl:output method="html"
		doctype-public="-//W3C//DTD XHTML 1.0 Transitional//EN"
		doctype-system="http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd"/>



<xsl:template match="/">
<html>
<head>
<title>MedCommons WADO Viewer</title>

<SCRIPT LANGUAGE="JavaSCRIPT">
<![CDATA[

var p = null;

var PatientName ="";
var PatientID="";

var StudyInstanceUID="";
var StudyDate="";
var StudyTime="";
var StudyDescription="";


var SeriesDescription="";
var SeriesInstanceUID=""; 
var Modality="";
var SeriesNumber="";

var InstanceNumber="";
var SOPInstanceUID = "";
var FileReferenceID="";

var DirectoryRecordType="";

var currentImage = -1;
var currentSeries = -1;

var winWidth = 0;
if(typeof window.innerWidth != "undefined") {
	 winWidth = window.innerWidth; 
	 }
else if(typeof document.documentElement.offsetWidth != "undefined") { 
	winWidth = document.documentElement.offsetWidth; 
	}
else { 
	winWidth = document.body.offsetWidth;
 }
 
 var winHeight = 0;
if(typeof window.innerHeight != "undefined") {
	 winHeight = window.innerHeight; 
	 }
else if(typeof document.documentElement.offsetHeight != "undefined") { 
	winHeight = document.documentElement.offsetHeight; 
	}
else { 
	winHeight = document.body.offsetHeight;
 }
 

function Patient(PatientName, PatientID){
	this.PatientName =PatientName;
	this.PatientID = PatientID;
	this.StudiesArray = new Array();
};
	
	
function Study(StudyDescription, StudyInstanceUID, StudyDate, StudyTime ){
	this.StudyDescription = StudyDescription;
	this.StudyInstanceUID = StudyInstanceUID;
	this.StudyDate = StudyDate;
	this.StudyTime = StudyTime;
	this.SeriesArray=new Array();
};

function Series(SeriesDescription, SeriesInstanceUID, Modality, SeriesNumber){
	this.SeriesDescription=SeriesDescription;
	this.SeriesInstanceUID=SeriesInstanceUID;
	this.Modality=Modality;
	this.SeriesNumber = parseInt(SeriesNumber);
	this.InstanceArray = new Array();
}

function Instance(SOPInstanceUID, InstanceNumber, FileReferenceID){
	this.InstanceNumber= parseInt(InstanceNumber);
	this.SOPInstanceUID = SOPInstanceUID;
	this.FileReferenceID=FileReferenceID;
	}
	

/**
 * Returns the array offset of a series specified by SeriesInstanceUID
 */
function getSeries(SeriesInstanceUID){
	var series = null;
	var tempSeries = null;
	for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
		tempSeries = p.StudiesArray[0].SeriesArray[i];
		if (SeriesInstanceUID == tempSeries.SeriesInstanceUID){
			series=tempSeries;
			break;
			}
	}
	if (series == null){
		var msg = SeriesInstanceUID;
		msg += " not in ";
		for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
			msg += "\n";
			msg += p.StudiesArray[0].SeriesArray[i].SeriesInstanceUID;
		}
		alert(msg);
	}
	return(series);
}
function createArrayElement(){
	var newObj;

	if (DirectoryRecordType == "IMAGE "){
		newObj= new Instance(SOPInstanceUID, InstanceNumber, FileReferenceID);
		var imageExists = false;
		// Get the series containing the current SeriesInstanceUID
		var series = getSeries(SeriesInstanceUID);
		 
		if (series == null){
		/*
			alert("Series doesn't exist " + SeriesInstanceUID + "\n" +
			"number of series:" + p.StudiesArray[0].SeriesArray.length);
			*/
			;
		}
		
		else{
			series.InstanceArray[series.InstanceArray.length] = newObj;	
		}
		
		
		
	}

	else if (DirectoryRecordType == "SERIES"){
		newObj = new Series(SeriesDescription, SeriesInstanceUID, Modality, SeriesNumber);

		var seriesExists = false;

	
		for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
			if (p.StudiesArray[0].SeriesArray[i].SeriesInstanceUID == SeriesInstanceUID){
				seriesExists = true;
			}
		}

		if (seriesExists == false){
			p.StudiesArray[0].SeriesArray[p.StudiesArray[0].SeriesArray.length] = newObj;
			//alert("Added new series " + newObj.SeriesDescription);
		}
	}
	else if (DirectoryRecordType == "STUDY "){
		newObj=  new Study(StudyDescription, StudyInstanceUID, StudyDate, StudyTime );
		p.StudiesArray[0] = newObj;
		
	}
	else if (DirectoryRecordType == "PATIENT "){
		p = new Patient(PatientName, PatientID);
		
	
	}
	else 
		alert("Unknown DirectoryType: " + DirectoryRecordType);
}
/*
Controls.
Zoom/Pan
Window/Level
Stack mode

Overlay on/off
Reset

*/
/*
function createToolPalette(){
	var toolPallete = '\n<table name="tools">';
	toolPallete+="\n <tr> <td> <img
}
*/
function createSeriesControl(){
	parent.seriesControl.document.open();
	var seriesControl ="";

	seriesControl += "<body BGCOLOR=\"#000000\" >";
	seriesControl += "<table name='SeriesControl'>";
	var portrait = winHeight>winWidth;
	if (!portrait)
		seriesControl +="<tr>";
	
	for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
		var series = p.StudiesArray[0].SeriesArray[i];
		var thumbnailInstance= series.InstanceArray[(series.InstanceArray.length/2)];
		
		if (portrait)
			seriesControl+="\n<tr>";
		seriesControl +="\n<td>";
		seriesControl +='<img name=';
		seriesControl +='"series' + i +'"';
		seriesControl +=' border=0 width="128" height="128" src=';
		seriesControl +='"/router/WADO?studyUID=';
		seriesControl +=p.StudiesArray[0].StudyInstanceUID;
		seriesControl +='&rows=128&columns=128&fname=';
		seriesControl +=thumbnailInstance.FileReferenceID;
		seriesControl +='" ';
		seriesControl +=' alt="';
		seriesControl +=series.SeriesDescription;
		seriesControl +='" onClick="parent.control.displaySelectedSeries(';
		seriesControl +=i;
		seriesControl +='); "';
		seriesControl +=' >';
		seriesControl +="</td>";
		if (portrait){
			seriesControl+="</tr>\n";
			}
	}
	
		
	if (portrait)
		seriesControl +="<tr><td>";
	else
		seriesControl +="<td>";
	seriesControl +='\n<form name="seriesSelection">';
	seriesControl +='<input type="button" value="Previous Image" onclick="parent.control.displayPreviousImage()"/>';
	seriesControl +='<input type="button" value="Next Image" onclick="parent.control.displayNextImage()"/>';
	seriesControl +='NearestNeighbor<input type="checkbox" name="Fast" CHECKED>';
	seriesControl+="";
	seriesControl += '</form>'	
	if (portrait)
		seriesControl +="</tr></td>";
	else
		seriesControl +="</td>";
		
	if (!portrait) 
		seriesControl +="\n</tr>";
	
	seriesControl +="</table>";
	seriesControl +="</body>";
	//seriesControl +="</html>";
	parent.seriesControl.document.write(seriesControl);
	//alert(seriesControl);
	parent.seriesControl.document.close();
}	



function displaySelectedSeries(n){
	selectSeries(n);
	displayCurrentImage();
}

function numberOfSeries(){
	return(p.StudiesArray[0].SeriesArray.length);
}
function numberOfImageInSeries(seriesNumber){
	return(p.StudiesArray[0].SeriesArray[seriesNumber].InstanceArray.length);
}
function displayPreviousImage(){
	currentImage--;
	if (currentImage < 0){
		currentSeries--;
		var nSeries = numberOfSeries();
		if (currentSeries < 0){
			currentSeries = nSeries -1;
		}
		currentImage = numberOfImageInSeries(currentSeries)-1;
	}

	displayCurrentImage();
}
function displayNextImage(){
	currentImage++;
	if (currentImage < 0) {
		currentImage=0;
		}
	else if (currentImage >= numberOfImageInSeries(currentSeries)){
		var nSeries = numberOfSeries();
		currentSeries++;
		if (currentSeries>=nSeries){
			currentSeries = 0;
			currentImage = 0;
			}
		else{
			currentImage = 0;
		}	
		}
	displayCurrentImage();
}

function handleMousewheel(n){
	//alert ("handleMousewheel " + n);
	if (n>0)
		displayNextImage();
	else if (n<0)
		displayPreviousImage();
}
function selectSeries(n){
	var previousSeries = currentSeries;
	currentSeries = n;
	//eval("parent.seriesControl.document.series" + n + ".border = 5;");
	currentImage = 0;
	
}
function finalizeArrays(){
	p.StudiesArray[0].SeriesArray.sort(function (a,b){
		return (a.SeriesNumber - b.SeriesNumber);
	});
	for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
		p.StudiesArray[0].SeriesArray[i].InstanceArray.sort(function(a,b){
			return(a.InstanceNumber - b.InstanceNumber);
		});
	}
	//displayStudyArrays();
	createSeriesControl();
	selectSeries(0);
	currentImage = 0;
	displayCurrentImage();
}
function displayStudyArrays(){
	
	var msg =p.StudiesArray[0].StudyDescription + "\n";
	msg += "Series in study:";
	msg+= p.StudiesArray[0].SeriesArray.length + ".";
	for (i=0;i<p.StudiesArray[0].SeriesArray.length;i++){
		msg+="\n===";
		msg+=p.StudiesArray[0].SeriesArray[i].SeriesDescription;
		msg+="\n images: " ;
		msg+=p.StudiesArray[0].SeriesArray[i].InstanceArray.length;
		msg+= "\n";
		msg+=p.StudiesArray[0].SeriesArray[i].SeriesInstanceUID;
		
	}
	msg+= "\n width=" +winWidth;
	msg+= "\n height=" +winHeight;
	alert (msg);
}

var currentImage = -1;
var currentSeries = -1;

function displayCurrentImage(){
		
		
		if (parent.images.isImageLoaded() == false){
			window.status="Skipping image";
			return;	
		}
		window.status="Series:" + currentSeries + ", image:" + currentImage + " / " + numberOfImageInSeries(currentSeries);
		var maxRows = parent.images.winHeight-20; // Adjustment for scrollbars. Need a more precise mechanism.
		var maxColumns = parent.images.winWidth -20;
	 	var image= p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[currentImage];
	 	if (image ==null){
	 		alert("Series " + currentSeries + " image " + currentImage + " is null");
	 		return;
	 		}
	 	if (image.FileReferenceID ==null){
	 		alert("Series " + currentSeries + " image " + currentImage + " FileReferenceID is null");
	 		return;
	 		}
	 	var imageURL = "/router/WADO?studyUID=";
	 	imageURL+= StudyInstanceUID; 
	 	imageURL+= "&fname=";
	 	imageURL+= image.FileReferenceID;
	 	imageURL+="&annotation=patient,technique";
	 	imageURL+="&maxRows=";
	 	imageURL+=maxRows;
	 	imageURL+="&maxColumns=";
	 	imageURL+=maxColumns;
	 	if(parent.seriesControl.document.seriesSelection.Fast.checked==true)
	 		imageURL+="&interpolation=FAST";

	 	parent.images.setImage(imageURL);
	 	

	 		
	 
	
}

function handleZoomRegion(startX, startY, endX, endY, imageWidth, imageHeight){
	//alert("zoom region:" + startX + "," + startY + "," + endX + "," + endY + ",w=" + imageWidth	+ " h=" + imageHeight);
		var x1 = (1.0 * startX)/(1.0 * imageWidth);
		var y1 = (1.0 * startY)/(1.0 * imageHeight);
		var x2 = (1.0 *  endX)/(1.0 * imageWidth);
		var y2 = (1.0 * endY)/ (1.0 * imageHeight);
		
		
		if (x1 > x2){
			var temp;
			temp = x1; x1=x2; x2=temp;
		}
		if (y1 > y2){
			var temp;
			temp = y1; y1=y2; y2=temp;
		}
		/*
		// Set min zoom size (10:1)
		if ((x2-x1) < .1){
			if (x1 <.1)
				x2 = x1 + .1;
			else if (x2>.9)
				x1 = x2 - .1;
		}
			
		if ((y2-y1) < .1){
			if (y1 <.1)
				y2 = y1 + .1;
			else if (y2>.9)
				y1 = y2 - .1;
		}
		// Shouldn't happen - but it appears that drag event can let loose outside of 
		// image region.
		if (x2 > 1) x2=1.0;
		if (y2 > 1) y2 = 1.0;
			*/
	// Need to refactor:
		
		if (parent.images.isImageLoaded() == false){
			window.status="Skipping image";
			return;	
		}
		window.status="Series:" + currentSeries + ", image:" + currentImage + " / " + numberOfImageInSeries(currentSeries);
		var maxRows = parent.images.winHeight-20; // Adjustment for scrollbars. Need a more precise mechanism.
		var maxColumns = parent.images.winWidth -20;
	 	var image= p.StudiesArray[0].SeriesArray[currentSeries].InstanceArray[currentImage];
	 	if (image ==null){
	 		alert("Series " + currentSeries + " image " + currentImage + " is null");
	 		return;
	 		}
	 	if (image.FileReferenceID ==null){
	 		alert("Series " + currentSeries + " image " + currentImage + " FileReferenceID is null");
	 		return;
	 		}
	 	var imageURL = "/router/WADO?studyUID=";
	 	imageURL+= StudyInstanceUID; 
	 	imageURL+= "&fname=";
	 	imageURL+= image.FileReferenceID;
	 	imageURL+="&annotation=patient,technique";
	 	imageURL+="&maxRows=";
	 	imageURL+=maxRows;
	 	imageURL+="&maxColumns=";
	 	imageURL+=maxColumns;
	 	imageURL+="&region=";
	 	imageURL+=x1; imageURL+=",";
	 	imageURL+=y1; imageURL+=",";
	 	imageURL+=x2; imageURL+=",";
	 	imageURL+=y2; 
	 	if(parent.seriesControl.document.seriesSelection.Fast.checked==true)
	 		imageURL+="&interpolation=FAST";
	 	parent.images.setImage(imageURL);

}
 
]]>
<xsl:apply-templates select="dicomfile"/>
</SCRIPT>
</head>
<body onLoad="finalizeArrays();" BGCOLOR="#000000">
</body>
</html>

</xsl:template>


<xsl:template match="dicomfile">
		<xsl:apply-templates select="dataset"/>
</xsl:template>
<!--
	<xsl:template match="dataset">	
        
                <xsl:apply-templates select="elm"/>
       
	</xsl:template>
	-->
    
    <xsl:template match="seq">
               
                <xsl:for-each select= "item">
                 	<xsl:apply-templates select="elm"/>
createArrayElement(); //******
                </xsl:for-each>


    </xsl:template>

    <!-- Modality -->
	<xsl:template match="elm[@tag='00080060']">
Modality= &quot;<xsl:value-of select="val/@data"/>&quot;;
    </xsl:template>
    
    <!-- Study InstanceUID -->
	<xsl:template match="elm[@tag='0020000d']">
StudyInstanceUID= &quot;<xsl:value-of select="val/@data"/>&quot;;
    </xsl:template>
     
     <!-- Patient Name -->
	<xsl:template name="patientName" match="elm[@tag='00100010']">
PatientName=&quot;<xsl:value-of select="val/@data"/>&quot;;
    </xsl:template>
    
         <!-- Patient ID -->
	<xsl:template name="patientID" match="elm[@tag='00100020']">
PatientID=&quot;<xsl:value-of select="val/@data"/>&quot;;
    </xsl:template>
    
    <!-- Series Description -->
<xsl:template match="elm[@tag='0008103e']">
SeriesDescription=&quot;<xsl:value-of select="val/@data"/>&quot;;
</xsl:template>

    <!-- Series Number -->
<xsl:template match="elm[@tag='00200011']">
SeriesNumber=&quot;<xsl:value-of select="val/@data"/>&quot;;
</xsl:template>


 <!-- Study Description -->
<xsl:template match="elm[@tag='00081030']">
StudyDescription=&quot;<xsl:value-of select="val/@data"/>&quot;;
</xsl:template>

   <!-- Series InstanceUID -->
	<xsl:template match="elm[@tag='0020000e']">
SeriesInstanceUID=&quot;<xsl:value-of select="val/@data"/>&quot;;
    </xsl:template>
    
     <!-- Directory record type -->
	<xsl:template match="elm[@tag='00041430']">
DirectoryRecordType=&quot;<xsl:value-of select="val/@data"/>&quot;;
    </xsl:template>
    
    <!-- File reference ID -->
<xsl:template match="elm[@tag='00041500']">
FileReferenceID=&quot;<xsl:value-of select="translate(val/@data, '\','/')"/>&quot;;
</xsl:template>
    
    <!-- Instance Number -->
<xsl:template match="elm[@tag='00200013']">
InstanceNumber=&quot;<xsl:value-of select="val/@data"/>&quot;;
</xsl:template>

    <!-- Instance UID -->
    <!--
<xsl:template match="elm[@tag='00041511']">
RefSOPInstanceUID=&quot;<xsl:value-of select="val/@data"/>&quot;;
</xsl:template>
-->
    <!-- ReferencedImageSequence -->

<xsl:template match="elm[@tag='00081140']">
  var a = 0;
</xsl:template>




</xsl:stylesheet>

