<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

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

<c:forEach var="categoryEntry" items="${fileData}">
	<div class="card">
		<h2>
			<i class="fas fa-folder"></i>
			<c:out value="${categoryEntry.key}" />
		</h2>
		<ul class="file-list">
			<c:forEach var="file" items="${categoryEntry.value}" varStatus="loop">
				<li style="padding: 0.75rem 0;"><a
					href="<c:url value='/download?id=${file.id}'/>"><i
						class="fas fa-download"></i> <c:out value="${file.filename}" /></a></li>
			</c:forEach>
		</ul>
	</div>
</c:forEach>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />