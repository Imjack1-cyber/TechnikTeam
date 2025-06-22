<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  logout.jsp
  
  This is a confirmation page shown to the user immediately after they have
  logged out. It displays a personalized message and uses JavaScript to
  automatically redirect the user to the login page after a short delay.
  
  - It is served by: LogoutServlet (via redirect).
  - Expected parameters:
    - 'username' (String): The name of the user who just logged out.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Logout" />
</c:import>

<div style="text-align: center; margin-top: 5rem; padding: 2rem;">
	<h1>
		<c:out value="${not empty param.username ? param.username : 'Du'}" />
		, du wurdest erfolgreich ausgeloggt!
	</h1>
	<p>Du wirst in 5 Sekunden automatisch zur Login-Seite
		weitergeleitet.</p>
	<p>
		<a href="${pageContext.request.contextPath}/login">Jetzt zur
			Login-Seite</a>
	</p>
</div>

<script>
	setTimeout(function() {
		// Use the contextPath to build the URL safely, making it robust
		// even if the application is deployed under a different name.
		window.location.href = "${pageContext.request.contextPath}/login";
	}, 5000); // 5000 milliseconds = 5 seconds
</script>

</body>
</html>