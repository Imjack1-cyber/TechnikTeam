<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Ressourcen-Kalender" />
</c:import>

<h1>
	<i class="fas fa-calendar-alt"></i> Ressourcen-Belegung
</h1>
<p>Dieser Kalender zeigt, welches Material für welche Veranstaltung
	reserviert ist. Dies hilft, Doppelbelegungen zu erkennen.</p>

<div class="card">
	<div id="calendar"></div>
</div>

<link
	href='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.css'
	rel='stylesheet' />
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.js'></script>
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/locales/de.js'></script>
<script
	src='https://cdn.jsdelivr.net/npm/@fullcalendar/resource-timeline@5.11.3/main.global.min.js'></script>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/resource_calendar.js"></script>