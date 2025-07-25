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

	<div class="card">
		<h2>Aktionen</h2>
		<form
			action="${pageContext.request.contextPath}/admin/dateien/createCategory"
			method="post" style="margin-bottom: 2rem;">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}">
			<div class="form-group">
				<label for="newCategoryName">Neue Kategorie erstellen</label> <input
					type="text" name="categoryName" id="newCategoryName" required>
			</div>
			<button type="submit" class="btn">
				<i class="fas fa-plus"></i> Erstellen
			</button>
		</form>

		<hr>

		<h3 style="margin-top: 1.5rem;">Neue Datei hochladen</h3>
		<form action="${pageContext.request.contextPath}/admin/dateien/create"
			method="post" enctype="multipart/form-data">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}">
			<div class="form-group">
				<label for="file">Datei auswählen</label> <input type="file"
					name="file" id="file" class="file-input" data-max-size="20971520"
					required> <small class="file-size-warning">Datei
					ist zu groß! (Max. 20 MB)</small>
			</div>
			<div class="form-group">
				<label for="categoryId">In Kategorie</label> <select
					name="categoryId" id="categoryId" required>
					<option value="">-- Bitte wählen --</option>
					<c:forEach var="cat" items="${allCategories}">
						<option value="${cat.id}"><c:out value="${cat.name}" /></option>
					</c:forEach>
				</select>
			</div>
			<div class="form-group">
				<label for="requiredRole">Sichtbar für</label> <select
					name="requiredRole" id="requiredRole">
					<option value="NUTZER" selected>Alle zugeordneten Nutzer</option>
					<option value="ADMIN">Nur Admins</option>
				</select>
			</div>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-cloud-upload-alt"></i> Hochladen
			</button>
		</form>
	</div>

	<div class="card">
		<h2>Vorhandene Dateien nach Kategorie</h2>
		<c:if test="${empty groupedFiles}">
			<p>Es sind keine Kategorien oder Dateien vorhanden.</p>
		</c:if>

		<c:forEach var="categoryEntry" items="${groupedFiles}">
			<div class="category-group" style="margin-bottom: 2rem;">
				<div
					style="display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--border-color); padding-bottom: 0.5rem;">
					<h3>
						<i class="fas fa-folder"></i>
						<c:out value="${categoryEntry.key}" />
					</h3>
					<c:if test="${categoryEntry.key != 'Ohne Kategorie'}">
						<form
							action="${pageContext.request.contextPath}/admin/dateien/deleteCategory"
							method="post" class="js-confirm-form"
							data-confirm-message="Kategorie '${fn:escapeXml(categoryEntry.key)}' wirklich löschen? Alle Dateien in dieser Kategorie werden der Gruppe 'Ohne Kategorie' zugeordnet.">
							<input type="hidden" name="csrfToken"
								value="${sessionScope.csrfToken}"> <input type="hidden"
								name="categoryId" value="${categoryEntry.value[0].categoryId}">
							<button type="submit" class="btn btn-small btn-danger-outline"
								title="Kategorie löschen">
								<i class="fas fa-trash"></i>
							</button>
						</form>
					</c:if>
				</div>
				<ul class="file-list">
					<c:if test="${empty categoryEntry.value}">
						<li style="justify-content: center;">Keine Dateien in dieser
							Kategorie.</li>
					</c:if>
					<c:forEach var="file" items="${categoryEntry.value}">
						<li>
							<div class="file-info">
								<a href="<c:url value='/download?id=${file.id}'/>"
									title="Datei herunterladen"> <i class="fas fa-download"></i>
									<c:out value="${file.filename}" />
								</a> <small class="file-meta">Sichtbar für: <c:out
										value="${file.requiredRole}" /></small>
							</div>
							<div class="file-actions"
								style="display: flex; gap: 0.5rem; align-items: center;">
								<button type="button"
									class="btn btn-small btn-info reassign-file-btn"
									data-file-id="${file.id}"
									data-file-name="${fn:escapeXml(file.filename)}">
									<i class="fas fa-random"></i>
								</button>
								<button type="button"
									class="btn btn-small btn-secondary upload-new-version-btn"
									data-file-id="${file.id}"
									data-file-name="${fn:escapeXml(file.filename)}">
									<i class="fas fa-upload"></i>
								</button>
								<form
									action="${pageContext.request.contextPath}/admin/dateien/delete"
									method="post" class="js-confirm-form">
									<input type="hidden" name="csrfToken"
										value="${sessionScope.csrfToken}"> <input
										type="hidden" name="fileId" value="${file.id}">
									<button type="submit" class="btn btn-small btn-danger-outline"
										title="Löschen">
										<i class="fas fa-trash-alt"></i>
									</button>
								</form>
							</div>
						</li>
					</c:forEach>
				</ul>
			</div>
		</c:forEach>
	</div>
</div>

<!-- Modal for reassigning a file -->
<div class="modal-overlay" id="reassign-file-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
		<h3 id="reassign-modal-title">Datei neu zuordnen</h3>
		<form
			action="${pageContext.request.contextPath}/admin/dateien/reassign"
			method="post">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="fileId" id="reassign-file-id">
			<p>
				Wählen Sie die neue Kategorie für die Datei <strong
					id="reassign-file-name"></strong>.
			</p>
			<div class="form-group">
				<label for="newCategoryId">Neue Kategorie</label> <select
					name="newCategoryId" id="newCategoryId" required>
					<c:forEach var="cat" items="${allCategories}">
						<option value="${cat.id}">${cat.name}</option>
					</c:forEach>
				</select>
			</div>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-save"></i> Neu zuordnen
			</button>
		</form>
	</div>
</div>

<!-- Modal for uploading a new version -->
<div class="modal-overlay" id="upload-version-modal">
	<div class="modal-content">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
		<h3 id="upload-modal-title">Neue Version hochladen</h3>
		<form id="upload-version-form"
			action="${pageContext.request.contextPath}/admin/dateien/update"
			method="post" enctype="multipart/form-data">
			<input type="hidden" name="csrfToken"
				value="${sessionScope.csrfToken}"> <input type="hidden"
				name="fileId" id="upload-file-id">
			<p>
				Sie sind dabei, eine neue Version für die Datei <strong
					id="upload-file-name"></strong> hochzuladen. Die alte Version wird
				dabei überschrieben.
			</p>
			<div class="form-group">
				<label for="new-file-version">Neue Datei auswählen (muss
					denselben Dateityp haben)</label> <input type="file" name="file"
					id="new-file-version" class="file-input" data-max-size="20971520"
					required> <small class="file-size-warning">Datei
					ist zu groß! (Max. 20 MB)</small>
			</div>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-cloud-upload-alt"></i> Jetzt hochladen
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/admin/admin_files.js"></script>