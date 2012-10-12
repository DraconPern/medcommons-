<html>
  <body>
    <b>DICOM Order Notification</b>
    <hr/>
    <p>A DICOM Order has been received at <a href='<?=$applianceUrl?>'><?=$applianceUrl?></a>.</p>
    <br/>
    <b>HealthURL:</b>  <a href='<?=$applianceUrl?>/<?=$order->mcid?>'><?=$applianceUrl?>/<?=$order->mcid?></a>.</p>
    <br/>
    <b>Order Reference:</b>  <?=htmlentities($order->callers_order_reference)?>
    <br/>
    <b>Scan Date:</b>        <?=$order->scan_date_time?>
    <br/>
    <b>Comments:</b>        <?=htmlentities($order->comments)?>
    <br/>
  </body>
</html>


