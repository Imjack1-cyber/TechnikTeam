<%-- src/main/webapp/views/public/profile.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Mein Profil" />
</c:import>

<h1>Mein Profil</h1>
<p>Hier finden Sie eine Übersicht Ihrer Daten, Qualifikationen und
	Aktivitäten.</p>

<c:import url="/WEB-INF/jspf/message_banner.jspf" />

<div class="responsive-dashboard-grid" id="profile-container">
	<div class="card" id="profile-details-container">
		<h2 class="card-title">Stammdaten</h2>
		<p>Lade Profil...</p>
	</div>
	<div class="card" id="profile-security-container">
		<h2 class="card-title">Sicherheit (Passkeys)</h2>
		<p>Lade Sicherheitseinstellungen...</p>
	</div>
	<div class="card" id="profile-qualifications-container">
		<h2 class="card-title">Meine Qualifikationen</h2>
		<p>Lade Qualifikationen...</p>
	</div>
	<div class="card" style="grid-column: 1/-1;"
		id="profile-achievements-container">
		<h2 class="card-title">Meine Abzeichen</h2>
		<p>Lade Abzeichen...</p>
	</div>
	<div class="card" id="profile-history-container">
		<h2 class="card-title">Meine Event-Historie</h2>
		<p>Lade Event-Historie...</p>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script src="${pageContext.request.contextPath}/js/auth/passkey_auth.js"></script>
<script src="${pageContext.request.contextPath}/js/public/profile.js"></script>