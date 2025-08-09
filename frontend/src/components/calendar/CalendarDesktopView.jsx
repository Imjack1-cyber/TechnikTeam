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
	const firstDayOfGrid = startOfWeek(firstDayOfMonth, { weekStartsOn: 1 }); // Monday start
	const lastDayOfGrid = endOfWeek(lastDayOfMonth, { weekStartsOn: 1 });
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
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
				<h2 style={{ margin: 0, border: 'none' }}>{format(currentDate, 'MMMM yyyy', { locale: de })}</h2>
				<div>
					<button onClick={handlePrevMonth} className="btn btn-secondary">{'<'}</button>
					<button onClick={handleToday} className="btn btn-secondary" style={{ margin: '0 0.5rem' }}>Heute</button>
					<button onClick={handleNextMonth} className="btn btn-secondary">{'>'}</button>
				</div>
			</div>
			<div style={{ display: 'grid', gridTemplateColumns: 'repeat(7, 1fr)', gap: '1px', backgroundColor: 'var(--border-color)', border: '1px solid var(--border-color)' }}>
				{['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'].map(day => (
					<div key={day} style={{ textAlign: 'center', padding: '0.5rem', backgroundColor: 'var(--bg-color)', fontWeight: 'bold' }}>{day}</div>
				))}
				{daysInGrid.map(day => {
					const dateKey = format(day, 'yyyy-MM-dd');
					const dayEvents = eventsByDate[dateKey] || [];
					return (
						<div
							key={dateKey}
							style={{
								backgroundColor: 'var(--surface-color)',
								minHeight: '120px',
								padding: '0.5rem',
								opacity: isSameMonth(day, currentDate) ? 1 : 0.5,
								borderTop: isToday(day) ? '2px solid var(--primary-color)' : 'none'
							}}
						>
							<div style={{ fontWeight: isToday(day) ? 'bold' : 'normal' }}>{format(day, 'd')}</div>
							{dayEvents.map(event => (
								<Link
									key={`${event.type}-${event.id}`}
									to={event.url}
									style={{
										display: 'block',
										fontSize: '0.8rem',
										padding: '0.2rem 0.4rem',
										borderRadius: '4px',
										marginBottom: '0.25rem',
										whiteSpace: 'nowrap',
										overflow: 'hidden',
										textOverflow: 'ellipsis',
										backgroundColor: event.type === 'Event' ? 'var(--danger-color)' : 'var(--primary-color)',
										color: '#fff'
									}}
									title={event.title}
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