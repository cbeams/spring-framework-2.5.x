<%@ include file="/WEB-INF/jsp/includes.jsp" %>
<%@ include file="/WEB-INF/jsp/header.jsp" %>

<h2>Owner Information</h2>

  <table>
    <tr>
      <th>Name</th>
      <td><b>${owner.firstName} ${owner.lastName}</b></td>
    </tr>
    <tr>
      <th>Address</th>
      <td>${owner.address}</td>
    </tr>
    <tr>
      <th>City</th>
      <td>${owner.city}</td>
    </tr>
    <tr>
      <th>Telephone </th>
      <td>${owner.telephone}</td>
    </tr>
    <tr>
      <td></td>
      <td>
        <form method=GET action="<c:url value="/editOwner.do"/>">
          <input type="hidden" name="ownerId" value="${owner.id}"/>
          <input type="submit" value="Edit Owner"/>
        </form>
      </td>
    </tr>
  </table>

  <br/>
  <h3>Pets and Visits</h3>

  <c:forEach var="pet" items="${owner.pets}">
    <table>
      <tr>
        <td valign="top">
          <table>
            <tr>
              <th>Name</th>
              <td><b>${pet.name}</b></td>
            </tr>
            <tr>
              <th>Birth Date</th>
              <td><fmt:formatDate value="${pet.birthDate}" pattern="yyyy-MM-dd"/></td>
            </tr>
            <tr>
              <th>Type</th>
              <td>${pet.type.name}</td>
            </tr>
            <tr>
              <td colspan="2">
                <form method="GET" action="<c:url value="/editPet.do"/>" name="formEditPet${pet.id}">
                  <input type="hidden" name="petId" value="${pet.id}"/>
                  <input type="submit" value="Edit Pet"/>
                </form>
              </td>
            </tr>
            <tr>
              <td colspan="2">
                <form method="GET" action="<c:url value="/addVisit.do"/>" name="formVisitPet${pet.id}">
                  <input type="hidden" name="petId" value="${pet.id}"/>
                  <input type="submit" value="Add Visit"/>
                </form>
              </td>
            </tr>
          </table>
        </td>
        <td valign="top">
          <table>
            <tr>
              <th>Visit Date</th>
              <th>Description</th>
            </tr>
            <c:forEach var="visit" items="${pet.visits}">
              <tr>
                <td><fmt:formatDate value="${visit.date}" pattern="yyyy-MM-dd"/></td>
                <td>${visit.description}</td>
              </tr>
            </c:forEach>
          </table>
        </td>
      </tr>
    </table>
    <br/>
  </c:forEach>

  <form method=GET action="<c:url value="/addPet.do"/>" name="formAddPet">
    <input type="hidden" name="ownerId" value="${owner.id}"/>
    <input type="submit" value="Add New Pet"/>
  </form>

  <br/>
  
<%@ include file="/WEB-INF/jsp/footer.jsp" %>
