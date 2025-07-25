<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<%--
    This is the template for a wiki documentation file.
    A copy of this file exists for every source file in the project.
    You can now directly edit the content for this specific file below.
--%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Wiki: ${filePath}" />
</c:import>

<c:set var="wikiContent">
	<%-- =================================================================== --%>
	<%--                START OF EDITABLE MARKDOWN CONTENT                   --%>
	<%-- =================================================================== --%>

# Documentation for `${fn:escapeXml(filePath)}`

This documentation has not been written yet. You can add the content here by switching to "Edit (Raw)" mode.

**Instructions:**
1. Click the "Edit (Raw)" toggle switch above.
2. Copy and paste or write your Markdown documentation into the text area.
3. Switch back to "View" mode to see a preview.

*Note: This editor is for drafting and viewing only. The content is not saved to a database.*

<%-- =================================================================== --%>
	<%--                 END OF EDITABLE MARKDOWN CONTENT                    --%>
	<%-- =================================================================== --%>
</c:set>

<div id="editor-page-wrapper">
	<div id="editor-page-container">
		<div class="editor-header">
			<h1>
				<i class="fas fa-file-alt"></i> Wiki:
				<code>
					<c:out value="${filePath}" />
				</code>
			</h1>
			<div class="editor-controls">
				<div class="mode-switcher">
					<span>View</span> <label class="toggle-switch"> <input
						type="checkbox" id="mode-toggle"> <span class="slider"></span>
					</label> <span>Edit (Raw)</span>
				</div>
				<a href="${pageContext.request.contextPath}/admin/wiki"
					class="btn btn-secondary btn-small"> <i
					class="fas fa-arrow-left"></i> Back to Index
				</a>
			</div>
		</div>

		<div class="editor-container card">
			<textarea id="editor" name="content" style="display: none;">${fn:escapeXml(wikiContent)}</textarea>
			<div id="markdown-preview" class="markdown-content">
				<%-- The initial content is now rendered by JavaScript from the textarea --%>
				${wikiContent}
			</div>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_wiki_details.js"></script>