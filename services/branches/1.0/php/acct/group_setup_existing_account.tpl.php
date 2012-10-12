<?
$template->extend("groupsetup.tpl.php");
$template->set("title","Quick Group Setup");
?>

<?block("defaultMessage")?>
<p>The email address you entered corresponds to an existing account.</p>
<b>If you continue, the new practice will be associated to your existing account, and will
   be additional to any practices or groups you may already be a member of.</b>
<p>Please enter your password for the existing account in the space provided, or click 'Cancel' to go back and
change the email address you wish to register with.
</p>
<?end_block()?>

<?block("personalDetails")?>
    <script type='text/javascript'>
        addLoadEvent(function() {
            $('pw').focus();
            $('pw').select();
            connect('cancelButton', 'onclick', function() {
                $('email').value = '';
                document.registrationForm.action='groupsetup.php?cancel=true';
                document.registrationForm.submit();
            });
        });
    </script>
    
   <p class='error'>You have entered an email that's already registered. Please enter the original password. It will not be changed.</p>
    <input type='hidden' name='is_existing_account' value='true'/>
    <input type='hidden' name='fn' value='<?=field('fn')?>'/>
    <input type='hidden' name='ln' value='<?=field('ln')?>'/>
    <table border='0' id='personalDetails'>
    <tr>
      <th><label for='email'>Email Address</label></th>
      <td><input type='text' id='email' name='email' class='infield' value='<?=field('email')?>' readonly='true'/></td>
      <td class='error'><?=error_msg('email')?></td>
    </tr>
    <tr>
      <th><label for='pw'>Password</label></th>
      <td><input type='password' id='pw' name='pw' class='infield'/></td>
      <td class='error'><?=error_msg('pw')?></td>
    </tr>
   </table>
<?end_block()?>F

<?block("extraButtons")?>
<input type='button' name='cancelButton' id='cancelButton' value='Cancel'/>
<?end_block()?>