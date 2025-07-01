<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Home" />
</c:import>

<h1>
	Willkommen zurück,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>
<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="dashboard-grid">
	<div class="card">
		<h2 class="card-title">Deine nächsten 3 Veranstaltungen</h2>
		<c:choose>
			<c:when test="${not empty upcomingEvents}">
				<ul style="list-style: none; padding-left: 0;">
					<c:forEach var="event" items="${upcomingEvents}" varStatus="loop">
						<li
							style="padding: 0.5rem 0; ${!loop.last ? 'border-bottom: 1px solid var(--border-color);' : ''}">
							<a
							href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
									value="${event.name}" /></a> <br> <small><c:out
									value="${event.formattedEventDateTimeRange}" /></small>
						</li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>Keine anstehenden Veranstaltungen.</p>
			</c:otherwise>
		</c:choose>
		<%-- CORRECTED: Link points to /veranstaltungen as per servlet mapping. --%>
		<a href="${pageContext.request.contextPath}/veranstaltungen"
			class="btn btn-small" style="margin-top: 1rem;">Alle
			Veranstaltungen anzeigen</a>
	</div>
	<div class="card">
		<h2 class="card-title">Deine nächsten 3 Lehrgänge/Meetings</h2>
		<c:choose>
			<c:when test="${not empty upcomingMeetings}">
				<ul style="list-style: none; padding-left: 0;">
					<c:forEach var="meeting" items="${upcomingMeetings}"
						varStatus="loop">
						<li
							style="padding: 0.5rem 0; ${!loop.last ? 'border-bottom: 1px solid var(--border-color);' : ''}">
							<a
							href="${pageContext.request.contextPath}/meeting/details?id=${meeting.id}"><c:out
									value="${meeting.name}" /></a> <br> <small><c:out
									value="${meeting.formattedMeetingDateTimeRange}" /></small>
						</li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>Keine anstehenden Lehrgänge.</p>
			</c:otherwise>
		</c:choose>
		<a href="${pageContext.request.contextPath}/lehrgaenge"
			class="btn btn-small" style="margin-top: 1rem;">Alle Lehrgänge
			anzeigen</a>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />F