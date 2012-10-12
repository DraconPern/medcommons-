<?
/**
 * Template for voucher pickup page
 */
$template->extend("base.tpl.php");
$template->set("title","Claim Voucher - MedCommons on Demand");
?>
    
<?block("head")?>
<style type='text/css'>
    p {
        margin: 0.5em 0em;
    }
    #signinTable {
        padding-bottom: 1em;
    }
    #signinTable td, 
    #signinTable th {
        padding: 10px;
    }
    .errorAlert {
        color: orange;
    }
    #topheader {
     border-bottom: 2px solid #336699;
     width: 740px;
    }
    #footer {
	    border: none;
    }
    .inperr {
        color: orange;
    }
</style>
<?end_block("head")?>

<?block("content")?>

<table width="740" border="0" cellspacing="0" cellpadding="0">
  <tr>
    <td>
     
      
    </td>
    <td>&nbsp;</td>
    <td>&nbsp;</td>
  </tr>

  <tr>
    <td width="525" height="227" valign="top">
      <h2>Pickup Records</h2>

		<div class='inperr' id='voucherid_err'><?=hsc($v->voucherid_err)?></div>
		<div class='inperr' id='err'><?=hsc($v->err)?></div>
		
      <p>
Please enter the <b>Voucher ID</b> and <b>PIN</b> from your 
voucher. 

<p>
If you have a <b>Promotional Code</b>, please enter that as 
well. 
<p>
Payment or a valid promotional code will enable sharing and collaboration
features for one patient <b>HealthURL</b> for 30 days.</td>
    <td width="15" valign="top">&nbsp;</td>

    <td width="200" valign="top">
    
   <!-- Log In Table ** added forms code in here ** -->
   <form action="/mod/voucherclaim.php" method='POST'>
    <table id='signinTable' width="200" border="0" cellspacing="0" cellpadding="10" style="background-image: url(/images/blueGradientBG.png); background-repeat: no-repeat;">
      <tr>
        <td valign="top">
			  Voucher ID
			  <br />
			
			<div class=inperr id=voucherid_err></div>
			<input type=text name=voucherid value='<?=htmlentities($v->voucherid,ENT_QUOTES)?>' />
			<br />
			      PIN
			      <br />
			<input type=password name=otp value='<?=htmlentities($v->otp,ENT_QUOTES)?>' />   
			<br />
			<div class=inperr id=otp_err></div>
			     Promo Code
			<br />
			<input name="PromoCode" type="text" value="WELCOME" />    
			<p>
			<input type=submit class='mainwide' value='Claim Voucher' />
			</p>
        </td>
      </tr>
    </table>
    </form>
    <!-- Log In Table ** added forms code above here ** --></td>
  </tr>
</table>
<?end_block("content")?>