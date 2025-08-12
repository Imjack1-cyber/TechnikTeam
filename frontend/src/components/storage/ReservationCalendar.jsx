import React, { useState, useMemo } from 'react';
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
	isWithinInterval,
} from 'date-fns';
import { de } from 'date-fns/locale';
import { Link } from 'react-router-dom';

const ReservationCalendar = ({ reservations }) => {
	const [currentDate, setCurrentDate] = useState(new Date());

	const reservationIntervals = useMemo(() => {
		if (!reservations) return [];
		return reservations.map(res => ({
			start: parseISO(res.event_datetime),
			end: res.end_datetime ? parseISO(res.end_datetime) : parseISO(res.event_datetime),
			title: res.event_name,
			eventId: res.event_id,
		}));
	}, [reservations]);

	const getReservationsForDay = (day) => {
		return reservationIntervals.filter(interval =>
			isWithinInterval(day, { start: interval.start, end: interval.end })
		);
	};

	const firstDayOfMonth = startOfMonth(currentDate);
	const lastDayOfMonth = endOfMonth(currentDate);
	const firstDayOfGrid = startOfWeek(firstDayOfMonth, { weekStartsOn: 1 }); // Monday start
	const lastDayOfGrid = endOfWeek(lastDayOfMonth, { weekStartsOn: 1 });
	const daysInGrid = eachDayOfInterval({ start: firstDayOfGrid, end: lastDayOfGrid });

	const handlePrevMonth = () => setCurrentDate(subMonths(currentDate, 1));
	const handleNextMonth = () => setCurrentDate(addMonths(currentDate, 1));

	return (
		<div>
			<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '1rem' }}>
				<h4 style={{ margin: 0, border: 'none' }}>{format(currentDate, 'MMMM yyyy', { locale: de })}</h4>
				<div>
					<button onClick={handlePrevMonth} className="btn btn-secondary btn-small">{'<'}</button>
					<button onClick={handleNextMonth} className="btn btn-secondary btn-small" style={{ marginLeft: '0.5rem' }}>{'>'}</button>
				</div>
			</div>
			<div className="reservation-calendar-grid">
				{['Mo', 'Di', 'Mi', 'Do', 'Fr', 'Sa', 'So'].map(day => (
					<div key={day} className="reservation-calendar-header">{day}</div>
				))}
				{daysInGrid.map(day => {
					const dayReservations = getReservationsForDay(day);
					const isReserved = dayReservations.length > 0;
					const title = isReserved ? "Reserviert für: " + dayReservations.map(r => r.title).join(', ') : '';
					const eventId = isReserved ? dayReservations[0].eventId : null;

					const className = `reservation-calendar-day ${!isSameMonth(day, currentDate) ? 'other-month' : ''} ${isToday(day) ? 'today' : ''} ${isReserved ? 'reserved' : ''}`;

					const dayCellContent = (
						<span>{format(day, 'd')}</span>
					);

					if (isReserved && eventId) {
						return (
							<Link
								key={day.toString()}
								to={`/veranstaltungen/details/${eventId}`}
								className={className}
								title={title}
								style={{ textDecoration: 'none', color: 'inherit' }}
							>
								{dayCellContent}
							</Link>
						);
					} else {
						return (
							<div
								key={day.toString()}
								className={className}
								title={title}
							>
								{dayCellContent}
							</div>
						);
					}
				})}
			</div>
		</div>
	);
};

export default ReservationCalendar;