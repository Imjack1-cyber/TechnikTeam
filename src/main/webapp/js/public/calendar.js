document.addEventListener('DOMContentLoaded', function() {
    const calendarEl = document.getElementById('calendar');
    const contextPath = document.body.dataset.contextPath || '';

    if (!calendarEl) {
        console.error("Calendar element not found!");
        return;
    }

	const calendar = new FullCalendar.Calendar(calendarEl, {
		initialView: 'dayGridMonth',
		locale: 'de',
		headerToolbar: {
			left: 'prev,next today',
			center: 'title',
			right: 'dayGridMonth,timeGridWeek,listWeek'
		},
		events: `${contextPath}/api/calendar/entries`,
		eventClick: function(info) {
			info.jsEvent.preventDefault(); // don't let the browser navigate
			if (info.event.url) {
				window.open(info.event.url, "_self");
			}
		},
        eventTimeFormat: { // German time format
            hour: '2-digit',
            minute: '2-digit',
            meridiem: false,
            hour12: false
        }
	});
	calendar.render();
});
```
***
### FILE: C:\Users\techn\eclipse\workspace\TechnikTeam\src\main\webapp\js\public\storage_item_details.js
```javascript
document.addEventListener('DOMContentLoaded', () => {
    const lightbox = document.getElementById('lightbox');
    if (!lightbox) return;

    const lightboxImage = document.getElementById('lightbox-image');
    const closeBtn = lightbox.querySelector('.lightbox-close');

    document.querySelectorAll('.lightbox-trigger').forEach(trigger => {
        trigger.addEventListener('click', (e) => {
            e.preventDefault();
            lightboxImage.src = trigger.href; // Get src from the anchor's href
            lightbox.style.display = 'flex';
        });
    });

    const closeLightbox = () => { lightbox.style.display = 'none'; };
    if (closeBtn) closeBtn.addEventListener('click', closeLightbox);
    lightbox.addEventListener('click', (e) => { if (e.target === lightbox) closeLightbox(); });
    document.addEventListener('keydown', (e) => { if (e.key === 'Escape' && lightbox.style.display === 'flex') closeLightbox(); });
});