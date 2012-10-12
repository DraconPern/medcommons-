<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML>
<HEAD>
<TITLE>Query Page</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
<META content=none name=robots>
<SCRIPT language=JavaScript src="form_functions.js"></SCRIPT>
<SCRIPT language=JavaScript src="WADO.js"></SCRIPT>

<%@ page import="java.util.*"  %>

<%@ page import="java.io.*"  %>

<%@ page import="net.medcommons.router.services.dicom.util.*"  %>

<%@ page import="net.medcommons.router.configuration.*" %>

<%

   String dest =
		(String) Configuration.getInstance().getConfiguredValue(
			"net.medcommons.dicom.directory");
   File rootDirectory = new File(dest);
   if (!rootDirectory.exists())
   	throw new java.io.FileNotFoundException(dest);
   	
   StudyMetadataManager studyMetadata = new StudyMetadataManager();
   studyMetadata.scan(rootDirectory);
   
   String[] guids = studyMetadata.guidQuery();
   int numberOfStudies;
   if (guids==null)
   	numberOfStudies = 0;
   else
   	numberOfStudies = guids.length;
   System.out.println("Root directory is " + rootDirectory);
   System.out.println("guids is " + guids);
%>


<STYLE type=text/css>.FolderTab {
	BORDER-RIGHT: #000000 1px solid; BORDER-TOP: #000000 1px solid; BORDER-LEFT-COLOR: #000000; BORDER-BOTTOM-COLOR: #000000; HEIGHT: 25px
}
.FolderTop {
	BORDER-LEFT-COLOR: #000000; BORDER-TOP-COLOR: #000000; BORDER-BOTTOM: #000000 1px solid; BORDER-RIGHT-COLOR: #000000
}
.FolderLeft {
	BORDER-BOTTOM-COLOR: #000000; BORDER-LEFT: #000000 1px solid; BORDER-TOP-COLOR: #000000; BORDER-RIGHT-COLOR: #000000
}
.FolderHeader {
	FONT-WEIGHT: bold; FONT-SIZE: 12px; TEXT-INDENT: 4px; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.TrackingField {
	BORDER-RIGHT: #ccccff 1px solid; BORDER-TOP: #ccccff 1px solid; LEFT: 12px; BORDER-LEFT: #ccccff 1px solid; WIDTH: 270px; BORDER-BOTTOM: #ccccff 1px solid; POSITION: relative; TOP: 14px; HEIGHT: 25px; BACKGROUND-COLOR: #ffffff
}
.AccountFieldLeft {
	LEFT: 5px; WIDTH: 110px; POSITION: relative; TOP: 10px; HEIGHT: 25px; BACKGROUND-COLOR: #e1e3f0
}
.AccountFieldLarge {
	LEFT: 5px; WIDTH: 270px; POSITION: relative; TOP: 10px; HEIGHT: 25px; BACKGROUND-COLOR: #e1e3f0
}
.AccountFieldSmall {
	LEFT: 0px; WIDTH: 40px; POSITION: relative; TOP: 10px; HEIGHT: 25px; BACKGROUND-COLOR: #e1e3f0
}
.AccountFieldRight {
	LEFT: 0px; WIDTH: 96px; POSITION: relative; TOP: 10px; HEIGHT: 25px; BACKGROUND-COLOR: #e1e3f0
}
.FieldHeader {
	FONT-WEIGHT: bold; FONT-SIZE: 10px; COLOR: #999999; TEXT-INDENT: 5px; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.CreditCardField {
	LEFT: 5px; WIDTH: 150px; POSITION: relative; TOP: 0px; HEIGHT: 25px; BACKGROUND-COLOR: #e1e3f0
}
.CreditCardFieldRight {
	LEFT: 5px; WIDTH: 100px; POSITION: relative; TOP: 0px; HEIGHT: 25px; BACKGROUND-COLOR: #e1e3f0
}
.HIPAAblock {
	FONT-SIZE: 9px; LEFT: 5px; OVERFLOW: hidden; WIDTH: 270px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; POSITION: relative; TOP: 5px; HEIGHT: 40px; BACKGROUND-COLOR: #e1e3f0
}
.RightColHeader {
	FONT-WEIGHT: bold; FONT-SIZE: 16px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.RightFieldHeader {
	FONT-WEIGHT: bold; FONT-SIZE: 12px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.RightColTextField {
	BORDER-RIGHT: medium none; BORDER-TOP: medium none; FONT-SIZE: 12px; BACKGROUND-IMAGE: url(/mc/images/fieldbackg_rightscreen.gif); OVERFLOW: hidden; BORDER-LEFT: medium none; WIDTH: 320px; LINE-HEIGHT: 30px; BORDER-BOTTOM: medium none; FONT-FAMILY: "Courier New", Courier, mono; POSITION: relative; TOP: 5px; HEIGHT: 150px
}
.RightColTextSmall {
	BORDER-RIGHT: medium none; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 320px; BORDER-BOTTOM: medium none; POSITION: relative; HEIGHT: 30px; BACKGROUND-COLOR: #ffffff
}
BODY {
	PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; MARGIN: 0px; OVERFLOW: hidden; PADDING-TOP: 0px; BACKGROUND-COLOR: #dcdfee
}
.ThumbLabel {
	PADDING-LEFT: 5px; FONT-WEIGHT: bold; FONT-SIZE: 12px; COLOR: #ffffff; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.ImageCount {
	FONT-WEIGHT: normal; FONT-SIZE: 12px; COLOR: #cccccc; TEXT-INDENT: 5px; FONT-STYLE: normal; FONT-FAMILY: Arial, Helvetica, sans-serif; TEXT-ALIGN: right
}
.FooterCell {
	POSITION: relative; HEIGHT: 50px
}
</STYLE>
</HEAD>
<BODY>
<h3>Total studies on Router: <%=numberOfStudies%> </h3>
     <table>
     <TR>
     	<TD>
     	Patient Name
     	</TD>
     	<TD>
     	Patient ID
     	</TD>
     	<TD>
     	Study Description
     	</TD>
     	<TD>
     	GUID
     	</TD>
     </TR>
     
     <% 
     
     for (int i=0;i<numberOfStudies;i++){
     	MCStudy study = studyMetadata.getStudy(guids[i]);
     	
     %>
     <TR>
     	<TD>
     	<%=study.PatientName %>
     	</TD>
     	<TD>
     	<%=study.PatientID %>
     	</TD>
     	<TD>
     	<%=study.StudyDescription %>
     	</TD>
     	<TD>
     	<a href="/router/WADOViewer.jsp?guid=<%=study.mcGUID%>" >
     	<%=study.mcGUID%>
     	</a>
     	</TD>
     </TR>
     <%
     }
     %>
     </table>
</BODY></HTML>
