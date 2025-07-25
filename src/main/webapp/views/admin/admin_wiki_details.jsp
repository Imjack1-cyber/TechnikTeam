<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Wiki: ${wikiEntry.filePath}" />
</c:import>

<div id="editor-page-wrapper">
	<div id="editor-page-container" data-wiki-id="${wikiEntry.id}">
		<div class="editor-header">
			<h1>
				<i class="fas fa-file-alt"></i> Wiki:
				<code>
					<c:out value="${wikiEntry.filePath}" />
				</code>
			</h1>
			<div class="editor-controls">
				<div class="mode-switcher">
					<span>View</span> <label class="toggle-switch"> <input
						type="checkbox" id="mode-toggle"> <span class="slider"></span>
					</label> <span>Edit</span>
				</div>
				<button id="save-wiki-btn" class="btn btn-success btn-small"
					style="display: none;">
					<i class="fas fa-save"></i> Speichern
				</button>
				<span id="save-status-indicator" class="status-badge"
					style="display: none;"></span> <a
					href="${pageContext.request.contextPath}/admin/wiki"
					class="btn btn-secondary btn-small"> <i
					class="fas fa-arrow-left"></i> Zurück zum Index
				</a>
			</div>
		</div>

		<c:import url="/WEB-INF/jspf/message_banner.jspf" />

		<div class="editor-container card">
			<textarea id="editor" name="content" style="display: none;">${fn:escapeXml(wikiEntry.content)}</textarea>
			<div id="markdown-preview" class="markdown-content">
				<%-- The initial content is now rendered by JavaScript from the textarea --%>
			</div>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_wiki_details.js"></script>