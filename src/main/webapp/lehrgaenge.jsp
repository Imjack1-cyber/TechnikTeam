<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="java-time" uri="http://sargue.net/jsptags/time" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Lehrgänge"/></c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Anstehende Lehrgänge</h1>

<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>

<div class="filter-controls" style="margin-bottom: 1rem; display: flex; gap: 10px;">
    <button id="filter-all" class="btn-small active">Alle anzeigen</button>
    <button id="filter-open" class="btn-small">Nur noch nicht besuchte</button>
</div>

<div class="card">
    <table class="styled-table">
        <thead>
            <tr><th>Name</th><th>Typ</th><th>Datum & Uhrzeit</th><th>Leitung</th><th>Deine Teilnahme</th></tr>
        </thead>
        <tbody id="course-table-body">
            <c:forEach var="course" items="${courses}">
                <tr class="course-row" data-attended="${attendedCourseIds.contains(course.id) ? 'true' : 'false'}">
                    <td><a href="${pageContext.request.contextPath}/courseDetails?id=${course.id}">${course.name}</a></td>
                    <td>${course.type}</td>
                    <td><java-time:format value="${course.courseDateTime}" pattern="dd.MM.yyyy HH:mm" /> Uhr</td>
                    <td>${course.leader}</td>
                    <td>
                        <form action="${pageContext.request.contextPath}/course-action" method="post" class="attendance-form" style="display: flex; align-items: center; gap: 1rem;">
                            <input type="hidden" name="courseId" value="${course.id}">
                            <span class="attendance-status">
                                <c:choose>
                                    <c:when test="${course.userAttendanceStatus == 'ANGEMELDET'}"><span class="status-angemeldet">Angemeldet</span></c:when>
                                    <c:when test="${course.userAttendanceStatus == 'ABGEMELDET'}"><span class="status-abgemeldet">Abgemeldet</span></c:when>
                                    <c:otherwise>Offen</c:otherwise>
                                </c:choose>
                            </span>
                            <div class="attendance-buttons">
                                <c:if test="${course.userAttendanceStatus != 'ANGEMELDET'}"><button type="submit" name="action" value="signup" class="btn-small">Anmelden</button></c:if>
                                <c:if test="${course.userAttendanceStatus == 'ANGEMELDET'}"><button type="submit" name="action" value="signoff" class="btn-small btn-danger">Abmelden</button></c:if>
                            </div>
                        </form>
                    </td>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<!-- KORREKTUR: Angepasstes JavaScript -->
<script>
document.addEventListener('DOMContentLoaded', () => {
    // Referenzen auf die Filter-Buttons und die Tabellenzeilen holen
    const btnAll = document.getElementById('filter-all');
    const btnOpen = document.getElementById('filter-open');
    const rows = document.querySelectorAll('.course-row');

    /**
     * Hilfsfunktion, um den "aktiven" Style auf den geklickten Button zu setzen
     * und von den anderen zu entfernen.
     * @param {HTMLElement} activeButton Der Button, der aktiv sein soll.
     */
    function setActiveButton(activeButton) {
        btnAll.classList.remove('active');
        btnOpen.classList.remove('active');
        activeButton.classList.add('active');
    }

    // Event-Listener für den "Alle anzeigen"-Button
    btnAll.addEventListener('click', () => {
        setActiveButton(btnAll);
        // Iteriert durch alle Tabellenzeilen und macht sie sichtbar
        rows.forEach(row => {
            row.style.display = ''; // Setzt den display-Style zurück auf den Standard (sichtbar)
        });
    });
    
    // Event-Listener für den "Nur noch nicht besuchte"-Button
    btnOpen.addEventListener('click', () => {
        setActiveButton(btnOpen);
        rows.forEach(row => {
            // Überprüft das data-attended Attribut, das vom Server gesetzt wurde
            if (row.dataset.attended === 'false') {
                // Wenn der Lehrgang NICHT besucht wurde, Zeile anzeigen
                row.style.display = '';
            } else {
                // Wenn der Lehrgang bereits besucht wurde, Zeile ausblenden
                row.style.display = 'none';
            }
        });
    });
});
</script>

<style>
/* Optional: Style für den aktiven Filter-Button */
.filter-controls .btn-small.active {
	background-color: var(--primary-color);
	color: #fff;
	border: 1px solid var(--primary-color);
}

.filter-controls .btn-small {
	border: 1px solid var(--border-color);
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />