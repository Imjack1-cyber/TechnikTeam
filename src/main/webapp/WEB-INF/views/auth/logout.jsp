<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Logout" />
	<c:param name="showNav" value="false" />
</c:import>

<div style="text-align: center; margin-top: 5rem; padding: 2rem;">
	<h1>
		<c:out value="${not empty param.username ? param.username : 'Du'}" />
		, du wurdest erfolgreich ausgeloggt!
	</h1>
	<p>Du wirst in 5 Sekunden automatisch zur Login-Seite
		weitergeleitet.</p>
	<p>
		<a href="${pageContext.request.contextPath}/WEB-INF/views/auth//WEB-INF/views/auth/login.jsp">Jetzt zur
			Login-Seite</a>
	</p>
</div>

<script>
	setTimeout(function() {
		window.location.href = "${pageContext.request.contextPath}/WEB-INF/views/auth//WEB-INF/views/auth/login.jsp";
	}, 5000); // 5 seconds
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />