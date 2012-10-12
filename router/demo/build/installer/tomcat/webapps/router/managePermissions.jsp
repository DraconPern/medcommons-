<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons CCR Sharing / Consents Widget

  This page renders a table showing the account level rights that the
  current user (account id) is sharing with others.  The table also
  allows them to update the rights.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:choose>
  <c:when test='${embedded}'><c:set var='layoutName' value='/embeddedGadget.jsp'/></c:when>
  <c:otherwise><c:set var='layoutName' value='/gadgetBase.jsp'/></c:otherwise>
</c:choose>
<s:layout-render name="${layoutName}" title="Consents">
  <s:layout-component name="head">
    <script type="text/javascript">
      var returnUrl = '${widgetReturnUrl}';
      var effect = 0;
      function saveConsents() {
        var qs = queryString(document.consentsForm);
        <c:if test='${not empty ccrIndex}'>
          qs += '&ccrIndex='+${ccrIndex};
        </c:if>
        qs += '&enc='+urlEncode(hex_sha1(getCookie('JSESSIONID')));
        execJSONRequest(document.consentsForm.action,qs, function(result) {
          if(result.status=="ok") {
            show('updateStatus');
            if($('shareLink')) {
              addElementClass($('shareLink'),'hidden');
              window.setTimeout( function() { removeElementClass($('shareLink'),'hidden');}, 4000);
            }
            if(effect % 3 == 0) {
              blindUp($('updateStatus'), {delay: 2.0, duration: 1.5});
            }
            else
            if(effect % 2 == 0) {
              squish($('updateStatus'), {delay: 1.5, duration: 0.5, fps:25});
            }
            else
            if(effect % 1 == 0) {
              fade($('updateStatus'), {delay: 2.0, duration: 0.5, fps:25});
            }
            effect++;
          }
          else {
            alert("A problem occurred while saving your consents:\r\n\r\n " + result.error);
            window.location.reload();
          }
        });
      }

      /**
       * Submit open id share form
       */
      function submitShare() {
        if(removeSpaces($('idsForm')['shareIds[0]'].value)=="") {
          alert("Please enter an OpenID URL to share with!");
          addElementClass($('idsForm')['shareIds[0]'],'invalidField');
          return;
        }
        execJSONRequest('ShareOpenID.action?ccrIndex=${ccrIndex}', queryString($('idsForm')), 
          function(r) {
            if(r.status == "ok") {
              alert("Successfully shared!");
              dlg.hide();
              doSimpleXMLHttpRequest('AccountSharing.action?table').addCallbacks(function(r) {
                  document.consentsForm.innerHTML = r.responseText;
                  broadCastHeight();
                }, genericErrorHandler);
            }
            else 
              alert("Sharing this CCR failed unexpectedly:\r\n\r\n"+ r.error);
          }
        );
      }

      var dialogDefaults = {
            width: '400px',
            fixedcenter:true,
            modal:true,
            visible:false,
            draggable:false,
            useShim: false,
            hideonsubmit: false
      };
    
      /**
       * Try to place the dialog in a sensible position
       */
      function positionDlg(dlg) {
          dlg.style.position = 'fixed';
          dlg.style.top = '80px';
          dlg.style.left = ( viewportSize().w / 2 - (parseInt(dialogDefaults.width) / 2) ) + 'px';
      }

      /**
       * Share Function
       */
      function shareOpenId() {
        yuiLoader().insert(function() {
          var Y = YAHOO;
          var dlg = new YAHOO.widget.SimpleDialog("dlg", Y.lang.merge(dialogDefaults,{ 
            buttons: [ { text: 'OK', isDefault: true, handler: submitShare }, { text: 'Cancel', handler: function() { dlg.destroy(); } } ]
          }));
          dlg.setHeader("Share with Open ID");
          dlg.setBody("<p>Share this CCR with another person by entering their OpenID below:</p><form id='idsForm' onsubmit='submitShare(); return false;'><table id='ids'><tbody id='idsBody'></tbody></table></form>");
          dlg.render(document.body);
          appendChildNodes($('idsBody'), idsRow());
          positionDlg($('dlg'));
          dlg.show();
          $('dlg').getElementsByTagName('input')[0].focus();
          window.dlg = dlg;
          broadCastHeight();
        });
      }

      /**
       * Share by Phone
       */
      function sharePhone() {
        yuiLoader().insert(function() {
          var Y = YAHOO;
          var dlg = new YAHOO.widget.SimpleDialog("phoneDlg", Y.lang.merge(dialogDefaults,{ 
            buttons: [ { text: 'OK', isDefault: true, handler: submitPhone }, { text: 'Cancel', handler: function() { dlg.destroy(); } } ]
          }));

          dlg.setHeader("Share by Phone");
          dlg.setBody("<p>Share this CCR with another person by entering their Phone Number below:</p>"
            + "<form id='phoneForm' onsubmit='submitPhone(); return false;'>"
            + "<table id='phoneTable'><tr>"
            + "<th>Name</th><td><input type='text' size='12' name='firstName'/> <input type='text' size='17' name='lastName'/></td></tr>"
            + "<th>Phone Number</th><td><input type='text' size='12' name='phoneNumber'/> <span style='vertical-align: middle; color: #888; font-style: italic; font-size:9px'>10 digits, xxx-xxx-xxxx</span></td></tr>"
            + "<tr><th>Carrier</th><td>"
            + "<select name='carrier'><option value='att'>AT&amp;T</option><option value='vrzn'>Verizon</option>"
            + "<option value='sprintpcs'>Sprint PCS</option><option value='tmob'>T-Mobile</option></select></td></tr>"
            + "</table></form>");

          dlg.render(document.body);
          positionDlg($('phoneDlg'));
          dlg.show();
          $('phoneDlg').getElementsByTagName('input')[0].focus();
          window.dlg = dlg;
          broadCastHeight();
        });
      }

      function validate(field,regex) {
        var passed = true;
        if(!regex.exec(field.value)) {
          log('field ' + field.name + ' failed validation');
          addElementClass(field,'invalidField');
          passed = false;
        }
        else
          removeElementClass(field,'invalidField');
        return passed;
      }

      var patientId = '${ccr.patientMedCommonsId}';
      function submitPhone() {

        log("Submitting phone for sharing");

        var f = $('phoneForm');
        f.phoneNumber.value = f.phoneNumber.value.replace(/[^0-9]/g,'');
        var validations = [[f.firstName,/^[A-Za-z]{2,60}$/],
                           [f.lastName, /^[A-Za-z]{2,60}$/],
                           [f.phoneNumber,/^[0-9]{10}$/]];

        var failed = filter(function(v) { return !validate(v[0],v[1]);}, validations);
        if(failed.length != 0) {
          failed[0][0].focus();
          failed[0][0].select();
          log(failed.length + " or more validations failed");
          return;
        }

        // Disable all the fields ...
        forEach(validations, function(v) { v[0].disabled = true; });
        f.carrier.disabled = true;

        execJSONRequest('ShareByPhone.action?shareAccountId='+patientId, queryString($('phoneForm')), 
          function(r) {
            if(r.status == "ok") {
            alert("This account has been successfully shared to the specified phone number.\r\n\r\n"+
              "The access code for this patient is: " + + r.accessCode + "\r\n\r\n" +
              "The recipient must use this code to authenticate when accessing the patient account.");
              dlg.hide();
              doSimpleXMLHttpRequest('AccountSharing.action?table').addCallbacks(function(r) {
                  document.consentsForm.innerHTML = r.responseText;
                  broadCastHeight();
                }, genericErrorHandler);
            }
            else 
              alert("Sharing this CCR failed unexpectedly:\r\n\r\n"+ r.error);
          }
        );
      }

      function idsRow() {
        return TR(null,TH('Open ID URL: '),TD(null,INPUT({name:'shareIds[0]','class':'oidinput',type:'text'})));
      }

      /**
       * Fill all practice entries with value for group
       */
      function fillConsentGroup(groupAcctId, rights) {
        log("filling group " + groupAcctId + " with rights " + rights);
        forEach(document.consentsForm.getElementsByTagName('tr'), function(tr) {
          if(tr.id && (("at_"+tr.id == groupAcctId) || tr.id.match( new RegExp(groupAcctId+".[0-9]{16}")))) {
            // select the value
            forEach(tr.getElementsByTagName('option'),function(o) {
              o.selected = ( o.value == rights );
            });
          }
        });
      }

      function centerDialog(d) {
        d.style.position='fixed';
        var w = '450px';
        d.style.top = '80px';
        d.style.left = ( viewportSize().w / 2 - (parseInt(w) / 2) ) + 'px';
      }

      function consentInfo(id) {
        var panel = new YAHOO.widget.Panel("consentInfo", { width:"450px", visible:false, draggable:false, close:true } );
        panel.setFooter("");
        if(id == 0) {
            panel.setHeader("PIN / Tracking Number Access");
            panel.setBody('<p>PIN Access means that a 5 digit PIN (Personal Identification Number) has been issued to allow '
                +'access to your account using a 12 digit tracking number as an access code.  Anyone who knows the Tracking Number '
                +'and PIN can access your account in accordance with the consent shown.</p>');
            panel.render(document.body);
            panel.show();
            centerDialog($('consentInfo'));
        }
        else  {
          doSimpleXMLHttpRequest('AccountSharing.action',{shareId:id,info:true}).addCallbacks(function(r) {
            panel.setHeader("Group / Application Information");
            panel.setBody(r.responseText);
            panel.render(document.body);
            panel.show();
            centerDialog($('consentInfo'));
          }, genericErrorHandler);
        }
      }

    </script>
    <style type="text/css">
      table tr.row1 {
        background-color: #f6f6f6;
      }
      #privacyWidget {
        padding-top: 1px;
        padding-left: -10px;
      }
      #privacyWidget table tr td {
        text-align: left;
      }

      table tr.practice td {
        color: white;
        font-weight: bold;
        padding: 0px 3px;
      }

      table tr.detailRow td {
        padding: 0px 3px;
      }

      #updateStatus { 
        position: absolute;
        top: -8px;
        right: 0px;
        width: 110px;
        color: #6094a5;
        font-size: 9px;
        background-color: #fff39c;
        display: none;
      }

      #updateStatusInner {
        text-align: center;
      }
      .rightLinks {
        float:right;
        margin: 0px 5px;
        vertical-align: middle;
      }
      #shareLink {
        position: relative;
        top: 1px;
      }
      a#shareLink:link,a#shareLink:visited, a#sharePhoneLink, a#sharePhoneLink:visited {
        color: white;
      }
      #shareLink img {
        vertical-align: middle;
      }

      .rightLinks img {
        vertical-align: middle;
      }
      input.oidinput {
        background: white url(images/openid-icon-small.gif) no-repeat 0px 0px; 
        padding-left: 20px;
        width: 270px;
      }
      a.groupInfoLink:link, a.groupInfoLink:visited {
        color: white;
      }
      .groupInfoLink {
        float: right;
        margin: 0px 5px;
      }
      .shareInfoTable tr th {
        width: 10em;
        text-align: right;
        padding-right: 30px;
      }
      #phoneTable th {
        text-align: right;
        padding-right: 10px;
      }
      input.invalidField {
        border: 2px solid red;
      }
    </style>
  </s:layout-component>
  <s:layout-component name="body">
    <script type="text/javascript">
      addLoadEvent(function() {
      /*
        if($('updateStatus')) {
          roundElement('updateStatus'); 
        }
      */
        window.setTimeout(yuiLoader,1000); // pre-cache
      });
    </script>
    <div id="privacyWidget">
      <div id="updateStatus"><div id="updateStatusInner">Consents updated succcessfully</div></div>
      <form name="consentsForm" action="AccountSharing.action?update">
        <p style='margin-top: 0px;'>Set privileges for accounts that can access this HealthURL below.</p>
          <jsp:include page="/permissionsTable.jsp"/>
      </form>
      <div style="display:none;">
      <fieldset>
        <legend>Get Records</legend>
         <form name="hipaaDownloadForm" action="PersonalBackup" method="post" target="_new">
            You can download all your records from MedCommons:
             <input type="hidden" class="text" size="5" name="storageId" value="${ccr.patientMedCommonsId}"/>&nbsp;&nbsp;&nbsp;
                 <input type="submit" value="Get My Records"/>
          </form>
        </fieldset>
      </div>
    </div>
  </s:layout-component>
</s:layout-render>
