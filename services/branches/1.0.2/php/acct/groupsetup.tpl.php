<?
$template->extend("base.tpl.php");
$template->set("title","Quick Group Setup");
?>

<?block("head")?>
<style type='text/css'>
  @import url('register.css');
  
  table {
      width: 100%;
  }
  
  hr {
      clear: both;
  }
  
  h4 {
      margin-top: 6px;
  }
  
  .submitButtons {
      float: right;
  }
  
  #inviteEmails {
      width: 80%;
      margin-left: 20%;
  }
  
  #registerContents {
      margin-left: 2px;
      margin-top: 15px; 
      width: 540px;
  }
  p.error,span.error { color: red; }
  #registerContents h4 img, #portalTypeInstructionsButton {
      float: right;
      position: relative;
      top: 5px;
      cursor: pointer;
  }
  #portalTypeInstructionsButton {
    <?/* compensate for 2px padding in table cell*/?>
    left: 2px;
  }
  
  option.disabled {
    color: gray;
  }
  
  p.instr {
      font-style: italic;
      font-size: 95%;
      display: none;
  }
</style>
<script type='text/javascript' src='acct_all.js'></script>
<script type='text/javascript'>
    addLoadEvent(function() {
        var imgs = $$('#registerContents h4 img');
        imgs.push($('portalTypeInstructionsButton'));
        forEach(imgs, function(img) { 
            img.title = 'Click to show Help / Instructions';
            connect(img,'onclick', function() {
                var el = $(img.id.replace('Button',''));
                if(getStyle(el,'display') == 'none') 
                     blindDown(el,{duration:0.3});
                else
                     blindUp(el,{duration:0.3});
            });
        });
        connect($('portalType'),'onchange', function() {
            if($('portalType').options[$('portalType').selectedIndex].className == 'disabled')
	            alert('This workflow template is not available yet for automatic setup.\r\n\r\nA MedCommons respresentative will contact you to setup practice after you register.');
        });
        connect('submitButton','onclick', function() { setTimeout( function() {disable('submitButton');}, 200); });
    });
</script>

<?end_block("head")?>

<?block("content")?>
<h2>MedCommons Practice Registration</h2> 
<?if(isset($errors)):?>
    <p class='error'>One or more errors was found in your form.  Please correct them and try submitting again.</p>
<?elseif(isset($msg)):?>
<p><?=$msg?></p> 
<?else:?>
<?block("defaultMessage")?>
<p>Use this page to register a Practice and get started in minutes.</p>
<?end_block()?>
<?endif;?>

<form name='registrationForm' method='post' action='groupsetup.php'>
<div id='registerContents' class='left'>
  <h4>Your Practice  <img id='practiceInstructionsButton' src='images/help.png'/></h4>
  <p id='practiceInstructions' class='instr'>
  This name will label your Dropbox and identify the group of colleagues that 
  have access to the health records in the dropbox.
  </p>
  <hr/>
  <table border='0'>
    <tr>
      <th><label for='practiceName'>Name of Your Practice</label></th>
      <td><input type='text' name='practiceName' class='infield' value='<?=field('practiceName')?>'/></td>
      <td class='error'><?=error_msg('practiceName')?></td>
    </tr>

    <tr>
      <th><label for='practiceName'>Practice Workflow Template</label></th>
      <td>
        <select id='portalType' name='portalType'>
            <option value='basic'>Basic</option>
            <option class='disabled' value='open'>Open</option>
            <option class='disabled' value='private'>Private</option>
        </select>
      </td>
      <td><img id='portalTypeInstructionsButton' src='images/help.png'/></td>
    </tr>
    <tr>
        <td colspan='3'>
        <p id='portalTypeInstructions' class='instr'>
            Your Workflow Template selects a default configuration for your Practice to 
            suit your preferences.   The Basic template provides you with a 
            simple upload form while other templates give you features such as
            a directory of consultants for visitors to refer to and collaboration
            features such as phone, video or SMS integration..
        </p>
        </td>
    </tr>
  </table>
    
  <h4>Your Details <img id='detailsInstructionsButton' src='images/help.png'/></h4>
  <p id='detailsInstructions' class='instr'>
  You will sign-in to view the Dropbox, make changes and invite others to the access group. 
  Your name and email will not be shared beyond 
  MedCommons but will be visible to others with access to the Dropbox and the activity logs.
  </p>
  <hr/>
  
  <?block("personalDetails")?>
    <table border='0' id='personalDetails'>
     <tr>
      <th>
       <label for='fn'>Admin First Name</label>
      </th>
      <th>
       <label for='ln'>Admin Last Name</label>
      </th>
     </tr>
     <tr>
      <td>
       <input class='infield'  type='text' name='fn' id='fn' value='<?=field('fn')?>' />
      </td>
      <td>
       <input class='infield'  type='text' name='ln' id='ln' value='<?=field('ln')?>' />
      </td>
      <td class='error'><?=error_msg('fn')?><?if(isset($errors->ln)&&isset($errors->fn)):?> , <?endif;?><?=error_msg('ln')?></td>
     </tr>
     <tr id='p_email'>
      <th><label>Admin Email</label></th>
      <td><input class='infield'  type='text' name='email' id='email' value='<?=field('email')?>' /></td>
      <td class='error'><?=error_msg()?> </td>
     </tr>
     <tr id='p_pw1'>
        <th>
           <label>Password</label>
        </th>
        <td>
          <input class='infield'  type='password' name='pw1' id='pw1' />
        </td>
        <td class='error'> 
          <?=error_msg("pw1")?>
        </td>
      </tr>
      <tr>
        <th><label>Password (again)</label></th>
        <td><input class='infield'  type='password' name='pw2' id='pw2' /></td>
        <td class='error'>
          <?=error_msg("pw2")?></td>
      </tr>
      </table>
    <?end_block("personalDetails")?>
      
      
      <h4>Invite Others in your Practice (Optional) <img id='inviteInstructionsButton' src='images/help.png'/></h4>
      <p id='inviteInstructions' class='instr'>
      For HIPAA compliance, each user must have their 
      own sign-in and password. MedCommons will send an email to these users and request a 
      registration similar to this form. You can leave this blank for a personal Dropbox or to 
      invite others at a later time.
      </p>
      <hr/>
      
      <table border='0'>
        <tr><th colspan='2'><label for='inviteEmails'>Email Addresses (comma separated):</label></th></tr>
        <tr><td colspan='2'><input type='text' id='inviteEmails' name='inviteEmails' class='infield' value='<?=field("inviteEmails")?>'/></td></tr>
      </table>      
      
      
      <h4>Terms and Conditions</h4>
      <hr/>
      <div style='margin-left: 2em;'>
      <p id='p_termsOfUse'>
	      <label>
	        <input class='infield'  type='checkbox' name='termsOfUse' id='termsOfUse' <?if(field('termsOfUse')):?>checked='true'<?endif;?>/>
	        I have read and understand the
	        <a target='_new' href="/termsofuse.php"> Terms Of Use </a>
	      </label>
	      <span class='submitButtons'>
	          <input type='submit' id='submitButton' name='submitButton' value='Submit'/>
	          <?block("extraButtons")?> <?end_block();?>
	      </span>
      </p>
      <p class='error'><?=error_msg('termsOfUse')?></p>
      </div>
</div>
</form>
<?end_block("content")?>
