document.addEventListener('DOMContentLoaded', function() {
	const calendarEl = document.getElementById('calendar-container');
	const contextPath = document.body.dataset.contextPath || '';

	// This check ensures FullCalendar only runs if the desktop container is visible
	if (calendarEl && getComputedStyle(calendarEl).display !== 'none') {
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