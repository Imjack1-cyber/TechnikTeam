<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Meeting-Details" />
</c:import>

<div style="max-width: 800px; margin: 0 auto;">
	<h1>
		<c:out value="${meeting.name}" />
	</h1>
	<p class="details-subtitle" style="margin-top: -1rem;">
		Teil des Lehrgangs: <strong><c:out
				value="${meeting.parentCourseName}" /></strong>
	</p>

	<div class="card">
		<h2 class="card-title">Details</h2>
		<ul class="details-list">
			<li><strong>Termin:</strong><span><c:out
						value="${meeting.formattedMeetingDateTimeRange}" /></span></li>
			<li><strong>Ort:</strong><span><c:out
						value="${not empty meeting.location ? meeting.location : 'N/A'}" /></span></li>
			<li><strong>Leitung:</strong><span><c:out
						value="${not empty meeting.leaderUsername ? meeting.leaderUsername : 'N/A'}" /></span></li>
		</ul>
	</div>

	<div class="card">
		<h2 class="card-title">Beschreibung</h2>
		<p>
			<c:out
				value="${not empty meeting.description ? meeting.description : 'Keine Beschreibung vorhanden.'}" />
		</p>
	</div>

	<div class="card">
		<h2 class="card-title">Anhänge</h2>
		<ul class="details-list">
			<c:if test="${empty attachments}">
				<li style="justify-content: center;">Für dieses Meeting gibt es
					keine Anhänge.</li>
			</c:if>
			<c:forEach var="att" items="${attachments}">
				<li><a
					href="${pageContext.request.contextPath}/download?file=${att.filepath}"><i
						class="fas fa-download"></i> <c:out value="${att.filename}" /></a> <c:if
						test="${sessionScope.user.permissions.contains('ACCESS_ADMIN_PANEL')}">
						<small class="text-muted">(Sichtbar für: <c:out
								value="${att.requiredRole}" />)
						</small>
					</c:if></li>
			</c:forEach>
		</ul>
	</div>

	<a href="${pageContext.request.contextPath}/lehrgaenge" class="btn"
		style="margin-top: 1rem;"> <i class="fas fa-arrow-left"></i>
		Zurück zur Übersicht
	</a>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />