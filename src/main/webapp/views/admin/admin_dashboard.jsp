<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Admin Dashboard" />
</c:import>

<h1>
	Willkommen im Admin-Bereich,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>
<p>Hier können Sie die Anwendung verwalten. Wählen Sie eine Option
	aus der Navigation oder nutzen Sie den Schnellzugriff.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="dashboard-grid">
	<div class="card" id="widget-upcoming-events">
		<h2>
			<i class="fas fa-calendar-check"></i> Nächste Einsätze
		</h2>
		<p>Lade Daten...</p>
	</div>

	<div class="card" id="widget-low-stock">
		<h2>
			<i class="fas fa-battery-quarter"></i> Niedriger Lagerbestand
		</h2>
		<p>Lade Daten...</p>
	</div>

	<div class="card">
		<h2 class="card-title">
			<i class="fas fa-exclamation-triangle text-danger"></i> Defekte
			Artikel
		</h2>
		<p>
			Es sind aktuell <strong><c:out
					value="${fn:length(defectiveItems)}" /></strong> Artikel als defekt
			gemeldet.
		</p>
		<a href="${pageContext.request.contextPath}/admin/defekte"
			class="btn btn-small" style="margin-top: 1rem;">Defekte anzeigen</a>
	</div>

	<div class="card" id="widget-recent-logs">
		<h2>
			<i class="fas fa-history"></i> Letzte Aktivitäten
		</h2>
		<p>Lade Daten...</p>
	</div>
</div>

<div class="card" style="margin-top: 2rem;">
	<h2 class="card-title">Event-Trend (Letzte 12 Monate)</h2>
	<div style="position: relative; height: 300px;">
		<canvas id="eventTrendChart"></canvas>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script
	src="${pageContext.request.contextPath}/js/admin/admin_dashboard.js"></script>