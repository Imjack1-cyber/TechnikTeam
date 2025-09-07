import React from 'react';
import { View, Text, ScrollView, TouchableOpacity, StyleSheet } from 'react-native';
import ErrorPage from '../../pages/error/ErrorPage';
import { navigationRef } from '../../router/navigation';

class ErrorBoundary extends React.Component {
    constructor(props) {
        super(props);
        this.state = { hasError: false, error: null, errorLocation: 'Unknown Screen' };
    }

    static getDerivedStateFromError(error) {
        // Update state so the next render will show the fallback UI.
        return { hasError: true, error: error };
    }

    componentDidCatch(error, errorInfo) {
        // You can also log the error to an error reporting service
        console.error("Uncaught error in ErrorBoundary:", error, errorInfo);
        const routeName = navigationRef.current?.getCurrentRoute()?.name;
        this.setState({ errorLocation: routeName || 'Unknown Screen' });
    }

    render() {
        if (this.state.hasError) {
            // You can render any custom fallback UI
            return (
                <View style={styles.container}>
                    <ErrorPage error={this.state.error} location={this.state.errorLocation} />
                </View>
            );
        }

        return this.props.children;
    }
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        justifyContent: 'center',
        alignItems: 'center',
        padding: 16,
        backgroundColor: '#f8f9fa'
    }
});

export default ErrorBoundary;