import React from 'react';
import { useToast } from '../../context/ToastContext';

const Toast = ({ message, type, onHide }) => {
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

	return (
		<div className={`toast ${visible ? 'show' : ''} ${getTypeClass()}`}>
			{message}
		</div>
	);
};


const ToastContainer = () => {
	const { toasts, addToast } = useToast(); // addToast isn't used here, but context provides it

	const handleHide = (id) => {
		// The timeout in ToastProvider already handles removal.
		// This component just renders what's in the state.
	};

	return (
		<div style={{ position: 'fixed', bottom: '20px', right: '20px', zIndex: 9999, display: 'flex', flexDirection: 'column', gap: '0.5rem' }}>
			{toasts.map(toast => (
				<Toast key={toast.id} message={toast.message} type={toast.type} onHide={() => handleHide(toast.id)} />
			))}
		</div>
	);
};

export default ToastContainer;