<%@ page session="false" %>
<%@ page import="java.util.*" %>
<%@ page import="org.springframework.samples.phonebook.domain.Person" %>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet" %>

<FORM method="POST" name="newSearchForm" action="<portlet:actionURL />">
	<INPUT type="hidden" name="_flowId" value="person.Search" />
	<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
	<INPUT type="hidden" name="_eventId" value="newSearch">
</FORM>
<DIV align="left">
	<P>
		<TABLE>
			<TR>
				<TR>
					<TD COLSPAN="2">
	    				<DIV class="portlet-section-header">Search Results</DIV>
					</TD>
				</TR>
			</TR>
			<TR>
				<TD COLSPAN="2"><HR /></TD>
			</TR>
			<TR>
				<TD COLSPAN="2">
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
								    <A href="<portlet:renderURL>
						            	<portlet:param name="_flowExecutionId" value="<%=(String)request.getAttribute("flowExecutionId")%>" />
						            	<portlet:param name="_eventId" value="select" />
						            	<portlet:param name="id" value="<%=person.getId().toString()%>" />
						            	</portlet:renderURL>">
					    	  			<%=person.getUserId() %>
								    </A>
								</TD>
								<TD><%=person.getPhone() %></TD>
							</TR>
						<%
							}
						%>
					</TABLE>
				</TD>
			</TR>
			<TR>
				<TD COLSPAN="2"><HR /></TD>
			</TR>
			<TR>
				<TD COLSPAN="2">
					<DIV align="right">
						<INPUT type="button" class="portlet-form-button" value="New Search"
						       onclick="javascript:document.newSearchForm.submit()" >
					</DIV>
				</TD>
			</TR>
		</TABLE>
	</P>
</DIV>

