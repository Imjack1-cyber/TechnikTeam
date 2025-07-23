<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Mein Feedback" />
</c:import>

<h1>
	<i class="fas-fa-inbox"></i> Mein eingereichtes Feedback
</h1>
<p>Hier sehen Sie den Status Ihrer Vorschläge und Meldungen.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="feedback-list">
	<c:if test="${empty mySubmissions}">
		<div class="card">
			<p>
				Sie haben noch kein Feedback eingereicht. <a
					href="${pageContext.request.contextPath}/feedback">Jetzt eine
					Idee teilen!</a>
			</p>
		</div>
	</c:if>

	<c:forEach var="sub" items="${mySubmissions}">
		<c:set var="statusClass" value="status-info" />
		<c:if test="${sub.status == 'PLANNED'}">
			<c:set var="statusClass" value="status-warn" />
		</c:if>
		<c:if test="${sub.status == 'COMPLETED'}">
			<c:set var="statusClass" value="status-ok" />
		</c:if>
		<c:if test="${sub.status == 'REJECTED'}">
			<c:set var="statusClass" value="status-danger" />
		</c:if>

		<div class="card">
			<div
				style="display: flex; justify-content: space-between; align-items: start;">
				<div>
					<h3 class="card-title" style="border: none; padding: 0;">
						<c:out value="${sub.subject}" />
					</h3>
					<p class="details-subtitle" style="margin-top: -0.75rem;">
						Eingereicht am
						<c:out value="${sub.formattedSubmittedAt}" />
						Uhr
					</p>
				</div>
				<div>
					<span class="status-badge ${statusClass}"><c:out
							value="${sub.status}" /></span>
				</div>
			</div>

			<div class="markdown-content"
				style="white-space: pre-wrap; background-color: var(--bg-color); padding: 1rem; border-radius: var(--border-radius);">${fn:escapeXml(sub.content)}
			</div>
		</div>
	</c:forEach>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />