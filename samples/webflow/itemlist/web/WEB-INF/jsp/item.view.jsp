<%@ page session="false" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<DIV align="left">Add Item</DIV>
		<HR>
		<DIV align="left">
			<FORM name="submitForm" action="itemList.htm">
				<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
				<INPUT type="hidden" name="_currentStateId" value="item.view">
				<INPUT type="hidden" name="_eventId" value="submit">
				<%-- make sure we send the transaction token back to the server --%>
				<INPUT type="hidden" name="_txToken" value="<%=request.getAttribute("txToken") %>">
				<TABLE>
					<TR>
						<TD>Item Data</TD>
						<TD><INPUT type="text" name="data" value=""></TD>
					</TR>
				</TABLE>
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Add">
		</DIV>
	</BODY>
</HTML>
