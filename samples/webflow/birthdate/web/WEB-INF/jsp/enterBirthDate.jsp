<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

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
						<TD>Your name</TD>
						<TD>
							<spring:bind path="birthDate.name">
								<INPUT
									type="text"
									name="<c:out value="${status.expression}"/>"
									value="<c:out value="${status.value}"/>">
								<c:if test="${status.error}">
									<DIV style="color: red"><c:out value="${status.errorMessage}"/></DIV>
								</c:if>
							</spring:bind>
						</TD>
					</TR>
					<TR>
						<TD>Your Birth Date (DD-MM-YYYY)</TD>
						<TD>
							<spring:bind path="birthDate.date">
								<INPUT
									type="text"
									name="<c:out value="${status.expression}"/>"
									value="<c:out value="${status.value}"/>">
								<c:if test="${status.error}">
									<DIV style="color: red"><c:out value="${status.errorMessage}"/></DIV>
								</c:if>
							</spring:bind>
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
