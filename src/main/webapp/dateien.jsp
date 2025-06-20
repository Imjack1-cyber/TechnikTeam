<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Dateien" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Dateien &amp; Dokumente</h1>

<c:forEach var="categoryEntry" items="${fileData}">
	<div class="card file-category">
		<h2 class="card-title">${categoryEntry.key}</h2>
		<ul>
			<c:forEach var="file" items="${categoryEntry.value}">
				<li>
					<%-- In dateien.jsp --%> <a
					href="${pageContext.request.contextPath}/download?file=${file.filepath}">
						${file.filename} </a>
				</li>
			</c:forEach>
		</ul>
	</div>
</c:forEach>

<style>
.file-category ul {
	list-style: none;
	padding-left: 0;
}

.file-category li {
	padding: 0.5rem 0;
	border-bottom: 1px solid var(--border-color);
}

.file-category li:last-child {
	border-bottom: none;
}

.file-category a {
	text-decoration: none;
	color: var(--primary-color);
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />