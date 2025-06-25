<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Admin Dashboard" />
	<c:param name="navType" value="admin" />
</c:import>

<h1>
	Willkommen im Admin-Bereich,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>
<p>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option
	aus der Navigation.</p>

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">Schnellzugriff</h2>
		<ul style="list-style: none; padding: 0;">
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/users">Benutzer
					verwalten</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/events">Events
					erstellen & bearbeiten</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/courses">Lehrgänge
					& Meetings verwalten</a></li>
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