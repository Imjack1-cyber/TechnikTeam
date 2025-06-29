<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Bericht: ${reportTitle}" />
</c:import>

<h1>${reportTitle}</h1>
<div class="table-controls">
	<a href="<c:url value='/admin/reports'/>" class="btn btn-secondary"><i
		class="fas fa-arrow-left"></i> Zurück zur Berichtsübersicht</a> <a
		href="?report=${param.report}&export=csv" class="btn btn-success"><i
		class="fas fa-file-csv"></i> Als CSV exportieren</a>
</div>

<div class="table-wrapper">
	<c:if test="${not empty reportData}">
		<table class="data-table">
			<thead>
				<tr>
					<c:forEach var="header" items="${reportData[0].keySet()}">
						<th><c:out value="${fn:replace(header, '_', ' ')}" /></th>
					</c:forEach>
				</tr>
			</thead>
			<tbody>
				<c:forEach var="row" items="${reportData}">
					<tr>
						<c:forEach var="key" items="${reportData[0].keySet()}">
							<td><c:out value="${row[key]}" /></td>
						</c:forEach>
					</tr>
				</c:forEach>
			</tbody>
		</table>
	</c:if>
	<c:if test="${empty reportData}">
		<p style="text-align: center; padding: 2rem;">Keine Daten für
			diesen Bericht verfügbar.</p>
	</c:if>
</div>

<c:import url="../../jspf/main_footer.jspf" />