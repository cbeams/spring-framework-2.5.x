
<jsp:useBean id="snap"
	type="com.lch.gcs.position.intf.PositionSnapDto"
	 scope="request"
/>

Found <%=snap.getCount()%> 
open positions for organization with id
<%=snap.getOrgId()%>.