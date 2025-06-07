<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Teilnahmehistorie"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Teilnahmehistorie</h1>
<table class="styled-table">
    <%-- ... table header (Benutzer, Event, Datum, Status) ... --%>
    <tbody>
        <c:set var="currentUser" value=""/>
        <c:forEach var="entry" items="${history}">
            <c:if test="${entry.username != currentUser}">
                <tr style="background-color: var(--secondary-color);"><td colspan="4"><strong>${entry.username}</strong></td></tr>
                <c:set var="currentUser" value="${entry.username}"/>
            </c:if>
            <tr>
                <td></td>
                <td>${entry.eventName}</td>
                <td>${entry.eventDate}</td>
                <td>${entry.status}</td>
            </tr>
        </c:forEach>
    </tbody>
</table>
<c:import url="/WEB-INF/jspf/footer.jspf" />