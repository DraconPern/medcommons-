<?php
  
  /*
   * Some notes:
   * 1.  Try not to leak any information about how many accounts an email has,
   *     or even if a particular email has any accounts at all.
   */

require 'template.inc.php';
require_once 'mc.inc.php';

$t = template( 'uploadwww.tpl.php');



if (isset($_REQUEST['next']))
  $t->esc('next', $_REQUEST['next']);

echo $t->fetch();

?>
