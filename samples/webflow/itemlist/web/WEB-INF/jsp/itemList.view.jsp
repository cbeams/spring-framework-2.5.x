<%@ page session="false" %>

<%@ page import="java.util.*" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<FORM name="newItemForm" action="itemList.htm">
			<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
			<INPUT type="hidden" name="_currentStateId" value="itemList.view">
			<INPUT type="hidden" name="_eventId" value="add">
		</FORM>
		<DIV align="left">Item List</DIV>
		<HR>
		<DIV align="left">
			<P>
				<TABLE border="1" width="100%">
					<TR>
						<TD><B>Item Data</B></TD>
					</TR>
					<%
						List list=(List)request.getAttribute("list");
						if (list!=null) for (int i=0; i<list.size(); i++) {
					%>
						<TR>
							<TD><%=list.get(i) %></TD>
						</TR>
					<%
						}
					%>
				</TABLE>
			</P>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.newItemForm.submit()" value="New Item">
		</DIV>
	</BODY>
</HTML>
