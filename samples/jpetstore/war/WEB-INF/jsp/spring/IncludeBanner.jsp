<br>
<c:if test="${userSession.account.username}">
	<c:if test="${userSession.account.username}">
		<table align="center" background="../images/bkg-topbar.gif" cellpadding="5" width="100%">
		<tr><td>
		<center>
				<c:out value="${userSession.account.bannerName}"/>
				&nbsp;
		</center>
		</td></tr>
		</table>
	</c:if>
</c:if>
