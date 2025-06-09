<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event Details" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<div class="details-container">
	<h1>${event.name}</h1>
	<p class="details-date">
		<java-time:format value="${event.eventDateTime}"
			pattern="EEEE, dd. MMMM yyyy 'um' HH:mm" />
		Uhr
	</p>

	<div class="card">
		<h2 class="card-title">Beschreibung</h2>
		<p>${event.description}</p>
	</div>

	<div class="card">
		<h2 class="card-title">Benötigte Qualifikationen</h2>
		<c:if test="${empty event.skillRequirements}">
			<p>Für dieses Event werden keine speziellen Qualifikationen
				benötigt.</p>
		</c:if>
		<ul>
			<c:forEach var="req" items="${event.skillRequirements}">
				<li>${req.courseName}:${req.requiredPersons} Person(en)</li>
			</c:forEach>
		</ul>
	</div>

	<%-- Ersetzen Sie den bestehenden Admin-Block durch diesen --%>
	<c:if test="${sessionScope.user.role == 'ADMIN'}">
		<div class="card">
			<h2 class="card-title">Teilnehmer-Status</h2>

			<c:choose>
				<c:when test="${not empty signedUpUsers}">
					<ul>
						<c:forEach var="u" items="${signedUpUsers}">
							<li>${u.username}(<c:out value="${u.role}" />)
							</li>
						</c:forEach>
					</ul>
				</c:when>
				<c:otherwise>
					<p>Bisher hat sich niemand für dieses Event angemeldet.</p>
				</c:otherwise>
			</c:choose>

		</div>
	</c:if>
</div>
<style>
.details-date {
	font-style: italic;
	color: #666;
	margin-top: -1rem;
	margin-bottom: 1.5rem;
}
</style>
<c:import url="/WEB-INF/jspf/footer.jspf" />