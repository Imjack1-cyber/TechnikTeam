<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="To-Do Liste" />
</c:import>

<div class="todo-app-container">
	<div
		style="display: flex; justify-content: space-between; align-items: center; flex-wrap: wrap; gap: 1rem;">
		<h1>
			<i class="fas fa-check-square"></i> To-Do Liste
		</h1>
		<form id="new-category-form" class="form-group"
			style="display: flex; gap: 0.5rem; margin-bottom: 0;">
			<input type="text" id="new-category-name"
				placeholder="Neue Kategorie..." required>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-plus"></i>
			</button>
		</form>
	</div>
	<p>Organisieren Sie hier Aufgaben und Projekte. Sie können
		Kategorien und Aufgaben per Drag & Drop umsortieren.</p>

	<div id="todo-categories-container">
		<!-- Categories will be rendered here by JavaScript -->
	</div>
</div>

<!-- Template for a Category -->
<template id="category-template">
	<div class="card todo-category" data-category-id="">
		<div class="category-header">
			<h2 class="category-title" title="Doppelklick zum Bearbeiten"></h2>
			<button class="btn btn-small btn-danger delete-category-btn"
				title="Kategorie löschen">
				<i class="fas fa-trash"></i>
			</button>
		</div>
		<ul class="task-list">
			<!-- Tasks will be rendered here -->
		</ul>
		<form class="new-task-form"
			style="display: flex; gap: 0.5rem; margin-top: 1rem;">
			<input type="text" class="new-task-content"
				placeholder="Neue Aufgabe..." required>
			<button type="submit" class="btn btn-small">
				<i class="fas fa-plus"></i>
			</button>
		</form>
	</div>
</template>

<!-- Template for a Task -->
<template id="task-template">
	<li class="task-item" data-task-id=""><input type="checkbox"
		class="task-checkbox"> <span class="task-content"
		title="Doppelklick zum Bearbeiten"></span>
		<button class="btn btn-small btn-danger delete-task-btn"
			title="Aufgabe löschen">
			<i class="fas fa-times"></i>
		</button></li>
</template>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/vendor/sortablejs/Sortable.min.js"></script>
<script
	src="${pageContext.request.contextPath}/js/admin/admin_feedback.js"></script>