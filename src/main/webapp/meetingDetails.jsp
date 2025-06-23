<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  meetingDetails.jsp
  
  This is the public-facing detail page for a single course meeting.
  It displays the meeting's name, parent course, date/time, leader,
  description, and a list of any file attachments visible to the current user.
  
  - It is served by: MeetingDetailsServlet.
  - Expected attributes:
    - 'meeting' (de.technikteam.model.Meeting): The meeting being displayed.
    - 'attachments' (List<MeetingAttachment>): A list of attachments for the meeting.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Meeting-Details" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="details-container" style="max-width: 800px; margin: 0 auto;">
	<h1>
		<c:out value="${meeting.name}" />
	</h1>
	<p
		style="color: var(--text-muted-color); margin-top: -1rem; margin-bottom: 2rem;">
		Teil des Lehrgangs: <strong><c:out
				value="${meeting.parentCourseName}" /></strong>
	</p>

	<div class="card">
		<h2>Details</h2>
		<ul class="details-list">
			<li><strong>Termin:</strong> <c:out
					value="${meeting.formattedMeetingDateTimeRange}" /></li>
			<li><strong>Leitung:</strong> <c:out
					value="${not empty meeting.leaderUsername ? meeting.leaderUsername : 'N/A'}" /></li>
		</ul>
	</div>

	<div class="card">
		<h2>Beschreibung</h2>
		<p>
			<c:out
				value="${not empty meeting.description ? meeting.description : 'Keine Beschreibung vorhanden.'}" />
		</p>
	</div>

	<div class="card">
		<h2>Anhänge</h2>
		<c:choose>
			<c:when test="${not empty attachments}">
				<ul class="details-list">
					<c:forEach var="att" items="${attachments}">
						<li><a
							href="${pageContext.request.contextPath}/download?file=${att.filepath}"><c:out
									value="${att.filename}" /></a> <c:if
								test="${sessionScope.user.role == 'ADMIN'}">
								<small
									style="color: var(--text-muted-color); margin-left: 1rem;">(Sichtbar
									für: <c:out value="${att.requiredRole}" />)
								</small>
							</c:if></li>
					</c:forEach>
				</ul>
			</c:when>
			<c:otherwise>
				<p>Für dieses Meeting gibt es keine Anhänge.</p>
			</c:otherwise>
		</c:choose>
	</div>

	<a href="${pageContext.request.contextPath}/lehrgaenge" class="btn"
		style="margin-top: 1rem;"> &laquo; Zurück zur Übersicht </a>
</div>

<style>
.details-list {
	list-style-type: none;
	padding-left: 0;
}

.details-list li {
	padding: 0.75rem 0;
	border-bottom: 1px solid var(--border-color);
	display: flex;
	justify-content: space-between;
	align-items: center;
}

.details-list li:last-child {
	border-bottom: none;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />