<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<%-- REMOVED: hardcoded data-theme="dark" --%>
<html lang="de">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Lager-Aktion: ${item.name}</title>
<link rel="stylesheet"
	href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/5.15.4/css/all.min.css">
<link rel="stylesheet"
	href="${pageContext.request.contextPath}/css/style.css">

<%-- ADDED: Inline script to set the theme from localStorage on page load --%>
<script>
    // This script runs immediately to prevent a "flash" of the wrong theme.
    const savedTheme = localStorage.getItem('theme') || 'light';
    document.documentElement.setAttribute('data-theme', savedTheme);
</script>

</head>
<body class="qr-action-body">

	<div class="qr-action-container card">
		<p>Aktion für:</p>
		<h1 class="qr-action-item-name">
			<c:out value="${item.name}" />
		</h1>
		<p class="details-subtitle"
			style="margin-top: -1rem; margin-bottom: 2rem;">Bestand:
			${item.quantity} / ${item.maxQuantity} (Verfügbar:
			${item.availableQuantity})</p>


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
				type="hidden" name="redirectUrl"
				value="${pageContext.request.contextPath}/lager/aktionen?id=${item.id}">

			<div class="form-group">
				<label for="quantity">Anzahl</label> <input type="number"
					name="quantity" id="quantity" value="1" min="1" required>
			</div>

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
				<button type="submit" name="type" value="checkout" id="checkout-btn"
					class="btn btn-danger qr-action-btn"
					${item.availableQuantity <= 0 ? 'disabled' : ''}>
					<i class="fas fa-sign-out-alt"></i> Entnehmen
				</button>
				<button type="submit" name="type" value="checkin" id="checkin-btn"
					class="btn btn-success qr-action-btn"
					${item.maxQuantity > 0 && item.quantity >= item.maxQuantity ? 'disabled' : ''}>
					<i class="fas fa-sign-in-alt"></i> Einräumen
				</button>
			</div>
		</form>
	</div>

	<script>
    document.addEventListener('DOMContentLoaded', () => {
        const quantityInput = document.getElementById('quantity');
        const checkoutBtn = document.getElementById('checkout-btn');
        const checkinBtn = document.getElementById('checkin-btn');
        
        const availableQty = ${item.availableQuantity};
        const totalQty = ${item.quantity};
        const maxQty = ${item.maxQuantity};

        // Set initial state for checkout
        quantityInput.max = availableQty;

        checkoutBtn.addEventListener('click', () => {
            quantityInput.max = availableQty;
            quantityInput.title = `Maximal entnehmbar: ${availableQty}`;
        });

        checkinBtn.addEventListener('click', () => {
            const availableSpace = maxQty > 0 ? maxQty - totalQty : 9999;
            quantityInput.max = availableSpace > 0 ? availableSpace : 9999;
            quantityInput.title = `Maximal einräumbar: ${availableSpace}`;
        });
    });
    </script>

</body>
</html>