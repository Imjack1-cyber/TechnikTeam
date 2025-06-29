<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"
	isELIgnored="false"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/functions" prefix="fn"%>

<c:import url="../../jspf/main_header.jspf">
	<c:param name="pageTitle" value="Defekte Artikel" />
</c:import>

<h1>
	<i class="fas fa-wrench"></i> Defekte Artikel verwalten
</h1>
<p>Hier sind alle Artikel gelistet, von denen mindestens ein
	Exemplar als defekt markiert wurde.</p>

<c:import url="../../jspf/message_banner.jspf" />

<div class="table-wrapper">
	<table class="data-table">
		<thead>
			<tr>
				<th>Name</th>
				<th>Defekt / Gesamt</th>
				<th>Grund</th>
				<th>Aktion</th>
			</tr>
		</thead>
		<tbody>
			<c:if test="${empty defectiveItems}">
				<tr>
					<td colspan="4" style="text-align: center;">Aktuell sind keine
						Artikel als defekt gemeldet.</td>
				</tr>
			</c:if>
			<c:forEach var="item" items="${defectiveItems}">
				<tr>
					<td><c:out value="${item.name}" /></td>
					<td><c:out value="${item.defectiveQuantity}" /> / <c:out
							value="${item.quantity}" /></td>
					<td><c:out value="${item.defectReason}" /></td>
					<td>
						<button class="btn btn-small btn-success defect-modal-btn"
							data-item-id="${item.id}"
							data-item-name="${fn:escapeXml(item.name)}"
							data-current-defect-qty="${item.defectiveQuantity}"
							data-current-reason="${fn:escapeXml(item.defectReason)}">Status
							bearbeiten</button>
					</td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- Modal for updating defect status -->
<div class="modal-overlay" id="defect-modal">
	<div class="modal-content">
		<button class="modal-close-btn">×</button>
		<h3 id="defect-modal-title">Defekt-Status bearbeiten</h3>
		<form action="${pageContext.request.contextPath}/admin/storage"
			method="post">
			<input type="hidden" name="action" value="updateDefect"> <input
				type="hidden" name="id" id="defect-item-id"> <input
				type="hidden" name="returnTo" value="defects">
			<div class="form-group">
				<label for="defective_quantity">Anzahl defekter Artikel</label><input
					type="number" name="defective_quantity" id="defective_quantity"
					min="0" required>
			</div>
			<div class="form-group">
				<label for="defect_reason">Grund (optional)</label>
				<textarea name="defect_reason" id="defect_reason" rows="3"></textarea>
			</div>
			<button type="submit" class="btn">Speichern</button>
		</form>
	</div>
</div>

<c:import url="../../jspf/main_footer.jspf" />
<script>
document.addEventListener('DOMContentLoaded', () => {
    const defectModal = document.getElementById('defect-modal');
    if (!defectModal) return;

    const modalTitle = document.getElementById('defect-modal-title');
    const itemIdInput = document.getElementById('defect-item-id');
    const defectQtyInput = document.getElementById('defective_quantity');
    const reasonInput = document.getElementById('defect_reason');
    const closeModalBtn = defectModal.querySelector('.modal-close-btn');

    document.querySelectorAll('.defect-modal-btn').forEach(btn => {
        btn.addEventListener('click', () => {
            modalTitle.textContent = `Defekt-Status für "${btn.dataset.itemName}" bearbeiten`;
            itemIdInput.value = btn.dataset.itemId;
            defectQtyInput.value = btn.dataset.currentDefectQty;
            defectQtyInput.max = btn.dataset.maxQty; // Set max based on total quantity
            reasonInput.value = btn.dataset.currentReason;
            defectModal.classList.add('active');
        });
    });

    closeModalBtn.addEventListener('click', () => defectModal.classList.remove('active'));
    defectModal.addEventListener('click', (e) => {
        if (e.target === defectModal) defectModal.classList.remove('active');
    });
});
</script>