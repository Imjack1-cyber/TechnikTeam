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
	const [navOrder, setNavOrder] = useState([]); // Will store URLs
	const [showHelpButton, setShowHelpButton] = useState(layout.showHelpButton !== false);
	const [dashboardWidgets, setDashboardWidgets] = useState({ ...defaultWidgets, ...layout.dashboardWidgets });
	const [selectedItemUrl, setSelectedItemUrl] = useState(null); // Changed from selectedItemLabel
	const { addToast } = useToast();

	useEffect(() => {
		const userNavOrder = layout.navOrder || []; // This is now an array of URLs
		const defaultOrder = navigationItems.map(item => item.url);

		const combinedOrder = [
			...userNavOrder,
			...defaultOrder.filter(url => !userNavOrder.includes(url))
		];

		setNavOrder(combinedOrder);
		setSidebarPosition(layout.sidebarPosition || 'left');
		setShowHelpButton(layout.showHelpButton !== false);
		setDashboardWidgets({ ...defaultWidgets, ...layout.dashboardWidgets });
	}, [layout, navigationItems]);

	const { userItems, adminItems, isUserBlockFirst } = useMemo(() => {
		const itemMap = new Map(navigationItems.map(item => [item.url, item]));
		const orderedItems = navOrder.map(url => itemMap.get(url)).filter(Boolean);

		const userItems = orderedItems.filter(item => item.requiredPermission === null);
		const adminItems = isAdmin ? orderedItems.filter(item => item.requiredPermission !== null) : [];

		const firstUserUrl = userItems.length > 0 ? userItems[0].url : null;
		const firstAdminUrl = adminItems.length > 0 ? adminItems[0].url : null;

		let isUserFirst = true;
		if (firstUserUrl && firstAdminUrl) {
			isUserFirst = navOrder.indexOf(firstUserUrl) < navOrder.indexOf(firstAdminUrl);
		} else if (!firstUserUrl && firstAdminUrl) {
			isUserFirst = false;
		}

		return { userItems, adminItems, isUserBlockFirst: isUserFirst };
	}, [navOrder, navigationItems, isAdmin]);

	const handleSave = () => {
		const finalNavOrder = isUserBlockFirst
			? [...userItems.map(i => i.url), ...adminItems.map(i => i.url)]
			: [...adminItems.map(i => i.url), ...userItems.map(i => i.url)];

		const newLayout = {
			sidebarPosition,
			navOrder: finalNavOrder,
			showHelpButton,
			dashboardWidgets
		};
		setLayout(newLayout);
		addToast('Layout-Einstellungen gespeichert!', 'success');
		setSelectedItemUrl(null);
	};

	const handleReset = () => {
		if (window.confirm('Möchten Sie alle Layout- und Navigationseinstellungen auf den Standard zurücksetzen?')) {
			const defaultNavOrderUrls = navigationItems.map(item => item.url);
			setSidebarPosition('left');
			setNavOrder(defaultNavOrderUrls);
			setShowHelpButton(true);
			setDashboardWidgets(defaultWidgets);
			setLayout({
				sidebarPosition: 'left',
				navOrder: [],
				showHelpButton: true,
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

	const moveItem = (direction, itemUrl) => {
		if (!itemUrl) return;
		const currentIndex = navOrder.indexOf(itemUrl);
		if (currentIndex === -1) return;

		const newIndex = currentIndex + direction;
		if (newIndex < 0 || newIndex >= navOrder.length) return;

		const newNavOrder = [...navOrder];
		const itemToMove = newNavOrder.splice(currentIndex, 1)[0];
		newNavOrder.splice(newIndex, 0, itemToMove);
		setNavOrder(newNavOrder);
		setSelectedItemUrl(itemUrl);
	};

	const moveBlock = (direction) => {
		if (userItems.length === 0 || adminItems.length === 0) return;
		if ((isUserBlockFirst && direction === -1) || (!isUserBlockFirst && direction === 1)) return;

		const userUrls = userItems.map(i => i.url);
		const adminUrls = adminItems.map(i => i.url);
		setNavOrder(isUserBlockFirst ? [...adminUrls, ...userUrls] : [...userUrls, ...adminUrls]);
	};

	useEffect(() => {
		const handleKeyDown = (e) => {
			if (!selectedItemUrl) return;
			const isBlockSelection = selectedItemUrl === "Benutzerbereich" || selectedItemUrl === "Admin-Bereich";

			if (e.key === 'ArrowUp') {
				e.preventDefault();
				if (isBlockSelection) moveBlock(-1);
				else moveItem(-1, selectedItemUrl);
			} else if (e.key === 'ArrowDown') {
				e.preventDefault();
				if (isBlockSelection) moveBlock(1);
				else moveItem(1, selectedItemUrl);
			} else if (e.key === 'Escape') {
				setSelectedItemUrl(null);
			}
		};
		window.addEventListener('keydown', handleKeyDown);
		return () => window.removeEventListener('keydown', handleKeyDown);
	}, [selectedItemUrl, navOrder, userItems, adminItems, isUserBlockFirst]);

	const renderNavBlock = (title, items, isBlockSelected, onBlockMove, type) => (
		<div className={`nav-order-block ${isBlockSelected ? 'selected' : ''}`}>
			<div className="nav-order-block-header" onClick={() => setSelectedItemUrl(isBlockSelected ? null : title)}>
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
					const isSelected = selectedItemUrl === item.url;
					return (
						<div key={item.url} className={`nav-order-item ${isSelected ? 'selected' : ''}`} onClick={() => setSelectedItemUrl(isSelected ? null : item.url)}>
							<span>{item.label} <span className="text-muted">({type})</span></span>
							{isSelected && (
								<div className="nav-order-controls">
									<span className="desktop-only-inline">Pfeiltasten zum Verschieben nutzen</span>
									<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); moveItem(-1, item.url); }}><i className="fas fa-arrow-up"></i></button>
									<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); moveItem(1, item.url); }}><i className="fas fa-arrow-down"></i></button>
								</div>
							)}
						</div>
					);
				})}
			</div>
		</div>
	);

	const userBlock = renderNavBlock("Benutzerbereich", userItems, selectedItemUrl === "Benutzerbereich", moveBlock, "Benutzer");
	const adminBlock = isAdmin && adminItems.length > 0 ? renderNavBlock("Admin-Bereich", adminItems, selectedItemUrl === "Admin-Bereich", moveBlock, "Admin") : null;

	return (
		<div>
			<h1><i className="fas fa-cog"></i> Layout-Einstellungen</h1>
			<p>Passe das Erscheinungsbild der Benutzeroberfläche an deine Vorlieben an.</p>

			<div className="card">
				<h2 className="card-title">Allgemeine UI-Optionen</h2>
				<div className="form-group">
					<label>
						<input type="checkbox" checked={showHelpButton} onChange={e => setShowHelpButton(e.target.checked)} />
						Kontextbezogenen Hilfe-Button anzeigen
					</label>
				</div>
			</div>

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