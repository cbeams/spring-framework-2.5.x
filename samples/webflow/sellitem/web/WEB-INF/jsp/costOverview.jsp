<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<HTML>
	<BODY>
		<DIV align="left">Cost overview</DIV>
		<HR>
		<DIV align="left">
			Price: <c:out value="${pos.price}"/><BR>
			Item count: <c:out value="${pos.itemCount}"/><BR>
			Category: <c:out value="${pos.category}"/><BR>
			<c:choose>
				<c:when test="${pos.shipping}">
					Shipping: <c:out value="${pos.shipping}"/><BR>
					Shipping type: <c:out value="${pos.shippingType}"/>
				</c:when>
				<c:otherwise>
					No shipping, you're doing pickup of the items.
				</c:otherwise>
			</c:choose>
			
			<BR>
			<BR>
			
			<B>Amount:</B> <c:out value="${pos.amount}"/><BR>
			<B>Delivery cost:</B> + <c:out value="${pos.deliveryCost}"/><BR>
			<B>Discount:</B> - <c:out value="${pos.savings}"/> (Discount rate: <c:out value="${pos.discountRate}"/>)<BR>
			<B>Total:</B> <c:out value="${pos.totalCost}"/><BR>
		</DIV>
		<HR>
		<DIV align="right">
			<FORM action="<c:url value="/index.jsp"/>">
				<INPUT type="submit" value="Home">
			</FORM>
		</DIV>
	</BODY>
</HTML>
