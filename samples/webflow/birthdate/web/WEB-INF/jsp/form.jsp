<%@ page session="false" %>
<%@ taglib uri="http://java.sun.com/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jstl/fmt" prefix="fmt" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<DIV align="left">Your Age</DIV>
		
		<HR>
		
		<DIV align="left">
			<P>
				<c:out value="${birthDate.name}"/>, you are now <I><c:out value="${age}"/></I> old.
				You were born on <fmt:formatDate value="${birthDate.date}" pattern="dd-MM-yyyy"/>.
			</P>
		</DIV>
		
		<HR>

		<DIV align="right">
			<FORM action="<c:url value="/index.jsp"/>">
				<INPUT type="submit" value="Home">
			</FORM>
		</DIV>
		
	</BODY>
</HTML>
