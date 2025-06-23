<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
admin_course_list.jsp

This JSP displays a list of all parent course templates for administrators.
It provides actions to manage meetings, edit the template, or delete it.
Creating and editing courses are now handled via modal dialogs on this page.

    It is served by: AdminCourseServlet (doGet).

    Expected attributes:

        'courseList' (List<de.technikteam.model.Course>): A list of all course templates.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lehrgangs-Vorlagen" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Lehrgangs-Vorlagen verwalten</h1>
<p>Dies sind die übergeordneten Lehrgänge. Einzelne Termine
	(Meetings) werden für jede Vorlage separat verwaltet.</p>

<c:if test="${not empty sessionScope.successMessage}">
	<p class="success-message">
		<c:out value="${sessionScope.successMessage}" />
	</p>
	<c:remove var="successMessage" scope="session" />
</c:if>
<c:if test="${not empty sessionScope.errorMessage}">
	<p class="error-message">
		<c:out value="${sessionScope.errorMessage}" />
	</p>
	<c:remove var="errorMessage" scope="session" />
</c:if>
<div class="table-controls">
	<button type="button" id="new-course-btn" class="btn">Neue
		Lehrgangs-Vorlage anlegen</button>
	<div class="form-group" style="margin-bottom: 0;">
		<input type="search" id="table-filter"
			placeholder="Tabelle filtern..." aria-label="Tabelle filtern">
	</div>
</div>

<c:if test="${empty courseList}">
	<div class="card">
		<p>Es wurden noch keine Lehrgangs-Vorlagen erstellt.</p>
	</div>
</c:if>
<!-- MOBILE LAYOUT: A list of cards, one for each course template -->
<div class="mobile-card-list searchable-list">
	<c:forEach var="course" items="${courseList}">
		<div class="list-item-card"
			data-searchable-content="<c:out value='${course.name}'/> <c:out value='${course.abbreviation}'/>">
			<h3 class="card-title">
				<c:out value="${course.name}" />
			</h3>
			<div class="card-row">
				<span>Abkürzung:</span> <span><c:out
						value="${course.abbreviation}" /></span>
			</div>
			<div class="card-actions">
				<a
					href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
					class="btn btn-small btn-success">Meetings verwalten</a>
				<button type="button" class="btn btn-small edit-course-btn"
					data-id="${course.id}" data-name="${fn:replace(course.name, '"
					', '&quot;')}"
					data-abbreviation="${fn:replace(course.abbreviation, '"
					', '&quot;')}"
					data-description="${fn:replace(course.description, '"', '&quot;')}">Vorlage
					bearbeiten</button>
				<form action="${pageContext.request.contextPath}/admin/courses"
					method="post" class="js-confirm-form"
					data-confirm-message="Vorlage '${fn:escapeXml(course.name)}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!">
					<input type="hidden" name="action" value="delete"> <input
						type="hidden" name="id" value="${course.id}">
					<button type="submit" class="btn btn-small btn-danger">Löschen</button>
				</form>
			</div>
		</div>
	</c:forEach>
</div>
<!-- DESKTOP LAYOUT: A bordered table -->
<div class="desktop-table-wrapper">
	<table class="desktop-table sortable-table searchable-table">
		<thead>
			<tr>
				<th class="sortable" data-sort-type="string">Name der Vorlage</th>
				<th class="sortable" data-sort-type="string">Abkürzung (für
					Matrix)</th>
				<th style="width: 450px;">Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="course" items="${courseList}">
				<tr>
					<td><c:out value="${course.name}" /></td>
					<td><c:out value="${course.abbreviation}" /></td>
					<td style="display: flex; gap: 0.5rem; flex-wrap: wrap;"><a
						href="${pageContext.request.contextPath}/admin/meetings?courseId=${course.id}"
						class="btn btn-small btn-success">Meetings verwalten</a>
						<button type="button" class="btn btn-small edit-course-btn"
							data-id="${course.id}" data-name="${fn:replace(course.name, '"
							', '&quot;')}"
                            data-abbreviation="${fn:replace(course.abbreviation, '"
							', '&quot;')}"
                            data-description="${fn:replace(course.description, '"', '&quot;')}">Vorlage
							bearbeiten</button>
						<form action="${pageContext.request.contextPath}/admin/courses"
							method="post" class="js-confirm-form"
							data-confirm-message="Vorlage '${fn:escapeXml(course.name)}' wirklich löschen? Alle zugehörigen Meetings und Qualifikationen werden auch gelöscht!">
							<input type="hidden" name="action" value="delete"> <input
								type="hidden" name="id" value="${course.id}">
							<button type="submit" class="btn btn-small btn-danger">Löschen</button>
						</form></td>
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
		<form id="course-modal-form"
			action="${pageContext.request.contextPath}/admin/courses"
			method="post">
			<input type="hidden" name="action" id="course-modal-action">
			<input type="hidden" name="id" id="course-modal-id">
			<div class="form-group">
				<label for="name-modal">Name der Vorlage (z.B. Grundlehrgang
					Tontechnik)</label> <input type="text" id="name-modal" name="name" required>
			</div>
			<div class="form-group">
				<label for="abbreviation-modal">Abkürzung (für Matrix, max.
					10 Zeichen, z.B. GL-Ton)</label> <input type="text" id="abbreviation-modal"
					name="abbreviation" maxlength="10" required>
			</div>
			<div class="form-group">
				<label for="description-modal">Allgemeine Beschreibung des
					Lehrgangs</label>
				<textarea id="description-modal" name="description" rows="4"></textarea>
			</div>
			<button type="submit" class="btn">Vorlage Speichern</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/table-helper.jspf" />
<c:import url="/WEB-INF/jspf/footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    // Custom confirmation for delete forms
    document.querySelectorAll('.js-confirm-form').forEach(form => {
        form.addEventListener('submit', function(e) {
            e.preventDefault();
            const message = this.dataset.confirmMessage || 'Sind Sie sicher?';
            showConfirmationModal(message, () => this.submit());
        });
    });

    // Modal Logic
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
    
    const openEditModal = (btn) => {
        form.reset();
        const data = btn.dataset;
        title.textContent = "Lehrgangs-Vorlage bearbeiten";
        actionInput.value = "update";
        idInput.value = data.id;
        nameInput.value = data.name;
        abbrInput.value = data.abbreviation;
        descInput.value = data.description;
        modalOverlay.classList.add('active');
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