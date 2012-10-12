<?xml version="1.0" encoding="UTF-8"?>
<%@ page language="java" contentType="text/xml"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ page import="net.medcommons.router.util.metrics.*" %>
<%@ page isELIgnored="false" %> 
<%--
  MedCommons Router Self Test Page - XML version

  This page renders a list of SelfTest objects to display the results of tests.

  @author Sean Doyle, MedCommons Inc.
--%>
 <SelfTest>
 	 <Hostname>${actionBean.acDomain}</Hostname>
    	<Tests>
  			
      <c:forEach items="${actionBean.results}" var="r">
      	<Test>
      		<Status>${r.status}</Status>
      		<Name>${r.name}</Name>
      		<Message>${r.message}</Message>
      		<Tips>${r.tips}</Tips>
      		<TimeMsec>${r.timeMs}</TimeMsec>
      	</Test>
      </c:forEach>
   </Tests>
   </SelfTest>
   
