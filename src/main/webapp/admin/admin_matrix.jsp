<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf"><c:param name="title" value="Qualifikations-Matrix"/></c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Qualifikations-Matrix</h1>
<p>Klicken Sie auf eine Zelle, um den Status einer Qualifikation zu bearbeiten.</p>

<c:if test="${not empty sessionScope.successMessage}"><p class="success-message">${sessionScope.successMessage}</p><c:remove var="successMessage" scope="session" /></c:if>
<c:if test="${not empty sessionScope.errorMessage}"><p class="error-message">${sessionScope.errorMessage}</p><c:remove var="errorMessage" scope="session" /></c:if>

<div class="card" style="overflow-x: auto;">
    <table class="styled-table matrix-table">
        <thead>
            <tr>
                <th>Benutzer</th>
                <%-- KORREKTUR: Iteriere über allCourses, um die Spaltenköpfe zu erstellen --%>
                <c:forEach var="course" items="${allCourses}">
                    <th title="${course.name}">${course.abbreviation}</th>
                </c:forEach>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="user" items="${allUsers}">
                <tr>
                    <td>
                        <a href="${pageContext.request.contextPath}/admin/users?action=details&id=${user.id}">${user.username}</a>
                    </td>
                    <c:forEach var="course" items="${allCourses}">
                        <%-- Die restliche Logik mit dem Map-Lookup bleibt gleich --%>
                        <c:set var="lookupKey" value="${user.id}-${course.id}" />
                        <c:set var="qual" value="${qualificationMap[lookupKey]}" />
                        <td class="qual-cell" data-user-id="${user.id}" data-user-name="${user.username}" ...>
                            <c:choose>
                                <c:when test="${qual.status == 'ABSOLVIERT'}">A</c:when>
                                <c:when test="${qual.status == 'BESUCHT'}">X</c:when>
                                <c:otherwise>-</c:otherwise>
                            </c:choose>
                        </td>
                    </c:forEach>
                </tr>
            </c:forEach>
        </tbody>
    </table>
</div>

<!-- Modal für die Bearbeitung -->
<div id="qual-modal" class="lightbox" style="display: none;">
    <div class="modal-content">
        <span class="close-btn">×</span>
        <h3>Qualifikation bearbeiten</h3>
        <h4 id="modal-title"></h4>
        <form action="${pageContext.request.contextPath}/admin/users" method="post">
            <input type="hidden" name="action" value="updateQualification">
            <input type="hidden" name="userId" id="modal-user-id">
            <input type="hidden" name="courseId" id="modal-course-id">
            
            <div class="form-group"><label>Status</label><select name="status" id="modal-status"><option value="NONE">Kein Eintrag</option><option value="BESUCHT">Besucht</option><option value="ABSOLVIERT">Absolviert</option></select></div>
            <div class="form-group"><label>Absolviert am</label><input type="date" name="completionDate" id="modal-date"></div>
            <div class="form-group"><label>Bemerkungen</label><textarea name="remarks" id="modal-remarks" rows="3"></textarea></div>
            
            <button type="submit" class="btn">Speichern</button>
        </form>
    </div>
</div>

<style>.matrix-table th, .matrix-table td { text-align: center; } .matrix-table th:first-child, .matrix-table td:first-child { text-align: left; } .qual-cell { cursor: pointer; font-weight: bold; font-size: 1.2em; } .qual-cell:hover { background-color: var(--secondary-color); } .modal-content { background-color: var(--card-bg); padding: 20px; border-radius: 5px; width: 90%; max-width: 500px; position: relative; } .close-btn { position: absolute; top: 10px; right: 20px; font-size: 30px; cursor: pointer; }</style>
<script>
document.querySelectorAll('.qual-cell').forEach(cell => {
    cell.addEventListener('click', () => {
        const modal = document.getElementById('qual-modal');
        document.getElementById('modal-title').innerText = `Nutzer: ${cell.dataset.userName} | Lehrgang: ${cell.dataset.courseName}`;
        document.getElementById('modal-user-id').value = cell.dataset.userId;
        document.getElementById('modal-course-id').value = cell.dataset.courseId;
        document.getElementById('modal-status').value = cell.dataset.status;
        document.getElementById('modal-date').value = cell.dataset.date === 'null' ? '' : cell.dataset.date;
        document.getElementById('modal-remarks').value = cell.dataset.remarks === 'null' ? '' : cell.dataset.remarks;
        modal.style.display = 'flex';
    });
});
document.querySelector('.close-btn').addEventListener('click', () => document.getElementById('qual-modal').style.display = 'none');
window.addEventListener('click', (event) => { if (event.target == document.getElementById('qual-modal')) { document.getElementById('qual-modal').style.display = 'none'; } });
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />