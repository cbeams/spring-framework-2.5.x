<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.1//EN" "http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd">
<%@ include file="../common/includes.jsp" %>

<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
    <title><fmt:message key="${htitle}"/></title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <c:set var="css"><spring:theme code="css"/></c:set>
    <c:if test="${not empty css}"><link rel="stylesheet" href="<c:url value="${css}"/>" type="text/css" /></c:if>
  </head>
  <body>
    <div id="pageContent">
    <div id="inContent">
    <div id="nav"><c:import url="${nav}"/></div>
    <div id="top"><c:import url="top.jsp"/></div>
    <div id="content"><h1><fmt:message key="app.short"/></h1><c:import url="${content}"/></div>
    <div id="footer"><c:import url="${nav}"/></div>
    </div>
    </div>
  </body>
</html>
