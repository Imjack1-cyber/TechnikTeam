import React from 'react';
import { Link } from 'react-router-dom';
import { format, parseISO } from 'date-fns';
import { de } from 'date-fns/locale';

const CalendarMobileView = ({ entries }) => {
	if (!entries || entries.length === 0) {
		return <div className="card"><p>Derzeit sind keine Termine geplant.</p></div>;
	}

	const sortedEntries = [...entries].sort((a, b) => parseISO(a.start) - parseISO(b.start));

	return (
		<ul className="termin-list">
			{sortedEntries.map(entry => (
				<li key={`${entry.type}-${entry.id}`}>
					<Link to={entry.url} className="termin-item-link">
						<div className="termin-item">
							<div className="termin-date">
								<span className="termin-date-day">{format(parseISO(entry.start), 'dd')}</span>
								<span className="termin-date-month">{format(parseISO(entry.start), 'MMM', { locale: de })}</span>
							</div>
							<div className="termin-details">
								<span className="termin-title">{entry.title}</span>
								<span className={`status-badge ${entry.type === 'Event' ? 'termin-type-event' : 'termin-type-lehrgang'}`}>
									{entry.type}
								</span>
							</div>
							<div className="termin-arrow">
								<i className="fas fa-chevron-right"></i>
							</div>
						</div>
					</Link>
				</li>
			))}
		</ul>
	);
};

export default CalendarMobileView;