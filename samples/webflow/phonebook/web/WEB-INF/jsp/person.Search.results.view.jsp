<%@ page session="false" %>

<%@ page import="java.util.*" %>
<%@ page import="org.springframework.samples.phonebook.domain.Person" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<FORM name="newSearchForm" action="search.htm">
			<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
			<INPUT type="hidden" name="_eventId" value="newSearch">
		</FORM>
		<DIV align="left">
			<P>
				<TABLE border="0">
					<TR>
						<TD>
							<DIV align="left">Search Results</DIV>
						</TD>
					</TR>
					<TR>
						<TD><HR></TD>
					</TR>
					<TR><TD>
					<TABLE BORDER="1">
					<TR>
						<TD><B>First Name</B></TD>
						<TD><B>Last Name</B></TD>
						<TD><B>User Id</B></TD>
						<TD><B>Phone</B></TD>
					</TR>
					<%
						List results=(List)request.getAttribute("persons");
						for (int i=0; i<results.size(); i++) {
							Person person=(Person)results.get(i);
					%>
						<TR>
							<TD><%=person.getFirstName() %></TD>
							<TD><%=person.getLastName() %></TD>
							<TD>
								<A href="search.htm?_flowExecutionId=<%=request.getAttribute("flowExecutionId") %>&_eventId=select&id=<%=person.getId() %>">
									<%=person.getUserId() %>
								</A>
							</TD>
							<TD><%=person.getPhone() %></TD>
						</TR>
					<%
						}
					%>
					</TD></TR>
					</TABLE>
					<TR>
						<TD><HR></TD>
					</TR>
					<TR>
						<TD>
							<DIV align="right">
								<INPUT type="button" onclick="javascript:document.newSearchForm.submit()" value="New Search">
							</DIV>
						</TD>
					</TR>
				</TABLE>
			</P>
		</DIV>
	</BODY>
</HTML>
