<%@ include file="/taglibs.inc.jsp" %>
<%--
  MedCommons Router Self Test Page

  This page renders a list of SelfTest objects to display the results of tests.

  @author Simon Sadedin, MedCommons Inc.
--%>
<s:layout-render name="/gadgetBase.jsp" title="${actionBean.acDomain} Gateway Self Test Report">
  <s:layout-component name="head">
    <style type="text/css">
      body {
        padding: 5px;
      }
      table tr td {
        padding: 4px;
      }
      tr.Failed td.status {
        background-color: #eaa;
      }
      tr.Failed td.status i {
        font-size: 9px;
      }
      tr.OK td.status {
        background-color: #aea;
      }
      .invisible {
        display: none;
      }
      ul li {
        margin-top: 5px;
      }
      .detailDiv {
        border: solid 2px black;
        background-color: #ffe;
        padding: 10px;
        margin: 20px 10px;
      }
    </style>
    <script type="text/javascript" src='mochikit/MochiKit.js'></script>
    <script type="text/javascript" src='util.js'></script>
    <script type="text/javascript">
    </script>
     <title>${actionBean.acDomain} Gateway Self Test Report</title>
  </s:layout-component>
  <s:layout-component name="body">
    <h3>MedCommons Gateway Self Test for ${actionBean.acDomain}</h3>
    <table border="1">
      <tr><th>Name</th><th>Status</th><th>Comments</th><th>Run Time</th></tr>
      <c:forEach items="${actionBean.results}" var="r">
      <tr class='${r.status}'>
        <td><c:out value="${r.name}"/></td>
        <td class="status" title="${r.message}" onmouseover="show('info${r.name}');" onmouseout="if($('info${r.name}')) fade($('info${r.name}'), {delay: 3.0, duration: 0.5, fps:25});"><c:out value="${r.status}"/></td>
        <td>${r.tips}&nbsp;<c:if test='${not empty r.tips}'><br/><i>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;Mouse over status for error information</i></c:if></td>
        <td>${r.timeMs}</td>
      </tr>
      </c:forEach>
    </table>
    <c:forEach items="${actionBean.results}" var="r">
      <c:if test='${r.status == "Failed"}'>
      <div id='info${r.name}' class='detailDiv invisible'>
        <h4><c:out value="${r.name}"/></h4>
        <c:out value="${r.message}"/>
      </div>
      </c:if>
    </c:forEach>
  </s:layout-component>
</s:layout-render>
