import React from 'react';
import { Link } from 'react-router-dom';
import { useToast } from '../../context/ToastContext';

const Toast = ({ message, type, url, onHide }) => {
	const [visible, setVisible] = React.useState(false);

	React.useEffect(() => {
		setVisible(true);
		const timer = setTimeout(() => {
			setVisible(false);
			// Give time for fade out animation before removing from DOM
			setTimeout(onHide, 400);
		}, 4600);
		return () => clearTimeout(timer);
	}, [onHide]);


	const getTypeClass = () => {
		switch (type) {
			case 'success':
				return 'toast-success';
			case 'error':
				return 'toast-danger';
			default:
				return 'toast-info';
		}
	};

	const toastContent = (
		<div className={`toast ${visible ? 'show' : ''} ${getTypeClass()} ${url ? 'clickable' : ''}`}>
			{message}
			{url && <i className="fas fa-arrow-right" style={{ marginLeft: 'auto', paddingLeft: '1rem' }}></i>}
		</div>
	);

	if (url) {
		return <Link to={url} style={{ textDecoration: 'none' }}>{toastContent}</Link>
	}

	return toastContent;
};


const ToastContainer = () => {
	const { toasts } = useToast();

	const handleHide = (id) => {
		// The timeout in ToastProvider already handles removal.
		// This component just renders what's in the state.
	};

	return (
		<div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
			{toasts.map(toast => (
				<Toast key={toast.id} message={toast.message} type={toast.type} url={toast.url} onHide={() => handleHide(toast.id)} />
			))}
		</div>
	);
};

export default ToastContainer;