<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Teilnehmer zuweisen"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Teilnehmer für "${event.name}" zuweisen</h1>

<div class="card">
    <p>Wählen Sie die Benutzer aus, die diesem Event final zugewiesen werden sollen. Alle anderen Anmeldungen werden ignoriert.</p>
    <form action="${pageContext.request.contextPath}/admin/events" method="post">
        <input type="hidden" name="action" value="saveAssignments">
        <input type="hidden" name="eventId" value="${event.id}">

        <div class="form-group">
            <h4>Angemeldete Benutzer:</h4>
            <c:forEach var="user" items="${signedUpUsers}">
                <label style="display: block; margin-bottom: 0.5rem;">
                    <input type="checkbox" name="userIds" value="${user.id}" 
                           <c:if test="${assignedUserIds.contains(user.id)}">checked</c:if>
                    >
                    ${user.username}
                </label>
            </c:forEach>
            <c:if test="${empty signedUpUsers}">
                <p>Es haben sich noch keine Benutzer für dieses Event angemeldet.</p>
            </c:if>
        </div>
        
        <button type="submit" class="btn">Zuweisung speichern & Event als "Komplett" markieren</button>
    </form>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />