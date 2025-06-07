<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Logout" />
</c:import>

<script>
	// Redirect to login page after 5 seconds
	setTimeout(function() {
		window.location.href = "${pageContext.request.contextPath}/login";
	}, 5000); // 5000 milliseconds = 5 seconds
</script>

<div style="text-align: center; margin-top: 5rem;">
	<h1>${param.username},du wurdest erfolgreich ausgeloggt!</h1>
	<p>Du wirst in 5 Sekunden automatisch zur Login-Seite
		weitergeleitet.</p>
	<p>
		<a href="${pageContext.request.contextPath}/login">Jetzt zur
			Login-Seite</a>
	</p>
</div>

<!-- No navigation or footer needed here -->
</body>
</html>