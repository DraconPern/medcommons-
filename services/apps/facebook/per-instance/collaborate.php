<?php

require 'healthbook.inc.php';
require_once "collaboratepage.inc.php";


// *** basic start for all medcommons facebook programs
list ($facebook,$user) =fbinit(); 
// *** end of basic start

// jumping off to outer space
echo collaboration_page($facebook,$user);



?>