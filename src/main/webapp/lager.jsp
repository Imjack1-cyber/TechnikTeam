<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lager" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<h1>Lagerübersicht</h1>

<c:forEach var="locationEntry" items="${storageData}">
	<div class="location-section">
		<h2>${locationEntry.key}</h2>
		<table class="styled-table">
			<thead>
				<tr>
					<th>Gerät</th>
					<th>Schrank</th>
					<th>Regal</th>
					<th>Fach</th>
					<th>Anzahl</th>
					<th>Bild</th>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="item" items="${locationEntry.value}">
					<tr>
						<td>${item.name}</td>
						<td>${item.cabinet}</td>
						<td>${item.shelf}</td>
						<td>${item.compartment}</td>
						<td>${item.quantity}</td>
						<td><c:if test="${not empty item.imagePath}">
								<img src="${pageContext.request.contextPath}/${item.imagePath}"
									alt="${item.name}" width="50">
							</c:if></td>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</div>
</c:forEach>

<style>
.location-section {
	margin-bottom: 2rem;
}

.styled-table {
	width: 100%;
	border-collapse: collapse;
	margin-top: 1rem;
}

.styled-table th, .styled-table td {
	border: 1px solid var(--border-color);
	padding: 0.75rem;
	text-align: left;
}

.styled-table thead {
	background-color: var(--secondary-color);
}
</style>

<c:import url="/WEB-INF/jspf/footer.jspf" />