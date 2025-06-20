<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Teilnehmer bestätigen" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Teilnehmer für "${event.name}" bestätigen</h1>
<p>Hier können Sie verbindliche Zusagen ("Kommt") für angemeldete
	Teilnehmer festlegen.</p>

<div class="card">
	<h2 class="card-title">Angemeldete Benutzer</h2>
	<c:choose>
		<c:when test="${not empty attendances}">
			<ul style="list-style: none; padding: 0;">
				<%-- Iteriert über die vollständigen EventAttendance-Objekte --%>
				<c:forEach var="att" items="${attendances}">
					<li
						style="display: flex; justify-content: space-between; align-items: center; padding: 0.5rem 0; border-bottom: 1px solid var(--border-color);">
						<span> ${att.username} - Aktueller Status: <strong
							class="${att.commitmentStatus == 'BESTÄTIGT' ? 'status-angemeldet' : ''}">
								${att.commitmentStatus} </strong>
					</span>

						<form action="${pageContext.request.contextPath}/admin/events"
							method="post" style="display: inline-flex; gap: 5px;">
							<input type="hidden" name="action" value="updateCommitment">
							<input type="hidden" name="eventId" value="${event.id}">
							<input type="hidden" name="userId" value="${att.userId}">

							<%-- Zeige Buttons nur an, wenn eine Änderung möglich ist --%>
							<c:if test="${att.commitmentStatus != 'BESTÄTIGT'}">
								<button type="submit" name="newStatus" value="BESTÄTIGT"
									class="btn-small">Bestätigen</button>
							</c:if>
							<c:if test="${att.commitmentStatus == 'BESTÄTIGT'}">
								<button type="submit" name="newStatus" value="OFFEN"
									class="btn-small btn-danger">Zurücksetzen</button>
							</c:if>
						</form>
					</li>
				</c:forEach>
				<%-- In der Schleife über die angemeldeten Benutzer --%>
				<c:forEach var="user" items="${signedUpUsers}">
					<label
						style="display: flex; justify-content: space-between; align-items: center; padding: 5px;">
						<span> <input type="checkbox" name="userIds"
							value="${user.id}"
							<c:if test="${assignedUserIds.contains(user.id)}">checked</c:if>>
							${user.username}
					</span> <%-- Zeige den Bestätigungs-Button nur an, wenn der Nutzer noch nicht zugesagt hat --%>
						<c:if test="${user.commitmentStatus != 'ZUGESAGT'}">
							<a
								href="${pageContext.request.contextPath}/admin/events?action=confirmAttendance&eventId=${event.id}&userId=${user.id}"
								class="btn-small">Zusage senden</a>
						</c:if> <c:if test="${user.commitmentStatus == 'ZUGESAGT'}">
							<span class="status-badge">Zugesagt</span>
						</c:if>
					</label>
				</c:forEach>
			</ul>
		</c:when>
		<c:otherwise>
			<p>Es haben sich noch keine Benutzer für dieses Event angemeldet.</p>
		</c:otherwise>
	</c:choose>
</div>
<div class="card" style="margin-top: 1rem;">
	<form action="${pageContext.request.contextPath}/admin/events"
		method="post">
		<input type="hidden" name="action" value="setEventComplete"> <input
			type="hidden" name="id" value="${event.id}">
		<button type="submit" class="btn">Team finalisieren & Event
			als "Komplett" markieren</button>
	</form>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />