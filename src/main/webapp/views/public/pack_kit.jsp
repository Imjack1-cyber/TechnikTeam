<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<c:import url="/WEB-INF/jspf/main_header.jspf">
	<c:param name="pageTitle" value="Packliste: ${kit.name}" />
	<c:param name="showNav" value="false" />
</c:import>

<div class="main-content" style="max-width: 800px; margin: 2rem auto;">
	<div class="card">
		<div
			style="display: flex; justify-content: space-between; align-items: start;">
			<div>
				<h1>
					Packliste:
					<c:out value="${kit.name}" />
				</h1>
				<p class="details-subtitle" style="margin-top: -1rem;">
					<c:out value="${kit.description}" />
				</p>
			</div>
			<button class="btn no-print" onclick="window.print()">
				<i class="fas fa-print"></i> Drucken
			</button>
		</div>

		<c:if test="${not empty kit.location}">
			<div class="card" style="background-color: var(--bg-color);">
				<h3 class="card-title" style="border: none; padding: 0;">Standort</h3>
				<p style="font-size: 1.2rem; font-weight: 500;">
					<c:out value="${kit.location}" />
				</p>
			</div>
		</c:if>
		<c:if test="${empty kit.location}">
			<p class="info-message">Für dieses Kit ist kein physischer
				Standort hinterlegt.</p>
		</c:if>

		<h3 style="margin-top: 2rem;">Inhalt zum Einpacken</h3>
		<ul class="details-list">
			<c:if test="${empty kitItems}">
				<li>Dieses Kit hat keinen definierten Inhalt.</li>
			</c:if>
			<c:forEach var="item" items="${kitItems}">
				<li><label
					style="display: flex; align-items: center; gap: 1rem; cursor: pointer; width: 100%;">
						<input type="checkbox"
						style="width: 1.5rem; height: 1.5rem; flex-shrink: 0;"> <span>
							<strong>${item.quantity}x</strong> <c:out
								value="${item.itemName}" />
					</span>
				</label></li>
			</c:forEach>
		</ul>
		<div class="no-print" style="margin-top: 2rem; text-align: center;">
			<a href="${pageContext.request.contextPath}/lager"
				class="btn btn-secondary">Zurück zur Lagerübersicht</a>
		</div>
	</div>
</div>

<c:import url="/WEB-INF/jspf/main_footer.jspf" />