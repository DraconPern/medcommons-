
var commentPolicyName="Comment Policy";
var commentPolicyURL="http://www.hhs.gov/open/discussion/commentpolicy.html";

function commentPolicy() {	
	var suffix = "";
	if(commentPolicy.arguments.length == 1)
		suffix = commentPolicy.arguments[0];
	document.write("<a href='" + commentPolicyURL + "'>" + commentPolicyName + "</a>" + suffix);	
}

function commentForm(formID, parentid, folderid, commentName1, commentName2) { 			
	  createformdata="var form_data =  {" +
    		"formName: 'FormCommentscommentButton_' + formID," +
    		"formAction: '/solutions-ugc/comments.do'," +
    		"formMethod: 'post'," +
    		"title: 'Comments'," +
			"formPolicyUrl: ''," +
			"formPolicyLabel: 'Comment Policy'," +
			"fields: {" +
				"1: {name: 'ugc_author', label:'<span class=\"mainLabel\">Name/Nickname</span> (optional, leaving blank will post as anonymous)', type:'text', size:'70'}," +
				"2: {name: 'authorlocation', label:'<span class=\"mainLabel\">Location</span> (optional)', type:'text', size:'70'}," +
				"3: {name: 'body', label:'<span class=\"mainLabel\">Comment</span>', type:'textarea', rows:'10', cols:'80'}," +
				"4: {name: 'parentid', type:'hidden', size:'31', value:'" + parentid + "'}," +
				"5: {name: 'sys_folderid', type:'hidden', size:'31', value:'" + folderid + "'}," +
				"6: {name: 'sys_title', type:'hidden', size:'31'}" +
				"}" +    		
    	"}";
    eval(createformdata);
		createCustomForm2(form_data, commentName1 , commentName2);
}