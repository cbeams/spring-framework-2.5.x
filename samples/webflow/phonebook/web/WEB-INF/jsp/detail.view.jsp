<%@ page session="false" %>

<%@ page import="java.util.*" %>
<%@ page import="org.springframework.samples.phonebook.domain.Person" %>

<jsp:useBean id="person" scope="request" class="org.springframework.samples.phonebook.domain.Person"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<FORM name="backForm" action="detail.htm">
			<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
			<INPUT type="hidden" name="_eventId" value="back">
		</FORM>
		<DIV align="left">Person Detail</DIV>
		<HR>
		<DIV align="left">
			<TABLE>
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
			</TABLE>
			<BR>
			<B>Colleagues:</B>
			<BR>
			<%
				for (int i=0; i<person.nrColleagues(); i++) {
					Person colleague=person.getColleague(i);
			%>
				<A href="detail.htm?_flowExecutionId=<%=request.getAttribute("flowExecutionId") %>&_eventId=select&id=<%=colleague.getUserId() %>">
					<%=colleague.getFirstName() %> <%=colleague.getLastName() %>
				</A>
				<BR>
			<%
				}
			%>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.backForm.submit()" value="Back">
		</DIV>
	</BODY>
</HTML>