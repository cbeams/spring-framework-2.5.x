<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<HTML>
	<BODY>
		<DIV align="left">Cost overview</DIV>
		<HR>
		<DIV align="left">
			Price: <c:out value="${sale.price}"/><BR>
			Item count: <c:out value="${sale.itemCount}"/><BR>
			Category: <c:out value="${sale.category}"/><BR>
			<c:choose>
				<c:when test="${sale.shipping}">
					Shipping: <c:out value="${sale.shipping}"/><BR>
					Shipping type: <c:out value="${sale.shippingType}"/>
				</c:when>
				<c:otherwise>
					No shipping, you're doing pickup of the items.
				</c:otherwise>
			</c:choose>
			
			<BR>
			<BR>
			
			<B>Amount:</B> <c:out value="${sale.amount}"/><BR>
			<B>Delivery cost:</B> + <c:out value="${sale.deliveryCost}"/><BR>
			<B>Discount:</B> - <c:out value="${sale.savings}"/> (Discount rate: <c:out value="${sale.discountRate}"/>)<BR>
			<B>Total:</B> <c:out value="${sale.totalCost}"/><BR>
		</DIV>
		<HR>
		<DIV align="right">
			<FORM action="<c:url value="/index.jsp"/>">
				<INPUT type="submit" value="Home">
			</FORM>
		</DIV>
	</BODY>
</HTML>