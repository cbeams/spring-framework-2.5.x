<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Enter price and item count</DIV>
		<HR>
		<DIV align="left">
			<FORM name="priceAndItemCountForm" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
				<INPUT type="hidden" name="_eventId" value="submit">
				
				Price:
				<spring:bind path="sale.price">
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
				<spring:bind path="sale.itemCount">
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
			<INPUT type="button" onclick="javascript:document.priceAndItemCountForm.submit()" value="Next">
		</DIV>
	</BODY>
</HTML>