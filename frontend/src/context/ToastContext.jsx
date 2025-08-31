import React, { createContext, useState, useCallback, useContext } from 'react';

const ToastContext = createContext(null);

export const useToast = () => {
	const context = useContext(ToastContext);
	if (!context) {
		throw new Error('useToast muss innerhalb eines ToastProviders verwendet werden');
	}
	return context;
};

export const ToastProvider = ({ children }) => {
	const [toasts, setToasts] = useState([]);

	const addToast = useCallback((message, type = 'info', url = null) => {
		const id = Date.now() + Math.random();
		setToasts(prevToasts => [...prevToasts, { id, message, type, url }]);
	}, []);
    
    const removeToast = useCallback((id) => {
        setToasts(prevToasts => prevToasts.filter(toast => toast.id !== id));
    }, []);

	const value = { toasts, addToast, removeToast };

	return (
		<ToastContext.Provider value={value}>
			{children}
		</ToastContext.Provider>
	);
};