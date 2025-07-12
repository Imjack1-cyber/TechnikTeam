<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Meeting Details" />
</c:import>

<h1>
	<c:out value="${meeting.parentCourseName}" />
</h1>
<h2 style="border: none; padding: 0; margin-top: -1rem;">
	<c:out value="${meeting.name}" />
</h2>

<div class="dashboard-grid"
	style="grid-template-columns: 2fr 1fr; align-items: flex-start;">

	<div class="card">
		<ul class="details-list">
			<li><strong>Datum & Uhrzeit:</strong> <span><c:out
						value="${meeting.formattedMeetingDateTimeRange}" /></span></li>
			<li><strong>Ort:</strong> <span><c:out
						value="${not empty meeting.location ? meeting.location : 'N/A'}" /></span></li>
			<li><strong>Leitung:</strong> <span><c:out
						value="${not empty meeting.leaderUsername ? meeting.leaderUsername : 'N/A'}" /></span></li>
		</ul>
		<h3 style="margin-top: 2rem;">Beschreibung</h3>
		<div class="markdown-content">${fn:escapeXml(not empty meeting.description ? meeting.description : 'Keine Beschreibung für dieses Meeting vorhanden.')}
		</div>
	</div>

	<div class="card">
		<h2 class="card-title">Anhänge</h2>
		<c:if test="${empty attachments}">
			<p>Für dieses Meeting sind keine Anhänge verfügbar.</p>
		</c:if>
		<ul class="details-list">
			<c:forEach var="att" items="${attachments}">
				<li><a href="<c:url value='/download?id=${att.id}'/>"><c:out
							value="${att.filename}" /></a> <c:if
						test="${sessionScope.user.id == meeting.leaderUserId or sessionScope.user.permissions.contains('ACCESS_ADMIN_PANEL')}">
						<span class="text-muted">(<c:out
								value="${att.requiredRole}" />)
						</span>
					</c:if></li>
			</c:forEach>
		</ul>
	</div>

</div>

<div style="margin-top: 1rem;">
	<a href="${pageContext.request.contextPath}/lehrgaenge"
		class="btn btn-secondary"> <i class="fas fa-arrow-left"></i>
		Zurück zu allen Lehrgängen
	</a>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />