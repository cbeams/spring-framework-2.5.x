<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Enter shipping details</DIV>
		<HR>
		<DIV align="left">
			Price: <c:out value="${pos.price}"/><BR>
			Item count: <c:out value="${pos.itemCount}"/><BR>
			Category: <c:out value="${pos.category}"/><BR>
			Shipping: <c:out value="${pos.shipping}"/>

			<FORM name="submitForm" method="post">
				<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
				<INPUT type="hidden" name="_eventId" value="submit">
		
				Shipping type:
				<spring:bind path="pos.shippingType">
					<SELECT name="<c:out value="${status.expression}"/>">
						<OPTION value="S" <c:if test="${status.value=='S'}">selected</c:if>>
							Standard (10 extra cost)
						</OPTION>
						<OPTION value="E" <c:if test="${status.value=='E'}">selected</c:if>>
							Express (20 extra cost)
						</OPTION>
					</SELECT>
				</spring:bind>					
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Next">
		</DIV>
	</BODY>
</HTML>
				