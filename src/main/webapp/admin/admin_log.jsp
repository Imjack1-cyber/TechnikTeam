<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Admin Log" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Admin Aktions-Protokoll</h1>

<div class="card">
	<div class="form-group">
		<label for="log-filter">Protokoll filtern</label> <input type="search"
			id="log-filter"
			placeholder="Nach Details, Name oder Aktion filtern...">
	</div>
</div>

<!-- MOBILE LAYOUT -->
<div class="mobile-card-list" id="log-card-list">
	<c:forEach var="log" items="${logs}">
		<div class="list-item-card">
			<p>${log.details}</p>
			<div class="card-row">
				<span>Wer:</span> <span>${log.adminUsername}</span>
			</div>
			<div class="card-row">
				<span>Wann:</span> <span>${log.formattedActionTimestamp} Uhr</span>
			</div>
		</div>
	</c:forEach>
</div>

<!-- DESKTOP LAYOUT -->
<div class="desktop-table-wrapper">
	<table class="desktop-table" id="log-table">
		<thead>
			<tr>
				<th>Wann</th>
				<th>Wer</th>
				<th>Was</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="log" items="${logs}">
				<tr>
					<td>${log.formattedActionTimestamp}Uhr</td>
					<td>${log.adminUsername}</td>
					<td>${log.details}</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />

<%-- This script provides the live filtering functionality --%>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const filterInput = document.getElementById('log-filter');
    const desktopRows = document.querySelectorAll('#log-table tbody tr');
    const mobileCards = document.querySelectorAll('#log-card-list .list-item-card');

    filterInput.addEventListener('input', (e) => {
        const searchTerm = e.target.value.toLowerCase();
        
        desktopRows.forEach(row => {
            row.style.display = row.textContent.toLowerCase().includes(searchTerm) ? '' : 'none';
        });

        mobileCards.forEach(card => {
            card.style.display = card.textContent.toLowerCase().includes(searchTerm) ? '' : 'none';
        });
    });
});
</script>