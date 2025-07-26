<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Technical Wiki" />
    <c:param name="page" value="wiki" /> <%-- This is crucial for the CSS override --%>
</c:import>

<%-- This new wrapper takes over the entire main content area --%>
<div class="wiki-page-wrapper">

    <button id="wiki-sidebar-toggle" class="btn btn-secondary mobile-only no-print">
        <i class="fas fa-bars"></i> Navigation
    </button>

	<aside class="wiki-sidebar">
		<div class="wiki-sidebar-header">
			<h3><i class="fas fa-book-reader"></i> Wiki Navigation</h3>
			<button id="add-wiki-page-btn" class="btn btn-small btn-success no-print" title="Neue Seite erstellen">
				<i class="fas fa-plus"></i>
			</button>
		</div>
		<div class="form-group">
			<input type="search" id="wiki-search" class="form-group"
				placeholder="Dateien filtern...">
		</div>
		<div id="wiki-tree-container" class="wiki-tree-container">
			<p>Lade Navigation...</p>
		</div>
	</aside>

	<main id="wiki-content-pane">
		<div class="wiki-welcome-pane">
			<i class="fas fa-book-open fa-3x"></i>
			<h1>Willkommen im Wiki</h1>
			<p>Wählen Sie eine Datei aus der Navigation auf der linken Seite aus,
				um deren Dokumentation anzuzeigen.</p>
		</div>
	</main>

</div>

<!-- Modal for adding a new wiki page -->
<div class="modal-overlay" id="add-wiki-page-modal">
	<div class="modal-content" style="max-width: 700px;">
		<button class="modal-close-btn" type="button" aria-label="Schließen">×</button>
		<h3>Neue Wiki-Seite erstellen</h3>
		<form id="add-wiki-page-form">
			<div class="form-group">
				<label for="new-wiki-filepath">Dateipfad (eindeutiger
					Schlüssel)</label> <input type="text" id="new-wiki-filepath"
					name="filePath" required
					placeholder="z.B. src/main/java/de/technikteam/neueKlasse.java">
			</div>
			<div class="form-group">
				<label for="new-wiki-content">Initialer Inhalt (Markdown)</label>
				<textarea id="new-wiki-content" name="content" rows="10"></textarea>
			</div>
			<button type="submit" class="btn btn-success">
				<i class="fas fa-save"></i> Seite erstellen
			</button>
		</form>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_wiki.js"></script>