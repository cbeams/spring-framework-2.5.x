<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="/tags/struts-html" prefix="html" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
	
		<DIV align="left">Enter Birth Date</DIV>
		
		<HR>
		
		<DIV align="left">
			<FORM name="submitForm" action="birthDate.do" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
				<INPUT type="hidden" name="_eventId" value="submit">
				
				<TABLE>
					<TR>
						<TD colspan="2">
							<html:errors />
						</TD>
					</TR>
					<TR>
						<TD>Your name</TD>
						<TD>
							<html:text property="name" size="25" maxlength="30"/>
						</TD>
					</TR>
					<TR>
						<TD>Your Birth Date (DD-MM-YYYY)</TD>
						<TD>
							<html:text property="date" size="10" maxlength="10"/>
						</TD>
					</TR>
				</TABLE>
			</FORM>
		</DIV>
		
		<HR>

		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Calculate Age">
		</DIV>
		
	</BODY>
</HTML>
