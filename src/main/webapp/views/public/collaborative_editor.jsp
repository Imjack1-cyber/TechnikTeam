<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Gemeinsamer Editor" />
	<c:param name="navType" value="user" />
</c:import>

<h1>
	<i class="fas fa-edit"></i> Gemeinsamer Notizblock
</h1>
<p>Änderungen werden automatisch für alle Benutzer gespeichert und
	angezeigt.</p>

<div class="card">
	<textarea id="editor" class="form-group"
		style="width: 100%; height: 60vh; font-family: monospace; font-size: 16px; margin: 0; background-color: var(--surface-color);"
		placeholder="Lade Inhalt..."></textarea>
	<div id="status-indicator"
		style="text-align: right; font-style: italic; color: var(--text-muted-color); padding-top: 5px; min-height: 1.2em;"></div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="<c:url value='/js/public/collaborative_editor.js'/>"></script>