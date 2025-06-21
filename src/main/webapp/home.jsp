<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="jakarta.tags.core" prefix="c"%>
<c:import url="/WEB-INF/jspf/header.jspf">
	<c:param name="title" value="Home" />
</c:import>
<c:import url="/WEB-INF/jspf/navigation.jspf" />

<!--  The user's main dashboard after logging in. It is served by HomeServlet and provides a quick overview of upcoming events and courses. -->

<h1>Willkommen zurück, ${sessionScope.user.username}!</h1>
<div class="dashboard">
	<div class="card">
		<h2 class="card-title">Anstehende Veranstaltungen</h2>
		<ul>
			<c:forEach var="event" items="${upcomingEvents}">
				<li>${event.name}- ${event.formattedEventDateTime} Uhr</li>
			</c:forEach>
		</ul>
	</div>
	<div class="card">
		<h2 class="card-title">Anstehende Lehrgänge</h2>
		<ul>
			<c:forEach var="course" items="${upcomingCourses}">
				<li>${course.name}- ${course.formattedCourseDateTime} Uhr</li>
			</c:forEach>
		</ul>
	</div>
</div>
<c:import url="/WEB-INF/jspf/footer.jspf" />