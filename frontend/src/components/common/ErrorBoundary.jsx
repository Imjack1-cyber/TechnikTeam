import React from 'react';
import { View } from 'react-native';
import ErrorPage from '../../pages/error/ErrorPage';
import { navigationRef } from '../../router/navigation';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null, errorLocation: 'Unknown Screen', timestamp: null };
    }

    static getDerivedStateFromError(error) {
        // Update state so the next render will show the fallback UI.
        return { hasError: true, error: error };
    }

    componentDidCatch(error, errorInfo) {
        // You can also log the error to an error reporting service
        console.error("Uncaught error in ErrorBoundary:", error, errorInfo);
        const routeName = navigationRef.current?.getCurrentRoute()?.name;
        this.setState({ errorLocation: routeName || 'Unknown Screen', timestamp: new Date() });
    }

    render() {
        if (this.state.hasError) {
            // Render the ErrorPage directly. It is responsible for its own full-screen layout.
            return (
                <ErrorPage 
                    error={this.state.error} 
                    location={this.state.errorLocation} 
                    timestamp={this.state.timestamp} 
                />
            );
        }

        return this.props.children;
    }
}

export default ErrorBoundary;