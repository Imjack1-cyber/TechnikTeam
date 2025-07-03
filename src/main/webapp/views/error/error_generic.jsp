<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isErrorPage="true" isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/error_header.jspf">
	<c:param name="pageTitle" value="Fehler" />
</c:import>

<c:set var="statusCode" value="${pageContext.errorData.statusCode}" />

<c:set var="errorMessage"
	value="Ein unerwarteter Fehler ist aufgetreten." />
<c:choose>
	<c:when test="${statusCode == 402}">
		<c:set var="errorMessage"
			value="Zahlung erforderlich. (Dieser Code ist für die zukünftige Verwendung reserviert.)" />
	</c:when>
	<c:when test="${statusCode == 405}">
		<c:set var="errorMessage"
			value="Methode nicht erlaubt. Die verwendete HTTP-Methode (z.B. GET, POST) ist für diese Ressource nicht zulässig." />
	</c:when>
	<c:when test="${statusCode == 406}">
		<c:set var="errorMessage"
			value="Nicht akzeptabel. Der Server kann keine Antwort generieren, die den Kriterien des Clients entspricht." />
	</c:when>
	<c:when test="${statusCode == 408}">
		<c:set var="errorMessage"
			value="Anfrage-Zeitüberschreitung. Der Server hat zu lange auf eine Antwort gewartet." />
	</c:when>
	<c:when test="${statusCode == 409}">
		<c:set var="errorMessage"
			value="Konflikt. Die Anfrage konnte wegen eines Konflikts mit dem aktuellen Zustand der Ressource nicht abgeschlossen werden." />
	</c:when>
	<c:when test="${statusCode == 410}">
		<c:set var="errorMessage"
			value="Verschwunden. Die angeforderte Ressource ist nicht mehr verfügbar und wird es auch in Zukunft nicht sein." />
	</c:when>
	<c:when test="${statusCode == 501}">
		<c:set var="errorMessage"
			value="Nicht implementiert. Der Server erkennt die Anfragemethode nicht oder kann sie nicht erfüllen." />
	</c:when>
	<c:when test="${statusCode == 502}">
		<c:set var="errorMessage"
			value="Bad Gateway. Der Server hat als Gateway oder Proxy eine ungültige Antwort vom Upstream-Server erhalten." />
	</c:when>
	<c:when test="${statusCode == 504}">
		<c:set var="errorMessage"
			value="Gateway-Zeitüberschreitung. Der Server hat als Gateway oder Proxy keine rechtzeitige Antwort erhalten." />
	</c:when>
</c:choose>

<div class="error-page-container">
	<h1 class="error-code">${statusCode}</h1>
	<h2>${errorMessage}</h2>
	<p class="error-message-text">Wenn Sie glauben, dass dies ein
		Fehler ist, kontaktieren Sie bitte den Administrator.</p>
	<a href="${pageContext.request.contextPath}/home"
		class="btn btn-primary"> <i class="fas fa-home"></i> Zurück zur
		Startseite
	</a>
</div>

<c:import url="/WEB-INF/jspf/error_footer.jspf" />