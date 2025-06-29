<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Admin Dashboard" />
</c:import>

<h1>
	Willkommen im Admin-Bereich,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>
<p>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option
	aus der Navigation oder nutzen Sie den Schnellzugriff.</p>

<c:import url="../../jspf/message_banner.jspf" />

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-rocket"></i> Schnellzugriff
		</h2>
		<ul style="list-style: none; padding: 0;">
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/events">Neue
					Veranstaltung anlegen</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/users">Neuen
					Benutzer erstellen</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/storage">Neuen
					Lagerartikel anlegen</a></li>
			<li style="padding: 0.5rem 0;"><a
				href="${pageContext.request.contextPath}/admin/courses">Neue
					Lehrgangs-Vorlage erstellen</a></li>
		</ul>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-chart-bar"></i> System-Statistiken
		</h2>
		<p style="font-size: 1.1rem; margin-bottom: 0.5rem;">
			Anzahl registrierter Benutzer: <strong><c:out
					value="${userCount}" /></strong>
		</p>
		<p style="font-size: 1.1rem;">
			Anzahl aktiver Events: <strong><c:out
					value="${activeEventCount}" /></strong>
		</p>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-exclamation-triangle text-warning"></i> Defekte
			Artikel
		</h2>
		<p>
			Es sind aktuell <strong><c:out
					value="${fn:length(defectiveItems)}" /></strong> Artikel als defekt
			gemeldet.
		</p>
		<a href="${pageContext.request.contextPath}/admin/defects"
			class="btn btn-small" style="margin-top: 1rem;">Defekte anzeigen</a>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-history"></i> Letzte Aktionen
		</h2>
		<p>Die letzten Log-Einträge des Systems.</p>
		<a href="${pageContext.request.contextPath}/admin/log"
			class="btn btn-small" style="margin-top: 1rem;">Komplettes Log
			ansehen</a>
	</div>
</div>

<c:import url="../../jspf/main_footer.jspf" />