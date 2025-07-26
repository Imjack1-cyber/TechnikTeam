document.addEventListener('DOMContentLoaded', function() {
	const calendarEl = document.getElementById('calendar-container');
	const contextPath = document.body.dataset.contextPath || '';

	if (calendarEl) {
		try {
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
					info.jsEvent.preventDefault();
					if (info.event.url) {
						window.location.href = info.event.url;
					}
				}
			});
			calendar.render();
		} catch (e) {
			console.error("FullCalendar failed to initialize. This is often caused by a corrupted library file.", e);
			calendarEl.innerHTML = `<div class="error-message">Kalender konnte nicht geladen werden. Möglicherweise ist eine Bibliotheksdatei beschädigt.</div>`;
		}
	}
});