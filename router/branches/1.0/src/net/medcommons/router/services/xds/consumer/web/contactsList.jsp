<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/xml" prefix="x" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn" %>
<%@ page isELIgnored="false" %> 
<script language="JavaScript">
  var contacts = new Array();
  var oldContactClass;
</script>
<script language="JavaScript" src="contactsList.js"></script>

<div id="contactslist">
  <%-- The contact card at the top of the contact list --%>
  <div id="contactlistCard" class="contactCard">
      <span class="contactDetails contactDetailsContactList">
        <span id="clName" class="contactName">&nbsp;</span>
        <img class="contactPicture" src="images/adrian.jpg"/>
        <span class="medcommonsId">MedCommons ID<sup>#</sup>1111 1111 1111 1111</span>
        <span id="clTitle" class="contactTitle"><br/>TODO</span><br/>
        <span id="clOrg" class="contactOrg">&nbsp;</span><br/><br/>
        <span id="clAddress" class="contactAddress"><br/>&nbsp;</span>
        <span id="clPhone" class="phoneSection">&nbsp;</span> <!-- phone section -->
        <span id="clEmail" class="contactEmail">&nbsp;</span>
      </span> <!-- contact details -->
  </div> <!-- end contactCard -->

  <%-- <div class="contactsHeader"><h2 class="contactsHeading">Contacts</h2></div> --%>
  <div id="contactsDetails">
    <table class="contactsTable" margin="0" cellspacing="0" border="0">
    <c:forEach var="contact" items="${contacts}" varStatus="status">
      <script language="JavaScript">
        var contact = { 
          selected: false, 
          givenName: '${contact.givenName}',
          familyName: '${contact.familyName}',
          address1: '${contact.line1}',
          city: '${contact.city}',
          state: '${contact.state}',
          postalCode: '${contact.postalCode}',
          country: '${contact.country}',
          organization: '${contact.organization}',
          gender: '${contact.gender}',
          dateOfBirth: '${contact.dateOfBirth}',
          emails: new Array(),
          phoneNumbers: new Array()
        };

        <c:forEach var="phoneNumberEntry" items="${contact.phoneNumbers}" varStatus="phoneNumberStatus">
          contact.phoneNumbers[${phoneNumberStatus.index}] = new Object();
          contact.phoneNumbers[${phoneNumberStatus.index}].value = '${fn:trim(phoneNumberEntry.value)}';
          contact.phoneNumbers[${phoneNumberStatus.index}].type = '${fn:trim(phoneNumberEntry.key)}';
        </c:forEach>
        <c:forEach var="emailEntry" items="${contact.emails}" varStatus="emailStatus">
          contact.emails[${emailStatus.index}] = '${fn:trim(emailEntry.value)}';
        </c:forEach>
        contacts[${status.index}] = contact;</script>
      <tr id="contactRow${status.index}" class="contactsRow contactsRow${status.index % 2}" onclick="selectContact(${status.index});">
        <td class="contactImageCell"><img src="images/contactblack.gif"/>&nbsp;</td>
        <td class="contactDetailsCell"  width="140"><b>${contact.givenName} ${contact.familyName}</b></td>
      </tr>
      <tr id="contactRow2-${status.index}" 
          class="contactsRow contactsRow${status.index % 2}" 
          onclick="selectContact(${status.index});">
        <td class="contactImageCell">&nbsp;</td><td class="contactBarEmail">
          <%--${contact.line1}<c:if test="${contact.line1 != ''}"><br/></c:if>
             ${contact.city} ${contact.state} ${contact.postalCode}
             <c:if test="${contact.city != ''}"><br/></c:if> --%>
           <c:choose>
             <c:when test="${contact.organization !=''}">${contact.organization}</c:when>
             <c:otherwise>
               <c:forEach var="emailEntry" items="${contact.emails}" varStatus="emailStatus">
                <c:if test='${emailStatus.index} gt 0'><br/></c:if> 
                 ${emailEntry.value}
               </c:forEach>
             </c:otherwise>
           </c:choose>
           &nbsp;
        </td>
      </tr>
    </c:forEach>
    </table>
  </div> <%-- contactsDetails --%>
</div>
<div id="contactsgripper" onclick="toggleContacts();">&nbsp;</div>

