<html>
    <head>
        <title>New Order</title>
        <meta name="layout" content="timc" />
        <script type='text/javascript'>
        function order() {
            window.open("about:blank", "popup");
            window.location.reload();
        }
        </script>
    </head>
    <body>
      <h1>Sample Order Form</h1>
      <form action='order' method='POST' target='popup' onclick='order'>
      <%if(params.groupAccountId) { %>
      <input type='hidden' name='groupAccountId' value='${params.groupAccountId?:params.group_account_id}'/>
      <% } %>
      <table id='dicomOrderForm'>
        <tr><th>Reference *</th><td><input type='text' name=callers_order_reference value = '<%=System.currentTimeMillis()%>' size=100 /></td></tr>
        <tr><th>Patient ID *</th><td><input type='text' name='patient_id' value = '19965428' size=100 /></td></tr>
        <tr><th>Callback</th><td><input type='text' name=status_callback value = '' size='100' /></td></tr>
        <tr><th>Sender Name</th><td><input type='text' name='sender_name' value = '' size=100 maxlength="255"/></td></tr>
        <tr><th>Sender Email</th><td><input type='text' name='sender_email' value = '' size=100 maxlength="255" /></td></tr>
        <tr><th>Accession #</th><td><input type='text' name='accession_number' value = '' size=100 maxlength="60" /></td></tr>
        <tr><th>Facility</th><td><input type='text' name='facility' value = 'Big Hospital' size=100 maxlength="255" /></td></tr>
        <tr><th>Due Date</th><td><input type='text' name='due_date' value = '03/06/2009' size=100 /></td></tr>
        <tr><th>Due Time</th><td><input type='text' name='due_time' value = '09:00' size=100 /></td></tr>
        <tr><th>Scan Date</th><td><input type='text' name='scan_date' value = '03/04/2009' size=100 /></td></tr>
        <tr><th>Scan Time</th><td><input type='text' name='scan_time' value = '09:00' size=100 /></td></tr>
        <tr><th>Email</th><td><input type='text' name='email' value = 'Y' size=100 /></td></tr>
        <tr><th>Baseline</th><td><input type='text' name='baseline' value = 'Y' size=100 /></td></tr>
        <tr><th>Protocol</th><td><input type='text' name=protocol_id value = 'FullBodyDiag' size=100 /></td></tr>
        <tr><th>Modality</th><td><g:select name='modality' from='${["PT","CT","MR"]}'/></td></tr>
        <tr><th>Source</th><td><input type='text' name='source' value='MedCommons' /></td></tr>
        <tr><th>Anonymization Profile</th><td><input type='text' name='anon_profile' value = '' /></td></tr>
        <tr><th>Comments</th><td><textarea name='order_comments' rows='6' cols='42' >Handle with Special Care</textarea></td></tr>
        <tr><th>Custom 1</th><td><input type='text' name='custom_00' value='Some custom text'/></td></tr>
        <tr><th>Label 1</th><td><input type='text' name='custom_00_label' value='The Custom Text'/></td></tr>
        <tr><th>Next</th><td><input type='text' name='next' value='' size='100'/></td></tr>
        <tr><td>&nbsp;</td><td><input type='submit' name='SUBMIT' value='Enter Order' /></td></tr>
        </table>
      </form>
    </body>
</html>
