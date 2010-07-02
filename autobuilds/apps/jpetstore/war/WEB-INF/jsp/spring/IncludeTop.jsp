<%@ page contentType="text/html" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jstl/fmt" %>

<html><head><title>JPetStore Demo</title>
<meta content="text/html; charset=windows-1252" http-equiv="Content-Type" />
<META HTTP-EQUIV="Cache-Control" CONTENT="max-age=0">
<META HTTP-EQUIV="Cache-Control" CONTENT="no-cache">
<meta http-equiv="expires" content="0">
<META HTTP-EQUIV="Expires" CONTENT="Tue, 01 Jan 1980 1:00:00 GMT">
<META HTTP-EQUIV="Pragma" CONTENT="no-cache">
</head>

<body bgcolor="white">

<table background="../images/bkg-topbar.gif" border="0" cellspacing="0" cellpadding="5" width="100%">
  <tbody>
  <tr>
    <td><a href="<c:url value="/shop/index.do"/>">HOME</a></td>
    <td align="right"><a href="<c:url value="/shop/viewCart.do"/>">CART</a>
    |

<c:if test="${empty userSession.account}" >
      <a href="<c:url value="/shop/signonForm.do"/>">SIGN-IN</a>
</c:if>

<c:if test="${!empty userSession.account}" >
      <a href="<c:url value="/shop/signoff.do"/>">SIGN-OUT</a>
      |
      <a href="<c:url value="/shop/editAccount.do"/>">MY ACCOUNT</a>
</c:if>

      |<a href="<c:url value="/shop/help.do"/>">HELP</a>
    </td>
    <td align="left" valign="bottom">
      <form action="<c:url value="/shop/searchProducts.do"/>" method="post">
			  <input type="hidden" name="search" value="true"/>
        <input name="keyword" size="14" />&nbsp;<input border="0" src="../images/search.gif" type="image"/>
      </form>
    </td>
  </tr>
  </tbody>
</table>

<%@ include file="IncludeQuickHeader.jsp" %>
