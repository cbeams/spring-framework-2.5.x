<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://www.springframework.org/tags" prefix="spring" %>

<HTML>
	<BODY>
		<DIV align="left">Enter category</DIV>
		<HR>
		<DIV align="left">
			Price: <c:out value="${sale.price}"/><BR>
			Item count: <c:out value="${sale.itemCount}"/>
			
			<FORM name="categoryForm">
				<INPUT type="hidden" name="_flowExecutionId" value="<c:out value="${flowExecutionId}"/>">
				<INPUT type="hidden" name="_eventId" value="submit">
			
				Category:
				<spring:bind path="sale.category">
					<SELECT name="<c:out value="${status.expression}"/>">
						<OPTION value="" <c:if test="${status.value==''}">selected</c:if>>
							None (0.02 discount rate)
						</OPTION>
						<OPTION value="A" <c:if test="${status.value=='A'}">selected</c:if>>
							Cat. A (0.1 discount rate when more than 100 items)
						</OPTION>
						<OPTION value="B" <c:if test="${status.value=='B'}">selected</c:if>>
							Cat. B (0.2 discount rate when more than 200 items)
						</OPTION>
					</SELECT>
				</spring:bind>
				
				<BR>
				
				Ship item to you?:
				<spring:bind path="sale.shipping"> 
					<INPUT type="hidden" name="_<c:out value="${status.expression}"/>"  value="visible" /> 
					<INPUT type="checkbox" name="<c:out value="${status.expression}"/>" value="true" <c:if test="${status.value}">checked</c:if>> 
				</spring:bind>
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Next">
		</DIV>
	</BODY>
</HTML>