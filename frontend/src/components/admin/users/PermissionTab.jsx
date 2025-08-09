import React from 'react';

const PermissionsTab = ({ groupedPermissions, assignedIds, onPermissionChange, isLoading }) => {
	if (isLoading) {
		return <p>Lade Berechtigungen...</p>;
	}

	return (
		<div>
			<h4>Individuelle Berechtigungen</h4>
			<p>Diese Berechtigungen gelten zusätzlich zu denen, die eine Rolle evtl. standardmäßig hat.</p>
			<div style={{ maxHeight: '40vh', overflowY: 'auto', padding: '0.5rem', border: '1px solid var(--border-color)', borderRadius: 'var(--border-radius)' }}>
				{Object.entries(groupedPermissions).map(([groupName, permissionsInGroup]) => (
					<details key={groupName} open>
						<summary style={{ fontWeight: 'bold', cursor: 'pointer', padding: '0.5rem 0' }}>
							{groupName}
						</summary>
						<div style={{ paddingLeft: '1rem' }}>
							{permissionsInGroup.map(p => (
								<label key={p.id} style={{ display: 'flex', alignItems: 'flex-start', marginBottom: '0.5rem' }}>
									<input
										type="checkbox"
										value={p.id}
										checked={assignedIds.has(p.id)}
										onChange={() => onPermissionChange(p.id)}
										style={{ marginTop: '5px', marginRight: '10px' }}
									/>
									<div>
										<strong>{p.permissionKey.replace(groupName + '_', '')}</strong>
										<small style={{ display: 'block', color: 'var(--text-muted-color)' }}>{p.description}</small>
									</div>
								</label>
							))}
						</div>
					</details>
				))}
			</div>
		</div>
	);
};

export default PermissionsTab;