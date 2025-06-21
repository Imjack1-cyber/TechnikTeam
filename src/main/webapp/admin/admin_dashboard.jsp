<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Admin Dashboard" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<!-- 
admin_dashboard.jsp: The main landing page for the admin area, showing statistics and quick links.

    Served by: AdminDashboardServlet (doGet).

    Dependencies: Includes css/style.css, header.jspf, admin_navigation.jspf, footer.jspf. Links to other admin pages like /admin/users, /admin/events, etc.
-->

<h1>Willkommen im Admin-Bereich</h1>
<p>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option
	aus der Navigation.</p>

<div class="dashboard">
	<div class="card">
		<h2 class="card-title">Schnellzugriff</h2>
		<ul>
			<li><a href="${pageContext.request.contextPath}/admin/users">Benutzer
					verwalten</a></li>
			<li><a href="${pageContext.request.contextPath}/admin/events">Events
					erstellen &amp; bearbeiten</a></li>
			<li><a href="${pageContext.request.contextPath}/admin/files">Dateien
					hochladen</a></li>
		</ul>
	</div>

	<div class="card">
		<h2 class="card-title">Statistiken</h2>
		<p>
			Anzahl registrierter Benutzer: <strong>${userCount}</strong>
		</p>
		<p>
			Anzahl aktiver Events: <strong>${activeEventCount}</strong>
		</p>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />