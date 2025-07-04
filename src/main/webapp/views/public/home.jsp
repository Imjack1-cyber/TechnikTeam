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
		<h2 class="card-title">Meine nächsten Einsätze</h2>
		<c:choose>
			<c:when test="${not empty assignedEvents}">
				<ul class="details-list">
					<c:forEach var="event" items="${assignedEvents}">
						<li><a
							href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
									value="${event.name}" /></a> <small><c:out
									value="${event.formattedEventDateTimeRange}" /></small></li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>Du bist derzeit für keine kommenden Events fest eingeteilt.</p>
			</c:otherwise>
		</c:choose>
		<a href="${pageContext.request.contextPath}/veranstaltungen"
			class="btn btn-small" style="margin-top: 1rem;">Alle
			Veranstaltungen anzeigen</a>
	</div>

	<div class="card">
		<h2 class="card-title">Meine offenen Aufgaben</h2>
		<c:choose>
			<c:when test="${not empty openTasks}">
				<ul class="details-list">
					<c:forEach var="task" items="${openTasks}">
						<li><a
							href="${pageContext.request.contextPath}/veranstaltungen/details?id=${task.eventId}">
								<c:out value="${task.description}" /> <small
								style="display: block; color: var(--text-muted-color);">Für
									Event: <c:out value="${task.eventName}" />
							</small>
						</a></li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>Super! Du hast aktuell keine offenen Aufgaben.</p>
			</c:otherwise>
		</c:choose>
	</div>

	<div class="card">
		<h2 class="card-title">Weitere anstehende Veranstaltungen</h2>
		<c:choose>
			<c:when test="${not empty upcomingEvents}">
				<ul class="details-list">
					<c:forEach var="event" items="${upcomingEvents}">
						<li><a
							href="${pageContext.request.contextPath}/veranstaltungen/details?id=${event.id}"><c:out
									value="${event.name}" /></a> <small><c:out
									value="${event.formattedEventDateTimeRange}" /></small></li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>Keine weiteren anstehenden Veranstaltungen.</p>
			</c:otherwise>
		</c:choose>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />