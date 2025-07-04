document.addEventListener('DOMContentLoaded', function() {
	const calendarEl = document.getElementById('calendar');
	const contextPath = document.body.dataset.contextPath || '';

	if (!calendarEl) {
		console.error("Calendar element not found!");
		return;
	}

	const calendar = new FullCalendar.Calendar(calendarEl, {
		schedulerLicenseKey: 'CC-Attribution-NonCommercial-NoDerivatives',
		initialView: 'resourceTimelineMonth',
		locale: 'de',
		headerToolbar: {
			left: 'prev,next today',
			center: 'title',
			right: 'resourceTimelineDay,resourceTimelineWeek,resourceTimelineMonth'
		},
		resourceAreaHeaderContent: 'Material',
		events: function(fetchInfo, successCallback, failureCallback) {
			const startStr = fetchInfo.startStr.substring(0, 10);
			const endStr = fetchInfo.endStr.substring(0, 10);
			const url = `${contextPath}/api/admin/resource-calendar?start=${startStr}&end=${endStr}`;

			fetch(url)
				.then(response => {
					if (!response.ok) {
						throw new Error(`HTTP error! status: ${response.status}`);
					}
					return response.json();
				})
				.then(data => {
					// FullCalendar's event source function expects just the events array
					// The resources are loaded separately
					successCallback(data.events);
				})
				.catch(error => {
					console.error('Error fetching resource calendar data:', error);
					failureCallback(error);
				});
		},
		resources: function(fetchInfo, successCallback, failureCallback) {
			// We fetch resources from the same endpoint
			const startStr = fetchInfo.startStr.substring(0, 10);
			const endStr = fetchInfo.endStr.substring(0, 10);
			const url = `${contextPath}/api/admin/resource-calendar?start=${startStr}&end=${endStr}`;

			fetch(url)
				.then(response => response.json())
				.then(data => successCallback(data.resources))
				.catch(error => failureCallback(error));
		},
		eventClick: function(info) {
			info.jsEvent.preventDefault();
			if (info.event.url) {
				window.open(info.event.url, "_self");
			}
		}
	});

	calendar.render();
});