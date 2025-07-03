<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%-- CORRECTED: Import uses absolute path and correct filename --%>
<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Meeting-Details" />
</c:import>

<div style="max-width: 800px; margin: 0 auto;">
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
		<ul style="list-style: none; padding: 0;">
			<li
				style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Termin:</strong>
				<c:out value="${meeting.formattedMeetingDateTimeRange}" /></li>
			<li
				style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Ort:</strong>
				<c:out
					value="${not empty meeting.location ? meeting.location : 'N/A'}" /></li>
			<li
				style="padding: 0.75rem 0; display: flex; justify-content: space-between;"><strong>Leitung:</strong>
				<c:out
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
				<ul style="list-style: none; padding: 0;">
					<c:forEach var="att" items="${attachments}">
						<li
							style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between; align-items: center;"><a
							href="${pageContext.request.contextPath}/download?file=${att.filepath}"><c:out
									value="${att.filename}" /></a> <c:if
								test="${sessionScope.user.roleName == 'ADMIN'}">
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
		style="margin-top: 1rem;"> « Zurück zur Übersicht </a>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />