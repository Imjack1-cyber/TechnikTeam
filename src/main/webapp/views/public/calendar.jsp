<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Kalender" />
</c:import>

<h1>
	<i class="fas fa-calendar-alt"></i> Kalender
</h1>
<p>
	Übersicht aller anstehenden Veranstaltungen und Lehrgänge. <a
		href="<c:url value='/public/calendar.ics'/>"
		class="btn btn-small btn-info" style="margin-left: 1rem;"> <i
		class="fas fa-rss"></i> Kalender abonnieren
	</a>
</p>

<div class="card">
	<div id="calendar"></div>
</div>

<%-- FIX: Updated CDN links to a stable and correct version of FullCalendar --%>
<link
	href='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.css'
	rel='stylesheet' />
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/main.min.js'></script>
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.5/locales-all.min.js'></script>


<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/calendar.js"></script>