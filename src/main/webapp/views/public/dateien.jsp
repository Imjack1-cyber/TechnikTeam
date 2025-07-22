<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Dateien & Dokumente" />
</c:import>

<h1>Dateien & Dokumente</h1>
<p>Hier können Sie zentrale Dokumente und Vorlagen herunterladen.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<c:if test="${empty fileData}">
	<div class="card">
		<p>Es sind keine Dateien oder Dokumente verfügbar.</p>
	</div>
</c:if>

<c:set var="canUpdateFiles"
	value="${sessionScope.user.permissions.contains('FILE_UPDATE') or sessionScope.user.permissions.contains('ACCESS_ADMIN_PANEL')}" />

<c:forEach var="categoryEntry" items="${fileData}">
	<div class="card">
		<h2>
			<i class="fas fa-folder"></i>
			<c:out value="${categoryEntry.key}" />
		</h2>
		<ul class="file-list">
			<c:forEach var="file" items="${categoryEntry.value}">
				<li style="padding: 0.75rem 0;">
					<div>
						<a href="<c:url value='/download?id=${file.id}'/>"><i
							class="fas fa-download"></i> <c:out value="${file.filename}" /></a>
					</div> <c:if
						test="${canUpdateFiles and fn:endsWith(fn:toLowerCase(file.filename), '.md')}">
						<div class="file-actions">
							<a href="<c:url value='/editor?fileId=${file.id}'/>"
								class="btn btn-small"> <i class="fas fa-edit"></i>
								Bearbeiten
							</a>
						</div>
					</c:if>
				</li>
			</c:forEach>
		</ul>
	</div>
</c:forEach>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />