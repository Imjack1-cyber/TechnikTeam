<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Wiki: ${filePath}" />
</c:import>

<h1>
	<i class="fas fa-file-alt"></i> Wiki:
	<code>
		<c:out value="${filePath}" />
	</code>
</h1>
<a href="${pageContext.request.contextPath}/admin/wiki"
	class="btn btn-secondary" style="margin-bottom: 1.5rem;"> <i
	class="fas fa-arrow-left"></i> Back to Wiki Index
</a>

<div class="card">
	<div id="wiki-content-container" class="markdown-content">
		${wikiContent}</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script>
	document.addEventListener('DOMContentLoaded', () => {
		const wikiContainer = document.getElementById('wiki-content-container');
		if (wikiContainer && window.renderMarkdown) {
			window.renderMarkdown(wikiContainer);
		}
	});
</script>