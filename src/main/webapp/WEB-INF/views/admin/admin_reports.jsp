<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Berichte & Analysen" />
</c:import>

<h1>
	<i class="fas fa-chart-pie"></i> Berichte & Analysen
</h1>
<p>Hier finden Sie zusammengefasste Daten und Analysen über die
	Anwendungsnutzung.</p>

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">Event-Berichte</h2>
		<ul class="details-list">
			<li><a
				href="<c:url value='/admin/reports?report=event_participation'/>">Teilnahme-Zusammenfassung</a>
				<p class="text-muted" style="margin: 0; padding: 0;">Zeigt die
					Anzahl der Anmeldungen pro Event.</p></li>
		</ul>
	</div>
	<div class="card">
		<h2 class="card-title">Benutzer-Berichte</h2>
		<ul class="details-list">
			<li><a
				href="<c:url value='/admin/reports?report=user_activity'/>">Benutzeraktivität</a>
				<p class="text-muted" style="margin: 0; padding: 0;">Zeigt, wie
					aktiv einzelne Benutzer sind.</p></li>
		</ul>
	</div>
	<div class="card">
		<h2 class="card-title">Lager-Berichte</h2>
		<ul class="details-list">
			<li><a
				href="<c:url value='/admin/reports?report=inventory_usage'/>">Nutzungsfrequenz</a>
				<p class="text-muted" style="margin: 0; padding: 0;">Zeigt,
					welche Artikel am häufigsten entnommen werden.</p></li>
			<li><span>Gesamtwert des Lagers</span> <span
				style="font-weight: bold;"> <fmt:setLocale value="de_DE" /> <fmt:formatNumber
						value="${totalInventoryValue}" type="currency" />
			</span></li>
		</ul>
	</div>
</div>

<c:import url="../../jspf/main_footer.jspf" />