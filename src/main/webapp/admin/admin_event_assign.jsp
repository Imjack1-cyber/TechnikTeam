<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Teilnehmer zuweisen" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<!-- 
admin_event_assign.jsp: This form is used to finalize the team for an event by selecting from the list of users who signed up.

    Served by: AdminEventServlet (doGet with action=assign).

    Submits to: AdminEventServlet (doPost with action=assignUsers).

    Dependencies: Includes header.jspf, admin_navigation.jspf, footer.jspf.
-->

<h1>Teilnehmer für "${event.name}" zuweisen</h1>
<p>Wählen Sie die Benutzer aus, die dem finalen Team für dieses
	Event angehören sollen.</p>

<div class="card">
	<form action="${pageContext.request.contextPath}/admin/events"
		method="post">
		<input type="hidden" name="action" value="assignUsers"> <input
			type="hidden" name="eventId" value="${event.id}">

		<h2 class="card-title">Angemeldete Benutzer</h2>

		<c:choose>
			<c:when test="${not empty signedUpUsers}">
				<div class="user-checkbox-list">
					<c:forEach var="user" items="${signedUpUsers}">
						<label class="checkbox-label"> <%-- The name="userIds" is crucial. It creates an array of values. --%>
							<input type="checkbox" name="userIds" value="${user.id}"
							<%-- Pre-check the box if this user's ID is in the list of already assigned users --%>
                                <c:if test="${assignedUserIds.contains(user.id)}">checked</c:if>>
							${user.username}
						</label>
					</c:forEach>
				</div>
			</c:when>
			<c:otherwise>
				<p>Es haben sich noch keine Benutzer für dieses Event
					angemeldet.</p>
			</c:otherwise>
		</c:choose>

		<div style="margin-top: 2rem;">
			<button type="submit" class="btn">Team finalisieren</button>
			<a href="${pageContext.request.contextPath}/admin/events" class="btn"
				style="background-color: #6c757d;">Abbrechen</a>
		</div>
	</form>
</div>

<style>
.user-checkbox-list {
	display: flex;
	flex-direction: column;
	gap: 0.5rem;
}

.checkbox-label {
	display: flex;
	align-items: center;
	gap: 0.5rem;
	font-size: 1.1rem;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />