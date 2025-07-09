<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Dateien" />
</c:import>

<h1>Dateien & Dokumente</h1>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

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
						<c:when test="${file.id == -1}">
							<a href="${pageContext.request.contextPath}/editor-page"
								style="font-weight: 600;"><c:out value="${file.filename}" /></a>
						</c:when>
						<c:otherwise>
							<a href="<c:url value='/download?type=file&id=${file.id}'/>"><c:out
									value="${file.filename}" /></a>
						</c:otherwise>
					</c:choose>
				</li>
			</c:forEach>
		</ul>
	</div>
</c:forEach>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />