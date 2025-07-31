import React from 'react';

const DynamicSkillRows = ({ rows, setRows, courses }) => {
	const handleRowChange = (index, field, value) => {
		const newRows = [...rows];
		newRows[index][field] = value;
		setRows(newRows);
	};

	const handleAddRow = () => {
		setRows([...rows, { requiredCourseId: '', requiredPersons: 1 }]);
	};

	const handleRemoveRow = (index) => {
		setRows(rows.filter((_, i) => i !== index));
	};

	return (
		<div>
			{rows.map((row, index) => (
				<div className="dynamic-row" key={index}>
					<select
						name="requiredCourseIds"
						value={row.requiredCourseId}
						onChange={(e) => handleRowChange(index, 'requiredCourseId', e.target.value)}
						className="form-group"
					>
						<option value="">-- Qualifikation auswählen --</option>
						{courses.map(course => <option key={course.id} value={course.id}>{course.name}</option>)}
					</select>
					<input
						type="number"
						name="requiredPersons"
						value={row.requiredPersons}
						onChange={(e) => handleRowChange(index, 'requiredPersons', e.target.value)}
						className="form-group"
						style={{ maxWidth: '100px' }}
						min="1"
					/>
					<button type="button" className="btn btn-small btn-danger" onClick={() => handleRemoveRow(index)} title="Zeile entfernen">×</button>
				</div>
			))}
			<button type="button" className="btn btn-small" onClick={handleAddRow}>
				<i className="fas fa-plus"></i> Anforderung hinzufügen
			</button>
		</div>
	);
};

export default DynamicSkillRows;