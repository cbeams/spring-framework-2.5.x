<c:if test="${not (rc.theme.name == 'spring')}">
  <li><a href="<c:url value="/home.htm"><c:param name="theme" value="spring"/></c:url>"><fmt:message key="nav.css.spring"/><span><fmt:message key="nav.css.spring.info"/></span></a></li>
</c:if>
<c:if test="${not (rc.theme.name == 'blue')}">
  <li><a href="<c:url value="/home.htm"><c:param name="theme" value="blue"/></c:url>"><fmt:message key="nav.css.blue"/><span><fmt:message key="nav.css.blue.info"/></span></a></li>
</c:if>
<c:if test="${not (rc.theme.name == 'white')}">
  <li><a href="<c:url value="/home.htm"><c:param name="theme" value="white"/></c:url>"><fmt:message key="nav.css.white"/><span><fmt:message key="nav.css.white.info"/></span></a></li>
</c:if>
<c:if test="${not (rc.theme.name == 'none')}">
    <li><a href="<c:url value="/home.htm"><c:param name="theme" value="none"/></c:url>"><fmt:message key="nav.css.none"/><span><fmt:message key="nav.css.none.info"/></span></a></li>
</c:if>
<c:if test="${not (rc.locale.language == 'en')}">
  <fmt:message key="img.en" var="img"/>
  <li><a href="<c:url value="/home.htm"><c:param name="locale" value="en_US"/></c:url>"><img src="<c:url value="/${img}"/>" alt="<fmt:message key="nav.lang.en"/>"><span> <fmt:message key="nav.lang.en.info"/></span></a></li>
</c:if>
<c:if test="${not (rc.locale.language == 'fr')}">
  <fmt:message key="img.fr" var="img"/>
  <li><a href="<c:url value="/home.htm"><c:param name="locale" value="fr_FR"/></c:url>"><img src="<c:url value="/${img}"/>" alt="<fmt:message key="nav.lang.fr"/>" /><span> <fmt:message key="nav.lang.fr.info"/></span></a></li>
</c:if>
<c:if test="${not (rc.locale.language == 'de')}">
  <fmt:message key="img.de" var="img"/>
  <li><a href="<c:url value="/home.htm"><c:param name="locale" value="de_GE"/></c:url>"><img src="<c:url value="/${img}"/>" alt="<fmt:message key="nav.lang.de"/>" /><span> <fmt:message key="nav.lang.de.info"/></span></a></li> 
</c:if>
