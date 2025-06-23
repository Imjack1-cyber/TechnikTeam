<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
  dateien.jsp
  
  This is the main user-facing page for accessing files and documents.
  It displays a list of all files the user is authorized to see, grouped
  by their category. It also includes a special, "virtual" link to the
  collaborative editor page.
  
  - It is served by: FileServlet.
  - Expected attributes:
    - 'fileData' (Map<String, List<de.technikteam.model.File>>): Files grouped by category.
--%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Dateien" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Dateien & Dokumente</h1>

<c:if test="${empty fileData}">
	<div class="card">
		<p>Es sind keine Dateien oder Dokumente verfügbar.</p>
	</div>
</c:if>

<c:forEach var="categoryEntry" items="${fileData}">
	<div class="card">
		<h2>
			<c:out value="${categoryEntry.key}" />
		</h2>
		<ul style="list-style: none; padding-left: 0;">
			<c:forEach var="file" items="${categoryEntry.value}" varStatus="loop">
				<li
					style="padding: 0.75rem 0; ${!loop.last ? 'border-bottom: 1px solid var(--border-color);' : ''}">
					<c:choose>
						<%-- The virtual file with ID -1 links to the editor page --%>
						<c:when test="${file.id == -1}">
							<a href="${pageContext.request.contextPath}/editor-page"
								style="font-weight: 600;"><c:out value="${file.filename}" /></a>
						</c:when>
						<%-- All other files link to the download servlet --%>
						<c:otherwise>
							<a
								href="${pageContext.request.contextPath}/download?file=${file.filepath}"><c:out
									value="${file.filename}" /></a>
						</c:otherwise>
					</c:choose>
				</li>
			</c:forEach>
		</ul>
	</div>
</c:forEach>

<c:import url="/WEB-INF/jspf/footer.jspf" />