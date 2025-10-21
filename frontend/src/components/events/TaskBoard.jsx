import React, { useState, useCallback, useMemo, useEffect } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, TextInput, ScrollView, useWindowDimensions } from 'react-native';
import useApi from '../../hooks/useApi';
import apiClient from '../../services/apiClient';
import TaskModal from '../admin/events/TaskModal';
import { useAuthStore } from '../../store/authStore';
import { getCommonStyles } from '../../styles/commonStyles';
import { getThemeColors, spacing, typography, borders, shadows } from '../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import BouncyCheckbox from "react-native-bouncy-checkbox";
import DraggableFlatList from 'react-native-draggable-flatlist';
import { useToast } from '../../context/ToastContext';
import AccordionSection from '../ui/AccordionSection';

const TaskCard = ({ item: task, onOpenModal, onAction, styles, colors, drag, isActive, user, isParticipant, canManageTasks, userHasActiveTask }) => {
    const needsHelp = task.status === 'IN_PROGRESS' && task.assignedUsers.length < task.requiredPersons;
    const getTaskCardStyle = () => {
        if (task.status === 'LOCKED') return styles.lockedTask;
        if (task.status === 'DONE') return styles.doneTask;
        if (task.isImportant || needsHelp) return styles.importantTask;
        if (task.status === 'IN_PROGRESS') return styles.inProgressTask;
        return {}; // Default for OPEN
    };
    const isDone = task.status === 'DONE';
    const isAssigned = task.assignedUsers.some(u => u.id === user.id);
    const isActionable = task.status === 'OPEN' || task.status === 'IN_PROGRESS';
    const canComplete = task.status === 'IN_PROGRESS' && (isAssigned || canManageTasks) && (task.assignedUsers.length >= task.requiredPersons);
    const canClaim = isActionable && (!userHasActiveTask || (needsHelp && !isAssigned));


    const renderDependencies = () => {
        if (task.status !== 'LOCKED' || !task.dependsOn || task.dependsOn.length === 0) {
            return null;
        }
        return (
            <Text style={styles.dependencyText}>
                Gesperrt, wartet auf: {task.dependsOn.map(t => `#${t.displayOrder}`).join(', ')}
            </Text>
        );
    };

    return (
        <TouchableOpacity 
            style={[styles.taskCard, getTaskCardStyle(), isActive && { transform: [{ scale: 1.05 }], ...shadows.lg }]} 
            onLongPress={drag} 
            onPress={() => onOpenModal('task', task)}
        >
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Text style={[styles.taskName, isDone && styles.doneTaskText]}>{task.name}</Text>
                <Text style={styles.displayOrder}>#{task.displayOrder}</Text>
            </View>
            {renderDependencies()}
             <View style={{flexDirection: 'row', justifyContent: 'flex-end', gap: 8, marginTop: 8}}>
                {canManageTasks && (
                    <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => onOpenModal('task', task)}>
                        <Text style={styles.buttonText}>Bearbeiten</Text>
                    </TouchableOpacity>
                )}
                {!isAssigned && isParticipant && isActionable && (
                    <TouchableOpacity style={[styles.button, styles.successButton, !canClaim && styles.disabledButton]} onPress={() => onAction(task.id, 'claim')} disabled={!canClaim}>
                        <Text style={styles.buttonText}>Mitmachen</Text>
                    </TouchableOpacity>
                )}
                {isAssigned && (
                    <TouchableOpacity style={[styles.button, styles.dangerButton]} onPress={() => onAction(task.id, 'unclaim')}>
                        <Text style={styles.buttonText}>Verlassen</Text>
                    </TouchableOpacity>
                )}
                {isParticipant && isActionable && (
                    <TouchableOpacity style={[styles.button, styles.primaryButton, !canComplete && styles.disabledButton]} onPress={() => onAction(task.id, 'updateStatus', 'DONE')} disabled={!canComplete}>
                        <Text style={styles.buttonText}>Abschließen</Text>
                    </TouchableOpacity>
                )}
            </View>
        </TouchableOpacity>
    );
};

const KanbanColumn = ({ category, onOpenModal, onAction, handleReorder, styles, colors, user, isParticipant, canManageTasks, userHasActiveTask }) => {
    const [tasks, setTasks] = useState(category.tasks);
    const [inlineTaskName, setInlineTaskName] = useState('');
    const [isAdding, setIsAdding] = useState(false);

    useEffect(() => {
        setTasks(category.tasks);
    }, [category.tasks]);

    const handleInlineSubmit = () => {
        if (inlineTaskName.trim()) {
            onAction(null, 'create', { categoryId: category.id, name: inlineTaskName.trim() });
            setInlineTaskName('');
        }
        setIsAdding(false);
    };

    return (
        <View style={styles.kanbanColumn}>
            <Text style={styles.columnTitle}>{category.name}</Text>
            <DraggableFlatList
                data={tasks}
                onDragEnd={({ data }) => {
                    setTasks(data); // Optimistic update
                    handleReorder(category.id, data.flat());
                }}
                keyExtractor={(row) => row.map(t => t.id).join('-')}
                renderItem={({ item: row, drag, isActive }) => (
                    <View style={styles.taskRow}>
                        {row.map(task => (
                            <View key={task.id} style={styles.taskCardWrapper}>
                                <TaskCard item={task} onOpenModal={onOpenModal} onAction={onAction} user={user} isParticipant={isParticipant} canManageTasks={canManageTasks} userHasActiveTask={userHasActiveTask} styles={styles} colors={colors} drag={canManageTasks ? drag : undefined} isActive={isActive} />
                            </View>
                        ))}
                    </View>
                )}
                containerStyle={{ flex: 1 }}
                enabled={canManageTasks}
            />
            {canManageTasks && (
                isAdding ? (
                     <View style={styles.inlineAddContainer}>
                        <TextInput
                            style={styles.inlineInput}
                            value={inlineTaskName}
                            onChangeText={setInlineTaskName}
                            placeholder="Neue Aufgabe..."
                            autoFocus
                            onSubmitEditing={handleInlineSubmit}
                            onBlur={() => setIsAdding(false)}
                        />
                        <TouchableOpacity onPress={handleInlineSubmit} style={styles.inlineButton}>
                            <Icon name="check" size={16} color={colors.success} />
                        </TouchableOpacity>
                    </View>
                ) : (
                    <TouchableOpacity onPress={() => setIsAdding(true)} style={styles.addTaskButton}>
                        <Icon name="plus" size={14} color={colors.textMuted} />
                        <Text style={{color: colors.textMuted}}> Neue Aufgabe</Text>
                    </TouchableOpacity>
                )
            )}
        </View>
    );
};

const TaskBoard = ({ event, user, categories, canManageTasks, isParticipant, onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    const { addToast } = useToast();
    const { width } = useWindowDimensions();
    const isLargeScreen = width >= 768;

    const [modalState, setModalState] = useState({ type: null, data: null });
    const [showDoneTasks, setShowDoneTasks] = useState(false);

    const userHasActiveTask = useMemo(() => {
        return event.eventTasks?.some(task => 
            task.status === 'IN_PROGRESS' && task.assignedUsers.some(u => u.id === user.id)
        );
    }, [event.eventTasks, user.id]);

    const openModal = (type, data = null) => setModalState({ type, data });

    const handleModalSuccess = () => {
        setModalState({ type: null, data: null });
        onUpdate();
    };

    const tasksByCategory = useMemo(() => {
        const byCategory = {};
        const allCategories = categories ? [...categories, {id: 0, name: 'Unkategorisiert'}] : [{id: 0, name: 'Unkategorisiert'}];
        allCategories.forEach(cat => byCategory[cat.id || 0] = { ...cat, tasks: [] });

        const filteredTasks = event.eventTasks?.filter(task => showDoneTasks || task.status !== 'DONE');
        filteredTasks?.forEach(task => {
            const categoryId = task.categoryId || 0;
            if (byCategory[categoryId]) byCategory[categoryId].tasks.push(task);
        });

        Object.values(byCategory).forEach(cat => {
            cat.tasks.sort((a, b) => a.displayOrder - b.displayOrder);
            
            // Group tasks by displayOrder for parallel rendering
            const grouped = [];
            let lastOrder = -999;
            cat.tasks.forEach(task => {
                if (task.displayOrder === lastOrder && grouped.length > 0) {
                    grouped[grouped.length - 1].push(task);
                } else {
                    grouped.push([task]);
                    lastOrder = task.displayOrder;
                }
            });
            cat.tasks = grouped;
        });

        return Object.values(byCategory).filter(cat => cat.tasks.length > 0 || (canManageTasks && categories?.some(c => c.id === cat.id)));
    }, [event.eventTasks, categories, showDoneTasks, canManageTasks]);
    
    const handleReorder = async (categoryId, reorderedTasks) => {
        const payload = { [categoryId || 0]: reorderedTasks.map(t => t.id) };
        try {
            await apiClient.post(`/events/${event.id}/tasks/reorder`, payload);
            addToast('Reihenfolge gespeichert.', 'success');
            onUpdate(); // Reload all event data to ensure consistency
        } catch (err) {
            addToast(`Fehler: ${err.message}`, 'error');
            onUpdate(); // Reload to revert optimistic update on failure
        }
    };
    
    const handleTaskAction = async (taskId, action, data) => {
        if (action === 'create') {
            try {
                const result = await apiClient.post(`/events/${event.id}/tasks`, { ...data, isImportant: data.isImportant || false });
                if (result.success) { addToast('Aufgabe erstellt.', 'success'); onUpdate(); }
                else { throw new Error(result.message); }
            } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        } else {
            try {
                const result = await apiClient.post(`/events/${event.id}/tasks/${taskId}/action`, { action, status: data });
                if (result.success) { addToast('Aktion erfolgreich.', 'success'); onUpdate(); }
                else { throw new Error(result.message); }
            } catch (err) { addToast(`Fehler: ${err.message}`, 'error'); }
        }
    };

    const renderMobileView = () => (
        <ScrollView>
            {tasksByCategory.map(category => (
                <AccordionSection key={category.id || 0} title={category.name}>
                    {category.tasks.flat().map(task => (
                        <TaskCard key={task.id} item={task} onOpenModal={openModal} onAction={handleTaskAction} user={user} isParticipant={isParticipant} canManageTasks={canManageTasks} userHasActiveTask={userHasActiveTask} styles={styles} colors={colors} />
                    ))}
                    {canManageTasks && (
                        <TouchableOpacity onPress={() => openModal('task')} style={styles.addTaskButton}>
                            <Icon name="plus" size={14} color={colors.textMuted} />
                            <Text style={{color: colors.textMuted}}> Neue Aufgabe in dieser Kategorie</Text>
                        </TouchableOpacity>
                    )}
                </AccordionSection>
            ))}
        </ScrollView>
    );

    const renderDesktopView = () => (
        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
            <View style={styles.kanbanBoard}>
                {tasksByCategory.map(category => (
                    <KanbanColumn key={category.id || 0} category={category} onOpenModal={openModal} onAction={handleTaskAction} handleReorder={handleReorder} styles={styles} colors={colors} user={user} isParticipant={isParticipant} canManageTasks={canManageTasks} userHasActiveTask={userHasActiveTask} />
                ))}
            </View>
        </ScrollView>
    );

    return (
        <View style={{flex: 1}}>
            <View style={styles.controlsContainer}>
                {canManageTasks &&
                    <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal('task')}>
                        <Icon name="plus" size={16} color={colors.white} />
                        <Text style={styles.buttonText}> Aufgabe</Text>
                    </TouchableOpacity>
                }
                <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                    <BouncyCheckbox isChecked={showDoneTasks} onPress={(isChecked) => setShowDoneTasks(isChecked)} size={20} fillColor={colors.primary} />
                    <Text style={{color: colors.text}}>Erledigte anzeigen</Text>
                </View>
            </View>
            
            {isLargeScreen ? renderDesktopView() : renderMobileView()}

            <TaskModal
                isOpen={modalState.type === 'task'}
                onClose={() => setModalState({type: null, data: null})}
                onSuccess={handleModalSuccess}
                event={event}
                task={modalState.data}
                allUsers={event.assignedAttendees}
                allTasks={event.eventTasks || []}
                categories={categories || []}
            />
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        controlsContainer: { padding: spacing.md, flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', borderBottomWidth: 1, borderColor: colors.border },
        kanbanBoard: { flexDirection: 'row', gap: spacing.md, padding: spacing.md, flexGrow: 1, alignSelf: 'flex-start' },
        kanbanColumn: { minWidth: 320, width: 320, backgroundColor: colors.background, borderRadius: borders.radius, padding: spacing.sm, display: 'flex', flexDirection: 'column' },
        columnTitle: { fontSize: typography.h4, fontWeight: 'bold', padding: spacing.sm, color: colors.heading },
        taskRow: { flexDirection: 'row', gap: spacing.sm },
        taskCardWrapper: { flex: 1 },
        taskCard: { backgroundColor: colors.surface, borderRadius: borders.radius, padding: spacing.md, marginBottom: spacing.sm, borderWidth: 1, borderColor: colors.border, ...shadows.sm },
        lockedTask: { opacity: 0.7, backgroundColor: colors.background },
        importantTask: { borderColor: colors.warning, borderWidth: 2 },
        inProgressTask: { backgroundColor: 'rgba(40, 167, 69, 0.1)' },
        doneTask: { backgroundColor: colors.background, opacity: 0.6 },
        doneTaskText: { textDecorationLine: 'line-through', color: colors.textMuted },
        taskName: { fontWeight: 'bold', marginBottom: spacing.xs, color: colors.text, fontSize: typography.body },
        displayOrder: { fontSize: typography.caption, color: colors.textMuted, fontWeight: 'bold' },
        dependencyText: { fontSize: typography.caption, color: colors.warning, fontStyle: 'italic', marginTop: 4 },
        addTaskButton: { padding: spacing.sm, borderRadius: 6, alignItems: 'center', justifyContent: 'center', flexDirection: 'row' },
        inlineAddContainer: { flexDirection: 'row', alignItems: 'center', marginTop: spacing.sm },
        inlineInput: { flex: 1, paddingVertical: 8, paddingHorizontal: 12, backgroundColor: colors.surface, borderRadius: 6, borderWidth: 1, borderColor: colors.border, color: colors.text },
        inlineButton: { padding: 8, marginLeft: 4 }
    });
};

export default TaskBoard;