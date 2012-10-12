<%@ page language="java" contentType="text/html; charset=UTF-8"%>
<%@ include file="/taglibs.inc.jsp" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-html" prefix="html" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ page isELIgnored="false" %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%--
  MedCommons CCR Editor Form

  This page shows a form for editing CCRs that the the user may have loaded.

  The user may have any number of CCRs concurrently being edited in their 
  session.  Which one is displayed then?  Answer: the "active" CCR.  This
  CCR is set as "ccr" in the request scope and is also identified by index as
  "ccrIndex" in the request scope.   Outgoing links that interact with the CCR edited
  *must* send the "ccrIndex" parameter back to the server so that it knows which
  CCR to operate on.

  All outbound URLs from this page should route though an update action:

    *  the 'updateCcr.do' struts action
    *  any Stripes action extending CCRActionBean

  In order that modifications get sent you should navigate to these actions
  either by submitting the 'referralForm' or generating a queryString from it
  (see MochiKit function queryString(...) ).

  If you want to navigate to a custom URL use the stripes action UpdateCCR.action 
  and pass it a forward to where you want it to go afterwards.  This ensures that 
  all edits are saved, no matter where the user is going.

  @author Simon Sadedin, MedCommons Inc.
--%>
<c:set var="patient" value="${ccr.patient}"/>
<fmt:formatDate pattern="MM/dd/yyyy K:mm a" timeZone="${tz}" value="${ccr.createTime}" var="creationDateTime"/>
<fmt:formatDate pattern="MM/dd/yyyy" timeZone="${tz}" value="${ccr.createTime}" var="creationDate"/>
<c:set var="brandName"><mc:config property="BrandName" default="MedCommons"/></c:set>
<html>
  <head>
    <pack:style>
      <src>common.css</src>
      <src>ereferralLetter.css</src>
    </pack:style>
      
    <pack:script>
      <src>mochikit/MochiKit.js</src>
      <src>utils.js</src>
      <src>common.js</src>
      <src>ccreditor.js</src>
      <src>contextManager.js</src>
      <src>autoComplete.js</src>
    </pack:script>

    <%-- conditional CSS needed to fix IE-only layout problem for very narrow
         windows displaying wide sections (eg. medicines). --%>
    <!--[if IE]>
    <style type="text/css">
     <%-- don't know why but IE seems to calc position of box AFTER float space is removed,
          may be quirks mode behavior --%>
     #patientGivenLabel {
       left: 5px;
     }
     #patientFamilyLabel {
       left: 122px;
     }
    </style>
    <![endif]-->
    <style type="text/css">
      .yui-overlay, .yui-panel-container {
        position: relative;
      }
      .yui-skin-sam .yui-panel-container.shadow .underlay {
        position:relative;
      }
      #dialogDiv #orderForm_c {
        position:relative;
      }
      div.hd {
        height: 23px;
      }
    </style>

    <c:set var="currentCcrDOM" value="${ccr.JDOMDocument}" scope="request"/>
    <script type="text/javascript">
      <tiles:insert page="ccrDetailJavaScript.jsp"/>
    </script>
    <script type="text/javascript">

      var displayMode = '${ccr.displayMode}';
      var createDate = '${createDate}';
      var acctServer = '<mc:config property='AccountServer'/>';
      var secureServer = '<mc:config property='CommonsServer'/>';

      var enableBilling = <mc:config property='EnableBilling' default="false"/>;

      ce_add_server(acctServer+'/ce_signal.php');

      <mc:xnode bean='notificationForm' path='patientIds' forceList="true" name='patientIds'/>
      var patientIds = [
        <c:forEach items="${patientIds}" var="patientId" varStatus="status">
          <c:if test='${status.index > 0}'>,</c:if>
          { type: '<mc:xvalue bean="patientId" path="idType"/>', value: '<mc:xvalue bean="patientId" path="idValue"/>' }
        </c:forEach>
      ];

      <c:set var="patientMedCommonsId"><mc:medcommonsId><mc:xvalue bean="notificationForm" path="patientMedCommonsId"/></mc:medcommonsId></c:set>
      <jsp:include page="accountDocumentsJavascript.jsp"/>
      var remoteAccessAddress = '<mc:config property="RemoteAccessAddress"/>';
      var ccrCreateDateTime = '${creationDateTime}';
      var patientMode = ${desktop.patientMode};
      var ccrGuid = null;
      <c:if test='${ ! empty ccr.guid }'>
        ccrGuid = '${ccr.guid}';
        window.parent.ce_signal('openCCR',ccrGuid,remoteAccessAddress);
      </c:if>
      <%--
        General handling for tracking number success/fail
      --%>


      var medcommonsIdKnown = false;
      <c:if test='${fn:length(ccr.patient.medcommonsId) > 0}'>
      medcommonsIdKnown = true;
      </c:if>

      var ccrPurpose = '<mc:xvalue bean="notificationForm" path="ccrPurpose" jsEscape="true"/>';

      var commonsUrl = '<mc:config property="CommonsServer"/>';

      var cxp2Protocol = '<mc:config property="RemoteProtocol"/>';
      var cxp2Port =  '<mc:config property="RemotePort"/>';
      var cxp2Host =  '<mc:config property="RemoteHost"/>';
      var cxp2Path =  '<mc:config property="CXP2Path"/>';

      <c:set var='isCurrentCCR' value='${not empty currentCcrGuid and (currentCcrGuid == ccr.guid)}'/>
      var isCCCR = ${isCurrentCCR};

      var mergeTool = ["Merge into Current CCR","mergeIntoCurrentCCR();"];
      var saveAsFixedTool = ["Save as CCR Tab","saveAsFixed();"];
      var createReplyTool = ["Create Reply","createReplyCCR();"];
      var newOrderTool = ["Create Order","createOrderCCR();"];
      var editExternally;
      if (isHealthBookInstalled()){
       	editExternally = ["Edit With HealthBook","editCCRExternally();"];
      }
      else{
      	editExternally = ["Install HealthBook Editor","installHealthBook();"];
      }
      var importTool = 
        [{ text: 'Import', submenu: { id: 'import', itemdata: [ 
            {text: 'Voucher', onclick: {fn: importVoucher}},
            {text: 'Account', onclick: {fn: importAccount}},
            {text: 'CCR', onclick: {fn: importCCR}},
            {text: 'Upload Account Files', onclick: {fn: uploadAccountFiles}}
           ]}}];

      var defaultTools = [
            ["View CCR","viewCcr();"],
            newOrderTool,
            ["Edit as New CCR","sendAsNew();"],
            editExternally,
            saveAsFixedTool,
            createReplyTool,
            mergeTool,
            importTool,
            ["Print this Page","print();"],
            [CCR_ATTACHMENTS_MENU, "Add PDF Document",'showAddDocument();',"window.contents.addDocumentWindow = window.open('blank.html','addDocument','scrollbars=1,width=520,height=220,resizable=1')"],
            [CCR_ATTACHMENTS_MENU, "Add Web Reference","addWebReference();"],
            [CCR_SECTIONS_MENU, "Patient Demographics", "showDemographicsForm()"]
          ];
 
      addSectionMenus(defaultTools);

      var emergencyCcrGuid = '${desktop.accountSettings.emergencyCcrGuid}';
      for(var t in accountDocuments) {
        var d = accountDocuments[t];
        if(d.guid == ccrGuid) {
            tabText = documentTypes[d.type];
            window.parent.setTabText(null,tabText);
        }
      };

      var accountSettings = ${desktop.accountSettings.JSON};
      <c:if test='${not empty patientAccountSettings}'>
      var patientSettings = ${patientAccountSettings.JSON};
      </c:if>

      tabText = window.parent.getTabText();

      var tools = new Array();
      forEach(defaultTools, function(o){tools.push(o);});
      var emergencyCcrTool = ["Set as Emergency CCR", "setEmergencyCCR();"];


      if(!window.parent.currentTab.secondaryTab)
        window.parent.setTabIcon(null, '<img class="tabIcon" title="Switch to View Mode" style="position: relative; top: 1px; left: 3px;" src="images/tancycle.gif" onmouseout="this.src=\'images/tancycle.gif\'" onmouseover="this.src=\'images/redcycle.gif\'"/>');

      var closeTab = false;
      <c:if test='${!empty actionBean and mc:has(actionBean,"removeTab")}'>closeTab = ${actionBean.removeTab};</c:if>

      var cleanPatient = ${! empty param['clean'] or ! empty cleanPatient};
      var registry = null;
      var groupId = null;
      var changeNotifications = null;
      var storageMode = '${ccr.storageMode}';
      var storageId = '${ccr.storageId}';
      var patientMedCommonsId = "${patientMedCommonsId}";
      var auth='${desktop.authenticationToken}';

      <c:set var="writeable" value="false"/>
      <mc:hasPerms accountId="${patientMedCommonsId}" rights="W"><c:set var="writeable" value="true"/></mc:hasPerms>
      var writeable = ${writeable};

      <c:if test='${ccr.storageMode == "FIXED" and ! empty patientMedCommonsId}'>
        tools.push( [ "Hide CCR", "hideCCR()" ] );
      </c:if>

      var setAsCurrentCCRTool = ["Set as Current CCR", "saveCurrentCcr()"];

      var ccrPurposes = "<c:out value='${brandName}'/> Notification for [Recipient],Private Communication,Request For Consult,Request For Procedure,Request for Service,Request for Encounter,Request for Authorization,Request for Medical Device Or Product,Request for Medication,Request for Immunization,For Patient Use".split(/,/);
      function init() {
        <c:if test='${displayOrderForm}'>
          showOrderForm(null,saveNewOrder);
          <c:set var="hideDefaultContents" value="true"/>
        </c:if>
        parent.currentTab.url = 'updateCcr.do';
        forEach(findChildElements($('ccrTable'), [ '.insecureFormInput']), function(i) {
            connect(i,'onfocus',function(e) { privacyHighlight(e.target()) });  
        });
        
        initTabInfo(${ccrIndex},'${ccr.logicalType}','${ccr.storageMode}','edit');

        updateFragment();

        if(cleanPatient) {
          cleanTabs();
        }

        if(!empty(parent.currentTab.ccr.documentType)) {
          log("setting tab text for " + documentTypes[parent.currentTab.ccr.documentType]);
          parent.setTabText(null,documentTypes[parent.currentTab.ccr.documentType]);
        }

        if(ccrGuid != null) {
          if(window.parent.getTabText().match(/\*$/)) {
            window.parent.setTabText(null,tabText);
          }
        }

        displayPatientId();
        <%-- if this is a new ccr, hide the received tab --%>
        if(${ccr.newCcr}) {
          window.parent.hideTab('tab6');
          if(ccrGuid == null) {
            window.parent.ce_signal('newCCR',ccrGuid,remoteAccessAddress);
          }
        }

        setPatientTitle('${mc:jsEscape(ccr.patientGivenName)}', '${mc:jsEscape(ccr.patientFamilyName)}'); 

        <%-- if there are pending change notifications, show them --%>
        <c:if test='${isCurrentCCR and  !empty changeNotifications}'>
         changeNotifications = ${changeNotifications};
         if(changeNotifications.length>0)
           showChangeNotifications();
        </c:if>
        <c:if test='${isCurrentCCR}'>
          window.parent.currentCcrTab = window.parent.currentTab;
          tabText = 'Current CCR';
        </c:if>

        window.activeForm=document.referralForm;

        var purposes = el('ccrPurpose');
        var selectedPurpose = purposes.selectedIndex;
        if(selectedPurpose == 0) {
          if(ccrPurpose != '') {
            purposes.options[purposes.options.length]=new Option(ccrPurpose);
            purposes.selectedIndex = purposes.options.length-1;
          }
        }

        roundElement('privacyWarning');

        initPinFields();

        tools.push(setAsCurrentCCRTool);

        var f = document.referralForm;
        for(i=0; i<f.elements.length; i++) {
          var e = f.elements[i];
          if((e.id != 'termsOfUseCheckBox') && (e.id != 'assignedPin')) {
            connect(e,'onchange',onFieldChange);
          }
        }
        headerSync();
        addToCallStack(f.patientDateOfBirth, 'onchange', headerSync);
        <c:if test='${! empty desktop.accountSettings.registry}'>
        fetchRegistryPatients();
        registry = '${desktop.accountSettings.registry}';
        </c:if>

        groupId = '${desktop.accountSettings.groupId}';
        groupName = '${mc:jsEscape(desktop.accountSettings.groupName)}';

        if( !empty(accId) && (displayMode != "eccr")) {
          tools.push(emergencyCcrTool);
          <bean:define id="acctTns" name="desktop" property="accountTrackingNumbers" type="java.util.Collection"/>
          <c:set var="tn" value="${ccr.trackingNumber}" scope="page"/>
          <%if(!acctTns.contains(pageContext.getAttribute("tn"))) { %>
            if(groupId) {
              tools.push(["Add to my Worklist","addToAccount();"]);
            }
            <c:choose>
              <c:when test='${desktop.accountSettings.vouchersEnabled}'>
                else {
                  tools.push(["Add to my Patient List","addToAccount();"]);
                }
              </c:when>
              <c:otherwise>
                else {
                  tools.push(["Add to my Account","addToAccount();"]);
                }
              </c:otherwise>
            </c:choose>
          <%}%>
        }

        addContextManager(tools);

        window.parent.setTools(tools);
        parent.enableTool(createReplyTool,storageMode == 'FIXED');
        setWriteable(writeable);

        fetchToDir();
        fetchPatientCCRs();
        resolveToEmail();

        parent.addPatientHeader(this.getCcr());

        <c:if test='${ccr.newCcr or showDemographics}'>
          if(canEditDemographics()) 
            showDemographicsForm(function() {document.referralForm.patientGivenName.focus();} );
          else
            hidePatientDemographics();
          <c:set var="hideDefaultContents" value="true"/>
          <c:if test='${ccr.logicalType == "CURRENTCCR"}'>
            tabText = 'Current CCR';
          </c:if>
        </c:if>

        connect('toEmail','onblur',resolveToEmail);
        updatePin();
        updateCCRFrameSize();

        connect(window,'ccrSaved',updateStorageId);
        connect(window,'ccrSaved',updateCCRLink);
        connect(window,'ccrSaved',updateTools);
        connect(window,'ccrSaved', function() { $('closePatientCardButton').value = 'Close'; });
        connect(window,'ccrModified', function() { $('closePatientCardButton').value = 'Save'; });

        if(ccrGuid == null) {
          setModified();
        }

        <c:if test='${resetTabText}'>
          connect(window,'ccrSaved', function(){parent.tabText = tabDateTitle(ccr); parent.resetTabText(); });
        </c:if>

        static_init();
      }

      window.parent.setAdvertisedCcr('${desktop.ownerMedCommonsId}','${desktop.accessPin}');

      <c:set var="editmode" value="true"/>

      <%-- The guid that was used (if any) to load this page --%>
      var loadedGuid = '${ccr.guid}';

      var hasUnvalidatedAttachments = false;
      var accId = null;
      <c:if test="${desktop.hasAccount}">accId = '${desktop.ownerMedCommonsId}';</c:if>

      ce_connect('osirixOpen',function(g) {
        if(ccrGuid == null) {
          if(!confirm("Your new CCR has been uploaded and is available for viewing.\r\n\r\nYou have unsaved changes in this CCR.  If you continue, they will be lost.\r\n\r\nContinue and view new CCR?"))
            return;
        }
        window.location='access?load&g='+g;
      });
    </script>
  </head>
  <mc:xnode bean='notificationForm' path='patientAge' name='patientAge'/>
  <body onload="init();" style="height: 100%;" class="yui-skin-sam" style="z-index: 0;">

  <%-- Container for "floating" dialogs --%>
  <div id="dialogDiv" <c:if test='${!displayOrderForm}'>class="invisible"</c:if> > &nbsp;</div>

  <div id="notopbuttons" class="topbuttonmsg"
       <c:if test='${writeable or ccr.storageMode == "FIXED"}'>style="display:none;"</c:if> >
    <s:layout-render name='/greenbox.jsp'>
      <s:layout-component name='content'><p><img src='images/padlock.gif'/>&nbsp;Save is Disabled: You do not have Consent to modify this Patient's PHR.</p></s:layout-component>
    </s:layout-render>
  </div>
  <div id="nobuttonsfixed" class="topbuttonmsg"
       <c:if test='${ccr.storageMode != "FIXED"}'>style="display:none;"</c:if> >
    <s:layout-render name='/greenbox.jsp'>
        <s:layout-component name='content'><p><img src='images/padlock.gif'/>This is a saved CCR. Use the Edit as New CCR tool to edit and reply.</p></s:layout-component>
    </s:layout-render>
  </div>
  <div id="privacyWarning" style="z-index: 2">
     <table style="margin-left: 5px;" class="privacyTable" cellpadding="2" cellspacing="0" border="0">
     <tr>
       <td valign="top"><img src="images/padlock.gif"/></td>
       <td><b>Warning!</b> This information will appear in unsecure mail notifications and should not include private information.</td>
     </tr>
     </table>
  </div>
  <form name="referralForm" id="referralForm" method="post" onsubmit="return goReferralEmail();" action="/router/confirmReferral.do" target="referralEmail">
    <div id="referralFormContainer" class="dialog <c:if test='${hideDefaultContents}'>hidden</c:if>">
      <s:layout-render name='/brownbox.jsp' title='Notifications'>
        <s:layout-component name='content'>
            <input type="hidden" name="initialSeriesGuid" value=""/>
            <input type="hidden" name="initialSeriesIndex" value=""/>
            <input type="hidden" name="updateIndex" value="${ccrIndex}"/>
            <input type="hidden" name="newAcct" value="${ccr.newCcr and not empty desktop.accountSettings.registry and !desktop.anyPatientHasAccountId}"/>
            <input type="hidden" id="destAcctId" name="destAcctId" value=""/>
            <input type="hidden" id="restore" name="restore" value=""/>
            <input type="hidden" id="assignedPin" name="assignedPin" value="${desktop.replyPin}"/>

            <%-- this is a dummy field: it is here only to support autocomplete on tabs --%>
            <input type="hidden" name="patientPrevCCRs" value=""/><input type="hidden" name="patientLaterCCRs" value=""/>
            <c:set var='accountServer'><mc:config property='AccountServer'/></c:set>
            <c:url var="ccrLinkUrl" value="${accountServer}/../ccrredir.php">
              <c:param name="guid" value="${ccr.guid}" />
            </c:url>
            <table id="ccrTable" class="ccrTable" cellpadding="0" cellspacing="0">
            <s:layout-render name='/notificationsTable.jsp' readonly='true'/>
            <tr ><th height="30">&nbsp;</th>
                 <td class="smallltrtxt" align="right">
                   <div id="termsOfUseLabel" style="display:none;">
                     <html:checkbox styleId="termsOfUseCheckBox" name="notificationForm" property="termsOfUse"/><span  style="position: relative; top: -2px">I accept the <a href="http://www.medcommons.net/termsofuse.html" target="termsOfUse" title="Click here to read the Terms of Use">Terms of Use</a></span>
                    </div>
                 </td>
            </tr>
            <tr><th>COMMENT<br/><br/></th>
                <c:set var="purposeText"><mc:xvalue bean="notificationForm" jsEscape="false" path="purposeText" /></c:set>
                <c:set var="rows">${mc:rowsIn(purposeText,80)+1}</c:set>
                <td class="ltrtxt">
                    <textarea 
                      id="purposeText"
                      rows="${rows}"
                      style="font-weight: bold; height: ${12*rows + 2}px;"
                      cols="80"
                      class="formInput"
                      onfocus="highlight(this)"
                      onblur="unhighlight(this)" name="purposeText"
                      onkeyup="adjustTextAreaHeight(this);"
                      >${purposeText}</textarea>
                      <br/>
                      <br/>
                </td>
            </tr>
            <c:choose>
              <c:when test='${fn:length(ccr.seriesList) > 1}'>
                <tr height="20">
                  <th class="leftnav" id="referencesLabel" style='position: relative;'><span>REFERENCES</span></td>
                  <td class="ltrtxt" rowspan="2">
                        <div id="referencesTable">
                          <jsp:include page="referenceTable.jsp"/>
                        </div>
                  </td>
                </tr>
              </c:when>
            </c:choose>
            <tr>
              <th rowspan="2" valign="top">
              &nbsp;
                </th>
                <%-- note no td here because of rowspan above --%>
            </tr>
          </table>
        </s:layout-component>
      </s:layout-render>
    </div> <%-- end referralFormContainer --%>

    <div id="patientCard" class="dialog invisible expanded" style="z-index: 0;">
    <s:layout-render name='/brownbox.jsp' title='Patient Demographics'>
      <s:layout-component name='content'>
          <div class="bd">
              <h4 id="patientNameLabel">Name</h4>
              <span id="patientNameBlock">
                 <mc:xinput styleClass="formInput" title="First Name" style="width: 100px;" onfocus="highlight(this);" bean="notificationForm" name="patientGivenName"/>
                 <span id="patientMiddleNameBlock" style="display: none;">&nbsp;
                 <mc:xinput styleClass="formInput" title="Middle Name" style="width: 40px;" onfocus="highlight(this);" bean="notificationForm" name="patientMiddleName"/>
                 </span>
                 &nbsp;
                 <mc:xinput styleClass="formInput" title="Family Name" style="width: 90px;" onfocus="highlight(this);" bean="notificationForm" name="patientFamilyName"/>
                 &nbsp;
                 <input type="text" name="ageSex" id="pcAgeSex" class="formInput" title="Age/Sex" style="width: 35px;" readonly="true" value=""/>
                 &nbsp;
                 <input type="text" name="headerDob" id="pcHeaderDob" class="formInput" title="Patient Date of Birth" style="width: 75px;" readonly="true" value=""/>
                 &nbsp;
                 <br id="patientIdBreak"/>
                 <h4 id="patientIdHeading">ID</h4>
                 <input name="patientId" class="formInput" title="Patient ID" onfocus="highlight(this);" 
                        style="width: 140px; position: relative;" readonly="true" value='${patientMedCommonsId}'/>
                 <span id="patientIdButtons">
                   <img id="patientIdUp"
                      onclick="editPatientId();"
                      src="images/greypen.gif"
                      title="Edit Patient ID"/>
                   <img id="patientIdDown"
                     onclick="patientIdDownClick();"
                     src="images/black_arrow_down.gif"
                     title="Choose Patient Id"
                     />
                 </span>
                 <span id="headerMcId" style="position:relative;">
                 </span>
              </span>
              <div id="patientIdLabel" style='clear:both;'>&nbsp;</div>
              <span id="patientLabels" class="invisible">
                <span id="patientGivenLabel">Given</span>
                <span id="patientFamilyLabel">Family</span>
              </span>
           <div id="patientCardInner">
           <c:set var='dob'><mc:xvalue bean='notificationForm' path='patientExactDateOfBirth'/></c:set>
           <c:set var='approxDob'><mc:xvalue bean='notificationForm' path='patientApproxDateOfBirth'/></c:set>
           <h4>DOB</h4>
             <input name="patientDateOfBirth" class="formInput" onfocus="highlight(this);" size="28" style=""
              title="Enter dates as either exact dates in mm/dd/yyyy or yyyy-mm-dd hh:mm form or as approximate dates in free text form"
              value="<c:choose><c:when test='${dob != ""}'>${dob}</c:when><c:when test='${approxDob != ""}'>${approxDob}</c:when><c:otherwise>Unknown</c:otherwise></c:choose>"/>
              <span class="smallPatientLabel">&nbsp;(example: mm/dd/yyyy)<br/></span>
           <br style="clear:both;"/>
           <h4>Age</h4>
           <input name="patientAge" class="formInput" onfocus="highlight(this);" size="4" style="text-align: center" value="${patientAge.textTrim}"/>
            &nbsp;<b class='patientCardHeading'>Sex</b>&nbsp;
            <mc:xinput bean="notificationForm" styleClass="formInput" styleId="patientGender" onfocus="highlight(this);" name="patientGender"/>
            <div id='genderACContainer'></div>
           <br style="clear:both;"/>
           <h5>Email&nbsp;&nbsp;</h5>
           <mc:xinput styleClass="formInput" style="width: 263px;" onfocus="highlight(this);" bean="notificationForm" name="patientEmail" />
           <br style="clear:both;"/>
           <h5>Street&nbsp;&nbsp;</h5>
           <mc:xinput styleClass="formInput" style="width: 263px;" onfocus="highlight(this);" bean="notificationForm" name="patientAddress1" />
           <br style="clear:both;"/>
           <h5>City&nbsp;&nbsp;</h5>
           <mc:xinput styleClass="formInput" onfocus="highlight(this);" bean="notificationForm" name="patientCity" />
           &nbsp;<span class="smallPatientLabel">State</span>
           <mc:xinput styleClass="formInput" style="width:30px;" onfocus="highlight(this);" bean="notificationForm" name="patientState" />
           &nbsp;<span class="smallPatientLabel">ZIP</span>
           <mc:xinput styleClass="formInput" style="width:40px;" onfocus="highlight(this);" bean="notificationForm" name="patientPostalCode" />
           <br style="clear:both;"/>
           <h5>Country&nbsp;&nbsp;</h5>
           <mc:xinput styleClass="formInput" style="width:60px;" onfocus="highlight(this);" bean="notificationForm" name="patientCountry" />
            <span style="position: absolute; left: 150px;">
              <span class="smallPatientLabel">Phone</span>&nbsp;<mc:xinput styleClass="formInput" onfocus="highlight(this);" bean="notificationForm" name="patientPhoneNumber"/>
            </span>
            <input type="button" id='closePatientCardButton' onclick='demographicsOK()' value="Close"/>
          </div> <%-- end patientCardInner --%>
        </div> <%-- end bd --%>
        </s:layout-component>
      </s:layout-render>
     </div> <%-- end patientCard --%>
    <br/>
   </form>

    <%--
    <div id="footerLogo"><img src='images/poweredbymc.png'/></div>
    --%>

    <div id="acdiv" ></div>

    <div id="addWebReferenceDiv" class="popupForm">
      <h4>Add Web Reference</h4>
      <form name="addWebReferenceForm" onsubmit="return false;">
        <label for="webRefUrl">Enter the URL of the document here:</label>
        <br/><br/>
        &nbsp;&nbsp;&nbsp;<input type="text" name="webRefUrl" size="50" value=""/>
        <input type="button" value="Add" onclick="addWebRefSubmit()"/>
        <input type="button" value="Cancel" onclick="addWebRefCancel();"/>
      </form>
    </div>

    <div id="editPatientIdDiv" class="popupForm">
      <h4>Edit Patient ID</h4>
      <form name="editPatientIdForm" onsubmit="return false;">
        <label for="patientId">ID:</label>&nbsp;&nbsp;<input type="text" name="patientId" size="30" value=""/>
        <br/>
        <label for="patientId">Type:</label>&nbsp;&nbsp;<input type="text" name="patientIdType" size="30" value=""/>
        <br/>
        <br style="height: 3px;"/>
        <label>&nbsp;</label>&nbsp;&nbsp;
        <input type="button" value="Save" onclick="editPatientIdSubmit()"/>
        <button name="delete" onclick="deletePatientIdSubmit();">Delete<img style="vertical-align: middle; margin: 3px 2px;" src="images/delete.gif"/></button>
        <input type="button" value="Cancel" onclick="hide('editPatientIdDiv');"/>
      </form>
    </div>

    <tiles:insert page='familyhistoryEditor.jsp'/>

    <tiles:insert page='vitalsignsEditor.jsp'/>

    <tiles:insert page='proceduresEditor.jsp'/>

    <tiles:insert page='medicationsEditor.jsp'/>

    <jsp:include page="sendStatus.jsp"/>
    
    <div id="mergeStatusDiv" class="popupForm">
      <h3 id="mergeStatusTitle">Merging to Current CCR&nbsp;&nbsp;</h3>
      <table id="statusTable" style="margin-left: 10px;">
          <tr><th>Status</th><td id="mgStatus"></td></tr>
          <tr><th>Description</th><td id="mgDesc"></td></tr>
          <tr><th>&nbsp;</th><td>&nbsp;</td></tr>
          <tr><th>&nbsp;</th><td><input type="button" value="OK" style="width: 80px" onClick="hide('mergeStatusDiv');"></td></tr>
      </table>
    </div>

    <jsp:include page='changeNotificationDiv.jsp'/>

    <%-- results from Send are returned here --%>
    <iframe name="resultsframe"
            id="results"
            onload="resultReady();"
            src="blank.html">You need support for frames to view this page</iframe>

    <%-- CCR Frame holds the standard stylesheet rendering of the CCR --%>
    <iframe name="ccrframe"  id="ccrframe"
            style="border-style: none; overflow: auto; border-top-style: solid;  border-top-color: #ccc;  border-top-width: 2px;" 
            frameborder="0"
            src="DisplayCCR.action?ccrIndex=${ccrIndex}">You need support for frames to view this page</iframe>
    <script src="yui-2.6.0/yuiloader/yuiloader-min.js"></script> 
  
    <%-- <script type="text/javascript" src="/ibug4j/ibug.js"></script> --%>
  </body>
</html>
<%-- if cleanPatient flag was set, remove it --%>
<c:remove var="cleanPatient"/>
