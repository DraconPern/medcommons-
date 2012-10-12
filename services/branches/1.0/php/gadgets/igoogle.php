<?
/**
 * Renders the main igoogle gadget
 */

require_once "utils.inc.php";
require_once "template.inc.php";

nocache();

$layout = template("igoogle.tpl.php");

echo $layout->set("content",template("igooglecontent.tpl.php"))
            ->fetch();

?>