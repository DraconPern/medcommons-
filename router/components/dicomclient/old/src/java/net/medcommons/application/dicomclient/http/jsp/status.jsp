<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="stripes" uri="http://stripes.sourceforge.net/stripes.tld"%>
<%@ include file="header.jsp" %>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<html>
  <head>
  <title>DDL Status</title>
  <link rel="stylesheet"
              type="text/css"
              href="${pageContext.request.contextPath}/ddl.css"></link>
    <script type="text/javascript">
		djConfig = { isDebug: true };
		debugAtAllCosts: true
	</script>          
  	<script  type="text/javascript" src="dojo.js"> /* Load Dojo engine */ </script> 
 	<script  type="text/javascript" src="ddl.js"> /* Load basic DDL routines */ </script> 
 	<script  type="text/javascript" src="format-utils.js"> /* Load dojo-based format routines */ </script> 

    <script  type="text/javascript" >
    /* Initial Nodes */
    /*
var treeDat = {
	treeNodes: [
		{ title:"CxpUp" },
		{ title:"CxpDown" },
		{ title:"Dicom" }
	]
	};
	
var TreeBuilder = {
	buildTreeNodes:function (dataObjs, treeParentNode){
		for(var i=0; i<dataObjs.length;i++){
			var node =  dojo.widget.createWidget("TreeNode",{
				title:dataObjs[i].title,
				isFolder: true,
				widgetId:(((treeParentNode)?treeParentNode.widgetId:"root_")+"_"+i)
			});
			treeParentNode.addChild(node);
			treeParentNode.registerChild(node,i);
		}
	},
	buildTree:function (){
		var myTreeWidget = dojo.widget.createWidget("Tree",{
			widgetId:"myTreeWidget"
		});
		this.buildTreeNodes(treeDat.treeNodes,myTreeWidget);
		var treeContainer = document.getElementById("myWidgetContainer");
		var placeHolder = document.getElementById("treePlaceHolder");
		treeContainer.replaceChild(myTreeWidget.domNode,placeHolder);
		}
	}
  	dojo.addOnLoad(function(){
	TreeBuilder.buildTree();
	var myRpcController = dojo.widget.createWidget("TreeRPCController",{
			widgetId:"treeController",
			RPCUrl:"/localDDL/Status.action?getCxpDownloads"
		});
	myRpcController.onTreeClick = function(message){
		var node = message.source;
		if (node.isExpanded){
			this.expand(node);
		} else {
			this.collapse(node);
		}
	};
	var treeContainer = document.getElementById("myWidgetContainer");
	treeContainer.appendChild(myRpcController.domNode);
	myRpcController.listenTree(dojo.widget.manager.getWidgetById("myTreeWidget"));
	});
	*/
	dojo.addOnLoad(initAjax);
 	</script>        
 	    
 	
 	

  </head>
  <body onload="javascript:initialize();"> 	
  <c:set var="contextManager" value="${actionBean.contextManager}" scope="page" />
  <stripes:useActionBean binding="/Status.action" var="bean"/>
    <h2>DDL Status Version ${actionBean.contextManager.configurations.version}</h2>

  	
	
    <stripes:form action="/Status.action">
    <stripes:errors/>
    
	<h3>Context information</h3>
	${actionBean.cxpUploads}
	
     Form loaded at:
    <script type="text/javascript">document.write(new Date());</script>
    

    <form name="myForm">
    	<input type="button" id="loadIt" value="Click here to load value.">
    

    	<input type="text" name="myBox" size="50" />
    	Text loaded at: <span id="boxLoadTime">N/A</span>
    </form>
  
   <div id="myWidgetContainer"
	style="width: 17em; border: solid #888 1px; height:300px;">
	<span id="treePlaceHolder"
		style="background-color:#F00; color:#FFF;">
		Loading tree widget...
	</span>
</div>
</stripes:form>
<%@ include file="footer.jsp" %>


