import React, { createContext, useState, useCallback, useContext } from 'react';

const ToastContext = createContext(null);

export const useToast = () => {
	const context = useContext(ToastContext);
	if (!context) {
		throw new Error('useToast must be used within a ToastProvider');
	}
	return context;
};

export const ToastProvider = ({ children }) => {
	const [toasts, setToasts] = useState([]);

	const addToast = useCallback((message, type = 'info') => {
		const id = Date.now() + Math.random();
		setToasts(prevToasts => [...prevToasts, { id, message, type }]);
		setTimeout(() => {
			setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
		}, 5000);
	}, []);

	const value = { toasts, addToast };

	return (
		<ToastContext.Provider value={value}>
			{children}
		</ToastContext.Provider>
	);
};