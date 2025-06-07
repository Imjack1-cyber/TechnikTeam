<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Dateiverwaltung" />
</c:import>
<c:import url="/WEB-INF/jspf/admin_navigation.jspf" />

<h1>Dateiverwaltung</h1>

<div class="card">
	<h2 class="card-title">Neue Datei hochladen</h2>
	<form action="${pageContext.request.contextPath}/admin/files"
		method="post" enctype="multipart/form-data">
		<div class="form-group">
			<label for="file">Datei auswählen</label> <input type="file"
				name="file" id="file" required>
		</div>
		<div class="form-group">
			<label for="category">Kategorie</label> <select name="category"
				id="category" required>
				<option value="BEDIENUNGSANLEITUNGEN">Bedienungsanleitungen</option>
				<option value="LEHRGAENGE">Lehrgänge</option>
				<option value="LAGER">Lager</option>
				<option value="SONSTIGES">Sonstiges</option>
			</select>
		</div>
		<button type="submit" class="btn">Hochladen</button>
	</form>
</div>

<%-- Ersetzen Sie den "Vorhandene Dateien"-Platzhalter --%>
<div class="card" style="margin-top: 2rem;">
	<h2 class="card-title">Vorhandene Dateien</h2>
	<table class="styled-table">
		<thead>
			<tr>
				<th>Dateiname</th>
				<th>Kategorie</th>
				<th>Aktionen</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="categoryEntry" items="${groupedFiles}">
				<c:if test="${not empty categoryEntry.value}">
					<tr
						style="background-color: var(--secondary-color); font-weight: bold;">
						<td colspan="3">${categoryEntry.key}</td>
					</tr>
					<c:forEach var="file" items="${categoryEntry.value}">
						<tr>
							<td><a
								href="${pageContext.request.contextPath}/${file.filepath}"
								target="_blank">${file.filename}</a></td>
							<td>${file.category}</td>
							<td>
								<form action="${pageContext.request.contextPath}/admin/files"
									method="post" style="display: inline;">
									<input type="hidden" name="action" value="delete"> <input
										type="hidden" name="fileId" value="${file.id}">
									<button type="submit" class="btn-small btn-danger"
										onclick="return confirm('Datei \'${file.filename}\' wirklich löschen?')">Löschen</button>
								</form>
							</td>
						</tr>
					</c:forEach>
				</c:if>
			</c:forEach>
		</tbody>
	</table>
</div>

<c:import url="/WEB-INF/jspf/footer.jspf" />