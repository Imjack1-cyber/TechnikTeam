import React from 'react';
import { Outlet } from 'react-router-dom';

/**
 * A minimal layout component for pages that should not have the main sidebar and header,
 * such as printable views or QR code landing pages.
 */
const MinimalLayout = () => {
	return (
		<div className="main-content" style={{ maxWidth: '800px', margin: '2rem auto' }}>
			<Outlet />
		</div>
	);
};

export default MinimalLayout;