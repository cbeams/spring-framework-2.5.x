<%@ taglib prefix="tiles"  uri="http://tiles.apache.org/tags-tiles" %>
<%@ taglib prefix="c"      uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"    uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
<html>
	<head>
		<link rel="stylesheet" type="text/css" href="styles.css"/>
		<title><fmt:message key="title"/></title>
	</head>
	<body>
		<table width="100%" border="0">
			<tr>
				<td colspan="2"><h1><spring:message code="title"/></h1></td>
			</tr>
			<tr>
				<td class="menu" colspan="2"><a href="index.html">home</a> | <a href="about.html">about</a> | <a href="contact.html">contact</a></tD>
			</tr>
			<tr>
				<td width="70%" valign="top">
					<tiles:insertAttribute name="content"/>
				</td>
				<td valign="top">
					<tiles:insertAttribute name="newsOne"/>
					<tiles:insertAttribute name="newsTwo"/>
				</td>
			</tr>
		</table>
	</body>
</html>

