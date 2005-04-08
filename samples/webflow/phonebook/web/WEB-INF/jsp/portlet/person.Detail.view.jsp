<%@ page session="false" %>
<%@ page import="org.springframework.samples.phonebook.domain.Person" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<jsp:useBean id="person" scope="request" class="org.springframework.samples.phonebook.domain.Person"/>

<FORM method="post" name="backForm" action="<portlet:actionURL />">
    <INPUT type="hidden" name="_flowId" value="person.Detail" />
    <INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>" />
    <INPUT type="hidden" name="_eventId" value="back" />
</FORM>
<TABLE>
  <TR>
    <TD class="portlet-section-subheader">Person Details</TD>
  </TR>
  <TR>
    <TD COLSPAN="2"><HR></TD>
  </TR>
  <TR>
    <TD><B>First Name</B></TD>
    <TD><jsp:getProperty name="person" property="firstName"/></TD>
  </TR>
  <TR>
    <TD><B>Last Name</B></TD>
    <TD><jsp:getProperty name="person" property="lastName"/></TD>
  </TR>
  <TR>
    <TD><B>User Id</B></TD>
    <TD><jsp:getProperty name="person" property="userId"/></TD>
  </TR>
  <TR>
    <TD><B>Phone</B></TD>
    <TD><jsp:getProperty name="person" property="phone"/></TD>
  </TR>
  <TR>
    <TD COLSPAN="2">
      <BR>
      <B>Colleagues:</B>
      <BR>
      <%
        for (int i = 0 ; i < person.getColleagueCount(); i++) {
        Person colleague = person.getColleague(i);
      %>
        <A href="<portlet:renderURL>
                   <portlet:param name="_flowExecutionId" value="<%=(String)request.getAttribute("flowExecutionId")%>" />
                   <portlet:param name="_eventId" value="select" />
                   <portlet:param name="id" value="<%=colleague.getId().toString()%>" />
                 </portlet:renderURL>">
        <%= colleague.getFirstName() %> <%=colleague.getLastName() %>
        </A>
      <BR>
      <%
        }
      %>
    </TD>
  </TR>
  <TR>
    <TD COLSPAN="2"><HR></TD>
  </TR>
  <TR>
    <TD COLSPAN="2">
      <DIV align="right">
        <INPUT type="button" class="portlet-form-button" value="Back" onclick="javascript:document.backForm.submit()" >
      </DIV>
    </TD>
  </TR>
</TABLE>
