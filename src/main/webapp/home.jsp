<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  home.jsp
  
  This is the main landing page (dashboard) for a logged-in user. It provides
  a welcome message and a quick overview of their upcoming commitments,
  displaying the next few events and meetings they are eligible for or
  signed up for.
  
  - It is served by: HomeServlet.
  - Expected attributes:
    - 'upcomingEvents' (List<de.technikteam.model.Event>): A list of the next 3 events.
    - 'upcomingMeetings' (List<de.technikteam.model.Meeting>): A list of the next 3 meetings.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Home" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

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