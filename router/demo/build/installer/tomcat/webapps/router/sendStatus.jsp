<div id="sendStatusDiv" class="popupForm">
  <h3 id="sendStatusTitle">Send Status</h3>
  <table id="statusTable" style="margin-left: 10px;">
      <tr><th>Status</th><td id="ssStatus">Sending</td></tr>
      <tr><th>Tracking Number</th><td id="ssTrackingNumber">-</td></tr>
      <tr><th>PIN</th><td id="ssPin">-</td></tr>
      <tr id="ssAcctCreatedRow" ><th id="ssAcctCreatedLabel">&nbsp;</th><td id="ssAcctCreated">&nbsp;</td></tr>
      <tr><th>&nbsp;</th><td><input type="button" value="Print" style="width: 80px" onClick="printResults()"/>
        &nbsp;<input type="button" value="OK" style="width: 80px" onClick="hide('sendStatusDiv');el('ssAcctCreated').innerHTML=' '; el('ssAcctCreatedLabel').innerHTML=' ';"></td></tr>
  </table>
  <div id="ssError">&nbsp;</div>
</div>
