<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Home" />
	<c:param name="navType" value="user" />
</c:import>

<h1>
	Willkommen zurück,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>

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
							href="${pageContext.request.contextPath}/eventDetails?id=${event.id}"><c:out
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
							href="${pageContext.request.contextPath}/meetingDetails?id=${meeting.id}"><c:out
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
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />