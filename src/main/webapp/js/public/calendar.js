document.addEventListener('DOMContentLoaded', function() {
	const calendarEl = document.getElementById('calendar-container');
	const contextPath = document.body.dataset.contextPath || '';

	// FIX: Removed the flawed getComputedStyle check. The calendar should always be initialized
	// if the container element exists in the DOM. CSS media queries are responsible for visibility.
	if (calendarEl) {
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
	}
});