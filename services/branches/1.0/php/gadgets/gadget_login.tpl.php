<? 
require_once "settings.php";
require_once "session.inc.php";

global $Secure_Url;

$url = "/gadgets/connect.php?userid=".urlencode($userid)."&idp=".urlencode($ctx->type)."&mc_gadget_ctx=".urlencode($ctx->type);
$next = strong_url($url);


// Debug code
//
$enc = substr($next,strpos($next,"enc="));
dbg("encrypted query string = ".$enc);
//
$qs = get_encrypted_query_string($enc);
dbg("Got decrypted query string: ".$qs);
//

//$next = "/gadgets/connect.php?userid=".urlencode($userid)."&idp=".urlencode($ctx->type)."&mc_gadget_ctx=".urlencode($ctx->type);

?>

<div style='width: 85%;'>
<h4>Welcome <?=$ctx->user_name()?></h4>

<p style='font-size: 10px;'>You don't appear to have linked your <?$ctx->name?> account to your  
MedCommons Account.  To get started, click the button below.</p>  
<form name='connectForm' action='<?=$Secure_Url?>/acct/login.php' <?=$ctx->top_target()?> method='POST'>
    <input name='prompt' type='hidden' value='../gadgets/gadget_login_prompt'/>
    <input name='next' type='hidden' value='<?=$next?>'/>
    <input name='mc_gadget_ctx' type='hidden' value='<?=$ctx->type?>'/>
    <input name='connect' type='submit' value='Connect'/>
</form>
</div>
<? 
/*
    echo "<p> There are ".count($_REQUEST)." request parameters</p>";
    foreach($_REQUEST as $key => $value) {
        echo htmlentities($key)." => ".htmlentities($value)."<br/>";
    }
*/
?>