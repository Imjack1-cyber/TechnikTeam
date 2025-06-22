<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<%--
admin_course_form.jsp

This JSP provides the form for editing an existing parent course template.
The creation of new courses is now handled by a modal on admin_course_list.jsp.

    It is served by: AdminCourseServlet (doGet with action=edit).

    It submits to: AdminCourseServlet (doPost).

    Expected attributes:

        'course' (de.technikteam.model.Course): The course object to edit.
        --%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lehrgangs-Vorlage bearbeiten" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />
<h1>Lehrgangs-Vorlage bearbeiten</h1>
<p>Hier definieren Sie die übergeordnete Vorlage für einen Lehrgang.
	Einzelne Termine (Meetings) werden separat hinzugefügt.</p>
<div class="form-center-wrapper">
	<div class="card">
		<form action="${pageContext.request.contextPath}/admin/courses"
			method="post">
			<input type="hidden" name="action" value="update"> <input
				type="hidden" name="id" value="${course.id}">
			<div class="form-group">
				<label for="name">Name der Vorlage (z.B. Grundlehrgang
					Tontechnik)</label> <input type="text" id="name" name="name"
					value="${course.name}" required>
			</div>

			<div class="form-group">
				<label for="abbreviation">Abkürzung (für Matrix, max. 10
					Zeichen, z.B. GL-Ton)</label> <input type="text" id="abbreviation"
					name="abbreviation" value="${course.abbreviation}" maxlength="10"
					required>
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
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />