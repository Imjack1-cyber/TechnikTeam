<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  admin_event_assign.jsp
  
  This JSP provides the interface for an administrator to assign the final team
  for an event. It displays a list of all users who have signed up for the event,
  with checkboxes to select them for the final assignment. Previously assigned
  users are pre-checked.
  
  - It is served by: AdminEventServlet (doGet with action=assign).
  - It submits to: AdminEventServlet (doPost with action=assignUsers).
  - Expected attributes:
    - 'event' (de.technikteam.model.Event): The event being managed.
    - 'signedUpUsers' (List<de.technikteam.model.User>): Users who signed up.
    - 'assignedUserIds' (Set<Integer>): The IDs of users already assigned.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Teilnehmer zuweisen" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Teilnehmer für "${event.name}" zuweisen</h1>
<p>Wählen Sie die Benutzer aus, die dem finalen Team für dieses
	Event angehören sollen. Nach dem Speichern wird der Event-Status
	automatisch auf "KOMPLETT" gesetzt.</p>

<div class="form-center-wrapper">
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
							<label class="checkbox-label"> <input type="checkbox"
								name="userIds" value="${user.id}"
								style="width: auto; height: 1.2rem;"
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

			<div style="margin-top: 2rem; display: flex; gap: 1rem;">
				<button type="submit" class="btn">Team finalisieren &
					Speichern</button>
				<a href="${pageContext.request.contextPath}/admin/events"
					class="btn" style="background-color: var(--text-muted-color);">Abbrechen</a>
			</div>
		</form>
	</div>
</div>

<style>
.user-checkbox-list {
	display: flex;
	flex-direction: column;
	gap: 0.75rem;
}

.checkbox-label {
	display: flex;
	align-items: center;
	gap: 0.75rem;
	font-size: 1.1rem;
	cursor: pointer;
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />