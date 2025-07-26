<%-- src/main/webapp/views/admin/admin_reports.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Berichte & Analysen" />
</c:import>
<h1>
	<i class="fas fa-chart-pie"></i> Berichte & Analysen
</h1>
<p>Hier finden Sie zusammengefasste Daten und Analysen über die
	Anwendungsnutzung.</p>
<div class="dashboard-grid">
	<div class="card" style="grid-column: 1/-1;">
		<h2 class="card-title">Event-Trend (Letzte 12 Monate)</h2>
		<div style="position: relative; height: 300px;">
			<canvas id="eventTrendChart"></canvas>
		</div>
	</div>
	<div class="card">
		<h2 class="card-title">Top 10 Aktivste Benutzer</h2>
		<div style="position: relative; height: 400px;">
			<canvas id="userActivityChart"></canvas>
		</div>
		<a href="<c:url value='/admin/berichte?report=user_activity'/>"
			class="btn btn-small" style="margin-top: 1rem;">Vollständiger
			Bericht</a>
	</div>
	<div class="card">
		<h2 class="card-title">Sonstige Berichte</h2>
		<ul class="details-list">
			<li><a
				href="<c:url value='/admin/berichte?report=event_participation'/>">Teilnahme-Zusammenfassung</a></li>
			<li><a
				href="<c:url value='/admin/berichte?report=inventory_usage'/>">Nutzungsfrequenz
					(Material)</a></li>
			<li><span>Gesamtwert des Lagers</span> <span
				id="total-inventory-value" style="font-weight: bold;">Lade...</span></li>
		</ul>
	</div>
</div>
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
<script
	src="${pageContext.request.contextPath}/js/admin/admin_reports.js"></script>