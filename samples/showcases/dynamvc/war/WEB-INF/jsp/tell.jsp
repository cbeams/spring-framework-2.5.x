<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<html>
<head><title>Fortunes, Fortunes!</title></head>

<body>
<p>${fortune.fortune}
<br><span style="margin-left:10px"> - ${fortune.source}</span>
<p>
    <a href="<c:url value='/home.htm'/>">Back</a>
</body>
</html>
