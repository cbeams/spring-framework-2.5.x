<%@ include file="IncludeTop.jsp" %>

<table border="0" cellspacing="0" width="100%">
  <tbody>
  <tr>
    <td valign="top" width="100%">

      <table align="left" border="0" cellspacing="0" width="80%">
        <tbody>
        <tr>
          <td valign="top">

            <!-- SIDEBAR -->

            <table bgcolor="#FFFF88" border="0" cellspacing="0" cellpadding="5" width="200">
              <tbody>
      <tr>
      <td>
        <c:if test="${!empty userSession.account}">
					<b><i><font size="2" color="BLACK">Welcome <c:out value="${userSession.account.firstName}"/>!</font></i></b>
        </c:if>
        &nbsp;
      </td>
      </tr>
              <tr>
                <td>
                <a href="<c:url value="/shop/viewCategory.do?categoryId=FISH"/>">
                FISH</a>
                </td>
              </tr>
              <tr>
                <td>
                <a href="<c:url value="/shop/viewCategory.do?categoryId=DOGS"/>">
                DOGS</a>
                </td>
              </tr>
              <tr>
                <td>
                <a href="<c:url value="/shop/viewCategory.do?categoryId=CATS"/>">
                CATS</a>
                </td>
              </tr>
              <tr>
                <td>
                <a href="<c:url value="/shop/viewCategory.do?categoryId=REPTILES"/>">
                REPTILES</a>
                </td>
              </tr>
              <tr>
                <td>
                <a href="<c:url value="/shop/viewCategory.do?categoryId=BIRDS"/>">
                BIRDS</a>
                </td>
              </tr>

              </tbody>
             </table>

           </td>
          <td align="center" bgcolor="white" height="300" width="100%">

          select a category
          
          </td></tr></tbody></table></td></tr>

        </tbody>
        </table>

<%@ include file="IncludeBanner.jsp" %>

<%@ include file="IncludeBottom.jsp" %>
