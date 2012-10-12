<?

$_GLOBALS['no_session_check']=true;
require_once "utils.inc.php";

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
    input {
        font-size: 90%;
        padding: 3px;
    }
    #signin label { display: block;  margin: 0.4em 0 0.2em 0; }
    #topheader {
     border-bottom: 2px solid #336699;
     width: 740px;
	}
    #footer {
	    border: none;
    }
    #mcid, #password, #openid_url {
        width: 90%;
    }
    #biglinks {
        font-size: 110%;
    }
</style>
<?
end_block("head");
block("content");
?>
<h2>Sign In</h2>

<?if(isset($prompt)):?>
	<?=$prompt?>
<?endif;?>
        
<?if(isset($error)): ?>
  <div class='errorAlert'>
    <?= $error ?>
  </div>
<?endif?>
      
   <table><tr><td>
   <!-- Log In Table-->
   <table cellspacing="0" cellpadding="10" border="0" width="300">
      <tbody>
          <tr>
             <td valign="top">
    		  <form name="login" id="signin" action='/acct/login.php' method="post">
    		    <label for="openid_url">Email Address</label>
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
    			<span class="bodyLink">
        			<a class="bodyLink" target="popUp" 
    			       href="<?=gpath("Secure_Url")?>/acct/forgot.php?next=/acct/home.php">Recover Password</a></span>
    	        </p>			
    		</form>
    		</td>
    	  </tr>
        </tbody>
        </table>
    </td>
    <td valign='top'>
	  <ul id='biglinks'>
		  <li><a href='/mod/voucherclaim.php'>Pickup Records</a></li>
		  <li><a href='/acct/register.php'>Register</a></li>
	  </ul>
   </td>
  </tr>
</tbody>
</table>
<?
end_block("content");
?>
