<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

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

<!-- Mobile List View (Default for small screens) -->
<div class="mobile-list-view">
	<c:if test="${empty mobileList}">
		<div class="card">
			<p>Derzeit sind keine Termine geplant.</p>
		</div>
	</c:if>

	<c:forEach var="entry" items="${mobileList}">
		<a href="${entry.url}" class="termin-item-link">
			<div class="termin-item">
				<div class="termin-date">
					<span class="termin-date-day">${entry.day}</span> <span
						class="termin-date-month">${entry.monthAbbr}</span>
				</div>
				<div class="termin-details">
					<span class="termin-title">${entry.title}</span> <span
						class="status-badge ${entry.typeClass}">${entry.type}</span>
				</div>
				<div class="termin-arrow">
					<i class="fas fa-chevron-right"></i>
				</div>
			</div>
		</a>
	</c:forEach>
</div>

<!-- Desktop Calendar View -->
<div class="desktop-calendar-view card">
	<div class="calendar-controls">
		<div>
			<a
				href="?view=month&year=${prevMonth.year}&month=${prevMonth.monthValue}"
				class="btn btn-secondary"><</a> <a
				href="?view=month&year=${nextMonth.year}&month=${nextMonth.monthValue}"
				class="btn btn-secondary">></a> <a href="?view=month"
				class="btn btn-secondary">Heute</a>
		</div>
		<h2>${monthName}${year}</h2>
		<div>
			<a href="?view=week"
				class="btn ${view == 'week' ? 'btn-primary' : 'btn-secondary'}">Woche</a>
			<a href="?view=month"
				class="btn ${view == 'month' ? 'btn-primary' : 'btn-secondary'}">Monat</a>
		</div>
	</div>

	<c:if test="${view == 'month'}">
		<div class="calendar-grid">
			<div class="calendar-header">So</div>
			<div class="calendar-header">Mo</div>
			<div class="calendar-header">Di</div>
			<div class="calendar-header">Mi</div>
			<div class="calendar-header">Do</div>
			<div class="calendar-header">Fr</div>
			<div class="calendar-header">Sa</div>

			<c:if test="${startDayOfWeekOffset > 0}">
				<c:forEach begin="1" end="${startDayOfWeekOffset}">
					<div class="calendar-day other-month"></div>
				</c:forEach>
			</c:if>

			<c:forEach var="day" begin="1" end="${daysInMonth}">
				<c:set var="dayDate" value="${currentYearMonth.atDay(day)}" />
				<div
					class="calendar-day ${currentDate.isEqual(dayDate) ? 'today' : ''}">
					<div class="day-number">${day}</div>
					<c:forEach var="entry" items="${eventsByDate[dayDate]}">
						<c:choose>
							<c:when test="${entry.type == 'Event'}">
								<a
									href="${pageContext.request.contextPath}/veranstaltungen/details?id=${entry.object.id}"
									class="calendar-event">${entry.object.name}</a>
							</c:when>
							<c:when test="${entry.type == 'Meeting'}">
								<a
									href="${pageContext.request.contextPath}/meetingDetails?id=${entry.object.id}"
									class="calendar-meeting">${entry.object.name}</a>
							</c:when>
						</c:choose>
					</c:forEach>
				</div>
			</c:forEach>
		</div>
	</c:if>

	<c:if test="${view == 'week'}">
		<div class="calendar-week-grid">
			<c:forEach var="day" items="${weekData}">
				<div class="calendar-week-day">
					<div class="calendar-header">
						${day.dayName} <span class="day-number">${day.dayOfMonth}</span>
					</div>
					<div class="events-container">
						<c:forEach var="entry" items="${eventsByDate[day.date]}">
							<c:choose>
								<c:when test="${entry.type == 'Event'}">
									<a
										href="${pageContext.request.contextPath}/veranstaltungen/details?id=${entry.object.id}"
										class="calendar-event">${entry.object.name}</a>
								</c:when>
								<c:when test="${entry.type == 'Meeting'}">
									<a
										href="${pageContext.request.contextPath}/meetingDetails?id=${entry.object.id}"
										class="calendar-meeting">${entry.object.name}</a>
								</c:when>
							</c:choose>
						</c:forEach>
					</div>
				</div>
			</c:forEach>
		</div>
	</c:if>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />