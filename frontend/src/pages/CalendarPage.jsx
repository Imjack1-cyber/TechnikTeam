import React from 'react';
import useApi from '@/hooks/useApi';
import apiClient from '@/services/apiClient';
import CalendarDesktopView from '@/components/calendar/CalendarDesktopView';
import CalendarMobileView from '@/components/calendar/CalendarMobileView';

const CalendarPage = () => {
	// NOTE: This assumes the backend exposes a public endpoint at /api/v1/public/calendar/entries
	// which returns a flat list of all event and meeting objects for the calendar.
	const { data: calendarEntries, loading, error } = useApi(() => apiClient.get('/public/calendar/entries'));

	if (loading) {
		return <div>Lade Kalenderdaten...</div>;
	}

	if (error) {
		return <div className="error-message">{error}</div>;
	}

	return (
		<div>
			<h1>
				<i className="fas fa-calendar-alt"></i> Terminübersicht
			</h1>
			<p>
				Übersicht aller anstehenden Veranstaltungen und Lehrgänge.
				<a href="/api/v1/public/calendar.ics" className="btn btn-small btn-info" style={{ marginLeft: '1rem' }}>
					<i className="fas fa-rss"></i> Kalender abonnieren
				</a>
			</p>

			<div className="mobile-list-view">
				<CalendarMobileView entries={calendarEntries || []} />
			</div>

			<div className="desktop-calendar-view card">
				<CalendarDesktopView entries={calendarEntries || []} />
			</div>
		</div>
	);
};

export default CalendarPage;