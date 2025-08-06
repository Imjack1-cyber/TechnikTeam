import React, { useState, useEffect, useMemo } from 'react';
import { useAuthStore } from '../store/authStore';
import { useToast } from '../context/ToastContext';

const SettingsPage = () => {
	const { layout, setLayout, navigationItems, isAdmin } = useAuthStore(state => ({
		layout: state.layout,
		setLayout: state.setLayout,
		navigationItems: state.navigationItems,
		isAdmin: state.isAdmin
	}));

	const defaultWidgets = {
		recommendedEvents: true,
		assignedEvents: true,
		openTasks: true,
		upcomingEvents: true,
		recentConversations: true,
		upcomingMeetings: true,
		lowStockItems: false,
	};

	const [sidebarPosition, setSidebarPosition] = useState(layout.sidebarPosition);
	const [navOrder, setNavOrder] = useState([]);
	const [dashboardWidgets, setDashboardWidgets] = useState({ ...defaultWidgets, ...layout.dashboardWidgets });
	const [selectedItemLabel, setSelectedItemLabel] = useState(null);
	const { addToast } = useToast();

	useEffect(() => {
		const userNavOrder = layout.navOrder || [];
		const defaultOrder = navigationItems.map(item => item.label);

		const combinedOrder = [
			...userNavOrder,
			...defaultOrder.filter(item => !userNavOrder.includes(item))
		];

		setNavOrder(combinedOrder);
		setSidebarPosition(layout.sidebarPosition || 'left');
		setDashboardWidgets({ ...defaultWidgets, ...layout.dashboardWidgets });
	}, [layout, navigationItems]);

	const { userItems, adminItems, isUserBlockFirst } = useMemo(() => {
		const itemMap = new Map(navigationItems.map(item => [item.label, item]));
		const allLabelsInOrder = navOrder.map(label => itemMap.get(label)).filter(Boolean).map(item => item.label);

		const defaultUserLabels = navigationItems.filter(i => i.requiredPermission === null).map(i => i.label);
		const defaultAdminLabels = isAdmin ? navigationItems.filter(i => i.requiredPermission !== null).map(i => i.label) : [];

		const userLabels = allLabelsInOrder.filter(label => defaultUserLabels.includes(label));
		const adminLabels = allLabelsInOrder.filter(label => defaultAdminLabels.includes(label));

		const isUserFirst = (layout.navOrder && layout.navOrder.length > 0)
			? defaultUserLabels.includes(layout.navOrder[0])
			: true;

		const sortedUserItems = userLabels.map(label => itemMap.get(label));
		const sortedAdminItems = adminLabels.map(label => itemMap.get(label));

		return {
			userItems: sortedUserItems,
			adminItems: sortedAdminItems,
			isUserBlockFirst: isUserFirst,
		};
	}, [navOrder, navigationItems, isAdmin, layout.navOrder]);

	const handleSave = () => {
		const newLayout = {
			sidebarPosition,
			navOrder: isUserBlockFirst ? [...userItems.map(i => i.label), ...adminItems.map(i => i.label)] : [...adminItems.map(i => i.label), ...userItems.map(i => i.label)],
			dashboardWidgets
		};
		setLayout(newLayout);
		addToast('Layout-Einstellungen gespeichert!', 'success');
		setSelectedItemLabel(null);
	};

	const handleReset = () => {
		if (window.confirm('Möchten Sie alle Layout- und Navigationseinstellungen auf den Standard zurücksetzen?')) {
			const defaultNavOrder = navigationItems.map(item => item.label);
			setSidebarPosition('left');
			setNavOrder(defaultNavOrder);
			setDashboardWidgets(defaultWidgets);
			setLayout({
				sidebarPosition: 'left',
				navOrder: [], // Empty array signifies default order
				dashboardWidgets: defaultWidgets,
			});
			addToast('Einstellungen wurden auf den Standard zurückgesetzt.', 'success');
		}
	};

	const handleWidgetToggle = (widgetKey) => {
		setDashboardWidgets(prev => ({
			...prev,
			[widgetKey]: !prev[widgetKey]
		}));
	};

	const moveItem = (direction, itemLabel, isUserItem) => {
		const list = isUserItem ? userItems.map(i => i.label) : adminItems.map(i => i.label);
		const currentIndex = list.indexOf(itemLabel);

		const newIndex = currentIndex + direction;
		if (newIndex < 0 || newIndex >= list.length) return;

		const newList = [...list];
		const itemToMove = newList.splice(currentIndex, 1)[0];
		newList.splice(newIndex, 0, itemToMove);

		const userLabels = isUserItem ? newList : userItems.map(i => i.label);
		const adminLabels = !isUserItem ? newList : adminItems.map(i => i.label);

		setNavOrder(isUserBlockFirst ? [...userLabels, ...adminLabels] : [...adminLabels, ...userLabels]);
	};

	const moveBlock = (direction) => {
		if ((isUserBlockFirst && direction === -1) || (!isUserBlockFirst && direction === 1)) {
			return;
		}
		const userLabels = userItems.map(i => i.label);
		const adminLabels = adminItems.map(i => i.label);
		setNavOrder(isUserBlockFirst ? [...adminLabels, ...userLabels] : [...userLabels, ...adminLabels]);
	};

	useEffect(() => {
		const handleKeyDown = (e) => {
			if (!selectedItemLabel) return;
			const isUserItem = userItems.some(i => i.label === selectedItemLabel);

			if (e.key === 'ArrowUp') {
				e.preventDefault();
				moveItem(-1, selectedItemLabel, isUserItem);
			} else if (e.key === 'ArrowDown') {
				e.preventDefault();
				moveItem(1, selectedItemLabel, isUserItem);
			} else if (e.key === 'Escape') {
				setSelectedItemLabel(null);
			}
		};
		window.addEventListener('keydown', handleKeyDown);
		return () => window.removeEventListener('keydown', handleKeyDown);
	}, [selectedItemLabel, navOrder, userItems, adminItems, isUserBlockFirst]);

	const renderNavBlock = (title, items, isBlockSelected, onBlockMove, type) => (
		<div className={`nav-order-block ${isBlockSelected ? 'selected' : ''}`}>
			<div className="nav-order-block-header" onClick={() => setSelectedItemLabel(title)}>
				<h3 className="nav-section-title">{title}</h3>
				<div className="nav-order-controls">
					{isBlockSelected && (
						<>
							<span className="desktop-only-inline">Pfeiltasten zum Verschieben nutzen</span>
							<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); onBlockMove(-1); }}><i className="fas fa-arrow-up"></i></button>
							<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); onBlockMove(1); }}><i className="fas fa-arrow-down"></i></button>
						</>
					)}
					<i className="fas fa-arrows-alt-v"></i>
				</div>
			</div>
			<div className="nav-order-list">
				{items.map(item => {
					const isSelected = selectedItemLabel === item.label;
					return (
						<div key={item.label} className={`nav-order-item ${isSelected ? 'selected' : ''}`} onClick={() => setSelectedItemLabel(isSelected ? null : item.label)}>
							<span>{item.label} <span className="text-muted">({type})</span></span>
							{isSelected && (
								<div className="nav-order-controls">
									<span className="desktop-only-inline">Pfeiltasten zum Verschieben nutzen</span>
									<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); moveItem(-1, item.label, type === 'Benutzer'); }}><i className="fas fa-arrow-up"></i></button>
									<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); moveItem(1, item.label, type === 'Benutzer'); }}><i className="fas fa-arrow-down"></i></button>
								</div>
							)}
						</div>
					);
				})}
			</div>
		</div>
	);

	const userBlock = renderNavBlock("Benutzerbereich", userItems, selectedItemLabel === "Benutzerbereich", moveBlock, "Benutzer");
	const adminBlock = isAdmin && adminItems.length > 0 ? renderNavBlock("Admin-Bereich", adminItems, selectedItemLabel === "Admin-Bereich", moveBlock, "Admin") : null;

	return (
		<div>
			<h1><i className="fas fa-cog"></i> Layout-Einstellungen</h1>
			<p>Passe das Erscheinungsbild der Benutzeroberfläche an deine Vorlieben an.</p>

			<div className="card">
				<h2 className="card-title">Dashboard Widgets</h2>
				<p>Wähle aus, welche Widgets auf deinem Dashboard angezeigt werden sollen.</p>
				<div className="responsive-dashboard-grid">
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.recommendedEvents} onChange={() => handleWidgetToggle('recommendedEvents')} /> Empfohlene Events</label>
					</div>
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.assignedEvents} onChange={() => handleWidgetToggle('assignedEvents')} /> Meine nächsten Einsätze</label>
					</div>
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.openTasks} onChange={() => handleWidgetToggle('openTasks')} /> Meine offenen Aufgaben</label>
					</div>
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.upcomingEvents} onChange={() => handleWidgetToggle('upcomingEvents')} /> Weitere anstehende Veranstaltungen</label>
					</div>
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.recentConversations} onChange={() => handleWidgetToggle('recentConversations')} /> Letzte Gespräche</label>
					</div>
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.upcomingMeetings} onChange={() => handleWidgetToggle('upcomingMeetings')} /> Meine nächsten Lehrgänge</label>
					</div>
					<div className="form-group">
						<label><input type="checkbox" checked={dashboardWidgets.lowStockItems} onChange={() => handleWidgetToggle('lowStockItems')} /> Niedriger Lagerbestand</label>
					</div>
				</div>
			</div>

			<div className="card">
				<h2 className="card-title">Seitenleisten-Position</h2>
				<div className="form-group">
					<select value={sidebarPosition} onChange={e => setSidebarPosition(e.target.value)}>
						<option value="left">Links</option>
						<option value="right">Rechts</option>
						<option value="top">Oben</option>
						<option value="bottom">Unten</option>
					</select>
				</div>
			</div>

			<div className="card">
				<h2 className="card-title">Reihenfolge der Navigation</h2>
				<p>Klicke ein Element oder einen Block an, um es/ihn auszuwählen. Nutze dann die Pfeiltasten (Desktop) oder die angezeigten Buttons (Mobil), um die Reihenfolge zu ändern.</p>
				{isUserBlockFirst ? <>{userBlock}{adminBlock}</> : <>{adminBlock}{userBlock}</>}
			</div>

			<div style={{ marginTop: '1.5rem', display: 'flex', justifyContent: 'space-between' }}>
				<button className="btn btn-danger-outline" onClick={handleReset}>
					<i className="fas fa-undo"></i> Auf Standard zurücksetzen
				</button>
				<button className="btn btn-success" onClick={handleSave}>
					<i className="fas fa-save"></i> Einstellungen speichern
				</button>
			</div>
		</div>
	);
};

export default SettingsPage;