<%-- src/main/webapp/views/public/home.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Home" />
</c:import>

<h1>
	Willkommen zurück,
	<c:out value="${sessionScope.user.username}" />
	!
</h1>
<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="dashboard-grid">
	<div class="card" id="assigned-events-widget">
		<h2 class="card-title">Meine nächsten Einsätze</h2>
		<div id="assigned-events-content">
			<p>Lade Daten...</p>
		</div>
		<a href="${pageContext.request.contextPath}/veranstaltungen"
			class="btn btn-small" style="margin-top: 1rem;">Alle
			Veranstaltungen anzeigen</a>
	</div>

	<div class="card" id="open-tasks-widget">
		<h2 class="card-title">Meine offenen Aufgaben</h2>
		<div id="open-tasks-content">
			<p>Lade Daten...</p>
		</div>
	</div>

	<div class="card" id="upcoming-events-widget">
		<h2 class="card-title">Weitere anstehende Veranstaltungen</h2>
		<div id="upcoming-events-content">
			<p>Lade Daten...</p>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/public/home.js"></script>