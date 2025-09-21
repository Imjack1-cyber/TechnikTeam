import React, { useState, useCallback, useMemo } from 'react';
import { View, Text, StyleSheet, FlatList, TouchableOpacity, ActivityIndicator, Alert, TextInput, ScrollView } from 'react-native';
import useApi from '../../../hooks/useApi';
import apiClient from '../../../services/apiClient';
import TaskModal from '../../events/TaskModal';
import { useAuthStore } from '../../../store/authStore';
import { getCommonStyles } from '../../../styles/commonStyles';
import { getThemeColors, spacing, typography, borders, shadows } from '../../../styles/theme';
import Icon from 'react-native-vector-icons/FontAwesome5';
import AdminModal from '../../ui/AdminModal';
import { useRoute } from '@react-navigation/native';
import BouncyCheckbox from "react-native-bouncy-checkbox";

const CategoryModal = ({ isOpen, onClose, eventId, onSuccess }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = getCommonStyles(theme);
    const [name, setName] = useState('');
    const [isSubmitting, setIsSubmitting] = useState(false);

    const handleSubmit = async () => {
        setIsSubmitting(true);
        try {
            await apiClient.post(`/admin/events/${eventId}/task-categories`, { name });
            onSuccess();
        } catch (error) {
            // handle error
        } finally {
            setIsSubmitting(false);
        }
    };

    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title="Neue Kategorie erstellen" onSubmit={handleSubmit} isSubmitting={isSubmitting}>
            <Text style={styles.label}>Kategoriename</Text>
            <TextInput style={styles.input} value={name} onChangeText={setName} />
        </AdminModal>
    );
};

const DeleteCategoryModal = ({ isOpen, onClose, category, onSuccess }) => {
    const [isSubmitting, setIsSubmitting] = useState(false);
    
    const handleDelete = async () => {
        setIsSubmitting(true);
        try {
            await apiClient.delete(`/admin/events/${category.eventId}/task-categories/${category.id}`);
            onSuccess();
        } catch (error) {
            // handle error
        } finally {
            setIsSubmitting(false);
        }
    };
    
    return (
        <AdminModal isOpen={isOpen} onClose={onClose} title={`Kategorie "${category.name}" löschen?`} onSubmit={handleDelete} isSubmitting={isSubmitting} submitText="Löschen" submitButtonVariant="danger">
            <Text>Alle Aufgaben in dieser Kategorie werden als "Unkategorisiert" markiert. Möchten Sie fortfahren?</Text>
        </AdminModal>
    );
};

const TaskCard = ({ task, onOpenModal, styles, colors }) => {
    const getTaskCardStyle = () => {
        const needsHelp = task.status === 'IN_PROGRESS' && task.assignedUsers.length < task.requiredPersons;
        if (task.status === 'LOCKED') return styles.lockedTask;
        if (task.status === 'DONE') return styles.doneTask;
        if (task.isImportant || needsHelp) return styles.importantTask;
        if (task.status === 'IN_PROGRESS') return styles.inProgressTask;
        return {}; // Default for OPEN
    };
    const isDone = task.status === 'DONE';

    return (
        <TouchableOpacity style={[styles.taskCard, getTaskCardStyle()]} onPress={() => onOpenModal('task', task)}>
            <View style={{ flexDirection: 'row', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                <Text style={[styles.taskName, isDone && styles.doneTaskText]}>{task.name}</Text>
                <Text style={styles.displayOrder}>#{task.displayOrder}</Text>
            </View>
            <View style={styles.metaContainer}>
                <View style={styles.metaItem}>
                    <Icon name="users" size={12} color={colors.textMuted} />
                    <Text style={styles.metaText}>{task.assignedUsers.length} / {task.requiredPersons}</Text>
                </View>
            </View>
            {task.assignedUsers.length > 0 && (
                <View style={styles.assigneeList}>
                    <Text style={styles.assigneeText}>Aktiv: {task.assignedUsers.map(u => u.username).join(', ')}</Text>
                </View>
            )}
        </TouchableOpacity>
    );
};

const KanbanColumn = ({ title, tasks, onOpenModal, styles, colors }) => (
    <View style={styles.kanbanColumn}>
        <Text style={styles.columnTitle}>{title}</Text>
        <ScrollView>
            {tasks.map(task => (
                <TaskCard key={task.id} task={task} onOpenModal={onOpenModal} styles={styles} colors={colors} />
            ))}
        </ScrollView>
    </View>
);

const AdminEventTasksTab = () => {
    const route = useRoute();
    const { event, onUpdate } = route.params;
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    
    const categoriesApiCall = useCallback(() => apiClient.get(`/admin/events/${event.id}/task-categories`), [event.id]);
    const { data: categories, loading: categoriesLoading, reload: reloadCategories } = useApi(categoriesApiCall);

    const [modalState, setModalState] = useState({ type: null, data: null });
    const [showDoneTasks, setShowDoneTasks] = useState(false);

    const openModal = (type, data = null) => setModalState({ type, data });

    const handleSuccess = () => {
        setModalState({ type: null, data: null });
        reloadCategories();
        onUpdate();
    };

    const tasksByCategory = useMemo(() => {
        const byCategory = {};

        const allCategories = categories ? [...categories, {id: 0, name: 'Unkategorisiert'}] : [{id: 0, name: 'Unkategorisiert'}];
        allCategories.forEach(cat => {
            byCategory[cat.id] = { ...cat, tasks: [] };
        });

        const filteredTasks = event.eventTasks?.filter(task => showDoneTasks || task.status !== 'DONE');

        filteredTasks?.forEach(task => {
            const categoryId = task.categoryId || 0;
            if (byCategory[categoryId]) {
                byCategory[categoryId].tasks.push(task);
            }
        });

        Object.values(byCategory).forEach(cat => {
            cat.tasks.sort((a, b) => a.displayOrder - b.displayOrder);
        });

        return Object.values(byCategory).filter(cat => cat.tasks.length > 0);
    }, [event.eventTasks, categories, showDoneTasks]);
    

    return (
        <View style={{flex: 1}}>
            <View style={{flexDirection: 'row', justifyContent: 'space-between', alignItems: 'center', marginBottom: spacing.md}}>
                <View style={{flexDirection: 'row', gap: spacing.sm}}>
                    <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal('task')}>
                        <Icon name="plus" size={16} color={colors.white} />
                        <Text style={styles.buttonText}> Aufgabe</Text>
                    </TouchableOpacity>
                     <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal('category')}>
                        <Icon name="folder-plus" size={16} color={colors.text} />
                        <Text style={{color: colors.text}}> Kategorie</Text>
                    </TouchableOpacity>
                </View>
                 <View style={{ flexDirection: 'row', alignItems: 'center' }}>
                    <BouncyCheckbox isChecked={showDoneTasks} onPress={(isChecked) => setShowDoneTasks(isChecked)} size={20} />
                    <Text>Erledigte anzeigen</Text>
                </View>
            </View>

            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                <View style={styles.kanbanBoard}>
                    {tasksByCategory.map(category => (
                        <KanbanColumn key={category.id} title={category.name} tasks={category.tasks} onOpenModal={openModal} styles={styles} colors={colors} />
                    ))}
                </View>
            </ScrollView>

            <TaskModal
                isOpen={modalState.type === 'task'}
                onClose={() => setModalState({type: null, data: null})}
                onSuccess={handleSuccess}
                event={event}
                task={modalState.data}
                allUsers={event.assignedAttendees}
                categories={categories || []}
            />
            <CategoryModal
                isOpen={modalState.type === 'category'}
                onClose={() => setModalState({type: null, data: null})}
                onSuccess={handleSuccess}
                eventId={event.id}
            />
             {modalState.type === 'deleteCategory' && (
                <DeleteCategoryModal
                    isOpen={true}
                    onClose={() => setModalState({type: null, data: null})}
                    onSuccess={handleSuccess}
                    category={modalState.data}
                />
             )}
        </View>
    );
};

const pageStyles = (theme) => {
    const colors = getThemeColors(theme);
    return StyleSheet.create({
        kanbanBoard: { flexDirection: 'row', gap: spacing.md, paddingBottom: spacing.md },
        kanbanColumn: { width: 280, backgroundColor: colors.background, borderRadius: borders.radius, padding: spacing.sm, height: '100%'},
        columnTitle: { fontSize: typography.h4, fontWeight: 'bold', padding: spacing.sm, color: colors.heading },
        taskCard: { backgroundColor: colors.surface, borderRadius: borders.radius, padding: spacing.md, marginBottom: spacing.sm, borderWidth: 1, borderColor: colors.border, ...shadows.sm },
        lockedTask: { backgroundColor: colors.background, opacity: 0.7 },
        importantTask: { backgroundColor: 'rgba(255, 193, 7, 0.1)', borderColor: colors.warning },
        inProgressTask: { backgroundColor: 'rgba(40, 167, 69, 0.1)', borderColor: colors.success },
        doneTask: { backgroundColor: colors.background, opacity: 0.6 },
        doneTaskText: { textDecorationLine: 'line-through', color: colors.textMuted },
        taskName: { fontWeight: 'bold', marginBottom: spacing.xs, color: colors.text, fontSize: typography.body },
        displayOrder: { fontSize: typography.caption, color: colors.textMuted, fontWeight: 'bold' },
        metaContainer: { flexDirection: 'row', gap: spacing.md, marginVertical: spacing.sm },
        metaItem: { flexDirection: 'row', alignItems: 'center', gap: spacing.xs },
        metaText: { color: colors.textMuted, fontSize: typography.small },
        assigneeList: { marginTop: spacing.sm, borderTopWidth: 1, borderColor: colors.border, paddingTop: spacing.sm },
        assigneeText: { color: colors.textMuted, fontSize: typography.small },
    });
};

export default AdminEventTasksTab;