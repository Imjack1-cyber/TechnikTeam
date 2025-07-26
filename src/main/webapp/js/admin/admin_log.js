// src/main/webapp/js/admin/admin_log.js
document.addEventListener('DOMContentLoaded', () => {
	const contextPath = document.body.dataset.contextPath || '';
	const tableBody = document.getElementById('log-table-body');
	const mobileList = document.getElementById('log-mobile-list');

	if (!tableBody || !mobileList) return;

	const api = {
		getLogs: () => fetch(`${contextPath}/api/v1/logs`).then(res => res.json())
	};

	const renderLogs = (logs) => {
		tableBody.innerHTML = '';
		mobileList.innerHTML = '';

		if (!logs || logs.length === 0) {
			tableBody.innerHTML = `<tr><td colspan="4" style="text-align: center;">Keine Log-Einträge gefunden.</td></tr>`;
			mobileList.innerHTML = `<div class="card"><p>Keine Log-Einträge gefunden.</p></div>`;
			return;
		}

		logs.forEach(log => {
			const formattedTime = new Date(log.actionTimestamp).toLocaleString('de-DE');

			// Desktop Row
			const row = document.createElement('tr');
			row.innerHTML = `
                <td>${formattedTime} Uhr</td>
                <td>${escape(log.adminUsername)}</td>
                <td>${escape(log.actionType)}</td>
                <td style="white-space: normal;">${escape(log.details)}</td>
            `;
			tableBody.appendChild(row);

			// Mobile Card
			const card = document.createElement('div');
			card.className = 'list-item-card';
			card.innerHTML = `
                <h3 class="card-title" style="word-break: break-all;">${escape(log.actionType)}</h3>
                <div class="card-row"><span>Wer:</span> <strong>${escape(log.adminUsername)}</strong></div>
                <div class="card-row"><span>Wann:</span> <strong>${formattedTime} Uhr</strong></div>
                <div class="card-row" style="flex-direction: column; align-items: flex-start;">
                    <span style="font-weight: 500;">Details:</span>
                    <p style="margin-top: 0.25rem; font-size: 0.9em; width: 100%;">${escape(log.details)}</p>
                </div>
            `;
			mobileList.appendChild(card);
		});
	};

	const loadLogs = async () => {
		try {
			const result = await api.getLogs();
			if (result.success) {
				renderLogs(result.data);
			} else { throw new Error(result.message); }
		} catch (error) {
			console.error("Failed to load logs:", error);
			tableBody.innerHTML = `<tr><td colspan="4" class="error-message">Fehler beim Laden der Logs.</td></tr>`;
		}
	};

	const escape = (str) => {
		if (!str) return '';
		return str.replace(/&/g, '&').replace(/</g, '<').replace(/>/g, '>').replace(/"/g, '"').replace(/'/g, ''');
    };

	loadLogs();
});