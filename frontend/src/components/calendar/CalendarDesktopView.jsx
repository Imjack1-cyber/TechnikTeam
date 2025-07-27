import React, { useState, useMemo } from 'react';
import { Link } from 'react-router-dom';
import {
	format,
	addMonths,
	subMonths,
	startOfMonth,
	endOfMonth,
	startOfWeek,
	endOfWeek,
	eachDayOfInterval,
	isSameMonth,
	isToday,
	parseISO,
} from 'date-fns';
import { de } from 'date-fns/locale';

const CalendarDesktopView = ({ entries }) => {
	const [currentDate, setCurrentDate] = useState(new Date());

	const firstDayOfMonth = startOfMonth(currentDate);
	const lastDayOfMonth = endOfMonth(currentDate);
	const firstDayOfGrid = startOfWeek(firstDayOfMonth, { locale: de });
	const lastDayOfGrid = endOfWeek(lastDayOfMonth, { locale: de });
	const daysInGrid = eachDayOfInterval({ start: firstDayOfGrid, end: lastDayOfGrid });

	const eventsByDate = useMemo(() => {
		const grouped = {};
		entries.forEach(entry => {
			const dateKey = format(parseISO(entry.start), 'yyyy-MM-dd');
			if (!grouped[dateKey]) {
				grouped[dateKey] = [];
			}
			grouped[dateKey].push(entry);
		});
		return grouped;
	}, [entries]);

	const handlePrevMonth = () => setCurrentDate(subMonths(currentDate, 1));
	const handleNextMonth = () => setCurrentDate(addMonths(currentDate, 1));
	const handleToday = () => setCurrentDate(new Date());

	return (
		<div>
			<div className="calendar-controls">
				<div>
					<button onClick={handlePrevMonth} className="btn btn-secondary">{'<'}</button>
					<button onClick={handleNextMonth} className="btn btn-secondary" style={{ marginLeft: '0.5rem' }}>{'>'}</button>
					<button onClick={handleToday} className="btn btn-secondary" style={{ marginLeft: '0.5rem' }}>Heute</button>
				</div>
				<h2>{format(currentDate, 'MMMM yyyy', { locale: de })}</h2>
			</div>
			<div className="calendar-grid">
				{['So', 'Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa'].map(day => (
					<div key={day} className="calendar-header">{day}</div>
				))}
				{daysInGrid.map(day => {
					const dateKey = format(day, 'yyyy-MM-dd');
					const dayEvents = eventsByDate[dateKey] || [];
					return (
						<div
							key={dateKey}
							className={`calendar-day ${!isSameMonth(day, currentDate) ? 'other-month' : ''} ${isToday(day) ? 'today' : ''}`}
						>
							<div className="day-number">{format(day, 'd')}</div>
							{dayEvents.map(event => (
								<Link
									key={`${event.type}-${event.id}`}
									to={event.url}
									className={event.type === 'Event' ? 'calendar-event' : 'calendar-meeting'}
								>
									{event.title}
								</Link>
							))}
						</div>
					);
				})}
			</div>
		</div>
	);
};

export default CalendarDesktopView;