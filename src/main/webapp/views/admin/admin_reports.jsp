<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

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
				href="<c:url value='/admin/berichte?report=event_participation'/>">Teilnahme-Zusammenfassung</a>
				<p class="text-muted" style="margin: 0; padding: 0;">Zeigt die
					Anzahl der Anmeldungen pro Event.</p></li>
			<li><a
				href="<c:url value='/admin/berichte?report=inventory_usage'/>">Nutzungsfrequenz
					(Material)</a>
				<p class="text-muted" style="margin: 0; padding: 0;">Zeigt,
					welche Artikel am häufigsten entnommen werden.</p></li>
			<li><span>Gesamtwert des Lagers</span> <span
				style="font-weight: bold;"> <fmt:setLocale value="de_DE" />
					<fmt:formatNumber value="${totalInventoryValue}" type="currency" />
			</span></li>
		</ul>
	</div>
</div>

<script id="eventTrendData" type="application/json">
    <c:out value="${eventTrendDataJson}" />
</script>
<script id="userActivityData" type="application/json">
    <c:out value="${userActivityDataJson}" />
</script>


<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_reports.js"></script>