<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<HTML>
	<BODY>
		<DIV align="left">Sample A Flow</DIV>
		<HR>
		<DIV align="left">
			Flow input was: "<c:out value="${input}"/>"<BR>
			<BR>
			From Sample A, you can terminate Sample A and launch Sample B from an end state of Sample A.
			Input parameters are given as normal request parameters. Again, this can be done using
			either an anchor or a form:
			<LI>
				<A href="<c:url value="/flow.htm?_flowExecutionId=${flowExecutionId}&_eventId=endAAndLaunchB&input=someInputForSampleB"/>">
					End Sample A and Launch Sample B
				</A>
			</LI>
			<LI>
				<FORM action="<c:url value="/flow.htm"/>" method="post">
					<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
					<INPUT type="text" name="input" value="someInputForSampleB">
					<INPUT type="submit" name="_eventId_endAAndLaunchB" value="End Sample A and Launch Sample B">
				</FORM>
			</LI>
			<BR>
			Alternatively, you can spawn Sample B as a sub flow from Sample A. In this case a flow
			attribute mapper maps input into the spawning subflow. Here you also have the option of
			using either an anchor or a form:
			<LI>
				<A href="<c:url value="/flow.htm?_flowExecutionId=${flowExecutionId}&_eventId=launchBAsSubFlow"/>">
					Launch Sample B as a Sub Flow
				</A>
			</LI>
			<LI>
				<FORM action="<c:url value="/flow.htm"/>" method="post">
					<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
					<INPUT type="submit" name="_eventId_launchBAsSubFlow" value="Launch Sample B as a Sub Flow">
				</FORM>
			</LI>
			<BR>
			Yet another option is just launching Sample B as a top-level flow, whithout even terminating
			Sample A:
			<LI>
				<A href="<c:url value="/flow.htm?_flowId=sampleB&input=someInputForSampleB"/>">
					Launch Sample B
				</A>
			</LI>
			<LI>
				<FORM action="<c:url value="/flow.htm"/>" method="post">
					<INPUT type="hidden" name="_flowId" value="sampleB">
					<INPUT type="text" name="input" value="someInputForSampleB">
					<INPUT type="submit" value="Launch Sample B">
				</FORM>
			</LI>
		</DIV>
		<HR>
		<DIV align="right">
			<FORM action="<c:url value="/index.html"/>">
				<INPUT type="submit" value="Home">
			</FORM>
		</DIV>
	</BODY>
</HTML>