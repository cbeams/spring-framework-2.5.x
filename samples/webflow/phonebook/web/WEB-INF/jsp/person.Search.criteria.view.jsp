<%@ page session="false" %>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<jsp:useBean id="query" scope="request" class="org.springframework.samples.phonebook.domain.PhoneBookQuery"/>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<DIV align="left">Search Criteria</DIV>
		<HR>
		<spring:hasBindErrors name="query">
			<FONT color="red">Please provide valid query criteria!</FONT>
		</spring:hasBindErrors>
		<DIV align="left">
			<FORM name="searchForm" action="search.htm">
				<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
				<INPUT type="hidden" name="_eventId" value="submit">
				<TABLE>
					<TR>
						<TD>First Name</TD>
						<TD><INPUT type="text" name="firstName" value="<jsp:getProperty name="query" property="firstName"/>"></TD>
					</TR>
					<TR>
						<TD>Last Name</TD>
						<TD><INPUT type="text" name="lastName" value="<jsp:getProperty name="query" property="lastName"/>"></TD>
					</TR>
				</TABLE>
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.searchForm.submit()" value="Search">
		</DIV>
	</BODY>
</HTML>