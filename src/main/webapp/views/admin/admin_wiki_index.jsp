<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Technical Wiki Index" />
</c:import>

<style>
.wiki-tree ul {
	list-style-type: none;
	padding-left: 20px;
}

.wiki-tree li {
	padding: 2px 0;
}
</style>

<h1>
	<i class="fas fa-book-reader"></i> Technical Wiki - Index
</h1>
<p>Select a file from the project tree below to view its detailed
	technical documentation.</p>

<div class="card">
	<div class="wiki-tree">${projectTreeHtml}</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />