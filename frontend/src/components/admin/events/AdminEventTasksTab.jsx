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

const TaskCard = ({ task, onOpenModal, styles, colors }) => (
    <TouchableOpacity style={styles.taskCard} onPress={() => onOpenModal('task', task)}>
        <Text style={styles.taskName}>{task.name}</Text>
        <View style={styles.assignees}>
            {task.assignedUsers?.slice(0, 3).map(user => (
                 <Icon key={user.id} name={user.profileIconClass?.replace('fa-', '') || 'user-circle'} solid size={18} style={{marginRight: -8}} color={colors.primary}/>
            ))}
            {task.assignedUsers?.length > 3 && <Text style={styles.moreAssignees}>+{task.assignedUsers.length - 3}</Text>}
        </View>
    </TouchableOpacity>
);

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

const AdminEventTasksTab = ({ event, onUpdate }) => {
    const theme = useAuthStore(state => state.theme);
    const styles = { ...getCommonStyles(theme), ...pageStyles(theme) };
    const colors = getThemeColors(theme);
    
    const categoriesApiCall = useCallback(() => apiClient.get(`/admin/events/${event.id}/task-categories`), [event.id]);
    const { data: categories, loading: categoriesLoading, reload: reloadCategories } = useApi(categoriesApiCall);

    const [modalState, setModalState] = useState({ type: null, data: null });

    const openModal = (type, data = null) => setModalState({ type, data });

    const handleSuccess = () => {
        setModalState({ type: null, data: null });
        reloadCategories();
        onUpdate();
    };

    const tasksByStatus = useMemo(() => {
        const grouped = { LOCKED: [], OPEN: [], IN_PROGRESS: [], DONE: [] };
        event.eventTasks?.forEach(task => {
            if (grouped[task.status]) {
                grouped[task.status].push(task);
            }
        });
        Object.values(grouped).forEach(arr => arr.sort((a, b) => a.displayOrder - b.displayOrder));
        return grouped;
    }, [event.eventTasks]);
    

    return (
        <View style={{flex: 1}}>
            <View style={{flexDirection: 'row', gap: spacing.sm, marginBottom: spacing.md}}>
                <TouchableOpacity style={[styles.button, styles.successButton]} onPress={() => openModal('task')}>
                    <Icon name="plus" size={16} color={colors.white} />
                    <Text style={styles.buttonText}> Aufgabe</Text>
                </TouchableOpacity>
                 <TouchableOpacity style={[styles.button, styles.secondaryButton]} onPress={() => openModal('category')}>
                    <Icon name="folder-plus" size={16} color={colors.text} />
                    <Text style={{color: colors.text}}> Kategorie</Text>
                </TouchableOpacity>
            </View>

            <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                <View style={styles.kanbanBoard}>
                    <KanbanColumn title="Gesperrt" tasks={tasksByStatus.LOCKED} onOpenModal={openModal} styles={styles} colors={colors} />
                    <KanbanColumn title="Offen" tasks={tasksByStatus.OPEN} onOpenModal={openModal} styles={styles} colors={colors} />
                    <KanbanColumn title="In Arbeit" tasks={tasksByStatus.IN_PROGRESS} onOpenModal={openModal} styles={styles} colors={colors} />
                    <KanbanColumn title="Erledigt" tasks={tasksByStatus.DONE} onOpenModal={openModal} styles={styles} colors={colors} />
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
        kanbanBoard: {
            flexDirection: 'row',
            gap: spacing.md,
            paddingBottom: spacing.md,
        },
        kanbanColumn: {
            width: 280,
            backgroundColor: colors.background,
            borderRadius: borders.radius,
            padding: spacing.sm,
            height: '100%',
        },
        columnTitle: {
            fontSize: typography.h4,
            fontWeight: 'bold',
            padding: spacing.sm,
            color: colors.heading,
        },
        taskCard: {
            backgroundColor: colors.surface,
            borderRadius: borders.radius,
            padding: spacing.sm,
            marginBottom: spacing.sm,
            borderWidth: 1,
            borderColor: colors.border,
            ...shadows.sm,
        },
        taskName: {
            fontWeight: 'bold',
            marginBottom: spacing.sm,
        },
        assignees: {
            flexDirection: 'row',
            marginTop: spacing.sm,
        },
        moreAssignees: {
            backgroundColor: colors.background,
            borderRadius: 12,
            paddingHorizontal: 6,
            marginLeft: 12,
            fontSize: typography.small,
        }
    });
};

export default AdminEventTasksTab;