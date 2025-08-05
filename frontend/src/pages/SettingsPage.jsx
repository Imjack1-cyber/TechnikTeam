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

	const [sidebarPosition, setSidebarPosition] = useState(layout.sidebarPosition);
	const [navOrder, setNavOrder] = useState([]);
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
	}, [layout, navigationItems]);

	const { userItems, adminItems } = useMemo(() => {
		const itemMap = new Map(navigationItems.map(item => [item.label, item]));
		const orderedItems = navOrder.map(label => itemMap.get(label)).filter(Boolean);
		const userItems = orderedItems.filter(item => item.requiredPermission === null);
		const adminItems = isAdmin ? orderedItems.filter(item => item.requiredPermission !== null) : [];
		return { userItems, adminItems };
	}, [navOrder, navigationItems, isAdmin]);

	const handleSave = () => {
		const newLayout = { sidebarPosition, navOrder };
		setLayout(newLayout);
		addToast('Layout-Einstellungen gespeichert!', 'success');
		setSelectedItemLabel(null);
	};

	const moveItem = (direction) => {
		if (!selectedItemLabel) return;

		const currentIndex = navOrder.indexOf(selectedItemLabel);
		if (currentIndex === -1) return;

		const newIndex = currentIndex + direction;
		if (newIndex < 0 || newIndex >= navOrder.length) return;

		const newNavOrder = [...navOrder];
		const itemToMove = newNavOrder.splice(currentIndex, 1)[0];
		newNavOrder.splice(newIndex, 0, itemToMove);
		setNavOrder(newNavOrder);
	};

	const moveBlock = (direction) => {
		const isUserBlockFirst = navOrder.indexOf(userItems[0]?.label) < navOrder.indexOf(adminItems[0]?.label);
		if ((isUserBlockFirst && direction === -1) || (!isUserBlockFirst && direction === 1)) {
			// Can't move user block up if it's already first, or down if it's already last
			return;
		}

		const userLabels = userItems.map(i => i.label);
		const adminLabels = adminItems.map(i => i.label);
		setNavOrder(isUserBlockFirst ? [...adminLabels, ...userLabels] : [...userLabels, ...adminLabels]);
	};

	// Keyboard controls for desktop
	useEffect(() => {
		const handleKeyDown = (e) => {
			if (!selectedItemLabel) return;
			if (e.key === 'ArrowUp') {
				e.preventDefault();
				moveItem(-1);
			} else if (e.key === 'ArrowDown') {
				e.preventDefault();
				moveItem(1);
			} else if (e.key === 'Escape') {
				setSelectedItemLabel(null);
			}
		};
		window.addEventListener('keydown', handleKeyDown);
		return () => window.removeEventListener('keydown', handleKeyDown);
	}, [selectedItemLabel, navOrder]);

	const renderNavBlock = (title, items, isBlockSelected, onBlockMove) => (
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
							{item.label}
							{isSelected && (
								<div className="nav-order-controls">
									<span className="desktop-only-inline">Pfeiltasten zum Verschieben nutzen</span>
									<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); moveItem(-1); }}><i className="fas fa-arrow-up"></i></button>
									<button className="mobile-only btn btn-small" onClick={(e) => { e.stopPropagation(); moveItem(1); }}><i className="fas fa-arrow-down"></i></button>
								</div>
							)}
						</div>
					);
				})}
			</div>
		</div>
	);

	const isUserBlockSelected = selectedItemLabel === "Benutzerbereich";
	const isAdminBlockSelected = selectedItemLabel === "Admin-Bereich";
	const isUserBlockFirst = navOrder.indexOf(userItems[0]?.label) < navOrder.indexOf(adminItems[0]?.label);

	const userBlock = renderNavBlock("Benutzerbereich", userItems, isUserBlockSelected, moveBlock);
	const adminBlock = isAdmin && adminItems.length > 0 ? renderNavBlock("Admin-Bereich", adminItems, isAdminBlockSelected, moveBlock) : null;

	return (
		<div>
			<h1><i className="fas fa-cog"></i> Layout-Einstellungen</h1>
			<p>Passe das Erscheinungsbild der Benutzeroberfläche an deine Vorlieben an.</p>

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

			<button className="btn btn-success" onClick={handleSave} style={{ marginTop: '1.5rem' }}>
				<i className="fas fa-save"></i> Einstellungen speichern
			</button>
		</div>
	);
};

export default SettingsPage;