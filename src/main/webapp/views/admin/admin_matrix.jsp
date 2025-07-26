<%-- src/main/webapp/views/admin/admin_matrix.jsp --%>
<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Qualifikations-Matrix" />
</c:import>
<h1>
	<i class="fas fa-th-list"></i> Qualifikations-Matrix
</h1>
<p>Klicken Sie auf eine Zelle, um die Teilnahme an einem Meeting zu
	bearbeiten. Die Kopfzeile und die Benutzerleiste bleiben beim Scrollen
	fixiert.</p>
<c:import url="/WEB-INF/jspf/message_banner.jspf" />
<div class="horizontal-scroll-hint">
	<i class="fas fa-arrows-alt-h"></i> Tabelle ist seitlich scrollbar
</div>
<div class="table-wrapper" id="matrix-table-container">
	<div class="card">
		<p>Lade Matrix-Daten...</p>
	</div>
</div>
<!-- Attendance Modal -->
<div class="modal-overlay" id="attendance-modal">
	<%-- Modal content remains the same as previous step --%>
</div>
<c:import url="/WEB-INF/jspf/main_footer.jspf" />
<script
	src="${pageContext.request.contextPath}/js/admin/admin_matrix.js"></script>