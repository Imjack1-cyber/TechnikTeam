<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="de">
<head>
<meta charset="UTF-8">
<title>Logout - Technik Team</title>
<link rel="stylesheet" href="css/style.css">
</head>
<body>
	<div class="error-container"
		style="text-align: center; margin-top: 5rem;">
		<%-- Verwende den 'username' Parameter, der vom LogoutServlet übergeben wird --%>
		<h1>${param.username},du wurdest erfolgreich ausgeloggt!</h1>
		<p>Du wirst in 5 Sekunden automatisch zur Login-Seite
			weitergeleitet.</p>
		<p>
			<a href="${pageContext.request.contextPath}/login">Jetzt zur
				Login-Seite</a>
		</p>
	</div>

	<%-- ======================================================= --%>
	<%--          DIES IST DER ENTSCHEIDENDE FIX                 --%>
	<%-- ======================================================= --%>
	<%-- Das Weiterleitungs-Skript ist jetzt NUR HIER und isoliert --%>
	<script>
		setTimeout(function() {
			// Verwende den contextPath, um die URL sicher aufzubauen
			window.location.href = "${pageContext.request.contextPath}/login";
		}, 5000); // 5000 Millisekunden = 5 Sekunden
	</script>
</body>
</html>