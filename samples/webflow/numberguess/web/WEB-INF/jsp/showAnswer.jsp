<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>

<HTML>
	<BODY>
		<DIV align="left">Show answer</DIV>
		<HR>
		<DIV align="left">
			Total number of guesses: <c:out value="${data.guesses}"/><BR>
			Elapsed time in seconds: <c:out value="${data.durationSeconds}"/><BR>
			Answer: <c:out value="${data.answer}"/>
		</DIV>
		<HR>
		<DIV align="right">
			<FORM action="<c:url value="/play.htm"/>">
				<INPUT type="hidden" name="_flowId" value="numberGuess">
				<INPUT type="submit" value="Play Again!">
			</FORM>
		</DIV>
	</BODY>
</HTML>