<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<%-- Header must be included first to prevent Quirks Mode and CSS path errors --%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title"
		value="${empty course ? 'Neue Lehrgangs-Vorlage anlegen' : 'Lehrgangs-Vorlage bearbeiten'}" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>${empty course ? 'Neue Lehrgangs-Vorlage anlegen' : 'Lehrgangs-Vorlage bearbeiten'}</h1>
<p>Hier definieren Sie die übergeordnete Vorlage für einen Lehrgang.
	Einzelne Termine (Meetings) werden separat hinzugefügt.</p>

<div class="card form-container"
	style="max-width: 700px; margin: 2rem auto;">
	<form action="${pageContext.request.contextPath}/admin/courses"
		method="post">

		<%-- Hidden fields to tell the servlet whether to create or update --%>
		<input type="hidden" name="action"
			value="${empty course ? 'create' : 'update'}">
		<c:if test="${not empty course}">
			<input type="hidden" name="id" value="${course.id}">
		</c:if>

		<div class="form-group">
			<label for="name">Name der Vorlage (z.B. Grundlehrgang)</label> <input
				type="text" id="name" name="name" value="${course.name}" required>
		</div>

		<div class="form-group">
			<label for="abbreviation">Abkürzung (für Matrix, z.B. GL)</label> <input
				type="text" id="abbreviation" name="abbreviation"
				value="${course.abbreviation}" maxlength="10" required>
		</div>

		<div class="form-group">
			<label for="description">Allgemeine Beschreibung des
				Lehrgangs</label>
			<textarea id="description" name="description" rows="4">${course.description}</textarea>
		</div>

		<div style="display: flex; gap: 1rem; margin-top: 1.5rem;">
			<button type="submit" class="btn">Vorlage Speichern</button>
			<a href="${pageContext.request.contextPath}/admin/courses"
				class="btn" style="background-color: var(--text-muted-color);">Abbrechen</a>
		</div>
	</form>
</div>

<%-- Footer must be included last --%>
<c:import url="/WEB-INF/jspf/footer.jspf" />