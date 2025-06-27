<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="pageTitle" value="Artikeldetails" />
</c:import>

<h1>Artikeldetails</h1>

<div class="card" style="max-width: 600px; margin: 1rem auto;">
	<h2 class="card-title">
		<c:out value="${item.name}" />
	</h2>

	<ul style="list-style: none; padding: 0;">
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Status:</strong>
			<span class="status-badge ${item.availabilityStatusCssClass}">
				<c:out value="${item.availabilityStatus}" />
		</span></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Verfügbare
				Anzahl:</strong> <c:out value="${item.availableQuantity}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Defekte
				Anzahl:</strong> <c:out value="${item.defectiveQuantity}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Ort:</strong>
			<c:out value="${item.location}" /></li>
		<li
			style="padding: 0.75rem 0; border-bottom: 1px solid var(--border-color); display: flex; justify-content: space-between;"><strong>Schrank:</strong>
			<c:out value="${not empty item.cabinet ? item.cabinet : 'N/A'}" /></li>
	</ul>

	<div style="margin-top: 2rem;">
		<a href="<c:url value='/lager'/>" class="btn">Zurück zur
			Lagerübersicht</a>
	</div>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />