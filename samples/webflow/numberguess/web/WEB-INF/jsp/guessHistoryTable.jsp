			<c:if test="${!empty data.guessHistory}">
				Guess history:
				<table border="1">
				    <th>Guess</th><th>Right Position</th><th>Present But Wrong Position</th>
				    <c:forEach var="guessData" items="${data.guessHistory}">
				    	<tr><td><c:out value="${guessData.guess}"/></td><td><c:out value="${guessData.rightPosition}"/></td><td><c:out value="${guessData.correctButWrongPosition}"/></td></tr>
				    </c:forEach>
				</table>
			</c:if>