<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Datei- & Kategorienverwaltung" />
</c:import>

<h1>
	<i class="fas fa-folder-open"></i> Datei- & Kategorienverwaltung
</h1>
<p>Verwalten Sie hier alle hochgeladenen Dateien und deren
	Kategorien.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="file-manager-layout">
	<div class="card file-manager-categories">
		<h2>Kategorien</h2>
		<ul class="category-list">
			<c:forEach var="cat" items="${allCategories}">
				<li data-category-id="${cat.id}"><i class="fas fa-folder"></i>
					<c:out value="${cat.name}" /></li>
			</c:forEach>
		</ul>
		<hr>
		<h4 style="margin-top: 1rem;">Kategorie-Aktionen</h4>
		<form action="<c:url value='/admin/categories/create'/>" method="post">
			<div class="form-group">
				<label for="newCategoryName">Neue Kategorie erstellen</label> <input
					type="text" name="categoryName" id="newCategoryName" required>
			</div>
			<button type="submit" class="btn">
				<i class="fas fa-plus"></i> Erstellen
			</button>
		</form>
	</div>
	<div class="file-manager-content">
		<div id="section-placeholder" class="card"
			style="text-align: center; padding: 3rem; border-style: dashed;">
			<i class="fas fa-arrow-left fa-2x"
				style="color: var(--text-muted-color); margin-bottom: 1rem;"></i>
			<p style="font-size: 1.2rem; color: var(--text-muted-color);">Bitte
				wählen Sie links eine Kategorie aus.</p>
		</div>
		<div id="dynamic-content-area"></div>
	</div>
</div>

<template id="category-section-template">
	<div class="file-category-section">
		<div class="card">
			<h2>
				<i class="fas fa-file-alt"></i> Dateien in "<span
					class="category-name"></span>"
			</h2>
			<ul class="file-list"></ul>
		</div>
		<div class="card">
			<h2>
				<i class="fas fa-upload"></i> Datei zu "<span class="category-name"></span>"
				hochladen
			</h2>
			<form action="<c:url value='/admin/files'/>" method="post"
				enctype="multipart/form-data">
				<input type="hidden" name="categoryId" class="category-id-input">
				<div class="form-group">
					<label>Datei auswählen</label><input type="file" name="file"
						class="file-input" data-max-size="20971520" required><small
						class="file-size-warning" style="color: red; display: none;">Datei
						ist zu groß! (Max. 20 MB)</small>
				</div>
				<div class="form-group">
					<label>Sichtbar für</label><select name="requiredRole"><option
							value="NUTZER" selected>Alle Nutzer</option>
						<option value="ADMIN">Nur Admins</option></select>
				</div>
				<button type="submit" class="btn">
					<i class="fas fa-cloud-upload-alt"></i> Hochladen
				</button>
			</form>
		</div>
		<div class="card">
			<h2>
				<i class="fas fa-edit"></i> Kategorie "<span class="category-name"></span>"
				verwalten
			</h2>
			<div class="category-actions-grid">
				<form action="<c:url value='/admin/categories/update'/>"
					method="post" class="category-action-form">
					<input type="hidden" name="categoryId" class="category-id-input">
					<div class="form-group">
						<label>Umbenennen</label><input type="text" name="categoryName"
							class="category-name-input" required>
					</div>
					<button type="submit" class="btn btn-small">
						<i class="fas fa-save"></i> Umbenennen
					</button>
				</form>
				<form action="<c:url value='/admin/categories/delete'/>"
					method="post" class="category-action-form js-confirm-form"
					data-confirm-message="Kategorie wirklich löschen? Zugehörige Dateien verlieren ihre Kategoriezuordnung.">
					<input type="hidden" name="categoryId" class="category-id-input">
					<div class="form-group">
						<label>Löschen</label>
						<p>
							<small>Zugehörige Dateien verlieren ihre
								Kategoriezuordnung.</small>
						</p>
					</div>
					<button type="submit" class="btn btn-small btn-danger">
						<i class="fas fa-trash-alt"></i> Endgültig Löschen
					</button>
				</form>
			</div>
		</div>
	</div>
</template>

<template id="file-item-template">
	<li>
		<div class="file-info">
			<a href="#" class="file-download-link" title="Datei herunterladen"></a>
			<small class="file-meta"></small>
		</div>
		<form action="<c:url value='/admin/files'/>" method="post"
			class="js-confirm-form"
			data-confirm-message="Datei wirklich löschen?">
			<input type="hidden" name="action" value="delete"><input
				type="hidden" name="fileId" class="file-id-input">
			<button type="submit" class="btn btn-small btn-danger-outline"
				title="Löschen">
				<i class="fas fa-trash-alt"></i>
			</button>
		</form>
	</li>
</template>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script type="text/javascript" src="/js/admin/admin_files.js"></script>