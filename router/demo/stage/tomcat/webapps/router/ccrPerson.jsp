<%@ page language="java"%>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-bean" prefix="bean" %>
<%@ taglib uri="http://jakarta.apache.org/taglibs/datetime-1.0" prefix="dt" %>
<%@ taglib uri="http://jakarta.apache.org/struts/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ taglib uri="http://www.medcommons.net/medcommons-tld-1.0" prefix="mc" %>
<%@ page isELIgnored="false" %> 

<c:set var="cardType" value="Default"/>
<c:set var="medcommonsId" value="1111111111111111"/>
<tiles:importAttribute name="cardType"/>
<tiles:useAttribute name="index"/>
<tiles:useAttribute name="idPrefix"/>

<script language="javascript">
    {
      var person = new Object();
      //person.medcommonsId='';
      person.name = new Object();
     }
</script>

<div id="${idPrefix}contactCard-${cardType}-${index}" class="contactCard">
    <span class="contactDetails contactDetails${cardType}">
      <span class="contactName">
        <mc:xvalue bean="person" path="personName"/>
      </span>
      <img class="contactPicture" src="images/adrian.jpg"/>
      <span class="medcommonsId">MedCommons ID<sup>#</sup>&#160;
        <mc:medcommonsId><mc:xvalue bean="person" path="personMedCommonsId"/></mc:medcommonsId>
      </span>
        <span class="contactTitle"><br/>TODO</span>
        <span class="contactOrg"><br/><mc:xvalue bean="person" path="personOrg"/></span>
        <span class="contactAddress"><br/><br/>
          <mc:xvalue bean="person" path="personAddress1"/>
          <br/>
            <mc:xvalue bean="person" path="personCityStatePostal"/>
          </span>

          <mc:xnode bean="person" path="personPhoneList" name="phoneNumbers" forceList="true"/>
          <span class="phoneSection">
          <c:if test='${!empty phoneNumbers}'>
            <c:forEach var="phoneNumber" items="${phoneNumbers}">
            <span class="contactWorkNo"><br/>
              <c:set var="type"><mc:xvalue bean="phoneNumber" path="telephoneNumberType"/></c:set>
              <c:choose>
                <c:when test="${type == 'Office Phone'}">Office</c:when>
                <c:otherwise>${type}</c:otherwise>
              </c:choose>
              :<span class="phoneNo">&#160;<mc:xvalue bean="phoneNumber" path="telephoneNumber"/></span></span>
            </c:forEach>
          </c:if>
          &#160;
        </span> <!-- phone section -->
        <span class="contactEmail">
          <mc:xvalue bean="person" path="personEmail"/>&nbsp;
        </span>
    </span> <!-- contact details -->
</div> <!-- end contactCard -->

