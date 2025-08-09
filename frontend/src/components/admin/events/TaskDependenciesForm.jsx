import React from 'react';

const TaskDependenciesForm = ({ allTasks, selectedDependencies, onDependencyChange }) => {

	const handleToggle = (taskId) => {
		const newSelection = new Set(selectedDependencies);
		if (newSelection.has(taskId)) {
			newSelection.delete(taskId);
		} else {
			newSelection.add(taskId);
		}
		onDependencyChange(newSelection);
	};

	if (!allTasks || allTasks.length === 0) {
		return <p className="text-muted">Keine anderen Aufgaben vorhanden, von denen diese abhängen könnte.</p>;
	}

	return (
		<div className="form-group">
			<label>Abhängig von (Tasks, die vorher erledigt sein müssen):</label>
			<div style={{ maxHeight: '150px', overflowY: 'auto', border: '1px solid var(--border-color)', padding: '0.5rem', borderRadius: 'var(--border-radius)' }}>
				{allTasks.map(task => (
					<label key={task.id} style={{ display: 'flex', alignItems: 'center', gap: '0.5rem', marginBottom: '0.25rem' }}>
						<input
							type="checkbox"
							checked={selectedDependencies.has(task.id)}
							onChange={() => handleToggle(task.id)}
						/>
						{task.description}
					</label>
				))}
			</div>
		</div>
	);
};

export default TaskDependenciesForm;