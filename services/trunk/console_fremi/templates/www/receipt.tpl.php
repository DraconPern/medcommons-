<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
    "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xml:lang="en" lang="en">
  <head>
    <meta http-equiv='Content-Type' content="text/html; charset=iso-8859-1" />
    <style type='text/css'>
    h2 {
        font-size: 16px;
        color: #666;
    }
    table tr th {
      font-size: 13px;
      text-align: right;
      padding-right: 2em;
    }
    
    @media print {
      .noprint {
        display: none; 
      }
      #info {
       display: none;
      }
    }
    </style>
  </head>
  <body>
    <p>
    <img src='cid:logo' />
    </p>
    <h2>Thank you for registering an account with MedCommons</h2>
    
    <p>Keep this email as a reference to log in to your account in the future.</p>
    
    <p>To sign in to your account, use the following link:</p>
    
    <a href='<?=$signInUrl?>'><?=$signInUrl?></a>
    
<?if(isset($uploadUrl) && $uploadUrl):?>
    <p>Your image dropbox URL is:</p>
    
    <a href='<?=$uploadUrl?>'><?=$uploadUrl?></a>
    
    <p>You can share this link with others to allow them to upload images to your account.</p>
<?endif?>    
<?/*    
    <p>
    This email contains important registration information.
    Please <strong>print it out</strong> or keep it securely where
    you can find it later if you need it.
    This is your MedCommons <strong>Registration Receipt</strong>.
    </p>
    <p>
    Each one of the <strong>Recovery Passwords</strong> listed below can only
    be used once, in order.
    </p>
    <p>
    Cross out each one as you use them.  This protects against someone
    using your account without you knowing it!
    </p>
    
    <table>
     <tbody>
     <?if($first_name || $last_name):?>
      <tr>
       <th>For:</th>
       <td><?php echo $first_name . ' ' . $last_name; ?></td>
      </tr>
      <?endif;?>
      <tr>
       <th>Email:</th>
       <td><?php echo $email?></td>
      </tr>
      <tr>
       <th>Domain:</th>
       <td><?php echo $domain?></td>
      </tr>
      <tr>
       <th><acronym title='MedCommons ID'>MCID</acronym>:</td>
       <td><?php echo $mcid; ?></td>
      </tr>
      <tr>
       <th>Recovery Passwords:</th>
       <td>
    <?php
    
    	$i = 1;
    	while ($skey) {
    	  echo $i;
    	  echo ". ";
    	  echo array_pop($skey);
    	  echo "<br />\n";
    	  $i++;
    	}
    ?>
        </td>
       </tr>
      </tbody>
     </table>
*/?>     
     
    </body>
</html>