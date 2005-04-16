<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<HTML>
	<BODY>
		<DIV align="left">Sample B Flow</DIV>
		<HR>
		<DIV align="left">
			Flow input was: "<c:out value="${input}"/>"<BR>
			<c:if test="${!flowExecution.rootFlowActive}">
				<BR>
				Sample B is running as a sub flow of another flow, so we can end Sample B and
				return to the parent flow, using either an anchor or a form:
				<LI>
					<A href="<c:url value="/flow.htm?_flowExecutionId=${flowExecutionId}&_eventId=end"/>">
						End Sample B
					</A>
				</LI>
				<LI>
					<FORM action="<c:url value="/flow.htm"/>" method="post">
						<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
						<INPUT type="submit" name="_eventId_end" value="End Sample B">
					</FORM>
				</LI>
			</c:if>
		</DIV>
		<HR>
		<DIV align="right">
			<FORM action="<c:url value="/index.html"/>">
				<INPUT type="submit" value="Home">
			</FORM>
		</DIV>
	</BODY>
</HTML>