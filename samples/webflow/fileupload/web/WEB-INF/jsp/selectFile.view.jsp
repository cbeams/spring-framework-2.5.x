<%@ page session="false" %>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<HTML>
	<HEAD>
	</HEAD>
	<BODY>
		<DIV align="left">Select File</DIV>
		<HR>
		<DIV align="left">
			<FORM name="submitForm" action="upload.htm" method="post" enctype="multipart/form-data">
				<INPUT type="hidden" name="_flowExecutionId" value="<%=request.getAttribute("flowExecutionId") %>">
				<INPUT type="hidden" name="_eventId" value="submit">
				<INPUT type="file" name="file">
			</FORM>
		</DIV>
		<HR>
		<DIV align="right">
			<INPUT type="button" onclick="javascript:document.submitForm.submit()" value="Upload">
		</DIV>
	</BODY>
</HTML>
