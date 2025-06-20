<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Admin Log"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Admin Aktions-Protokoll</h1>
<div class="card">
    <table class="styled-table">
        <thead><tr><th>Wann</th><th>Wer</th><th>Was</th></tr></thead>
        <tbody>
            <c:forEach var="log" items="${logs}">
                <tr>
                    <td data-label="Wann">${log.formattedActionTimestamp} Uhr</td>
                    <td data-label="Wer">${log.adminUsername}</td>
                    <td data-label="Was">${log.details}</td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />