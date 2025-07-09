document.addEventListener('DOMContentLoaded', () => {
	const lightbox = document.getElementById('lightbox');
	if (lightbox) {
		const lightboxImage = document.getElementById('lightbox-image');
		const closeBtn = lightbox.querySelector('.lightbox-close');

		document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
			trigger.addEventListener('click', (e) => {
				e.preventDefault();
				lightboxImage.src = trigger.href;
				lightbox.style.display = 'flex';
			});
		});

		const closeLightbox = () => { lightbox.style.display = 'none'; };
		if (closeBtn) closeBtn.addEventListener('click', closeLightbox);
		lightbox.addEventListener('click', (e) => { if (e.target === lightbox) closeLightbox(); });
		document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && lightbox.style.display === 'flex') closeLightbox(); });
	}

	const tabButtons = document.querySelectorAll('.modal-tab-button');
	const tabContents = document.querySelectorAll('.modal-tab-content');
	tabButtons.forEach(button => {
		button.addEventListener('click', () => {
			tabButtons.forEach(btn => btn.classList.remove('active'));
			button.classList.add('active');
			tabContents.forEach(content => {
				content.classList.toggle('active', content.id === button.dataset.tab);
			});
		});
	});
});