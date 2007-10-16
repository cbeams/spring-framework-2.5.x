<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2><fmt:message key="welcome"/></h2>

<ul>
  <li><a href="<c:url value="/findOwners.htm"/>">Find owner</a></li>
  <li><a href="<c:url value="/vets.htm"/>">Display all veterinarians</a></li>
  <li><a href="<c:url value="/html/petclinic.html"/>">Tutorial</a></li>
  <li><a href="<c:url value="/docs/index.html"/>">Documentation</a></li>
</ul>

<%@ include file="/WEB-INF/jsp/footer.jsp" %>
