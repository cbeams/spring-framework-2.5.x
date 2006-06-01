<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
  <head><title>User List</title></head>
  <body>
    <table>

    <c:forEach items="${userList}" var="user">
        <tr>
            <td><c:out value="${user.id}"/></td>
            <td><c:out value="${user.lastName}"/>, <c:out value="${user.firstName}"/></td>
            <td><a href="form.htm?id=<c:out value="${user.id}"/>">[edit]</a></td>
        </tr>
    </c:forEach>
    </table>
  </body>
</html>