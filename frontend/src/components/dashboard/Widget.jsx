import React from 'react';
import { Link } from 'react-router-dom';

const Widget = ({ icon, title, children, linkTo, linkText }) => {
	return (
		<div className="card">
			<h2 className="card-title">
				<i className={`fas ${icon}`}></i> {title}
			</h2>
			{children}
			{linkTo && linkText && (
				<Link to={linkTo} className="btn btn-small" style={{ marginTop: '1rem' }}>
					{linkText}
				</Link>
			)}
		</div>
	);
};

export default Widget;