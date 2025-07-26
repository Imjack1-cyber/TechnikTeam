<%-- src/main/webapp/views/public/eventDetails.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Event Details" />
</c:import>

<%-- This page is now a client-side rendered shell. --%>
<div id="event-details-container" data-event-id="${eventId}"
	data-user-id="${sessionScope.user.id}">
	<div id="event-header-placeholder">
		<h1>
			<i class="fas fa-spinner fa-spin"></i> Lade Event...
		</h1>
	</div>
	<div id="event-content-placeholder" class="responsive-dashboard-grid">
		<div class="card" style="grid-column: 1/-1;">
			<p>Lade Aufgaben...</p>
		</div>
		<div class="card">
			<p>Lade Beschreibung...</p>
		</div>
		<div class="card">
			<p>Lade Personalbedarf...</p>
		</div>
		<div class="card">
			<p>Lade Material...</p>
		</div>
		<div class="card">
			<p>Lade Anhänge...</p>
		</div>
		<div class="card">
			<p>Lade Team...</p>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script>
	// Pass data from the shell servlet to the JS file
	document.body.dataset.eventId = "${eventId}";
	document.body.dataset.userId = "${sessionScope.user.id}";
	document.body.dataset.isAdmin = "${sessionScope.user.hasAdminAccess()}";
</script>
<script
	src="${pageContext.request.contextPath}/js/public/eventDetails.js"></script>