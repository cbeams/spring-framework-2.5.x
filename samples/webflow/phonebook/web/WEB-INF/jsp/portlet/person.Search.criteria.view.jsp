<%@ page session="false" %>

<%@ taglib uri="http://www.springframework.org/tags" prefix="spring"%>
<%@ taglib uri="http://java.sun.com/portlet" prefix="portlet"%>

<jsp:useBean id="query" scope="request"
  					 class="org.springframework.samples.phonebook.domain.PhoneBookQuery" />

	<DIV align="left">
  <FORM method="POST" name="searchForm" action="<portlet:actionURL />">
	    <INPUT type="hidden" name="_flowId" value="person.Search" /> 
			<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
			<INPUT type="hidden" name="_eventId" value="submit">
			<TABLE>
				<TR>
					<TD>
						<DIV class="portlet-section-header">Search Criteria</DIV>
					</TD>
					<TD align="right">
				    <A HREF="<portlet:actionURL>
							        <portlet:param name="_flowExecutionId" value="<%=(String)request.getAttribute("flowExecutionId")%>" />
							        <portlet:param name="_eventId" value="help" />
				    				 </portlet:actionURL>">Help</A>
					</TD>
				</TR>
				<TR>
					<TD COLSPAN="2"><HR></TD>
				</TR>
				<spring:hasBindErrors name="query">
					<TR>
						<TD COLSPAN="2">
						  <DIV class="portlet-msg-error">
						    Please provide valid query criteria!
						  </DIV>
						</TD>
					</TR>
				</spring:hasBindErrors>
					<TR>
						<TD>First Name</TD>
						<TD><INPUT type="text" name="firstName" value="<jsp:getProperty name="query" property="firstName"/>"></TD>
					</TR>
					<TR>
						<TD>Last Name</TD>
						<TD><INPUT type="text" name="lastName" value="<jsp:getProperty name="query" property="lastName"/>"></TD>
					</TR>
					<TR>
						<TD COLSPAN="2"><HR></TD>
					</TR>
					<TR>
  				  <TD COLSPAN="2">
						    <DIV align="right">
							    <INPUT type="button" class="portlet-form-button" value="Search"
							           onclick="javascript:document.searchForm.submit()" >
						    </DIV>
						</TD>
					</TR>
				</TABLE>
			</FORM>
		</DIV>
