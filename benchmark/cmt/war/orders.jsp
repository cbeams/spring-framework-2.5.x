

<jsp:useBean id="orders"
		type="org.springframework.benchmark.cmt.web.OrdersBean"
		scope="request"
/>

Found <%=orders.getOrders().length%> orders:
<br>

<table border="1">

<tr bgcolor="yellow" >
		<td>userid</td>
		<td>itemid</td>
		<td>quantity</td>
	</tr>
		
<% for (int i = 0; i < orders.getOrders().length; i++) { %>
	
	<tr>
	 	<td><%= orders.getOrders()[i].getUserId() %> </td>
		<td><%= orders.getOrders()[i].getItemId() %> </td>
		<td><%= orders.getOrders()[i].getQuantity() %> </td>
	</tr>
<% } %>

</table>

Request processed in <%=orders.getTime()%> milliseconds
by benchmark factory of class <%=orders.getMessage()%>.