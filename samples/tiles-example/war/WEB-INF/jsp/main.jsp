<%@ taglib prefix="tiles" uri="http://jakarta.apache.org/struts/tags-tiles" %>

<html>
	<head>
		<link rel="stylesheet" type="text/css" href="styles.css"/>
		<title>SomeCompany Inc.</title>
	</head>
	<body>
		<table width="100%" border="0">
			<tr>
				<td colspan="2"><h1>Some Company</h1></td>
			</tr>
			<tr>
				<td class="menu" colspan="2"><a href="index.html">home</a> | <a href="about.html">about</a> | <a href="contact.html">contact</a></tD>
			</tr>
			<tr>
				<td width="70%" valign="top">
					<tiles:insert name="content"/>
				</td>
				<td valign="top">
					<tiles:insert name="newsOne"/>
					<tiles:insert name="newsTwo"/>
				</td>
			</tr>
		</table>
	</body>
</html>
				
				