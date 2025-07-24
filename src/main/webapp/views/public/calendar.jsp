<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Kalender" />
</c:import>

<h1>
	<i class="fas fa-calendar-alt"></i> Terminübersicht
</h1>
<p>
	Übersicht aller anstehenden Veranstaltungen und Lehrgänge. <a
		href="<c:url value='/calendar.ics'/>" class="btn btn-small btn-info"
		style="margin-left: 1rem;"> <i class="fas fa-rss"></i> Kalender
		abonnieren
	</a>
</p>

<!-- Unified Calendar View for all devices -->
<div id="calendar-container" class="card"></div>


<c:import url="/WEB-INF/jspf/main_footer.jspf" />

<script
	src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/main.min.js"></script>
<script
	src="https://cdn.jsdelivr.net/npm/fullcalendar@5.11.3/locales-all.min.js"></script>
<script src="${pageContext.request.contextPath}/js/public/calendar.js"></script>