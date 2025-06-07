<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Event-Verwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Event-Verwaltung</h1>
<a href="${pageContext.request.contextPath}/admin/events?action=new"
	class="btn">Neues Event erstellen</a>
<table class="styled-table" style="margin-top: 1rem;">
	<%-- Table Header --%>
	<%-- In der <thead> --%>
	<th>Aktionen</th>

	<%-- In der <tbody> innerhalb der c:forEach-Schleife --%>
	<td><a
		href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}"
		class="btn-small">Bearbeiten</a> <%-- NEUER LINK ZUR ZUWEISUNGSSEITE --%>
		<a
		href="${pageContext.request.contextPath}/admin/events?action=assign&id=${event.id}"
		class="btn-small" style="background-color: var(--success-color);">Teilnehmer</a>

		<%-- Delete Form... --%></td>
	<tbody>
		<c:forEach var="event" items="${eventList}">
			<tr>
				<td>${event.name}</td>
				<td>${event.eventDateTime}</td>
				<td><a
					href="${pageContext.request.contextPath}/admin/events?action=edit&id=${event.id}"
					class="btn-small">Bearbeiten</a> <%-- Delete Form --%></td>
			</tr>
		</c:forEach>
	</tbody>
</table>
<c:import url="/WEB-INF/jspf/footer.jspf" />