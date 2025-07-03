<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html lang="de" data-theme="dark">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Lager-Aktion: ${item.name}</title>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">
</head>
<body class="qr-action-body">

	<div class="qr-action-container card">
		<p>Aktion für:</p>
		<h1 class="qr-action-item-name">
			<c:out value="${item.name}" />
		</h1>

		<c:if test="${not empty sessionScope.successMessage}">
			<p class="success-message" style="margin-top: 1rem;">${sessionScope.successMessage}</p>
			<c:remove var="successMessage" scope="session" />
		</c:if>
		<c:if test="${not empty sessionScope.errorMessage}">
			<p class="error-message" style="margin-top: 1rem;">${sessionScope.errorMessage}</p>
			<c:remove var="errorMessage" scope="session" />
		</c:if>

		<form action="${pageContext.request.contextPath}/lager/transaktion"
			method="post">
			<input type="hidden" name="itemId" value="${item.id}"> <input
				type="hidden" name="quantity" value="1"> <input
				type="hidden" name="redirectUrl"
				value="${pageContext.request.contextPath}/lager/aktionen?id=${item.id}">

			<div class="form-group">
				<label for="notes">Notiz (optional, z.B. für welches Event)</label>
				<input type="text" name="notes" id="notes"
					placeholder="z.B. für Event XYZ">
			</div>
			<div class="form-group">
				<label for="eventId">Zuweisen zu Event (optional)</label> <select
					name="eventId" id="eventId">
					<option value="">Kein Event</option>
					<c:forEach var="event" items="${activeEvents}">
						<option value="${event.id}">${event.name}</option>
					</c:forEach>
				</select>
			</div>

			<div class="qr-action-buttons">
				<button type="submit" name="type" value="checkout"
					class="btn btn-danger qr-action-btn"
					${item.availableQuantity <= 0 ? 'disabled' : ''}>
					<i class="fas fa-sign-out-alt"></i> 1 Stk. Entnehmen
				</button>
				<button type="submit" name="type" value="checkin"
					class="btn btn-success qr-action-btn">
					<i class="fas fa-sign-in-alt"></i> 1 Stk. Einräumen
				</button>
			</div>
		</form>
	</div>

</body>
</html>