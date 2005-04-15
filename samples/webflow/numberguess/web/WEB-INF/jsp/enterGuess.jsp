<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<HTML>
	<BODY>
		<DIV align="left">Enter guess</DIV>
		<HR>
		<DIV align="left">
			Number of guesses so far: <c:out value="${data.guesses}" default="0"/><BR>
			Indication: <c:out value="${data.indication}"/><BR>
			
			<FORM name="guessForm" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
				<INPUT type="hidden" name="_eventId" value="submit">
			
				<INPUT type="text" name="guess" value="<c:out value="${param.guess}"/>"/>
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.guessForm.submit()" value="Guess">
		</DIV>
	</BODY>
</HTML>