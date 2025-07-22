<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Editor: ${file.filename}" />
	<c:param name="page" value="editor" />
</c:import>

<div id="editor-page-wrapper">
	<div id="editor-page-container" data-file-id="${file.id}">
		<div class="editor-header">
			<h1>
				<i class="fas fa-file-alt"></i>
				<c:out value="${file.filename}" />
			</h1>
			<div class="editor-controls">
				<c:if test="${editorMode eq 'edit'}">
					<div class="mode-switcher">
						<span>View</span> <label class="toggle-switch"> <input
							type="checkbox" id="mode-toggle" checked> <span
							class="slider"></span>
						</label> <span>Edit</span>
					</div>
				</c:if>
				<span id="save-status-indicator" class="status-badge"
					style="display: none;"></span>
			</div>
		</div>

		<c:import url="/WEB-INF/jspf/message_banner.jspf" />

		<div class="editor-container card">
			<textarea id="editor" name="content"
				style="display: ${editorMode eq 'edit' ? 'block' : 'none'};"
				<c:if test="${editorMode ne 'edit'}">readonly</c:if>>${fn:escapeXml(fileContent)}</textarea>
			<div id="markdown-preview" class="markdown-content"
				style="display: ${editorMode eq 'edit' ? 'none' : 'block'};">
			</div>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_editor.js"></script>