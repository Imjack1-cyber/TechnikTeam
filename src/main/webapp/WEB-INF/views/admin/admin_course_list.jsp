%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Lehrgangs-Vorlagen" />
</c:import>

<h1>Lehrgangs-Vorlagen verwalten</h1>
<p>Dies sind die übergeordneten Lehrgänge. Einzelne Termine (Meetings) werden für jede Vorlage separat verwaltet.</p>

<c:import url="../../jspf/message_banner.jspf"/>

<div class="table-controls">
	<button type="button" id="new-course-btn" class="btn">Neue Lehrgangs-Vorlage anlegen</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter" placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<div class="table-wrapper">
	<table class="data-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name der Vorlage</th>
				<th class="sortable" data-sort-type="string">Abkürzung (für Matrix)</th>
				<th style="width: 450px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
            <c:if test="${empty courseList}">
                <tr><td colspan="3" style="text-align: center;">Es wurden noch keine Lehrgangs-Vorlagen erstellt.</td></tr>
            </c:if>
			<c:forEach var="course" items="${courseList}">
				<tr>
					<td><c:out value="${course.name}" /></td>
					<td><c:out value="${course.abbreviation}" /></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;">
                        <a href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}" class="btn btn-small btn-success">Meetings verwalten</a>
						<button type="button" class="btn btn-small edit-course-btn" data-id="${course.id}">Vorlage bearbeiten</button>
						<form action="${pageContext.request.contextPath}/admin/courses" method="post" class="js-confirm-form" data-confirm-message="Vorlage '${fn:escapeXml(course.name)}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!">
							<input type="hidden" name="action" value="delete">
                            <input type="hidden" name="id" value="${course.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form>
                    </td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- MODAL FOR CREATE/EDIT COURSE -->
<div class="modal-overlay" id="course-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="course-modal-title">Lehrgangs-Vorlage</h3>
		<form id="course-modal-form" action="${pageContext.request.contextPath}/admin/courses" method="post">
			<input type="hidden" name="action" id="course-modal-action">
			<input type="hidden" name="id" id="course-modal-id">
			<div class="form-group"><label for="name-modal">Name der Vorlage</label><input type="text" id="name-modal" name="name" required></div>
			<div class="form-group"><label for="abbreviation-modal">Abkürzung (max. 10 Zeichen)</label><input type="text" id="abbreviation-modal" name="abbreviation" maxlength="10" required></div>
			<div class="form-group"><label for="description-modal">Allgemeine Beschreibung</label><textarea id="description-modal" name="description" rows="4"></textarea></div>
			<button type="submit" class="btn">Vorlage Speichern</button>
		</form>
	</div>
</div>

<c:import url="../../jspf/table_scripts.jspf" />
<c:import url="../../jspf/main_footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    const contextPath = "${'${pageContext.request.contextPath}'}";
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    const modalOverlay = document.getElementById('course-modal');
    const form = document.getElementById('course-modal-form');
    const title = document.getElementById('course-modal-title');
    const actionInput = document.getElementById('course-modal-action');
    const idInput = document.getElementById('course-modal-id');
    const nameInput = document.getElementById('name-modal');
    const abbrInput = document.getElementById('abbreviation-modal');
    const descInput = document.getElementById('description-modal');
    const closeModalBtn = modalOverlay.querySelector('.modal-close-btn');
    
    const closeModal = () => modalOverlay.classList.remove('active');
    
    const openCreateModal = () => {
        form.reset();
        title.textContent = "Neue Lehrgangs-Vorlage anlegen";
        actionInput.value = "create";
        idInput.value = "";
        modalOverlay.classList.add('active');
    };
    
    const openEditModal = async (btn) => {
        form.reset();
        title.textContent = "Lehrgangs-Vorlage bearbeiten";
        actionInput.value = "update";
        const courseId = btn.dataset.id;
        idInput.value = courseId;
        
        try {
            const response = await fetch(`${contextPath}/WEB-INF/views/admin/admin_course_list.jsp?action=getCourseData&id=${courseId}`);
            if(!response.ok) throw new Error('Could not fetch course data');
            const data = await response.json();

            nameInput.value = data.name || '';
            abbrInput.value = data.abbreviation || '';
            descInput.value = data.description || '';

            modalOverlay.classList.add('active');
        } catch(error) {
            console.error("Failed to open edit modal:", error);
            alert("Fehler beim Laden der Vorlagen-Daten.");
        }
    };
    
    document.getElementById('new-course-btn').addEventListener('click', openCreateModal);
    document.querySelectorAll('.edit-course-btn').forEach(btn => {
        btn.addEventListener('click', () => openEditModal(btn));
    });
    
    closeModalBtn.addEventListener('click', closeModal);
    modalOverlay.addEventListener('click', (e) => {
        if (e.target === modalOverlay) closeModal();
    });
    document.addEventListener('keydown', (e) => {
        if (e.key === 'Escape' && modalOverlay.classList.contains('active')) closeModal();
    });
});
</script>