// src/main/webapp/js/public/home.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';

	const assignedEventsContent = document.getElementById('assigned-events-content');
	const openTasksContent = document.getElementById('open-tasks-content');
	const upcomingEventsContent = document.getElementById('upcoming-events-content');

	if (!assignedEventsContent || !openTasksContent || !upcomingEventsContent) return;

	const api = {
		getDashboardData: () => fetch(`${contextPath}/api/v1/public/dashboard`).then(res => res.json())
	};

	const escape = (str) => {
		if (!str) return '';
		const div = document.createElement('div');
		div.innerText = str;
		return div.innerHTML;
	};

	const renderList = (container, items, message, itemRenderer) => {
		if (!items || items.length === 0) {
			container.innerHTML = `<p>${message}</p>`;
			return;
		}
		container.innerHTML = `<ul class="details-list">${items.map(itemRenderer).join('')}</ul>`;
	};

	const renderAssignedEvent = (event) => `
        <li>
            <a href="${contextPath}/veranstaltungen/details?id=${event.id}">${escape(event.name)}</a>
            <small>${escape(event.formattedEventDateTimeRange)}</small>
        </li>`;

	const renderOpenTask = (task) => `
        <li>
            <a href="${contextPath}/veranstaltungen/details?id=${task.eventId}">
                ${escape(task.description)}
                <small style="display: block; color: var(--text-muted-color);">Für Event: ${escape(task.eventName)}</small>
            </a>
        </li>`;

	const renderUpcomingEvent = (event) => `
        <li>
            <a href="${contextPath}/veranstaltungen/details?id=${event.id}">${escape(event.name)}</a>
            <small>${escape(event.formattedEventDateTimeRange)}</small>
        </li>`;

	const loadDashboard = async () => {
		try {
			const result = await api.getDashboardData();
			if (result.success) {
				const data = result.data;
				renderList(assignedEventsContent, data.assignedEvents, 'Du bist derzeit für keine kommenden Events fest eingeteilt.', renderAssignedEvent);
				renderList(openTasksContent, data.openTasks, 'Super! Du hast aktuell keine offenen Aufgaben.', renderOpenTask);
				renderList(upcomingEventsContent, data.upcomingEvents, 'Keine weiteren anstehenden Veranstaltungen.', renderUpcomingEvent);
			} else {
				throw new Error(result.message);
			}
		} catch (error) {
			console.error("Failed to load dashboard data:", error);
			assignedEventsContent.innerHTML = `<p class="error-message">Daten konnten nicht geladen werden.</p>`;
			openTasksContent.innerHTML = `<p class="error-message">Daten konnten nicht geladen werden.</p>`;
			upcomingEventsContent.innerHTML = `<p class="error-message">Daten konnten nicht geladen werden.</p>`;
		}
	};

	loadDashboard();
});