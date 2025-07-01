<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Logout" />
	<c:param name="showNav" value="false" />
</c:import>

<div style="text-align: center; margin-top: 5rem; padding: 2rem;">
	<h1>
		<c:out value="${not empty username ? username : 'Du'}" />
		, du wurdest erfolgreich ausgeloggt!
	</h1>
	<p>Du wirst in 5 Sekunden automatisch zur Login-Seite
		weitergeleitet.</p>
	<p>
		<%-- CORRECTED: The link must point to the /login servlet URL --%>
		<a href="${pageContext.request.contextPath}/login">Jetzt zur
			Login-Seite</a>
	</p>
</div>

<script>
	setTimeout(function() {
		// CORRECTED: The redirect must point to the /login servlet URL
		window.location.href = "${pageContext.request.contextPath}/login";
	}, 5000); // 5 seconds
</script>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />