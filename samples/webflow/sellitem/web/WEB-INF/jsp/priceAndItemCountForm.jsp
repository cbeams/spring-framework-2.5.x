<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Enter price and item count</DIV>
		<HR>
		<DIV align="left">
			<FORM name="submitForm" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
				<INPUT type="hidden" name="_eventId" value="submit">
				
				Price:
				<spring:bind path="pos.price">
					<INPUT
						type="text"
						name="<c:out value="${status.expression}"/>"
						value="<c:out value="${status.value}"/>">
					<c:if test="${status.error}">
						<DIV style="color: red"><c:out value="${status.errorMessage}"/></DIV>
					</c:if>
				</spring:bind>
				
				<BR>
				
				Item count:
				<spring:bind path="pos.itemCount">
					<INPUT
						type="text"
						name="<c:out value="${status.expression}"/>"
						value="<c:out value="${status.value}"/>">
					<c:if test="${status.error}">
						<DIV style="color: red"><c:out value="${status.errorMessage}"/></DIV>
					</c:if>
				</spring:bind>
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Next">
		</DIV>
	</BODY>
</HTML>
