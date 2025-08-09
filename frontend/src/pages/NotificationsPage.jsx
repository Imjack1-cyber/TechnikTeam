import React, { useCallback, useEffect } from 'react';
import { Link } from 'react-router-dom';
import useApi from '../hooks/useApi';
import apiClient from '../services/apiClient';
import { useAuthStore } from '../store/authStore';

const NotificationItem = ({ notification }) => {
	const getIcon = (level) => {
		switch (level) {
			case 'Warning': return 'fa-exclamation-triangle';
			case 'Important': return 'fa-exclamation-circle';
			default: return 'fa-info-circle';
		}
	};

	const getIconColor = (level) => {
		switch (level) {
			case 'Warning': return 'var(--danger-color)';
			case 'Important': return 'var(--warning-color)';
			default: return 'var(--info-color)';
		}
	};

	const content = (
		<div style={{ display: 'flex', alignItems: 'flex-start', gap: '1rem', padding: '1rem', borderBottom: '1px solid var(--border-color)' }}>
			<i className={`fas ${getIcon(notification.level)}`} style={{ fontSize: '1.5rem', color: getIconColor(notification.level), marginTop: '0.25rem' }}></i>
			<div style={{ flex: 1 }}>
				<div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
					<h4 style={{ margin: 0 }}>{notification.title}</h4>
					<small style={{ color: 'var(--text-muted-color)' }}>{new Date(notification.createdAt).toLocaleString('de-DE')}</small>
				</div>
				<p style={{ margin: '0.25rem 0 0 0' }}>{notification.description}</p>
			</div>
		</div>
	);

	return notification.url ? <Link to={notification.url} style={{ textDecoration: 'none', color: 'inherit' }}>{content}</Link> : content;
};


const NotificationsPage = () => {
	const setUnseenCount = useAuthStore(state => state.setUnseenNotificationCount);
	const apiCall = useCallback(() => apiClient.get('/public/notifications'), []);
	const { data: notifications, loading, error, reload } = useApi(apiCall);

	useEffect(() => {
		const markAsSeen = async () => {
			try {
				await apiClient.post('/public/notifications/mark-all-seen');
				setUnseenCount(0);
			} catch (err) {
				console.error("Failed to mark notifications as seen", err);
			}
		};
		markAsSeen();
	}, [setUnseenCount]);

	const unseenNotifications = notifications?.filter(n => !n.seen) || [];
	const seenNotifications = notifications?.filter(n => n.seen) || [];

	// Custom sort for unseen notifications
	const levelOrder = { 'Warning': 1, 'Important': 2, 'Informational': 3 };
	unseenNotifications.sort((a, b) => {
		const levelCompare = (levelOrder[a.level] || 99) - (levelOrder[b.level] || 99);
		if (levelCompare !== 0) return levelCompare;
		return new Date(b.createdAt) - new Date(a.createdAt);
	});


	return (
		<div>
			<h1><i className="fas fa-bell"></i> Benachrichtigungen</h1>
			<p>Hier sehen Sie eine Übersicht aller an Sie gesendeten Benachrichtigungen.</p>

			{loading && <p>Lade Benachrichtigungen...</p>}
			{error && <p className="error-message">{error}</p>}

			<div className="card">
				<h2 className="card-title">Ungelesene Benachrichtigungen</h2>
				{unseenNotifications.length > 0 ? (
					unseenNotifications.map(n => <NotificationItem key={n.id} notification={n} />)
				) : (
					<p style={{ padding: '1rem' }}>Keine ungelesenen Benachrichtigungen.</p>
				)}
			</div>

			<div className="card" style={{ marginTop: '1.5rem' }}>
				<h2 className="card-title">Gelesene Benachrichtigungen</h2>
				{seenNotifications.length > 0 ? (
					seenNotifications.map(n => <NotificationItem key={n.id} notification={n} />)
				) : (
					<p style={{ padding: '1rem' }}>Keine gelesenen Benachrichtigungen.</p>
				)}
			</div>
		</div>
	);
};

export default NotificationsPage;