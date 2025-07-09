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
			info.jsEvent.preventDefault(); 
			if (info.event.url) {
				window.open(info.event.url, "_self");
			}
		},
		eventTimeFormat: { 
			hour: '2-digit',
			minute: '2-digit',
			meridiem: false,
			hour12: false
		}
	});
	calendar.render();
});