<?PHP
function display_whitespace()
{
	$x=<<<XXX
<TABLE cellSpacing=0 cellPadding=0 border=0>
    <TBODY>
	   <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
 
   </TBODY>
 </TABLE>               

XXX;
return $x;
}
function display_partner_footer_form()
{
	$x=<<<XXX
<TABLE cellSpacing=0 cellPadding=0 border=0>
    <TBODY>
	   <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD></TR>
        </TR>
      
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300><P></P></TD>
          <TD width=30>&nbsp;</TD>
        </TR>
   </TBODY>
 </TABLE>               

XXX;
return $x;
}
function display_tracking_form($trackingNumber,$guid)
{
	// <INPUT class=TrackingField type=protected name=trackingNumber size=50 value='$trackingNumber'/>
	$x=<<<XXX
<TABLE cellSpacing=0 cellPadding=0 border=0>
   <TBODY>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300><P >Tracking</TD>
          <TD width=30>&nbsp;</TD></TR>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width = 300 colSpan=2 >
          		$trackingNumber
                 <INPUT TYPE="HIDDEN" NAME="guid" VALUE='$guid'/>
          </TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
      </TBODY>
 </TABLE>               
XXX;
return $x;                
}

function display_hipaa_form()
{
	$x=<<<XXX
	
                
<TABLE cellSpacing=0 cellPadding=0 border=0>
   <TBODY>
      <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300><P >HIPAA</TD>
          <TD width=30>&nbsp;</TD></TR>
        </TR>
 <TR>
           <TD width=30>&nbsp;</TD>

  <TD width=300>
   <TEXTAREA class=HIPAAblock cols=38 rows=3 name=signature1>MedCommons: Order Form DEM001rev1</TEXTAREA>
   </TD>
             <TD width=30>&nbsp;</TD>

  </TR>
  <TR>
            <TD width=30>&nbsp;</TD>

   <TD width=300><TEXTAREA class=HIPAAblock cols=38 rows=3 name=signature2>Sender: John Smith</TEXTAREA>
   </TD>
             <TD width=30>&nbsp;</TD>

  </TR>
  <TR> 
   <TD width=30>&nbsp;</TD>

   <TD width=300><TEXTAREA class=HIPAAblock cols=38 rows=3 name=signature3>Destination: - Adrian Gropper, MD - Unsigned</TEXTAREA>
   </TD>
             <TD width=30>&nbsp;</TD>

  </TR>
  </TBODY>
 </TABLE>  
XXX;
return $x;
}

function display_account_form($accountName,$address)
{
	$x=<<<XXX
	
<TABLE cellSpacing=0 cellPadding=0 border=0>
  <TBODY>
  <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300><P >Account</TD>
          <TD width=30>&nbsp;</TD>
  </TR>
    <TR>
      <TD width=30>&nbsp;</TD>
      <TD width=300><input type="TEXT" name=accountName size=50 value=$accountName></TD>
      <TD width=30>&nbsp;</TD>
    </TR>
     <TR>
       <TD width=30>&nbsp;</TD>
           <TD width=300><TEXTAREA class=HIPAAblock cols=38 rows=4 name=address>$address</TEXTAREA></TD>
      <TD width=30>&nbsp;</TD>
     </TR>
 </TBODY>
</TABLE>
XXX;
return $x;
}
                  
function display_credit_form()
{
	$x=<<<XXX
	              <TABLE cellSpacing=0 cellPadding=0 width=330 border=0>
                    <TBODY>
                    <TR>
                      <TD class=FolderTab width=72 bgColor=#e1e3f0> <P class=FolderHeader>Charges</P></TD>
                      <TD class=FolderTop width=218>&nbsp;</TD>
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
                            <TD width=170><input type="text" name="cardNumber" value="7817574478133225" class="CreditCardField"></TD>
                            <TD width=130><input class=CreditCardFieldRight name=expiration value='12/09'></TD></TR>
                          <TR>
                            <TD class=FieldHeader colSpan=2><IMG height=5 src="formspacer_leftscreen.gif" width=1></TD></TR>
                          <TR>
                            <TD class=FieldHeader width=170> <P align=right>amount</P></TD>
                            <TD width=130><input type="text" name="amount" value="150.00" class="CreditCardFieldRight"></TD></TR>
                          <TR>
                            <TD class=FieldHeader width=170> <P align=right>tax</P></TD>
                            <TD width=130><input class=CreditCardFieldRight name=tax value='12/09'></TD></TR>
                          <TR>
                            <TD class=FieldHeader width=170> <P align=right>charge</P></TD>
                            <TD width=130><input type="text" name="charge" value="162.00" class="CreditCardFieldRight"></TD></TR>
                              <TR><TD colspan="3">&nbsp;</TD></TR>
                        </TBODY>
                       </TABLE>
                      </TD>
                      <TD class=FolderLeft>&nbsp;</TD></TR>
                  </TBODY>
              </TABLE>
XXX;
return ""; //for now
}


function display_partner_form($partnerheader, $trackingNumber,$guid,
                                 $accountName,$address,$error)
{	$spacer = display_whitespace();
	$creditstuff = display_credit_form();
	$accountstuff = display_account_form($accountName,$address);
 	$hipaastuff = display_hipaa_form();
	$trackingstuff = display_tracking_form($trackingNumber,$guid);
	$partnerfooterstuff = display_partner_footer_form();
	$x=<<<XXX
	<TABLE cellSpacing=0 cellPadding=0  border=0>
        <TBODY> 
            <TR>
         <TD width=30>&nbsp;</TD>
          <TD width=300><P ><b>$partnerheader</b></P></TD>
          <TD width=30>&nbsp;</TD>
		</TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;$error</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        $trackingstuff
        $spacer
        $accountstuff
        $spacer
        $spacer
        $hipaastuff
        $spacer
        $spacer
        $partnerfooterstuff
    </TBODY>
    </TABLE>
XXX;
return $x;
}

function display_phi_form($phiheader,$history,$comments,$error)
{
	$x=<<<XXX
	   <TABLE bgColor=#dcdfee cellSpacing=0 cellPadding=0  border=0>
        <TBODY>
        <TR>
         <TD width=30>&nbsp;</TD>
          <TD width=300><P ><b>$phiheader</b></P></TD>
          <TD width=30>&nbsp;</TD>
		</TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;$error</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300> <P class=RightFieldHeader>History (Non Private Information)</P></TD>
          <TD width=30>&nbsp;</TD></TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300> <TEXTAREA rows=8 cols=38 class=RightColTextField name=history>$history</TEXTAREA> </TD>
          <TD width=30>&nbsp;</TD></TR>
        <TR>
          <TD colSpan=3><IMG height=15 src="formspacer_rightscreen.gif" width=1></TD></TR>
        <TR>
          <TD width=30 height=20>&nbsp;</TD>
          <TD width=300><P class=RightFieldHeader>Comments</P></TD>
          <TD width=30>&nbsp;</TD></TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300><TEXTAREA rows=8 cols=38 class=RightColTextField name=comments>$comments</TEXTAREA></TD>
          <TD width=30>&nbsp;</TD></TR>
        <TR>
          <TD colSpan=3><IMG height=15 src="formspacer_rightscreen.gif" width=1></TD></TR>
        <TR>
          <TD>&nbsp;</TD>
          <TD> <P class=RightFieldHeader>Send Invitations to</P></TD>
          <TD>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>
            <input type="text" name="email1" size="50"  class="RightColTextSmall"></TD>
          <TD width=30>&nbsp;</TD>
        </TR>
         <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>
            <input type="text" name="email2" size="50"  class="RightColTextSmall"></TD>
          <TD width=30>&nbsp;</TD>
        </TR>
         <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>
            <input type="text" name="email3" size="50"  class="RightColTextSmall"></TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        <TR>
          <TD width=30>&nbsp;</TD>
          <TD width=300>&nbsp;</TD>
          <TD width=30>&nbsp;</TD>
        </TR>
        </TBODY>
     </TABLE>
XXX;
return $x;
}

function display_order_form(
$trackingNumber,
$guid,
$accountName,
$address,
$history='',
$comments='',
$partnererror='',
$mcerror='')
{
	$wbh = wbheader('order',"MedCommons Order Form",true);
	$phiheader = "Night Interpretation Order";
	$partnerheader ="Partner Order Form";
	$partnerform = display_partner_form($partnerheader,$trackingNumber,$guid,$accountName,$address,
	$partnererror);
	$phiform = display_phi_form($phiheader,$history,$comments,$mcerror);
$x=<<<XXX
$wbh  
<FORM action=../whitebox/orderhandler.php method=Get 
                enctype="application/x-www-form-urlencoded"> 
<TABLE cellSpacing=0 cellPadding=0 align="left"
            valign="top" border=0>
  <TBODY>
  <TR>
    <TD align="left" width=360 bgColor=#bfc4e1>$partnerform
    </TD>
    <TD align="left" width=360 bgColor=#dcdfee>$phiform
   </TD>
   </TR>
 </TBODY>
</TABLE>
<br>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
<input type="SUBMIT" value = "submit" name = "submit">
</FORM>  
</body>
</html>
XXX;


echo $x;
}



?>
