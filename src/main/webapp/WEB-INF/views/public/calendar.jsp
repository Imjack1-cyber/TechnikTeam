<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
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

<link
	href='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/main.min.css'
	rel='stylesheet' />
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/main.min.js'></script>
<script
	src='https://cdn.jsdelivr.net/npm/fullcalendar@5.11.0/locales/de.js'></script>

<c:import url="../../jspf/footer.jspf" />

<script>
	document.addEventListener('DOMContentLoaded', function() {
		var calendarEl = document.getElementById('calendar');
		var calendar = new FullCalendar.Calendar(calendarEl, {
			initialView : 'dayGridMonth',
			locale : 'de',
			headerToolbar : {
				left : 'prev,next today',
				center : 'title',
				right : 'dayGridMonth,timeGridWeek,listWeek'
			},
			events : '${pageContext.request.contextPath}/api/calendar/entries',
			eventClick : function(info) {
				info.jsEvent.preventDefault(); // don't let the browser navigate
				if (info.event.url) {
					window.open(info.event.url, "_self");
				}
			}
		});
		calendar.render();
	});
</script>