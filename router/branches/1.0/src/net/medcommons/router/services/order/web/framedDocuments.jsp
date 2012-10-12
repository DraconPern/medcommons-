<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons Patient Info Display 

  This single large gadget renders a number of smaller gadgets showing 
  information about the current patient.

  @author Simon Sadedin, MedCommons Inc.
--%>

<c:if test='${!empty ccr}'>
  <c:set var="ccrXml" value="${ccr.JDOMDocument}"/>
  <c:set var="sex" value="${ccr.patientGender=='Male'?'M': ccr.patientGender=='Female'?'F':''}"/>
  <c:set var="ageSex"><mc:xvalue bean="ccrXml" path="patientAge"/> ${sex}</c:set>
  <c:set var='headerTitle'>Documents - <mc:xvalue bean="ccrXml" path="patientFamilyName"/> <mc:xvalue bean="ccrXml" path="patientGivenName"/> <c:if test='${! empty ageSex}'>- ${ageSex}</c:if></c:set>

  <s:layout-render name='/healthURL.jsp' mcid='${ccr.patientMedCommonsId}'/>

  <c:set var='rightLinks'>
    <a href="${hurl}" onfocus="blur();" 
      title="Open/Edit CCR" 
      target="ccr" onclick="return openCcrWindow('${hurl}');"><img src='images/hurl.png'/></a>
      &nbsp;<a href="${hurl}" onfocus="blur();" title="Open/Edit CCR" target="ccr" onclick="return openCcrWindow('${hurl}');">HealthURL</a>

    &nbsp;
    <c:set var='formsLink' value='?framedForms=true'/>
    <a href="${formsLink}" onfocus="blur();" title="Forms" style='position: relative; top: 1px;'><img src='images/form-icon.png'/></a>
      <a href="${formsLink}" onfocus="blur();" title="Forms">Forms</a>

    &nbsp;
    <c:set var='activityLink' value='?combined=true'/>
    <a href="${activityLink}" onfocus="blur();" title="Activity"><img src='images/activity-icon.png'/></a>
      <a href="${activityLink}" onfocus="blur();" title="Activity">Activity</a>
  </c:set>

  <c:set var='gadgets'>
  <pack:style>
    <src>/yui-2.6.0/datatable/assets/skins/sam/datatable.css</src>
    <src>/yui-2.6.0/paginator/assets/skins/sam/paginator.css</src>
  </pack:style>

  <pack:script>
      <src>yui-2.6.0/utilities/utilities.js</src>
      <src>yui-2.6.0/datasource/datasource-min.js</src>
      <src>yui-2.6.0/datatable/datatable-min.js</src>
      <src>yui-2.6.0/logger/logger-min.js</src>
      <src>yui-2.6.0/paginator/paginator-min.js</src>
      <src>mochikit/Base.js</src>
      <src>mochikit/Format.js</src>
      <src>mochikit/DateTime.js</src>
  </pack:script>
  <script type="text/javascript">
  // var myLogReader = new YAHOO.widget.LogReader();
  </script>

  <style type='text/css'>
    #docsContent {
      margin: 0px 1em 1em 1em;
    }
    #docTable table a img {
       position: relative;
       top: 2px;
    }

    #docTable table {
      background-color: white;
      border-collapse: collapse;
      width: 100%;
    }
  </style>

  <div id='docsContent'>
    <p>The table below shows documents stored in this account.</p>
    <div id='docTable' class='yui-dt'>
    </div>
    <div id='pag-below'></div>
  </div>

  <script type='text/javascript'>
    var documentTypes = {
      "application/pdf" : 'PDF',
      "image/jpg" : 'JPEG Image',
      "image/pjpeg" : 'JPEG Image',
      "image/jpeg" : 'JPEG Image',
      "image/gif" : 'GIF Image',
      "text/x-cdar1+xml" : 'CDA Document',
      "application/x-ccr+xml" : 'CCR',
      "application/x-medcommons-ccr-history" : 'CCR History Document'
    };
    var documentImages = {
      "application/pdf" : 'images/pdf_icon.gif',
      "image/jpg" : 'images/image_icon.gif',
      "image/pjpeg" : 'images/image_icon.gif',
      "image/jpeg" : 'images/image_icon.gif',
      "image/gif" : 'images/image_icon.gif',
      "text/x-cdar1+xml" : 'images/text_icon.gif',
      "application/x-ccr+xml" : 'images/text_icon.gif',
      "application/x-medcommons-dicom-study" : 'images/openccrp.png'
    };

    function documentLink(rec,html,title) {
     return '<a href="streamDocument.do?dl=true&guid='+rec.getData().sha1 +'&accid='+rec.getData().storageId+'" title="'+title+'">'+html+'</a>';
    }

    var documents = <mc:json src='${actionBean.documents}'/>;
    var dataSource = new YAHOO.util.LocalDataSource(documents);
    dataSource.responseSchema = { 
      fields: [{ key: 'documentName'}, {key: 'creationDate'}, {key: 'length'}, {key: 'contentType'}, {key: 'sha1'}, {key:'storageId'}, {key: 'guid'}] 
    };
    var columnDefs = [
         {key:"documentName",  label:"Name", sortable: true, formatter: function(cell, rec, col, data) {
            var docName = '<No Name Recorded>';
            var contentType = rec.getData().contentType;
            var img = 'images/text_icon.gif';
            if(rec.getData().guid) { // compound document
              img = 'images/openccrp.png';
            }
            else
            if(documentImages[contentType]) {
              img = documentImages[contentType];
            }
            if(data != null)
              docName = data;
            else
            if(documentTypes[contentType]) {
              docName = documentTypes[contentType];
            }
            cell.innerHTML=documentLink(rec,'<img src="'+img+'"/>',docName)+'&nbsp;&nbsp;'+ documentLink(rec,trunc(docName,25),docName);
          }},
         {key:"creationDate", sortable: true, width: '15em',
          formatter: function(cell, rec, col, data) { cell.innerHTML=formatLocalDateTime(new Date(data)); }, label:"Date"},
         {key:"contentType", label:"Type", sortable: true},
         {key:"size", field:"length", label:"Size", sortable: true, formatter: function(c,r,col,d) { c.innerHTML = formatSize(d);}}
    ];
    var docsTable = new YAHOO.widget.DataTable("docTable", columnDefs, dataSource, {
            scrollable:false, 
            width: '100%', 
            height:"14em",
            paginator: new YAHOO.widget.Paginator({ 
                rowsPerPage : 15,  
                containers : [ "pag-below" ], 
                firstPageLinkLabel: '&lt;&lt;', 
                lastPageLinkLabel: '&gt;&gt;',
                previousPageLinkLabel: '&lt;',
                nextPageLinkLabel: '&gt;'
              }) 
            });
  </script>
  </c:set>
</c:if>

<s:layout-render name='/tiledGadgets.jsp' headerTitle='${headerTitle}' gadgets='${gadgets}' rightLinks='${rightLinks}'/>

