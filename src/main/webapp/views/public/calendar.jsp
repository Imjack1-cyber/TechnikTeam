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

<!-- Mobile List View -->
<div class="mobile-list-view">
	<div class="termin-container">
		<c:if test="${empty groupedEntries}">
			<div class="card">
				<p>Derzeit sind keine Termine geplant.</p>
			</div>
		</c:if>

		<c:forEach var="entry" items="${groupedEntries}">
			<h2 class="termin-month-header">${entry.key}</h2>
			<ul class="termin-list">
				<c:forEach var="termin" items="${entry.value}">
					<a href="${termin.url}" class="termin-item-link">
						<li class="termin-item">
							<div class="termin-date">
								<span class="termin-date-day">${termin.day}</span> <span
									class="termin-date-month">${termin.monthAbbr}</span>
							</div>
							<div class="termin-details">
								<span class="termin-title">${termin.title}</span> <span
									class="status-badge ${termin.typeClass}">${termin.type}</span>
							</div>
							<div class="termin-arrow">
								<i class="fas fa-chevron-right"></i>
							</div>
					</li>
					</a>
				</c:forEach>
			</ul>
		</c:forEach>
	</div>
</div>

<!-- Desktop Full Calendar View -->
<div id="calendar-container" class="desktop-calendar-view card"></div>


<c:import url="/WEB-INF/jspf/main_footer.jspf" />

<script
	src="${pageContext.request.contextPath}/vendor/fullcalendar/main.global.min.js"></script>
<script
	src="${pageContext.request.contextPath}/vendor/fullcalendar/locales/de.js"></script>
<script src="${pageContext.request.contextPath}/js/public/calendar.js"></script>