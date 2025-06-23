<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  admin_dashboard.jsp
  
  This is the main landing page for the administrative area. It greets the
  admin user and displays quick access links to major management sections,
  as well as some basic application statistics.
  
  - It is served by: AdminDashboardServlet.
  - Expected attributes:
    - 'userCount' (int): The total number of registered users.
    - 'activeEventCount' (int): The number of upcoming events.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Admin Dashboard" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>
	Willkommen im Admin-Bereich,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>
<p>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option
	aus der Navigation oder den Schnellzugriffen.</p>

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">Schnellzugriff</h2>
		<ul style="list-style: none; padding: 0;">
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/users">Benutzer
					verwalten</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/events">Events
					erstellen &amp; bearbeiten</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/courses">Lehrgänge
					&amp; Meetings verwalten</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/files">Dateien
					hochladen</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/storage">Lager
					verwalten</a></li>
		</ul>
	</div>

	<div class="card">
		<h2 class="card-title">Statistiken</h2>
		<p style="font-size: 1.1rem; margin-bottom: 0.5rem;">
			Anzahl registrierter Benutzer: <strong><c:out
					value="${userCount}" /></strong>
		</p>
		<p style="font-size: 1.1rem;">
			Anzahl aktiver Events: <strong><c:out
					value="${activeEventCount}" /></strong>
		</p>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />