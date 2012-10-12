
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">
<HTML><HEAD><TITLE>MedCommons Order Form</TITLE>
<META http-equiv=Content-Type content="text/html; charset=iso-8859-1">
<META content=none name=robots>
<link href="WADO.css" rel="stylesheet" type="text/css">
<SCRIPT language=JavaScript src="form_functions.js"></SCRIPT>
<SCRIPT language=JavaScript src="WADO.js"></SCRIPT>

<%@ page import="java.util.*"  %>
<%@ page import="java.io.*"  %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%@ page import="net.medcommons.router.services.transfer.*" %>

<%@ include file="OrderStudyFragment.htmlf" %>

<SCRIPT language=JavaScript>
FormType = OrderFormType;

function orderThankYou(tracking, guid){
	alert("Your order has been accepted.");
    displaySelectedSeries(0);
}

function printOrder(tracking, guid){
	alert("Your order has been sent to your printer.");
}

version='<%=net.medcommons.Version.getVersionString()%>';
buildDate='<%=net.medcommons.Version.getBuildTime()%>';

currentThumb=-1;
<%if(!"new".equals(request.getParameter("ordertype"))) { %>
  currentThumb=0;
<%}%>

</SCRIPT>

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
	FONT-SIZE: 9px; LEFT: 5px; WIDTH: 270px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif; POSITION: relative; TOP: 5px; HEIGHT: 40px; BACKGROUND-COLOR: #e1e3f0
}
.RightColHeader {
	FONT-WEIGHT: bold; FONT-SIZE: 16px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.RightFieldHeader {
	FONT-WEIGHT: bold; FONT-SIZE: 12px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
}
.RightColTextField {
	BORDER-RIGHT: medium none; BORDER-TOP: medium none; FONT-SIZE: 12px; BACKGROUND-IMAGE: url(/mc/images/fieldbackg_rightscreen.gif); BORDER-LEFT: medium none; WIDTH: 320px; LINE-HEIGHT: 30px; BORDER-BOTTOM: medium none; FONT-FAMILY: "Courier New", Courier, mono; POSITION: relative; TOP: 5px; HEIGHT: 150px
}
.RightColTextSmall {
	BORDER-RIGHT: medium none; BORDER-TOP: medium none; BORDER-LEFT: medium none; WIDTH: 320px; BORDER-BOTTOM: medium none; POSITION: relative; HEIGHT: 30px; BACKGROUND-COLOR: #ffffff
}
BODY {
	PADDING-RIGHT: 0px; PADDING-LEFT: 0px; PADDING-BOTTOM: 0px; MARGIN: 0px; PADDING-TOP: 0px; BACKGROUND-COLOR: #dcdfee
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
.MenuHighlight {
	HEIGHT: 30px;FONT-WEIGHT: bold; COLOR: #0000FF; TEXT-INDENT: 5px; FONT-STYLE: normal; FONT-FAMILY: Arial, Helvetica, sans-serif; TEXT-ALIGN: right
}
.ButtonText {
	FONT-WEIGHT: bold; FONT-SIZE: 16px; COLOR: #000000; FONT-FAMILY: Arial, Helvetica, sans-serif
}

</STYLE>
</HEAD>
<BODY onload="Initialize();">
<TABLE height=800 cellSpacing=0 cellPadding=0 width=750 align=left border=0>
  <TBODY>
  <TR>
    <TD width=750 height=750>
      <TABLE height=750 cellSpacing=0 cellPadding=0 width=750 align=left 
      border=0>
        <TBODY>
        <TR vAlign=top align=left>
          <TD width=330 bgColor=#bfc4e1>
            <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
              <TBODY>
              <TR>
                <TD>
                  <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                    <TBODY>
                    <TR>
                      <TD width=20><IMG height=18 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD width=72><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=72></TD>
                      <TD width=218><IMG height=8 
                        src="formspacer_leftscreen.gif" 
                      width=218></TD>
                      <TD width=20><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD class=FolderTab width=72 bgColor=#e1e3f0>
                        <P class=FolderHeader>Tracking</P></TD>
                      <TD class=FolderTop width=218>&nbsp;</TD>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD rowSpan=2><IMG height=80 
                        src="formspacer_leftscreen.gif" width=1></TD>
                      <TD bgColor=#e1e3f0 colSpan=2 height=40>
                      	 <FORM name=form1>
							 <INPUT class=TrackingField name=tracking value="<%= tracking %>">
							 <INPUT TYPE="HIDDEN" NAME="guid" VALUE="<%= guid %>"/>
 						</FORM>
                  
                      </TD>
                      <TD class=FolderLeft rowSpan=2>&nbsp;</TD></TR>
                    <TR>
                      <TD bgColor=#e1e3f0 colSpan=2 height=40><IMG height=40 
                        src="medcommons_trackingfooter.gif" 
                        width=300></TD></TR></TBODY></TABLE></TD></TR>
              <TR>
                <TD>
                  <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                    <TBODY>
                    <TR>
                      <TD width=20><IMG height=18 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD width=72><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=72></TD>
                      <TD width=218><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=218></TD>
                      <TD width=20><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD class=FolderTab width=72 bgColor=#e1e3f0>
                        <P class=FolderHeader>Account</P></TD>
                      <TD class=FolderTop width=218>&nbsp;</TD>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD>&nbsp;</TD>
                      <TD bgColor=#e1e3f0 colSpan=2>
                        <TABLE cellSpacing=0 cellPadding=0 width=300 border=0>
                          <TBODY>
                          <TR>
                            <TD colSpan=3><INPUT class=AccountFieldLeft 
                              name=name value="<%= name %>"></TD></TR>
                          <TR>
                            <TD colSpan=3><IMG height=10 
                              src="formspacer_leftscreen.gif" 
                              width=1></TD></TR>
                          <TR>
                            <TD colSpan=3><TEXTAREA class=HIPAAblock name=address><%= address %></TEXTAREA></TD></TR>
                          <TR>
                            <TD width=120><INPUT class=AccountFieldLeft name=city value="<%= city %>"></TD>
                            <TD align=middle width=60><INPUT class=AccountFieldSmall name=state value="<%= state %>"> </TD>
                            <TD width=120><INPUT class=AccountFieldRight name=zip value="<%= zip %>"></TD></TR>
                              <TR><TD colspan="3">&nbsp;</TD></TR>
                           </TBODY>
                        </TABLE>
                      </TD>
                      <TD class=FolderLeft>&nbsp;</TD>
                  </TR>
                  </TBODY>
                  </TABLE>
              </TD>
              </TR>
              <TR>
                <TD>
                  <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                    <TBODY>
                    <TR>
                      <TD width=20><IMG height=18 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD width=72><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=72></TD>
                      <TD width=218><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=218></TD>
                      <TD width=20><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD class=FolderTab width=72 bgColor=#e1e3f0>
                        <P class=FolderHeader>Charges</P></TD>
                      <TD class=FolderTop width=218>&nbsp;</TD>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD>&nbsp;</TD>
                      <TD bgColor=#e1e3f0 colSpan=2>
                        <TABLE cellSpacing=0 cellPadding=0 width=300 border=0>
                          <TBODY>
                          <TR>
                            <TD vAlign=bottom width=170>
                              <P class=FieldHeader>card number</P></TD>
                            <TD width=130>
                              <P class=FieldHeader>exp</P></TD></TR>
                          <TR>
                            <TD width=170><INPUT class=CreditCardField 
                              name=cardnumber value="<%= cardnumber %>"></TD>
                            <TD width=130><INPUT class=CreditCardFieldRight 
                              name=expiration value="<%= expiration %>"></TD></TR>
                          <TR>
                            <TD class=FieldHeader colSpan=2><IMG height=5 
                              src="formspacer_leftscreen.gif" 
                              width=1></TD></TR>
                          <TR>
                            <TD class=FieldHeader width=170>
                              <P align=right>amount</P></TD>
                            <TD width=130><INPUT class=CreditCardFieldRight 
                              name=amount value="<%= amount %>"></TD></TR>
                          <TR>
                            <TD class=FieldHeader width=170>
                              <P align=right>tax</P></TD>
                            <TD width=130><INPUT class=CreditCardFieldRight 
                              name=tax value="<%= tax %>"></TD></TR>
                          <TR>
                            <TD class=FieldHeader width=170>
                              <P align=right>charge</P></TD>
                            <TD width=130><INPUT class=CreditCardFieldRight 
                              name=charge value="<%= charge %>"></TD></TR>
                              <TR><TD colspan="3">&nbsp;</TD></TR>
                              </TBODY></TABLE></TD>
                      <TD class=FolderLeft>&nbsp;</TD></TR></TBODY></TABLE></TD></TR>
              <TR>
                <TD>
                  <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                    <TBODY>
                    <TR>
                      <TD width=20><IMG height=18 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD width=72><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=72></TD>
                      <TD width=218><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=218></TD>
                      <TD width=20><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD></TR>
                    <TR>
                      <TD><IMG height=1 
                        src="formspacer_leftscreen.gif" 
                      width=20></TD>
                      <TD class=FolderTab width=72 bgColor=#e1e3f0> <P class=FolderHeader>HIPAA</P></TD>
                      <TD class=FolderTop width=218>&nbsp;</TD>
                      <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                    <TR>
                      <TD>&nbsp;</TD>
                      <TD bgColor=#e1e3f0 colSpan=2>
                        <TABLE cellSpacing=0 cellPadding=0 width=300 border=0>
                          <TBODY>
                          <TR>
                            <TD vAlign=center align=middle><IMG height=15 
                              src="formspacer_leftscreen.gif" 
                              width=1></TD></TR>
                          <TR>
                          <%-- ssadedin: Signatures are hard coded (hacked) for the demo --%>
                            <TD vAlign=center><TEXTAREA class=HIPAAblock name=signature1>MedCommons: Order Form DEM001rev1<%= signature1 %></TEXTAREA></TD></TR>
                          <TR>
                            <TD vAlign=center><TEXTAREA class=HIPAAblock name=signature2>Sender: John Smith<%= signature2 %></TEXTAREA></TD></TR>
                          <TR>
                            <TD vAlign=center><TEXTAREA class=HIPAAblock name=signature3>Destination: - Adrian Gropper, MD - Unsigned<%= signature3 %></TEXTAREA></TD></TR>
                              <TR><TD colspan="3">&nbsp;</TD></TR>
                           </TBODY></TABLE></TD>
                           <TD class=FolderLeft>&nbsp;</TD></TR>
                      </TBODY>
                      </TABLE>
                  </TD>
                </TR>
                   <TR>
          <TD class=FooterCell vAlign=center align=middle height=50><br/>
          <%if("new".equals(request.getParameter("ordertype"))) { %>
	        <button id="PlaceOrder" onclick="orderThankYou('<%= tracking %>','<%= guid %>' );" class="ButtonText">Submit<br/></button>
	        &nbsp;<button id="CancelOrder" onclick="displaySelectedSeries(0);" class="ButtonText">Cancel</button>
          <% } %>
	        &nbsp;<button id="PrintOrder" onclick="printOrder('<%= tracking %>','<%= guid %>' );" class="ButtonText">&nbsp;&nbsp;Print&nbsp;&nbsp;<br/></button>
    </TD>
    </TR>
    </TBODY></TABLE></TD>
          <TD width=420 bgColor=#dcdfee>
            <TABLE cellSpacing=0 cellPadding=0 width=420 border=0>
              <TBODY>
              <TR>
                <TD width=60><IMG height=18 
                  src="formspacer_rightscreen.gif" width=60></TD>
                <TD width=320><IMG height=1 
                  src="formspacer_rightscreen.gif" width=320></TD>
                <TD width=40><IMG height=1 
                  src="formspacer_rightscreen.gif" width=40></TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>
                  <P class=RightColHeader>Night Interpretation Order</P></TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD colSpan=3><IMG height=15 
                  src="formspacer_rightscreen.gif" width=1></TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>
                  <P class=RightFieldHeader>History (Non Private 
                Information)</P></TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>
                  <TEXTAREA class=RightColTextField name=history><%= history %></TEXTAREA> 
                  </TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD colSpan=3><IMG height=15 
                  src="formspacer_rightscreen.gif" width=1></TD></TR>
              <TR>
                <TD width=60 height=20>&nbsp;</TD>
                <TD width=320>
                  <P class=RightFieldHeader>Comments</P></TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>
                  <TEXTAREA class=RightColTextField name=comments><%= comments %></TEXTAREA> 
                </TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD colSpan=3><IMG height=15 
                  src="formspacer_rightscreen.gif" width=1></TD></TR>
              <TR>
                <TD>&nbsp;</TD>
                <TD>
                  <P class=RightFieldHeader>Send Invitation To</P></TD>
                <TD>&nbsp;</TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>
                  <INPUT class=RightColTextSmall name=copyto value="<%= copyto %>"></TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>&nbsp;</TD>
                <TD width=40>&nbsp;</TD></TR>
              <TR>
                <TD width=60>&nbsp;</TD>
                <TD width=320>&nbsp;</TD>
                <TD width=40>&nbsp;</TD>
                </TR>
                </TBODY>
                </TABLE>
                </TD>
                </TR>
                </TBODY>
  </TABLE></TD></TR>

  <% pageContext.setAttribute("notools", "true"); %>
  <%@ include file="ThumbnailFragment.htmlf" %>
     
    </TBODY>
    </TABLE></TD></TR>
    </TBODY>
    </TABLE>
    </BODY>
    </HTML>
