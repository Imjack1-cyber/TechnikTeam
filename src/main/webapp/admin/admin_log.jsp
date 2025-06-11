<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Admin Logs"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Admin Aktions-Log</h1>
<div class="card">
    <table class="styled-table">
        <thead><tr><th>Zeitpunkt</th><th>Admin</th><th>Aktion</th><th>Details</th></tr></thead>
        <tbody>
            <c:forEach var="log" items="${logs}">
                <tr>
                    <td>${log.actionTimestamp}</td>
                    <td>${log.adminUsername}</td>
                    <td>${log.actionType}</td>
                    <td>${log.details}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />