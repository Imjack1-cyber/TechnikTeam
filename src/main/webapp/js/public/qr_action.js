document.addEventListener('DOMContentLoaded', () => {
	const quantityInput = document.getElementById('quantity');
	const checkoutBtn = document.getElementById('checkout-btn');
	const checkinBtn = document.getElementById('checkin-btn');

	const form = document.querySelector('.qr-action-container form');
	const availableQty = parseInt(form.dataset.availableQty, 10);
	const totalQty = parseInt(form.dataset.totalQty, 10);
	const maxQty = parseInt(form.dataset.maxQty, 10);

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