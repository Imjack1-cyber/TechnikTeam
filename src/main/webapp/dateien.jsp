<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" isELIgnored="false" %>
<%@ taglib uri="jakarta.tags.core" prefix="c" %>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Dateien" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Dateien & Dokumente</h1>

<c:forEach var="categoryEntry" items="${fileData}">
    <div class="card">
        <h2>${categoryEntry.key}</h2>
        <ul style="list-style: none; padding-left: 0;">
            <c:forEach var="file" items="${categoryEntry.value}">
                <li style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color);">
                    <c:choose>
                        <c:when test="${file.id == -1}">
                            <a href="${pageContext.request.contextPath}/editor-page" style="font-weight: 600;">${file.filename}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${pageContext.request.contextPath}/download?file=${file.filepath}">${file.filename}</a>
                        </c:otherwise>
                    </c:choose>
                </li>
            </c:forEach>
        </ul>
    </div>
</c:forEach>

<c:import url="/WEB-INF/jspf/footer.jspf" />