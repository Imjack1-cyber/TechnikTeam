import React from 'react';
import { ScrollView, StyleSheet, Platform } from 'react-native';

const ScrollableContent = ({ children, style, contentContainerStyle, ...props }) => {
    return (
        <ScrollView
            style={[styles.container, style]}
            contentContainerStyle={contentContainerStyle}
            {...props}
        >
            {children}
        </ScrollView>
    );
};

const styles = StyleSheet.create({
    container: {
        flex: 1,
        ...(Platform.OS === 'web' ? { overflowY: 'auto' } : {}),
    },
});

export default ScrollableContent;