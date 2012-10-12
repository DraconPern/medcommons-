<?
$template->extend("base.tpl.php");
$template->set("title", "Welcome to MedCommons - Login");

// The name of username field depends on whether we're doing an openid login
$name = (isset($password) && $password) ? 'mcid' : 'openid_url';

if(!isset($openid_url)) {
  $openid_url = "";
}

block("head");
?>
<style type='text/css'>
    p {
	    margin: 0.5em 0em;
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
    #mcid, #password {
        width: 90%;
    }
    #biglinks {
        font-size: 120%;
        margin-top: 2em;
    }
</style>
<?
end_block("head");
block("content");
?>


<table id="signinTable" cellspacing="0" cellpadding="0" border="0" width="740">
  <tbody>
  <tr>
    <td width="525" valign="top">
      <span class="header2">Sign In</span>

        <?if(isset($prompt)):?>
        	<?=$prompt?>
        <?endif;?>
        
		<?if(isset($error)): ?>
		  <div class='errorAlert'>
		    <?= $error ?>
		  </div>
		<?endif?>

    <p style='color: orange;'>
     <b>Important:</b> If you are uploading images to TIMC then please close 
     this window and click 'OK' in the Order Entry screen next to the order you want 
     to upload.
    </p>
		      
	  <ul id='biglinks'>
		  <li><a href='/mod/voucherclaim.php'>Pickup Records</a></li>
		  <li><a href='/acct/groupsetup.php'>Register</a></li>
	  </ul>
</td>
    <td width="10" valign="top">&nbsp;</td>
    <td width="200" valign="top">
   <!-- Log In Table-->
    <table cellspacing="0" cellpadding="10" border="0" width="200" style="background-image: url(/images/blueGradientBG.png); background-repeat: no-repeat;">
      <tbody><tr>
             <td valign="top">

		  <span class="header2">Sign In</span>
		  <form name="login" id="signin" action='/acct/login.php' method="post">
		    <label for="openid_url">User ID (email,OpenID)</label>
		    <input type="text" value="<?=$openid_url?>" id="<?=$name?>" name="<?=$name?>" class="infield"/>
		
		    <label for="password">Password</label>
		    <input type="password" id="password" name="password"/>
			
			<?if(isset($next)):?>
				<input type='hidden' name='next' value="<?= $next ?>" />
			<?endif?>
			
			<p>
			<input type="submit" value="Sign In" name="loginsubmit" class="mainwide"/>
			</p>
			
			<p>
			<span class="bodyLink"><a class="bodyLink" target="popUp" href="https://healthurl.medcommons.net/acct/forgot.php?next=/acct/home.php">Problems?</a></span>
	        </p>			
		</form>
		</td>
	  </tr>
    </tbody>
    </table>
   </td>
  </tr>
</tbody>
</table>
<?
end_block("content");
?>
