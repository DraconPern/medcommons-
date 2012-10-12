<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-logic" prefix="logic" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-template" prefix="template" %>
<%@ page import="net.medcommons.router.services.dicom.util.*"  %>
<%@ page import="net.medcommons.router.services.transfer.*"  %>
<%@ page import="net.medcommons.router.configuration.*" %>
<%--

  MedCommons WADO Viewer JSP

  This file has a default dummy order form and also the code to show
  an externally provided order form based on an orderUrl parameter. 

--%>
<logic:present name="viewerForm" property="orderUrl">
  <iframe src='orderFormPost.jsp?menuKey=<%=request.getParameter("menuKey")%>' name='orderWindow' width="750" height="750" style="border-style: none; "/>  
</logic:present>
<logic:notPresent name="viewerForm" property="orderUrl">
  <bean:define id="tracking" name="viewerForm" property="tracking"/>
  <bean:define id="guid" name="viewerForm" property="guid"/>
  <script language="JavaScript">
    //FormType=ORDERFormType;
  </script>
  <TABLE cellSpacing=0 cellPadding=0 width="768px" height="100%" align=left valign="top" border="0">
    <TBODY>
    <TR>
      <TD width="100%" height="100%">
        <TABLE height="100%" cellSpacing=0 cellPadding=0 width="100%" align=left border=0>
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
                        <TD width=20><IMG height=18 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD width=72><IMG height=1 src="formspacer_leftscreen.gif" width=72></TD>
                        <TD width=218><IMG height=8 src="formspacer_leftscreen.gif" width=218></TD>
                        <TD width=20><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD>
                      </TR>
                      <TR>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD class=FolderTab width=72 bgColor=#e1e3f0> <P class=FolderHeader>Tracking</P></TD>
                        <TD class=FolderTop width=218>&nbsp;</TD>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                      <TR>
                        <TD rowSpan=2><IMG height=80 src="formspacer_leftscreen.gif" width=1></TD>
                        <TD bgColor=#e1e3f0 colSpan=2 height=40>
                          <FORM name=form1>
                             <INPUT class=TrackingField name=tracking value='<bean:write name="viewerForm" property="tracking"/>'/>
                             <INPUT TYPE="HIDDEN" NAME="guid" VALUE='<bean:write name="viewerForm" property="guid"/>'/>
                          </FORM>
                        </TD>
                        <TD class=FolderLeft rowSpan=2>&nbsp;</TD>
                      </TR>
                      <TR>
                        <TD bgColor=#e1e3f0 colSpan=2 height=40><IMG height=40 
                          src="medcommons_trackingfooter.gif" 
                          width=300></TD></TR>
                    </TBODY>
                  </TABLE>
                  </TD>
                </TR>
                <TR>
                  <TD>
                    <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                      <TBODY>
                      <TR>
                        <TD width=20><IMG height=18 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD width=72><IMG height=1 src="formspacer_leftscreen.gif" width=72></TD>
                        <TD width=218><IMG height=1 src="formspacer_leftscreen.gif" width=218></TD>
                        <TD width=20><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                      <TR>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD class=FolderTab width=72 bgColor=#e1e3f0> <P class=FolderHeader>Account</P></TD>
                        <TD class=FolderTop width=218>&nbsp;</TD>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                      <TR>
                        <TD>&nbsp;</TD>
                        <TD bgColor=#e1e3f0 colSpan=2>
                          <TABLE cellSpacing=0 cellPadding=0 width=300 border=0>
                            <TBODY>
                            <TR>
                              <TD colSpan=3><input class=AccountFieldLeft name=name value='<bean:write name="viewerForm" property="name"/>'></TD></TR>
                            <TR>
                              <TD colSpan=3><IMG height=10 src="formspacer_leftscreen.gif" width=1></TD></TR>
                            <TR> <TD colSpan=3><TEXTAREA class=HIPAAblock name=address><bean:write name="viewerForm" property="address"/></TEXTAREA></TD></TR>
                            <TR>
                              <TD width=120><input class=AccountFieldLeft name=city value='<bean:write name="viewerForm" property="city"/>'></TD>
                              <TD align=middle width=60><input class=AccountFieldSmall name=state value='<bean:write name="viewerForm" property="state"/>'> </TD>
                              <TD width=120><input class=AccountFieldRight name=zip value='<bean:write name="viewerForm" property="zip"/>'></TD></TR>
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
                        <TD width=20><IMG height=18 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD width=72><IMG height=1 src="formspacer_leftscreen.gif" width=72></TD>
                        <TD width=218><IMG height=1 src="formspacer_leftscreen.gif" width=218></TD>
                        <TD width=20><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                      <TR>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD class=FolderTab width=72 bgColor=#e1e3f0> <P class=FolderHeader>Charges</P></TD>
                        <TD class=FolderTop width=218>&nbsp;</TD>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                      <TR>
                        <TD>&nbsp;</TD>
                        <TD bgColor=#e1e3f0 colSpan=2>
                          <TABLE cellSpacing=0 cellPadding=0 width=300 border=0>
                            <TBODY>
                            <TR>
                              <TD vAlign=bottom width=170>
                                <P class=FieldHeader>card number</P></TD>
                              <TD width=130> <P class=FieldHeader>exp</P></TD></TR>
                            <TR>
                              <TD width=170><html:text styleClass="CreditCardField" name="viewerForm" property="cardNumber"/></TD>
                              <TD width=130><input class=CreditCardFieldRight name=expiration value='<bean:write name="viewerForm" property="expiration"/>'></TD></TR>
                            <TR>
                              <TD class=FieldHeader colSpan=2><IMG height=5 src="formspacer_leftscreen.gif" width=1></TD></TR>
                            <TR>
                              <TD class=FieldHeader width=170> <P align=right>amount</P></TD>
                              <TD width=130><html:text styleClass='CreditCardFieldRight' name="viewerForm" property="amount"/></TD></TR>
                            <TR>
                              <TD class=FieldHeader width=170> <P align=right>tax</P></TD>
                              <TD width=130><input class=CreditCardFieldRight name=tax value='<bean:write name="viewerForm" property="expiration"/>'></TD></TR>
                            <TR>
                              <TD class=FieldHeader width=170> <P align=right>charge</P></TD>
                              <TD width=130><html:text styleClass="CreditCardFieldRight" name="viewerForm" property="charge"/></TD></TR>
                                <TR><TD colspan="3">&nbsp;</TD></TR>
                          </TBODY>
                         </TABLE>
                        </TD>
                        <TD class=FolderLeft>&nbsp;</TD></TR>
                    </TBODY>
                </TABLE>
                </TD>
                </TR>
                <TR>
                  <TD>
                    <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                      <TBODY>
                      <TR>
                        <TD width=20><IMG height=18 src="formspacer_leftscreen.gif" width=20></TD>
                        <TD width=72><IMG height=1 src="formspacer_leftscreen.gif" width=72></TD>
                        <TD width=218><IMG height=1 src="formspacer_leftscreen.gif" width=218></TD>
                        <TD width=20><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD></TR>
                      <TR>
                        <TD><IMG height=1 src="formspacer_leftscreen.gif" width=20></TD>
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
                              <TD vAlign=center><TEXTAREA class=HIPAAblock name=signature1>MedCommons: Order Form DEM001rev1<bean:write name="viewerForm" property="signature1"/></TEXTAREA></TD></TR>
                            <TR>
                              <TD vAlign=center><TEXTAREA class=HIPAAblock name=signature2>Sender: John Smith<bean:write name="viewerForm" property="signature2"/></TEXTAREA></TD></TR>
                            <TR>
                              <TD vAlign=center><TEXTAREA class=HIPAAblock name=signature3>Destination: - Adrian Gropper, MD - Unsigned<bean:write name="viewerForm" property="signature3"/></TEXTAREA></TD></TR>
                                <TR><TD colspan="3">&nbsp;</TD></TR>
                             </TBODY>
                          </TABLE></TD>
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
              </TABLE>
      </TD>
      </TR>
      </TBODY>
      </TABLE>

      </TD>
      <TD width=420 bgColor=#dcdfee>
        <TABLE cellSpacing=0 cellPadding=0 width=420 border=0>
          <TBODY>
          <TR>
            <TD width=60><IMG height=18 src="formspacer_rightscreen.gif" width=60></TD> <TD width=320><IMG height=1 
              src="formspacer_rightscreen.gif" width=320></TD> <TD width=40><IMG height=1 src="formspacer_rightscreen.gif" width=40></TD></TR>
          <TR>
            <TD width=60>&nbsp;</TD>
            <TD width=320> <P class=RightColHeader>Night Interpretation Order</P></TD>
            <TD width=40>&nbsp;</TD></TR>
          <TR>
            <TD colSpan=3><IMG height=15 src="formspacer_rightscreen.gif" width=1></TD></TR>
          <TR>
            <TD width=60>&nbsp;</TD>
            <TD width=320> <P class=RightFieldHeader>History (Non Private Information)</P></TD>
            <TD width=40>&nbsp;</TD></TR>
          <TR>
            <TD width=60>&nbsp;</TD>
            <TD width=320> <TEXTAREA class=RightColTextField name=history><bean:write name="viewerForm" property="history"/></TEXTAREA> </TD>
            <TD width=40>&nbsp;</TD></TR>
          <TR>
            <TD colSpan=3><IMG height=15 src="formspacer_rightscreen.gif" width=1></TD></TR>
          <TR>
            <TD width=60 height=20>&nbsp;</TD>
            <TD width=320><P class=RightFieldHeader>Comments</P></TD>
            <TD width=40>&nbsp;</TD></TR>
          <TR>
            <TD width=60>&nbsp;</TD>
            <TD width=320><TEXTAREA class=RightColTextField name=comments><bean:write name="viewerForm" property="comments"/></TEXTAREA></TD>
            <TD width=40>&nbsp;</TD></TR>
          <TR>
            <TD colSpan=3><IMG height=15 src="formspacer_rightscreen.gif" width=1></TD></TR>
          <TR>
            <TD>&nbsp;</TD>
            <TD> <P class=RightFieldHeader>Send Invitation To</P></TD>
            <TD>&nbsp;</TD>
          </TR>
          <TR>
            <TD width=60>&nbsp;</TD>
            <TD width=320>
              <html:text styleClass="RightColTextSmall" name="viewerForm" property="copyTo"/></TD>
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
  </TABLE>
</logic:notPresent>

