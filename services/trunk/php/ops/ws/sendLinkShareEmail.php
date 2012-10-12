<?
  /**
   * Sends an email notifying a user about an email share link
   */

  require_once 'email.inc.php';
  require_once 'template.inc.php';
  require_once 'utils.inc.php';
  require_once 'JSON.php';
  require_once 'DB.inc.php';

  $result = new stdClass;

  try {
    $to = req('to');
    if(!$to || ($to == ""))
      throw new Exception("Missing parameter 'to'");
    
    $subject = req('subject');
    if(!$subject || ($subject == ""))
      throw new Exception("Missing parameter 'subject'");

    $link = req('link');
    if(!$link || ($link == ""))
      throw new Exception("Missing parameter 'link'");
      
    $comments = req('comments');

    $t = new Template();
    $t->set('link', $link);
    $t->set('comments', $comments);

    dbg("Using email templates from : " . email_template_dir());

    $text = $t->fetch(email_template_dir() . "linkShareText.tpl.php");
    $html = $t->fetch(email_template_dir() . "linkShareHTML.tpl.php");

    $stat = send_mc_email($to, $subject, $text, $html, array('logo' => get_logo_as_attachment()));
    
    $db = DB::get();
    $db->execute("INSERT INTO emailstatus (time,requesturi,sendermcid,rcvremail,template,arga,argb,argc,argd,arge,argf,argg,message,status)".
             "VALUES(NOW(),'','',?,'linkShare',?,'','','','','','',?,'$stat')",
              array($to,$link,$subject));

    $result->status = "ok";
  }
  catch(Exception $e) {
    $result->status = "failed";
    $result->error = $e->getMessage();
  }
  $json = new Services_JSON();
  echo $json->encode($result);
?>
