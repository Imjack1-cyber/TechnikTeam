<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt"%>
<!DOCTYPE html>
<head>
<link rel="stylesheet" href="css/style.css">
</head>

<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Lehrgänge" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<%-- Add at the top of lehrgaenge.jsp --%>
<h1>Anstehende Lehrgänge</h1>
<div class="filter-controls" style="margin-bottom: 1rem;">
	<button id="filter-all" class="btn-small">Alle anzeigen</button>
	<button id="filter-attended" class="btn-small">Nur besuchte</button>
</div>

<%-- Modify the table body --%>
<tbody id="course-table-body">
	<c:forEach var="course" items="${courses}">
		<tr
			data-attended="${attendedCourseIds.contains(course.id) ? 'true' : 'false'}">
			<%-- ... table cells ... --%>
		</tr>
	</c:forEach>
</tbody>

<%-- Add at the bottom of lehrgaenge.jsp, before the footer import --%>
<script>
document.addEventListener('DOMContentLoaded', () => {
    const tableBody = document.getElementById('course-table-body');
    const rows = tableBody.querySelectorAll('tr');

    document.getElementById('filter-all').addEventListener('click', () => {
        rows.forEach(row => row.style.display = '');
    });
    
    document.getElementById('filter-attended').addEventListener('click', () => {
        rows.forEach(row => {
            if (row.dataset.attended === 'true') {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });
});
</script>

<c:import url="/WEB-INF/jspf/footer.jspf" />